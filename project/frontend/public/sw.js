/**
 * 我的伊甸园 - Service Worker
 * 
 * 功能说明：
 * - 缓存静态资源，提升加载速度
 * - 实现网络优先的缓存策略
 * - 处理推送通知
 * - 管理应用更新
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

const CACHE_NAME = 'myeden-v1.0.0'
const STATIC_CACHE_NAME = 'myeden-static-v1.0.0'
const DYNAMIC_CACHE_NAME = 'myeden-dynamic-v1.0.0'

// 需要缓存的静态资源
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/favicon.ico',
  '/logo.png',
  'icons/icon-72x72.png',
  'icons/icon-96x96.png',
  'icons/icon-128x128.png',
  'icons/icon-144x144.png',
  'icons/icon-152x152.png',
  'icons/icon-192x192.png',
  'icons/icon-384x384.png',
  'icons/icon-512x512.png'
]

// 需要缓存的API路径
const API_CACHE_PATTERNS = [
  '/api/posts',
  '/api/comments',
  '/api/users',
  '/api/robots'
]

// 不需要缓存的路径
const NO_CACHE_PATTERNS = [
  '/api/auth',
  '/api/websocket',
  '/api/upload'
]

/**
 * 安装事件 - 缓存静态资源
 */
self.addEventListener('install', (event) => {
  console.log('🔄 Service Worker 安装中...')
  
  event.waitUntil(
    caches.open(STATIC_CACHE_NAME)
      .then((cache) => {
        console.log('📦 缓存静态资源')
        return cache.addAll(STATIC_ASSETS)
      })
      .then(() => {
        console.log('✅ Service Worker 安装完成')
        return self.skipWaiting()
      })
      .catch((error) => {
        console.error('❌ Service Worker 安装失败:', error)
      })
  )
})

/**
 * 激活事件 - 清理旧缓存
 */
self.addEventListener('activate', (event) => {
  console.log('🚀 Service Worker 激活中...')
  
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            // 删除旧版本的缓存
            if (cacheName !== STATIC_CACHE_NAME && cacheName !== DYNAMIC_CACHE_NAME) {
              console.log('🗑️ 删除旧缓存:', cacheName)
              return caches.delete(cacheName)
            }
          })
        )
      })
      .then(() => {
        console.log('✅ Service Worker 激活完成')
        return self.clients.claim()
      })
  )
})

/**
 * 获取事件 - 处理网络请求
 */
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)
  
  // 跳过非GET请求
  if (request.method !== 'GET') {
    return
  }
  
  // 跳过非HTTP(S)请求
  if (!url.protocol.startsWith('http')) {
    return
  }
  
  // 处理静态资源请求
  if (isStaticAsset(request)) {
    event.respondWith(handleStaticAsset(request))
    return
  }
  
  // 处理API请求
  if (isApiRequest(request)) {
    event.respondWith(handleApiRequest(request))
    return
  }
  
  // 处理其他请求
  event.respondWith(handleOtherRequest(request))
})

/**
 * 判断是否为静态资源
 */
function isStaticAsset(request) {
  const url = new URL(request.url)
  return url.pathname.startsWith('/static/') ||
         url.pathname.startsWith('/assets/') ||
         url.pathname.startsWith('/icons/') ||
         url.pathname.startsWith('/images/') ||
         url.pathname.includes('icons/') ||
         url.pathname.endsWith('.js') ||
         url.pathname.endsWith('.css') ||
         url.pathname.endsWith('.png') ||
         url.pathname.endsWith('.jpg') ||
         url.pathname.endsWith('.jpeg') ||
         url.pathname.endsWith('.gif') ||
         url.pathname.endsWith('.svg')
}

/**
 * 判断是否为API请求
 */
function isApiRequest(request) {
  const url = new URL(request.url)
  return url.pathname.startsWith('/api/')
}

/**
 * 判断是否应该缓存
 */
function shouldCache(request) {
  const url = new URL(request.url)
  
  // 检查是否在不需要缓存的模式中
  for (const pattern of NO_CACHE_PATTERNS) {
    if (url.pathname.startsWith(pattern)) {
      return false
    }
  }
  
  // 检查是否在需要缓存的模式中
  for (const pattern of API_CACHE_PATTERNS) {
    if (url.pathname.startsWith(pattern)) {
      return true
    }
  }
  
  return false
}

/**
 * 处理静态资源请求 - 缓存优先策略
 */
async function handleStaticAsset(request) {
  try {
    // 首先尝试从缓存获取
    const cachedResponse = await caches.match(request)
    if (cachedResponse) {
      return cachedResponse
    }
    
    // 缓存中没有，从网络获取
    const networkResponse = await fetch(request)
    
    // 如果网络请求成功，缓存响应
    if (networkResponse.ok) {
      const cache = await caches.open(STATIC_CACHE_NAME)
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.error('❌ 静态资源请求失败:', error)
    throw error
  }
}

/**
 * 处理API请求 - 网络优先策略
 */
async function handleApiRequest(request) {
  // 如果不需要缓存，直接返回网络请求
  if (!shouldCache(request)) {
    return fetch(request)
  }
  
  try {
    // 首先尝试从网络获取
    const networkResponse = await fetch(request)
    
    // 如果网络请求成功，缓存响应
    if (networkResponse.ok) {
      const cache = await caches.open(DYNAMIC_CACHE_NAME)
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.log('🌐 网络请求失败，尝试从缓存获取:', error)
    
    // 网络失败，尝试从缓存获取
    const cachedResponse = await caches.match(request)
    if (cachedResponse) {
      return cachedResponse
    }
    
    // 缓存中也没有，抛出错误
    throw error
  }
}

/**
 * 处理其他请求
 */
async function handleOtherRequest(request) {
  try {
    return await fetch(request)
  } catch (error) {
    console.error('❌ 请求失败:', error)
    throw error
  }
}

/**
 * 推送事件 - 处理推送通知
 */
self.addEventListener('push', (event) => {
  console.log('📱 收到推送通知')
  
  if (event.data) {
    const data = event.data.json()
    
    const options = {
      body: data.body || '您有一条新消息',
      icon: '/icons/icon-192x192.png',
      badge: '/icons/badge-72x72.png',
      vibrate: [200, 100, 200],
      data: {
        url: data.url || '/',
        timestamp: Date.now()
      },
      actions: [
        {
          action: 'view',
          title: '查看',
          icon: '/icons/action-view.png'
        },
        {
          action: 'close',
          title: '关闭',
          icon: '/icons/action-close.png'
        }
      ]
    }
    
    event.waitUntil(
      self.registration.showNotification(data.title || '我的伊甸园', options)
    )
  }
})

/**
 * 通知点击事件
 */
self.addEventListener('notificationclick', (event) => {
  console.log('👆 通知被点击:', event.action)
  
  event.notification.close()
  
  if (event.action === 'view' || event.action === '') {
    event.waitUntil(
      clients.openWindow(event.notification.data.url)
    )
  }
})

/**
 * 消息事件 - 处理来自主线程的消息
 */
self.addEventListener('message', (event) => {
  console.log('💬 收到主线程消息:', event.data)
  
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting()
  }
  
  if (event.data && event.data.type === 'CACHE_UPDATED') {
    // 处理缓存更新
    console.log('🔄 缓存已更新')
  }
})

/**
 * 同步事件 - 处理后台同步
 */
self.addEventListener('sync', (event) => {
  console.log('🔄 后台同步:', event.tag)
  
  if (event.tag === 'background-sync') {
    event.waitUntil(doBackgroundSync())
  }
})

/**
 * 执行后台同步
 */
async function doBackgroundSync() {
  try {
    // 这里可以添加需要后台同步的逻辑
    // 例如：同步离线数据、发送待发送的消息等
    console.log('🔄 执行后台同步...')
    
    // 模拟同步过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    console.log('✅ 后台同步完成')
  } catch (error) {
    console.error('❌ 后台同步失败:', error)
  }
} 