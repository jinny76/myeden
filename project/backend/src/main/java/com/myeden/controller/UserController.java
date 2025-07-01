package com.myeden.controller;

import com.myeden.entity.User;
import com.myeden.service.JwtService;
import com.myeden.service.UserService;
import com.myeden.controller.EventResponse;
import com.myeden.config.RequireAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * 功能说明：
 * - 提供用户注册、登录、信息管理等REST API接口
 * - 支持用户头像上传和个人资料更新
 * - 提供用户查询和统计功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * 用户注册
     * POST /api/v1/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<EventResponse> register(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String password = request.get("password");
            
            // 参数验证
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error(400, "手机号不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error(400, "密码不能为空"));
            }
            
            // 执行注册
            UserService.UserRegisterResult result = userService.register(phone, password);
            
            return ResponseEntity.ok(EventResponse.success(result, "注册成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     * POST /api/v1/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<EventResponse> login(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String password = request.get("password");
            
            // 参数验证
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error(400, "手机号不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EventResponse.error(400, "密码不能为空"));
            }
            
            // 执行登录
            UserService.UserLoginResult result = userService.login(phone, password);
            
            return ResponseEntity.ok(EventResponse.success(result, "登录成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<EventResponse> getCurrentUserInfo(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取用户ID
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取用户信息
            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "用户不存在"));
            }
            
            User user = userOpt.get();
            return ResponseEntity.ok(EventResponse.success(user, "获取当前用户信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户信息
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<EventResponse> getUserInfo(@PathVariable String userId) {
        try {
            var userOpt = userService.getUserById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(EventResponse.error(404, "用户不存在"));
            }
            
            User user = userOpt.get();
            
            return ResponseEntity.ok(EventResponse.success(user, "获取用户信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新用户信息
     * PUT /api/v1/users/{userId}
     * 
     * 安全要求：
     * - 需要JWT认证
     * - 只能更新自己的用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<EventResponse> updateUser(@PathVariable String userId, @RequestBody User userUpdate, HttpServletRequest request) {
        try {
            // 从请求头获取token进行身份验证
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取当前用户ID
            String currentUserId = jwtService.extractUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 授权检查：确保用户只能更新自己的信息
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "只能更新自己的用户信息"));
            }
            
            // 执行用户信息更新
            User updatedUser = userService.updateUser(userId, userUpdate);
            
            return ResponseEntity.ok(EventResponse.success(updatedUser, "更新用户信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 上传用户头像
     * POST /api/v1/users/{userId}/avatar
     * 
     * 安全要求：
     * - 需要JWT认证
     * - 只能上传自己的头像
     */
    @PostMapping("/{userId}/avatar")
    public ResponseEntity<EventResponse> uploadAvatar(@PathVariable String userId, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 从请求头获取token进行身份验证
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取当前用户ID
            String currentUserId = jwtService.extractUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 授权检查：确保用户只能上传自己的头像
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "只能上传自己的头像"));
            }
            
            // 执行头像上传
            String avatarUrl = userService.uploadAvatar(userId, file);
            
            return ResponseEntity.ok(EventResponse.success(Map.of("avatarUrl", avatarUrl), "头像上传成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 完成首次登录
     * POST /api/v1/users/{userId}/complete-first-login
     * 
     * 安全要求：
     * - 需要JWT认证
     * - 只能完成自己的首次登录
     */
    @PostMapping("/{userId}/complete-first-login")
    public ResponseEntity<EventResponse> completeFirstLogin(@PathVariable String userId, HttpServletRequest request) {
        try {
            // 从请求头获取token进行身份验证
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取当前用户ID
            String currentUserId = jwtService.extractUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 授权检查：确保用户只能完成自己的首次登录
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "只能完成自己的首次登录"));
            }
            
            // 执行首次登录完成
            userService.completeFirstLogin(userId);
            
            return ResponseEntity.ok(EventResponse.success(null, "首次登录完成"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查手机号是否存在
     * GET /api/v1/users/check-phone?phone={phone}
     */
    @GetMapping("/check-phone")
    public ResponseEntity<EventResponse> checkPhone(@RequestParam String phone) {
        try {
            boolean exists = userService.existsByPhone(phone);
            
            return ResponseEntity.ok(EventResponse.success(Map.of("exists", exists), "手机号检查成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查昵称是否存在
     * GET /api/v1/users/check-nickname?nickname={nickname}
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<EventResponse> checkNickname(@RequestParam String nickname) {
        try {
            boolean exists = userService.existsByNickname(nickname);
            
            return ResponseEntity.ok(EventResponse.success(Map.of("exists", exists), "昵称检查成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 搜索用户
     * GET /api/v1/users/search?nickname={nickname}&limit={limit}
     */
    @GetMapping("/search")
    public ResponseEntity<EventResponse> searchUsers(@RequestParam String nickname, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<User> users = userService.searchUsersByNickname(nickname, limit);
            
            return ResponseEntity.ok(EventResponse.success(users, "搜索用户成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取最近注册的用户
     * GET /api/v1/users/recent?limit={limit}
     */
    @GetMapping("/recent")
    public ResponseEntity<EventResponse> getRecentUsers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<User> users = userService.getRecentUsers(limit);
            
            return ResponseEntity.ok(EventResponse.success(users, "获取最近用户成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户统计信息
     * GET /api/v1/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<EventResponse> getStatistics() {
        try {
            UserService.UserStatistics statistics = userService.getStatistics();
            
            return ResponseEntity.ok(EventResponse.success(statistics, "获取统计信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户个人统计信息
     * GET /api/v1/users/{userId}/statistics
     * 
     * 功能说明：
     * - 返回指定用户的个人统计信息
     * - 包括注册天数、发帖数、评论数、获得的点赞数等
     * - 支持获取自己的统计信息或其他用户的统计信息
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<EventResponse> getUserPersonalStatistics(@PathVariable String userId) {
        try {
            UserService.UserPersonalStatistics statistics = userService.getUserPersonalStatistics(userId);
            
            return ResponseEntity.ok(EventResponse.success(statistics, "获取用户个人统计信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户个人统计信息
     * GET /api/v1/users/me/statistics
     * 
     * 功能说明：
     * - 返回当前登录用户的个人统计信息
     * - 需要JWT认证
     * - 包括注册天数、发帖数、评论数、获得的点赞数等
     */
    @GetMapping("/me/statistics")
    public ResponseEntity<EventResponse> getCurrentUserPersonalStatistics(HttpServletRequest request) {
        try {
            // 从请求头获取token进行身份验证
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取当前用户ID
            String currentUserId = jwtService.extractUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(EventResponse.error(401, "Token无效"));
            }
            
            // 获取当前用户的个人统计信息
            UserService.UserPersonalStatistics statistics = userService.getUserPersonalStatistics(currentUserId);
            
            return ResponseEntity.ok(EventResponse.success(statistics, "获取当前用户个人统计信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()));
        }
    }
} 