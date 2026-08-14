<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import type { SearchResult, Vehicle } from '@/types/api'
import { formatDateTime } from '@/utils/format'

const detail = ref<SearchResult>()
const vehicles = ref<Vehicle[]>([])
const selectedVehicleIndex = ref(0)
const startAt = ref('')
const endAt = ref('')
const submitting = ref(false)
const selectedVehicle = computed(() => vehicles.value[selectedVehicleIndex.value])

onLoad(async (query) => {
  startAt.value = decodeURIComponent(String(query?.startAt || ''))
  endAt.value = decodeURIComponent(String(query?.endAt || ''))
  try {
    const [space, vehicleList] = await Promise.all([
      api.spaceDetail(Number(query?.spaceId), Number(query?.slotId)),
      api.vehicles(),
    ])
    detail.value = space
    vehicles.value = vehicleList.filter((item) => item.active)
  } catch (error) {
    showError(error)
  }
})

async function book() {
  if (!detail.value || !selectedVehicle.value) {
    uni.showModal({
      title: '请先添加车辆',
      content: '预约需要绑定一辆有效车辆。',
      confirmText: '去添加',
      success: (result) => {
        if (result.confirm) uni.navigateTo({ url: '/pages/vehicles/index' })
      },
    })
    return
  }
  submitting.value = true
  try {
    const booking = await api.createBooking({
      slotId: detail.value.slotId,
      vehicleId: selectedVehicle.value.id,
      startAt: startAt.value,
      endAt: endAt.value,
    })
    uni.redirectTo({ url: `/pages/booking/detail?id=${booking.id}&created=1` })
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

function addVehicle() {
  uni.navigateTo({ url: '/pages/vehicles/index' })
}
</script>

<template>
  <view class="detail-page page" v-if="detail">
    <view class="image-block"><text class="parking-letter">P</text><text class="image-note">安全审核车位</text></view>
    <view class="main-card card">
      <view class="title-row"
        ><view
          ><text class="title">{{ detail.title }}</text
          ><text class="muted subtitle">{{ detail.communityName }} · {{ detail.maskedSpaceCode }}</text></view
        ><text class="free">免费</text></view
      >
      <view class="info-row"
        ><text class="label">地址</text><text class="value">{{ detail.communityAddress }}</text></view
      >
      <view class="info-row"
        ><text class="label">可停车型</text><text class="value">{{ detail.vehicleLimit }}</text></view
      >
      <view class="info-row"
        ><text class="label">预约时间</text
        ><text class="value">{{ formatDateTime(startAt) }} 至 {{ formatDateTime(endAt) }}</text></view
      >
    </view>
    <text class="section-title">选择车辆</text>
    <picker
      v-if="vehicles.length"
      :range="vehicles"
      range-key="plateNumber"
      @change="selectedVehicleIndex = Number($event.detail.value)"
    >
      <view class="vehicle-picker card"
        ><view
          ><text class="plate">{{ selectedVehicle?.plateNumber }}</text
          ><text class="muted type">{{ selectedVehicle?.vehicleType }}</text></view
        ><text>更换 ›</text></view
      >
    </picker>
    <view v-else class="vehicle-picker card" @click="addVehicle"><text>还没有车辆，点击添加</text><text>›</text></view>
    <view class="privacy-tip">预约前仅显示脱敏车位号；预约成功后，详情页会展示完整车位号与入场说明。</view>
    <button class="primary-button book-button" :loading="submitting" @click="book">确认免费预约</button>
  </view>
</template>

<style scoped lang="scss">
.detail-page {
  padding-top: 0;
}
.image-block {
  height: 320rpx;
  margin: 0 -28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #dcebe3;
  background: #1c5139;
}
.parking-letter {
  font-size: 124rpx;
  font-weight: 900;
  line-height: 1;
}
.image-note {
  margin-top: 18rpx;
  letter-spacing: 4rpx;
}
.main-card {
  margin-top: -38rpx;
  padding: 32rpx;
  position: relative;
}
.title-row,
.info-row,
.vehicle-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}
.title,
.subtitle,
.plate,
.type {
  display: block;
}
.title {
  font-size: 36rpx;
  font-weight: 900;
}
.subtitle {
  margin-top: 10rpx;
}
.free {
  padding: 10rpx 18rpx;
  color: #16794f;
  background: #eaf6ef;
  border-radius: 999rpx;
  font-weight: 800;
}
.info-row {
  margin-top: 28rpx;
  align-items: flex-start;
}
.label {
  flex-shrink: 0;
  color: #7a8880;
}
.value {
  max-width: 72%;
  text-align: right;
  line-height: 1.5;
}
.vehicle-picker {
  padding: 28rpx;
}
.plate {
  font-size: 31rpx;
  font-weight: 800;
}
.type {
  margin-top: 8rpx;
  font-size: 23rpx;
}
.privacy-tip {
  margin-top: 28rpx;
  padding: 24rpx;
  color: #53655b;
  background: #edf5f1;
  border-radius: 18rpx;
  font-size: 24rpx;
  line-height: 1.6;
}
.book-button {
  margin: 34rpx 0 10rpx;
}
</style>
