import { useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { apiClient } from '../api/client'
import type { AuthUser } from '../api/auth'
import { AuthContext } from './AuthState'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('bloodbond_token'))
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem('bloodbond_user')
    if (!stored) return null
    try {
      return JSON.parse(stored) as AuthUser
    } catch {
      localStorage.removeItem('bloodbond_user')
      localStorage.removeItem('bloodbond_token')
      delete apiClient.defaults.headers.common.Authorization
      return null
    }
  })

  useEffect(() => {
    const handleUnauthorized = () => {
      setToken(null)
      setUser(null)
    }
    window.addEventListener('bloodbond:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('bloodbond:unauthorized', handleUnauthorized)
  }, [])

  const persistAuth = (newToken: string, nextUser: AuthUser) => {
    setToken(newToken)
    setUser(nextUser)
    localStorage.setItem('bloodbond_token', newToken)
    localStorage.setItem('bloodbond_user', JSON.stringify(nextUser))
    apiClient.defaults.headers.common.Authorization = 'Bearer ' + newToken
  }

  const login = async (email: string, password: string) => {
    const response = await apiClient.post<{ data: { token: string; user: AuthUser } }>('/auth/login', {
      email,
      password,
    })
    const { token: nextToken, user: nextUser } = response.data.data
    persistAuth(nextToken, nextUser)
  }

  const register = async (payload: {
    name: string
    email: string
    password: string
    role: 'DONOR' | 'RECIPIENT'
  }) => {
    const response = await apiClient.post<{ data: { token: string; user: AuthUser } }>('/auth/register', payload)
    const { token: nextToken, user: nextUser } = response.data.data
    persistAuth(nextToken, nextUser)
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('bloodbond_token')
    localStorage.removeItem('bloodbond_user')
    delete apiClient.defaults.headers.common.Authorization
  }

  const value = useMemo(
    () => ({ token, user, login, register, logout }),
    [token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}