/**
 * API 模块统一导出
 * 
 * 功能说明：
 * - 统一导出所有API模块
 * - 提供HTTP请求服务
 * - 方便其他模块导入使用
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-16
 */

// 导出HTTP请求服务
export { default as api } from '@/utils/request'

// 导出各个API模块
export * from './user'
export * from './chat'
export * from './activity'
export * from './communication'
export * from './userRobotLink'
export * from './tts'
export * from './robot'
export * from './robotEditor'
export * from './userSetting'
export * from './post'
export * from './world'
export * from './websocket'
export * from './comment'
export * from './chatroom'

// 默认导出HTTP请求服务
export { default } from '@/utils/request' 