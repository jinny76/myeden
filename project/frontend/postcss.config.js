/**
 * PostCSS配置文件
 * 
 * 功能说明：
 * - 配置CSS后处理器
 * - 自动添加浏览器前缀
 * - CSS压缩和优化
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

export default {
  plugins: {
    // 自动添加浏览器前缀
    autoprefixer: {
      overrideBrowserslist: [
        '> 1%',
        'last 2 versions',
        'not dead',
        'not ie 11'
      ]
    },
    // CSS压缩和优化（仅在生产环境）
    ...(process.env.NODE_ENV === 'production' ? {
      cssnano: {
        preset: ['default', {
          discardComments: {
            removeAll: true
          },
          normalizeWhitespace: true
        }]
      }
    } : {})
  }
} 