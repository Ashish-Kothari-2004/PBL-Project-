import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  timeout: 10000,
})

apiClient.interceptors.request.use((config) => {
  if (config.url?.startsWith('/auth/')) {
    delete config.headers.Authorization
  } else {
    const token = localStorage.getItem('bloodbond_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    } else {
      delete config.headers.Authorization
    }
  }

  return config
})

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message ?? error.message
  }

  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.startsWith('/auth/')) {
      localStorage.removeItem('bloodbond_token')
      localStorage.removeItem('bloodbond_user')
      delete apiClient.defaults.headers.common.Authorization
      window.dispatchEvent(new Event('bloodbond:unauthorized'))
    }

    return Promise.reject(error)
  },
)
