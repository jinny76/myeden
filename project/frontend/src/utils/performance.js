/**
 * 前端性能优化工具类
 * 包含懒加载、图片优化、缓存管理、性能监控等功能
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */

/**
 * 图片懒加载工具
 */
export class ImageLazyLoader {
  constructor() {
    this.observer = null
    this.initIntersectionObserver()
  }

  /**
   * 初始化Intersection Observer
   */
  initIntersectionObserver() {
    if ('IntersectionObserver' in window) {
      this.observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target
            this.loadImage(img)
            this.observer.unobserve(img)
          }
        })
      }, {
        rootMargin: '50px 0px',
        threshold: 0.01
      })
    }
  }

  /**
   * 加载图片
   */
  loadImage(img) {
    const src = img.dataset.src
    if (src) {
      img.src = src
      img.classList.remove('lazy')
      img.classList.add('loaded')
    }
  }

  /**
   * 观察图片元素
   */
  observe(img) {
    if (this.observer) {
      this.observer.observe(img)
    } else {
      // 降级处理：直接加载
      this.loadImage(img)
    }
  }

  /**
   * 停止观察
   */
  disconnect() {
    if (this.observer) {
      this.observer.disconnect()
    }
  }
}

/**
 * 组件懒加载工具
 */
export class ComponentLazyLoader {
  /**
   * 创建懒加载组件
   */
  static createLazyComponent(importFunc, loadingComponent = null, errorComponent = null) {
    return {
      component: () => importFunc(),
      loading: loadingComponent,
      error: errorComponent,
      delay: 200,
      timeout: 10000
    }
  }

  /**
   * 预加载组件
   */
  static preloadComponent(importFunc) {
    return importFunc()
  }
}

/**
 * 缓存管理工具
 */
export class CacheManager {
  constructor() {
    this.memoryCache = new Map()
    this.maxMemorySize = 100
  }

  /**
   * 设置内存缓存
   */
  set(key, value, ttl = 300000) { // 默认5分钟
    const item = {
      value,
      timestamp: Date.now(),
      ttl
    }
    
    // 清理过期缓存
    this.cleanup()
    
    // 检查内存大小
    if (this.memoryCache.size >= this.maxMemorySize) {
      this.evictOldest()
    }
    
    this.memoryCache.set(key, item)
  }

  /**
   * 获取内存缓存
   */
  get(key) {
    const item = this.memoryCache.get(key)
    if (!item) return null
    
    // 检查是否过期
    if (Date.now() - item.timestamp > item.ttl) {
      this.memoryCache.delete(key)
      return null
    }
    
    return item.value
  }

  /**
   * 删除缓存
   */
  delete(key) {
    return this.memoryCache.delete(key)
  }

  /**
   * 清理过期缓存
   */
  cleanup() {
    const now = Date.now()
    for (const [key, item] of this.memoryCache.entries()) {
      if (now - item.timestamp > item.ttl) {
        this.memoryCache.delete(key)
      }
    }
  }

  /**
   * 淘汰最旧的缓存
   */
  evictOldest() {
    let oldestKey = null
    let oldestTime = Date.now()
    
    for (const [key, item] of this.memoryCache.entries()) {
      if (item.timestamp < oldestTime) {
        oldestTime = item.timestamp
        oldestKey = key
      }
    }
    
    if (oldestKey) {
      this.memoryCache.delete(oldestKey)
    }
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.memoryCache.clear()
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      size: this.memoryCache.size,
      maxSize: this.maxMemorySize,
      hitRate: this.calculateHitRate()
    }
  }

  /**
   * 计算命中率
   */
  calculateHitRate() {
    // 这里可以实现更复杂的命中率计算
    return 0.85 // 示例值
  }
}

/**
 * 性能监控工具
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {
      pageLoadTime: 0,
      domContentLoaded: 0,
      firstContentfulPaint: 0,
      largestContentfulPaint: 0,
      cumulativeLayoutShift: 0,
      firstInputDelay: 0
    }
    this.init()
  }

  /**
   * 初始化性能监控
   */
  init() {
    this.observePageLoad()
    this.observeWebVitals()
    this.observeUserInteractions()
  }

  /**
   * 观察页面加载性能
   */
  observePageLoad() {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0]
      if (navigation) {
        this.metrics.pageLoadTime = navigation.loadEventEnd - navigation.loadEventStart
        this.metrics.domContentLoaded = navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart
      }
    })
  }

  /**
   * 观察Web Vitals指标
   */
  observeWebVitals() {
    // First Contentful Paint
    new PerformanceObserver((list) => {
      const entries = list.getEntries()
      if (entries.length > 0) {
        this.metrics.firstContentfulPaint = entries[entries.length - 1].startTime
      }
    }).observe({ entryTypes: ['paint'] })

    // Largest Contentful Paint
    new PerformanceObserver((list) => {
      const entries = list.getEntries()
      if (entries.length > 0) {
        this.metrics.largestContentfulPaint = entries[entries.length - 1].startTime
      }
    }).observe({ entryTypes: ['largest-contentful-paint'] })

    // Cumulative Layout Shift
    new PerformanceObserver((list) => {
      let cls = 0
      for (const entry of list.getEntries()) {
        if (!entry.hadRecentInput) {
          cls += entry.value
        }
      }
      this.metrics.cumulativeLayoutShift = cls
    }).observe({ entryTypes: ['layout-shift'] })

    // First Input Delay
    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        this.metrics.firstInputDelay = entry.processingStart - entry.startTime
        break
      }
    }).observe({ entryTypes: ['first-input'] })
  }

  /**
   * 观察用户交互
   */
  observeUserInteractions() {
    let interactionCount = 0
    const events = ['click', 'input', 'scroll', 'keydown']
    
    events.forEach(eventType => {
      document.addEventListener(eventType, () => {
        interactionCount++
        this.recordInteraction(eventType)
      }, { passive: true })
    })
  }

  /**
   * 记录用户交互
   */
  recordInteraction(type) {
    // 可以发送到分析服务
    console.log(`用户交互: ${type}`)
  }

  /**
   * 获取性能指标
   */
  getMetrics() {
    return { ...this.metrics }
  }

  /**
   * 发送性能数据
   */
  sendMetrics() {
    const metrics = this.getMetrics()
    // 这里可以发送到后端或分析服务
    console.log('性能指标:', metrics)
  }
}

/**
 * 资源预加载工具
 */
export class ResourcePreloader {
  /**
   * 预加载图片
   */
  static preloadImage(src) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(img)
      img.onerror = reject
      img.src = src
    })
  }

  /**
   * 预加载CSS
   */
  static preloadCSS(href) {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link')
      link.rel = 'preload'
      link.as = 'style'
      link.href = href
      link.onload = () => resolve(link)
      link.onerror = reject
      document.head.appendChild(link)
    })
  }

  /**
   * 预加载JavaScript
   */
  static preloadJS(src) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = src
      script.onload = () => resolve(script)
      script.onerror = reject
      document.head.appendChild(script)
    })
  }

  /**
   * 预加载字体
   */
  static preloadFont(href) {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link')
      link.rel = 'preload'
      link.as = 'font'
      link.href = href
      link.crossOrigin = 'anonymous'
      link.onload = () => resolve(link)
      link.onerror = reject
      document.head.appendChild(link)
    })
  }
}

/**
 * 防抖工具
 */
export function debounce(func, wait, immediate = false) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func(...args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func(...args)
  }
}

/**
 * 节流工具
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 创建全局实例
 */
export const imageLazyLoader = new ImageLazyLoader()
export const cacheManager = new CacheManager()
export const performanceMonitor = new PerformanceMonitor()

/**
 * 初始化性能优化
 */
export function initPerformanceOptimization() {
  console.log('性能优化工具初始化完成')
  
  // 定期清理缓存
  setInterval(() => {
    cacheManager.cleanup()
  }, 60000) // 每分钟清理一次
  
  // 定期发送性能指标
  setInterval(() => {
    performanceMonitor.sendMetrics()
  }, 300000) // 每5分钟发送一次
} 