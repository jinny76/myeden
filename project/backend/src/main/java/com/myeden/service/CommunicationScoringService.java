package com.myeden.service;

import com.myeden.dto.CommunicationReportDto;
import com.myeden.entity.ChatMessage;
import com.myeden.entity.CommunicationReport;
import com.myeden.entity.Robot;
import com.myeden.entity.User;
import com.myeden.repository.ChatMessageRepository;
import com.myeden.repository.CommunicationReportRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;

/**
 * 沟通评分服务
 * 
 * 功能说明：
 * - 定时评估用户已结束的对话质量
 * - 使用AI社交沟通大师背景进行专业评价
 * - 生成沟通报告并给予积分奖励
 * - 避免重复评估同一对话
 * 
 * @author MyEden Team
 * @version 2.0.0
 * @since 2025-07-15
 */
@Service
public class CommunicationScoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommunicationScoringService.class);
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private CommunicationReportRepository communicationReportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private DifyService difyService;
    
    @Autowired
    private PromptService promptService;
    
    /**
     * 定时评估已结束的对话
     * 每30分钟执行一次，只评价当天的对话
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void evaluateCompletedConversations() {
        try {
            logger.info("开始定时评估当天已结束的对话...");
            
            // 获取所有用户
            List<User> allUsers = userRepository.findAll();
            
            int totalEvaluated = 0;
            
            for (User user : allUsers) {
                try {
                    evaluateUserTodayConversations(user.getUserId());
                    totalEvaluated++;
                } catch (Exception e) {
                    logger.error("评估用户 {} 当天的对话失败: {}", user.getUserId(), e.getMessage(), e);
                }
            }
            
            logger.info("定时评估完成，共评估了 {} 个当天对话", totalEvaluated);
            
        } catch (Exception e) {
            logger.error("定时评估对话失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 评估用户今天所有与机器人对话的结束情况，并对已结束的对话进行评分
     * @param userId 用户ID
     */
    public void evaluateUserTodayConversations(String userId) {
        // 1. 只查找今天已评估的conversationId，历史会话可重复评估
        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime todayEnd = java.time.LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        Set<String> evaluatedConversationIds = communicationReportRepository
            .findEvaluatedConversationIdsByUserIdAndDateRange(userId, todayStart, todayEnd);

        // 2. 只查找今天的消息
        List<ChatMessage> messages = chatMessageRepository.findByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        if (messages == null || messages.isEmpty()) return;

        // 3. 按robotId分组（senderType/receiverType为'robot'时，robotId为senderId或receiverId）
        Map<String, List<ChatMessage>> messagesByRobot = messages.stream()
            .filter(msg -> ("robot".equals(msg.getSenderType()) || "robot".equals(msg.getReceiverType())))
            .collect(Collectors.groupingBy(msg -> {
                if ("robot".equals(msg.getSenderType())) return msg.getSenderId();
                if ("robot".equals(msg.getReceiverType())) return msg.getReceiverId();
                return "unknown";
            }));

        // 4. 遍历每个robot
        for (Map.Entry<String, List<ChatMessage>> entry : messagesByRobot.entrySet()) {
            String robotId = entry.getKey();
            if ("unknown".equals(robotId)) continue;
            List<ChatMessage> robotMsgs = entry.getValue();
            if (robotMsgs == null || robotMsgs.isEmpty()) continue;

            // 5. 找到该robot下所有conversationId
            Set<String> conversationIds = robotMsgs.stream()
                    .map(ChatMessage::getConversationId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 6. 找到该robot的“最后一次”会话ID（按时间排序，取最新的conversationId）
            String lastConversationId = robotMsgs.stream()
                    .max(Comparator.comparing(ChatMessage::getCreatedAt))
                    .map(ChatMessage::getConversationId)
                    .orElse(null);

            // 7. 只对不是最后一次且今天未被评估的conversationId进行评价
            for (String convId : conversationIds) {
                if (evaluatedConversationIds.contains(convId)) continue; // 跳过今天已评估
                if (lastConversationId == null || !convId.equals(lastConversationId)) {
                    // 该conversationId不是最后一次会话，且今天未被评估，可以评价
                    scoreConversation(userId, robotId, convId);
                }
            }
        }
    }

    /**
     * 对指定会话进行评分（需根据实际业务实现）
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param conversationId 会话ID
     */
    private void scoreConversation(String userId, String robotId, String conversationId) {
        try {
            // 获取对话中的所有消息
            List<ChatMessage> conversationMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            if (conversationMessages.isEmpty()) {
                logger.debug("对话 {} 没有消息，跳过评估", conversationId);
                return;
            }
            
            // 过滤出用户的消息（排除系统消息等）
            List<ChatMessage> userMessages = conversationMessages.stream()
                .filter(msg -> userId.equals(msg.getSenderId()) || userId.equals(msg.getReceiverId()))
                .collect(Collectors.toList());
            
            if (userMessages.size() < 2) {
                logger.debug("对话 {} 消息数量不足，跳过评估", conversationId);
                return;
            }
            
            // 生成沟通评估报告
            CommunicationEvaluationResult result = generateCommunicationReport(userId, conversationId, userMessages);
            
            if (result != null) {
                // 保存评估报告
                saveCommunicationReport(userId, conversationId, result);
                
                // 给用户加积分
                awardPointsToUser(userId, result.getScore());
                
                logger.info("对话评估完成，用户: {}, 对话ID: {}, 评分: {}, 积分: {}", 
                          userId, conversationId, result.getScore(), result.getPointsAwarded());
            }
            
        } catch (Exception e) {
            logger.error("评估对话失败，用户: {}, 对话ID: {}, 错误: {}", userId, conversationId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户最新的对话ID
     * 
     * @param userId 用户ID
     * @return 最新的对话ID
     */
    private String getLatestConversationId(String userId) {
        try {
            List<ChatMessage> latestMessages = chatMessageRepository.findTopByUserIdOrderByCreatedAtDesc(userId, 1);
            if (!latestMessages.isEmpty()) {
                return latestMessages.get(0).getConversationId();
            }
            return null;
        } catch (Exception e) {
            logger.error("获取用户 {} 最新对话ID失败: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 使用AI生成沟通评估报告
     * 
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param messages 消息列表
     * @return 评估结果
     */
    private CommunicationEvaluationResult generateCommunicationReport(String userId, String conversationId, List<ChatMessage> messages) {
        try {
            // 构建对话内容
            StringBuilder conversationContent = new StringBuilder();
            for (ChatMessage message : messages) {
                String senderType = message.getSenderType();
                String content = message.getContent();
                
                if (content != null && !content.trim().isEmpty()) {
                    if ("user".equals(senderType)) {
                        conversationContent.append("用户: ").append(content).append("\n");
                    } else if ("robot".equals(senderType) || "ai".equals(senderType)) {
                        conversationContent.append("对方: ").append(content).append("\n");
                    }
                }
            }
            
            if (conversationContent.length() == 0) {
                logger.debug("对话 {} 没有有效内容", conversationId);
                return null;
            }
            
            // 构建社交沟通大师的评估prompt
            String evaluationPrompt = buildCommunicationMasterPrompt(conversationContent.toString());
            
            // 调用AI进行评估
            DifyService.DifyChatResult aiResult = difyService.callDifyApi(evaluationPrompt, "communication_master", null, null);
            
            if (aiResult != null && aiResult.answer != null) {
                // 解析AI的评估结果
                return parseAIEvaluationResult(aiResult.answer);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("生成沟通评估报告失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 构建社交沟通大师的评估prompt
     * 
     * @param conversationContent 对话内容
     * @return 评估prompt
     */
    private String buildCommunicationMasterPrompt(String conversationContent) {
        return String.format(
            "你是一位资深的社交沟通大师，拥有心理学博士学位，专门研究人际沟通和社交技巧，目标是帮助用户在未来提升与人沟通的能力和技巧。" +
            "现在请你分析以下对话，从专业角度评估用户本人的沟通质量，可以参考对方的话语，但仅做参考，无需对对方的话语进行评估。\n\n" +
            "对话内容：\n%s\n\n" +
            "请从以下维度进行评估：\n" +
            "1. 沟通深度 (0-10分): 用户是否有深入的思考和分享\n" +
            "2. 情感表达 (0-10分): 用户情感的真实性和丰富度\n" +
            "3. 互动质量 (0-10分): 用户回应的及时性和相关性\n" +
            "4. 语言表达 (0-10分): 用户表达的清晰度和准确性\n" +
            "5. 共情能力 (0-10分): 用户对对方的理解和关怀\n\n" +
            "请按以下格式返回评估结果：\n" +
            "总分: [0-10的整数]\n" +
            "沟通深度: [0-10的整数]\n" +
            "情感表达: [0-10的整数]\n" +
            "互动质量: [0-10的整数]\n" +
            "语言表达: [0-10的整数]\n" +
            "共情能力: [0-10的整数]\n" +
            "评价: [详细的对用户内容的专业评价，包括优点、不足和改进建议，100-300字, 可以举例]\n" +
            "建议: [具体的对用户沟通的改进建议，50-150字, 可以举例]",
            conversationContent
        );
    }
    
    /**
     * 解析AI的评估结果
     * 
     * @param aiResponse AI的回复
     * @return 评估结果对象
     */
    private CommunicationEvaluationResult parseAIEvaluationResult(String aiResponse) {
        try {
            CommunicationEvaluationResult result = new CommunicationEvaluationResult();
            
            // 解析各项评分
            result.setScore(extractScore(aiResponse, "总分"));
            result.setDepthScore(extractScore(aiResponse, "沟通深度"));
            result.setEmotionScore(extractScore(aiResponse, "情感表达"));
            result.setInteractionScore(extractScore(aiResponse, "互动质量"));
            result.setLanguageScore(extractScore(aiResponse, "语言表达"));
            result.setEmpathyScore(extractScore(aiResponse, "共情能力"));
            
            // 解析评价和建议
            result.setEvaluation(extractContent(aiResponse, "评价"));
            result.setSuggestions(extractContent(aiResponse, "建议"));
            
            // 计算积分奖励
            result.setPointsAwarded(calculatePointsReward(result.getScore()));
            
            return result;
            
        } catch (Exception e) {
            logger.error("解析AI评估结果失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从AI回复中提取评分
     * 
     * @param response AI回复
     * @param scoreName 评分名称
     * @return 评分值
     */
    private int extractScore(String response, String scoreName) {
        try {
            String pattern = scoreName + ":\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(response);
            
            if (m.find()) {
                int score = Integer.parseInt(m.group(1));
                return Math.max(0, Math.min(10, score)); // 确保分数在0-10之间
            }
            
            return 5; // 默认中等分数
            
        } catch (Exception e) {
            logger.warn("提取评分失败，评分名称: {}, 错误: {}", scoreName, e.getMessage());
            return 5;
        }
    }
    
    /**
     * 从AI回复中提取内容
     * 
     * @param response AI回复
     * @param contentName 内容名称
     * @return 内容字符串
     */
    private String extractContent(String response, String contentName) {
        try {
            String pattern = contentName + ":\\s*(.+?)(?=\\n[\\u4e00-\\u9fa5]+:|$)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(response);
            
            if (m.find()) {
                return m.group(1).trim();
            }
            
            return "暂无" + contentName;
            
        } catch (Exception e) {
            logger.warn("提取内容失败，内容名称: {}, 错误: {}", contentName, e.getMessage());
            return "暂无" + contentName;
        }
    }
    
    /**
     * 保存沟通评估报告
     * 
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param result 评估结果
     */
    private void saveCommunicationReport(String userId, String conversationId, CommunicationEvaluationResult result) {
        try {
            CommunicationReport report = new CommunicationReport();
            report.setUserId(userId);
            report.setConversationId(conversationId);
            report.setScore(result.getScore());
            report.setDepthScore(result.getDepthScore());
            report.setEmotionScore(result.getEmotionScore());
            report.setInteractionScore(result.getInteractionScore());
            report.setLanguageScore(result.getLanguageScore());
            report.setEmpathyScore(result.getEmpathyScore());
            report.setEvaluation(result.getEvaluation());
            report.setSuggestions(result.getSuggestions());
            report.setPointsAwarded(result.getPointsAwarded());
            report.setCreatedAt(LocalDateTime.now());
            
            communicationReportRepository.save(report);
            
        } catch (Exception e) {
            logger.error("保存沟通评估报告失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 给用户加积分
     * 
     * @param userId 用户ID
     * @param score 评分
     */
    private void awardPointsToUser(String userId, int score) {
        try {
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user != null) {
                int pointsToAdd = calculatePointsReward(score);
                Integer currentPoints = user.getPoints();
                if (currentPoints == null) {
                    currentPoints = 0;
                }
                user.setPoints(currentPoints + pointsToAdd);
                userRepository.save(user);
                
                logger.debug("用户 {} 获得积分: {} (当前总积分: {})", userId, pointsToAdd, user.getPoints());
            }
        } catch (Exception e) {
            logger.error("给用户加积分失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 根据评分计算积分奖励
     * 
     * @param score 评分
     * @return 积分奖励
     */
    private int calculatePointsReward(int score) {
        if (score >= 9) return 20;
        if (score >= 8) return 15;
        if (score >= 7) return 12;
        if (score >= 6) return 10;
        if (score >= 5) return 8;
        if (score >= 4) return 5;
        if (score >= 3) return 3;
        if (score >= 2) return 1;
        return 0;
    }
    
    /**
     * 获取用户的沟通报告列表
     * 
     * @param userId 用户ID
     * @return 沟通报告列表
     */
    public List<CommunicationReport> getUserCommunicationReports(String userId) {
        try {
            return communicationReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            logger.error("获取用户沟通报告失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取用户的沟通报告列表（扩展信息）
     * 
     * @param userId 用户ID
     * @return 扩展的沟通报告列表
     */
    public List<CommunicationReportDto> getUserCommunicationReportsWithDetails(String userId) {
        try {
            List<CommunicationReport> reports = communicationReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            return reports.stream()
                    .map(report -> {
                        // 获取对方名称和第一句话
                        String partnerName = getPartnerName(report.getConversationId());
                        String firstMessage = getFirstMessage(report.getConversationId());
                        
                        return CommunicationReportDto.fromEntity(report, partnerName, firstMessage);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取用户沟通报告详情失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取对话中的对方名称（机器人名称）
     * 
     * @param conversationId 对话ID
     * @return 对方名称
     */
    private String getPartnerName(String conversationId) {
        try {
            // 获取对话中的第一条消息来确定机器人ID
            List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            if (messages.isEmpty()) {
                return "未知对话者";
            }
            
            // 找到机器人ID
            String robotId = null;
            for (ChatMessage msg : messages) {
                if ("robot".equals(msg.getSenderType())) {
                    robotId = msg.getSenderId();
                    break;
                } else if ("robot".equals(msg.getReceiverType())) {
                    robotId = msg.getReceiverId();
                    break;
                }
            }
            
            if (robotId == null) {
                return "未知对话者";
            }
            
            // 通过机器人ID获取机器人名称
            Robot robot = robotRepository.findByRobotId(robotId).orElse(null);
            if (robot != null) {
                return robot.getName();
            }
            
            return "机器人-" + robotId;
            
        } catch (Exception e) {
            logger.warn("获取对话 {} 的对方名称失败: {}", conversationId, e.getMessage());
            return "未知对话者";
        }
    }
    
    /**
     * 获取对话的第一句话
     * 
     * @param conversationId 对话ID
     * @return 第一句话内容
     */
    private String getFirstMessage(String conversationId) {
        try {
            List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            if (messages.isEmpty()) {
                return "暂无对话内容";
            }
            
            // 找到第一条有效消息
            for (ChatMessage msg : messages) {
                if (msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                    // 截取前100个字符，避免过长
                    String content = msg.getContent().trim();
                    if (content.length() > 100) {
                        return content.substring(0, 100) + "...";
                    }
                    return content;
                }
            }
            
            return "暂无对话内容";
            
        } catch (Exception e) {
            logger.warn("获取对话 {} 的第一句话失败: {}", conversationId, e.getMessage());
            return "暂无对话内容";
        }
    }
    
    /**
     * 获取过滤后的沟通报告列表
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID过滤（可选）
     * @param startDate 开始日期过滤（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期过滤（可选，格式：yyyy-MM-dd）
     * @return 过滤后的沟通报告列表
     */
    public List<CommunicationReport> getFilteredCommunicationReports(String userId, String robotId, String startDate, String endDate) {
        try {
            List<CommunicationReport> allReports = communicationReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            return allReports.stream()
                .filter(report -> {
                    // 机器人ID过滤
                    if (robotId != null && !robotId.trim().isEmpty()) {
                        // 通过conversationId查找相关的聊天记录来获取机器人ID
                        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(report.getConversationId());
                        boolean hasRobotId = messages.stream()
                            .anyMatch(msg -> robotId.equals(msg.getSenderId()) || robotId.equals(msg.getReceiverId()));
                        if (!hasRobotId) {
                            return false;
                        }
                    }
                    
                    // 日期过滤
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        try {
                            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                            if (report.getCreatedAt().isBefore(startDateTime)) {
                                return false;
                            }
                        } catch (Exception e) {
                            logger.warn("解析开始日期失败: {}", startDate);
                        }
                    }
                    
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        try {
                            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                            if (report.getCreatedAt().isAfter(endDateTime)) {
                                return false;
                            }
                        } catch (Exception e) {
                            logger.warn("解析结束日期失败: {}", endDate);
                        }
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            logger.error("获取过滤沟通报告失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取用户的沟通统计
     * 
     * @param userId 用户ID
     * @return 沟通统计
     */
    public CommunicationStatistics getUserCommunicationStatistics(String userId) {
        try {
            List<CommunicationReport> reports = communicationReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            // 获取用户的总积分
            int totalUserPoints = 0;
            try {
                User user = userRepository.findByUserId(userId).orElse(null);
                if (user != null && user.getPoints() != null) {
                    totalUserPoints = user.getPoints();
                }
            } catch (Exception e) {
                logger.warn("获取用户积分失败，用户ID: {}", userId, e);
            }
            
            if (reports.isEmpty()) {
                CommunicationStatistics stats = new CommunicationStatistics(userId, 0, 0.0, 0, 0, 0, 0, 0, 0);
                stats.setUserLevel(calculateUserLevel(totalUserPoints));
                return stats;
            }
            
            int totalReports = reports.size();
            double averageScore = reports.stream().mapToInt(CommunicationReport::getScore).average().orElse(0.0);
            int totalPoints = reports.stream().mapToInt(CommunicationReport::getPointsAwarded).sum();
            
            double averageDepth = reports.stream().mapToInt(CommunicationReport::getDepthScore).average().orElse(0.0);
            double averageEmotion = reports.stream().mapToInt(CommunicationReport::getEmotionScore).average().orElse(0.0);
            double averageInteraction = reports.stream().mapToInt(CommunicationReport::getInteractionScore).average().orElse(0.0);
            double averageLanguage = reports.stream().mapToInt(CommunicationReport::getLanguageScore).average().orElse(0.0);
            double averageEmpathy = reports.stream().mapToInt(CommunicationReport::getEmpathyScore).average().orElse(0.0);
            
            CommunicationStatistics stats = new CommunicationStatistics(
                userId, totalReports, averageScore, totalPoints,
                averageDepth, averageEmotion, averageInteraction, averageLanguage, averageEmpathy
            );
            
            // 设置用户等级信息
            stats.setUserLevel(calculateUserLevel(totalUserPoints));
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取用户沟通统计失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            CommunicationStatistics stats = new CommunicationStatistics(userId, 0, 0.0, 0, 0, 0, 0, 0, 0);
            stats.setUserLevel(calculateUserLevel(0));
            return stats;
        }
    }
    
    /**
     * 沟通评估结果内部类
     */
    private static class CommunicationEvaluationResult {
        private int score;
        private int depthScore;
        private int emotionScore;
        private int interactionScore;
        private int languageScore;
        private int empathyScore;
        private String evaluation;
        private String suggestions;
        private int pointsAwarded;
        
        // Getters and Setters
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public int getDepthScore() { return depthScore; }
        public void setDepthScore(int depthScore) { this.depthScore = depthScore; }
        
        public int getEmotionScore() { return emotionScore; }
        public void setEmotionScore(int emotionScore) { this.emotionScore = emotionScore; }
        
        public int getInteractionScore() { return interactionScore; }
        public void setInteractionScore(int interactionScore) { this.interactionScore = interactionScore; }
        
        public int getLanguageScore() { return languageScore; }
        public void setLanguageScore(int languageScore) { this.languageScore = languageScore; }
        
        public int getEmpathyScore() { return empathyScore; }
        public void setEmpathyScore(int empathyScore) { this.empathyScore = empathyScore; }
        
        public String getEvaluation() { return evaluation; }
        public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
        
        public String getSuggestions() { return suggestions; }
        public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
        
        public int getPointsAwarded() { return pointsAwarded; }
        public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    }
    
    /**
     * 根据积分计算用户等级
     * 
     * @param points 积分
     * @return 用户等级信息
     */
    public UserLevel calculateUserLevel(int points) {
        if (points < 50) {
            return new UserLevel(1, "新手", points, 50, "刚开始学习沟通技巧");
        } else if (points < 150) {
            return new UserLevel(2, "入门", points, 150, "基础沟通能力正在提升");
        } else if (points < 300) {
            return new UserLevel(3, "熟练", points, 300, "具备良好的沟通技巧");
        } else if (points < 500) {
            return new UserLevel(4, "专家", points, 500, "沟通能力出色，能够深度交流");
        } else if (points < 800) {
            return new UserLevel(5, "大师", points, 800, "沟通大师，擅长各种社交场景");
        } else {
            return new UserLevel(6, "宗师", points, -1, "沟通宗师，达到了极高的水平");
        }
    }
    
    /**
     * 用户等级信息类
     */
    public static class UserLevel {
        private int level;
        private String levelName;
        private int currentPoints;
        private int nextLevelPoints;
        private String description;
        
        public UserLevel(int level, String levelName, int currentPoints, int nextLevelPoints, String description) {
            this.level = level;
            this.levelName = levelName;
            this.currentPoints = currentPoints;
            this.nextLevelPoints = nextLevelPoints;
            this.description = description;
        }
        
        // Getters
        public int getLevel() { return level; }
        public String getLevelName() { return levelName; }
        public int getCurrentPoints() { return currentPoints; }
        public int getNextLevelPoints() { return nextLevelPoints; }
        public String getDescription() { return description; }
        
        public int getPointsToNextLevel() {
            return nextLevelPoints > 0 ? nextLevelPoints - currentPoints : 0;
        }
        
        public double getProgress() {
            if (nextLevelPoints <= 0) return 100.0;
            int prevLevelPoints = getPrevLevelPoints();
            return ((double)(currentPoints - prevLevelPoints) / (nextLevelPoints - prevLevelPoints)) * 100;
        }
        
        private int getPrevLevelPoints() {
            switch (level) {
                case 1: return 0;
                case 2: return 50;
                case 3: return 150;
                case 4: return 300;
                case 5: return 500;
                case 6: return 800;
                default: return 0;
            }
        }
    }
    
    /**
     * 沟通统计类
     */
    public static class CommunicationStatistics {
        private String userId;
        private int totalReports;
        private double averageScore;
        private int totalPointsEarned;
        private double averageDepthScore;
        private double averageEmotionScore;
        private double averageInteractionScore;
        private double averageLanguageScore;
        private double averageEmpathyScore;
        private UserLevel userLevel;
        
        public CommunicationStatistics(String userId, int totalReports, double averageScore, int totalPointsEarned,
                                     double averageDepthScore, double averageEmotionScore, double averageInteractionScore,
                                     double averageLanguageScore, double averageEmpathyScore) {
            this.userId = userId;
            this.totalReports = totalReports;
            this.averageScore = averageScore;
            this.totalPointsEarned = totalPointsEarned;
            this.averageDepthScore = averageDepthScore;
            this.averageEmotionScore = averageEmotionScore;
            this.averageInteractionScore = averageInteractionScore;
            this.averageLanguageScore = averageLanguageScore;
            this.averageEmpathyScore = averageEmpathyScore;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public int getTotalReports() { return totalReports; }
        public double getAverageScore() { return averageScore; }
        public int getTotalPointsEarned() { return totalPointsEarned; }
        public double getAverageDepthScore() { return averageDepthScore; }
        public double getAverageEmotionScore() { return averageEmotionScore; }
        public double getAverageInteractionScore() { return averageInteractionScore; }
        public double getAverageLanguageScore() { return averageLanguageScore; }
        public double getAverageEmpathyScore() { return averageEmpathyScore; }
        public UserLevel getUserLevel() { return userLevel; }
        
        public void setUserLevel(UserLevel userLevel) { this.userLevel = userLevel; }
    }
}