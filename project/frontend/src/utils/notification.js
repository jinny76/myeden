/**
 * 通知工具函数
 * 兼容移动端和桌面端，优先用 Service Worker，权限不足时自动请求
 * @param {string} title 通知标题
 * @param {object} options 通知内容（body, icon, tag, data等）
 */
export function notify(title, options = {}) {
  // 判断 Notification API 是否可用
  if (!('Notification' in window)) {
    // 可选：降级为 Toast
    // showToast(title, options.body)
    return;
  }

  // 权限已授权
  if (Notification.permission === 'granted') {
    // 优先用 Service Worker
    if ('serviceWorker' in navigator && navigator.serviceWorker.ready) {
      navigator.serviceWorker.ready.then(reg => {
        reg.showNotification(title, options);
      });
    } else {
      // 桌面端或无 Service Worker 时降级
      new Notification(title, options);
    }
    return;
  }

  // 未授权，自动请求权限
  if (Notification.permission !== 'denied') {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        notify(title, options);
      }
    });
  }
}