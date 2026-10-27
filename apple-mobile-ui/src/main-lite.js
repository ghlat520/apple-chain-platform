// Lite bundle entry (BUILD_TARGET=lite).
// Parallel to src/main.js (full bundle). Uses lib-flexible + shared-core api wiring.
import 'lib-flexible'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { showToast } from 'vant'
import 'vant/lib/index.css'
import '@/design' // shared tokens + Vant theme overrides

import { configureApi } from '@apple/shared-core'
import App from './App.vue'
import router from './router/index.js'

// Wire shared-core HTTP client to Vant toasts + Vue router navigation.
configureApi({
  baseURL: '/api',
  timeout: 15000,
  getToken: () => localStorage.getItem('h5_token'),
  onUnauthorized: () => {
    localStorage.removeItem('h5_token')
    localStorage.removeItem('h5_user')
    if (router.currentRoute.value.path !== '/h5/login') {
      void router.replace({
        path: '/h5/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }
  },
  onError: (message) => {
    showToast({ type: 'fail', message, duration: 2500 })
  }
})

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
