import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { captureApproximateLocation } from '../api/geolocation'
import { getApiErrorMessage } from '../api/client'
import { getDonorProfile, updateDonorProfile } from '../api/profiles'
import type { DonorProfileInput } from '../api/profiles'
import { useAuth } from '../auth/useAuth'

const maxBirthDate = (() => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 18)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})()

const initialProfile: DonorProfileInput = {
  bloodGroup: 'O+',
  dateOfBirth: '',
  city: '',
  approxLat: null,
  approxLng: null,
  isAvailable: true,
}

export function DonorProfilePage() {
  const { logout } = useAuth()
  const [profile, setProfile] = useState(initialProfile)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getDonorProfile()
      .then((saved) => setProfile({
        bloodGroup: saved.bloodGroup || 'O+',
        dateOfBirth: saved.dateOfBirth ?? '',
        city: saved.city ?? '',
        approxLat: saved.approxLat,
        approxLng: saved.approxLng,
        isAvailable: saved.isAvailable,
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
      await updateDonorProfile(profile)
      setMessage('Your donor profile has been saved.')
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
        <div><p className="eyebrow">Donor profile</p><h1>Your details</h1></div>
        <div className="header-actions"><Link to="/dashboard">Dashboard</Link><button type="button" onClick={logout}>Logout</button></div>
      </header>
      <section className="dashboard-card">
        <form className="auth-form" onSubmit={saveProfile}>
          {error ? <p role="alert">{error}</p> : null}
          {message ? <p role="status">{message}</p> : null}
          <label>Blood group
            <select value={profile.bloodGroup} onChange={(event) => setProfile({ ...profile, bloodGroup: event.target.value })}>
              {['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'].map((group) => <option key={group}>{group}</option>)}
            </select>
          </label>
          <label>Date of birth (donors must be at least 18)
            <input type="date" value={profile.dateOfBirth} max={maxBirthDate} onChange={(event) => setProfile({ ...profile, dateOfBirth: event.target.value })} required />
          </label>
          <label>City
            <input value={profile.city} onChange={(event) => setProfile({ ...profile, city: event.target.value })} />
          </label>
          <button type="button" className="secondary-button" onClick={captureLocation}>Use approximate location</button>
          <p className="note">{profile.approxLat === null ? 'Location not set.' : `Approximate location: ${profile.approxLat}, ${profile.approxLng}`}</p>
          <label className="checkbox-label"><input type="checkbox" checked={profile.isAvailable} onChange={(event) => setProfile({ ...profile, isAvailable: event.target.checked })} /> Available to donate</label>
          <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save profile'}</button>
        </form>
      </section>
    </main>
  )
}
