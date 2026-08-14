import { defineStore } from 'pinia'
import { api } from '@/api'
import { TOKEN_KEY, USER_KEY } from '@/config/auth'
import type { User } from '@/types/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (uni.getStorageSync(TOKEN_KEY) as string) || '',
    user: (uni.getStorageSync(USER_KEY) as User | undefined) || undefined,
  }),
  getters: {
    loggedIn: (state) => Boolean(state.token),
    isOwner: (state) => Boolean(state.user?.roles.includes('OWNER')),
  },
  actions: {
    async login(userId: number) {
      const response = await api.devLogin(userId)
      this.token = response.accessToken
      this.user = response.user
      uni.setStorageSync(TOKEN_KEY, this.token)
      uni.setStorageSync(USER_KEY, this.user)
    },
    async refresh() {
      if (!this.token) return
      try {
        this.user = await api.me()
        uni.setStorageSync(USER_KEY, this.user)
      } catch (error) {
        this.logout()
        throw error
      }
    },
    logout() {
      this.token = ''
      this.user = undefined
      uni.removeStorageSync(TOKEN_KEY)
      uni.removeStorageSync(USER_KEY)
    },
  },
})
