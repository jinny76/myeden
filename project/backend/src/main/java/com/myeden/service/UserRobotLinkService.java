package com.myeden.service;

import com.myeden.entity.UserRobotLink;
import java.util.List;
import java.util.Optional;

/**
 * 用户机器人链接服务接口
 * 
 * 功能说明：
 * - 管理用户与机器人的链接关系
 * - 提供链接的创建、查询、更新、删除功能
 * - 支持链接状态管理和强度评估
 * - 提供链接统计和分析功能
 * 
 * @author MyEden Team
 * @version 1.0.1
 * @since 2025-01-27
 */
public interface UserRobotLinkService {
    
    /**
     * 创建用户机器人链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 链接结果
     */
    LinkResult createLink(String userId, String robotId);
    
    /**
     * 删除用户机器人链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean deleteLink(String userId, String robotId);
    
    /**
     * 激活链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean activateLink(String userId, String robotId);
    
    /**
     * 停用链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean deactivateLink(String userId, String robotId);
    
    /**
     * 增加互动次数
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean incrementInteraction(String userId, String robotId);
    
    /**
     * 获取用户的链接列表
     * 
     * @param userId 用户ID
     * @return 链接列表
     */
    List<LinkSummary> getUserLinks(String userId);
    
    /**
     * 获取用户的激活链接列表
     * 
     * @param userId 用户ID
     * @return 激活链接列表
     */
    List<LinkSummary> getUserActiveLinks(String userId);
    
    /**
     * 获取机器人的链接列表
     * 
     * @param robotId 机器人ID
     * @return 链接列表
     */
    List<LinkSummary> getRobotLinks(String robotId);
    
    /**
     * 获取机器人的激活链接列表
     * 
     * @param robotId 机器人ID
     * @return 激活链接列表
     */
    List<LinkSummary> getRobotActiveLinks(String robotId);
    
    /**
     * 检查用户和机器人之间是否存在链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否存在链接
     */
    boolean hasLink(String userId, String robotId);
    
    /**
     * 检查用户和机器人之间是否存在激活的链接
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否存在激活的链接
     */
    boolean hasActiveLink(String userId, String robotId);
    
    /**
     * 获取链接详情
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 链接详情
     */
    LinkDetail getLinkDetail(String userId, String robotId);
    
    /**
     * 获取用户最活跃的链接
     * 
     * @param userId 用户ID
     * @return 最活跃链接
     */
    LinkSummary getMostActiveLink(String userId);

    
    /**
     * 获取所有用户ID
     * @return 用户ID列表
     */
    List<String> getAllUserIds();

    /**
     * 获取所有机器人ID
     * @return 机器人ID列表
     */
    List<String> getAllRobotIds();
    
    /**
     * 根据用户ID和机器人ID获取连接对象
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 连接对象Optional
     */
    Optional<UserRobotLink> getLink(String userId, String robotId);
    
    /**
     * 保存用户-机器人连接对象
     * @param link 连接对象
     * @return 保存后的对象
     */
    UserRobotLink save(UserRobotLink link);
    
    // 熟悉度相关方法
    
    /**
     * 增加熟悉度积分
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param points 积分数
     * @return 是否成功
     */
    boolean addFamiliarityScore(String userId, String robotId, Integer points);
    
    /**
     * 根据行为类型自动计算并增加熟悉度积分
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param actionType 行为类型（comment、reply、chat等）
     * @return 是否成功
     */
    boolean addFamiliarityScoreByAction(String userId, String robotId, String actionType);
    
    /**
     * 获取用户有待沟通消息的机器人链接
     * 
     * @param userId 用户ID
     * @return 有待沟通消息的链接列表
     */
    List<LinkSummary> getPendingChatLinks(String userId);
    
    /**
     * 清除待沟通消息标记
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @return 是否成功
     */
    boolean clearPendingMessage(String userId, String robotId);
    
    /**
     * 获取可以主动沟通的机器人链接（好友及以上等级）
     * 
     * @param userId 用户ID
     * @return 可主动沟通的链接列表
     */
    List<LinkSummary> getProactiveChatLinks(String userId);
    
    /**
     * 获取用户熟悉度统计
     * 
     * @param userId 用户ID
     * @return 熟悉度统计
     */
    FamiliarityStatistics getFamiliarityStatistics(String userId);
    
    /**
     * 世界模块专用：按优先级获取机器人链接
     * 优先级：有待沟通消息 > 熟悉度高 > 已链接
     * 
     * @param userId 用户ID
     * @param pageSize 页面大小
     * @return 按优先级排序的链接列表
     */
    List<LinkSummary> getLinksForWorldModule(String userId, int pageSize);
    
    /**
     * 链接结果类
     */
    class LinkResult {
        private String linkId;
        private String userId;
        private String robotId;
        private String status;
        private String message;
        
        public LinkResult(String linkId, String userId, String robotId, String status, String message) {
            this.linkId = linkId;
            this.userId = userId;
            this.robotId = robotId;
            this.status = status;
            this.message = message;
        }
        
        // Getter方法
        public String getLinkId() { return linkId; }
        public String getUserId() { return userId; }
        public String getRobotId() { return robotId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
    
    /**
     * 链接摘要类
     */
    class LinkSummary {
        private String linkId;
        private String userId;
        private String robotId;
        private String robotName;
        private String robotAvatar;
        private String status;
        private Integer interactionCount;
        private String lastInteractionTime;
        private String impression;
        private Integer familiarityScore;
        private Integer familiarityLevel;
        private String familiarityLevelName;
        private Boolean hasPendingMessage;
        
        public LinkSummary(String linkId, String userId, String robotId, String robotName, String robotAvatar,
                          String status, Integer interactionCount, String lastInteractionTime, String impression) {
            this.linkId = linkId;
            this.userId = userId;
            this.robotId = robotId;
            this.robotName = robotName;
            this.robotAvatar = robotAvatar;
            this.status = status;
            this.interactionCount = interactionCount;
            this.lastInteractionTime = lastInteractionTime;
            this.impression = impression;
        }
        
        public LinkSummary(String linkId, String userId, String robotId, String robotName, String robotAvatar,
                          String status, Integer interactionCount, String lastInteractionTime, String impression,
                          Integer familiarityScore, Integer familiarityLevel, String familiarityLevelName, Boolean hasPendingMessage) {
            this(linkId, userId, robotId, robotName, robotAvatar, status, interactionCount, lastInteractionTime, impression);
            this.familiarityScore = familiarityScore;
            this.familiarityLevel = familiarityLevel;
            this.familiarityLevelName = familiarityLevelName;
            this.hasPendingMessage = hasPendingMessage;
        }
        
        // Getter方法
        public String getLinkId() { return linkId; }
        public String getUserId() { return userId; }
        public String getRobotId() { return robotId; }
        public String getRobotName() { return robotName; }
        public String getRobotAvatar() { return robotAvatar; }
        public String getStatus() { return status; }
        public Integer getInteractionCount() { return interactionCount; }
        public String getLastInteractionTime() { return lastInteractionTime; }
        public String getImpression() { return impression; }
        public Integer getFamiliarityScore() { return familiarityScore; }
        public Integer getFamiliarityLevel() { return familiarityLevel; }
        public String getFamiliarityLevelName() { return familiarityLevelName; }
        public Boolean getHasPendingMessage() { return hasPendingMessage; }
    }
    
    /**
     * 链接详情类
     */
    class LinkDetail {
        private String linkId;
        private String userId;
        private String robotId;
        private String robotName;
        private String robotAvatar;
        private String status;
        private String createdAt;
        private String updatedAt;
        
        public LinkDetail(String linkId, String userId, String robotId, String robotName, String robotAvatar,
                         String status, String createdAt, String updatedAt) {
            this.linkId = linkId;
            this.userId = userId;
            this.robotId = robotId;
            this.robotName = robotName;
            this.robotAvatar = robotAvatar;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getter方法
        public String getLinkId() { return linkId; }
        public String getUserId() { return userId; }
        public String getRobotId() { return robotId; }
        public String getRobotName() { return robotName; }
        public String getRobotAvatar() { return robotAvatar; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
    
    /**
     * 链接统计类
     */
    class LinkStatistics {
        private String userId;
        private Long totalLinks;
        private Long activeLinks;
        private Long inactiveLinks;
        private Long totalInteractions;
        private String strongestRobotId;
        private String mostActiveRobotId;
        
        public LinkStatistics(String userId, Long totalLinks, Long activeLinks, Long inactiveLinks, Long totalInteractions, String strongestRobotId, String mostActiveRobotId) {
            this.userId = userId;
            this.totalLinks = totalLinks;
            this.activeLinks = activeLinks;
            this.inactiveLinks = inactiveLinks;
            this.totalInteractions = totalInteractions;
            this.strongestRobotId = strongestRobotId;
            this.mostActiveRobotId = mostActiveRobotId;
        }
        
        // Getter方法
        public String getUserId() { return userId; }
        public Long getTotalLinks() { return totalLinks; }
        public Long getActiveLinks() { return activeLinks; }
        public Long getInactiveLinks() { return inactiveLinks; }
        public Long getTotalInteractions() { return totalInteractions; }
        public String getStrongestRobotId() { return strongestRobotId; }
        public String getMostActiveRobotId() { return mostActiveRobotId; }
    }
    
    /**
     * 熟悉度统计类
     */
    class FamiliarityStatistics {
        private String userId;
        private Long totalFriends;      // 朋友及以上等级的机器人数量
        private Long closeFriends;      // 好友及以上等级的机器人数量
        private Long intimateFriends;   // 密友等级的机器人数量
        private Long pendingMessages;   // 有待沟通消息的机器人数量
        private Double averageFamiliarityScore;
        private String highestFamiliarityRobotId;
        private String mostRecentUpgradeRobotId;
        
        public FamiliarityStatistics(String userId, Long totalFriends, Long closeFriends, Long intimateFriends,
                                   Long pendingMessages, Double averageFamiliarityScore, String highestFamiliarityRobotId, String mostRecentUpgradeRobotId) {
            this.userId = userId;
            this.totalFriends = totalFriends;
            this.closeFriends = closeFriends;
            this.intimateFriends = intimateFriends;
            this.pendingMessages = pendingMessages;
            this.averageFamiliarityScore = averageFamiliarityScore;
            this.highestFamiliarityRobotId = highestFamiliarityRobotId;
            this.mostRecentUpgradeRobotId = mostRecentUpgradeRobotId;
        }
        
        // Getter方法
        public String getUserId() { return userId; }
        public Long getTotalFriends() { return totalFriends; }
        public Long getCloseFriends() { return closeFriends; }
        public Long getIntimateFriends() { return intimateFriends; }
        public Long getPendingMessages() { return pendingMessages; }
        public Double getAverageFamiliarityScore() { return averageFamiliarityScore; }
        public String getHighestFamiliarityRobotId() { return highestFamiliarityRobotId; }
        public String getMostRecentUpgradeRobotId() { return mostRecentUpgradeRobotId; }
    }
} 