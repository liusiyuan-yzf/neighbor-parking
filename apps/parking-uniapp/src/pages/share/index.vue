<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { AvailabilitySlot, Booking, ParkingSpace } from '@/types/api'
import { formatDateTime, statusLabel } from '@/utils/format'

const authStore = useAuthStore()
const spaces = ref<ParkingSpace[]>([])
const slots = ref<Record<number, AvailabilitySlot[]>>({})
const ownerBookings = ref<Booking[]>([])

onShow(load)

async function load() {
  if (!authStore.isOwner) return
  try {
    ;[spaces.value, ownerBookings.value] = await Promise.all([api.ownerSpaces(), api.ownerBookings()])
  } catch (error) {
    showError(error)
  }
}

async function toggleSlots(spaceId: number) {
  if (slots.value[spaceId]) {
    delete slots.value[spaceId]
    return
  }
  try {
    slots.value[spaceId] = await api.slots(spaceId)
  } catch (error) {
    showError(error)
  }
}

async function publish(spaceId: number) {
  const start = new Date(Date.now() + 30 * 60 * 1000)
  const end = new Date(start.getTime() + 4 * 60 * 60 * 1000)
  try {
    await api.publishSlot(spaceId, start.toISOString(), end.toISOString())
    slots.value[spaceId] = await api.slots(spaceId)
    uni.showToast({ title: '已发布 4 小时' })
  } catch (error) {
    showError(error)
  }
}

async function cancel(spaceId: number, slotId: number) {
  try {
    await api.cancelSlot(spaceId, slotId)
    slots.value[spaceId] = await api.slots(spaceId)
  } catch (error) {
    showError(error)
  }
}

const statusText = (status: ParkingSpace['status']) =>
  ({ PENDING_REVIEW: '待审核', APPROVED: '已通过', REJECTED: '已驳回', DISABLED: '已停用' })[status]

function addSpace() {
  uni.navigateTo({ url: '/pages/share/edit' })
}

function editSpace(id: number) {
  uni.navigateTo({ url: `/pages/share/edit?id=${id}` })
}

function switchToOwner() {
  authStore.logout()
  uni.navigateTo({ url: '/pages/login/index' })
}
</script>

<template>
  <view class="page">
    <view class="share-heading"
      ><view
        ><text class="heading">共享我的车位</text><text class="muted subheading">空闲时发布，邻里可免费预约</text></view
      ><button v-if="authStore.isOwner" size="mini" class="primary-button add" @click="addSpace">登记车位</button></view
    >
    <view v-if="!authStore.isOwner" class="intro card">
      <text class="intro-title">成为车位共享者</text
      ><text class="muted intro-text"
        >阶段 2 先由物业确认车位权属。请在演示登录页切换到“王阿姨”体验车位登记、审核和时段发布。</text
      >
      <button class="secondary-button" @click="switchToOwner">切换到车位主身份</button>
    </view>
    <view v-for="space in spaces" :key="space.id" class="space card">
      <view class="space-head"
        ><view
          ><text class="space-title">{{ space.title }}</text
          ><text class="muted code">{{ space.spaceCode }}</text></view
        ><text :class="['badge', space.status.toLowerCase()]">{{ statusText(space.status) }}</text></view
      >
      <text v-if="space.reviewNote" class="review-note">审核说明：{{ space.reviewNote }}</text>
      <view class="space-actions">
        <button size="mini" class="secondary-button" @click="editSpace(space.id)">修改车位</button>
        <button size="mini" class="secondary-button" @click="toggleSlots(space.id)">
          {{ slots[space.id] ? '收起时段' : '查看时段' }}
        </button>
        <button v-if="space.status === 'APPROVED'" size="mini" class="primary-button" @click="publish(space.id)">
          发布未来 4 小时
        </button>
      </view>
      <view v-if="slots[space.id]" class="slot-list">
        <view v-for="slot in slots[space.id]" :key="slot.id" class="slot">
          <view
            ><text>{{ formatDateTime(slot.startAt) }} — {{ formatDateTime(slot.endAt) }}</text
            ><text class="muted slot-status">{{ slot.status }}</text></view
          >
          <button
            v-if="slot.status === 'PUBLISHED'"
            size="mini"
            class="danger-button"
            @click="cancel(space.id, slot.id)"
          >
            取消
          </button>
        </view>
        <text v-if="!slots[space.id].length" class="muted no-slot">还没有共享时段</text>
      </view>
    </view>
    <template v-if="authStore.isOwner">
      <text class="section-title">车位预约</text>
      <view v-for="booking in ownerBookings" :key="booking.id" class="owner-booking card">
        <view
          ><text class="booking-no">{{ booking.bookingNo }}</text
          ><text class="muted booking-time"
            >{{ formatDateTime(booking.startAt) }} — {{ formatDateTime(booking.endAt) }}</text
          ></view
        >
        <text class="owner-status">{{ statusLabel(booking.status) }}</text>
      </view>
      <view v-if="!ownerBookings.length" class="no-booking card muted">暂无车位预约</view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.share-heading,
.space-head,
.space-actions,
.slot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}
.share-heading {
  margin: 12rpx 0 30rpx;
}
.heading,
.subheading,
.intro-title,
.intro-text,
.space-title,
.code,
.slot-status {
  display: block;
}
.heading {
  font-size: 40rpx;
  font-weight: 900;
}
.subheading {
  margin-top: 10rpx;
}
.add {
  margin: 0;
  flex-shrink: 0;
}
.intro {
  padding: 38rpx;
}
.intro-title {
  font-size: 34rpx;
  font-weight: 850;
}
.intro-text {
  margin: 18rpx 0 32rpx;
  line-height: 1.7;
}
.space {
  margin-bottom: 20rpx;
  padding: 28rpx;
}
.space-title {
  font-size: 31rpx;
  font-weight: 850;
}
.code {
  margin-top: 8rpx;
}
.badge {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  color: #8c671c;
  background: #fff5d9;
  font-size: 22rpx;
}
.approved {
  color: #146a46;
  background: #e8f6ee;
}
.rejected {
  color: #9b3838;
  background: #fff0f0;
}
.review-note {
  display: block;
  margin-top: 20rpx;
  padding: 18rpx;
  background: #f4f6f5;
  border-radius: 14rpx;
  font-size: 24rpx;
}
.space-actions {
  margin-top: 24rpx;
  justify-content: flex-start;
}
.space-actions button {
  margin: 0;
}
.slot-list {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #edf1ef;
}
.slot {
  padding: 16rpx 0;
  font-size: 24rpx;
}
.slot button {
  margin: 0;
}
.slot-status {
  margin-top: 6rpx;
  font-size: 21rpx;
}
.no-slot {
  display: block;
  padding: 20rpx 0;
  text-align: center;
}
.owner-booking {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
  padding: 24rpx;
}
.booking-no,
.booking-time {
  display: block;
}
.booking-no {
  font-weight: 800;
}
.booking-time {
  margin-top: 8rpx;
  font-size: 22rpx;
}
.owner-status {
  color: #176c48;
  font-weight: 750;
}
.no-booking {
  padding: 36rpx;
  text-align: center;
}
</style>
