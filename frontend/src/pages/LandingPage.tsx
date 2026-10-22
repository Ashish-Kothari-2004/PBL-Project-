import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function LandingPage() {
  const { user } = useAuth()

  return (
    <main className="landing-shell">
      <nav className="site-nav" aria-label="Main navigation">
        <Link className="brand-link" to="/">
          <span className="brand-mark" aria-hidden="true">B</span>
          <span>bloodbond</span>
        </Link>
        <div className="site-nav-links">
          <a className="nav-secondary" href="#how-it-works">How it works</a>
          {user ? (
            <Link className="nav-cta" to="/dashboard">My dashboard <span aria-hidden="true">↗</span></Link>
          ) : (
            <>
              <Link className="nav-secondary" to="/login">Sign in</Link>
              <Link className="nav-cta" to="/register">Join the community <span aria-hidden="true">↗</span></Link>
            </>
          )}
        </div>
      </nav>

      <section className="landing-content">
        <div className="landing-copy">
          <p className="eyebrow">A community that shows up</p>
          <h1>Somewhere, someone is waiting for <span>you.</span></h1>
          <p>Blood donation is a small act with a life-changing reach. Find people nearby, meet with care, and make a real connection.</p>
          {user ? (
            <div className="landing-actions">
              <Link className="button-link" to="/dashboard">Go to your dashboard <span aria-hidden="true">→</span></Link>
              {user.role === 'DONOR' ? <Link className="text-link" to="/donor/profile">Your donor profile</Link> : null}
              {user.role === 'RECIPIENT' ? <Link className="text-link" to="/recipient/profile">Your recipient profile</Link> : null}
            </div>
          ) : (
            <div className="landing-actions">
              <Link className="button-link" to="/register">Be there for someone <span aria-hidden="true">→</span></Link>
              <Link className="text-link" to="/login">I already have an account</Link>
            </div>
          )}
        </div>
        <div className="landing-aside">
          <span className="aside-mark" aria-hidden="true">“</span>
          <p>There’s no grand gesture required. Sometimes it’s simply being there when someone needs you.</p>
          <span className="aside-caption">The BloodBond community</span>
        </div>
      </section>
      <section className="impact-strip" id="how-it-works" aria-label="How BloodBond works">
        <div className="impact-inner">
          <div className="impact-item"><strong>Find your people</strong><span>Discover available donors in your area.</span></div>
          <div className="impact-item"><strong>Talk it through</strong><span>Start a private conversation when you’re ready.</span></div>
          <div className="impact-item"><strong>Make it meaningful</strong><span>Every connection begins with kindness.</span></div>
        </div>
      </section>
      <footer className="landing-footer">BloodBond is a community platform, not a substitute for medical advice or clinical eligibility screening.</footer>
    </main>
  )
}
