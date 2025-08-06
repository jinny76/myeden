package com.myeden.controller;

import com.myeden.entity.ChatRoom;
import com.myeden.entity.ChatRoomMember;
import com.myeden.entity.GroupChatMessage;
import com.myeden.entity.User;
import com.myeden.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 聊天室控制器
 * 提供聊天室管理、成员管理和消息处理的REST API接口
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@RestController
@RequestMapping("/api/v1/chatroom")
public class ChatRoomController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
    
    @Autowired
    private ChatRoomService chatRoomService;
    
    @Autowired
    private ChatRoomMemberService memberService;
    
    @Autowired
    private GroupChatService groupChatService;
    
    @Autowired
    private RobotChatService robotChatService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OnlineUserService onlineUserService;
    
    /**
     * 创建或获取用户的聊天室
     */
    @PostMapping("/create")
    public ResponseEntity<EventResponse> createOrGetChatRoom() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            Optional<User> user = userService.getUserById(userId);

            if (!user.isPresent()) {
                return ResponseEntity.badRequest().body(EventResponse.error("用户不存在"));
            }
            
            logger.info("创建或获取聊天室请求: userId={}", userId);
            
            ChatRoom chatRoom = chatRoomService.createOrGetUserChatRoom(userId, user.get().getNickname());
            
            // 启动聊天室的机器人调度
            if (chatRoom != null) {
                robotChatService.startRoomChatScheduler(chatRoom.getRoomId());
            }
            
            return ResponseEntity.ok(EventResponse.success(chatRoom, "聊天室创建成功"));
            
        } catch (Exception e) {
            logger.error("创建聊天室失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("创建聊天室失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天室信息
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<EventResponse> getChatRoom(@PathVariable String roomId) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查用户是否有权限访问该聊天室（只能访问自己的聊天室）
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限访问该聊天室"));
            }
            
            Optional<ChatRoom> chatRoom = chatRoomService.getChatRoomById(roomId);
            if (chatRoom.isPresent()) {
                return ResponseEntity.ok(EventResponse.success(chatRoom.get(), "获取聊天室信息成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("聊天室不存在"));
            }
            
        } catch (Exception e) {
            logger.error("获取聊天室信息失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("获取聊天室信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新聊天室名称
     */
    @PutMapping("/{roomId}/name")
    public ResponseEntity<EventResponse> updateChatRoomName(
            @PathVariable String roomId,
            @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限修改该聊天室"));
            }
            
            String newName = request.get("roomName");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error("聊天室名称不能为空"));
            }
            
            boolean success = chatRoomService.updateChatRoomName(roomId, newName.trim());
            if (success) {
                return ResponseEntity.ok(EventResponse.success(newName, "聊天室名称更新成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("聊天室名称更新失败"));
            }
            
        } catch (Exception e) {
            logger.error("更新聊天室名称失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("更新聊天室名称失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天室成员列表
     */
    @GetMapping("/{roomId}/members")
    public ResponseEntity<EventResponse> getChatRoomMembers(@PathVariable String roomId) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限访问该聊天室成员"));
            }
            
            List<ChatRoomMember> members = memberService.getChatRoomMembers(roomId);
            return ResponseEntity.ok(EventResponse.success(members, "获取成员列表成功"));
            
        } catch (Exception e) {
            logger.error("获取聊天室成员失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("获取聊天室成员失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加机器人到聊天室
     */
    @PostMapping("/{roomId}/members/robot")
    public ResponseEntity<EventResponse> addRobotToRoom(
            @PathVariable String roomId,
            @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限修改该聊天室"));
            }
            
            String robotId = request.get("robotId");
            String robotNickname = request.get("robotNickname");
            String robotAvatar = request.get("robotAvatar");
            
            if (robotId == null || robotId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error("机器人ID不能为空"));
            }
            
            ChatRoomMember member = memberService.addMemberToChatRoom(
                roomId, "ROBOT", robotId, robotNickname, robotAvatar);
            
            return ResponseEntity.ok(EventResponse.success(member, "机器人添加成功"));
            
        } catch (Exception e) {
            logger.error("添加机器人到聊天室失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("添加机器人失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从聊天室移除机器人
     */
    @DeleteMapping("/{roomId}/members/{memberId}")
    public ResponseEntity<EventResponse> removeMemberFromRoom(
            @PathVariable String roomId,
            @PathVariable String memberId) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限修改该聊天室"));
            }
            
            // 不能移除房主自己
            if (memberId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("不能移除房主"));
            }
            
            // 获取成员信息用于发送系统消息
            Optional<ChatRoomMember> memberOpt = memberService.getChatRoomMember(roomId, memberId);
            String memberNickname = memberOpt.map(ChatRoomMember::getMemberNickname).orElse("成员");
            
            boolean success = memberService.removeMemberFromChatRoom(roomId, memberId);
            if (success) {
                // 发送系统消息
                groupChatService.sendSystemMessage(roomId, memberNickname + " 离开了聊天室");
                return ResponseEntity.ok(EventResponse.success("", "移除成员成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("移除成员失败"));
            }
            
        } catch (Exception e) {
            logger.error("移除聊天室成员失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("移除成员失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送群聊消息
     */
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<EventResponse> sendMessage(
            @PathVariable String roomId,
            @RequestBody Map<String, Object> request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限在该聊天室发言"));
            }
            
            String content = (String) request.get("content");
            String imageUrl = (String) request.get("imageUrl");
            String replyToId = (String) request.get("replyToId");
            
            if ((content == null || content.trim().isEmpty()) && 
                (imageUrl == null || imageUrl.trim().isEmpty())) {
                return ResponseEntity.badRequest().body(EventResponse.error("消息内容不能为空"));
            }
            
            GroupChatMessage message = groupChatService.sendGroupMessage(
                roomId, userService.getUserById(userId).get(), "USER", userId, content, imageUrl, replyToId);
            
            // 触发机器人回复
            robotChatService.handleUserMessage(roomId, message);
            
            return ResponseEntity.ok(EventResponse.success(message, "消息发送成功"));
            
        } catch (Exception e) {
            logger.error("发送群聊消息失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("发送消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天历史（分页）
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<EventResponse> getChatHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限访问该聊天室消息"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<GroupChatMessage> messages = groupChatService.getChatHistory(roomId, pageable);
            
            return ResponseEntity.ok(EventResponse.success(messages, "获取聊天历史成功"));
            
        } catch (Exception e) {
            logger.error("获取聊天历史失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("获取聊天历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天室统计信息
     */
    @GetMapping("/{roomId}/stats")
    public ResponseEntity<EventResponse> getChatRoomStats(@PathVariable String roomId) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限访问该聊天室统计"));
            }
            
            Map<String, Object> stats = chatRoomService.getChatRoomStats(roomId);
            return ResponseEntity.ok(EventResponse.success(stats, "获取统计信息成功"));
            
        } catch (Exception e) {
            logger.error("获取聊天室统计失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 切换聊天室状态
     */
    @PutMapping("/{roomId}/status")
    public ResponseEntity<EventResponse> switchRoomStatus(
            @PathVariable String roomId,
            @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 检查权限
            if (!roomId.equals(userId)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无权限修改该聊天室状态"));
            }
            
            String status = request.get("status");
            if (!"active".equals(status) && !"inactive".equals(status)) {
                return ResponseEntity.badRequest().body(EventResponse.error("无效的状态值"));
            }
            
            boolean success = chatRoomService.switchChatRoomStatus(roomId, status);
            if (success) {
                // 根据状态调整机器人发言频率
                robotChatService.adjustChatFrequency(roomId);
                return ResponseEntity.ok(EventResponse.success(status, "状态切换成功"));
            } else {
                return ResponseEntity.badRequest().body(EventResponse.error("状态切换失败"));
            }
            
        } catch (Exception e) {
            logger.error("切换聊天室状态失败", e);
            return ResponseEntity.badRequest().body(EventResponse.error("状态切换失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户进入聊天室页面（触发高频模式）
     */
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<EventResponse> enterChatRoom(@PathVariable String roomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 标记用户进入聊天室
            chatRoomService.markUserEnterRoom(roomId, userId);
            
            // 更新心跳
            onlineUserService.updateHeartbeat(roomId, userId);
            
            int onlineCount = chatRoomService.getOnlineUserCount(roomId);
            logger.info("用户进入聊天室: roomId={}, userId={}, onlineCount={}", roomId, userId, onlineCount);
            
            return ResponseEntity.ok(EventResponse.success(Map.of(
                "onlineCount", onlineCount,
                "status", "entered"
            ), "进入聊天室成功"));
            
        } catch (Exception e) {
            logger.error("用户进入聊天室失败: roomId={}", roomId, e);
            return ResponseEntity.badRequest().body(EventResponse.error("进入聊天室失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户离开聊天室页面（可能触发低频模式）
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<EventResponse> leaveChatRoom(@PathVariable String roomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 标记用户离开聊天室
            chatRoomService.markUserLeaveRoom(roomId, userId);
            
            int onlineCount = chatRoomService.getOnlineUserCount(roomId);
            logger.info("用户离开聊天室: roomId={}, userId={}, onlineCount={}", roomId, userId, onlineCount);
            
            return ResponseEntity.ok(EventResponse.success(Map.of(
                "onlineCount", onlineCount,
                "status", "left"
            ), "离开聊天室成功"));
            
        } catch (Exception e) {
            logger.error("用户离开聊天室失败: roomId={}", roomId, e);
            return ResponseEntity.badRequest().body(EventResponse.error("离开聊天室失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送心跳保持在线状态
     */
    @PostMapping("/{roomId}/heartbeat")
    public ResponseEntity<EventResponse> sendHeartbeat(@PathVariable String roomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            // 更新心跳时间
            onlineUserService.updateHeartbeat(roomId, userId);
            
            int onlineCount = chatRoomService.getOnlineUserCount(roomId);
            
            return ResponseEntity.ok(EventResponse.success(Map.of(
                "onlineCount", onlineCount,
                "timestamp", System.currentTimeMillis()
            ), "心跳更新成功"));
            
        } catch (Exception e) {
            logger.error("心跳更新失败: roomId={}", roomId, e);
            return ResponseEntity.badRequest().body(EventResponse.error("心跳更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天室在线用户数
     */
    @GetMapping("/{roomId}/online-count")
    public ResponseEntity<EventResponse> getOnlineUserCount(@PathVariable String roomId) {
        try {
            int onlineCount = chatRoomService.getOnlineUserCount(roomId);
            
            return ResponseEntity.ok(EventResponse.success(Map.of(
                "onlineCount", onlineCount,
                "roomId", roomId
            ), "获取在线用户数成功"));
            
        } catch (Exception e) {
            logger.error("获取在线用户数失败: roomId={}", roomId, e);
            return ResponseEntity.badRequest().body(EventResponse.error("获取在线用户数失败: " + e.getMessage()));
        }
    }
}