package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * 全局概述：
 * ContentGenerationLog用于记录每次AI内容生成的详细日志，便于追溯和分析。
 * 包含完整Robot对象、生成用的prompt、生成时间、生成结果等。
 */
@Document(collection = "content_generation_logs")
public class ContentGenerationLog {
    @Id
    private String id;

    /** 机器人完整对象 */
    private Robot robot;
    /** 生成用的prompt */
    private String prompt;
    /** 生成内容的原始结果 */
    private String rawContent;
    /** 生成时间 */
    private LocalDateTime generatedAt;
    /** 生成类型（如post, comment等） */
    private String type;
    /** 生成上下文（可选） */
    private String context;

    /**
     * 构造方法
     * @param robot 机器人完整对象
     * @param prompt 生成用的prompt
     * @param rawContent 生成内容的原始结果
     * @param generatedAt 生成时间
     * @param type 生成类型
     * @param context 生成上下文
     */
    public ContentGenerationLog(Robot robot, String prompt, String rawContent, LocalDateTime generatedAt, String type, String context) {
        this.robot = robot;
        this.prompt = prompt;
        this.rawContent = rawContent;
        this.generatedAt = generatedAt;
        this.type = type;
        this.context = context;
    }

    public ContentGenerationLog() {}

    /**
     * 获取日志ID
     * @return 日志ID
     */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /**
     * 获取机器人完整对象
     * @return Robot对象
     */
    public Robot getRobot() { return robot; }
    public void setRobot(Robot robot) { this.robot = robot; }

    /**
     * 获取生成用的prompt
     * @return prompt字符串
     */
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    /**
     * 获取原始生成内容
     * @return 原始内容
     */
    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    /**
     * 获取生成时间
     * @return 生成时间
     */
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    /**
     * 获取生成类型
     * @return 类型字符串
     */
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /**
     * 获取上下文
     * @return 上下文字符串
     */
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
} 