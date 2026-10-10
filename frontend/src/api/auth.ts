import { apiClient } from './client'

export interface AuthUser {
  id: number
  name: string
  email: string
  role: 'DONOR' | 'RECIPIENT' | 'ADMIN'
}

export interface AuthResponse {
  token: string
  user: AuthUser
}

export const login = async (email: string, password: string) => {
  const response = await apiClient.post<{ data: AuthResponse }>('/auth/login', { email, password })
  return response.data.data
}

export const register = async (payload: {
  name: string
  email: string
  password: string
  role: 'DONOR' | 'RECIPIENT'
}) => {
  const response = await apiClient.post<{ data: AuthResponse }>('/auth/register', payload)
  return response.data.data
}
