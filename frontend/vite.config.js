import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  // 契约守护：生产构建禁止 Mock 进入产物
  if (mode === 'production' && env.VITE_ENABLE_MOCK !== 'off') {
    throw new Error(
      '[契约守护] 生产构建必须设置 VITE_ENABLE_MOCK=off，禁止 Mock 进入生产包'
    )
  }

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    }
  }
})
