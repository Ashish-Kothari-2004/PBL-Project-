import { apiClient } from './client'

export interface AdminOverview {
  totalUsers: number
  donors: number
  recipients: number
  openRequests: number
  fulfilledRequests: number
  conversations: number
  pendingDonorReviews: number
}

export async function getAdminOverview() {
  const response = await apiClient.get<{ data: AdminOverview }>('/admin/overview')
  return response.data.data
}
