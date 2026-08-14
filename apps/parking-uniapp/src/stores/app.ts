import { defineStore } from 'pinia'
import { getRuntimePlatform, type RuntimePlatform } from '@/platform/runtime'

interface AppState {
  initialized: boolean
  platform: RuntimePlatform
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    initialized: false,
    platform: getRuntimePlatform(),
  }),
  actions: {
    markInitialized() {
      this.initialized = true
    },
  },
})
