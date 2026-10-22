import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAdminOverview } from '../api/admin'
import type { AdminOverview } from '../api/admin'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'

const metricLabels: Array<[keyof AdminOverview, string]> = [
  ['totalUsers', 'Registered users'],
  ['donors', 'Donor accounts'],
  ['recipients', 'Recipient accounts'],
  ['openRequests', 'Open requests'],
  ['fulfilledRequests', 'Accepted requests'],
  ['conversations', 'Private conversations'],
  ['pendingDonorReviews', 'Donor profiles pending review'],
]

export function AdminPage() {
  const { logout } = useAuth()
  const [overview, setOverview] = useState<AdminOverview | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAdminOverview()
      .then(setOverview)
      .catch((reason: unknown) => setError(getApiErrorMessage(reason)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div><p className="eyebrow">BloodBond operations</p><h1>Admin overview</h1></div>
        <div className="header-actions"><Link to="/dashboard">Dashboard</Link><button type="button" onClick={logout}>Sign out</button></div>
      </header>
      <section className="admin-overview">
        <p className="note">Aggregate platform counts only. Donor profile review is not medical verification or clinical clearance.</p>
        {loading ? <p>Loading overview…</p> : overview ? (
          <dl className="admin-metrics">
            {metricLabels.map(([key, label]) => (
              <div className="admin-metric" key={key}><dt>{label}</dt><dd>{overview[key]}</dd></div>
            ))}
          </dl>
        ) : null}
        {error ? <p className="assistant-error" role="alert">{error}</p> : null}
      </section>
    </main>
  )
}
