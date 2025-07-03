package com.myeden.service.impl;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.myeden.config.WorldConfig;
import com.myeden.entity.Robot;
import com.myeden.entity.RobotDailyPlan;
import com.myeden.entity.User;
import com.myeden.repository.RobotDailyPlanRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.myeden.entity.Post;
import com.myeden.service.ExternalDataCacheService;
import com.myeden.model.external.HotSearchItem;
import org.springframework.context.annotation.Lazy;
import com.myeden.model.external.WeatherInfo;

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
    private RobotDailyPlanRepository planRepository;

    @Autowired
    private DifyService difyService;
    
    @Autowired
    @Lazy
    private ExternalDataCacheService externalDataCacheService;
    
    private final Random random = new Random();
    
    /**
     * 封装动态生成提示词和外部数据链接的结果对象
     */
    public static class PostPromptResult {
        private String prompt;
        private Post.LinkInfo link;
        public PostPromptResult(String prompt, Post.LinkInfo link) {
            this.prompt = prompt;
            this.link = link;
        }
        public String getPrompt() { return prompt; }
        public Post.LinkInfo getLink() { return link; }
    }

    @Override
    public PostPromptResult buildPostPrompt(Robot robot, String context) {
        StringBuilder prompt = new StringBuilder();
        Post.LinkInfo link = null;
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        // 构建机器人身份设定
        RobotConfig.Topic selectedTopic = selectRandomTopic(robot);
        prompt.append(String.format("你是%s（昵称：%s），%s。正在看朋友圈, 想发一条%s的动态\n\n",
            robot.getName(), 
            robotInfo != null ? robotInfo.getNickname() : robot.getName(), 
            robot.getPersonality(), selectedTopic.getContent()));

        // 新增：根据主题类型插入外部数据背景
        String dataType = selectedTopic.getDataType();
        if (dataType != null) {
            String dataBackground = getDataBackgroundByType(dataType);
            if (dataBackground != null && !dataBackground.isEmpty()) {
                prompt.append("\n\n## 相关数据参考：\n");
                prompt.append(dataBackground);
                // 获取外部数据LinkInfo对象
                // link = getDataLinkInfoByType(dataType);
            }
        }

        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n### 当前情况：%s", context));
        }

        // 添加动态生成要求
        prompt.append("\n\n## 根据以下发帖要求, 和你的性格和当前情况，生成一条纯文本的，自然、真实的朋友圈动态, 仅返回动态本身, 不包含任何标题。");
        prompt.append("\n- 避免机械感, 广告感, 官方口吻, 要使用口语化, 略带网络感的表达, 偶尔可以有小瑕疵(比如错别字, 用'...'代表思考)");
        prompt.append("\n- 内容要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在10-150字之间");
        prompt.append("\n- 必须围绕指定的主题进行创作");
        prompt.append("\n- 控制内容与职业相关回答占10%, 内容与职业无关的回答占90%");
        prompt.append("\n- 后面的背景信息可以作为参考");

        prompt.append("\n\n## 你的背景资料");
        
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));

        // 今日安排
        prompt.append(buildTodayPlanContext(robot, LocalDate.now()));

        log.info("生成的动态提示词: {}", prompt.toString());
        
        return new PostPromptResult(prompt.toString(), link);
    }
    
    @Override
    public String buildCommentPrompt(Robot robot, PostService.PostDetail post, String context) {
        StringBuilder prompt = new StringBuilder();
        
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        // 构建机器人身份设定
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），%s。",
            robot.getName(), nickname, robot.getPersonality()));

        // 添加动态信息
        prompt.append(String.format("\n\n## 你看到的动态内容：\"%s\" 想要评论一下。", post.getContent()));
        prompt.append(String.format("\n这条动态的作者信息是：%s", getAuthorInfo(post)));
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            prompt.append(String.format("\n动态有%s张图片", post.getImages().size()));
        }
        
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
        prompt.append("\n- 后面的背景信息可以作为参考");

        prompt.append("\n\n## 你的背景资料");

        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));

        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));

        // 今日安排
        prompt.append(buildTodayPlanContext(robot, LocalDate.now()));

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
        prompt.append(String.format("你是%s（昵称：%s），%s。\n",
            robot.getName(), nickname, robot.getPersonality()));

        // 添加上下文信息
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(String.format("\n\n当前的情况：%s", context));
        }

        // 添加动态和评论信息
        prompt.append(String.format("\n你正在查看朋友圈内容：%s", postDetail.getContent()));
        prompt.append(String.format("\n这条动态的作者信息是：%s", getAuthorInfo(postDetail)));
        prompt.append(String.format("\n你朋友圈下面你看到有条的评论内容：%s", commentDetail.getContent()));
        prompt.append(String.format("\n这条评论的评论者是：%s, 请注意, 他这条评论是对 %s 说的", getCommentAuthorInfo(commentDetail), postDetail.getAuthorName()));

        // 添加回复生成要求
        prompt.append("\n\n请根据以下要求, 结合你的性格和评论内容，生成一条纯文本的, 自然、真实的回复来回复这条评论，只返回回复内容, 不要任何标题。");
        prompt.append("\n- 避免机械感, 广告感, 官方口吻, 要使用口语化, 略带网络感的表达, 偶尔可以有小瑕疵(比如错别字, 用'...'代表思考)");
        prompt.append("\n- 回复要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在15-30字之间");
        prompt.append("\n- 控制内容与职业相关回答占10%, 内容与职业无关的回答占90%");
        prompt.append("\n- 后面的背景信息可以作为参考");

        prompt.append("\n\n## 你的背景资料");

        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));

        // 今日安排
        prompt.append(buildTodayPlanContext(robot, LocalDate.now()));

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
        prompt.append(String.format("你是%s（昵称：%s），一个%s普通居民。",
            robot.getName(), nickname, robot.getPersonality()));

        // 添加当前情况
        prompt.append(String.format("\n\n当前发生了：%s", situation));

        // 添加内心独白生成要求
        prompt.append("\n\n请根据你的性格和当前情况，生成一段内心独白。");
        prompt.append("要求：");
        prompt.append("\n- 独白要符合你的性格特征");
        prompt.append("\n- 语言风格要符合你的说话习惯");
        prompt.append("\n- 长度控制在30-100字之间");
        prompt.append("\n- 内容要真实自然，体现内心感受");
        prompt.append("\n- 可以适当使用省略号等表达方式");
        prompt.append("\n- 后面的背景信息可以作为参考");

        prompt.append("\n\n## 你的背景资料");
        // 使用智能选择器构建背景信息
        prompt.append(buildSmartBackground(robot));
        
        // 使用智能选择器构建个人档案
        prompt.append(buildSmartPersonalInfo(robot));

        // 今日安排
        prompt.append(buildTodayPlanContext(robot, LocalDate.now()));

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
        if (processedContent.startsWith("\"") && processedContent.endsWith("\"")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        }
        if (processedContent.startsWith("\"") && processedContent.endsWith("\"")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        }
        if (processedContent.startsWith("'") && processedContent.endsWith("'")) {
            processedContent = processedContent.substring(1, processedContent.length() - 1);
        }
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
        WeatherInfo weatherInfo = getWeather(robot);
        String weather = weatherInfo != null ? weatherInfo.getDescription() : "未知";
        String temperature = weatherInfo != null ? weatherInfo.getTemperature() : "";
        context.append(String.format("，天气%s, 温度%s", weather, temperature));
        
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

    /**
 * 获取机器人今日已生成的计划内容（可选）
 */
    private String buildTodayPlanContext(Robot robot, LocalDate planDate) {
        Optional<RobotDailyPlan> planOpt = planRepository.findByRobotIdAndPlanDate(robot.getRobotId(), planDate);
        if (planOpt.isPresent() && "SUCCESS".equals(planOpt.get().getStatus())) {
            RobotDailyPlan plan = planOpt.get();
            StringBuilder sb = new StringBuilder();
            sb.append("\n### 今日的计划\n");
            if (plan.getSlots() != null && !plan.getSlots().isEmpty()) {
                sb.append("- 时间段安排：\n");
                for (RobotDailyPlan.PlanSlot slot : plan.getSlots()) {
                    sb.append(slot.getStart()).append("-").append(slot.getEnd()).append("：");
                    if (slot.getEvents() != null) {
                        for (RobotDailyPlan.PlanEvent event : slot.getEvents()) {
                            sb.append(event.getContent());
                            if (event.getMood() != null) sb.append("（").append(event.getMood()).append("）");
                            sb.append("，");
                        }
                    }
                    sb.append("\n");
                }
            }
            // 当前时间段
            String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            RobotDailyPlan.PlanSlot currentSlot = plan.getSlots().stream()
                .filter(slot -> slot.getStart().compareTo(now) <= 0 && slot.getEnd().compareTo(now) > 0)
                .findFirst().orElse(null);
            if (currentSlot != null) {
                sb.append("\n### 当前时间 ").append(now).append("\n- 对应安排：")
                .append(currentSlot.getStart()).append("-").append(currentSlot.getEnd());
                if (currentSlot.getEvents() != null) {
                    for (RobotDailyPlan.PlanEvent event : currentSlot.getEvents()) {
                        sb.append(event.getContent());
                        if (event.getMood() != null) sb.append("（").append(event.getMood()).append("）");
                        sb.append("，");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        return "";
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
        info.append(String.format("\n- 家庭背景：%s", robot.getFamily() != null ? robot.getFamily() : "未知"));
        
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

    /**
     * 封装最终生成内容和link的结果对象
     */
    public static class PostContentResult {
        private String content;
        private Post.LinkInfo link;
        public PostContentResult(String content, Post.LinkInfo link) {
            this.content = content;
            this.link = link;
        }
        public String getContent() { return content; }
        public Post.LinkInfo getLink() { return link; }
    }

    @Override
    public PostContentResult generatePostContent(Robot robot, String context) {
        try {
            // 使用PromptService构建提示词和link
            PostPromptResult promptResult = buildPostPrompt(robot, context);
            // 调用Dify API
            String rawContent = difyService.callDifyApi(promptResult.getPrompt(), robot.getRobotId());
            // 使用PromptService处理生成的内容
            String content = processGeneratedContent(rawContent, robot, "post");
            return new PostContentResult(content, promptResult.getLink());
        } catch (Exception e) {
            log.error("生成机器人动态内容失败: {}", e.getMessage(), e);
            return new PostContentResult(generateFallbackPost(robot, context), null);
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
    
    /**
     * 获取机器人所在城市的天气信息（优先缓存，缺失则随机一个）
     *
     * @param robot 机器人实体，需包含location字段
     * @return WeatherInfo 天气信息对象，若无则返回null
     */
    private WeatherInfo getWeather(Robot robot) {
        // 获取缓存中的天气Map
        Map<String, WeatherInfo> weatherMap = externalDataCacheService.getWeatherMap();
        if (weatherMap != null) {
            String location = robot.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                WeatherInfo info = weatherMap.get(location.trim());
                if (info != null) {
                    // 找到对应城市天气
                    return info;
                }
            }

            // 未找到，随机返回一个已有城市的天气
            List<WeatherInfo> allWeather = new java.util.ArrayList<>(weatherMap.values());
            if (!allWeather.isEmpty()) {
                int idx = (int) (Math.random() * allWeather.size());
                return allWeather.get(idx);
            }
        }
        
        return null;
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
    public RobotConfig.Topic selectRandomTopic(Robot robot) {
        try {
            // 获取合并后的主题列表
            List<RobotConfig.Topic> mergedTopics = getMergedTopics(robot);
            
            if (mergedTopics.isEmpty()) {
                log.warn("机器人 {} 没有可用的主题，使用默认主题", robot.getName());
                return new RobotConfig.Topic("分享心情", 1, "分享你的心情", null);
            }
            
            // 根据频次创建权重列表
            List<RobotConfig.Topic> weightedTopics = new ArrayList<>();
            for (RobotConfig.Topic topic : mergedTopics) {
                // 根据频次重复添加主题，实现权重效果
                for (int i = 0; i < topic.getFrequency(); i++) {
                    weightedTopics.add(topic);
                }
            }
            
            // 随机选择一个主题
            RobotConfig.Topic selectedTopic = weightedTopics.get(random.nextInt(weightedTopics.size()));
            
            log.info("为机器人 {} 选择了主题: {}",
                    robot.getName(), selectedTopic.getName());
            
            return selectedTopic;
        } catch (Exception e) {
            log.error("为机器人 {} 选择主题失败: {}", robot.getName(), e.getMessage(), e);
            return new RobotConfig.Topic("分享心情", 1, "分享你的心情", null);
        }
    }
    
    @Override
    public List<RobotConfig.Topic> getMergedTopics(Robot robot) {
        List<RobotConfig.Topic> mergedTopics = new ArrayList<>();
        
        try {
            // 获取通用主题
            if (robotConfig.getBaseConfig() != null && robotConfig.getBaseConfig().getCommonTopic() != null) {
                for (RobotConfig.Topic commonTopic : robotConfig.getBaseConfig().getCommonTopic()) {
                    mergedTopics.add(commonTopic);
                }
            }
            
            // 获取机器人个人主题
            RobotConfig.RobotInfo robotInfo = findRobotInfo(robot.getRobotId());
            if (robotInfo != null && robotInfo.getTopic() != null) {
                for (RobotConfig.Topic personalTopic : robotInfo.getTopic()) {
                    mergedTopics.add(personalTopic);
                }
            }

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
        
        // 家庭背景 - 选择性获取
        List<String> familyInfo = getRobotValue(robot, "family", 1, 0.3);
        if (!familyInfo.isEmpty()) {
            background.append(String.format("\n### 家庭背景：%s", String.join("、", familyInfo)));
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
        String[] basicFields = {"gender", "age", "mbti", "bloodType", "zodiac", "occupation", "location", "education", "relationship", "family"};
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
            case "family": return "家庭背景";
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

    private String isHoliday(java.time.LocalDate planDate) {
        return planDate.getDayOfWeek().toString().equals("SATURDAY") || planDate.getDayOfWeek().toString().equals("SUNDAY") ? "周末" : "";
    }

    @Override
    public String buildDailyPlanPrompt(Robot robot, java.time.LocalDate planDate) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format(
            "你是一个居民，请为你生成 %s 的详细日常计划，包括一篇日记和当天的时间段安排。\n",
            planDate.toString() + ", 星期" + planDate.getDayOfWeek().toString() + ", " + isHoliday(planDate)
        ));
        // 拼接个人档案
        prompt.append(buildPersonalInfo(robot, null));
        prompt.append("\n");
        prompt.append(String.format("- 姓名：%s\n- 性格：%s\n- 兴趣：%s\n",
            robot.getName(),
            robot.getPersonality() != null ? robot.getPersonality() : "",            
            robot.getInterests() != null ? String.join(",", robot.getInterests()) : ""
        ));
        if (robot.getActiveHours() != null) {
            StringBuilder hours = new StringBuilder("\n- 活跃时间: ");
            for (int i = 0; i < robot.getActiveHours().size(); i++) {
                Robot.ActiveHours activeHours = robot.getActiveHours().get(i);
                hours.append(activeHours.getStart() + " - " + activeHours.getEnd() + ",");
            }
            prompt.append(hours.toString());
        }
        prompt.append("\n\n## 请注意：\n");
        prompt.append("1. 计划主要发生在活跃时间，但也可以略微不一致，内容要生活化、拟人化。\n");
        prompt.append("2. 每个时间段可包含多个事件，每个事件需有简短心情描述（如开心、难过等）。\n");
        prompt.append("3. 日记需反映当天整体心情和亮点, 100字以内。\n");
        prompt.append("4. 输出格式为JSON，包含diary字段和slots数组，每个slot含start、end、events（每个event含content、mood）\n");
        prompt.append("5. 注意区分工作日、周末和节假日，内容要有变化。\n");
        prompt.append("6. 事件可包含突发小插曲、情绪波动等细节。\n");
        prompt.append("7. 只返回JSON，不要其他解释。\n");

        prompt.append("\n\n## 范例:\n");
        prompt.append("""
{
  "diary": "今天早上可真叫有惊无险，公交车站了一半人就晕倒了，还是空调车呢，人就是直冒冷汗，然后眼前就黑了，亏得有好心人让座，我才得以安抵，现在才觉得上班族真是辛苦呀，穿得是套装加高跟鞋，衬衫加领带得，挤个公交车，湿了一身不说，像我这样的还带晕倒车上的，真是没到办公室，就已经历经磨难阿，可见白领不易啊，也看出身体本钱的重要性阿。",
  "slots": [
    {
      "start": "06:00",
      "end": "07:00",
      "events": [
        {"content": "早起洗漱", "mood": "平静"},
        {"content": "面膜不见了", "mood": "惊讶"},
        {"content": "化妆美美的", "mood": "开心"}
      ]
    },
    {
      "start": "07:00",
      "end": "08:30",
      "events": [
        {"content": "上班通勤", "mood": "平静"},
        {"content": "抢到座位", "mood": "满足"},
        {"content": "买了杯咖啡", "mood": "愉快"}
      ]
    },
    {
      "start": "08:30",
      "end": "12:00",
      "events": [
        {"content": "上午上班", "mood": "专注"},
        {"content": "老板又给安排了个新任务", "mood": "无奈"}
      ]
    },
    {
      "start": "12:00",
      "end": "13:00",
      "events": [
        {"content": "午休", "mood": "放松"}
      ]
    },
    {
      "start": "13:00",
      "end": "17:30",
      "events": [
        {"content": "下午上班", "mood": "努力"},
        {"content": "财务说报销单填错了", "mood": "郁闷"},
        {"content": "买了杯咖啡", "mood": "提神"}
      ]
    },
    {
      "start": "17:30",
      "end": "19:00",
      "events": [
        {"content": "回家", "mood": "轻松"},
        {"content": "给老奶奶让座", "mood": "温暖"}
      ]
    },
    {
      "start": "19:00",
      "end": "23:00",
      "events": [
        {"content": "晚上吃饭休息", "mood": "满足"},
        {"content": "做了碗面", "mood": "幸福"},
        {"content": "追剧", "mood": "放松"}
      ]
    },
    {
      "start": "23:00",
      "end": "23:30",
      "events": [
        {"content": "上床", "mood": "困倦"}
      ]
    }
  ]
}
""");

        return prompt.toString();
    }

    @Override
    public RobotDailyPlan generateDailyPlan(Robot robot, LocalDate planDate) {
        String prompt = buildDailyPlanPrompt(robot, planDate);
        int maxRetry = 10;
        Exception lastException = null;
        for (int i = 0; i < maxRetry; i++) {
            try {
                // 1. 调用Dify获取AI结果
                String aiResult = difyService.callDifyApi(prompt, robot.getId());
                // 2. 反序列化为RobotDailyPlan对象
                ObjectMapper mapper = new ObjectMapper();
                if (aiResult.indexOf("</think>\n") != -1) {
                    aiResult = aiResult.substring(aiResult.indexOf("</think>\n") + 9);
                }
                RobotDailyPlan plan = mapper.readValue(aiResult, RobotDailyPlan.class);
                // 3. 补充robotId/planDate等必要字段
                plan.setRobotId(robot.getId());
                plan.setPlanDate(planDate);
                plan.setCreatedAt(LocalDateTime.now());
                plan.setUpdatedAt(LocalDateTime.now());
                plan.setIsDeleted(false);
                return plan;
            } catch (Exception e) {
                lastException = e;
                log.error("AI调用失败:", e);
            }
        }
        // 多次失败，抛出异常
        throw new RuntimeException("AI生成每日计划失败", lastException);
    }

    /**
     * 根据dataType获取外部数据背景
     */
    private String getDataBackgroundByType(String dataType) {
        if (dataType == null) return "";
        Random rand = new Random();
        switch (dataType) {
            case "news":
                List<com.myeden.model.external.NewsItem> news = externalDataCacheService.getNews();
                if (news != null && !news.isEmpty()) {
                    com.myeden.model.external.NewsItem item = news.get(rand.nextInt(news.size()));
                    return "新闻: " + item.getTitle() + (item.getSummary() != null ? "：" + item.getSummary() : "");
                }
                return "";
            case "hot_search":
                List<HotSearchItem> hot = externalDataCacheService.getHotSearchItems();
                if (hot != null && !hot.isEmpty()) {
                    HotSearchItem item = hot.get(rand.nextInt(hot.size()));
                    return "今日热搜：" + item.getTitle() + (item.getSummary() != null ? "：" + item.getSummary() : "");
                }
                return "";
            case "music":
                List<com.myeden.model.external.MusicItem> music = externalDataCacheService.getMusic();
                if (music != null && !music.isEmpty()) {
                    com.myeden.model.external.MusicItem item = music.get(rand.nextInt(music.size()));
                    return "推荐歌曲：" + item.getTitle() + (item.getArtist() != null ? " - " + item.getArtist() : "");
                }
                return "";
            case "movie":
                List<com.myeden.model.external.MovieItem> movies = externalDataCacheService.getMovies();
                if (movies != null && !movies.isEmpty()) {
                    com.myeden.model.external.MovieItem item = movies.get(rand.nextInt(movies.size()));
                    return "推荐影视：" + item.getTitle();
                }
                return "";
            default:
                return "";
        }
    }

    /**
     * 根据dataType获取外部数据url
     */
    private String getDataUrlByType(String dataType) {
        if (dataType == null) return null;
        switch (dataType) {
            case "news":
                List<com.myeden.model.external.NewsItem> news = externalDataCacheService.getNews();
                return news != null && !news.isEmpty() ? news.get(0).getUrl() : null;
            case "music":
                List<com.myeden.model.external.MusicItem> music = externalDataCacheService.getMusic();
                return music != null && !music.isEmpty() ? music.get(0).getUrl() : null;
            case "movie":
                List<com.myeden.model.external.MovieItem> movies = externalDataCacheService.getMovies();
                return movies != null && !movies.isEmpty() ? movies.get(0).getUrl() : null;
            default:
                return null;
        }
    }

    /**
     * 根据dataType获取外部数据LinkInfo对象
     */
    private Post.LinkInfo getDataLinkInfoByType(String dataType) {
        if (dataType == null) return null;
        Post.LinkInfo link = new Post.LinkInfo();
        link.setDataType(dataType);
        switch (dataType) {
            case "news":
                List<com.myeden.model.external.NewsItem> news = externalDataCacheService.getNews();
                if (news != null && !news.isEmpty()) {
                    com.myeden.model.external.NewsItem item = news.get(0);
                    link.setUrl(item.getUrl());
                    link.setTitle(item.getTitle());
                    link.setImage(item.getImage());
                }
                break;
            case "hot_search":
                List<HotSearchItem> hot = externalDataCacheService.getHotSearchItems();
                if (hot != null && !hot.isEmpty()) {
                    HotSearchItem item = hot.get(0);
                    link.setUrl(item.getUrl());
                    link.setTitle(item.getTitle());
                    link.setImage(item.getImage());
                }
                break;
            case "music":
                List<com.myeden.model.external.MusicItem> music = externalDataCacheService.getMusic();
                if (music != null && !music.isEmpty()) {
                    com.myeden.model.external.MusicItem item = music.get(0);
                    link.setUrl(item.getUrl());
                    link.setTitle(item.getTitle());
                    link.setImage(item.getImage());
                }
                break;
            case "movie":
                List<com.myeden.model.external.MovieItem> movies = externalDataCacheService.getMovies();
                if (movies != null && !movies.isEmpty()) {
                    com.myeden.model.external.MovieItem item = movies.get(0);
                    link.setUrl(item.getUrl());
                    link.setTitle(item.getTitle());
                    // 可扩展图片字段
                }
                break;
            default:
                return null;
        }
        return (link.getUrl() != null) ? link : null;
    }
} 