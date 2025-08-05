package com.myeden.service.impl;

import com.myeden.entity.GroupChatMessage;
import com.myeden.entity.ChatRoomMember;
import com.myeden.model.WebSocketMessage;
import com.myeden.repository.GroupChatMessageRepository;
import com.myeden.service.GroupChatService;
import com.myeden.service.ChatRoomMemberService;
import com.myeden.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 群聊消息服务实现类
 * 负责群聊消息的存储、查询、统计等功能
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class GroupChatServiceImpl implements GroupChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupChatServiceImpl.class);
    
    @Autowired
    private GroupChatMessageRepository messageRepository;
    
    @Autowired
    private ChatRoomMemberService memberService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public GroupChatMessage sendGroupMessage(String roomId, String senderType, String senderId, 
                                           String content, String imageUrl, String replyToId) {
        try {
            GroupChatMessage message = new GroupChatMessage(roomId, senderType, senderId, content);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                message.setImageUrl(imageUrl);
                message.setMessageType("image");
            }
            if (replyToId != null && !replyToId.isEmpty()) {
                message.setReplyToId(replyToId);
                // 可选：获取原消息的内容摘要
                Optional<GroupChatMessage> originalMessage = messageRepository.findById(replyToId);
                if (originalMessage.isPresent()) {
                    GroupChatMessage orig = originalMessage.get();
                    message.setReplyToContent(orig.getContent().length() > 50 ? 
                                            orig.getContent().substring(0, 50) + "..." : orig.getContent());
                    message.setReplyToSenderNickname(orig.getSenderNickname());
                }
            }
            
            GroupChatMessage saved = messageRepository.save(message);
            logger.debug("群聊消息发送成功: roomId={}, senderId={}, messageId={}", roomId, senderId, saved.getId());
            
            // 通过WebSocket发送消息到聊天室
            try {
                // 创建WebSocket消息
                WebSocketMessage<GroupChatMessage> wsMessage = WebSocketMessage.chat(saved);
                
                // 发送到聊天室主题
                webSocketService.broadcastToRoom(roomId, wsMessage);
                
                logger.debug("WebSocket消息发送成功: roomId={}, messageId={}", roomId, saved.getId());
                
            } catch (Exception wsException) {
                logger.warn("WebSocket消息发送失败: roomId={}, messageId={}", roomId, saved.getId(), wsException);
                // WebSocket发送失败不影响消息保存
            }
            
            return saved;
            
        } catch (Exception e) {
            logger.error("发送群聊消息失败: roomId={}, senderId={}", roomId, senderId, e);
            throw new RuntimeException("发送消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public GroupChatMessage sendSystemMessage(String roomId, String content) {
        try {
            GroupChatMessage message = GroupChatMessage.createSystemMessage(roomId, content);
            GroupChatMessage saved = messageRepository.save(message);
            logger.debug("系统消息发送成功: roomId={}, messageId={}", roomId, saved.getId());
            
            // 通过WebSocket发送系统消息到聊天室
            try {
                // 创建WebSocket系统消息
                WebSocketMessage<GroupChatMessage> wsMessage = WebSocketMessage.systemMessage("系统消息", content, saved);
                
                // 发送到聊天室主题
                webSocketService.broadcastToRoom(roomId, wsMessage);
                
                logger.debug("WebSocket系统消息发送成功: roomId={}, messageId={}", roomId, saved.getId());
                
            } catch (Exception wsException) {
                logger.warn("WebSocket系统消息发送失败: roomId={}, messageId={}", roomId, saved.getId(), wsException);
                // WebSocket发送失败不影响消息保存
            }
            
            return saved;
            
        } catch (Exception e) {
            logger.error("发送系统消息失败: roomId={}", roomId, e);
            throw new RuntimeException("发送系统消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Page<GroupChatMessage> getChatHistory(String roomId, Pageable pageable) {
        try {
            return messageRepository.findByRoomIdAndIsDeletedFalseOrderBySentAtDesc(roomId, pageable);
        } catch (Exception e) {
            logger.error("获取聊天历史失败: roomId={}", roomId, e);
            return Page.empty();
        }
    }
    
    @Override
    public List<GroupChatMessage> getLatestMessages(String roomId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt"));
            List<GroupChatMessage> messages = messageRepository.findByRoomIdAndIsDeletedFalseOrderBySentAtDesc(roomId, pageable).getContent();
            
            // 返回时按时间正序排列
            messages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
            
            logger.debug("获取最新消息成功: roomId={}, limit={}, count={}", roomId, limit, messages.size());
            return messages;
            
        } catch (Exception e) {
            logger.error("获取最新消息失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public List<GroupChatMessage> getMessagesSince(String roomId, LocalDateTime sinceTime) {
        try {
            List<GroupChatMessage> messages = messageRepository.findByRoomIdAndSentAtAfterAndIsDeletedFalseOrderBySentAtAsc(roomId, sinceTime);
            logger.debug("获取指定时间后消息成功: roomId={}, since={}, count={}", roomId, sinceTime, messages.size());
            return messages;
            
        } catch (Exception e) {
            logger.error("获取指定时间后消息失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public Optional<GroupChatMessage> getMessageById(String messageId) {
        try {
            return messageRepository.findById(messageId);
        } catch (Exception e) {
            logger.error("根据ID获取消息失败: messageId={}", messageId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<GroupChatMessage> getReplies(String messageId) {
        try {
            return messageRepository.findByReplyToIdAndIsDeletedFalseOrderBySentAtAsc(messageId);
        } catch (Exception e) {
            logger.error("获取回复消息失败: messageId={}", messageId, e);
            return List.of();
        }
    }
    
    @Override
    public boolean deleteMessage(String messageId, String operatorId) {
        try {
            Optional<GroupChatMessage> message = messageRepository.findById(messageId);
            if (message.isPresent()) {
                GroupChatMessage m = message.get();
                m.softDelete();
                messageRepository.save(m);
                logger.info("删除消息成功: messageId={}, operator={}", messageId, operatorId);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("删除消息失败: messageId={}", messageId, e);
            return false;
        }
    }
    
    @Override
    public List<GroupChatMessage> searchMessages(String roomId, String keyword) {
        try {
            return messageRepository.searchByContent(roomId, keyword);
        } catch (Exception e) {
            logger.error("搜索消息失败: roomId={}, keyword={}", roomId, keyword, e);
            return List.of();
        }
    }
    
    @Override
    public List<GroupChatMessage> getImageMessages(String roomId) {
        try {
            return messageRepository.findImageMessagesByRoomId(roomId);
        } catch (Exception e) {
            logger.error("获取图片消息失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public long countMessages(String roomId) {
        try {
            return messageRepository.countByRoomIdAndIsDeletedFalse(roomId);
        } catch (Exception e) {
            logger.error("统计聊天室消息总数失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public long countMessagesBySender(String roomId, String senderId) {
        try {
            return messageRepository.countByRoomIdAndSenderIdAndIsDeletedFalse(roomId, senderId);
        } catch (Exception e) {
            logger.error("统计用户消息数量失败: roomId={}, senderId={}", roomId, senderId, e);
            return 0;
        }
    }
    
    @Override
    public long countMessagesSince(String roomId, LocalDateTime sinceTime) {
        try {
            return messageRepository.countByRoomIdAndSentAtAfterAndIsDeletedFalse(roomId, sinceTime);
        } catch (Exception e) {
            logger.error("统计指定时间后消息数量失败: roomId={}, since={}", roomId, sinceTime, e);
            return 0;
        }
    }
    
    @Override
    public Optional<GroupChatMessage> getLastMessage(String roomId) {
        try {
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sentAt"));
            Page<GroupChatMessage> messages = messageRepository.findByRoomIdAndIsDeletedFalseOrderBySentAtDesc(roomId, pageable);
            return messages.hasContent() ? Optional.of(messages.getContent().get(0)) : Optional.empty();
        } catch (Exception e) {
            logger.error("获取最后一条消息失败: roomId={}", roomId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<GroupChatMessage> getChatContext(String roomId, int contextSize) {
        try {
            Pageable pageable = PageRequest.of(0, contextSize, Sort.by(Sort.Direction.DESC, "sentAt"));
            List<GroupChatMessage> messages = messageRepository.findByRoomIdAndIsDeletedFalseOrderBySentAtDesc(roomId, pageable).getContent();
            
            // 返回时按时间正序排列，作为上下文
            messages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
            
            logger.debug("获取聊天上下文成功: roomId={}, contextSize={}, count={}", roomId, contextSize, messages.size());
            return messages;
            
        } catch (Exception e) {
            logger.error("获取聊天上下文失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public String buildAIChatContext(String roomId, int contextSize) {
        try {
            List<GroupChatMessage> messages = getChatContext(roomId, contextSize);
            
            StringBuilder context = new StringBuilder();
            for (GroupChatMessage message : messages) {
                String senderName = message.getSenderNickname() != null ? message.getSenderNickname() : message.getSenderId();
                context.append(senderName).append(": ").append(message.getContent()).append("\n");
            }
            
            return context.toString();
            
        } catch (Exception e) {
            logger.error("构建AI聊天上下文失败: roomId={}", roomId, e);
            return "";
        }
    }
    
    @Override
    public int clearChatRoomMessages(String roomId) {
        try {
            List<GroupChatMessage> messages = messageRepository.findByRoomIdAndIsDeletedFalseOrderBySentAtAsc(roomId);
            int count = messages.size();
            messageRepository.deleteAll(messages);
            
            logger.info("清理聊天室消息完成: roomId={}, 清理数量={}", roomId, count);
            return count;
            
        } catch (Exception e) {
            logger.error("清理聊天室消息失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public int cleanupOldMessages(LocalDateTime beforeTime) {
        try {
            messageRepository.deleteBySentAtBefore(beforeTime);
            int count = 0; // 无法获取删除数量
            
            logger.info("清理历史消息完成: before={}, 清理数量={}", beforeTime, count);
            return count;
            
        } catch (Exception e) {
            logger.error("清理历史消息失败: before={}", beforeTime, e);
            return 0;
        }
    }
    
    @Override
    public ChatActivityStats getChatActivityStats(String roomId, int hours) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            // 统计消息数量 - 使用现有方法的简化实现
            List<GroupChatMessage> allMessages = messageRepository.findByRoomIdAndSentAtBetweenAndIsDeletedFalseOrderBySentAtAsc(roomId, startTime, endTime);
            long totalMessages = allMessages.size();
            long userMessages = allMessages.stream().filter(m -> "USER".equals(m.getSenderType())).count();
            long robotMessages = allMessages.stream().filter(m -> "ROBOT".equals(m.getSenderType())).count();
            
            // 统计活跃用户数（发过消息的用户）
            List<String> activeUsers = allMessages.stream()
                .filter(m -> "USER".equals(m.getSenderType()))
                .map(GroupChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
            List<String> activeRobots = allMessages.stream()
                .filter(m -> "ROBOT".equals(m.getSenderType()))
                .map(GroupChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
            
            return new ChatActivityStats(totalMessages, userMessages, robotMessages, 
                                       activeUsers.size(), activeRobots.size(), startTime, endTime);
            
        } catch (Exception e) {
            logger.error("获取聊天活跃度统计失败: roomId={}, hours={}", roomId, hours, e);
            return new ChatActivityStats(0, 0, 0, 0, 0, 
                                       LocalDateTime.now().minusHours(hours), LocalDateTime.now());
        }
    }
}