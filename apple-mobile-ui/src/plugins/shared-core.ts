import { configureApi } from '@apple/shared-core'
import { showToast } from 'vant'
import router from '@/router/index.js'

export function setupSharedCore() {
  configureApi({
    baseURL: '/api',
    timeout: 15000,
    getToken: () => localStorage.getItem('token'),
    onUnauthorized: () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    },
    onError: (message: string) => {
      showToast({ type: 'fail', message, duration: 2500 })
    }
  })
}
