/**
 * Service Worker 负责后台推送通知和处理通知点击事件
 * 适配移动端和桌面端
 */

// 监听通知点击事件
self.addEventListener('notificationclick', function(event) {
  event.notification.close();
  // 可根据 data.url 跳转到指定页面
  const url = event.notification.data && event.notification.data.url ? event.notification.data.url : '/';
  event.waitUntil(
    clients.matchAll({ type: 'window' }).then(windowClients => {
      // 如果已打开目标页面则聚焦，否则新开
      for (let client of windowClients) {
        if (client.url.includes(url) && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(url);
      }
    })
  );
}); 