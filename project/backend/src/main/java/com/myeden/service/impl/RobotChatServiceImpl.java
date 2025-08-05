package com.myeden.service.impl;

import com.myeden.entity.ChatRoom;
import com.myeden.entity.ChatRoomMember;
import com.myeden.entity.GroupChatMessage;
import com.myeden.entity.Robot;
import com.myeden.repository.RobotRepository;
import com.myeden.service.*;
import com.myeden.service.impl.DifyServiceImpl;
import com.myeden.service.DifyService.DifyChatResult;
import com.myeden.model.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 机器人群聊服务实现类
 * 负责机器人在群聊中的发言调度、智能回复生成和多线程管理
 * 每个聊天室运行在独立线程中，房间内机器人按活跃度随机发言
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class RobotChatServiceImpl implements RobotChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotChatServiceImpl.class);
    
    @Autowired
    private ChatRoomService chatRoomService;
    
    @Autowired
    private ChatRoomMemberService memberService;
    
    @Autowired
    private GroupChatService groupChatService;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private DifyServiceImpl difyService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private ChatroomPromptService chatroomPromptService;
    
    // 线程池和调度管理
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private final Map<String, Future<?>> roomSchedulers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> roomScheduledTasks = new ConcurrentHashMap<>();
    
    // 随机数生成器
    private final Random random = new Random();
    
    // 发言频率配置（秒）
    private static final int HIGH_FREQUENCY_MIN = 10;
    private static final int HIGH_FREQUENCY_MAX = 30;
    private static final int LOW_FREQUENCY_MIN = 60;
    private static final int LOW_FREQUENCY_MAX = 300;
    
    @Override
    public Future<?> startRoomChatScheduler(String roomId) {
        logger.info("启动聊天室机器人调度: roomId={}", roomId);
        
        // 如果已经有调度器在运行，先停止
        stopRoomChatScheduler(roomId);
        
        // 创建新的调度任务
        ScheduledFuture<?> scheduledTask = scheduledExecutorService.scheduleWithFixedDelay(
            new RoomChatTask(roomId), 
            getInitialDelay(roomId), 
            getNextDelay(roomId), 
            TimeUnit.SECONDS
        );
        
        roomScheduledTasks.put(roomId, scheduledTask);
        logger.info("聊天室机器人调度启动成功: roomId={}", roomId);
        
        return scheduledTask;
    }
    
    @Override
    public boolean stopRoomChatScheduler(String roomId) {
        logger.info("停止聊天室机器人调度: roomId={}", roomId);
        
        ScheduledFuture<?> task = roomScheduledTasks.remove(roomId);
        if (task != null) {
            task.cancel(false);
            logger.info("聊天室机器人调度停止成功: roomId={}", roomId);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean isRoomSchedulerRunning(String roomId) {
        ScheduledFuture<?> task = roomScheduledTasks.get(roomId);
        return task != null && !task.isCancelled() && !task.isDone();
    }
    
    @Override
    public boolean triggerRoomChat(String roomId) {
        try {
            logger.debug("触发聊天室机器人发言: roomId={}", roomId);
            
            // 检查聊天室是否存在
            Optional<ChatRoom> roomOpt = chatRoomService.getChatRoomById(roomId);
            if (!roomOpt.isPresent()) {
                logger.warn("聊天室不存在: roomId={}", roomId);
                return false;
            }
            
            ChatRoom room = roomOpt.get();
            
            // 选择发言机器人
            Robot speaker = selectNextSpeaker(roomId);
            if (speaker == null) {
                logger.debug("没有合适的机器人发言: roomId={}", roomId);
                return false;
            }
            
            // 获取聊天上下文
            List<GroupChatMessage> contextMessages = groupChatService.getChatContext(roomId, 10);
            
            // 生成消息内容
            String processedContent = generateRobotChatMessage(speaker, roomId, contextMessages);
            if (processedContent == null || processedContent.trim().isEmpty()) {
                logger.warn("机器人消息生成失败: robotId={}, roomId={}", speaker.getRobotId(), roomId);
                return false;
            }

            if (processedContent.contains("</think>")) {
                processedContent = processedContent.substring(processedContent.indexOf("</think>\n") + "</think>\n".length());
            }
            if (processedContent.contains("<think>") && !processedContent.contains("</think>")) {
                String content = processedContent;
                int thinkIdx = content.indexOf("<think>");
                content = content.substring(thinkIdx + "<think>".length()).trim();
                int lastColon = content.lastIndexOf(':');
                if (lastColon != -1 && lastColon < content.length() - 1) {
                    processedContent = content.substring(lastColon + 1).trim();
                } else {
                    int lastComma = content.lastIndexOf(',');
                    if (lastComma != -1 && lastComma < content.length() - 1) {
                        processedContent = content.substring(lastComma + 1).trim();
                    } else {
                        processedContent = content.trim();
                    }
                }
            }
            
            // 发送消息
            GroupChatMessage message = sendRobotMessage(roomId, speaker, processedContent, null, null);
            if (message != null) {
                // 更新聊天室活跃时间
                chatRoomService.updateLastActiveTime(roomId);
                chatRoomService.updateLastMessageTime(roomId);
                
                // 更新机器人统计
                updateRobotChatStats(speaker.getRobotId(), roomId);
                
                logger.info("机器人发言成功: robotId={}, roomId={}, content={}", 
                          speaker.getRobotId(), roomId, processedContent.substring(0, Math.min(50, processedContent.length())));
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("触发聊天室机器人发言失败: roomId={}", roomId, e);
            return false;
        }
    }
    
    @Override
    public Robot selectNextSpeaker(String roomId) {
        try {
            // 获取聊天室内活跃的机器人
            List<Robot> activeRobots = getActiveRobotsInRoom(roomId);
            if (activeRobots.isEmpty()) {
                return null;
            }
            
            // 基于权重的随机选择
            Map<Robot, Double> robotWeights = new HashMap<>();
            
            for (Robot robot : activeRobots) {
                double weight = calculateRobotSpeakProbability(robot, roomId);
                if (weight > 0) {
                    robotWeights.put(robot, weight);
                }
            }
            
            if (robotWeights.isEmpty()) {
                return null;
            }
            
            // 加权随机选择
            return weightedRandomSelect(robotWeights);
            
        } catch (Exception e) {
            logger.error("选择发言机器人失败: roomId={}", roomId, e);
            return null;
        }
    }
    
    @Override
    public String generateRobotChatMessage(Robot robot, String roomId, List<GroupChatMessage> contextMessages) {
        try {
            // 使用新的提示词服务构建聊天上下文
            String chatContext = chatroomPromptService.buildChatContextString(contextMessages, 1000);
            
            // 构建基础聊天提示词
            String fullPrompt = chatroomPromptService.buildBasicChatPrompt(robot, chatContext);
            
            // 调用AI生成内容
            DifyChatResult result = difyService.callDifyApi(fullPrompt, robot.getRobotId(), robot.getAppKey());
            String generatedContent = result != null && result.success ? result.answer : "我现在有点累了，稍后再聊吧~";
            
            // 处理生成的内容
            if (generatedContent != null && !generatedContent.trim().isEmpty()) {
                // 限制长度和句数
                String processedContent = processGeneratedContent(generatedContent);
                return processedContent;
            }
            
            // 如果AI生成失败，使用配置的后备回复
            return chatroomPromptService.getFallbackResponse(robot);
            
        } catch (Exception e) {
            logger.error("生成机器人聊天消息失败: robotId={}, roomId={}", robot.getRobotId(), roomId, e);
            return chatroomPromptService.getFallbackResponse(robot);
        }
    }
    
    @Override
    public GroupChatMessage sendRobotMessage(String roomId, Robot robot, String content, 
                                           String imageUrl, String replyToId) {
        try {
            // 发送群聊消息
            GroupChatMessage message = groupChatService.sendGroupMessage(
                roomId, "ROBOT", robot.getRobotId(), content, imageUrl, replyToId);
            
            if (message != null) {
                // 设置发送者信息
                message.setSenderNickname(robot.getNickname());
                message.setSenderAvatar(robot.getAvatar());
                
                // 通过WebSocket推送消息
                List<ChatRoomMember> roomMembers = memberService.getChatRoomMembers(roomId);
                List<String> memberIds = roomMembers.stream().map(ChatRoomMember::getMemberId).collect(Collectors.toList());
                WebSocketMessage<GroupChatMessage> wsMessage = WebSocketMessage.chat(message);
                webSocketService.sendMessageToUsers(memberIds, wsMessage);
                
                logger.debug("机器人消息发送成功: robotId={}, roomId={}", robot.getRobotId(), roomId);
            }
            
            return message;
            
        } catch (Exception e) {
            logger.error("机器人发送消息失败: robotId={}, roomId={}", robot.getRobotId(), roomId, e);
            return null;
        }
    }
    
    @Override
    public int handleUserMessage(String roomId, GroupChatMessage userMessage) {
        try {
            logger.debug("处理用户消息，触发机器人回复: roomId={}, userId={}", roomId, userMessage.getSenderId());
            
            // 获取活跃机器人
            List<Robot> activeRobots = getActiveRobotsInRoom(roomId);
            if (activeRobots.isEmpty()) {
                return 0;
            }
            
            int replyCount = 0;
            
            // 异步处理机器人回复，避免阻塞用户消息发送
            executorService.submit(() -> {
                try {
                    // 等待一小段时间，让用户消息先发送完成
                    Thread.sleep(1000 + random.nextInt(3000)); // 1-4秒随机延迟
                    
                    for (Robot robot : activeRobots) {
                        // 计算回复概率
                        double replyProbability = calculateReplyProbability(robot, userMessage);
                        
                        if (random.nextDouble() < replyProbability) {
                            // 获取包含用户消息的上下文
                            List<GroupChatMessage> contextMessages = groupChatService.getChatContext(roomId, 10);
                            
                            // 生成回复内容
                            String replyContent = generateRobotReplyMessage(robot, userMessage, contextMessages);
                            
                            if (replyContent != null && !replyContent.trim().isEmpty()) {
                                // 发送回复（可能是回复用户消息）
                                String replyToId = shouldReplyToUser(robot, userMessage) ? userMessage.getId() : null;
                                
                                sendRobotMessage(roomId, robot, replyContent, null, replyToId);
                                
                                // 更新统计
                                updateRobotChatStats(robot.getRobotId(), roomId);
                                
                                logger.info("机器人回复用户消息: robotId={}, userId={}, roomId={}", 
                                          robot.getRobotId(), userMessage.getSenderId(), roomId);
                                
                                // 机器人之间也不要同时回复，加个随机延迟
                                Thread.sleep(2000 + random.nextInt(3000));
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("处理机器人回复失败: roomId={}", roomId, e);
                }
            });
            
            return replyCount;
            
        } catch (Exception e) {
            logger.error("处理用户消息失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public double calculateRobotSpeakProbability(Robot robot, String roomId) {
        try {
            // 基础概率（根据机器人活跃度配置）
            double baseProbability = robot.getBehaviorPatterns() != null ? 
                robot.getBehaviorPatterns().getSocialEnergy() / 10.0 : 0.05; // 使用socialEnergy作为活跃度
            
            // 获取聊天室状态
            Optional<ChatRoom> roomOpt = chatRoomService.getChatRoomById(roomId);
            if (!roomOpt.isPresent()) {
                return 0.0;
            }
            
            ChatRoom room = roomOpt.get();
            
            // 根据聊天室状态调整概率
            if ("active".equals(room.getStatus())) {
                baseProbability *= 1.5; // 活跃状态下增加50%概率
            } else {
                baseProbability *= 0.5; // 非活跃状态下减少50%概率
            }
            
            // 根据最近发言时间调整概率
            RobotChatStats stats = getRobotChatStats(robot.getRobotId(), roomId);
            if (stats != null && stats.getLastMessageTime() != null) {
                // 如果最近刚发过言，降低概率
                // 这里可以根据实际需求调整逻辑
            }
            
            // 确保概率在合理范围内
            return Math.max(0.0, Math.min(1.0, baseProbability));
            
        } catch (Exception e) {
            logger.error("计算机器人发言概率失败: robotId={}, roomId={}", robot.getRobotId(), roomId, e);
            return 0.0;
        }
    }
    
    @Override
    public boolean canRobotSpeakInRoom(Robot robot, String roomId) {
        try {
            // 检查机器人是否存在于聊天室
            if (!memberService.isMemberInChatRoom(roomId, robot.getRobotId())) {
                return false;
            }
            
            // 检查机器人是否被静音
            Optional<ChatRoomMember> memberOpt = memberService.getChatRoomMember(roomId, robot.getRobotId());
            if (memberOpt.isPresent() && Boolean.TRUE.equals(memberOpt.get().getIsMuted())) {
                return false;
            }
            
            // 检查机器人是否在活跃时间段
            return isRobotActiveNow(robot);
            
        } catch (Exception e) {
            logger.error("检查机器人是否可以发言失败: robotId={}, roomId={}", robot.getRobotId(), roomId, e);
            return false;
        }
    }
    
    @Override
    public List<Robot> getActiveRobotsInRoom(String roomId) {
        try {
            // 获取聊天室内的机器人成员
            List<ChatRoomMember> robotMembers = memberService.getActiveRobotMembers(roomId);
            
            // 获取机器人详细信息
            List<Robot> activeRobots = new ArrayList<>();
            for (ChatRoomMember member : robotMembers) {
                Optional<Robot> robotOpt = robotRepository.findByRobotId(member.getMemberId());
                if (robotOpt.isPresent()) {
                    Robot robot = robotOpt.get();
                    if (canRobotSpeakInRoom(robot, roomId)) {
                        activeRobots.add(robot);
                    }
                }
            }
            
            return activeRobots;
            
        } catch (Exception e) {
            logger.error("获取聊天室活跃机器人失败: roomId={}", roomId, e);
            return new ArrayList<>();
        }
    }
    
    // 以下是辅助方法的实现
    
    /**
     * 聊天室任务类
     */
    private class RoomChatTask implements Runnable {
        private final String roomId;
        
        public RoomChatTask(String roomId) {
            this.roomId = roomId;
        }
        
        @Override
        public void run() {
            try {
                // 触发聊天室机器人发言
                triggerRoomChat(roomId);
                
                // 重新调度下一次执行
                scheduleNextExecution(roomId);
                
            } catch (Exception e) {
                logger.error("聊天室任务执行失败: roomId={}", roomId, e);
            }
        }
    }
    
    /**
     * 重新调度下一次执行
     */
    private void scheduleNextExecution(String roomId) {
        try {
            // 取消当前任务
            ScheduledFuture<?> currentTask = roomScheduledTasks.get(roomId);
            if (currentTask != null) {
                currentTask.cancel(false);
            }
            
            // 计算下一次执行延迟
            int delay = getNextDelay(roomId);
            
            // 创建新的调度任务
            ScheduledFuture<?> newTask = scheduledExecutorService.schedule(
                new RoomChatTask(roomId), delay, TimeUnit.SECONDS);
            
            roomScheduledTasks.put(roomId, newTask);
            
        } catch (Exception e) {
            logger.error("重新调度任务失败: roomId={}", roomId, e);
        }
    }
    
    /**
     * 获取初始延迟时间
     */
    private int getInitialDelay(String roomId) {
        return 10 + random.nextInt(20); // 10-30秒的初始延迟
    }
    
    /**
     * 获取下次执行的延迟时间
     */
    private int getNextDelay(String roomId) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomService.getChatRoomById(roomId);
            if (roomOpt.isPresent()) {
                ChatRoom room = roomOpt.get();
                if ("active".equals(room.getStatus())) {
                    // 高频模式：10-30秒
                    return HIGH_FREQUENCY_MIN + random.nextInt(HIGH_FREQUENCY_MAX - HIGH_FREQUENCY_MIN);
                } else {
                    // 低频模式：1-5分钟
                    return LOW_FREQUENCY_MIN + random.nextInt(LOW_FREQUENCY_MAX - LOW_FREQUENCY_MIN);
                }
            }
        } catch (Exception e) {
            logger.error("获取下次延迟失败: roomId={}", roomId, e);
        }
        
        // 默认中等频率
        return 60 + random.nextInt(120); // 1-3分钟
    }
    
    /**
     * 加权随机选择
     */
    private Robot weightedRandomSelect(Map<Robot, Double> robotWeights) {
        double totalWeight = robotWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;
        
        double currentWeight = 0.0;
        for (Map.Entry<Robot, Double> entry : robotWeights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // 如果没有选中，返回第一个
        return robotWeights.keySet().iterator().next();
    }
    
    
    /**
     * 处理生成的内容
     */
    private String processGeneratedContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // 限制长度
        if (content.length() > 500) {
            content = content.substring(0, 500);
        }
        
        // 确保不超过5句话
        String[] sentences = content.split("[。！？.!?]");
        if (sentences.length > 5) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                result.append(sentences[i]);
                if (i < 4 && !sentences[i].trim().isEmpty()) {
                    result.append("。");
                }
            }
            content = result.toString();
        }
        
        return content.trim();
    }
    
    
    /**
     * 检查机器人当前是否活跃
     */
    private boolean isRobotActiveNow(Robot robot) {
        // 这里可以根据机器人的活跃时间配置判断
        // 目前简化为检查机器人是否在线
        return robot.getIsActive() != null && robot.getIsActive();
    }
    
    // 其他需要实现的方法...
    
    @Override
    public void updateRobotChatStats(String robotId, String roomId) {
        // 更新机器人聊天统计 - 可以使用缓存或数据库实现
        logger.debug("更新机器人聊天统计: robotId={}, roomId={}", robotId, roomId);
    }
    
    @Override
    public RobotChatStats getRobotChatStats(String robotId, String roomId) {
        // 获取机器人聊天统计 - 可以从缓存或数据库获取
        return new RobotChatStats(robotId, roomId);
    }
    
    @Override
    public void startAllRoomSchedulers() {
        logger.info("启动所有活跃聊天室的调度器");
        try {
            List<ChatRoom> activeChatRooms = chatRoomService.getActiveChatRooms();
            for (ChatRoom room : activeChatRooms) {
                startRoomChatScheduler(room.getRoomId());
            }
        } catch (Exception e) {
            logger.error("启动所有聊天室调度器失败", e);
        }
    }
    
    @Override
    public void stopAllRoomSchedulers() {
        logger.info("停止所有聊天室的调度器");
        try {
            for (String roomId : new HashSet<>(roomScheduledTasks.keySet())) {
                stopRoomChatScheduler(roomId);
            }
        } catch (Exception e) {
            logger.error("停止所有聊天室调度器失败", e);
        }
    }
    
    @Override
    public void adjustChatFrequency(String roomId) {
        logger.info("调整聊天室发言频率: roomId={}", roomId);
        // 重启调度器以应用新的频率设置
        if (isRoomSchedulerRunning(roomId)) {
            stopRoomChatScheduler(roomId);
            startRoomChatScheduler(roomId);
        }
    }
    
    @Override
    public GroupChatMessage generateRobotImageMessage(Robot robot, String roomId, String context) {
        // 生成带图片的机器人消息 - 集成图片生成功能
        // 这里可以调用图片生成服务
        logger.debug("生成机器人图片消息: robotId={}, roomId={}", robot.getRobotId(), roomId);
        return null;
    }
    
    @Override
    public boolean switchTopic(String roomId) {
        // 处理话题切换 - 当聊天内容单调时主动切换话题
        logger.debug("切换话题: roomId={}", roomId);
        return false;
    }
    
    /**
     * 计算回复概率
     */
    private double calculateReplyProbability(Robot robot, GroupChatMessage userMessage) {
        // 基础回复概率
        double baseProbability = robot.getBehaviorPatterns() != null ? 
            robot.getBehaviorPatterns().getSocialEnergy() / 20.0 : 0.025; // 降低回复概率，避免过于频繁
        
        // 如果用户消息包含机器人名字，增加回复概率
        if (userMessage.getContent().contains(robot.getNickname())) {
            baseProbability *= 3.0;
        }
        
        // 如果是回复机器人的消息，增加回复概率
        if (userMessage.getReplyToId() != null) {
            // 检查是否是回复这个机器人的消息
            Optional<GroupChatMessage> replyToMessage = groupChatService.getMessageById(userMessage.getReplyToId());
            if (replyToMessage.isPresent() && robot.getRobotId().equals(replyToMessage.get().getSenderId())) {
                baseProbability *= 2.0;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, baseProbability));
    }
    
    /**
     * 生成机器人回复消息
     */
    private String generateRobotReplyMessage(Robot robot, GroupChatMessage userMessage, List<GroupChatMessage> contextMessages) {
        try {
            // 使用新的提示词服务构建回复提示词
            String chatContext = chatroomPromptService.buildChatContextString(contextMessages, 1000);
            String replyPrompt = chatroomPromptService.buildReplyToUserPrompt(robot, userMessage, chatContext);
            
            DifyChatResult result = difyService.callDifyApi(replyPrompt, robot.getRobotId(), robot.getAppKey());
            String generatedContent = result != null && result.success ? result.answer : "我现在有点忙，稍后再回复您吧~";
            return processGeneratedContent(generatedContent);
            
        } catch (Exception e) {
            logger.error("生成机器人回复消息失败: robotId={}", robot.getRobotId(), e);
            return chatroomPromptService.getFallbackResponse(robot);
        }
    }
    
    /**
     * 判断是否应该回复用户消息
     */
    private boolean shouldReplyToUser(Robot robot, GroupChatMessage userMessage) {
        // 如果用户消息包含机器人名字，则回复
        if (userMessage.getContent().contains(robot.getNickname())) {
            return true;
        }
        
        // 30%概率直接回复用户消息
        return random.nextDouble() < 0.3;
    }
}