package com.myeden.model.external;

import lombok.Data;

/**
 * 天气信息实体类
 * 用于存储城市天气的描述和温度等信息
 *
 * @author MyEden Team
 * @version 1.0
 * @since 2024-07-03
 */
@Data
public class WeatherInfo {
    /** 城市名称 */
    private String city;
    /** 天气描述（如晴、阴、小雨等） */
    private String description;
    /** 温度（如"28℃"） */
    private String temperature;

    /**
     * 获取城市名称
     * @return 城市
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置城市名称
     * @param city 城市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取天气描述
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置天气描述
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取温度
     * @return 温度
     */
    public String getTemperature() {
        return temperature;
    }

    /**
     * 设置温度
     * @param temperature 温度
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
} 