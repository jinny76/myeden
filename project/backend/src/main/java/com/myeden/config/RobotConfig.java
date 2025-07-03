package com.myeden.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 机器人配置类
 * 
 * 功能说明：
 * - 映射robots-config.yaml配置文件的结构
 * - 提供机器人基本设定、性格特征和行为模式
 * - 支持配置热更新和动态加载
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "robots")
public class RobotConfig {
    
    private BaseConfig baseConfig;
    private List<RobotInfo> list;
    private BehaviorAlgorithm behaviorAlgorithm;
    
    // Getter和Setter方法
    public BaseConfig getBaseConfig() { return baseConfig; }
    public void setBaseConfig(BaseConfig baseConfig) { this.baseConfig = baseConfig; }
    
    public List<RobotInfo> getList() { return list; }
    public void setList(List<RobotInfo> list) { this.list = list; }
    
    public BehaviorAlgorithm getBehaviorAlgorithm() { return behaviorAlgorithm; }
    public void setBehaviorAlgorithm(BehaviorAlgorithm behaviorAlgorithm) { this.behaviorAlgorithm = behaviorAlgorithm; }
    
    /**
     * 机器人基础配置
     */
    public static class BaseConfig {
        private int maxDailyPosts;
        private int maxDailyComments;
        private int maxDailyReplies;
        private List<Topic> commonTopic;
        
        public int getMaxDailyPosts() { return maxDailyPosts; }
        public void setMaxDailyPosts(int maxDailyPosts) { this.maxDailyPosts = maxDailyPosts; }
        
        public int getMaxDailyComments() { return maxDailyComments; }
        public void setMaxDailyComments(int maxDailyComments) { this.maxDailyComments = maxDailyComments; }
        
        public int getMaxDailyReplies() { return maxDailyReplies; }
        public void setMaxDailyReplies(int maxDailyReplies) { this.maxDailyReplies = maxDailyReplies; }
        
        public List<Topic> getCommonTopic() { return commonTopic; }
        public void setCommonTopic(List<Topic> commonTopic) { this.commonTopic = commonTopic; }
    }
    
    /**
     * 机器人信息配置
     */
    public static class RobotInfo {
        private String id;
        private String name;
        private String nickname;
        private String avatar;
        private String personality;
        private String description;
        private String background;
        private String example;  // 新增：机器人发帖示例
        
        // 个人基本信息
        private String gender;           // 性别：male/female/other
        private int age;                 // 年龄
        private String mbti;             // MBTI性格类型
        private String bloodType;        // 血型：A/B/O/AB
        private String zodiac;           // 星座
        private String occupation;       // 职业
        private String location;         // 所在地
        private String education;        // 学历
        private String relationship;     // 感情状态：single/married/in_relationship
        private String family;           // 家庭背景
        
        private List<String> traits;
        private List<String> interests;
        private SpeakingStyle speakingStyle;
        private BehaviorPatterns behaviorPatterns;
        private List<Topic> topic;       // 个人主题
        private List<ActiveHours> activeHours;
        private boolean isActive;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        
        public String getPersonality() { return personality; }
        public void setPersonality(String personality) { this.personality = personality; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getBackground() { return background; }
        public void setBackground(String background) { this.background = background; }
        
        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
        
        // 个人基本信息的Getter和Setter
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public String getMbti() { return mbti; }
        public void setMbti(String mbti) { this.mbti = mbti; }
        
        public String getBloodType() { return bloodType; }
        public void setBloodType(String bloodType) { this.bloodType = bloodType; }
        
        public String getZodiac() { return zodiac; }
        public void setZodiac(String zodiac) { this.zodiac = zodiac; }
        
        public String getOccupation() { return occupation; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
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
        
        public SpeakingStyle getSpeakingStyle() { return speakingStyle; }
        public void setSpeakingStyle(SpeakingStyle speakingStyle) { this.speakingStyle = speakingStyle; }
        
        public BehaviorPatterns getBehaviorPatterns() { return behaviorPatterns; }
        public void setBehaviorPatterns(BehaviorPatterns behaviorPatterns) { this.behaviorPatterns = behaviorPatterns; }
        
        public List<Topic> getTopic() { return topic; }
        public void setTopic(List<Topic> topic) { this.topic = topic; }
        
        public List<ActiveHours> getActiveHours() { return activeHours; }
        public void setActiveHours(List<ActiveHours> activeHours) { this.activeHours = activeHours; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }
    
    /**
     * 个人主题配置
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Topic {
        private String name;
        private int frequency;
        private String content;
        private String dataType;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getFrequency() { return frequency; }
        public void setFrequency(int frequency) { this.frequency = frequency; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }
    
    /**
     * 说话风格配置
     */
    public static class SpeakingStyle {
        private String tone;
        private String vocabulary;
        private String emojiUsage;
        private String sentenceLength;
        private List<String> favoriteWords;
        private List<String> speechPatterns;
        
        public String getTone() { return tone; }
        public void setTone(String tone) { this.tone = tone; }
        
        public String getVocabulary() { return vocabulary; }
        public void setVocabulary(String vocabulary) { this.vocabulary = vocabulary; }
        
        public String getEmojiUsage() { return emojiUsage; }
        public void setEmojiUsage(String emojiUsage) { this.emojiUsage = emojiUsage; }
        
        public String getSentenceLength() { return sentenceLength; }
        public void setSentenceLength(String sentenceLength) { this.sentenceLength = sentenceLength; }
        
        public List<String> getFavoriteWords() { return favoriteWords; }
        public void setFavoriteWords(List<String> favoriteWords) { this.favoriteWords = favoriteWords; }
        
        public List<String> getSpeechPatterns() { return speechPatterns; }
        public void setSpeechPatterns(List<String> speechPatterns) { this.speechPatterns = speechPatterns; }
    }
    
    /**
     * 行为模式配置
     */
    public static class BehaviorPatterns {
        private double greetingFrequency;
        private double comfortFrequency;
        private double shareFrequency;
        private double commentFrequency;
        private double replyFrequency;
        private double moodSwings;
        private double socialEnergy;
        
        public double getGreetingFrequency() { return greetingFrequency; }
        public void setGreetingFrequency(double greetingFrequency) { this.greetingFrequency = greetingFrequency; }
        
        public double getComfortFrequency() { return comfortFrequency; }
        public void setComfortFrequency(double comfortFrequency) { this.comfortFrequency = comfortFrequency; }
        
        public double getShareFrequency() { return shareFrequency; }
        public void setShareFrequency(double shareFrequency) { this.shareFrequency = shareFrequency; }
        
        public double getCommentFrequency() { return commentFrequency; }
        public void setCommentFrequency(double commentFrequency) { this.commentFrequency = commentFrequency; }
        
        public double getReplyFrequency() { return replyFrequency; }
        public void setReplyFrequency(double replyFrequency) { this.replyFrequency = replyFrequency; }
        
        public double getMoodSwings() { return moodSwings; }
        public void setMoodSwings(double moodSwings) { this.moodSwings = moodSwings; }
        
        public double getSocialEnergy() { return socialEnergy; }
        public void setSocialEnergy(double socialEnergy) { this.socialEnergy = socialEnergy; }
    }
    
    /**
     * 活跃时间段配置
     */
    public static class ActiveHours {
        private String start;
        private String end;
        private double probability;
        
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }
    
    /**
     * 行为算法配置
     */
    public static class BehaviorAlgorithm {
        private PostProbability postProbability;
        private CommentProbability commentProbability;
        private ReplyProbability replyProbability;
        private ContentGeneration contentGeneration;
        private TimeControl timeControl;
        private EmotionSystem emotionSystem;
        private SocialRelationship socialRelationship;
        
        public PostProbability getPostProbability() { return postProbability; }
        public void setPostProbability(PostProbability postProbability) { this.postProbability = postProbability; }
        
        public CommentProbability getCommentProbability() { return commentProbability; }
        public void setCommentProbability(CommentProbability commentProbability) { this.commentProbability = commentProbability; }
        
        public ReplyProbability getReplyProbability() { return replyProbability; }
        public void setReplyProbability(ReplyProbability replyProbability) { this.replyProbability = replyProbability; }
        
        public ContentGeneration getContentGeneration() { return contentGeneration; }
        public void setContentGeneration(ContentGeneration contentGeneration) { this.contentGeneration = contentGeneration; }
        
        public TimeControl getTimeControl() { return timeControl; }
        public void setTimeControl(TimeControl timeControl) { this.timeControl = timeControl; }
        
        public EmotionSystem getEmotionSystem() { return emotionSystem; }
        public void setEmotionSystem(EmotionSystem emotionSystem) { this.emotionSystem = emotionSystem; }
        
        public SocialRelationship getSocialRelationship() { return socialRelationship; }
        public void setSocialRelationship(SocialRelationship socialRelationship) { this.socialRelationship = socialRelationship; }
    }
    
    /**
     * 动态发布概率配置
     */
    public static class PostProbability {
        private double baseRate;
        private double timeMultiplier;
        private double userInteractionMultiplier;
        private double contentQualityMultiplier;
        private double moodMultiplier;
        private double randomFactor;
        
        public double getBaseRate() { return baseRate; }
        public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
        
        public double getTimeMultiplier() { return timeMultiplier; }
        public void setTimeMultiplier(double timeMultiplier) { this.timeMultiplier = timeMultiplier; }
        
        public double getUserInteractionMultiplier() { return userInteractionMultiplier; }
        public void setUserInteractionMultiplier(double userInteractionMultiplier) { this.userInteractionMultiplier = userInteractionMultiplier; }
        
        public double getContentQualityMultiplier() { return contentQualityMultiplier; }
        public void setContentQualityMultiplier(double contentQualityMultiplier) { this.contentQualityMultiplier = contentQualityMultiplier; }
        
        public double getMoodMultiplier() { return moodMultiplier; }
        public void setMoodMultiplier(double moodMultiplier) { this.moodMultiplier = moodMultiplier; }
        
        public double getRandomFactor() { return randomFactor; }
        public void setRandomFactor(double randomFactor) { this.randomFactor = randomFactor; }
    }
    
    /**
     * 评论发布概率配置
     */
    public static class CommentProbability {
        private double baseRate;
        private double postRelevanceMultiplier;
        private double userRelationshipMultiplier;
        private double timeSincePostMultiplier;
        private double interestMatchMultiplier;
        private double randomFactor;
        
        public double getBaseRate() { return baseRate; }
        public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
        
        public double getPostRelevanceMultiplier() { return postRelevanceMultiplier; }
        public void setPostRelevanceMultiplier(double postRelevanceMultiplier) { this.postRelevanceMultiplier = postRelevanceMultiplier; }
        
        public double getUserRelationshipMultiplier() { return userRelationshipMultiplier; }
        public void setUserRelationshipMultiplier(double userRelationshipMultiplier) { this.userRelationshipMultiplier = userRelationshipMultiplier; }
        
        public double getTimeSincePostMultiplier() { return timeSincePostMultiplier; }
        public void setTimeSincePostMultiplier(double timeSincePostMultiplier) { this.timeSincePostMultiplier = timeSincePostMultiplier; }
        
        public double getInterestMatchMultiplier() { return interestMatchMultiplier; }
        public void setInterestMatchMultiplier(double interestMatchMultiplier) { this.interestMatchMultiplier = interestMatchMultiplier; }
        
        public double getRandomFactor() { return randomFactor; }
        public void setRandomFactor(double randomFactor) { this.randomFactor = randomFactor; }
    }
    
    /**
     * 回复发布概率配置
     */
    public static class ReplyProbability {
        private double baseRate;
        private double commentRelevanceMultiplier;
        private double userRelationshipMultiplier;
        private double conversationDepthMultiplier;
        private double socialEnergyMultiplier;
        private double randomFactor;
        
        public double getBaseRate() { return baseRate; }
        public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
        
        public double getCommentRelevanceMultiplier() { return commentRelevanceMultiplier; }
        public void setCommentRelevanceMultiplier(double commentRelevanceMultiplier) { this.commentRelevanceMultiplier = commentRelevanceMultiplier; }
        
        public double getUserRelationshipMultiplier() { return userRelationshipMultiplier; }
        public void setUserRelationshipMultiplier(double userRelationshipMultiplier) { this.userRelationshipMultiplier = userRelationshipMultiplier; }
        
        public double getConversationDepthMultiplier() { return conversationDepthMultiplier; }
        public void setConversationDepthMultiplier(double conversationDepthMultiplier) { this.conversationDepthMultiplier = conversationDepthMultiplier; }
        
        public double getSocialEnergyMultiplier() { return socialEnergyMultiplier; }
        public void setSocialEnergyMultiplier(double socialEnergyMultiplier) { this.socialEnergyMultiplier = socialEnergyMultiplier; }
        
        public double getRandomFactor() { return randomFactor; }
        public void setRandomFactor(double randomFactor) { this.randomFactor = randomFactor; }
    }
    
    /**
     * 内容生成配置
     */
    public static class ContentGeneration {
        private int maxLength;
        private int minLength;
        private double emojiProbability;
        private double mentionProbability;
        private double hashtagProbability;
        private double moodExpressionProbability;
        private double personalStoryProbability;
        
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        
        public int getMinLength() { return minLength; }
        public void setMinLength(int minLength) { this.minLength = minLength; }
        
        public double getEmojiProbability() { return emojiProbability; }
        public void setEmojiProbability(double emojiProbability) { this.emojiProbability = emojiProbability; }
        
        public double getMentionProbability() { return mentionProbability; }
        public void setMentionProbability(double mentionProbability) { this.mentionProbability = mentionProbability; }
        
        public double getHashtagProbability() { return hashtagProbability; }
        public void setHashtagProbability(double hashtagProbability) { this.hashtagProbability = hashtagProbability; }
        
        public double getMoodExpressionProbability() { return moodExpressionProbability; }
        public void setMoodExpressionProbability(double moodExpressionProbability) { this.moodExpressionProbability = moodExpressionProbability; }
        
        public double getPersonalStoryProbability() { return personalStoryProbability; }
        public void setPersonalStoryProbability(double personalStoryProbability) { this.personalStoryProbability = personalStoryProbability; }
    }
    
    /**
     * 时间控制配置
     */
    public static class TimeControl {
        private int minIntervalBetweenActions;
        private int maxActionsPerHour;
        private int cooldownAfterPeakHours;
        private double weekendMultiplier;
        private double holidayMultiplier;
        
        public int getMinIntervalBetweenActions() { return minIntervalBetweenActions; }
        public void setMinIntervalBetweenActions(int minIntervalBetweenActions) { this.minIntervalBetweenActions = minIntervalBetweenActions; }
        
        public int getMaxActionsPerHour() { return maxActionsPerHour; }
        public void setMaxActionsPerHour(int maxActionsPerHour) { this.maxActionsPerHour = maxActionsPerHour; }
        
        public int getCooldownAfterPeakHours() { return cooldownAfterPeakHours; }
        public void setCooldownAfterPeakHours(int cooldownAfterPeakHours) { this.cooldownAfterPeakHours = cooldownAfterPeakHours; }
        
        public double getWeekendMultiplier() { return weekendMultiplier; }
        public void setWeekendMultiplier(double weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }
        
        public double getHolidayMultiplier() { return holidayMultiplier; }
        public void setHolidayMultiplier(double holidayMultiplier) { this.holidayMultiplier = holidayMultiplier; }
    }
    
    /**
     * 情绪系统配置
     */
    public static class EmotionSystem {
        private double baseMood;
        private double moodSwingProbability;
        private double moodInfluenceOnContent;
        private double recoveryRate;
        
        public double getBaseMood() { return baseMood; }
        public void setBaseMood(double baseMood) { this.baseMood = baseMood; }
        
        public double getMoodSwingProbability() { return moodSwingProbability; }
        public void setMoodSwingProbability(double moodSwingProbability) { this.moodSwingProbability = moodSwingProbability; }
        
        public double getMoodInfluenceOnContent() { return moodInfluenceOnContent; }
        public void setMoodInfluenceOnContent(double moodInfluenceOnContent) { this.moodInfluenceOnContent = moodInfluenceOnContent; }
        
        public double getRecoveryRate() { return recoveryRate; }
        public void setRecoveryRate(double recoveryRate) { this.recoveryRate = recoveryRate; }
    }
    
    /**
     * 社交关系系统配置
     */
    public static class SocialRelationship {
        private List<String> friendshipLevels;
        private int interactionMemory;
        private double relationshipDecayRate;
        private double trustBuildingRate;
        
        public List<String> getFriendshipLevels() { return friendshipLevels; }
        public void setFriendshipLevels(List<String> friendshipLevels) { this.friendshipLevels = friendshipLevels; }
        
        public int getInteractionMemory() { return interactionMemory; }
        public void setInteractionMemory(int interactionMemory) { this.interactionMemory = interactionMemory; }
        
        public double getRelationshipDecayRate() { return relationshipDecayRate; }
        public void setRelationshipDecayRate(double relationshipDecayRate) { this.relationshipDecayRate = relationshipDecayRate; }
        
        public double getTrustBuildingRate() { return trustBuildingRate; }
        public void setTrustBuildingRate(double trustBuildingRate) { this.trustBuildingRate = trustBuildingRate; }
    }
} 