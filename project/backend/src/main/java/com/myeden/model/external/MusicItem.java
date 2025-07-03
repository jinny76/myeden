package com.myeden.model.external;

/**
 * 音乐条目实体类
 * 用于存储音乐的标题、歌手和链接等信息
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
public class MusicItem {
    /** 歌曲标题 */
    private String title;
    /** 歌手名 */
    private String artist;
    /** 歌曲链接 */
    private String url;

    /**
     * 获取歌曲标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置歌曲标题
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取歌手名
     * @return 歌手
     */
    public String getArtist() {
        return artist;
    }

    /**
     * 设置歌手名
     * @param artist 歌手
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * 获取歌曲链接
     * @return 链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置歌曲链接
     * @param url 链接
     */
    public void setUrl(String url) {
        this.url = url;
    }
} 