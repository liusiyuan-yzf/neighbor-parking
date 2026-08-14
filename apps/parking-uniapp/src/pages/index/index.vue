<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { Community, SearchResult } from '@/types/api'
import { defaultSearchRange, formatDateTime } from '@/utils/format'

const authStore = useAuthStore()
const communities = ref<Community[]>([])
const selectedCommunityId = ref(0)
const spaces = ref<SearchResult[]>([])
const loading = ref(false)
const range = ref(defaultSearchRange())

const community = computed(() => communities.value.find((item) => item.id === selectedCommunityId.value))
const markers = computed(() =>
  spaces.value.map((item, index) => ({
    id: item.spaceId,
    latitude: item.latitude + index * 0.00025,
    longitude: item.longitude + index * 0.0002,
    width: 34,
    height: 34,
    callout: { content: '免费', display: 'ALWAYS', color: '#16794f', bgColor: '#ffffff', padding: 8 },
  })),
)

onShow(async () => {
  if (!authStore.loggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!communities.value.length) await loadCommunities()
})

async function loadCommunities() {
  try {
    communities.value = await api.communities()
    selectedCommunityId.value = communities.value[0]?.id || 0
    if (selectedCommunityId.value) await search()
  } catch (error) {
    showError(error)
  }
}

async function search() {
  if (!selectedCommunityId.value) return
  loading.value = true
  try {
    spaces.value = await api.searchSpaces(selectedCommunityId.value, range.value.startAt, range.value.endAt)
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

function chooseCommunity(event: { detail: { value: string } }) {
  selectedCommunityId.value = communities.value[Number(event.detail.value)]?.id || 0
  search()
}

function chooseRange(hours: number, tomorrow = false) {
  const offset = tomorrow ? 24 * 60 * 60 * 1000 : 30 * 60 * 1000
  const start = new Date(Date.now() + offset)
  range.value = { startAt: start.toISOString(), endAt: new Date(start.getTime() + hours * 3600000).toISOString() }
  search()
}

function openSpace(space: SearchResult) {
  const query = `spaceId=${space.spaceId}&slotId=${space.slotId}&startAt=${encodeURIComponent(range.value.startAt)}&endAt=${encodeURIComponent(range.value.endAt)}`
  uni.navigateTo({ url: `/pages/space/detail?${query}` })
}

function goProfile() {
  uni.switchTab({ url: '/pages/profile/index' })
}
</script>

<template>
  <view class="home-page">
    <view class="hero">
      <view><text class="eyebrow">邻里车位</text><text class="headline">附近空闲车位</text></view>
      <view class="avatar" @click="goProfile">{{ authStore.user?.nickname?.slice(0, 1) || '我' }}</view>
    </view>
    <view class="search-card card">
      <picker :range="communities" range-key="name" @change="chooseCommunity">
        <view class="field-row"
          ><view
            ><text class="field-label">目的小区</text
            ><text class="field-value">{{ community?.name || '选择小区' }}</text></view
          ><text class="chevron">›</text></view
        >
      </picker>
      <view class="divider" />
      <view class="field-row"
        ><view
          ><text class="field-label">使用时间</text
          ><text class="field-value"
            >{{ formatDateTime(range.startAt) }} — {{ formatDateTime(range.endAt) }}</text
          ></view
        ></view
      >
      <view class="quick-times">
        <button size="mini" @click="chooseRange(2)">稍后 2 小时</button>
        <button size="mini" @click="chooseRange(4)">稍后 4 小时</button>
        <button size="mini" @click="chooseRange(3, true)">明天 3 小时</button>
      </view>
    </view>
    <view class="map-shell card">
      <!-- #ifdef H5 -->
      <view class="h5-map">
        <view class="map-block block-one" /><view class="map-block block-two" /><view class="map-block block-three" />
        <view class="road road-one" /><view class="road road-two" /><view class="road road-three" />
        <view class="park">邻里公园</view>
        <view v-if="spaces.length" class="css-pin pin-one"
          ><text class="pin-label">免费</text><text class="pin-dot">P</text></view
        >
        <view v-if="spaces.length" class="css-pin pin-two"><text class="pin-dot">P</text></view>
        <view class="community-name">{{ community?.name }}</view>
      </view>
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <map
        class="map"
        :latitude="community?.latitude || 31.2304"
        :longitude="community?.longitude || 121.4737"
        :markers="markers"
        :scale="15"
        show-location
      />
      <!-- #endif -->
      <view class="map-note">具体车位号将在预约成功后显示</view>
    </view>
    <view class="result-heading"
      ><text class="section-title">可预约车位</text
      ><text class="muted">{{ loading ? '查找中…' : `${spaces.length} 个结果` }}</text></view
    >
    <view v-if="!loading && !spaces.length" class="empty card"
      ><text class="empty-title">这个时段暂无空位</text><text class="muted">换一个快捷时间再试试</text></view
    >
    <view v-for="space in spaces" :key="space.slotId" class="space-card card" @click="openSpace(space)">
      <view class="space-top"
        ><view
          ><text class="space-title">{{ space.title }}</text
          ><text class="space-meta">{{ space.communityName }} · {{ space.maskedSpaceCode }}</text></view
        ><text class="free-badge">免费</text></view
      >
      <view class="space-bottom"
        ><text class="muted">{{ space.vehicleLimit }}</text
        ><text class="detail-link">查看并预约 ›</text></view
      >
    </view>
  </view>
</template>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 28rpx) 28rpx 40rpx;
  box-sizing: border-box;
}
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}
.eyebrow,
.headline,
.field-label,
.field-value,
.space-title,
.space-meta,
.empty-title {
  display: block;
}
.eyebrow {
  color: #16794f;
  font-size: 24rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}
.headline {
  margin-top: 8rpx;
  font-size: 46rpx;
  font-weight: 900;
  letter-spacing: -1rpx;
}
.avatar {
  width: 76rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #1d5039;
  border-radius: 50%;
  font-weight: 800;
}
.search-card {
  padding: 8rpx 28rpx 24rpx;
}
.field-row {
  min-height: 108rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.field-label {
  margin-bottom: 8rpx;
  color: #7a8880;
  font-size: 22rpx;
}
.field-value {
  font-size: 29rpx;
  font-weight: 750;
}
.chevron {
  color: #849188;
  font-size: 44rpx;
}
.divider {
  height: 1rpx;
  background: #e8eeeb;
}
.quick-times {
  display: flex;
  gap: 12rpx;
  overflow: hidden;
}
.quick-times button {
  flex: 1;
  margin: 0;
  padding: 0 10rpx;
  color: #28674c;
  background: #edf7f1;
  font-size: 22rpx;
}
.map-shell {
  position: relative;
  height: 390rpx;
  margin-top: 24rpx;
  overflow: hidden;
}
.map {
  width: 100%;
  height: 100%;
}
.h5-map {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #e7eee8;
}
.map-block {
  position: absolute;
  background: #d1dfd5;
  border: 5rpx solid #dce8df;
  border-radius: 12rpx;
  transform: rotate(-8deg);
}
.block-one {
  width: 190rpx;
  height: 90rpx;
  left: 30rpx;
  top: 58rpx;
}
.block-two {
  width: 220rpx;
  height: 104rpx;
  right: 12rpx;
  top: 126rpx;
}
.block-three {
  width: 250rpx;
  height: 82rpx;
  left: 120rpx;
  bottom: 48rpx;
}
.road {
  position: absolute;
  height: 34rpx;
  background: #f7faf8;
  border-block: 2rpx solid #d9e3dd;
}
.road-one {
  width: 130%;
  left: -15%;
  top: 182rpx;
  transform: rotate(-9deg);
}
.road-two {
  width: 115%;
  left: -8%;
  top: 286rpx;
  transform: rotate(8deg);
}
.road-three {
  width: 120%;
  left: -5%;
  top: 75rpx;
  transform: rotate(24deg);
}
.park {
  position: absolute;
  left: 28rpx;
  bottom: 34rpx;
  padding: 14rpx 20rpx;
  color: #45705a;
  background: #c8dfce;
  border-radius: 15rpx;
  font-size: 22rpx;
  font-weight: 750;
}
.community-name {
  position: absolute;
  right: 34rpx;
  top: 32rpx;
  color: #5a6d63;
  font-size: 24rpx;
  font-weight: 750;
}
.css-pin {
  position: absolute;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.pin-one {
  left: 44%;
  top: 84rpx;
}
.pin-two {
  right: 21%;
  bottom: 76rpx;
}
.pin-label {
  margin-bottom: -3rpx;
  padding: 6rpx 12rpx;
  color: #fff;
  background: #168055;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}
.pin-dot {
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #16794f;
  border: 5rpx solid #fff;
  border-radius: 50%;
  box-shadow: 0 5rpx 12rpx rgba(24, 81, 54, 0.22);
  font-size: 27rpx;
  font-weight: 900;
}
.map-note {
  position: absolute;
  left: 20rpx;
  right: 20rpx;
  bottom: 18rpx;
  padding: 12rpx 20rpx;
  color: #42534a;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 14rpx;
  text-align: center;
  font-size: 22rpx;
}
.result-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.space-card {
  margin-bottom: 18rpx;
  padding: 26rpx;
}
.space-top,
.space-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}
.space-title {
  font-size: 31rpx;
  font-weight: 800;
}
.space-meta {
  margin-top: 10rpx;
  color: #748178;
  font-size: 24rpx;
}
.free-badge {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  color: #16794f;
  background: #eaf6ef;
  border-radius: 999rpx;
  font-weight: 800;
}
.space-bottom {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #edf1ef;
}
.detail-link {
  color: #16794f;
  font-weight: 750;
}
.empty {
  padding: 52rpx 30rpx;
  text-align: center;
}
.empty-title {
  margin-bottom: 12rpx;
  font-size: 30rpx;
  font-weight: 800;
}
</style>
