package com.myeden.service;

import com.myeden.entity.Robot;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 机器人管理服务接口
 * 
 * 功能说明：
 * - 提供机器人基础CRUD操作
 * - 支持机器人权限管理和所有者控制
 * - 提供机器人查询和统计功能
 * - 支持软删除和恢复功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface RobotService {
    
    /**
     * 根据ID获取机器人
     * @param id 机器人ID
     * @return 机器人信息
     */
    Robot getRobotById(String id);
    
    /**
     * 根据机器人ID获取机器人
     * @param robotId 机器人ID
     * @return 机器人信息
     */
    Robot getRobotByRobotId(String robotId);
    
    /**
     * 保存机器人
     * @param robot 机器人信息
     * @return 保存后的机器人
     */
    Robot saveRobot(Robot robot);
    
    /**
     * 删除机器人（软删除）
     * @param id 机器人ID
     * @return 是否成功
     */
    boolean deleteRobot(String id);
    
    /**
     * 恢复机器人
     * @param id 机器人ID
     * @return 是否成功
     */
    boolean restoreRobot(String id);
    
    /**
     * 根据所有者获取机器人列表
     * @param owner 所有者ID
     * @return 机器人列表
     */
    List<Robot> getRobotsByOwner(String owner);
    
    /**
     * 根据所有者和删除状态获取机器人列表
     * @param owner 所有者ID
     * @param isDeleted 是否删除
     * @return 机器人列表
     */
    List<Robot> getRobotsByOwnerAndDeleted(String owner, Boolean isDeleted);
    
    /**
     * 获取所有激活的机器人
     * @return 激活的机器人列表
     */
    List<Robot> getActiveRobots();
    
    /**
     * 获取所有机器人
     * @return 所有机器人列表
     */
    List<Robot> getAllRobots();
    
    /**
     * 检查机器人名称是否存在
     * @param name 机器人名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查机器人ID是否存在
     * @param robotId 机器人ID
     * @return 是否存在
     */
    boolean existsByRobotId(String robotId);
    
    /**
     * 根据所有者统计机器人数量
     * @param owner 所有者ID
     * @return 机器人数量
     */
    long countByOwner(String owner);
    
    /**
     * 根据所有者和删除状态统计机器人数量
     * @param owner 所有者ID
     * @param isDeleted 是否删除
     * @return 机器人数量
     */
    long countByOwnerAndDeleted(String owner, Boolean isDeleted);
    
    /**
     * 统计所有机器人数量
     * @return 机器人总数
     */
    long countAllRobots();
    
    /**
     * 统计激活的机器人数量
     * @return 激活的机器人数量
     */
    long countActiveRobots();
    
    /**
     * 上传机器人头像
     * @param robotId 机器人ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(String robotId, MultipartFile file);
} 