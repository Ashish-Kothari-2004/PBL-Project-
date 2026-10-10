import { createContext } from 'react'
import type { AuthUser } from '../api/auth'

interface AuthContextValue {
  token: string | null
  user: AuthUser | null
  login: (email: string, password: string) => Promise<void>
  register: (payload: {
    name: string
    email: string
    password: string
    role: 'DONOR' | 'RECIPIENT'
  }) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
