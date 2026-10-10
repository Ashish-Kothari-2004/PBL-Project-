import { apiClient } from './client'

export interface DonorProfile {
  id: number | null
  userId: number
  bloodGroup: string
  dateOfBirth: string | null
  city: string | null
  approxLat: number | null
  approxLng: number | null
  isAvailable: boolean
  verificationStatus: string
}

export interface DonorProfileInput {
  bloodGroup: string
  dateOfBirth: string
  city: string
  approxLat: number | null
  approxLng: number | null
  isAvailable: boolean
}

export interface RecipientProfile {
  id: number | null
  userId: number
  city: string | null
  approxLat: number | null
  approxLng: number | null
}

export interface RecipientProfileInput {
  city: string
  approxLat: number | null
  approxLng: number | null
}

export async function getDonorProfile() {
  const response = await apiClient.get<{ data: DonorProfile }>('/donors/me')
  return response.data.data
}

export async function updateDonorProfile(profile: DonorProfileInput) {
  const response = await apiClient.put<{ data: DonorProfile }>('/donors/me', profile)
  return response.data.data
}

export async function getRecipientProfile() {
  const response = await apiClient.get<{ data: RecipientProfile }>('/recipients/me')
  return response.data.data
}

export async function updateRecipientProfile(profile: RecipientProfileInput) {
  const response = await apiClient.put<{ data: RecipientProfile }>('/recipients/me', profile)
  return response.data.data
}
