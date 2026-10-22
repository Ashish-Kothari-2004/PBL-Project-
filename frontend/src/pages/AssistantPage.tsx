import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { clearAssistantHistory, getAssistantHistory, sendAssistantMessage } from '../api/assistant'
import type { AssistantMessage } from '../api/assistant'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'

const prompts = [
  'How does BloodBond help donors and recipients?',
  'What does blood group compatibility mean in general?',
  'What should I expect at a blood donation appointment?',
]

export function AssistantPage() {
  const { user, logout } = useAuth()
  const [messages, setMessages] = useState<AssistantMessage[]>([])
  const [content, setContent] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [clearing, setClearing] = useState(false)
  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let active = true
    getAssistantHistory()
      .then((history) => { if (active) setMessages(history) })
      .catch((reason: unknown) => { if (active) setError(getApiErrorMessage(reason)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [messages])

  const send = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const text = content.trim()
    if (!text || sending) return
    const userMessage: AssistantMessage = {
      id: -Date.now(),
      role: 'user',
      content: text,
      createdAt: new Date().toISOString(),
    }
    setMessages((current) => [...current, userMessage])
    setContent('')
    setSending(true)
    setError(null)
    try {
      const exchange = await sendAssistantMessage(text)
      setMessages((current) => [
        ...current.filter((message) => message.id !== userMessage.id),
        exchange.userMessage,
        exchange.assistantMessage,
      ])
    } catch (reason) {
      setMessages((current) => current.filter((message) => message.id !== userMessage.id))
      setContent(text)
      setError(getApiErrorMessage(reason))
    } finally {
      setSending(false)
    }
  }

  const clearHistory = async () => {
    setClearing(true)
    setError(null)
    try {
      await clearAssistantHistory()
      setMessages([])
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setClearing(false)
    }
  }

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div><p className="eyebrow">A little guidance, when you need it</p><h1>BloodBond assistant</h1></div>
        <div className="header-actions">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/requests">Blood requests</Link>
          <button type="button" onClick={logout}>Sign out</button>
        </div>
      </header>
      <section className="assistant-layout">
        <div className="assistant-heading">
          <p className="assistant-kicker">Here to help, {user?.role === 'DONOR' ? 'donor' : 'recipient'}.</p>
          <h2>Ask about donations, the app, or the next step.</h2>
          <p className="assistant-disclosure">
            Your assistant messages are saved to this account and sent to Groq to generate replies.
            Only your BloodBond role and this assistant conversation are used for context—not profile
            details or private donor-recipient chats. Avoid sharing names, contact details, exact
            locations, or sensitive medical information.
          </p>
          <p className="assistant-safety">
            Educational information only. Blood groups, compatibility, and donor eligibility must be
            confirmed by qualified blood-bank staff. This assistant cannot assess an individual case.
            For urgent needs, contact your hospital or local emergency services.
          </p>
          <button className="assistant-clear" type="button" onClick={clearHistory} disabled={clearing || messages.length === 0}>
            {clearing ? 'Deleting…' : 'Delete my assistant history'}
          </button>
        </div>
        <section className="assistant-panel" aria-label="Assistant conversation">
          <div className="assistant-transcript" aria-live="polite">
            {loading ? <p className="note">Loading your conversation…</p> : null}
            {!loading && messages.length === 0 ? (
              <div className="assistant-welcome">
                <span className="assistant-mark" aria-hidden="true">B</span>
                <h3>What can I help you understand?</h3>
                <p>Start with a question, or choose one of these.</p>
                <div className="assistant-prompts">
                  {prompts.map((prompt) => (
                    <button key={prompt} type="button" onClick={() => setContent(prompt)}>{prompt}</button>
                  ))}
                </div>
              </div>
            ) : messages.map((message) => (
              <article className={`assistant-message ${message.role}`} key={message.id}>
                <span>{message.role === 'assistant' ? 'BloodBond' : 'You'}</span>
                <p>{message.content}</p>
              </article>
            ))}
            {sending ? <p className="assistant-typing" role="status">Putting together a careful answer…</p> : null}
            <div ref={endRef} />
          </div>
          <form className="assistant-compose" onSubmit={send}>
            <label className="visually-hidden" htmlFor="assistant-message">Ask the BloodBond assistant</label>
            <textarea id="assistant-message" value={content} onChange={(event) => setContent(event.target.value)} maxLength={2000} rows={2} placeholder="Ask a question…" required />
            <button type="submit" disabled={sending || !content.trim()}>{sending ? 'Sending…' : 'Send'}</button>
          </form>
          {error ? <p className="assistant-error" role="alert">{error}</p> : null}
        </section>
      </section>
    </main>
  )
}
