package com.myeden.model.external;

/**
 * 新闻条目实体类
 * 用于存储新闻的标题、摘要和链接等信息
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
public class NewsItem {
    /** 新闻标题 */
    private String title;
    /** 新闻摘要 */
    private String summary;
    /** 新闻原文链接 */
    private String url;

    /**
     * 获取新闻标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置新闻标题
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取新闻摘要
     * @return 摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置新闻摘要
     * @param summary 摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取新闻原文链接
     * @return 链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置新闻原文链接
     * @param url 链接
     */
    public void setUrl(String url) {
        this.url = url;
    }
} 