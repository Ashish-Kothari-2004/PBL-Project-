import { useCallback, useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import type { Client } from '@stomp/stompjs'
import { getApiErrorMessage } from '../api/client'
import { connectChatSocket, getChatMessages, getChatRooms, publishChatMessage } from '../api/chat'
import type { ChatMessage, ChatRoom } from '../api/chat'
import { useAuth } from '../auth/useAuth'

export function ChatPage() {
  const { user, token, logout } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const [rooms, setRooms] = useState<ChatRoom[]>([])
  const [content, setContent] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [connected, setConnected] = useState(false)
  const socketClientRef = useRef<Client | null>(null)
  const initialRoomIdRef = useRef(Number(searchParams.get('roomId')))
  const roomId = Number(searchParams.get('roomId')) || null
  const selectedRoom = rooms.find((room) => room.id === roomId) ?? null
  const [messageState, setMessageState] = useState<{ roomId: number | null; messages: ChatMessage[] }>({
    roomId: null,
    messages: [],
  })
  const messages = messageState.roomId === roomId ? messageState.messages : []

  useEffect(() => {
    let active = true
    const refreshRooms = async () => {
      try {
        const loadedRooms = await getChatRooms()
        if (!active) return
        setRooms(loadedRooms)
        const requestedRoom = initialRoomIdRef.current
        if (loadedRooms.length > 0 && !loadedRooms.some((room) => room.id === requestedRoom)) {
          setSearchParams({ roomId: String(loadedRooms[0].id) }, { replace: true })
        }
        setError(null)
      } catch (reason) {
        if (active) setError(getApiErrorMessage(reason))
      } finally {
        if (active) setLoading(false)
      }
    }

    void refreshRooms()
    return () => {
      active = false
    }
  }, [setSearchParams])

  const loadMessages = useCallback(async (id: number) => {
    try {
      const loadedMessages = await getChatMessages(id)
      setMessageState((current) => {
        const previousMessages = current.roomId === id ? current.messages : []
        const merged = new Map(previousMessages.map((message) => [message.id, message]))
        loadedMessages.forEach((message) => merged.set(message.id, message))
        return { roomId: id, messages: [...merged.values()].sort((left, right) => left.id - right.id) }
      })
      setError(null)
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    }
  }, [])

  useEffect(() => {
    if (!roomId) return
    const timeoutId = window.setTimeout(() => void loadMessages(roomId), 0)
    return () => window.clearTimeout(timeoutId)
  }, [loadMessages, roomId])

  useEffect(() => {
    if (!token) return
    const client = connectChatSocket(token, roomId, {
      onMessage: (message) => {
        setMessageState((current) => {
          const previousMessages = current.roomId === roomId ? current.messages : []
          if (previousMessages.some((item) => item.id === message.id)) return current
          return {
            roomId,
            messages: [...previousMessages, message].sort((left, right) => left.id - right.id),
          }
        })
      },
      onRoom: (room) => {
        setRooms((current) => current.some((item) => item.id === room.id) ? current : [room, ...current])
        if (!roomId) setSearchParams({ roomId: String(room.id) })
      },
      onConnectionChange: setConnected,
      onError: setError,
    })
    socketClientRef.current = client

    return () => {
      setConnected(false)
      socketClientRef.current = null
      void client.deactivate()
    }
  }, [roomId, setSearchParams, token])

  const submitMessage = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!roomId || !content.trim() || !connected) return
    setSending(true)
    setError(null)
    try {
      const client = socketClientRef.current
      if (!client?.connected) throw new Error('Chat is reconnecting. Please try again in a moment.')
      publishChatMessage(client, roomId, content.trim())
      setContent('')
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setSending(false)
    }
  }

  if (loading) return <main className="dashboard-shell"><p>Loading conversations...</p></main>

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div><p className="eyebrow">Private conversations</p><h1>Your chats</h1></div>
        <div className="header-actions">
          {user?.role === 'RECIPIENT' ? <Link to="/discover">Find donors</Link> : null}
          <Link to="/dashboard">Dashboard</Link><button type="button" onClick={logout}>Logout</button>
        </div>
      </header>
      <section className="chat-layout">
        <aside className="chat-list dashboard-card" aria-label="Conversations">
          <h2>Conversations</h2>
          {rooms.length === 0 ? <p className="note">No conversations yet.</p> : rooms.map((room) => (
            <button
              className={`chat-room-option${room.id === roomId ? ' active' : ''}`}
              key={room.id}
              type="button"
              onClick={() => setSearchParams({ roomId: String(room.id) })}
            >
              <strong>{room.counterpartName}</strong>
              <span>{room.bloodGroup} blood request{room.city ? ` · ${room.city}` : ''}</span>
            </button>
          ))}
        </aside>
        <section className="chat-conversation dashboard-card" aria-label="Conversation messages">
          {selectedRoom ? (
            <>
              <div className="chat-heading">
                <h2>{selectedRoom.counterpartName}</h2>
                <p className="note">{selectedRoom.bloodGroup} blood request{selectedRoom.city ? ` · ${selectedRoom.city}` : ''}</p>
                <p className={`socket-status${connected ? ' is-connected' : ''}`} role="status">
                  <span aria-hidden="true" />{connected ? 'Live connection' : 'Connecting to chat…'}
                </p>
              </div>
              <div className="chat-messages" aria-live="polite">
                {messages.length === 0 ? <p className="note">Start the conversation. Please avoid sending sensitive medical or identity information.</p> : messages.map((message) => (
                  <article className={`chat-message${message.senderId === user?.id ? ' own' : ''}`} key={message.id}>
                    <strong>{message.senderName}</strong>
                    <p>{message.content}</p>
                    <time dateTime={message.createdAt}>{new Date(message.createdAt).toLocaleString()}</time>
                  </article>
                ))}
              </div>
              <form className="chat-compose" onSubmit={submitMessage}>
                <label className="visually-hidden" htmlFor="chat-message">Message</label>
                <textarea id="chat-message" value={content} onChange={(event) => setContent(event.target.value)} maxLength={2000} placeholder="Write a message..." required />
                <button type="submit" disabled={sending || !connected || !content.trim()}>{sending ? 'Sending...' : 'Send'}</button>
              </form>
            </>
          ) : <p className="note">Choose a conversation to view messages.</p>}
          {error ? <p role="alert">{error}</p> : null}
        </section>
      </section>
    </main>
  )
}
