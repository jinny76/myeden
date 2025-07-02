package com.myeden.service.impl;

import com.myeden.entity.Robot;
import com.myeden.repository.RobotRepository;
import com.myeden.service.FileService;
import com.myeden.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 机器人管理服务实现类
 * 
 * 功能说明：
 * - 实现机器人基础CRUD操作
 * - 支持机器人权限管理和所有者控制
 * - 提供机器人查询和统计功能
 * - 支持软删除和恢复功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class RobotServiceImpl implements RobotService {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotServiceImpl.class);
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private FileService fileService;
    
    @Override
    public Robot getRobotById(String id) {
        try {
            Optional<Robot> robot = robotRepository.findById(id);
            return robot.orElse(null);
        } catch (Exception e) {
            logger.error("根据ID获取机器人失败: {}", id, e);
            return null;
        }
    }
    
    @Override
    public Robot getRobotByRobotId(String robotId) {
        try {
            Optional<Robot> robot = robotRepository.findByRobotId(robotId);
            return robot.orElse(null);
        } catch (Exception e) {
            logger.error("根据机器人ID获取机器人失败: {}", robotId, e);
            return null;
        }
    }
    
    @Override
    public Robot saveRobot(Robot robot) {
        try {
            // 设置更新时间
            robot.setUpdatedAt(java.time.LocalDateTime.now());
            
            Robot savedRobot = robotRepository.save(robot);
            logger.info("保存机器人成功: {}", savedRobot.getRobotId());
            return savedRobot;
        } catch (Exception e) {
            logger.error("保存机器人失败: {}", robot.getRobotId(), e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteRobot(String id) {
        try {
            Robot robot = getRobotById(id);
            if (robot == null) {
                logger.warn("要删除的机器人不存在: {}", id);
                return false;
            }
            
            // 软删除
            robot.softDelete();
            robotRepository.save(robot);
            
            logger.info("软删除机器人成功: {}", robot.getRobotId());
            return true;
        } catch (Exception e) {
            logger.error("删除机器人失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean restoreRobot(String id) {
        try {
            Robot robot = getRobotById(id);
            if (robot == null) {
                logger.warn("要恢复的机器人不存在: {}", id);
                return false;
            }
            
            // 恢复机器人
            robot.restore();
            robotRepository.save(robot);
            
            logger.info("恢复机器人成功: {}", robot.getRobotId());
            return true;
        } catch (Exception e) {
            logger.error("恢复机器人失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public List<Robot> getRobotsByOwner(String owner) {
        try {
            List<Robot> robots = robotRepository.findByOwner(owner);
            logger.debug("获取用户 {} 的机器人列表，数量: {}", owner, robots.size());
            return robots;
        } catch (Exception e) {
            logger.error("获取用户机器人列表失败: {}", owner, e);
            return List.of();
        }
    }
    
    @Override
    public List<Robot> getRobotsByOwnerAndDeleted(String owner, Boolean isDeleted) {
        try {
            List<Robot> robots = robotRepository.findByOwnerAndIsDeleted(owner, isDeleted);
            logger.debug("获取用户 {} 的机器人列表（删除状态: {}），数量: {}", owner, isDeleted, robots.size());
            return robots;
        } catch (Exception e) {
            logger.error("获取用户机器人列表失败: {}, 删除状态: {}", owner, isDeleted, e);
            return List.of();
        }
    }
    
    @Override
    public List<Robot> getActiveRobots() {
        try {
            List<Robot> robots = robotRepository.findByIsActiveTrue();
            logger.debug("获取激活机器人列表，数量: {}", robots.size());
            return robots;
        } catch (Exception e) {
            logger.error("获取激活机器人列表失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<Robot> getAllRobots() {
        try {
            List<Robot> robots = robotRepository.findAll();
            logger.debug("获取所有机器人列表，数量: {}", robots.size());
            return robots;
        } catch (Exception e) {
            logger.error("获取所有机器人列表失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean existsByName(String name) {
        try {
            return robotRepository.existsByName(name);
        } catch (Exception e) {
            logger.error("检查机器人名称是否存在失败: {}", name, e);
            return false;
        }
    }
    
    @Override
    public boolean existsByRobotId(String robotId) {
        try {
            return robotRepository.existsByRobotId(robotId);
        } catch (Exception e) {
            logger.error("检查机器人ID是否存在失败: {}", robotId, e);
            return false;
        }
    }
    
    @Override
    public long countByOwner(String owner) {
        try {
            return robotRepository.countByOwner(owner);
        } catch (Exception e) {
            logger.error("统计用户机器人数量失败: {}", owner, e);
            return 0;
        }
    }
    
    @Override
    public long countByOwnerAndDeleted(String owner, Boolean isDeleted) {
        try {
            return robotRepository.countByOwnerAndIsDeleted(owner, isDeleted);
        } catch (Exception e) {
            logger.error("统计用户机器人数量失败: {}, 删除状态: {}", owner, isDeleted, e);
            return 0;
        }
    }
    
    @Override
    public long countAllRobots() {
        try {
            return robotRepository.count();
        } catch (Exception e) {
            logger.error("统计所有机器人数量失败", e);
            return 0;
        }
    }
    
    @Override
    public long countActiveRobots() {
        try {
            return robotRepository.countByIsActiveTrue();
        } catch (Exception e) {
            logger.error("统计激活机器人数量失败", e);
            return 0;
        }
    }
    
    @Override
    public String uploadAvatar(String robotId, MultipartFile file) {
        try {
            // 检查是否为临时机器人ID（创建模式）
            boolean isTempRobot = robotId.startsWith("temp_");
            
            if (isTempRobot) {
                // 临时机器人：只上传文件，不更新数据库
                String avatarUrl = fileService.uploadFile(file, "avatars");
                logger.info("临时机器人头像上传成功: {} -> {}", robotId, avatarUrl);
                return avatarUrl;
            } else {
                // 正式机器人：获取机器人信息并更新
                Robot robot = getRobotByRobotId(robotId);
                if (robot == null) {
                    throw new RuntimeException("机器人不存在");
                }
                
                // 上传文件到avatars目录
                String avatarUrl = fileService.uploadFile(file, "avatars");
                
                // 更新机器人头像
                robot.setAvatar(avatarUrl);
                robotRepository.save(robot);
                
                logger.info("机器人头像上传成功: {} -> {}", robotId, avatarUrl);
                return avatarUrl;
            }
            
        } catch (Exception e) {
            logger.error("机器人头像上传失败: {}", robotId, e);
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        }
    }
} 