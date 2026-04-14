import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router/index.js'

// Design system — 必须放在 element-plus/dist/index.css 之后，
// 才能用 :root CSS 变量覆盖 Element Plus 默认主题。
import './design/element-theme.css'
import './design/global.css'

import './styles/index.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: undefined }) // uses default zh-CN from element-plus

// Register all Element Plus icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
