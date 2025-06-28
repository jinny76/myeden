import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * Vite配置文件 - 性能优化版本
 * 
 * 功能说明：
 * - 配置Vue 3开发环境
 * - 自动导入Element Plus组件
 * - 配置路径别名
 * - 配置开发服务器
 * - 性能优化配置（代码分割、懒加载、压缩等）
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
export default defineConfig({
  plugins: [
    vue(),
    // 自动导入Vue API
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: true,
      eslintrc: {
        enabled: true
      }
    }),
    // 自动导入Element Plus组件
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true
    })
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@views': resolve(__dirname, 'src/views'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@api': resolve(__dirname, 'src/api'),
      '@assets': resolve(__dirname, 'src/assets')
    }
  },
  
  server: {
    host: '0.0.0.0',
    port: 35000,
    open: true,
    cors: true,
    proxy: {
      '/api': {
        target: 'http://localhost:38080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      },
      '/ws': {
        target: 'ws://localhost:38080',
        ws: true,
        changeOrigin: true
      }
    },
    allowedHosts: ['localhost', '127.0.0.1', '0.0.0.0', 'myeden.kingfisher.live']
  },
  
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.debug'],
        passes: 2
      },
      mangle: {
        toplevel: true
      }
    },
    rollupOptions: {
      output: {
        // 代码分割配置
        manualChunks: {
          // Vue核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus UI库
          'element-plus': ['element-plus'],
          // STOMP客户端
          'stomp-client': ['@stomp/stompjs'],
          // 工具库
          'utils': ['axios', 'dayjs']
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(css)$/.test(assetInfo.name)) {
            return `css/[name]-[hash].${ext}`
          }
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name)) {
            return `images/[name]-[hash].${ext}`
          }
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name)) {
            return `fonts/[name]-[hash].${ext}`
          }
          return `${ext}/[name]-[hash].${ext}`
        }
      }
    },
    // 构建优化配置
    chunkSizeWarningLimit: 1000,
    reportCompressedSize: false,
    // 启用CSS代码分割
    cssCodeSplit: true
  },
  
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/variables.scss";`
      }
    }
  },
  
  define: {
    __VUE_OPTIONS_API__: false,
    __VUE_PROD_DEVTOOLS__: false
  },

  // 优化配置
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus',
      'axios',
      '@stomp/stompjs',
      'dayjs'
    ],
    exclude: []
  },

  // 预构建配置
  preview: {
    port: 35001,
    open: true
  }
}) 