import { apiClient } from './client'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export interface ChatRoom {
  id: number
  bloodRequestId: number
  counterpartName: string
  bloodGroup: string
  city: string | null
  createdAt: string
}

export interface ChatMessage {
  id: number
  senderId: number
  senderName: string
  content: string
  createdAt: string
}

export async function getChatRooms() {
  const response = await apiClient.get<{ data: ChatRoom[] }>('/chat/rooms')
  return response.data.data
}

export async function createChatRoom(payload: {
  donorProfileId: number
  bloodGroup: string
  city: string
  approxLat: number
  approxLng: number
}) {
  const response = await apiClient.post<{ data: ChatRoom }>('/chat/rooms', payload)
  return response.data.data
}

export async function getChatMessages(roomId: number) {
  const response = await apiClient.get<{ data: ChatMessage[] }>(`/chat/rooms/${roomId}/messages`)
  return response.data.data
}

export async function sendChatMessage(roomId: number, content: string) {
  const response = await apiClient.post<{ data: ChatMessage }>(`/chat/rooms/${roomId}/messages`, { content })
  return response.data.data
}

export function connectChatSocket(
  token: string,
  roomId: number | null,
  handlers: {
    onMessage: (message: ChatMessage) => void
    onRoom: (room: ChatRoom) => void
    onConnectionChange: (connected: boolean) => void
    onError: (message: string) => void
  },
) {
  const apiUrl = new URL(import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api')
  apiUrl.pathname = `${apiUrl.pathname.replace(/\/api\/?$/, '').replace(/\/$/, '')}/ws`

  const client = new Client({
    webSocketFactory: () => new SockJS(apiUrl.toString()) as WebSocket,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      handlers.onConnectionChange(true)
      client.subscribe('/user/queue/chat/rooms', (frame) => {
        handlers.onRoom(JSON.parse(frame.body) as ChatRoom)
      })
      if (roomId !== null) {
        client.subscribe(`/topic/chat/rooms/${roomId}`, (frame) => {
          handlers.onMessage(JSON.parse(frame.body) as ChatMessage)
        })
      }
    },
    onDisconnect: () => handlers.onConnectionChange(false),
    onWebSocketClose: () => handlers.onConnectionChange(false),
    onStompError: (frame) => {
      handlers.onConnectionChange(false)
      handlers.onError(frame.headers.message ?? 'Chat connection error.')
    },
    onWebSocketError: () => {
      handlers.onConnectionChange(false)
      handlers.onError('Could not connect to chat. Retrying...')
    },
  })

  client.activate()
  return client
}

export function publishChatMessage(client: Client, roomId: number, content: string) {
  client.publish({
    destination: `/app/chat/rooms/${roomId}/messages`,
    body: JSON.stringify({ content }),
  })
}
