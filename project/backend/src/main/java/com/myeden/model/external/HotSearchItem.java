package com.myeden.model.external;

import lombok.Data;

/**
 * 热搜条目实体类
 * 用于存储热搜的标题、摘要、链接和图片等信息
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Data
public class HotSearchItem {
    /** 热搜标题 */
    private String title;
    /** 热搜摘要 */
    private String summary;
    /** 热搜原文链接 */
    private String url;
    /** 热搜图片URL */
    private String image;

    /**
     * 获取热搜标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置热搜标题
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取热搜摘要
     * @return 摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置热搜摘要
     * @param summary 摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取热搜原文链接
     * @return 链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置热搜原文链接
     * @param url 链接
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取热搜图片URL
     * @return 图片URL
     */
    public String getImage() {
        return image;
    }

    /**
     * 设置热搜图片URL
     * @param image 图片URL
     */
    public void setImage(String image) {
        this.image = image;
    }
} 