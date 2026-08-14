import { TOKEN_KEY, USER_KEY } from '@/config/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

interface RequestOptions<T> {
  url: string
  method?: UniApp.RequestOptions['method']
  data?: T
}

export function request<R, T = unknown>({ url, method = 'GET', data }: RequestOptions<T>): Promise<R> {
  const token = uni.getStorageSync(TOKEN_KEY) as string
  return new Promise<R>((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}${url}`,
      method,
      data: data as UniApp.RequestOptions['data'],
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data as R)
          return
        }
        const error = response.data as { message?: string }
        if (response.statusCode === 401) {
          uni.removeStorageSync(TOKEN_KEY)
          uni.removeStorageSync(USER_KEY)
        }
        reject(new Error(error?.message || `请求失败（${response.statusCode}）`))
      },
      fail(error) {
        reject(new Error(error.errMsg || '网络连接失败'))
      },
    })
  })
}

export function showError(error: unknown) {
  uni.showToast({ title: error instanceof Error ? error.message : '操作失败', icon: 'none' })
}
