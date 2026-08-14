export type UserRole = 'USER' | 'OWNER' | 'PROPERTY_ADMIN' | 'PLATFORM_ADMIN'
export type BookingStatus = 'CONFIRMED' | 'IN_USE' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'DISPUTED'

export interface User {
  id: number
  nickname: string
  phoneMasked: string
  avatarUrl?: string
  roles: UserRole[]
}

export interface LoginResponse {
  accessToken: string
  user: User
}

export interface Community {
  id: number
  name: string
  address: string
  latitude: number
  longitude: number
  active: boolean
}

export interface Vehicle {
  id: number
  plateNumber: string
  vehicleType: string
  active: boolean
}

export interface SearchResult {
  spaceId: number
  slotId: number
  title: string
  communityId: number
  communityName: string
  communityAddress: string
  latitude: number
  longitude: number
  maskedSpaceCode: string
  vehicleLimit: string
  startAt: string
  endAt: string
  freeOfCharge: boolean
}

export interface Booking {
  id: number
  bookingNo: string
  status: BookingStatus
  spaceId: number
  spaceTitle: string
  spaceCode: string
  accessInstructions: string
  vehicleId: number
  startAt: string
  endAt: string
  cancelReason?: string
  createdAt: string
}

export interface ParkingSpace {
  id: number
  communityId: number
  spaceCode: string
  title: string
  accessInstructions: string
  vehicleLimit: string
  status: 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'DISABLED'
  reviewNote?: string
}

export interface AvailabilitySlot {
  id: number
  spaceId: number
  startAt: string
  endAt: string
  status: 'PUBLISHED' | 'CANCELLED' | 'EXPIRED'
}
