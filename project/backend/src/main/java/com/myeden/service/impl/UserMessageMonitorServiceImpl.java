package com.myeden.service.impl;

import com.myeden.dto.coze.CozeMessageListResponse;
import com.myeden.entity.UserConversation;
import com.myeden.service.CozeService;
import com.myeden.service.UserConversationService;
import com.myeden.service.UserMessageMonitorService;
import com.myeden.service.WeChatConversationService;
import com.myeden.service.WeChatSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 用户消息监控服务实现类
 */
@Service
public class UserMessageMonitorServiceImpl implements UserMessageMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(UserMessageMonitorServiceImpl.class);
    
    // 监控间隔时间（秒）
    private static final int MONITOR_INTERVAL = 15;
    // 最大监控时长（分钟）- 防止长时间无活动的监控任务
    private static final int MAX_MONITOR_DURATION = 60 * 24 * 10;
    
    private final CozeService cozeService;
    private final UserConversationService userConversationService;
    private final WeChatSendService weChatSendService;
    private final WeChatConversationService weChatConversationService;
    
    // 线程池用于执行监控任务
    private final ScheduledExecutorService executorService;
    
    // 存储每个用户的监控任务
    private final ConcurrentHashMap<String, UserMonitoringTask> monitoringTasks;
    
    @Autowired
    public UserMessageMonitorServiceImpl(CozeService cozeService,
                                       UserConversationService userConversationService,
                                       WeChatSendService weChatSendService,
                                       WeChatConversationService weChatConversationService) {
        this.cozeService = cozeService;
        this.userConversationService = userConversationService;
        this.weChatSendService = weChatSendService;
        this.weChatConversationService = weChatConversationService;
        this.executorService = Executors.newScheduledThreadPool(10);
        this.monitoringTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void startMonitoring(String wechatUserId) {
        // 如果已经在监控，先停止现有的监控任务
        stopMonitoring(wechatUserId);
        
        logger.info("开始监控用户消息 - 微信用户ID: {}", wechatUserId);
        
        // 获取用户会话信息
        Optional<UserConversation> userConvOpt = userConversationService.findActiveByUserId(wechatUserId);
        if (!userConvOpt.isPresent()) {
            logger.warn("用户没有活跃的会话，无法启动监控 - 微信用户ID: {}", wechatUserId);
            return;
        }
        
        UserConversation userConversation = userConvOpt.get();
        if (userConversation.getConversationId() == null) {
            logger.warn("用户会话ID为空，无法启动监控 - 微信用户ID: {}", wechatUserId);
            return;
        }
        
        // 检查是否有有效的lastMessageId，没有则不启动监控
        if (!StringUtils.hasText(userConversation.getLastMessageId())) {
            logger.info("用户没有lastMessageId，跳过启动监控以避免获取历史数据 - 微信用户ID: {}", wechatUserId);
            return;
        }
        
        // 创建并启动监控任务
        UserMonitoringTask task = new UserMonitoringTask(wechatUserId, userConversation.getConversationId());
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(
            task, 
            MONITOR_INTERVAL,
            MONITOR_INTERVAL, 
            TimeUnit.SECONDS
        );
        
        task.setScheduledFuture(scheduledFuture);
        monitoringTasks.put(wechatUserId, task);
        
        logger.info("用户消息监控任务已启动 - 微信用户ID: {}, Coze会话ID: {}", 
                   wechatUserId, userConversation.getConversationId());
    }

    @Override
    public void stopMonitoring(String wechatUserId) {
        UserMonitoringTask task = monitoringTasks.remove(wechatUserId);
        if (task != null) {
            task.stop();
            logger.info("停止用户消息监控 - 微信用户ID: {}", wechatUserId);
        }
    }

    @Override
    public boolean checkAndProcessNewMessages(String wechatUserId) {
        try {
            // 获取用户会话信息
            Optional<UserConversation> userConvOpt = userConversationService.findActiveByUserId(wechatUserId);
            if (!userConvOpt.isPresent()) {
                logger.debug("用户没有活跃的会话 - 微信用户ID: {}", wechatUserId);
                return false;
            }
            
            UserConversation userConversation = userConvOpt.get();
            String conversationId = userConversation.getConversationId();
            String lastMessageId = userConversation.getLastMessageId();
            
            if (conversationId == null) {
                logger.debug("用户Coze会话ID为空 - 微信用户ID: {}", wechatUserId);
                return false;
            }
            
            // 只有在有有效lastMessageId时才进行监控，避免获取历史数据
            if (!StringUtils.hasText(lastMessageId)) {
                logger.debug("没有lastMessageId，跳过监控以避免获取历史数据 - 微信用户ID: {}", wechatUserId);
                return false;
            }
            
            // 获取新消息列表 - 必须使用after_id参数
            CozeMessageListResponse response = cozeService.getMessageList(
                conversationId, 
                lastMessageId, 
                20
            );
            
            // 处理API调用结果
            if (!response.isSuccess()) {
                // 特殊处理：如果是after_id不存在的错误(code=4200)，停止监控避免获取历史数据
                if (response.getCode() == 4200 && StringUtils.hasText(lastMessageId)) {
                    logger.warn("lastMessageId失效，停止监控以避免获取历史数据 - 微信用户ID: {}, 失效MessageID: {}", 
                               wechatUserId, lastMessageId);
                    // 重置lastMessageId但不重新获取数据
                    userConversationService.updateLastMessageId(wechatUserId, null);
                    // 停止当前用户的监控任务
                    stopMonitoring(wechatUserId);
                    return false;
                } else {
                    logger.error("获取消息列表失败 - 微信用户ID: {}, 错误: {}", 
                                wechatUserId, response.getDetailError());
                    return false;
                }
            }
            
            if (response.getData() == null || response.getData().isEmpty()) {
                logger.debug("没有获取到新消息 - 微信用户ID: {}, Coze会话ID: {}", wechatUserId, conversationId);
                return false;
            }
            
            List<CozeMessageListResponse.CozeMessageItem> newMessages = response.getData();
            boolean hasNewMessages = false;
            
            // 处理新消息
            for (CozeMessageListResponse.CozeMessageItem message : newMessages) {
                // 只处理AI助手的回复消息
                if ("assistant".equals(message.getRole()) && 
                    ("answer".equals(message.getType()) || "follow_up".equals(message.getType()))) {
                    
                    logger.info("发现新的AI主动推送消息 - 微信用户ID: {}, 消息ID: {}, 内容: {}", 
                               wechatUserId, message.getId(), message.getContent());
                    
                    // 发送消息到微信
                    sendNewMessageToWeChat(wechatUserId, message.getContent());
                    
                    // 保存消息到数据库
                    weChatConversationService.saveAssistantMessage(wechatUserId, message.getContent());
                    
                    hasNewMessages = true;
                }
            }
            
            // 更新最后消息ID - 只有处理了新的answer消息时才更新
            if (hasNewMessages) {
                // 查找最后一条answer类型消息的ID
                String newLastMessageId = findLastAnswerMessageId(newMessages);
                
                if (newLastMessageId != null) {
                    userConversationService.updateLastMessageId(wechatUserId, newLastMessageId);
                    logger.debug("更新最后answer消息ID - 微信用户ID: {}, 新消息ID: {}", 
                                wechatUserId, newLastMessageId);
                } else {
                    logger.warn("处理了新消息但未找到answer类型消息ID - 微信用户ID: {}", wechatUserId);
                }
            }
            
            return hasNewMessages;
            
        } catch (Exception e) {
            logger.error("检查和处理新消息时发生异常 - 微信用户ID: {}", wechatUserId, e);
            return false;
        }
    }

    @Override
    public void stopAllMonitoring() {
        logger.info("停止所有用户消息监控，当前监控数量: {}", monitoringTasks.size());
        
        monitoringTasks.values().forEach(UserMonitoringTask::stop);
        monitoringTasks.clear();
    }

    @Override
    public int getActiveMonitoringCount() {
        return monitoringTasks.size();
    }

    @Override
    public void restartMonitoring(String wechatUserId) {
        logger.info("重启用户消息监控 - 微信用户ID: {}", wechatUserId);
        stopMonitoring(wechatUserId);
        // 短暂延迟后重新启动
        executorService.schedule(() -> startMonitoring(wechatUserId), 1, TimeUnit.SECONDS);
    }
    
    /**
     * 查找消息列表中最后一条answer类型消息的ID
     */
    private String findLastAnswerMessageId(List<CozeMessageListResponse.CozeMessageItem> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        
        // 从后往前遍历，查找最后一条answer类型的消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            CozeMessageListResponse.CozeMessageItem message = messages.get(i);
            if ("assistant".equals(message.getRole()) && 
                ("answer".equals(message.getType()) || "follow_up".equals(message.getType()))) {
                return message.getId();
            }
        }
        
        return null;
    }
    
    /**
     * 发送新消息到微信
     */
    private void sendNewMessageToWeChat(String wechatUserId, String content) {
        try {
            weChatSendService.sendTextMessage(wechatUserId, content);
            logger.info("新消息已推送到微信 - 用户: {}, 内容: {}", wechatUserId, content);
        } catch (Exception e) {
            logger.error("推送新消息到微信失败 - 用户: {}", wechatUserId, e);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("用户消息监控服务关闭，停止所有监控任务");
        stopAllMonitoring();
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 用户监控任务内部类
     */
    private class UserMonitoringTask implements Runnable {
        
        private final String wechatUserId;
        private final String cozeConversationId;
        private final long startTime;
        private volatile ScheduledFuture<?> scheduledFuture;
        private volatile boolean active = true;
        
        public UserMonitoringTask(String wechatUserId, String cozeConversationId) {
            this.wechatUserId = wechatUserId;
            this.cozeConversationId = cozeConversationId;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            if (!active) {
                return;
            }
            
            try {
                // 检查是否超过最大监控时长
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > TimeUnit.MINUTES.toMillis(MAX_MONITOR_DURATION)) {
                    logger.info("监控任务超时，自动停止 - 微信用户ID: {}, Coze会话ID: {}", 
                              wechatUserId, cozeConversationId);
                    stopMonitoring(wechatUserId);
                    return;
                }
                
                // 检查并处理新消息
                logger.debug("执行定时检查新消息 - 微信用户ID: {}", wechatUserId);
                checkAndProcessNewMessages(wechatUserId);
                
            } catch (Exception e) {
                logger.error("执行用户监控任务时发生异常 - 微信用户ID: {}", wechatUserId, e);
                // 发生异常时重启监控任务
                restartMonitoring(wechatUserId);
            }
        }
        
        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }
        
        public void stop() {
            active = false;
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
            }
        }
    }
}