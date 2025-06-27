package com.myeden.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步处理配置类
 * 负责线程池配置、异步任务管理、定时任务调度
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${async.core-pool-size:10}")
    private int corePoolSize;

    @Value("${async.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${async.thread-name-prefix:myeden-async-}")
    private String threadNamePrefix;

    /**
     * 通用异步任务执行器
     * 用于处理一般的异步任务，如文件上传、邮件发送等
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程名前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        logger.info("通用异步任务执行器配置完成，核心线程数: {}, 最大线程数: {}", corePoolSize, maxPoolSize);
        return executor;
    }

    /**
     * AI任务执行器
     * 专门用于处理AI相关的异步任务，如内容生成、机器人行为等
     */
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // AI任务使用较小的线程池，避免API调用过于频繁
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("myeden-ai-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        logger.info("AI任务执行器配置完成");
        return executor;
    }

    /**
     * 文件处理执行器
     * 专门用于处理文件上传、图片处理等IO密集型任务
     */
    @Bean("fileTaskExecutor")
    public Executor fileTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 文件处理使用较大的线程池，因为IO操作较多
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("myeden-file-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        logger.info("文件处理执行器配置完成");
        return executor;
    }

    /**
     * 定时任务调度器
     * 用于处理定时任务，如机器人行为检查、缓存清理等
     */
    @Bean("taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 定时任务使用较小的线程池
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("myeden-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setErrorHandler(throwable -> 
            logger.error("定时任务执行异常", throwable));
        
        scheduler.initialize();
        logger.info("定时任务调度器配置完成");
        return scheduler;
    }

    /**
     * WebSocket消息处理执行器
     * 专门用于处理WebSocket消息的异步发送
     */
    @Bean("websocketTaskExecutor")
    public Executor websocketTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // WebSocket消息处理使用中等大小的线程池
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("myeden-ws-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        logger.info("WebSocket任务执行器配置完成");
        return executor;
    }

    /**
     * 数据库操作执行器
     * 专门用于处理数据库相关的异步操作
     */
    @Bean("dbTaskExecutor")
    public Executor dbTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 数据库操作使用较小的线程池，避免数据库连接过多
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(80);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("myeden-db-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        logger.info("数据库任务执行器配置完成");
        return executor;
    }

    /**
     * 获取线程池统计信息
     * 用于监控线程池使用情况
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        return (ThreadPoolTaskExecutor) taskExecutor();
    }

    public ThreadPoolTaskExecutor getAiTaskExecutor() {
        return (ThreadPoolTaskExecutor) aiTaskExecutor();
    }

    public ThreadPoolTaskExecutor getFileTaskExecutor() {
        return (ThreadPoolTaskExecutor) fileTaskExecutor();
    }

    public ThreadPoolTaskExecutor getWebsocketTaskExecutor() {
        return (ThreadPoolTaskExecutor) websocketTaskExecutor();
    }

    public ThreadPoolTaskExecutor getDbTaskExecutor() {
        return (ThreadPoolTaskExecutor) dbTaskExecutor();
    }

    /**
     * 打印线程池状态信息
     * 用于调试和监控
     */
    public void printThreadPoolStatus() {
        try {
            ThreadPoolTaskExecutor taskExecutor = getTaskExecutor();
            ThreadPoolTaskExecutor aiExecutor = getAiTaskExecutor();
            ThreadPoolTaskExecutor fileExecutor = getFileTaskExecutor();
            ThreadPoolTaskExecutor wsExecutor = getWebsocketTaskExecutor();
            ThreadPoolTaskExecutor dbExecutor = getDbTaskExecutor();

            logger.info("=== 线程池状态信息 ===");
            logger.info("通用任务执行器 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                taskExecutor.getActiveCount(), 
                taskExecutor.getThreadPoolExecutor().getQueue().size(),
                taskExecutor.getThreadPoolExecutor().getCompletedTaskCount());
            
            logger.info("AI任务执行器 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                aiExecutor.getActiveCount(), 
                aiExecutor.getThreadPoolExecutor().getQueue().size(),
                aiExecutor.getThreadPoolExecutor().getCompletedTaskCount());
            
            logger.info("文件处理执行器 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                fileExecutor.getActiveCount(), 
                fileExecutor.getThreadPoolExecutor().getQueue().size(),
                fileExecutor.getThreadPoolExecutor().getCompletedTaskCount());
            
            logger.info("WebSocket执行器 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                wsExecutor.getActiveCount(), 
                wsExecutor.getThreadPoolExecutor().getQueue().size(),
                wsExecutor.getThreadPoolExecutor().getCompletedTaskCount());
            
            logger.info("数据库执行器 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                dbExecutor.getActiveCount(), 
                dbExecutor.getThreadPoolExecutor().getQueue().size(),
                dbExecutor.getThreadPoolExecutor().getCompletedTaskCount());
            
        } catch (Exception e) {
            logger.error("获取线程池状态信息失败", e);
        }
    }
} 