package com.myeden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 我的伊甸园 - 主启动类
 * 
 * 功能说明：
 * - 启动Spring Boot应用
 * - 启用定时任务调度
 * - 自动配置所有Spring Boot组件
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableScheduling
public class MyEdenApplication {

    /**
     * 应用程序入口点
     * 
     * 启动流程：
     * 1. 初始化Spring容器
     * 2. 加载配置文件
     * 3. 启动Web服务器
     * 4. 初始化数据库连接
     * 5. 启动定时任务
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MyEdenApplication.class, args);
        System.out.println("=================================");
        System.out.println("我的伊甸园(My-Eden) 启动成功！");
        System.out.println("欢迎来到这个充满爱与温暖的虚拟世界");
        System.out.println("=================================");
    }
} 