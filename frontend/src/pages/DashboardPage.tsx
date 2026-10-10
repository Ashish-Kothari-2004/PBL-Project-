import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function DashboardPage() {
  const { user, logout } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Welcome</p>
          <h1>{user.name}</h1>
        </div>
        <button type="button" onClick={logout}>Logout</button>
      </header>

      <section className="dashboard-card">
        <h2>Role</h2>
        <p>{user.role}</p>
        {user.role === 'DONOR' ? <p><Link to="/donor/profile">Manage donor profile</Link></p> : null}
        {user.role === 'RECIPIENT' ? <p><Link to="/recipient/profile">Manage recipient profile</Link></p> : null}
        <p className="note">
          Medical safety note: Eligibility and blood group information are self-reported reminders only, not medical clearance.
        </p>
      </section>
    </main>
  )
}
