import { apiClient } from './client'

export interface BloodRequestResponse {
  status: string
  createdAt: string
}

export interface BloodRequest {
  id: number
  bloodGroup: string
  city: string | null
  radiusKm: number
  status: 'OPEN' | 'FULFILLED' | 'CANCELLED'
  createdAt: string
  responses: BloodRequestResponse[]
}

export interface BloodRequestActionResult {
  request: BloodRequest
  chatRoom: { id: number } | null
}

export async function getOpenBloodRequests() {
  const response = await apiClient.get<{ data: BloodRequest[] }>('/requests/open')
  return response.data.data
}

export async function getMyBloodRequests() {
  const response = await apiClient.get<{ data: BloodRequest[] }>('/requests/mine')
  return response.data.data
}

export async function createBloodRequest(payload: {
  bloodGroup: string
  city: string
  approxLat: number | null
  approxLng: number | null
  radiusKm: number
}) {
  const response = await apiClient.post<{ data: BloodRequest }>('/requests', payload)
  return response.data.data
}

export async function cancelBloodRequest(id: number) {
  const response = await apiClient.delete<{ data: BloodRequest }>(`/requests/${id}`)
  return response.data.data
}

export async function respondToBloodRequest(id: number, decision: 'ACCEPTED' | 'DECLINED') {
  const response = await apiClient.post<{ data: BloodRequestActionResult }>(`/requests/${id}/respond`, { decision })
  return response.data.data
}
