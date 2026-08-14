<script setup lang="ts">
import { ref } from 'vue'
import { showError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loadingId = ref(0)

async function login(userId: number) {
  loadingId.value = userId
  try {
    await authStore.login(userId)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (error) {
    showError(error)
  } finally {
    loadingId.value = 0
  }
}
</script>

<template>
  <view class="login-page">
    <view class="brand-mark">P</view><text class="brand">邻里车位</text
    ><text class="slogan">让闲置车位，在邻里之间流动起来</text>
    <view class="login-card card">
      <text class="card-title">本地演示登录</text>
      <text class="muted description">阶段 2 使用演示身份联调，生产环境会关闭此入口并接入微信/手机号认证。</text>
      <button class="primary-button" :loading="loadingId === 1" @click="login(1)">以租用人小林登录</button>
      <button class="secondary-button" :loading="loadingId === 2" @click="login(2)">以车位主王阿姨登录</button>
      <button class="admin-button" :loading="loadingId === 3" @click="login(3)">以物业管理员登录</button>
    </view>
    <text class="privacy">登录即代表同意演示版隐私说明。平台只展示预约所需的最少信息。</text>
  </view>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: calc(var(--status-bar-height) + 110rpx) 44rpx 60rpx;
  background: #eef5f1;
}
.brand-mark {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #16794f;
  border-radius: 28rpx;
  font-size: 50rpx;
  font-weight: 900;
}
.brand,
.slogan,
.card-title {
  display: block;
}
.brand {
  margin-top: 36rpx;
  font-size: 54rpx;
  font-weight: 900;
}
.slogan {
  margin-top: 14rpx;
  color: #597066;
  font-size: 29rpx;
}
.login-card {
  margin-top: 80rpx;
  padding: 38rpx;
}
.card-title {
  font-size: 34rpx;
  font-weight: 850;
}
.description {
  display: block;
  margin: 16rpx 0 34rpx;
  line-height: 1.7;
}
button {
  margin-top: 20rpx;
}
.admin-button {
  color: #405047;
  background: #f0f2f1;
}
.privacy {
  display: block;
  margin-top: 36rpx;
  color: #829087;
  text-align: center;
  font-size: 22rpx;
  line-height: 1.6;
}
</style>
