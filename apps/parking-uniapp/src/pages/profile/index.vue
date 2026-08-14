<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { showError } from '@/api/http'

const authStore = useAuthStore()
onShow(async () => {
  try {
    await authStore.refresh()
  } catch (error) {
    showError(error)
  }
})

function logout() {
  authStore.logout()
  uni.navigateTo({ url: '/pages/login/index' })
}

function openVehicles() {
  uni.navigateTo({ url: '/pages/vehicles/index' })
}

function openShare() {
  uni.switchTab({ url: '/pages/share/index' })
}
</script>

<template>
  <view class="page profile-page">
    <view class="profile-card card">
      <view class="avatar">{{ authStore.user?.nickname?.slice(0, 1) || '我' }}</view>
      <view
        ><text class="name">{{ authStore.user?.nickname || '未登录' }}</text
        ><text class="muted phone">{{ authStore.user?.phoneMasked }}</text></view
      >
    </view>
    <text class="section-title">常用服务</text>
    <view class="menu card">
      <view class="menu-row" @click="openVehicles"
        ><view><text class="menu-title">我的车辆</text><text class="muted note">管理预约使用的车牌</text></view
        ><text>›</text></view
      >
      <view class="divider" />
      <view class="menu-row" @click="openShare"
        ><view><text class="menu-title">我的共享</text><text class="muted note">登记车位并发布空闲时间</text></view
        ><text>›</text></view
      >
      <view class="divider" />
      <view class="menu-row"
        ><view><text class="menu-title">隐私与规则</text><text class="muted note">阶段 2 免费共享版</text></view
        ><text>›</text></view
      >
    </view>
    <view class="role-card card"
      ><text class="muted">当前身份</text
      ><view class="roles"
        ><text v-for="role in authStore.user?.roles" :key="role">{{ role }}</text></view
      ></view
    >
    <button class="logout" @click="logout">切换演示身份</button>
  </view>
</template>

<style scoped lang="scss">
.profile-card {
  display: flex;
  align-items: center;
  gap: 26rpx;
  padding: 34rpx;
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #1d5039;
  border-radius: 50%;
  font-size: 42rpx;
  font-weight: 900;
}
.name,
.phone,
.menu-title,
.note {
  display: block;
}
.name {
  font-size: 36rpx;
  font-weight: 900;
}
.phone {
  margin-top: 10rpx;
}
.menu {
  padding: 0 28rpx;
}
.menu-row {
  min-height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.menu-title {
  font-weight: 800;
}
.note {
  margin-top: 8rpx;
  font-size: 23rpx;
}
.divider {
  height: 1rpx;
  background: #edf1ef;
}
.role-card {
  margin-top: 22rpx;
  padding: 28rpx;
}
.roles {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}
.roles text {
  padding: 8rpx 14rpx;
  color: #176746;
  background: #eaf5ef;
  border-radius: 999rpx;
  font-size: 21rpx;
}
.logout {
  margin-top: 34rpx;
  color: #425148;
  background: #e9eeeb;
}
</style>
