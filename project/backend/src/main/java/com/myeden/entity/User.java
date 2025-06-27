package com.myeden.entity;

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
     * 更新用户信息
     */
    public void updateUser(User user) {
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.title = user.getTitle();
        this.gender = user.getGender();
        this.age = user.getAge();
        this.birthday = user.getBirthday();
        this.introduction = user.getIntroduction();
        this.background = user.getBackground();
        this.bloodType = user.getBloodType();
        this.mbti = user.getMbti();
        this.favoriteColor = user.getFavoriteColor();
        this.likes = user.getLikes();
        this.dislikes = user.getDislikes();
        this.isFirstLogin = user.getIsFirstLogin();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 完成首次登录
     */
    public void completeFirstLogin() {
        this.isFirstLogin = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender='" + gender + '\'' +
                ", isFirstLogin=" + isFirstLogin +
                ", createdAt=" + createdAt +
                '}';
    }
} 