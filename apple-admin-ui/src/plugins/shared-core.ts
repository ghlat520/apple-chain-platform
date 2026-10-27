import { configureApi } from '@apple/shared-core'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

export function setupSharedCore() {
  configureApi({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 15000,
    getToken: () => localStorage.getItem('token'),
    onUnauthorized: () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        router.replace({
          path: '/login',
          query: { redirect: current.fullPath }
        })
      }
    },
    onError: (message: string) => {
      ElMessage.error(message)
    }
  })
}
