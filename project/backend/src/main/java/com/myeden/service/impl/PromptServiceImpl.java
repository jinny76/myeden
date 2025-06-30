package com.myeden.service.impl;

import com.myeden.config.WorldConfig;
import com.myeden.entity.Robot;
import com.myeden.entity.User;
import com.myeden.repository.RobotRepository;
import com.myeden.repository.UserRepository;
import com.myeden.service.DifyService;
import com.myeden.service.PromptService;
import com.myeden.service.PostService;
import com.myeden.service.CommentService;
import com.myeden.config.RobotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 提示词服务实现类
 * 实现提示词构建、内容处理和上下文构建的具体逻辑
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class PromptServiceImpl implements PromptService {
    
    @Autowired
    private RobotConfig robotConfig;

    @Autowired
    private WorldConfig worldConfig;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DifyService difyService;
    
    private final Random random = new Random();
    
    @Override
    public String buildPostPrompt(Robot robot, String context) {
        StringBuilder prompt = new StringBuilder();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 构建机器人身份设定
        prompt.append(String.format("你是%s（昵称：%s），%s。\n\n## 背景信息清单",
            robot.getName(), 
            robotInfo != null ? robotInfo.getNickname() : robot.getName(), 
            robot.getPersonality()));
        
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));
        
        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n### 当前情况：%s", context));
        }
        
        // 添加随机选择的主题
        String selectedTopic = selectRandomTopic(robot);
        prompt.append(String.format("\n\n## 生成%s相关内容", selectedTopic));
        
        // 添加动态生成要求
        prompt.append("\n\n## 根据以下发帖要求, 和你的性格和当前情况，生成一条纯文本的，自然、真实的朋友圈动态, 仅返回动态本身, 不包含任何标题。");
        prompt.append("\n- 避免机械感, 广告感, 官方口吻, 要使用口语化, 略带网络感的表达, 偶尔可以有小瑕疵(比如错别字, 用'...'代表思考)");
        prompt.append("\n- 内容要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在10-150字之间");
        prompt.append("\n- 必须围绕指定的主题进行创作");
        prompt.append("\n- 控制内容与职业相关回答占10%, 内容与职业无关的回答占90%");

        log.info("生成的动态提示词: {}", prompt.toString());
        
        return prompt.toString();
    }
    
    @Override
    public String buildCommentPrompt(Robot robot, PostService.PostDetail post, String context) {
        StringBuilder prompt = new StringBuilder();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 构建机器人身份设定
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），%s。你看到了一条朋友圈动态，想要评论一下。\n\n## 背景信息清单",
            robot.getName(), nickname, robot.getPersonality()));
        
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));
        
        // 添加动态信息
        prompt.append(String.format("\n\n## 你看到的动态内容：%s", post.getContent()));
        prompt.append(String.format("\n作者信息：%s", getAuthorInfo(post)));
        
        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n当前情况：%s", context));
        }
        
        // 添加评论生成要求
        prompt.append("\n\n请根据一下发帖要求，加上你的性格和先前你看到的动态内容，生成一条纯文本的，自然、真实的评论, 仅返回动态本身, 不包含任何标题。");
        prompt.append("\n- 避免机械感, 广告感, 官方口吻, 要使用口语化, 略带网络感的表达, 偶尔可以有小瑕疵(比如错别字, 用'...'代表思考)");
        prompt.append("\n- 评论要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在20-40字之间");
        prompt.append("\n- 控制内容与职业相关回答占10%, 内容与职业无关的回答占90%");

        log.info("生成的评论提示词: {}", prompt.toString());
        
        return prompt.toString();
    }
    
    @Override
    public String buildReplyPrompt(Robot robot, CommentService.CommentDetail commentDetail, 
                                  PostService.PostDetail postDetail, String context) {
        StringBuilder prompt = new StringBuilder();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 构建机器人身份设定
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），%s。你看到一个朋友圈消息的评论， 要回复一条评论\n\n## 背景信息列表",
            robot.getName(), nickname, robot.getPersonality()));
        
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));
        
        // 添加动态和评论信息
        prompt.append(String.format("\n\n你看到的朋友圈内容：%s", postDetail.getContent()));
        prompt.append(String.format("\n你看到的评论内容：%s", commentDetail.getContent()));
        prompt.append(String.format("\n评论者是：%s", getCommentAuthorInfo(commentDetail)));
        
        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n当前情况：%s", context));
        }
        
        // 添加回复生成要求
        prompt.append("\n\n请根据以下要求, 结合你的性格和评论内容，生成一条纯文本的, 自然、真实的回复，只返回评论内容, 不要任何标题。");
        prompt.append("\n- 避免机械感, 广告感, 官方口吻, 要使用口语化, 略带网络感的表达, 偶尔可以有小瑕疵(比如错别字, 用'...'代表思考)");
        prompt.append("\n- 回复要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在15-30字之间");
        prompt.append("\n- 控制内容与职业相关回答占10%, 内容与职业无关的回答占90%");

        log.info("生成的回复提示词: {}", prompt.toString());
        
        return prompt.toString();
    }
    
    @Override
    public String buildInnerThoughtsPrompt(Robot robot, String situation) {
        StringBuilder prompt = new StringBuilder();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 构建机器人身份设定
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），一个%s的伊甸园居民。现在你要进行内心独白。", 
            robot.getName(), nickname, robot.getPersonality()));
        
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));
        
        // 添加当前情况
        prompt.append(String.format("\n\n当前情况：%s", situation));
        
        // 添加内心独白生成要求
        prompt.append("\n\n请根据你的性格和当前情况，生成一段内心独白。");
        prompt.append("要求：");
        prompt.append("\n1. 独白要符合你的性格特征");
        prompt.append("\n2. 语言风格要符合你的说话习惯");
        prompt.append("\n3. 长度控制在30-100字之间");
        prompt.append("\n4. 内容要真实自然，体现内心感受");
        prompt.append("\n5. 可以适当使用省略号等表达方式");
        
        log.info("生成的内心活动提示词: {}", prompt.toString());
        
        return prompt.toString();
    }
    
    @Override
    public String processGeneratedContent(String rawContent, Robot robot, String contentType) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        String processedContent = rawContent.trim();

        // 移除多余的换行和空格
        processedContent = processedContent.replaceAll("\\n+", "\n").replaceAll(" +", " ");
        if (processedContent.startsWith("<think>\n</think>\n")) {
            processedContent = processedContent.replaceAll("<think>\n</think>\n", "");
        }

        log.info(processedContent);
        
        return processedContent;
    }
    
    @Override
    public String buildContext(Robot robot) {
        StringBuilder context = new StringBuilder();
        
        // 添加时间信息
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        String dayOfWeek = getDayOfWeek(now.getDayOfWeek().getValue());
        
        context.append(String.format("现在是%s，%s", timeStr, dayOfWeek));
        
        // 添加时间段信息
        String timeSlot = getTimeSlot(now.getHour());
        context.append(String.format("，%s", timeSlot));
        
        // 添加天气信息（模拟）
        String weather = getRandomWeather();
        context.append(String.format("，天气%s", weather));
        
        return context.toString();
    }
    
    @Override
    public boolean validateContent(String content, Robot robot, String contentType) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // 检查内容长度
        int minLength = 0, maxLength = 0;
        switch (contentType) {
            case "post":
                minLength = 10;
                maxLength = 150;
                break;
            case "comment":
                minLength = 5;
                maxLength = 80;
                break;
            case "reply":
                minLength = 3;
                maxLength = 60;
                break;
        }
        
        if (content.length() < minLength || content.length() > maxLength) {
            return false;
        }
        
        // 检查是否包含不当内容
        String[] inappropriateWords = {"死", "杀", "暴力", "色情", "政治"};
        for (String word : inappropriateWords) {
            if (content.contains(word)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String getRobotPersonality(Robot robot) {
        StringBuilder personality = new StringBuilder();
        
        personality.append(String.format("姓名：%s", robot.getName()));
        personality.append(String.format("\n性格：%s", robot.getPersonality()));
        
        if (robot.getGender() != null) {
            personality.append(String.format("\n性别：%s", robot.getGender()));
        }
        if (robot.getAge() != null) {
            personality.append(String.format("\n年龄：%d岁", robot.getAge()));
        }
        if (robot.getOccupation() != null) {
            personality.append(String.format("\n职业：%s", robot.getOccupation()));
        }
        if (robot.getLocation() != null) {
            personality.append(String.format("\n所在地：%s", robot.getLocation()));
        }
        
        return personality.toString();
    }
    
    // 私有辅助方法
    
    private RobotConfig.RobotInfo getRobotInfo(String robotName) {
        if (robotConfig.getList() != null) {
            for (RobotConfig.RobotInfo robotInfo : robotConfig.getList()) {
                if (robotName.equals(robotInfo.getName())) {
                    return robotInfo;
                }
            }
        }
        return null;
    }
    
    private String buildPersonalInfo(Robot robot, RobotConfig.RobotInfo robotInfo) {
        StringBuilder info = new StringBuilder();
        
        info.append("\n\n### 你的个人档案：");
        info.append(String.format("\n- 性别：%s", robot.getGender() != null ? robot.getGender() : "未知"));
        info.append(String.format("\n- 年龄：%d岁", robot.getAge() != null ? robot.getAge() : 25));
        info.append(String.format("\n- MBTI：%s", robot.getMbti() != null ? robot.getMbti() : "未知"));
        info.append(String.format("\n- 血型：%s型", robot.getBloodType() != null ? robot.getBloodType() : "未知"));
        info.append(String.format("\n- 星座：%s", robot.getZodiac() != null ? robot.getZodiac() : "未知"));
        info.append(String.format("\n- 职业：%s", robot.getOccupation() != null ? robot.getOccupation() : "未知"));
        info.append(String.format("\n- 所在地：%s", robot.getLocation() != null ? robot.getLocation() : "未知"));
        info.append(String.format("\n- 学历：%s", robot.getEducation() != null ? robot.getEducation() : "未知"));
        info.append(String.format("\n- 感情状态：%s", getRelationshipText(robot.getRelationship())));
        
        return info.toString();
    }
    
    private String buildTraitsAndInterests(Robot robot, RobotConfig.RobotInfo robotInfo) {
        StringBuilder traits = new StringBuilder();
        
        // 性格特征
        if (robot.getTraits() != null && !robot.getTraits().isEmpty()) {
            traits.append(String.format("\n- 性格特征：%s", String.join("、", robot.getTraits())));
        }
        
        // 兴趣爱好
        if (robot.getInterests() != null && !robot.getInterests().isEmpty()) {
            traits.append(String.format("\n- 兴趣爱好：%s", String.join("、", robot.getInterests())));
        }
        
        return traits.toString();
    }
    
    private String buildSpeakingStyle(RobotConfig.RobotInfo robotInfo) {
        if (robotInfo == null || robotInfo.getSpeakingStyle() == null) {
            return "";
        }
        
        StringBuilder style = new StringBuilder();
        RobotConfig.SpeakingStyle speakingStyle = robotInfo.getSpeakingStyle();
        
        style.append(String.format("\n- 说话风格：%s", speakingStyle.getTone()));
        style.append(String.format("\n- 用词特点：%s", speakingStyle.getVocabulary()));
        style.append(String.format("\n- 表情使用：%s", speakingStyle.getEmojiUsage()));
        style.append(String.format("\n- 句子长度：%s", speakingStyle.getSentenceLength()));
        
        if (speakingStyle.getFavoriteWords() != null && !speakingStyle.getFavoriteWords().isEmpty()) {
            style.append(String.format("\n- 常用词汇：%s", String.join("、", speakingStyle.getFavoriteWords())));
        }
        
        if (speakingStyle.getSpeechPatterns() != null && !speakingStyle.getSpeechPatterns().isEmpty()) {
            style.append(String.format("\n- 说话习惯：%s", String.join("、", speakingStyle.getSpeechPatterns())));
        }
        
        return style.toString();
    }
    
    /**
     * 获取动态作者信息
     * 从post中获取作者ID和类型，然后使用repository查询详细信息并拼接成作者信息
     * 
     * @param post 动态详情
     * @return 格式化的作者信息
     */
    private String getAuthorInfo(PostService.PostDetail post) {
        if (post == null || post.getAuthorId() == null) {
            return "未知作者";
        }
        
        StringBuilder authorInfo = new StringBuilder();
        
        try {
            if ("robot".equals(post.getAuthorType())) {
                // 查询机器人详细信息
                Robot robot = robotRepository.findByRobotId(post.getAuthorId()).orElse(null);
                if (robot != null) {
                    authorInfo.append(String.format("机器人：%s", robot.getName()));
                    
                    // 添加机器人详细信息
                    if (robot.getAge() != null) {
                        authorInfo.append(String.format("，%d岁", robot.getAge()));
                    }
                    if (robot.getGender() != null) {
                        authorInfo.append(String.format("，%s", "male".equals(robot.getGender()) ? "男" : "女"));
                    }
                    if (robot.getPersonality() != null) {
                        authorInfo.append(String.format("，%s", robot.getPersonality()));
                    }
                    if (robot.getLocation() != null) {
                        authorInfo.append(String.format("，来自%s", robot.getLocation()));
                    }
                } else {
                    authorInfo.append(String.format("机器人：%s", post.getAuthorName() != null ? post.getAuthorName() : "未知"));
                }
            } else {
                // 查询用户详细信息
                User user = userRepository.findByUserId(post.getAuthorId()).orElse(null);
                if (user != null) {
                    authorInfo.append(String.format("用户：%s", user.getNickname() != null ? user.getNickname() : "未知"));
                    
                    // 添加用户详细信息
                    if (user.getAge() != null) {
                        authorInfo.append(String.format("，%d岁", user.getAge()));
                    }
                    if (user.getGender() != null) {
                        authorInfo.append(String.format("，%s", "male".equals(user.getGender()) ? "男" : "女"));
                    }
                    if (user.getTitle() != null) {
                        authorInfo.append(String.format("，%s", user.getTitle()));
                    }
                    if (user.getIntroduction() != null && !user.getIntroduction().trim().isEmpty()) {
                        authorInfo.append(String.format("，%s", user.getIntroduction()));
                    }
                } else {
                    authorInfo.append(String.format("用户：%s", post.getAuthorName() != null ? post.getAuthorName() : "未知"));
                }
            }
        } catch (Exception e) {
            log.error("获取作者信息失败，postId: {}, authorId: {}, error: {}", 
                    post.getPostId(), post.getAuthorId(), e.getMessage(), e);
            authorInfo.append("未知作者");
        }
        
        return authorInfo.toString();
    }
    
    /**
     * 获取评论作者信息
     * 从comment中获取作者ID和类型，然后使用repository查询详细信息并拼接成作者信息
     * 
     * @param commentDetail 评论详情
     * @return 格式化的作者信息
     */
    private String getCommentAuthorInfo(CommentService.CommentDetail commentDetail) {
        if (commentDetail == null || commentDetail.getAuthorId() == null) {
            return "未知评论者";
        }
        
        StringBuilder authorInfo = new StringBuilder();
        
        try {
            if ("robot".equals(commentDetail.getAuthorType())) {
                // 查询机器人详细信息
                Robot robot = robotRepository.findByRobotId(commentDetail.getAuthorId()).orElse(null);
                if (robot != null) {
                    authorInfo.append(String.format("机器人：%s", robot.getName()));
                    
                    // 添加机器人详细信息
                    if (robot.getAge() != null) {
                        authorInfo.append(String.format("，%d岁", robot.getAge()));
                    }
                    if (robot.getGender() != null) {
                        authorInfo.append(String.format("，%s", "male".equals(robot.getGender()) ? "男" : "女"));
                    }
                    if (robot.getPersonality() != null) {
                        authorInfo.append(String.format("，%s", robot.getPersonality()));
                    }
                } else {
                    authorInfo.append(String.format("机器人：%s", commentDetail.getAuthorName() != null ? commentDetail.getAuthorName() : "未知"));
                }
            } else {
                // 查询用户详细信息
                User user = userRepository.findByUserId(commentDetail.getAuthorId()).orElse(null);
                if (user != null) {
                    authorInfo.append(String.format("用户：%s", user.getNickname() != null ? user.getNickname() : "未知"));
                    
                    // 添加用户详细信息
                    if (user.getAge() != null) {
                        authorInfo.append(String.format("，%d岁", user.getAge()));
                    }
                    if (user.getGender() != null) {
                        authorInfo.append(String.format("，%s", "male".equals(user.getGender()) ? "男" : "女"));
                    }
                    if (user.getTitle() != null) {
                        authorInfo.append(String.format("，%s", user.getTitle()));
                    }
                } else {
                    authorInfo.append(String.format("用户：%s", commentDetail.getAuthorName() != null ? commentDetail.getAuthorName() : "未知"));
                }
            }
        } catch (Exception e) {
            log.error("获取评论作者信息失败，commentId: {}, authorId: {}, error: {}", 
                    commentDetail.getCommentId(), commentDetail.getAuthorId(), e.getMessage(), e);
            authorInfo.append("未知评论者");
        }
        
        return authorInfo.toString();
    }

    @Override
    public String generatePostContent(Robot robot, String context) {
        try {
            // 使用PromptService构建提示词
            String prompt = buildPostPrompt(robot, context);

            // 调用Dify API
            String rawContent = difyService.callDifyApi(prompt, robot.getRobotId());

            // 使用PromptService处理生成的内容
            return processGeneratedContent(rawContent, robot, "post");
        } catch (Exception e) {
            log.error("生成机器人动态内容失败: {}", e.getMessage(), e);
            return generateFallbackPost(robot, context);
        }
    }

    @Override
    public String generateCommentContent(Robot robot, PostService.PostDetail post, String context) {
        try {
            // 使用PromptService构建提示词
            String prompt = buildCommentPrompt(robot, post, context);

            // 调用Dify API
            String rawContent = difyService.callDifyApi(prompt, robot.getRobotId());

            // 使用PromptService处理生成的内容
            return processGeneratedContent(rawContent, robot, "comment");
        } catch (Exception e) {
            log.error("生成机器人评论内容失败: {}", e.getMessage(), e);
            return generateFallbackComment(robot, post.getContent());
        }
    }

    @Override
    public String generateReplyContent(Robot robot, CommentService.CommentDetail commentDetail, PostService.PostDetail postDetail, String context) {
        try {
            // 使用PromptService构建提示词
            String prompt = buildReplyPrompt(robot, commentDetail, postDetail, context);

            // 调用Dify API
            String rawContent = difyService.callDifyApi(prompt, robot.getRobotId());

            // 使用PromptService处理生成的内容
            return processGeneratedContent(rawContent, robot, "reply");
        } catch (Exception e) {
            log.error("生成机器人回复内容失败: {}", e.getMessage(), e);
            return generateFallbackReply(robot, commentDetail.getContent());
        }
    }

    @Override
    public String generateInnerThoughts(Robot robot, String situation) {
        try {
            // 使用PromptService构建提示词
            String prompt = buildInnerThoughtsPrompt(robot, situation);

            // 调用Dify API
            String rawContent = difyService.callDifyApi(prompt, robot.getRobotId());

            // 使用PromptService处理生成的内容
            return processGeneratedContent(rawContent, robot, "inner_thoughts");
        } catch (Exception e) {
            log.error("生成机器人内心活动失败: {}", e.getMessage(), e);
            return generateFallbackInnerThoughts(robot, situation);
        }
    }
    
    private String getRelationshipText(String relationship) {
        if (relationship == null) return "未知";
        
        switch (relationship) {
            case "single": return "单身";
            case "married": return "已婚";
            case "in_relationship": return "恋爱中";
            case "complicated": return "复杂";
            default: return relationship;
        }
    }
    
    private String getDayOfWeek(int dayOfWeek) {
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return days[dayOfWeek];
    }
    
    private String getTimeSlot(int hour) {
        if (hour >= 6 && hour < 12) return "上午";
        else if (hour >= 12 && hour < 18) return "下午";
        else if (hour >= 18 && hour < 22) return "晚上";
        else return "深夜";
    }
    
    private String getRandomWeather() {
        String[] weathers = {"晴朗", "多云", "阴天", "小雨", "微风"};
        return weathers[random.nextInt(weathers.length)];
    }

    private String generateFallbackPost(Robot robot, String context) {
        return String.format("大家好，我是%s。今天天气不错，心情也很好！", robot.getName());
    }

    private String generateFallbackComment(Robot robot, String postContent) {
        return String.format("不错哦，%s！", robot.getName());
    }

    private String generateFallbackReply(Robot robot, String commentContent) {
        return "谢谢！";
    }

    private String generateFallbackInnerThoughts(Robot robot, String situation) {
        return String.format("%s正在思考：%s", robot.getName(), situation);
    }

    @Override
    public String selectRandomTopic(Robot robot) {
        try {
            // 获取合并后的主题列表
            List<TopicItem> mergedTopics = getMergedTopics(robot);
            
            if (mergedTopics.isEmpty()) {
                log.warn("机器人 {} 没有可用的主题，使用默认主题", robot.getName());
                return "分享生活";
            }
            
            // 根据频次创建权重列表
            List<TopicItem> weightedTopics = new ArrayList<>();
            for (TopicItem topic : mergedTopics) {
                // 根据频次重复添加主题，实现权重效果
                for (int i = 0; i < topic.getFrequency(); i++) {
                    weightedTopics.add(topic);
                }
            }
            
            // 随机选择一个主题
            TopicItem selectedTopic = weightedTopics.get(random.nextInt(weightedTopics.size()));
            
            log.info("为机器人 {} 选择了主题: {} (来源: {})", 
                    robot.getName(), selectedTopic.getName(), selectedTopic.getSource());
            
            return selectedTopic.getContent();
        } catch (Exception e) {
            log.error("为机器人 {} 选择主题失败: {}", robot.getName(), e.getMessage(), e);
            return "分享生活";
        }
    }
    
    @Override
    public List<TopicItem> getMergedTopics(Robot robot) {
        List<TopicItem> mergedTopics = new ArrayList<>();
        
        try {
            // 获取通用主题
            if (robotConfig.getBaseConfig() != null && robotConfig.getBaseConfig().getCommonTopic() != null) {
                for (RobotConfig.CommonTopic commonTopic : robotConfig.getBaseConfig().getCommonTopic()) {
                    mergedTopics.add(new TopicItem(
                            commonTopic.getName(),
                            commonTopic.getContent(),
                            commonTopic.getFrequency(),
                            "common"
                    ));
                }
            }
            
            // 获取机器人个人主题
            RobotConfig.RobotInfo robotInfo = findRobotInfo(robot.getRobotId());
            if (robotInfo != null && robotInfo.getTopic() != null) {
                for (RobotConfig.Topic personalTopic : robotInfo.getTopic()) {
                    mergedTopics.add(new TopicItem(
                            personalTopic.getName(),
                            personalTopic.getContent(),
                            personalTopic.getFrequency(),
                            "personal"
                    ));
                }
            }
            
            log.debug("机器人 {} 的合并主题列表: {} 个通用主题, {} 个个人主题", 
                    robot.getName(),
                    mergedTopics.stream().filter(t -> "common".equals(t.getSource())).count(),
                    mergedTopics.stream().filter(t -> "personal".equals(t.getSource())).count());
            
        } catch (Exception e) {
            log.error("获取机器人 {} 的合并主题列表失败: {}", robot.getName(), e.getMessage(), e);
        }
        
        return mergedTopics;
    }
    
    /**
     * 根据机器人ID查找机器人配置信息
     * 
     * @param robotId 机器人ID
     * @return 机器人配置信息，如果未找到返回null
     */
    private RobotConfig.RobotInfo findRobotInfo(String robotId) {
        if (robotConfig.getList() == null) {
            return null;
        }
        
        return robotConfig.getList().stream()
                .filter(robot -> robotId.equals(robot.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 构建智能背景信息
     * 使用智能选择器构建机器人的背景信息
     */
    private String buildSmartBackground(Robot robot) {
        StringBuilder background = new StringBuilder();
        
        // 基本信息 - 选择性获取
        background.append("\n性别:" + robot.getGender());
        background.append("\n年龄:" + robot.getAge());
        List<String> occupation = getRobotValue(robot, "occupation", 1, 0.8);
        background.append("\n职业:" + (occupation.size() > 0 ? occupation.get(0) : ""));
        
        
        // 性格特征 - 选择性获取2-3个
        List<String> traits = getRobotValue(robot, "traits", 1, 0.4);
        if (!traits.isEmpty()) {
            background.append("\n### 性格特征：");
            for (String trait : traits) {
                background.append(String.format("\n- %s", trait));
            }
        }
        
        // 兴趣爱好 - 选择性获取2-4个
        List<String> interests = getRobotValue(robot, "interests", 1, 0.3);
        if (!interests.isEmpty()) {
            background.append("\n### 兴趣爱好：");
            for (String interest : interests) {
                background.append(String.format("\n- %s", interest));
            }
        }
        
        // 说话风格 - 选择性获取
        List<String> speakingStyle = getRobotValue(robot, "speakingStyle.tone", 1, 0.2);
        if (!speakingStyle.isEmpty()) {
            background.append(String.format("\n### 说话风格：%s", speakingStyle.get(0)));
        }
        
        // 常用词汇 - 选择性获取1-3个
        List<String> favoriteWords = getRobotValue(robot, "speakingStyle.favoriteWords", 1, 0.5);
        if (!favoriteWords.isEmpty()) {
            background.append(String.format("\n### 常用词汇：%s", String.join("、", favoriteWords)));
        }
        
        // 说话习惯 - 选择性获取1-2个
        List<String> speechPatterns = getRobotValue(robot, "speakingStyle.speechPatterns", 2, 0.6);
        if (!speechPatterns.isEmpty()) {
            background.append(String.format("\n### 说话习惯：%s", String.join("、", speechPatterns)));
        }

        // 背景信息 - 选择性获取1-2个
        List<String> backgroundInfo = getRobotValue(robot, "background", 1, 0.4);
        if (!backgroundInfo.isEmpty()) {
            background.append(String.format("\n### 背景信息：%s", String.join("、", backgroundInfo)));
        }
        
        return background.toString();
    }
    
    /**
     * 构建智能个人档案
     * 使用智能选择器构建机器人的个人档案
     */
    private String buildSmartPersonalInfo(Robot robot) {
        StringBuilder info = new StringBuilder();
        
        info.append("\n### 个人档案：");
        
        // 基本信息 - 选择性显示
        String[] basicFields = {"gender", "age", "mbti", "bloodType", "zodiac", "occupation", "location", "education", "relationship"};
        for (String field : basicFields) {
            List<String> values = getRobotValue(robot, "robot." + field, 1, 0.3);
            if (!values.isEmpty()) {
                String label = getFieldLabel(field);
                String value = formatFieldValue(field, values.get(0));
                info.append(String.format("\n- %s：%s", label, value));
            }
        }
        
        return info.toString();
    }
    
    /**
     * 获取字段标签
     */
    private String getFieldLabel(String field) {
        switch (field) {
            case "gender": return "性别";
            case "age": return "年龄";
            case "mbti": return "MBTI";
            case "bloodType": return "血型";
            case "zodiac": return "星座";
            case "occupation": return "职业";
            case "location": return "所在地";
            case "education": return "学历";
            case "relationship": return "感情状态";
            default: return field;
        }
    }
    
    /**
     * 格式化字段值
     */
    private String formatFieldValue(String field, String value) {
        if (value == null || value.isEmpty()) {
            return "未知";
        }
        
        switch (field) {
            case "gender":
                return "male".equals(value) ? "男" : "female".equals(value) ? "女" : value;
            case "bloodType":
                return value + "型";
            case "relationship":
                return getRelationshipText(value);
            case "age":
                return value + "岁";
            default:
                return value;
        }
    }

    /**
     * 智能机器人属性选择器
     * 根据属性名选择性获取机器人配置信息，支持多级属性获取
     * 
     * @param robot 机器人实体
     * @param attributePath 属性路径，支持多级，如 "speakingStyle.speechPatterns"
     * @param maxUnits 最大返回单元数量
     * @param nullProbability 空值几率 (0.0-1.0)
     * @return 选中的属性值数组
     */
    private List<String> getRobotValue(Robot robot, String attributePath, int maxUnits, double nullProbability) {
        List<String> result = new ArrayList<>();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 解析属性路径
        String[] pathParts = attributePath.split("\\.");
        Object currentValue = null;
        
        // 确定起始对象
        if (pathParts[0].equals("robot")) {
            currentValue = robot;
        } else if (pathParts[0].equals("config")) {
            currentValue = robotInfo;
        } else {
            // 默认从robotInfo开始查找
            currentValue = robotInfo;
        }
        
        // 遍历属性路径
        for (int i = 0; i < pathParts.length && currentValue != null; i++) {
            String part = pathParts[i];
            if (part.equals("robot") || part.equals("config")) {
                continue; // 跳过前缀
            }
            
            currentValue = getPropertyValue(currentValue, part);
        }
        
        // 处理最终值
        if (currentValue != null) {
            List<String> units = extractUnits(currentValue);
            result = selectRandomUnits(units, maxUnits, nullProbability);
        }
        
        return result;
    }
    
    /**
     * 获取对象的属性值
     */
    private Object getPropertyValue(Object obj, String propertyName) {
        if (obj == null) return null;
        
        try {
            // 使用反射获取属性值
            String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            java.lang.reflect.Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (Exception e) {
            log.warn("获取属性值失败: {}.{}, error: {}", 
                    obj.getClass().getSimpleName(), propertyName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 从值中提取单元
     */
    private List<String> extractUnits(Object value) {
        List<String> units = new ArrayList<>();
        
        if (value == null) {
            return units;
        }
        
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.contains("\n")) {
                // 多行字符串，每行一个单元
                String[] lines = strValue.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        units.add(trimmed);
                    }
                }
            } else {
                // 单行字符串，按逗号分隔
                String[] parts = strValue.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        units.add(trimmed);
                    }
                }
            }
        } else if (value instanceof List) {
            // 列表，每个元素一个单元
            List<?> list = (List<?>) value;
            for (Object item : list) {
                if (item != null) {
                    units.add(item.toString());
                }
            }
        } else {
            // 其他类型，直接转换为字符串
            units.add(value.toString());
        }
        
        return units;
    }
    
    /**
     * 随机选择单元
     */
    private List<String> selectRandomUnits(List<String> units, int maxUnits, double nullProbability) {
        List<String> result = new ArrayList<>();
        
        if (units.isEmpty()) {
            return result;
        }
        
        // 计算每个单元的出现概率
        double unitProbability = 1.0 - nullProbability;
        
        // 随机选择单元
        Collections.shuffle(units);

        for (int i = 0; i < maxUnits && i < units.size(); i++) {
            if (random.nextDouble() < unitProbability) {
                String selectedUnit = units.get(i);
                result.add(selectedUnit);
            }
        }
        
        return result;
    }
} 