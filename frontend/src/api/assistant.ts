import { apiClient } from './client'

export interface AssistantMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

export async function getAssistantHistory() {
  const response = await apiClient.get<{ data: AssistantMessage[] }>('/assistant/messages')
  return response.data.data
}

export async function sendAssistantMessage(content: string) {
  const response = await apiClient.post<{ data: { userMessage: AssistantMessage; assistantMessage: AssistantMessage } }>('/assistant/messages', { content })
  return response.data.data
}

export async function clearAssistantHistory() {
  await apiClient.delete('/assistant/messages')
}
