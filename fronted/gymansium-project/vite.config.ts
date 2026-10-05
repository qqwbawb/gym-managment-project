import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0', // 允许任何ip都可以访问当前项目
    port: 8080, // 项目的端口号
    hmr: true, // 启用热加载
    open: true // 项目启动之后自动打开浏览器
  },
  resolve: {
    alias: {
      // 新版 Vite(ESM) 使用 fileURLToPath 替代 __dirname，兼容性更好
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  define: {
    'process.env': {
      'BASE_API': "http://localhost:8089"
    }
  }
})
