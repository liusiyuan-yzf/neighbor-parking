import { request } from './http'
import type {
  AvailabilitySlot,
  Booking,
  Community,
  LoginResponse,
  ParkingSpace,
  SearchResult,
  User,
  Vehicle,
} from '@/types/api'

export const api = {
  devLogin: (userId: number) =>
    request<LoginResponse, { userId: number }>({
      url: '/auth/dev-login',
      method: 'POST',
      data: { userId },
    }),
  me: () => request<User>({ url: '/auth/me' }),
  communities: () => request<Community[]>({ url: '/communities' }),
  vehicles: () => request<Vehicle[]>({ url: '/vehicles' }),
  createVehicle: (data: { plateNumber: string; vehicleType: string }) =>
    request<Vehicle, typeof data>({ url: '/vehicles', method: 'POST', data }),
  deactivateVehicle: (id: number) => request<void>({ url: `/vehicles/${id}`, method: 'DELETE' }),
  searchSpaces: (communityId: number, startAt: string, endAt: string) =>
    request<SearchResult[]>({
      url: `/spaces/search?communityId=${communityId}&startAt=${encodeURIComponent(startAt)}&endAt=${encodeURIComponent(endAt)}`,
    }),
  spaceDetail: (spaceId: number, slotId: number) =>
    request<SearchResult>({ url: `/spaces/${spaceId}?slotId=${slotId}` }),
  createBooking: (data: { slotId: number; vehicleId: number; startAt: string; endAt: string }) =>
    request<Booking, typeof data>({ url: '/bookings', method: 'POST', data }),
  bookings: () => request<Booking[]>({ url: '/bookings' }),
  booking: (id: number) => request<Booking>({ url: `/bookings/${id}` }),
  cancelBooking: (id: number, reason: string) =>
    request<Booking, { reason: string }>({
      url: `/bookings/${id}/cancel`,
      method: 'POST',
      data: { reason },
    }),
  checkIn: (id: number) => request<Booking>({ url: `/bookings/${id}/check-in`, method: 'POST' }),
  complete: (id: number) => request<Booking>({ url: `/bookings/${id}/complete`, method: 'POST' }),
  review: (id: number, rating: number, content: string) =>
    request<void, { rating: number; content: string }>({
      url: `/bookings/${id}/reviews`,
      method: 'POST',
      data: { rating, content },
    }),
  complain: (id: number, content: string) =>
    request<void, { content: string }>({
      url: `/bookings/${id}/complaints`,
      method: 'POST',
      data: { content },
    }),
  ownerSpaces: () => request<ParkingSpace[]>({ url: '/owner/spaces' }),
  createSpace: (data: Omit<ParkingSpace, 'id' | 'status' | 'reviewNote'>) =>
    request<ParkingSpace, typeof data>({ url: '/owner/spaces', method: 'POST', data }),
  updateSpace: (id: number, data: Omit<ParkingSpace, 'id' | 'status' | 'reviewNote'>) =>
    request<ParkingSpace, typeof data>({ url: `/owner/spaces/${id}`, method: 'PUT', data }),
  ownerBookings: () => request<Booking[]>({ url: '/owner/bookings' }),
  slots: (spaceId: number) => request<AvailabilitySlot[]>({ url: `/owner/spaces/${spaceId}/slots` }),
  publishSlot: (spaceId: number, startAt: string, endAt: string) =>
    request<AvailabilitySlot, { startAt: string; endAt: string }>({
      url: `/owner/spaces/${spaceId}/slots`,
      method: 'POST',
      data: { startAt, endAt },
    }),
  cancelSlot: (spaceId: number, slotId: number) =>
    request<void>({ url: `/owner/spaces/${spaceId}/slots/${slotId}`, method: 'DELETE' }),
}
