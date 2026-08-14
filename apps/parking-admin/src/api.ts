import type { AuditLog, Booking, Community, Complaint, LoginResponse, ParkingSpace } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const TOKEN_KEY = 'neighbor-parking-admin-token'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem(TOKEN_KEY)
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: `请求失败（${response.status}）` }))
    if (response.status === 401) localStorage.removeItem(TOKEN_KEY)
    throw new Error(error.message || `请求失败（${response.status}）`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  async login() {
    const response = await request<LoginResponse>('/auth/dev-login', {
      method: 'POST',
      body: JSON.stringify({ userId: 3 }),
    })
    localStorage.setItem(TOKEN_KEY, response.accessToken)
    return response
  },
  hasToken: () => Boolean(localStorage.getItem(TOKEN_KEY)),
  logout: () => localStorage.removeItem(TOKEN_KEY),
  communities: () => request<Community[]>('/admin/communities'),
  createCommunity: (data: Omit<Community, 'id'>) =>
    request<Community>('/admin/communities', { method: 'POST', body: JSON.stringify(data) }),
  updateCommunity: (id: number, data: Omit<Community, 'id'>) =>
    request<Community>(`/admin/communities/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  pendingSpaces: () => request<ParkingSpace[]>('/admin/spaces/pending'),
  reviewSpace: (id: number, approved: boolean, note: string) =>
    request<ParkingSpace>(`/admin/spaces/${id}/${approved ? 'approve' : 'reject'}`, {
      method: 'POST',
      body: JSON.stringify({ note }),
    }),
  bookings: () => request<Booking[]>('/admin/bookings'),
  complaints: () => request<Complaint[]>('/admin/complaints'),
  resolveComplaint: (id: number, resolved: boolean, note: string) =>
    request<Complaint>(`/admin/complaints/${id}/resolve`, { method: 'POST', body: JSON.stringify({ resolved, note }) }),
  audits: () => request<AuditLog[]>('/admin/audits'),
}
