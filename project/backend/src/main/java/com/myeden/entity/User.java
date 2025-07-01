package com.myeden.entity;

import com.myeden.constant.UserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 用户实体类
 * 
 * 功能说明：
 * - 存储用户的基本信息和配置
 * - 管理用户的个人资料和偏好设置
 * - 记录用户的登录状态和统计信息
 * - 支持用户个性化设定
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    /**
     * 用户ID，唯一
     */
    @Indexed(unique = true)
    private String userId;
    
    /**
     * 手机号，唯一
     */
    @Indexed(unique = true)
    private String phone;
    
    /**
     * 加密密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 称呼
     */
    private String title;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 生日
     */
    private LocalDate birthday;
    
    /**
     * 一句话介绍
     */
    private String introduction;
    
    /**
     * 背景
     */
    private String background;
    
    /**
     * 血型
     */
    private String bloodType;
    
    /**
     * MBTI
     */
    private String mbti;
    
    /**
     * 喜欢颜色
     */
    private String favoriteColor;
    
    /**
     * 喜欢的东西
     */
    private List<String> likes = new ArrayList<>();
    
    /**
     * 不喜欢的东西
     */
    private List<String> dislikes = new ArrayList<>();
    
    /**
     * 用户角色
     * 可选值：user（普通用户）、moderator（版主）、admin（管理员）、super_admin（超级管理员）
     * 默认为user
     */
    private String role = UserRole.USER;
    
    /**
     * 是否首次登录
     */
    private Boolean isFirstLogin = true;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.role = UserRole.USER; // 确保角色字段被初始化
    }
    
    public User(String userId, String phone, String password) {
        this();
        this.userId = userId;
        this.phone = phone;
        this.password = password;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public String getIntroduction() {
        return introduction;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    public String getBackground() {
        return background;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getMbti() {
        return mbti;
    }
    
    public void setMbti(String mbti) {
        this.mbti = mbti;
    }
    
    public String getFavoriteColor() {
        return favoriteColor;
    }
    
    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }
    
    public List<String> getLikes() {
        return likes;
    }
    
    public void setLikes(List<String> likes) {
        this.likes = likes;
    }
    
    public List<String> getDislikes() {
        return dislikes;
    }
    
    public void setDislikes(List<String> dislikes) {
        this.dislikes = dislikes;
    }
    
    public String getRole() {
        // 如果角色为空，返回默认用户角色
        return role != null ? role : UserRole.USER;
    }
    
    public Boolean getIsFirstLogin() {
        return isFirstLogin;
    }
    
    public void setIsFirstLogin(Boolean isFirstLogin) {
        this.isFirstLogin = isFirstLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 添加喜欢的东西
     */
    public void addLike(String like) {
        if (!this.likes.contains(like)) {
            this.likes.add(like);
        }
    }
    
    /**
     * 添加不喜欢的东西
     */
    public void addDislike(String dislike) {
        if (!this.dislikes.contains(dislike)) {
            this.dislikes.add(dislike);
        }
    }
    
    /**
     * 移除喜欢的东西
     */
    public void removeLike(String like) {
        this.likes.remove(like);
    }
    
    /**
     * 移除不喜欢的东西
     */
    public void removeDislike(String dislike) {
        this.dislikes.remove(dislike);
    }
    
    /**
     * 更新用户信息（只更新非空属性）
     */
    public void updateUser(User user) {
        if (user.getNickname() != null) {
            this.nickname = user.getNickname();
        }
        // 头像不在这里更新，由专门的头像上传接口处理
        // if (user.getAvatar() != null) {
        //     this.avatar = user.getAvatar();
        // }
        if (user.getTitle() != null) {
            this.title = user.getTitle();
        }
        if (user.getGender() != null) {
            this.gender = user.getGender();
        }
        if (user.getAge() != null) {
            this.age = user.getAge();
        }
        if (user.getBirthday() != null) {
            this.birthday = user.getBirthday();
        }
        if (user.getIntroduction() != null) {
            this.introduction = user.getIntroduction();
        }
        if (user.getBackground() != null) {
            this.background = user.getBackground();
        }
        if (user.getBloodType() != null) {
            this.bloodType = user.getBloodType();
        }
        if (user.getMbti() != null) {
            this.mbti = user.getMbti();
        }
        if (user.getFavoriteColor() != null) {
            this.favoriteColor = user.getFavoriteColor();
        }
        if (user.getLikes() != null && !user.getLikes().isEmpty()) {
            this.likes = user.getLikes();
        }
        if (user.getDislikes() != null && !user.getDislikes().isEmpty()) {
            this.dislikes = user.getDislikes();
        }
        if (user.getRole() != null) {
            this.role = user.getRole();
        } else if (this.role == null) {
            // 如果当前用户角色为空，设置为默认用户角色
            this.role = UserRole.USER;
        }
        if (user.getIsFirstLogin() != null) {
            this.isFirstLogin = user.getIsFirstLogin();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 完成首次登录
     */
    public void completeFirstLogin() {
        this.isFirstLogin = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.getRole());
    }
    
    /**
     * 检查是否为版主
     */
    public boolean isModerator() {
        return UserRole.MODERATOR.equals(this.getRole());
    }
    
    /**
     * 检查是否为普通用户
     */
    public boolean isUser() {
        return UserRole.USER.equals(this.getRole());
    }
    
    /**
     * 检查是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return UserRole.SUPER_ADMIN.equals(this.getRole());
    }
    
    /**
     * 检查是否有管理权限（管理员、版主或超级管理员）
     */
    public boolean hasAdminPermission() {
        return isAdmin() || isModerator() || isSuperAdmin();
    }
    
    /**
     * 检查是否有超级管理员权限
     */
    public boolean hasSuperAdminPermission() {
        return isSuperAdmin();
    }
    
    /**
     * 升级为超级管理员
     */
    public void promoteToSuperAdmin() {
        this.role = UserRole.SUPER_ADMIN;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 升级为管理员
     */
    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 升级为版主
     */
    public void promoteToModerator() {
        this.role = UserRole.MODERATOR;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 降级为普通用户
     */
    public void demoteToUser() {
        this.role = UserRole.USER;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置角色（带验证）
     */
    public void setRole(String role) {
        if (UserRole.isValidRole(role)) {
            this.role = role;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("无效的角色: " + role);
        }
    }
    
    /**
     * 获取角色显示名称
     */
    public String getRoleDisplayName() {
        return UserRole.getRoleDisplayName(this.getRole());
    }
    
    /**
     * 获取角色描述
     */
    public String getRoleDescription() {
        return UserRole.getRoleDescription(this.getRole());
    }
    
    /**
     * 检查是否有足够权限执行某个操作
     */
    public boolean hasPermission(String requiredRole) {
        return UserRole.hasPermission(this.getRole(), requiredRole);
    }
    
    /**
     * 确保角色字段有效，如果为空则设置为默认用户角色
     */
    public void ensureValidRole() {
        if (this.role == null || !UserRole.isValidRole(this.role)) {
            this.role = UserRole.USER;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 初始化用户角色（用于从数据库加载时的数据修复）
     */
    public void initializeRole() {
        if (this.role == null) {
            this.role = UserRole.USER;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender='" + gender + '\'' +
                ", role='" + getRole() + '\'' +
                ", isFirstLogin=" + isFirstLogin +
                ", createdAt=" + createdAt +
                '}';
    }
} 