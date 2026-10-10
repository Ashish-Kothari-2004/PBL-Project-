import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function LandingPage() {
  const { user } = useAuth()

  return (
    <main className="landing-shell">
      <section className="landing-content">
        <p className="eyebrow">BloodBond</p>
        <h1>Every connection can make a difference.</h1>
        <p>Join a community connecting blood donors and people who need support.</p>
        {user ? (
          <div className="landing-actions">
            <Link className="button-link" to="/dashboard">Go to your dashboard</Link>
            {user.role === 'DONOR' ? <Link className="text-link" to="/donor/profile">Donor profile</Link> : null}
            {user.role === 'RECIPIENT' ? <Link className="text-link" to="/recipient/profile">Recipient profile</Link> : null}
          </div>
        ) : (
          <div className="landing-actions">
            <Link className="button-link" to="/register">Create an account</Link>
            <Link className="text-link" to="/login">Sign in</Link>
          </div>
        )}
      </section>
    </main>
  )
}
