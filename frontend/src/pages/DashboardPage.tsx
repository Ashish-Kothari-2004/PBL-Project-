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
        <Link className="dashboard-brand" to="/"><span className="brand-mark" aria-hidden="true">B</span><span>bloodbond</span></Link>
        <div className="header-actions">
          {user.role === 'RECIPIENT' ? <Link to="/discover">Find donors</Link> : null}
          {user.role !== 'ADMIN' ? <Link to="/chat">Messages</Link> : null}
          {user.role === 'ADMIN' ? <Link to="/admin">Admin overview</Link> : null}
          <button type="button" onClick={logout}>Sign out</button>
        </div>
      </header>

      <section className="dashboard-intro">
        <div>
          <p className="eyebrow">{user.role === 'DONOR' ? 'Your generosity matters' : user.role === 'RECIPIENT' ? 'You don’t have to do this alone' : 'Community overview'}</p>
          <h1>Hello, <span>{user.name.split(' ')[0]}.</span></h1>
          <p className="intro-copy">
            {user.role === 'DONOR'
              ? 'A few details help the right person find you. Keep your availability and location up to date whenever you can.'
              : user.role === 'RECIPIENT'
                ? 'Your next step is just a conversation away. See who’s nearby, and connect when you feel ready.'
                : 'Thank you for helping keep the BloodBond community safe and supported.'}
          </p>
        </div>
        <div className="dashboard-aside">
          <p className="eyebrow">A note for today</p>
          <p className="dashboard-quote">{user.role === 'DONOR' ? 'A generous choice can change someone’s whole story.' : 'You don’t have to carry the hard parts on your own.'}</p>
          <span className="aside-caption">One step at a time.</span>
        </div>
      </section>
      {user.role !== 'ADMIN' ? (
        <section className="dashboard-actions">
          <div className="section-heading">
            <div><p className="eyebrow">Your next steps</p><h2>Where would you like to begin?</h2></div>
          </div>
          <div className="action-list">
            {user.role === 'DONOR' ? (
              <>
                <Link className="action-row" to="/donor/profile">
                  <span className="action-number">01</span><span className="action-copy"><strong>Keep your donor profile current</strong><span>Update your blood group, location, and availability.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/chat">
                  <span className="action-number">02</span><span className="action-copy"><strong>Check your conversations</strong><span>Reply to people you’re already connected with.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/requests">
                  <span className="action-number">03</span><span className="action-copy"><strong>See recipient requests</strong><span>Choose whether you would like to respond and start a conversation.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/assistant">
                  <span className="action-number">04</span><span className="action-copy"><strong>Ask the BloodBond assistant</strong><span>Get general information about donations and the app.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
              </>
            ) : (
              <>
                <Link className="action-row" to="/discover">
                  <span className="action-number">01</span><span className="action-copy"><strong>See donors around you</strong><span>Explore available donors by blood group and distance.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/recipient/profile">
                  <span className="action-number">02</span><span className="action-copy"><strong>Update your location</strong><span>Help us show the most relevant nearby matches.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/chat">
                  <span className="action-number">03</span><span className="action-copy"><strong>Open your conversations</strong><span>Continue a private chat with a donor.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/requests">
                  <span className="action-number">04</span><span className="action-copy"><strong>Post a blood request</strong><span>Share an approximate request with donors in the community.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
                <Link className="action-row" to="/assistant">
                  <span className="action-number">05</span><span className="action-copy"><strong>Ask the BloodBond assistant</strong><span>Learn about the platform and general donation topics.</span></span><span className="action-arrow" aria-hidden="true">↗</span>
                </Link>
              </>
            )}
          </div>
        </section>
      ) : null}
      {user.role === 'ADMIN' ? (
        <section className="dashboard-actions">
          <div className="section-heading"><div><p className="eyebrow">Platform operations</p><h2>Review community activity.</h2></div></div>
          <div className="action-list">
            <Link className="action-row" to="/admin"><span className="action-number">01</span><span className="action-copy"><strong>Open admin overview</strong><span>View aggregate account, request, and conversation counts.</span></span><span className="action-arrow" aria-hidden="true">↗</span></Link>
          </div>
        </section>
      ) : null}
      <p className="safety-note">A gentle reminder: Blood groups and eligibility details are self-reported and are not medical clearance. Always consult a qualified healthcare professional.</p>
    </main>
  )
}
