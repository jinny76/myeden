/**
 * PWA 工具函数
 * 
 * 功能说明：
 * - 管理 Service Worker 注册和更新
 * - 处理推送通知
 * - 管理应用安装
 * - 提供PWA相关功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 检查是否支持 PWA
 */
export const isPWASupported = () => {
  return 'serviceWorker' in navigator && 'PushManager' in window
}

/**
 * 检查是否已安装为 PWA
 */
export const isPWAInstalled = () => {
  return window.matchMedia('(display-mode: standalone)').matches ||
         window.navigator.standalone === true
}

/**
 * 注册 Service Worker
 */
export const registerServiceWorker = async () => {
  if (!isPWASupported()) {
    throw new Error('浏览器不支持 PWA')
  }

  try {
    const registration = await navigator.serviceWorker.register('/sw.js')
    console.log('✅ Service Worker 注册成功:', registration)
    return registration
  } catch (error) {
    console.error('❌ Service Worker 注册失败:', error)
    throw error
  }
}

/**
 * 检查 Service Worker 更新
 */
export const checkForUpdate = async (registration) => {
  if (!registration) return false

  try {
    await registration.update()
    return true
  } catch (error) {
    console.error('❌ 检查更新失败:', error)
    return false
  }
}

/**
 * 应用 Service Worker 更新
 */
export const applyUpdate = async (registration) => {
  if (!registration || !registration.waiting) {
    return false
  }

  try {
    // 发送消息给等待中的 Service Worker
    registration.waiting.postMessage({ type: 'SKIP_WAITING' })
    return true
  } catch (error) {
    console.error('❌ 应用更新失败:', error)
    return false
  }
}

/**
 * 请求通知权限
 */
export const requestNotificationPermission = async () => {
  if (!('Notification' in window)) {
    throw new Error('浏览器不支持通知')
  }

  if (Notification.permission === 'granted') {
    return 'granted'
  }

  if (Notification.permission === 'denied') {
    throw new Error('通知权限已被拒绝')
  }

  const permission = await Notification.requestPermission()
  return permission
}

/**
 * 发送本地通知
 */
export const sendNotification = (title, options = {}) => {
  if (!('Notification' in window) || Notification.permission !== 'granted') {
    return null
  }

  const defaultOptions = {
    icon: '/icons/icon-192x192.png',
    badge: '/icons/badge-72x72.png',
    vibrate: [200, 100, 200],
    requireInteraction: false,
    ...options
  }

  const notification = new Notification(title, defaultOptions)

  // 点击通知时的处理
  notification.onclick = (event) => {
    event.preventDefault()
    window.focus()
    
    if (options.url) {
      window.location.href = options.url
    }
    
    notification.close()
  }

  return notification
}

/**
 * 订阅推送通知
 */
export const subscribeToPush = async (registration) => {
  if (!registration) {
    throw new Error('Service Worker 未注册')
  }

  try {
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(process.env.VUE_APP_VAPID_PUBLIC_KEY)
    })

    console.log('✅ 推送订阅成功:', subscription)
    return subscription
  } catch (error) {
    console.error('❌ 推送订阅失败:', error)
    throw error
  }
}

/**
 * 取消推送订阅
 */
export const unsubscribeFromPush = async (registration) => {
  if (!registration) {
    throw new Error('Service Worker 未注册')
  }

  try {
    const subscription = await registration.pushManager.getSubscription()
    if (subscription) {
      await subscription.unsubscribe()
      console.log('✅ 推送订阅已取消')
      return true
    }
    return false
  } catch (error) {
    console.error('❌ 取消推送订阅失败:', error)
    throw error
  }
}

/**
 * 检查推送订阅状态
 */
export const getPushSubscription = async (registration) => {
  if (!registration) {
    return null
  }

  try {
    return await registration.pushManager.getSubscription()
  } catch (error) {
    console.error('❌ 获取推送订阅失败:', error)
    return null
  }
}

/**
 * 显示安装提示
 */
export const showInstallPrompt = () => {
  // 检查是否支持安装
  if (!window.deferredPrompt) {
    return false
  }

  // 显示安装提示
  window.deferredPrompt.prompt()

  // 等待用户响应
  window.deferredPrompt.userChoice.then((choiceResult) => {
    if (choiceResult.outcome === 'accepted') {
      console.log('✅ 用户接受了安装')
    } else {
      console.log('❌ 用户拒绝了安装')
    }
    window.deferredPrompt = null
  })

  return true
}

/**
 * 监听安装事件
 */
export const listenForInstallEvent = (callback) => {
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    window.deferredPrompt = e
    
    if (callback) {
      callback(e)
    }
  })
}

/**
 * 监听应用安装完成
 */
export const listenForAppInstalled = (callback) => {
  window.addEventListener('appinstalled', (e) => {
    console.log('✅ PWA 已安装')
    if (callback) {
      callback(e)
    }
  })
}

/**
 * 将 VAPID 公钥转换为 Uint8Array
 */
const urlBase64ToUint8Array = (base64String) => {
  const padding = '='.repeat((4 - base64String.length % 4) % 4)
  const base64 = (base64String + padding)
    .replace(/-/g, '+')
    .replace(/_/g, '/')

  const rawData = window.atob(base64)
  const outputArray = new Uint8Array(rawData.length)

  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i)
  }
  return outputArray
}

/**
 * 获取缓存信息
 */
export const getCacheInfo = async () => {
  if (!('caches' in window)) {
    return null
  }

  try {
    const cacheNames = await caches.keys()
    const cacheInfo = []

    for (const cacheName of cacheNames) {
      const cache = await caches.open(cacheName)
      const keys = await cache.keys()
      cacheInfo.push({
        name: cacheName,
        size: keys.length
      })
    }

    return cacheInfo
  } catch (error) {
    console.error('❌ 获取缓存信息失败:', error)
    return null
  }
}

/**
 * 清理缓存
 */
export const clearCache = async (cacheName) => {
  if (!('caches' in window)) {
    return false
  }

  try {
    if (cacheName) {
      await caches.delete(cacheName)
      console.log(`✅ 缓存 ${cacheName} 已清理`)
    } else {
      const cacheNames = await caches.keys()
      await Promise.all(cacheNames.map(name => caches.delete(name)))
      console.log('✅ 所有缓存已清理')
    }
    return true
  } catch (error) {
    console.error('❌ 清理缓存失败:', error)
    return false
  }
}

/**
 * 获取 PWA 状态信息
 */
export const getPWAStatus = () => {
  return {
    supported: isPWASupported(),
    installed: isPWAInstalled(),
    notificationPermission: 'Notification' in window ? Notification.permission : 'unsupported',
    serviceWorker: 'serviceWorker' in navigator
  }
} 