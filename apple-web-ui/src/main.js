import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import '@/design'  // Design System OS — tokens + Vant theme override
import App from './App.vue'
import router from './router/index.js'
import { installPermDirective } from './directives/perm.js'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vant)
installPermDirective(app)
app.mount('#app')
