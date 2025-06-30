// 主题切换工具
export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('theme', theme)
}

export function getTheme() {
  return localStorage.getItem('theme') || 'dark'
}

export function initTheme() {
  setTheme(getTheme())
} 