import { apiClient } from './client'

export interface DonorMatch {
  donorProfileId: number
  donorName: string
  bloodGroup: string
  city: string | null
  approxLat: number
  approxLng: number
  distanceKm: number
}

export async function searchNearbyDonors(params: {
  bloodGroup: string
  lat: number
  lng: number
  radiusKm: number
}) {
  const response = await apiClient.get<{ data: DonorMatch[] }>('/match/search', { params })
  return response.data.data
}
