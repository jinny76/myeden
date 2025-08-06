package com.myeden.service;

import java.util.List;

/**
 * 天气通知服务接口
 */
public interface WeatherNotificationService {
    
    /**
     * 推送天气预报到微信
     * 
     * @param cities 城市列表
     * @param users 用户列表
     * @return 推送成功数量
     */
    int pushWeatherToWechat(List<String> cities, List<String> users);
    
    /**
     * 推送单个城市天气给所有配置用户
     * 
     * @param city 城市名称
     * @return 推送成功数量
     */
    int pushCityWeatherToAllUsers(String city);
    
    /**
     * 推送所有配置城市天气给单个用户
     * 
     * @param user 用户ID
     * @return 推送成功数量
     */
    int pushAllCityWeatherToUser(String user);
    
    /**
     * 获取天气推送任务的状态
     * 
     * @return 任务是否启用
     */
    boolean isWeatherTaskEnabled();
    
    /**
     * 启用或禁用天气推送任务
     * 
     * @param enabled 是否启用
     */
    void setWeatherTaskEnabled(boolean enabled);
}