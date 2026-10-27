import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './styles/main.css'
import { isMockDisabled } from './mock/shouldMock'

// 按需导入 Mock 数据层：off 模式下不打包到生产 chunk
async function bootstrap() {
  if (!isMockDisabled()) {
    await import('./mock')
  }

  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.use(Vant)
  app.mount('#app')
}

bootstrap()
