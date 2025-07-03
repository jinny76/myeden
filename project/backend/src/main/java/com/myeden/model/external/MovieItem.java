package com.myeden.model.external;

/**
 * 影视条目实体类
 * 用于存储影视的标题、简介和链接等信息
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
public class MovieItem {
    /** 影视标题 */
    private String title;
    /** 影视简介 */
    private String description;
    /** 影视链接 */
    private String url;

    /**
     * 获取影视标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置影视标题
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取影视简介
     * @return 简介
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置影视简介
     * @param description 简介
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取影视链接
     * @return 链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置影视链接
     * @param url 链接
     */
    public void setUrl(String url) {
        this.url = url;
    }
} 