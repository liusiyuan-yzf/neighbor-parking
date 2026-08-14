<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '@/api'
import { showError } from '@/api/http'
import type { Community } from '@/types/api'

const communities = ref<Community[]>([])
const editId = ref(0)
const communityIndex = ref(0)
const saving = ref(false)
const form = reactive({ spaceCode: '', title: '', accessInstructions: '', vehicleLimit: '小型及紧凑型车辆' })

onLoad(async (query) => {
  try {
    communities.value = await api.communities()
    editId.value = Number(query?.id || 0)
    if (editId.value) {
      const space = (await api.ownerSpaces()).find((item) => item.id === editId.value)
      if (!space) throw new Error('车位不存在')
      communityIndex.value = Math.max(
        0,
        communities.value.findIndex((item) => item.id === space.communityId),
      )
      Object.assign(form, {
        spaceCode: space.spaceCode,
        title: space.title,
        accessInstructions: space.accessInstructions,
        vehicleLimit: space.vehicleLimit,
      })
    }
  } catch (error) {
    showError(error)
  }
})

async function save() {
  const community = communities.value[communityIndex.value]
  if (!community || !form.spaceCode.trim() || !form.title.trim() || !form.accessInstructions.trim()) {
    uni.showToast({ title: '请完整填写车位信息', icon: 'none' })
    return
  }
  saving.value = true
  try {
    const payload = { communityId: community.id, ...form }
    if (editId.value) await api.updateSpace(editId.value, payload)
    else await api.createSpace(payload)
    uni.showToast({ title: editId.value ? '已更新并重新提交' : '已提交审核' })
    setTimeout(() => uni.navigateBack(), 600)
  } catch (error) {
    showError(error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="notice">车位号和入场说明只会在预约成功后展示给租用人；提交后需物业审核。</view>
    <view class="form card">
      <text class="label">所在小区</text>
      <picker :range="communities" range-key="name" @change="communityIndex = Number($event.detail.value)"
        ><view class="input picker">{{ communities[communityIndex]?.name || '选择小区' }}<text>›</text></view></picker
      >
      <text class="label">车位编号</text
      ><input v-model="form.spaceCode" class="input" placeholder="例如 B2-128" maxlength="64" />
      <text class="label">展示标题</text
      ><input v-model="form.title" class="input" placeholder="例如 近 2 号门地下车位" maxlength="128" />
      <text class="label">车辆限制</text><input v-model="form.vehicleLimit" class="input" maxlength="64" />
      <text class="label">入场说明</text
      ><textarea
        v-model="form.accessInstructions"
        class="textarea"
        placeholder="请说明入口、楼层和物业通行方式"
        maxlength="1000"
      />
      <button class="primary-button" :loading="saving" @click="save">
        {{ editId ? '更新并重新提交' : '提交物业审核' }}
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.notice {
  margin-bottom: 22rpx;
  padding: 22rpx;
  color: #52645a;
  background: #eaf4ef;
  border-radius: 18rpx;
  font-size: 24rpx;
  line-height: 1.6;
}
.form {
  padding: 30rpx;
}
.label {
  display: block;
  margin: 22rpx 0 12rpx;
  color: #56665d;
  font-weight: 750;
}
.label:first-child {
  margin-top: 0;
}
.input,
.textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 22rpx;
  background: #f3f6f4;
  border-radius: 16rpx;
}
.input {
  height: 88rpx;
}
.picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.textarea {
  height: 200rpx;
}
.form button {
  margin-top: 34rpx;
}
</style>
