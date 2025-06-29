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
        
        // 添加个人基本信息
        prompt.append(buildPersonalInfo(robot, robotInfo));
        
        // 添加性格特征和兴趣爱好
        prompt.append(buildTraitsAndInterests(robot, robotInfo));
        
        // 添加说话风格
        prompt.append(buildSpeakingStyle(robotInfo));
        
        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n### 当前情况：%s", context));
        }
        
        // 添加随机选择的主题
        String selectedTopic = selectRandomTopic(robot);
        prompt.append(String.format("\n\n## 生成%s相关内容", selectedTopic));
        
        // 添加动态生成要求
        prompt.append("\n\n## 根据以下发帖要求, 和你的性格和当前情况，生成一条纯文本的，自然、真实的朋友圈动态, 仅返回动态本身, 不包含任何标题。");
        prompt.append("\n1. 内容要符合你的性格特征");
        prompt.append("\n2. 语言风格要符合你的说话习惯");
        prompt.append("\n3. 长度控制在10-150字之间, 以简短内容为主");
        prompt.append("\n4. 必须围绕指定的主题进行创作");
        prompt.append("\n5. 内容随意，不要有任何限制");
        if (robotInfo != null && robotInfo.getExample() != null && !robotInfo.getExample().trim().isEmpty()) {
            prompt.append(String.format("\n6. 发帖示例参考：\n%s", robotInfo.getExample()));
        }

        log.info(prompt.toString());
        
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
        
        // 添加个人基本信息
        prompt.append(buildPersonalInfo(robot, robotInfo));
        
        // 添加性格特征和兴趣爱好
        prompt.append(buildTraitsAndInterests(robot, robotInfo));
        
        // 添加说话风格
        prompt.append(buildSpeakingStyle(robotInfo));
        
        // 添加动态信息
        prompt.append(String.format("\n\n## 你看到的动态内容：%s", post.getContent()));
        prompt.append(String.format("\n作者信息：%s", getAuthorInfo(post)));
        
        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n当前情况：%s", context));
        }
        
        // 添加评论生成要求
        prompt.append("\n\n请根据一下发帖要求，加上你的性格和先前你看到的动态内容，生成一条纯文本的，自然、真实的评论, 仅返回动态本身, 不包含任何标题。");
        prompt.append("\n1. 评论要符合你的性格特征");
        prompt.append("\n2. 语言风格要符合你的说话习惯");
        prompt.append("\n3. 长度控制在20-80字之间");
        if (robotInfo != null && robotInfo.getExample() != null && !robotInfo.getExample().trim().isEmpty()) {
            prompt.append(String.format("\n4. 发帖示例参考：\n%s", robotInfo.getExample()));
        }

        log.info(prompt.toString());
        
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
        
        // 添加个人基本信息
        prompt.append(buildPersonalInfo(robot, robotInfo));
        
        // 添加性格特征和兴趣爱好
        prompt.append(buildTraitsAndInterests(robot, robotInfo));
        
        // 添加说话风格
        prompt.append(buildSpeakingStyle(robotInfo));
        
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
        prompt.append("\n1. 回复要符合你的性格特征");
        prompt.append("\n2. 语言风格要符合你的说话习惯");
        prompt.append("\n3. 长度控制在15-60字之间");
        if (robotInfo != null && robotInfo.getExample() != null && !robotInfo.getExample().trim().isEmpty()) {
            prompt.append(String.format("\n4. 发帖示例参考：\n%s", robotInfo.getExample()));
        }

        log.info(prompt.toString());
        
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
        
        // 添加个人基本信息
        prompt.append(buildPersonalInfo(robot, robotInfo));
        
        // 添加性格特征和兴趣爱好
        prompt.append(buildTraitsAndInterests(robot, robotInfo));
        
        // 添加当前情况
        prompt.append(String.format("\n\n当前情况：%s", situation));
        
        // 添加内心独白生成要求
        prompt.append("\n\n请根据你的性格和当前情况，生成一段内心独白。");
        prompt.append("要求：");
        prompt.append("\n1. 独白要符合你的性格特征");
        prompt.append("\n2. 语言风格要符合你的说话习惯");
        prompt.append("\n3. 长度控制在30-100字之间");
        prompt.append("\n4. 内容要真实自然，体现内心感受");
        
        return prompt.toString();
    }
    
    @Override
    public String processGeneratedContent(String rawContent, Robot robot, String contentType) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        
        String processedContent = rawContent.trim();

        if (processedContent.startsWith("\"") && processedContent.endsWith("\"")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        } else if (processedContent.startsWith("'") && processedContent.endsWith("'")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        } else if (processedContent.startsWith("“") && processedContent.endsWith("”")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        }
        
        // 移除多余的换行和空格
        processedContent = processedContent.replaceAll("\\n+", "\n").replaceAll(" +", " ");

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
        if (robot.getProfession() != null) {
            personality.append(String.format("\n职业：%s", robot.getProfession()));
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
        info.append(String.format("\n- 职业：%s", robot.getProfession() != null ? robot.getProfession() : "未知"));
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
     * 测试topic选择功能
     * 用于验证通用主题和个人主题的合并与随机选择是否正常工作
     * 
     * @param robot 机器人信息
     * @return 测试结果信息
     */
    public String testTopicSelection(Robot robot) {
        StringBuilder result = new StringBuilder();
        result.append("=== Topic选择功能测试 ===\n");
        result.append(String.format("测试机器人: %s (%s)\n", robot.getName(), robot.getRobotId()));
        
        try {
            // 获取合并后的主题列表
            List<TopicItem> mergedTopics = getMergedTopics(robot);
            result.append(String.format("合并主题总数: %d\n", mergedTopics.size()));
            
            // 统计通用主题和个人主题
            long commonCount = mergedTopics.stream().filter(t -> "common".equals(t.getSource())).count();
            long personalCount = mergedTopics.stream().filter(t -> "personal".equals(t.getSource())).count();
            result.append(String.format("通用主题: %d 个\n", commonCount));
            result.append(String.format("个人主题: %d 个\n", personalCount));
            
            // 显示所有主题详情
            result.append("\n主题详情:\n");
            for (TopicItem topic : mergedTopics) {
                result.append(String.format("- %s (频次: %d, 来源: %s): %s\n", 
                        topic.getName(), topic.getFrequency(), topic.getSource(), topic.getContent()));
            }
            
            // 进行多次随机选择测试
            result.append("\n随机选择测试 (10次):\n");
            for (int i = 1; i <= 10; i++) {
                String selectedTopic = selectRandomTopic(robot);
                result.append(String.format("%d. %s\n", i, selectedTopic));
            }
            
            // 测试提示词构建
            result.append("\n提示词构建测试:\n");
            String context = "现在是下午3点，天气晴朗";
            String prompt = buildPostPrompt(robot, context);
            result.append("生成的提示词包含指定主题要求: " + prompt.contains("指定主题"));
            
        } catch (Exception e) {
            result.append(String.format("测试失败: %s\n", e.getMessage()));
            log.error("Topic选择功能测试失败", e);
        }
        
        return result.toString();
    }
} 