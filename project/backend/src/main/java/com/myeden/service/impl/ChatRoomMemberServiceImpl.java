package com.myeden.service.impl;

import com.myeden.entity.ChatRoomMember;
import com.myeden.entity.Robot;
import com.myeden.repository.ChatRoomMemberRepository;
import com.myeden.repository.RobotRepository;
import com.myeden.service.ChatRoomMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 聊天室成员服务实现类
 * 负责聊天室成员的管理、状态同步和权限控制
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class ChatRoomMemberServiceImpl implements ChatRoomMemberService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomMemberServiceImpl.class);
    
    @Autowired
    private ChatRoomMemberRepository memberRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Override
    public ChatRoomMember addMemberToChatRoom(String roomId, String memberType, String memberId, 
                                               String memberNickname, String memberAvatar) {
        try {
            // 检查成员是否已存在
            Optional<ChatRoomMember> existing = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            if (existing.isPresent()) {
                logger.debug("成员已存在于聊天室: roomId={}, memberId={}", roomId, memberId);
                return existing.get();
            }
            
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomId(roomId);
            member.setMemberType(memberType);
            member.setMemberId(memberId);
            member.setMemberNickname(memberNickname);
            member.setMemberAvatar(memberAvatar);
            member.setRole("USER".equals(memberType) ? "MEMBER" : "ROBOT");
            member.setIsOnline(true);
            member.setIsMuted(false);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastActiveAt(LocalDateTime.now());
            
            ChatRoomMember saved = memberRepository.save(member);
            logger.info("成员添加到聊天室成功: roomId={}, memberId={}, type={}", roomId, memberId, memberType);
            return saved;
            
        } catch (Exception e) {
            logger.error("添加成员到聊天室失败: roomId={}, memberId={}", roomId, memberId, e);
            throw new RuntimeException("添加成员失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean removeMemberFromChatRoom(String roomId, String memberId) {
        try {
            Optional<ChatRoomMember> member = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            if (member.isPresent()) {
                memberRepository.delete(member.get());
                logger.info("成员从聊天室移除成功: roomId={}, memberId={}", roomId, memberId);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("从聊天室移除成员失败: roomId={}, memberId={}", roomId, memberId, e);
            return false;
        }
    }
    
    @Override
    public List<ChatRoomMember> getChatRoomMembers(String roomId) {
        try {
            return memberRepository.findByRoomId(roomId);
        } catch (Exception e) {
            logger.error("获取聊天室成员失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ChatRoomMember> getOnlineMembers(String roomId) {
        try {
            return memberRepository.findByRoomIdAndIsOnline(roomId, true);
        } catch (Exception e) {
            logger.error("获取聊天室在线成员失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ChatRoomMember> getRobotMembers(String roomId) {
        try {
            return memberRepository.findByRoomIdAndMemberType(roomId, "ROBOT");
        } catch (Exception e) {
            logger.error("获取聊天室机器人成员失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ChatRoomMember> getActiveRobotMembers(String roomId) {
        try {
            return memberRepository.findActiveRobotsByRoomId(roomId);
        } catch (Exception e) {
            logger.error("获取聊天室在线机器人成员失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ChatRoomMember> getUserMembers(String roomId) {
        try {
            return memberRepository.findByRoomIdAndMemberType(roomId, "USER");
        } catch (Exception e) {
            logger.error("获取聊天室用户成员失败: roomId={}", roomId, e);
            return List.of();
        }
    }
    
    @Override
    public Optional<ChatRoomMember> getChatRoomMember(String roomId, String memberId) {
        try {
            return memberRepository.findByRoomIdAndMemberId(roomId, memberId);
        } catch (Exception e) {
            logger.error("获取聊天室成员失败: roomId={}, memberId={}", roomId, memberId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean updateMemberOnlineStatus(String roomId, String memberId, Boolean isOnline) {
        try {
            Optional<ChatRoomMember> member = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            if (member.isPresent()) {
                ChatRoomMember m = member.get();
                m.setIsOnline(isOnline);
                if (isOnline) {
                    m.setLastActiveAt(LocalDateTime.now());
                }
                memberRepository.save(m);
                logger.debug("成员在线状态更新成功: roomId={}, memberId={}, isOnline={}", roomId, memberId, isOnline);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("更新成员在线状态失败: roomId={}, memberId={}", roomId, memberId, e);
            return false;
        }
    }
    
    @Override
    public void updateMemberLastActiveTime(String roomId, String memberId) {
        try {
            Optional<ChatRoomMember> member = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            if (member.isPresent()) {
                ChatRoomMember m = member.get();
                m.setLastActiveAt(LocalDateTime.now());
                memberRepository.save(m);
                logger.debug("成员活跃时间更新成功: roomId={}, memberId={}", roomId, memberId);
            }
            
        } catch (Exception e) {
            logger.error("更新成员活跃时间失败: roomId={}, memberId={}", roomId, memberId, e);
        }
    }
    
    @Override
    public boolean setMemberMuteStatus(String roomId, String memberId, Boolean isMuted) {
        try {
            Optional<ChatRoomMember> member = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            if (member.isPresent()) {
                ChatRoomMember m = member.get();
                m.setIsMuted(isMuted);
                memberRepository.save(m);
                logger.info("成员静音状态更新成功: roomId={}, memberId={}, isMuted={}", roomId, memberId, isMuted);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("设置成员静音状态失败: roomId={}, memberId={}", roomId, memberId, e);
            return false;
        }
    }
    
    @Override
    public boolean isMemberInChatRoom(String roomId, String memberId) {
        try {
            return memberRepository.findByRoomIdAndMemberId(roomId, memberId).isPresent();
        } catch (Exception e) {
            logger.error("检查成员是否在聊天室失败: roomId={}, memberId={}", roomId, memberId, e);
            return false;
        }
    }
    
    @Override
    public boolean isChatRoomOwner(String roomId, String memberId) {
        try {
            Optional<ChatRoomMember> member = memberRepository.findByRoomIdAndMemberId(roomId, memberId);
            return member.isPresent() && "OWNER".equals(member.get().getRole());
        } catch (Exception e) {
            logger.error("检查成员是否为房主失败: roomId={}, memberId={}", roomId, memberId, e);
            return false;
        }
    }

    @Override
    public ChatRoomMember getChatRoomOwner(String roomId) {
        try {
            return memberRepository.findByRoomIdAndRole(roomId, "OWNER").get(0);
        } catch (Exception e) {
            logger.error("获取聊天室房主失败: roomId={}", roomId, e);
            return null;
        }
    }
    
    @Override
    public long countChatRoomMembers(String roomId) {
        try {
            return memberRepository.countByRoomId(roomId);
        } catch (Exception e) {
            logger.error("统计聊天室成员数量失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public long countOnlineMembers(String roomId) {
        try {
            return memberRepository.countByRoomIdAndIsOnline(roomId, true);
        } catch (Exception e) {
            logger.error("统计聊天室在线成员数量失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public long countRobotMembers(String roomId) {
        try {
            return memberRepository.countByRoomIdAndMemberType(roomId, "ROBOT");
        } catch (Exception e) {
            logger.error("统计聊天室机器人成员数量失败: roomId={}", roomId, e);
            return 0;
        }
    }
    
    @Override
    public void refreshRobotOnlineStatus(String roomId) {
        try {
            List<ChatRoomMember> robotMembers = getRobotMembers(roomId);
            
            for (ChatRoomMember member : robotMembers) {
                // 查询机器人的活跃状态
                Optional<Robot> robot = robotRepository.findByRobotId(member.getMemberId());
                if (robot.isPresent()) {
                    boolean shouldBeOnline = robot.get().getIsActive() && robot.get().isInActiveTimeSlot();
                    
                    if (member.getIsOnline() != shouldBeOnline) {
                        member.setIsOnline(shouldBeOnline);
                        if (shouldBeOnline) {
                            member.setLastActiveAt(LocalDateTime.now());
                        }
                        memberRepository.save(member);
                        logger.debug("机器人在线状态已同步: robotId={}, isOnline={}", member.getMemberId(), shouldBeOnline);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("刷新机器人在线状态失败: roomId={}", roomId, e);
        }
    }
    
    @Override
    public int batchAddRobotsToRoom(String roomId, List<String> robotIds) {
        int successCount = 0;
        
        try {
            for (String robotId : robotIds) {
                // 查询机器人信息
                Optional<Robot> robot = robotRepository.findByRobotId(robotId);
                if (robot.isPresent()) {
                    Robot r = robot.get();
                    ChatRoomMember member = addMemberToChatRoom(roomId, "ROBOT", robotId, r.getNickname(), r.getAvatar());
                    if (member != null) {
                        successCount++;
                    }
                } else {
                    logger.warn("机器人不存在，跳过添加: robotId={}", robotId);
                }
            }
            
            logger.info("批量添加机器人到聊天室完成: roomId={}, 成功={}/{}", roomId, successCount, robotIds.size());
            
        } catch (Exception e) {
            logger.error("批量添加机器人到聊天室失败: roomId={}", roomId, e);
        }
        
        return successCount;
    }
    
    @Override
    public int batchRemoveRobotsFromRoom(String roomId, List<String> robotIds) {
        int successCount = 0;
        
        try {
            for (String robotId : robotIds) {
                if (removeMemberFromChatRoom(roomId, robotId)) {
                    successCount++;
                }
            }
            
            logger.info("批量从聊天室移除机器人完成: roomId={}, 成功={}/{}", roomId, successCount, robotIds.size());
            
        } catch (Exception e) {
            logger.error("批量从聊天室移除机器人失败: roomId={}", roomId, e);
        }
        
        return successCount;
    }
    
    @Override
    public int clearChatRoomMembers(String roomId) {
        try {
            List<ChatRoomMember> members = memberRepository.findByRoomId(roomId);
            int count = members.size();
            memberRepository.deleteAll(members);
            
            logger.info("清理聊天室成员完成: roomId={}, 清理数量={}", roomId, count);
            return count;
            
        } catch (Exception e) {
            logger.error("清理聊天室成员失败: roomId={}", roomId, e);
            return 0;
        }
    }
}