import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { cancelBloodRequest, createBloodRequest, getMyBloodRequests, getOpenBloodRequests, respondToBloodRequest } from '../api/requests'
import type { BloodRequest } from '../api/requests'
import { getRecipientProfile } from '../api/profiles'
import { useAuth } from '../auth/useAuth'

const bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

export function RequestsPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [requests, setRequests] = useState<BloodRequest[]>([])
  const [bloodGroup, setBloodGroup] = useState('')
  const [city, setCity] = useState('')
  const [radiusKm, setRadiusKm] = useState(50)
  const [shareApproximateCoordinates, setShareApproximateCoordinates] = useState(false)
  const [coordinates, setCoordinates] = useState<{ lat: number | null; lng: number | null }>({ lat: null, lng: null })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    try {
      const loaded = user?.role === 'DONOR' ? await getOpenBloodRequests() : await getMyBloodRequests()
      setRequests(loaded)
      setError(null)
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    let active = true
    const load = async () => {
      try {
        const loaded = user?.role === 'DONOR' ? await getOpenBloodRequests() : await getMyBloodRequests()
        if (active) {
          setRequests(loaded)
          setError(null)
        }
      } catch (reason) {
        if (active) setError(getApiErrorMessage(reason))
      } finally {
        if (active) setLoading(false)
      }
    }
    void load()
    return () => { active = false }
  }, [user?.role])

  useEffect(() => {
    if (user?.role !== 'RECIPIENT') return
    getRecipientProfile().then((profile) => {
      setCity(profile.city ?? '')
      setCoordinates({ lat: profile.approxLat, lng: profile.approxLng })
    }).catch((reason: unknown) => setError(getApiErrorMessage(reason)))
  }, [user?.role])

  const submitRequest = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createBloodRequest({
        bloodGroup,
        city,
        approxLat: shareApproximateCoordinates ? coordinates.lat : null,
        approxLng: shareApproximateCoordinates ? coordinates.lng : null,
        radiusKm,
      })
      await refresh()
      setBloodGroup('')
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setSaving(false)
    }
  }

  const respond = async (request: BloodRequest, decision: 'ACCEPTED' | 'DECLINED') => {
    setBusyId(request.id)
    setError(null)
    try {
      const result = await respondToBloodRequest(request.id, decision)
      if (decision === 'ACCEPTED' && result.chatRoom) {
        navigate(`/chat?roomId=${result.chatRoom.id}`)
        return
      }
      await refresh()
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setBusyId(null)
    }
  }

  const cancel = async (request: BloodRequest) => {
    setBusyId(request.id)
    setError(null)
    try {
      await cancelBloodRequest(request.id)
      await refresh()
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div><p className="eyebrow">{user?.role === 'DONOR' ? 'A way to show up' : 'Help people find you'}</p><h1>Blood requests</h1></div>
        <div className="header-actions"><Link to="/dashboard">Dashboard</Link><Link to="/assistant">Assistant</Link><button type="button" onClick={logout}>Sign out</button></div>
      </header>

      {user?.role === 'RECIPIENT' ? (
        <section className="request-layout">
          <form className="request-form" onSubmit={submitRequest}>
            <p className="eyebrow">Create a request</p>
            <h2>Let nearby donors know what you need.</h2>
            <p className="note">This shares your selected blood group and approximate city with donors in the matching area. Approximate coordinates are optional and used only to filter by your chosen radius. It does not confirm compatibility or replace a hospital or blood bank.</p>
            <label>Blood group
              <select value={bloodGroup} onChange={(event) => setBloodGroup(event.target.value)} required>
                <option value="">Choose a blood group</option>
                {bloodGroups.map((group) => <option key={group}>{group}</option>)}
              </select>
            </label>
            <label>City <input value={city} onChange={(event) => setCity(event.target.value)} maxLength={120} placeholder="Approximate city" /></label>
            <label className="checkbox-label"><input type="checkbox" checked={shareApproximateCoordinates} onChange={(event) => setShareApproximateCoordinates(event.target.checked)} disabled={coordinates.lat === null || coordinates.lng === null} />Share saved approximate coordinates for radius filtering</label>
            <label>Search radius
              <select value={radiusKm} onChange={(event) => setRadiusKm(Number(event.target.value))}>
                {[10, 25, 50, 100, 250].map((radius) => <option key={radius} value={radius}>{radius} km</option>)}
              </select>
            </label>
            <button className="primary-button" type="submit" disabled={saving}>{saving ? 'Publishing…' : 'Publish blood request'}</button>
          </form>
          <section className="request-list" aria-label="Your blood requests">
            <div className="request-list-heading"><div><p className="eyebrow">Your activity</p><h2>Your requests</h2></div><button type="button" onClick={() => void refresh()}>Refresh</button></div>
            {loading ? <p className="note">Loading requests…</p> : requests.length === 0 ? <p className="note">You haven’t posted a request yet.</p> : requests.map((request) => (
              <article className="request-row" key={request.id}>
                <div><strong>{request.bloodGroup}</strong><span>{request.city || 'Location not specified'} · {request.radiusKm} km</span><small>Posted {new Date(request.createdAt).toLocaleDateString()}</small></div>
                <span className={`request-status ${request.status.toLowerCase()}`}>{request.status}</span>
                {request.status === 'OPEN' ? <button type="button" disabled={busyId === request.id} onClick={() => void cancel(request)}>{busyId === request.id ? 'Cancelling…' : 'Cancel request'}</button> : null}
                {request.responses.length ? <p className="request-responses">{request.responses.length} donor response{request.responses.length === 1 ? '' : 's'}: {request.responses.map((item) => item.status.toLowerCase()).join(', ')}</p> : null}
              </article>
            ))}
          </section>
        </section>
      ) : (
        <section className="request-list donor-requests" aria-label="Open blood requests">
          <div className="request-list-heading"><div><p className="eyebrow">Requests from recipients</p><h2>Open near the community</h2></div><button type="button" onClick={() => void refresh()}>Refresh</button></div>
          <p className="note">Requests are filtered by the recipient’s radius when they shared approximate coordinates, or by matching city otherwise. A request is not a clinical compatibility or eligibility decision. Only accept if you are comfortable starting a conversation; your eligibility must be assessed by a blood-bank professional.</p>
          {loading ? <p className="note">Loading requests…</p> : requests.length === 0 ? <p className="note">There are no open requests right now.</p> : requests.map((request) => (
            <article className="request-row donor-request-row" key={request.id}>
              <div><strong>{request.bloodGroup}</strong><span>{request.city || 'Location not specified'} · within {request.radiusKm} km</span><small>Posted {new Date(request.createdAt).toLocaleDateString()}</small></div>
              <div className="request-buttons">
                <button type="button" disabled={busyId === request.id} onClick={() => void respond(request, 'DECLINED')}>Decline</button>
                <button className="primary-button" type="button" disabled={busyId === request.id} onClick={() => void respond(request, 'ACCEPTED')}>{busyId === request.id ? 'Saving…' : 'Accept & start chat'}</button>
              </div>
            </article>
          ))}
        </section>
      )}
      {error ? <p className="assistant-error" role="alert">{error}</p> : null}
    </main>
  )
}
