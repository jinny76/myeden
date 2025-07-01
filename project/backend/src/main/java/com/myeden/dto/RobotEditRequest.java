package com.myeden.dto;

import com.myeden.entity.Robot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.ArrayList;

/**
 * 机器人编辑请求 DTO
 * 
 * 功能说明：
 * - 包含机器人所有可编辑的字段
 * - 用于机器人整体编辑和保存
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Schema(description = "机器人编辑请求")
public class RobotEditRequest {
    
    @Schema(description = "机器人名称", example = "小艾")
    private String name;
    
    @Schema(description = "机器人头像URL", example = "/avatars/robot_001.png")
    private String avatar;
    
    @Schema(description = "机器人性别", example = "female")
    private String gender;
    
    @Schema(description = "机器人年龄", example = "25")
    private Integer age;
    
    @Schema(description = "一句话简介", example = "一个温柔细心的咖啡师")
    private String introduction;
    
    @Schema(description = "性格设定", example = "温柔细心，喜欢照顾别人")
    private String personality;
    
    @Schema(description = "MBTI性格类型", example = "ISFJ")
    private String mbti;
    
    @Schema(description = "血型", example = "A")
    private String bloodType;
    
    @Schema(description = "星座", example = "处女座")
    private String zodiac;
    
    @Schema(description = "所在地", example = "北京")
    private String location;
    
    @Schema(description = "职业", example = "咖啡师")
    private String occupation;
    
    @Schema(description = "背景故事", example = "从小在咖啡店长大...")
    private String background;
    
    @Schema(description = "学历", example = "本科")
    private String education;
    
    @Schema(description = "感情状态", example = "single")
    private String relationship;
    
    @Schema(description = "家庭背景", example = "独生子女，父母都是教师")
    private String family;
    
    @Schema(description = "性格特征列表")
    private List<String> traits;
    
    @Schema(description = "兴趣爱好列表")
    private List<String> interests;
    
    @Schema(description = "说话风格")
    private Robot.SpeakingStyle speakingStyle;
    
    @Schema(description = "行为模式")
    private Robot.BehaviorPatterns behaviorPatterns;
    
    @Schema(description = "回复速度（1-10）", example = "7")
    private Integer replySpeed;
    
    @Schema(description = "回复频度（1-10）", example = "8")
    private Integer replyFrequency;
    
    @Schema(description = "分享频度（1-10）", example = "6")
    private Integer shareFrequency;
    
    @Schema(description = "活跃时间段列表")
    private List<Robot.ActiveTimeRange> activeTimeRanges;
    
    @Schema(description = "个人主题列表")
    private List<Robot.Topic> topics;
    
    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    
    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }
    
    public String getMbti() { return mbti; }
    public void setMbti(String mbti) { this.mbti = mbti; }
    
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    
    public String getZodiac() { return zodiac; }
    public void setZodiac(String zodiac) { this.zodiac = zodiac; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    
    public List<String> getTraits() { return traits; }
    public void setTraits(List<String> traits) { this.traits = traits; }
    
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    
    public Robot.SpeakingStyle getSpeakingStyle() { return speakingStyle; }
    public void setSpeakingStyle(Robot.SpeakingStyle speakingStyle) { this.speakingStyle = speakingStyle; }
    
    public Robot.BehaviorPatterns getBehaviorPatterns() { return behaviorPatterns; }
    public void setBehaviorPatterns(Robot.BehaviorPatterns behaviorPatterns) { this.behaviorPatterns = behaviorPatterns; }
    
    public Integer getReplySpeed() { return replySpeed; }
    public void setReplySpeed(Integer replySpeed) { this.replySpeed = replySpeed; }
    
    public Integer getReplyFrequency() { return replyFrequency; }
    public void setReplyFrequency(Integer replyFrequency) { this.replyFrequency = replyFrequency; }
    
    public Integer getShareFrequency() { return shareFrequency; }
    public void setShareFrequency(Integer shareFrequency) { this.shareFrequency = shareFrequency; }
    
    public List<Robot.ActiveTimeRange> getActiveTimeRanges() { return activeTimeRanges; }
    public void setActiveTimeRanges(List<Robot.ActiveTimeRange> activeTimeRanges) { this.activeTimeRanges = activeTimeRanges; }
    
    public List<Robot.Topic> getTopics() { return topics; }
    public void setTopics(List<Robot.Topic> topics) { this.topics = topics; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    /**
     * 将请求转换为Robot实体
     */
    public Robot toRobot(String robotId, String owner) {
        Robot robot = new Robot(robotId, name, owner);
        robot.setAvatar(avatar);
        robot.setGender(gender);
        robot.setAge(age);
        robot.setIntroduction(introduction);
        robot.setPersonality(personality);
        robot.setMbti(mbti);
        robot.setBloodType(bloodType);
        robot.setZodiac(zodiac);
        robot.setLocation(location);
        robot.setOccupation(occupation);
        robot.setBackground(background);
        robot.setEducation(education);
        robot.setRelationship(relationship);
        robot.setFamily(family);
        robot.setTraits(traits != null ? traits : new ArrayList<>());
        robot.setInterests(interests != null ? interests : new ArrayList<>());
        robot.setSpeakingStyle(speakingStyle);
        robot.setBehaviorPatterns(behaviorPatterns);
        robot.setReplySpeed(replySpeed);
        robot.setReplyFrequency(replyFrequency);
        robot.setShareFrequency(shareFrequency);
        robot.setActiveTimeRanges(activeTimeRanges != null ? activeTimeRanges : new ArrayList<>());
        robot.setTopics(topics != null ? topics : new ArrayList<>());
        robot.setIsActive(isActive);
        return robot;
    }
    
    /**
     * 更新现有Robot实体的字段
     */
    public void updateRobot(Robot robot) {
        robot.setName(name);
        robot.setAvatar(avatar);
        robot.setGender(gender);
        robot.setAge(age);
        robot.setIntroduction(introduction);
        robot.setPersonality(personality);
        robot.setMbti(mbti);
        robot.setBloodType(bloodType);
        robot.setZodiac(zodiac);
        robot.setLocation(location);
        robot.setOccupation(occupation);
        robot.setBackground(background);
        robot.setEducation(education);
        robot.setRelationship(relationship);
        robot.setFamily(family);
        robot.setTraits(traits != null ? traits : new ArrayList<>());
        robot.setInterests(interests != null ? interests : new ArrayList<>());
        robot.setSpeakingStyle(speakingStyle);
        robot.setBehaviorPatterns(behaviorPatterns);
        robot.setReplySpeed(replySpeed);
        robot.setReplyFrequency(replyFrequency);
        robot.setShareFrequency(shareFrequency);
        robot.setActiveTimeRanges(activeTimeRanges != null ? activeTimeRanges : new ArrayList<>());
        robot.setTopics(topics != null ? topics : new ArrayList<>());
        robot.setIsActive(isActive);
        robot.setUpdatedAt(java.time.LocalDateTime.now());
    }
} 