import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const bffUrl = env.VITE_BFF_URL || 'http://localhost:8082'

  return {
    plugins: [vue()],
    server: {
      host: true,
      port: 5000,
      proxy: {
        '/bff': {
          target: bffUrl,
          rewrite: path => path.replace(/^\/bff/, ''),
          changeOrigin: true
        }
      }
    }
  }
})
