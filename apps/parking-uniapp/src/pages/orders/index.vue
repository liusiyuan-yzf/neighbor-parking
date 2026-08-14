<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import type { Booking } from '@/types/api'
import { formatDateTime, statusLabel } from '@/utils/format'

const bookings = ref<Booking[]>([])
const loading = ref(false)

onShow(load)

async function load() {
  loading.value = true
  try {
    bookings.value = await api.bookings()
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

function open(id: number) {
  uni.navigateTo({ url: `/pages/booking/detail?id=${id}` })
}

function goSearch() {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<template>
  <view class="page">
    <view class="page-heading"
      ><text class="heading">我的预约</text><text class="muted">{{ bookings.length }} 笔</text></view
    >
    <view v-if="!loading && !bookings.length" class="empty card">
      <text class="empty-title">还没有预约</text><text class="muted">去附近找一个空闲车位吧</text>
      <button class="primary-button" @click="goSearch">去找车位</button>
    </view>
    <view v-for="booking in bookings" :key="booking.id" class="booking-card card" @click="open(booking.id)">
      <view class="card-top"
        ><text class="title">{{ booking.spaceTitle }}</text
        ><text :class="['status', `status-${booking.status.toLowerCase()}`]">{{
          statusLabel(booking.status)
        }}</text></view
      >
      <text class="time">{{ formatDateTime(booking.startAt) }} — {{ formatDateTime(booking.endAt) }}</text>
      <view class="card-bottom"
        ><text class="muted">预约号 {{ booking.bookingNo }}</text
        ><text class="link">详情 ›</text></view
      >
    </view>
  </view>
</template>

<style scoped lang="scss">
.page-heading,
.card-top,
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}
.page-heading {
  margin: 14rpx 0 30rpx;
}
.heading {
  font-size: 42rpx;
  font-weight: 900;
}
.booking-card {
  margin-bottom: 20rpx;
  padding: 28rpx;
}
.title {
  font-size: 31rpx;
  font-weight: 800;
}
.status {
  flex-shrink: 0;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  color: #8b6618;
  background: #fff7df;
  font-size: 22rpx;
  font-weight: 750;
}
.status-in_use {
  color: #146a46;
  background: #e8f6ee;
}
.status-completed,
.status-cancelled {
  color: #68766e;
  background: #eef1ef;
}
.status-disputed {
  color: #9b3838;
  background: #fff0f0;
}
.time {
  display: block;
  margin-top: 22rpx;
  line-height: 1.5;
}
.card-bottom {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #edf1ef;
  font-size: 23rpx;
}
.link {
  color: #16794f;
  font-weight: 750;
}
.empty {
  margin-top: 100rpx;
  padding: 54rpx 34rpx;
  text-align: center;
}
.empty-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 32rpx;
  font-weight: 800;
}
.empty button {
  margin-top: 32rpx;
}
</style>
