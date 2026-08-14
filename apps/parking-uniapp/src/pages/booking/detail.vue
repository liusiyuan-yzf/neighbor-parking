<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import type { Booking } from '@/types/api'
import { formatDateTime, statusLabel } from '@/utils/format'

const booking = ref<Booking>()
const bookingId = ref(0)
const justCreated = ref(false)
const busy = ref(false)
const canCancel = computed(() => booking.value?.status === 'CONFIRMED')
const canCheckIn = computed(() => booking.value?.status === 'CONFIRMED')
const canComplete = computed(() => booking.value?.status === 'IN_USE')
const canReview = computed(() => booking.value?.status === 'COMPLETED')

onLoad((query) => {
  bookingId.value = Number(query?.id)
  justCreated.value = query?.created === '1'
})
onShow(load)

async function load() {
  if (!bookingId.value) return
  try {
    booking.value = await api.booking(bookingId.value)
  } catch (error) {
    showError(error)
  }
}

async function action(run: () => Promise<Booking>, success: string) {
  busy.value = true
  try {
    booking.value = await run()
    uni.showToast({ title: success, icon: 'success' })
  } catch (error) {
    showError(error)
  } finally {
    busy.value = false
  }
}

function cancel() {
  uni.showModal({
    title: '取消预约',
    editable: true,
    placeholderText: '请输入取消原因',
    success: (result) => {
      if (result.confirm && result.content?.trim())
        action(() => api.cancelBooking(bookingId.value, result.content!.trim()), '已取消')
    },
  })
}

function review() {
  uni.showModal({
    title: '评价本次停车',
    editable: true,
    placeholderText: '说说本次体验（默认 5 星）',
    success: async (result) => {
      if (!result.confirm) return
      try {
        await api.review(bookingId.value, 5, result.content || '体验很好')
        uni.showToast({ title: '评价成功' })
      } catch (error) {
        showError(error)
      }
    },
  })
}

function complain() {
  uni.showModal({
    title: '提交投诉',
    editable: true,
    placeholderText: '请描述遇到的问题',
    success: async (result) => {
      if (!result.confirm || !result.content?.trim()) return
      try {
        await api.complain(bookingId.value, result.content.trim())
        await load()
        uni.showToast({ title: '已提交' })
      } catch (error) {
        showError(error)
      }
    },
  })
}
</script>

<template>
  <view v-if="booking" class="page detail-page">
    <view v-if="justCreated" class="success card"
      ><view class="check">✓</view><text class="success-title">预约成功</text
      ><text class="muted">完整车位信息已为你解锁</text></view
    >
    <view class="status-card card"
      ><view
        ><text class="muted">当前状态</text><text class="status">{{ statusLabel(booking.status) }}</text></view
      ><text class="booking-no">{{ booking.bookingNo }}</text></view
    >
    <text class="section-title">停车信息</text>
    <view class="info-card card">
      <view class="space-code"
        ><text class="muted">车位号</text><text>{{ booking.spaceCode }}</text></view
      >
      <text class="space-title">{{ booking.spaceTitle }}</text>
      <view class="info-row"
        ><text class="muted">预约时间</text
        ><text>{{ formatDateTime(booking.startAt) }} 至 {{ formatDateTime(booking.endAt) }}</text></view
      >
      <view class="instructions"
        ><text class="instruction-title">入场说明</text><text>{{ booking.accessInstructions }}</text></view
      >
    </view>
    <view class="actions">
      <button
        v-if="canCheckIn"
        class="primary-button"
        :loading="busy"
        @click="action(() => api.checkIn(bookingId), '签到成功')"
      >
        到场签到
      </button>
      <button
        v-if="canComplete"
        class="primary-button"
        :loading="busy"
        @click="action(() => api.complete(bookingId), '本次停车已完成')"
      >
        确认离场
      </button>
      <button v-if="canCancel" class="danger-button" @click="cancel">取消预约</button>
      <button v-if="canReview" class="secondary-button" @click="review">五星评价</button>
      <button class="text-button" @click="complain">遇到问题？提交投诉</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.success {
  margin-bottom: 22rpx;
  padding: 38rpx;
  text-align: center;
}
.check {
  width: 72rpx;
  height: 72rpx;
  margin: 0 auto 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #16794f;
  border-radius: 50%;
  font-size: 42rpx;
  font-weight: 900;
}
.success-title,
.status,
.space-title,
.instruction-title {
  display: block;
}
.success-title {
  margin-bottom: 8rpx;
  font-size: 34rpx;
  font-weight: 850;
}
.status-card {
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.status {
  margin-top: 8rpx;
  color: #16794f;
  font-size: 36rpx;
  font-weight: 900;
}
.booking-no {
  color: #78867e;
  font-size: 22rpx;
}
.info-card {
  padding: 30rpx;
}
.space-code {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #edf6f1;
  border-radius: 18rpx;
  font-size: 42rpx;
  font-weight: 900;
}
.space-title {
  margin: 28rpx 0;
  font-size: 32rpx;
  font-weight: 800;
}
.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 30rpx;
  line-height: 1.5;
}
.info-row text:last-child {
  text-align: right;
}
.instructions {
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #edf1ef;
  line-height: 1.7;
}
.instruction-title {
  margin-bottom: 8rpx;
  font-weight: 800;
}
.actions {
  margin-top: 30rpx;
}
.actions button {
  margin-top: 18rpx;
}
.text-button {
  color: #6d7972;
  background: transparent;
  font-size: 25rpx;
}
</style>
