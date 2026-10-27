import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import pxToViewport from 'postcss-px-to-viewport-8-plugin'

// Dual-build config:
//   --mode full  → full bundle (replaces legacy apple-web-ui)
//   --mode lite  → lite bundle (replaces legacy apple-h5-ui, uses index-h5.html + px-to-vw)
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = mode === 'lite' ? 'lite' : (env.VITE_BUILD_TARGET || 'full')
  const isLite = target === 'lite'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    css: {
      postcss: isLite
        ? {
            plugins: [
              pxToViewport({
                unitToConvert: 'px',
                viewportWidth: 375,
                unitPrecision: 5,
                propList: ['*'],
                viewportUnit: 'vw',
                fontViewportUnit: 'vw',
                selectorBlackList: ['.ignore-'],
                minPixelValue: 2,
                mediaQuery: false,
                exclude: [/node_modules\/(?!vant)/]
              })
            ]
          }
        : {}
    },
    server: {
      host: '0.0.0.0',
      port: isLite ? 5175 : 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    build: {
      outDir: isLite ? 'dist-lite' : 'dist',
      sourcemap: false,
      target: 'es2020',
      chunkSizeWarningLimit: 1000,
      commonjsOptions: {
        transformMixedEsModules: true
      },
      rollupOptions: {
        input: isLite
          ? fileURLToPath(new URL('./index-h5.html', import.meta.url))
          : fileURLToPath(new URL('./index.html', import.meta.url)),
        // Tell Rollup to treat @apple/shared-core as external for treeshaking
        // and let Vite handle the pre-bundling instead
        treeshake: {
          moduleSideEffects: (id) => {
            // Don't treeshake shared-core - let it bundle as-is
            if (id.includes('apple-shared-core')) return true;
            return 'no-treeshake';
          }
        },
        output: isLite
          ? undefined
          : {
              manualChunks: {
                vendor: ['vue', 'vue-router', 'pinia'],
                vant: ['vant'],
                echarts: ['echarts']
              }
            }
      }
    },
    define: {
      __BUILD_TARGET__: JSON.stringify(target)
    }
  }
})
