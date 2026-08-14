<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import type { Vehicle } from '@/types/api'

const vehicles = ref<Vehicle[]>([])
const form = reactive({ plateNumber: '', vehicleType: '小型轿车' })
const saving = ref(false)
onShow(load)

async function load() {
  try {
    vehicles.value = await api.vehicles()
  } catch (error) {
    showError(error)
  }
}
async function add() {
  if (!form.plateNumber.trim()) {
    uni.showToast({ title: '请输入车牌号', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await api.createVehicle(form)
    form.plateNumber = ''
    await load()
    uni.showToast({ title: '添加成功' })
  } catch (error) {
    showError(error)
  } finally {
    saving.value = false
  }
}
async function deactivate(id: number) {
  try {
    await api.deactivateVehicle(id)
    await load()
  } catch (error) {
    showError(error)
  }
}
</script>

<template>
  <view class="page">
    <view class="form-card card">
      <text class="form-title">添加车辆</text>
      <input v-model="form.plateNumber" class="input" maxlength="24" placeholder="例如：沪A·12345" />
      <picker
        :range="['小型轿车', 'SUV', 'MPV', '新能源车']"
        @change="form.vehicleType = ['小型轿车', 'SUV', 'MPV', '新能源车'][Number($event.detail.value)]"
      >
        <view class="input picker">{{ form.vehicleType }} <text>›</text></view>
      </picker>
      <button class="primary-button" :loading="saving" @click="add">添加车辆</button>
    </view>
    <text class="section-title">已绑定车辆</text>
    <view v-for="vehicle in vehicles" :key="vehicle.id" class="vehicle card" :class="{ inactive: !vehicle.active }">
      <view
        ><text class="plate">{{ vehicle.plateNumber }}</text
        ><text class="muted type">{{ vehicle.vehicleType }} · {{ vehicle.active ? '可用' : '已停用' }}</text></view
      >
      <button v-if="vehicle.active" size="mini" class="danger-button" @click="deactivate(vehicle.id)">停用</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.form-card {
  padding: 30rpx;
}
.form-title,
.plate,
.type {
  display: block;
}
.form-title {
  margin-bottom: 24rpx;
  font-size: 32rpx;
  font-weight: 850;
}
.input {
  height: 88rpx;
  margin-bottom: 18rpx;
  padding: 0 22rpx;
  box-sizing: border-box;
  background: #f3f6f4;
  border-radius: 16rpx;
}
.picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.vehicle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
  padding: 28rpx;
}
.plate {
  font-size: 31rpx;
  font-weight: 850;
}
.type {
  margin-top: 8rpx;
  font-size: 23rpx;
}
.inactive {
  opacity: 0.55;
}
.vehicle button {
  margin: 0;
}
</style>
