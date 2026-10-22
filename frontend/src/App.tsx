import { lazy, Suspense } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { DashboardPage } from './pages/DashboardPage'
import { DonorProfilePage } from './pages/DonorProfilePage'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { RecipientProfilePage } from './pages/RecipientProfilePage'
import './App.css'

const DiscoverPage = lazy(() => import('./pages/DiscoverPage').then(({ DiscoverPage: page }) => ({ default: page })))
const ChatPage = lazy(() => import('./pages/ChatPage').then(({ ChatPage: page }) => ({ default: page })))
const AssistantPage = lazy(() => import('./pages/AssistantPage').then(({ AssistantPage: page }) => ({ default: page })))
const RequestsPage = lazy(() => import('./pages/RequestsPage').then(({ RequestsPage: page }) => ({ default: page })))
const AdminPage = lazy(() => import('./pages/AdminPage').then(({ AdminPage: page }) => ({ default: page })))

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<ProtectedRoute allowedRoles={['DONOR', 'RECIPIENT', 'ADMIN']} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={['DONOR']} />}>
            <Route path="/donor/profile" element={<DonorProfilePage />} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={['RECIPIENT']} />}>
            <Route path="/recipient/profile" element={<RecipientProfilePage />} />
            <Route path="/discover" element={<Suspense fallback={<main className="dashboard-shell">Loading donor map...</main>}><DiscoverPage /></Suspense>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={['DONOR', 'RECIPIENT']} />}>
            <Route path="/chat" element={<Suspense fallback={<main className="dashboard-shell">Loading chats...</main>}><ChatPage /></Suspense>} />
            <Route path="/assistant" element={<Suspense fallback={<main className="dashboard-shell">Loading assistant...</main>}><AssistantPage /></Suspense>} />
            <Route path="/requests" element={<Suspense fallback={<main className="dashboard-shell">Loading requests...</main>}><RequestsPage /></Suspense>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<Suspense fallback={<main className="dashboard-shell">Loading overview...</main>}><AdminPage /></Suspense>} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
