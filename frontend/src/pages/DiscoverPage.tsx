import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createChatRoom } from '../api/chat'
import { getApiErrorMessage } from '../api/client'
import { searchNearbyDonors } from '../api/matching'
import type { DonorMatch } from '../api/matching'
import { getRecipientProfile } from '../api/profiles'
import type { RecipientProfile } from '../api/profiles'
import { useAuth } from '../auth/useAuth'
import { DonorMap } from '../components/DonorMap'

const bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

export function DiscoverPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState<RecipientProfile | null>(null)
  const [bloodGroup, setBloodGroup] = useState('O+')
  const [radiusKm, setRadiusKm] = useState(50)
  const [donors, setDonors] = useState<DonorMatch[]>([])
  const [selectedDonor, setSelectedDonor] = useState<DonorMatch | null>(null)
  const [loading, setLoading] = useState(true)
  const [startingChat, setStartingChat] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getRecipientProfile()
      .then(setProfile)
      .catch((reason: unknown) => setError(getApiErrorMessage(reason)))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!profile || profile.approxLat === null || profile.approxLng === null) return
    searchNearbyDonors({ bloodGroup, radiusKm, lat: profile.approxLat, lng: profile.approxLng })
      .then((matches) => {
        setDonors(matches)
        setSelectedDonor(null)
        setError(null)
      })
      .catch((reason: unknown) => setError(getApiErrorMessage(reason)))
  }, [bloodGroup, profile, radiusKm])

  const selectDonor = useCallback((donor: DonorMatch) => setSelectedDonor(donor), [])

  const startChat = async () => {
    if (!profile || !selectedDonor || profile.approxLat === null || profile.approxLng === null) return
    setStartingChat(true)
    setError(null)
    try {
      const room = await createChatRoom({
        donorProfileId: selectedDonor.donorProfileId,
        bloodGroup,
        city: profile.city ?? '',
        approxLat: profile.approxLat,
        approxLng: profile.approxLng,
      })
      navigate(`/chat?roomId=${room.id}`)
    } catch (reason) {
      setError(getApiErrorMessage(reason))
    } finally {
      setStartingChat(false)
    }
  }

  if (loading) return <main className="dashboard-shell"><p>Loading donor matches...</p></main>

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div><p className="eyebrow">Nearby donors</p><h1>Find a match</h1></div>
        <div className="header-actions"><Link to="/dashboard">Dashboard</Link><button type="button" onClick={logout}>Logout</button></div>
      </header>
      <section className="discovery-layout">
        <div className="dashboard-card discovery-controls">
          <h2>Search by blood group and distance</h2>
          <div className="filter-row">
            <label>Blood group
              <select value={bloodGroup} onChange={(event) => setBloodGroup(event.target.value)}>
                {bloodGroups.map((group) => <option key={group}>{group}</option>)}
              </select>
            </label>
            <label>Within
              <select value={radiusKm} onChange={(event) => setRadiusKm(Number(event.target.value))}>
                {[10, 25, 50, 100, 250].map((radius) => <option key={radius} value={radius}>{radius} km</option>)}
              </select>
            </label>
          </div>
          {!profile || profile.approxLat === null || profile.approxLng === null ? (
            <p role="alert">Add your approximate location to your <Link to="/recipient/profile">recipient profile</Link> to find nearby donors.</p>
          ) : (
            <p className="note">Donor pins show approximate locations only. Exact addresses and contact details are never shown.</p>
          )}
          {error ? <p role="alert">{error}</p> : null}
          {profile && profile.approxLat !== null && profile.approxLng !== null ? (
            <DonorMap latitude={profile.approxLat} longitude={profile.approxLng} donors={donors} onSelectDonor={selectDonor} />
          ) : null}
          <p className="note">{donors.length} available donor{donors.length === 1 ? '' : 's'} in this search.</p>
          {selectedDonor ? (
            <div className="selected-donor">
              <div><strong>{selectedDonor.donorName}</strong><span>{selectedDonor.bloodGroup} · {selectedDonor.distanceKm} km away{selectedDonor.city ? ` · ${selectedDonor.city}` : ''}</span></div>
              <button type="button" onClick={startChat} disabled={startingChat}>{startingChat ? 'Starting chat...' : 'Start a private chat'}</button>
            </div>
          ) : <p className="note">Select a donor pin on the map to start a private chat.</p>}
        </div>
      </section>
    </main>
  )
}
