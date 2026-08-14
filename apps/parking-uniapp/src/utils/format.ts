import type { BookingStatus } from '@/types/api'

const STATUS_LABELS: Record<BookingStatus, string> = {
  CONFIRMED: '待使用',
  IN_USE: '使用中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '未到场',
  DISPUTED: '处理中',
}

export const statusLabel = (status: BookingStatus) => STATUS_LABELS[status]

export function formatDateTime(value: string) {
  const date = new Date(value)
  const pad = (number: number) => String(number).padStart(2, '0')
  return `${date.getMonth() + 1}月${date.getDate()}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function defaultSearchRange() {
  const start = new Date(Date.now() + 30 * 60 * 1000)
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000)
  return { startAt: start.toISOString(), endAt: end.toISOString() }
}
