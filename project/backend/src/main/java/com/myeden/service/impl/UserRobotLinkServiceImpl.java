package com.myeden.service.impl;

import com.myeden.entity.UserRobotLink;
import com.myeden.entity.Robot;
import com.myeden.entity.User;
import com.myeden.repository.UserRobotLinkRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.UserRepository;
import com.myeden.service.UserRobotLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户机器人链接服务实现类
 * 
 * 功能说明：
 * - 实现用户与机器人的链接关系管理
 * - 提供链接的创建、查询、更新、删除功能
 * - 支持链接状态管理和强度评估
 * - 提供链接统计和分析功能
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
@Service
public class UserRobotLinkServiceImpl implements UserRobotLinkService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRobotLinkServiceImpl.class);
    
    @Autowired
    private UserRobotLinkRepository userRobotLinkRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public LinkResult createLink(String userId, String robotId) {
        try {
            logger.info("创建用户机器人链接，用户ID: {}, 机器人ID: {}", userId, robotId);
            
            // 验证参数
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(robotId)) {
                throw new IllegalArgumentException("用户ID和机器人ID不能为空");
            }
            
            logger.info("开始验证用户是否存在，用户ID: {}", userId);
            
            // 验证用户是否存在 - 添加异常处理
            Optional<User> userOpt;
            try {
                userOpt = userRepository.findByUserId(userId);
                logger.info("用户查询结果: {}", userOpt.isPresent() ? "找到用户" : "未找到用户");
            } catch (Exception e) {
                logger.error("查询用户时发生MongoDB异常，用户ID: {}", userId, e);
                
                // 尝试使用更简单的查询方法
                try {
                    logger.info("尝试使用findAll()方法查询所有用户");
                    List<User> allUsers = userRepository.findAll();
                    logger.info("数据库中共有 {} 个用户", allUsers.size());
                    
                    // 手动查找用户
                    userOpt = allUsers.stream()
                        .filter(user -> userId.equals(user.getUserId()))
                        .findFirst();
                    
                    logger.info("手动查找结果: {}", userOpt.isPresent() ? "找到用户" : "未找到用户");
                } catch (Exception ex) {
                    logger.error("findAll()查询也失败", ex);
                    throw new RuntimeException("数据库连接异常，请稍后重试", ex);
                }
            }
            
            if (userOpt.isEmpty()) {
                logger.error("用户不存在，用户ID: {}", userId);
                // 尝试查找所有用户，看看数据库中有什么
                try {
                    List<User> allUsers = userRepository.findAll();
                    logger.info("数据库中的所有用户ID: {}", allUsers.stream().map(User::getUserId).collect(Collectors.toList()));
                } catch (Exception e) {
                    logger.error("获取所有用户失败", e);
                }
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 验证机器人是否存在
            Optional<Robot> robotOpt = robotRepository.findByRobotId(robotId);
            if (robotOpt.isEmpty()) {
                throw new IllegalArgumentException("机器人不存在");
            }
            
            // 检查是否已存在链接
            if (userRobotLinkRepository.existsByUserIdAndRobotId(userId, robotId)) {
                throw new IllegalArgumentException("用户与机器人之间已存在链接");
            }
            
            // 创建链接
            UserRobotLink link = new UserRobotLink(userId, robotId);
            link.setLinkId(generateLinkId());
            
            // 保存到数据库
            UserRobotLink savedLink = userRobotLinkRepository.save(link);
            
            logger.info("用户机器人链接创建成功，链接ID: {}", savedLink.getLinkId());
            
            return new LinkResult(
                savedLink.getLinkId(),
                savedLink.getUserId(),
                savedLink.getRobotId(),
                savedLink.getStatus(),
                savedLink.getStrength(),
                "链接创建成功"
            );
            
        } catch (Exception e) {
            logger.error("创建用户机器人链接失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteLink(String userId, String robotId) {
        try {
            logger.info("删除用户机器人链接，用户ID: {}, 机器人ID: {}", userId, robotId);
            
            // 查找链接
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                logger.warn("用户机器人链接不存在，用户ID: {}, 机器人ID: {}", userId, robotId);
                return false;
            }
            
            // 删除链接
            userRobotLinkRepository.deleteByUserIdAndRobotId(userId, robotId);
            
            logger.info("用户机器人链接删除成功");
            return true;
            
        } catch (Exception e) {
            logger.error("删除用户机器人链接失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean activateLink(String userId, String robotId) {
        try {
            logger.info("激活用户机器人链接，用户ID: {}, 机器人ID: {}", userId, robotId);
            
            // 查找链接
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                logger.warn("用户机器人链接不存在，用户ID: {}, 机器人ID: {}", userId, robotId);
                return false;
            }
            
            UserRobotLink link = linkOpt.get();
            link.activate();
            userRobotLinkRepository.save(link);
            
            logger.info("用户机器人链接激活成功");
            return true;
            
        } catch (Exception e) {
            logger.error("激活用户机器人链接失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean deactivateLink(String userId, String robotId) {
        try {
            logger.info("停用用户机器人链接，用户ID: {}, 机器人ID: {}", userId, robotId);
            
            // 查找链接
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                logger.warn("用户机器人链接不存在，用户ID: {}, 机器人ID: {}", userId, robotId);
                return false;
            }
            
            UserRobotLink link = linkOpt.get();
            link.deactivate();
            userRobotLinkRepository.save(link);
            
            logger.info("用户机器人链接停用成功");
            return true;
            
        } catch (Exception e) {
            logger.error("停用用户机器人链接失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean updateLinkStrength(String userId, String robotId, Integer strength) {
        try {
            logger.info("更新用户机器人链接强度，用户ID: {}, 机器人ID: {}, 强度: {}", userId, robotId, strength);
            // 查找链接
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                logger.warn("用户机器人链接不存在，用户ID: {}, 机器人ID: {}", userId, robotId);
                return false;
            }
            UserRobotLink link = linkOpt.get();
            link.updateStrength(strength);
            userRobotLinkRepository.save(link);
            logger.info("用户机器人链接强度更新成功");
            return true;
        } catch (Exception e) {
            logger.error("更新用户机器人链接强度失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean incrementInteraction(String userId, String robotId) {
        try {
            logger.info("增加用户机器人互动次数，用户ID: {}, 机器人ID: {}", userId, robotId);
            // 查找链接
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                logger.warn("用户机器人链接不存在，用户ID: {}, 机器人ID: {}", userId, robotId);
                return false;
            }
            UserRobotLink link = linkOpt.get();
            link.incrementInteraction();
            userRobotLinkRepository.save(link);
            logger.info("用户机器人互动次数增加成功");
            return true;
        } catch (Exception e) {
            logger.error("增加用户机器人互动次数失败", e);
            throw e;
        }
    }
    
    @Override
    public List<LinkSummary> getUserLinks(String userId) {
        try {
            logger.info("获取用户链接列表，用户ID: {}", userId);
            
            List<UserRobotLink> links = userRobotLinkRepository.findByUserId(userId);
            
            List<LinkSummary> summaries = links.stream()
                .map(this::convertToLinkSummary)
                .collect(Collectors.toList());
            
            logger.info("获取用户链接列表成功，总数: {}", summaries.size());
            return summaries;
            
        } catch (Exception e) {
            logger.error("获取用户链接列表失败", e);
            throw e;
        }
    }
    
    @Override
    public List<LinkSummary> getUserActiveLinks(String userId) {
        try {
            logger.info("获取用户激活链接列表，用户ID: {}", userId);

            List<UserRobotLink> links;
            if (userId.startsWith("user_")) {
                links = userRobotLinkRepository.findByUserIdAndStatus(userId, "active");
            } else {
                links = userRobotLinkRepository.findByRobotIdAndStatus(userId, "active");
            }
            
            List<LinkSummary> summaries = links.stream()
                .map(this::convertToLinkSummary)
                .collect(Collectors.toList());
            
            logger.info("获取用户激活链接列表成功，总数: {}", summaries.size());
            return summaries;
            
        } catch (Exception e) {
            logger.error("获取用户激活链接列表失败", e);
            throw e;
        }
    }
    
    @Override
    public List<LinkSummary> getRobotLinks(String robotId) {
        try {
            logger.info("获取机器人链接列表，机器人ID: {}", robotId);
            
            List<UserRobotLink> links = userRobotLinkRepository.findByRobotId(robotId);
            
            List<LinkSummary> summaries = links.stream()
                .map(this::convertToLinkSummary)
                .collect(Collectors.toList());
            
            logger.info("获取机器人链接列表成功，总数: {}", summaries.size());
            return summaries;
            
        } catch (Exception e) {
            logger.error("获取机器人链接列表失败", e);
            throw e;
        }
    }
    
    @Override
    public List<LinkSummary> getRobotActiveLinks(String robotId) {
        try {
            logger.info("获取机器人激活链接列表，机器人ID: {}", robotId);
            
            List<UserRobotLink> links = userRobotLinkRepository.findByRobotIdAndStatus(robotId, "active");
            
            List<LinkSummary> summaries = links.stream()
                .map(this::convertToLinkSummary)
                .collect(Collectors.toList());
            
            logger.info("获取机器人激活链接列表成功，总数: {}", summaries.size());
            return summaries;
            
        } catch (Exception e) {
            logger.error("获取机器人激活链接列表失败", e);
            throw e;
        }
    }
    
    @Override
    public boolean hasLink(String userId, String robotId) {
        return userRobotLinkRepository.existsByUserIdAndRobotId(userId, robotId);
    }
    
    @Override
    public boolean hasActiveLink(String userId, String robotId) {
        return userRobotLinkRepository.existsByUserIdAndRobotIdAndStatus(userId, robotId, "active");
    }
    
    @Override
    public LinkDetail getLinkDetail(String userId, String robotId) {
        try {
            logger.info("获取链接详情，用户ID: {}, 机器人ID: {}", userId, robotId);
            
            Optional<UserRobotLink> linkOpt = userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
            if (linkOpt.isEmpty()) {
                throw new IllegalArgumentException("链接不存在");
            }
            
            UserRobotLink link = linkOpt.get();
            LinkDetail detail = convertToLinkDetail(link);
            
            logger.info("获取链接详情成功");
            return detail;
            
        } catch (Exception e) {
            logger.error("获取链接详情失败", e);
            throw e;
        }
    }
    
    @Override
    public Optional<UserRobotLink> getLink(String userId, String robotId) {
        return userRobotLinkRepository.findByUserIdAndRobotId(userId, robotId);
    }
    
    @Override
    public LinkSummary getStrongestLink(String userId) {
        try {
            logger.info("获取用户最强链接，用户ID: {}", userId);
            
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "strength"));
            Page<UserRobotLink> linkPage = userRobotLinkRepository.findTopByUserIdOrderByStrengthDesc(userId, pageable);
            
            if (linkPage.hasContent()) {
                UserRobotLink link = linkPage.getContent().get(0);
                LinkSummary summary = convertToLinkSummary(link);
                
                logger.info("获取用户最强链接成功");
                return summary;
            }
            
            logger.info("用户没有链接");
            return null;
            
        } catch (Exception e) {
            logger.error("获取用户最强链接失败", e);
            throw e;
        }
    }
    
    @Override
    public LinkSummary getMostActiveLink(String userId) {
        try {
            logger.info("获取用户最活跃链接，用户ID: {}", userId);
            
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "interactionCount"));
            Page<UserRobotLink> linkPage = userRobotLinkRepository.findTopByUserIdOrderByInteractionCountDesc(userId, pageable);
            
            if (linkPage.hasContent()) {
                UserRobotLink link = linkPage.getContent().get(0);
                LinkSummary summary = convertToLinkSummary(link);
                
                logger.info("获取用户最活跃链接成功");
                return summary;
            }
            
            logger.info("用户没有链接");
            return null;
            
        } catch (Exception e) {
            logger.error("获取用户最活跃链接失败", e);
            throw e;
        }
    }
    
    @Override
    public LinkStatistics getLinkStatistics(String userId) {
        try {
            logger.info("获取用户链接统计，用户ID: {}", userId);
            
            long totalLinks = userRobotLinkRepository.countByUserId(userId);
            long activeLinks = userRobotLinkRepository.countByUserIdAndStatus(userId, "active");
            long inactiveLinks = totalLinks - activeLinks;
            
            // 计算平均强度
            List<UserRobotLink> links = userRobotLinkRepository.findByUserId(userId);
            double averageStrength = links.stream()
                .mapToInt(UserRobotLink::getStrength)
                .average()
                .orElse(0.0);
            
            // 计算总互动次数
            long totalInteractions = links.stream()
                .mapToLong(link -> link.getInteractionCount())
                .sum();
            
            // 获取最强和最活跃的机器人ID
            String strongestRobotId = null;
            String mostActiveRobotId = null;
            
            if (!links.isEmpty()) {
                strongestRobotId = links.stream()
                    .max((a, b) -> Integer.compare(a.getStrength(), b.getStrength()))
                    .map(UserRobotLink::getRobotId)
                    .orElse(null);
                
                mostActiveRobotId = links.stream()
                    .max((a, b) -> Integer.compare(a.getInteractionCount(), b.getInteractionCount()))
                    .map(UserRobotLink::getRobotId)
                    .orElse(null);
            }
            
            LinkStatistics statistics = new LinkStatistics(
                userId, totalLinks, activeLinks, inactiveLinks,
                averageStrength, totalInteractions, strongestRobotId, mostActiveRobotId
            );
            
            logger.info("获取用户链接统计成功");
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取用户链接统计失败", e);
            throw e;
        }
    }
    
    @Override
    public List<String> getAllUserIds() {
        return userRepository.findAll().stream().map(u -> u.getUserId()).toList();
    }

    @Override
    public List<String> getAllRobotIds() {
        return robotRepository.findAll().stream().map(r -> r.getRobotId()).toList();
    }
    
    @Override
    public UserRobotLink save(UserRobotLink link) {
        return userRobotLinkRepository.save(link);
    }
    
    /**
     * 转换为链接摘要
     */
    private LinkSummary convertToLinkSummary(UserRobotLink link) {
        // 获取机器人信息
        String robotName = "";
        String robotAvatar = "";
        Optional<Robot> robotOpt = robotRepository.findByRobotId(link.getRobotId());
        if (robotOpt.isPresent()) {
            Robot robot = robotOpt.get();
            robotName = robot.getName();
            robotAvatar = robot.getAvatar();
        }
        
        return new LinkSummary(
            link.getLinkId(),
            link.getUserId(),
            link.getRobotId(),
            robotName,
            robotAvatar,
            link.getStatus(),
            link.getStrength(),
            link.getStrengthLevel(),
            link.getInteractionCount(),
            link.getLastInteractionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            link.getImpression()
        );
    }
    
    /**
     * 转换为链接详情
     */
    private LinkDetail convertToLinkDetail(UserRobotLink link) {
        // 获取机器人信息
        String robotName = "";
        String robotAvatar = "";
        Optional<Robot> robotOpt = robotRepository.findByRobotId(link.getRobotId());
        if (robotOpt.isPresent()) {
            Robot robot = robotOpt.get();
            robotName = robot.getName();
            robotAvatar = robot.getAvatar();
        }
        
        return new LinkDetail(
            link.getLinkId(),
            link.getUserId(),
            link.getRobotId(),
            robotName,
            robotAvatar,
            link.getStatus(),
            link.getStrength(),
            link.getStrengthLevel(),
            link.getInteractionCount(),
            link.getLastInteractionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            link.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            link.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    /**
     * 生成链接ID
     */
    private String generateLinkId() {
        return "link_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
} 