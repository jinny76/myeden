package com.myeden.service;

import com.myeden.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 用户管理服务接口
 * 
 * 功能说明：
 * - 提供用户注册、登录、信息管理等核心业务功能
 * - 支持用户头像上传和个人资料更新
 * - 提供用户查询和统计功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param phone 手机号
     * @param password 密码
     * @return 注册结果，包含用户ID、昵称和JWT token
     */
    UserRegisterResult register(String phone, String password);
    
    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 登录结果，包含用户ID、JWT token和是否首次登录
     */
    UserLoginResult login(String phone, String password);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    Optional<User> getUserById(String userId);
    
    /**
     * 根据手机号获取用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> getUserByPhone(String phone);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(String userId, User user);
    
    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(String userId, MultipartFile file);
    
    /**
     * 完成首次登录
     * @param userId 用户ID
     */
    void completeFirstLogin(String userId);
    
    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 检查昵称是否已存在
     * @param nickname 昵称
     * @return 是否存在
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 根据昵称模糊查询用户
     * @param nickname 昵称关键词
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> searchUsersByNickname(String nickname, int limit);
    
    /**
     * 获取最近注册的用户
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> getRecentUsers(int limit);
    
    /**
     * 获取用户统计信息
     * @return 用户统计信息
     */
    UserStatistics getStatistics();
    
    /**
     * 获取用户个人统计信息
     * @param userId 用户ID
     * @return 用户个人统计信息
     */
    UserPersonalStatistics getUserPersonalStatistics(String userId);
    
    /**
     * 用户注册结果
     */
    class UserRegisterResult {
        private String userId;
        private String nickname;
        private String token;
        
        public UserRegisterResult(String userId, String nickname, String token) {
            this.userId = userId;
            this.nickname = nickname;
            this.token = token;
        }
        
        // Getter和Setter方法
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    /**
     * 用户登录结果
     */
    class UserLoginResult {
        private String userId;
        private String token;
        private Boolean isFirstLogin;
        private User user;
        
        public UserLoginResult(String userId, String token, Boolean isFirstLogin, User user) {
            this.userId = userId;
            this.token = token;
            this.isFirstLogin = isFirstLogin;
            this.user = user;
        }
        
        // Getter和Setter方法
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public Boolean getIsFirstLogin() { return isFirstLogin; }
        public void setIsFirstLogin(Boolean isFirstLogin) { this.isFirstLogin = isFirstLogin; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
    }
    
    /**
     * 用户统计信息
     */
    class UserStatistics {
        private Long totalUsers;
        private Long todayRegisteredUsers;
        private Long maleUsers;
        private Long femaleUsers;
        private Long firstLoginUsers;
        
        public UserStatistics(Long totalUsers, Long todayRegisteredUsers, Long maleUsers, Long femaleUsers, Long firstLoginUsers) {
            this.totalUsers = totalUsers;
            this.todayRegisteredUsers = todayRegisteredUsers;
            this.maleUsers = maleUsers;
            this.femaleUsers = femaleUsers;
            this.firstLoginUsers = firstLoginUsers;
        }
        
        // Getter和Setter方法
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getTodayRegisteredUsers() { return todayRegisteredUsers; }
        public void setTodayRegisteredUsers(Long todayRegisteredUsers) { this.todayRegisteredUsers = todayRegisteredUsers; }
        
        public Long getMaleUsers() { return maleUsers; }
        public void setMaleUsers(Long maleUsers) { this.maleUsers = maleUsers; }
        
        public Long getFemaleUsers() { return femaleUsers; }
        public void setFemaleUsers(Long femaleUsers) { this.femaleUsers = femaleUsers; }
        
        public Long getFirstLoginUsers() { return firstLoginUsers; }
        public void setFirstLoginUsers(Long firstLoginUsers) { this.firstLoginUsers = firstLoginUsers; }
    }
    
    /**
     * 用户个人统计信息
     */
    class UserPersonalStatistics {
        private String userId;
        private String nickname;
        private String avatar;
        private Long registrationDays; // 注册天数
        private Long totalPosts; // 发帖数
        private Long totalComments; // 评论数
        private Long totalLikes; // 获得的点赞数
        private Long totalPostLikes; // 动态获得的点赞数
        private Long totalCommentLikes; // 评论获得的点赞数
        private Long todayPosts; // 今日发帖数
        private Long todayComments; // 今日评论数
        private String lastActiveTime; // 最后活跃时间
        
        public UserPersonalStatistics(String userId, String nickname, String avatar, 
                                    Long registrationDays, Long totalPosts, Long totalComments,
                                    Long totalLikes, Long totalPostLikes, Long totalCommentLikes,
                                    Long todayPosts, Long todayComments, String lastActiveTime) {
            this.userId = userId;
            this.nickname = nickname;
            this.avatar = avatar;
            this.registrationDays = registrationDays;
            this.totalPosts = totalPosts;
            this.totalComments = totalComments;
            this.totalLikes = totalLikes;
            this.totalPostLikes = totalPostLikes;
            this.totalCommentLikes = totalCommentLikes;
            this.todayPosts = todayPosts;
            this.todayComments = todayComments;
            this.lastActiveTime = lastActiveTime;
        }
        
        // Getter和Setter方法
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        
        public Long getRegistrationDays() { return registrationDays; }
        public void setRegistrationDays(Long registrationDays) { this.registrationDays = registrationDays; }
        
        public Long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(Long totalPosts) { this.totalPosts = totalPosts; }
        
        public Long getTotalComments() { return totalComments; }
        public void setTotalComments(Long totalComments) { this.totalComments = totalComments; }
        
        public Long getTotalLikes() { return totalLikes; }
        public void setTotalLikes(Long totalLikes) { this.totalLikes = totalLikes; }
        
        public Long getTotalPostLikes() { return totalPostLikes; }
        public void setTotalPostLikes(Long totalPostLikes) { this.totalPostLikes = totalPostLikes; }
        
        public Long getTotalCommentLikes() { return totalCommentLikes; }
        public void setTotalCommentLikes(Long totalCommentLikes) { this.totalCommentLikes = totalCommentLikes; }
        
        public Long getTodayPosts() { return todayPosts; }
        public void setTodayPosts(Long todayPosts) { this.todayPosts = todayPosts; }
        
        public Long getTodayComments() { return todayComments; }
        public void setTodayComments(Long todayComments) { this.todayComments = todayComments; }
        
        public String getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(String lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    }
} 