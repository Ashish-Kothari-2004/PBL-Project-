import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { captureApproximateLocation } from '../api/geolocation'
import { getApiErrorMessage } from '../api/client'
import { getRecipientProfile, updateRecipientProfile } from '../api/profiles'
import type { RecipientProfileInput } from '../api/profiles'
import { useAuth } from '../auth/useAuth'

const initialProfile: RecipientProfileInput = { city: '', approxLat: null, approxLng: null }

export function RecipientProfilePage() {
  const { logout } = useAuth()
  const [profile, setProfile] = useState(initialProfile)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getRecipientProfile()
      .then((saved) => setProfile({
        city: saved.city ?? '',
        approxLat: saved.approxLat,
        approxLng: saved.approxLng,
      }))
      .catch((reason: unknown) => setError(getApiErrorMessage(reason)))
      .finally(() => setLoading(false))
  }, [])

  const captureLocation = async () => {
    setError(null)
    try {
      const location = await captureApproximateLocation()
      setProfile((current) => ({ ...current, ...location }))
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    }
  }

  const saveProfile = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      await updateRecipientProfile(profile)
      setMessage('Your recipient profile has been saved.')
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <main className="auth-shell"><p>Loading profile...</p></main>

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <Link className="dashboard-brand" to="/"><span className="brand-mark" aria-hidden="true">B</span><span>bloodbond</span></Link>
        <div className="header-actions"><Link to="/dashboard">Dashboard</Link><Link to="/discover">Find donors</Link><button type="button" onClick={logout}>Sign out</button></div>
      </header>
      <section className="profile-layout">
        <p className="eyebrow">Recipient profile</p>
        <h2>Let nearby find you.</h2>
        <p className="note">Your location stays approximate. It helps us show donors near you without revealing an exact address.</p>
        <form className="auth-form" onSubmit={saveProfile}>
          {error ? <p role="alert">{error}</p> : null}
          {message ? <p role="status">{message}</p> : null}
          <label>City
            <input value={profile.city} onChange={(event) => setProfile({ ...profile, city: event.target.value })} />
          </label>
          <button type="button" className="secondary-button" onClick={captureLocation}>Use approximate location</button>
          <p className="note">{profile.approxLat === null ? 'Location not set.' : `Approximate location: ${profile.approxLat}, ${profile.approxLng}`}</p>
          <button className="primary-button" type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save profile'}</button>
        </form>
      </section>
    </main>
  )
}
