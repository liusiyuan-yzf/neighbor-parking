<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

onLaunch(() => {
  appStore.markInitialized()
})
</script>

<style lang="scss">
page {
  min-height: 100%;
  background: #f4f7f5;
  color: #16231d;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Segoe UI', sans-serif;
  font-size: 28rpx;
}

button {
  border-radius: 18rpx;
  font-weight: 700;
}

button::after {
  border: 0;
}

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 28rpx;
}

.card {
  background: #ffffff;
  border: 1rpx solid #dfe8e3;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(29, 64, 46, 0.06);
}

.primary-button {
  color: #ffffff;
  background: #16794f;
}

.secondary-button {
  color: #146a46;
  background: #eaf5ef;
}

.danger-button {
  color: #a13636;
  background: #fff0f0;
}

.muted {
  color: #6c7c73;
}

.section-title {
  margin: 28rpx 0 18rpx;
  font-size: 34rpx;
  font-weight: 800;
}
</style>
