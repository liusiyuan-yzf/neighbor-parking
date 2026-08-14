export interface User {
  id: number
  nickname: string
  phoneMasked: string
  roles: string[]
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
export interface ParkingSpace {
  id: number
  ownerId: number
  communityId: number
  spaceCode: string
  title: string
  vehicleLimit: string
  status: string
  reviewNote?: string
  createdAt: string
}
export interface Booking {
  id: number
  bookingNo: string
  spaceId: number
  ownerId: number
  renterId: number
  startAt: string
  endAt: string
  status: string
  createdAt: string
}
export interface Complaint {
  id: number
  bookingId: number
  complainantId: number
  content: string
  status: string
  resolutionNote?: string
  createdAt: string
}
export interface AuditLog {
  id: number
  operatorId: number
  action: string
  targetType: string
  targetId: number
  detail?: string
  createdAt: string
}
