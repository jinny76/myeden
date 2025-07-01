import request from '@/utils/request'

/**
 * 机器人编辑相关 API
 * 
 * 功能说明：
 * - 提供机器人创建、编辑、删除等操作
 * - 支持用户权限验证
 * - 包含完整的 CRUD 操作
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * 获取用户拥有的机器人列表
 * @returns {Promise} 机器人列表
 */
export function getMyRobots() {
  return request({
    url: '/robots/my-robots',
    method: 'get'
  })
}

/**
 * 获取机器人编辑信息
 * @param {string} robotId 机器人ID
 * @returns {Promise} 机器人编辑信息
 */
export function getRobotForEdit(robotId) {
  return request({
    url: `/robots/${robotId}/edit`,
    method: 'get'
  })
}

/**
 * 创建新机器人
 * @param {Object} robotData 机器人数据
 * @returns {Promise} 创建结果
 */
export function createRobot(robotData) {
  return request({
    url: '/robots/create',
    method: 'post',
    data: robotData
  })
}

/**
 * 更新机器人信息
 * @param {string} robotId 机器人ID
 * @param {Object} robotData 机器人数据
 * @returns {Promise} 更新结果
 */
export function updateRobot(robotId, robotData) {
  return request({
    url: `/robots/${robotId}/update`,
    method: 'put',
    data: robotData
  })
}

/**
 * 删除机器人（软删除）
 * @param {string} robotId 机器人ID
 * @returns {Promise} 删除结果
 */
export function deleteRobot(robotId) {
  return request({
    url: `/robots/${robotId}`,
    method: 'delete'
  })
}

/**
 * 恢复机器人
 * @param {string} robotId 机器人ID
 * @returns {Promise} 恢复结果
 */
export function restoreRobot(robotId) {
  return request({
    url: `/robots/${robotId}/restore`,
    method: 'post'
  })
}

/**
 * 复制机器人
 * @param {string} robotId 机器人ID
 * @param {string} newName 新机器人名称
 * @returns {Promise} 复制结果
 */
export function copyRobot(robotId, newName) {
  return request({
    url: `/robots/${robotId}/copy`,
    method: 'post',
    data: { newName }
  })
}

/**
 * 上传机器人头像
 * @param {string} robotId 机器人ID
 * @param {FormData} formData 文件数据
 * @returns {Promise} 上传结果
 */
export function uploadRobotAvatar(robotId, formData) {
  return request({
    url: `/robots/${robotId}/avatar`,
    method: 'post',
    data: formData
  })
} 