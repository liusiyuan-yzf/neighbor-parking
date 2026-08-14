<template>
  <view class="page">
    <view class="status-mark" />
    <text class="title">{{ APP_NAME }}</text>
    <text class="summary">车位共享平台工程骨架已初始化</text>

    <view class="divider" />

    <view class="details">
      <view class="detail-row">
        <text class="detail-label">当前阶段</text>
        <text class="detail-value">MVP 方案设计</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">首发平台</text>
        <text class="detail-value">微信小程序</text>
      </view>
      <view class="detail-row">
        <text class="detail-label">运行平台</text>
        <text class="detail-value">{{ appStore.platform }}</text>
      </view>
    </view>

    <text class="notice">业务界面将在视觉方向确认后开始实现。</text>
  </view>
</template>

<script setup lang="ts">
import { APP_NAME } from '@/config/app'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style lang="scss" scoped>
.page {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 100vh;
  padding: 64rpx 48rpx;
}

.status-mark {
  width: 64rpx;
  height: 8rpx;
  margin-bottom: 40rpx;
  background: #087f5b;
  border-radius: 4rpx;
}

.title {
  font-size: 56rpx;
  font-weight: 700;
  line-height: 1.2;
  color: #17211d;
}

.summary {
  margin-top: 20rpx;
  font-size: 30rpx;
  line-height: 1.6;
  color: #58635e;
}

.divider {
  height: 1rpx;
  margin: 56rpx 0 40rpx;
  background: #dbe2de;
}

.details {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 32rpx;
  font-size: 28rpx;
  line-height: 1.5;
}

.detail-label {
  color: #7a8580;
}

.detail-value {
  color: #26312c;
  text-align: right;
}

.notice {
  margin-top: 64rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #7a8580;
}
</style>
