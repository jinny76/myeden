package com.myeden.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Arrays;

/**
 * 性能监控配置类
 * 负责Prometheus指标、健康检查、性能监控配置
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringConfig.class);

    /**
     * 配置MeterRegistry
     * 用于收集和导出应用指标
     */
    @Bean
    public MeterRegistry meterRegistry() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        logger.info("MeterRegistry配置完成");
        return registry;
    }

    /**
     * 配置TimedAspect
     * 用于自动记录方法执行时间
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        TimedAspect aspect = new TimedAspect(meterRegistry);
        logger.info("TimedAspect配置完成");
        return aspect;
    }

    /**
     * 性能指标收集器
     * 用于收集应用性能指标
     */
    @Bean
    public PerformanceMetricsCollector performanceMetricsCollector(MeterRegistry meterRegistry) {
        PerformanceMetricsCollector collector = new PerformanceMetricsCollector(meterRegistry);
        logger.info("性能指标收集器配置完成");
        return collector;
    }

    /**
     * 性能指标收集器内部类
     * 负责收集和记录各种性能指标
     */
    public static class PerformanceMetricsCollector {

        private final MeterRegistry meterRegistry;
        private final Logger logger = LoggerFactory.getLogger(PerformanceMetricsCollector.class);

        public PerformanceMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * 记录API调用次数
         */
        public void recordApiCall(String endpoint, String method, int statusCode) {
            try {
                meterRegistry.counter("api.calls", 
                    "endpoint", endpoint,
                    "method", method,
                    "status", String.valueOf(statusCode)
                ).increment();
            } catch (Exception e) {
                logger.error("记录API调用指标失败", e);
            }
        }

        /**
         * 记录API响应时间
         */
        public void recordApiResponseTime(String endpoint, String method, long durationMs) {
            try {
                meterRegistry.timer("api.response.time",
                    "endpoint", endpoint,
                    "method", method
                ).record(java.time.Duration.ofMillis(durationMs));
            } catch (Exception e) {
                logger.error("记录API响应时间指标失败", e);
            }
        }

        /**
         * 记录数据库查询次数
         */
        public void recordDatabaseQuery(String collection, String operation) {
            try {
                meterRegistry.counter("database.queries",
                    "collection", collection,
                    "operation", operation
                ).increment();
            } catch (Exception e) {
                logger.error("记录数据库查询指标失败", e);
            }
        }

        /**
         * 记录数据库查询时间
         */
        public void recordDatabaseQueryTime(String collection, String operation, long durationMs) {
            try {
                meterRegistry.timer("database.query.time",
                    "collection", collection,
                    "operation", operation
                ).record(java.time.Duration.ofMillis(durationMs));
            } catch (Exception e) {
                logger.error("记录数据库查询时间指标失败", e);
            }
        }

        /**
         * 记录缓存命中率
         */
        public void recordCacheHit(String cacheName) {
            try {
                meterRegistry.counter("cache.hits",
                    "cache", cacheName
                ).increment();
            } catch (Exception e) {
                logger.error("记录缓存命中指标失败", e);
            }
        }

        /**
         * 记录缓存未命中
         */
        public void recordCacheMiss(String cacheName) {
            try {
                meterRegistry.counter("cache.misses",
                    "cache", cacheName
                ).increment();
            } catch (Exception e) {
                logger.error("记录缓存未命中指标失败", e);
            }
        }

        /**
         * 记录WebSocket连接数
         */
        public void recordWebSocketConnection(String status) {
            try {
                meterRegistry.counter("websocket.connections",
                    "status", status
                ).increment();
            } catch (Exception e) {
                logger.error("记录WebSocket连接指标失败", e);
            }
        }

        /**
         * 记录WebSocket消息数
         */
        public void recordWebSocketMessage(String type) {
            try {
                meterRegistry.counter("websocket.messages",
                    "type", type
                ).increment();
            } catch (Exception e) {
                logger.error("记录WebSocket消息指标失败", e);
            }
        }

        /**
         * 记录AI API调用次数
         */
        public void recordAiApiCall(String apiType, String status) {
            try {
                meterRegistry.counter("ai.api.calls",
                    "type", apiType,
                    "status", status
                ).increment();
            } catch (Exception e) {
                logger.error("记录AI API调用指标失败", e);
            }
        }

        /**
         * 记录AI API响应时间
         */
        public void recordAiApiResponseTime(String apiType, long durationMs) {
            try {
                meterRegistry.timer("ai.api.response.time",
                    "type", apiType
                ).record(java.time.Duration.ofMillis(durationMs));
            } catch (Exception e) {
                logger.error("记录AI API响应时间指标失败", e);
            }
        }

        /**
         * 记录文件上传次数
         */
        public void recordFileUpload(String fileType, long fileSize) {
            try {
                meterRegistry.counter("file.uploads",
                    "type", fileType
                ).increment();
                
                meterRegistry.summary("file.upload.size",
                    "type", fileType
                ).record(fileSize);
            } catch (Exception e) {
                logger.error("记录文件上传指标失败", e);
            }
        }

        /**
         * 记录用户活跃度
         */
        public void recordUserActivity(String activityType) {
            try {
                meterRegistry.counter("user.activity",
                    "type", activityType
                ).increment();
            } catch (Exception e) {
                logger.error("记录用户活跃度指标失败", e);
            }
        }

        /**
         * 记录机器人行为次数
         */
        public void recordRobotBehavior(String behaviorType) {
            try {
                meterRegistry.counter("robot.behavior",
                    "type", behaviorType
                ).increment();
            } catch (Exception e) {
                logger.error("记录机器人行为指标失败", e);
            }
        }

        /**
         * 记录错误次数
         */
        public void recordError(String errorType, String component) {
            try {
                meterRegistry.counter("errors",
                    "type", errorType,
                    "component", component
                ).increment();
            } catch (Exception e) {
                logger.error("记录错误指标失败", e);
            }
        }

        /**
         * 记录内存使用情况
         */
        public void recordMemoryUsage() {
            try {
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                
                meterRegistry.gauge("memory.used", usedMemory);
                meterRegistry.gauge("memory.free", freeMemory);
                meterRegistry.gauge("memory.total", totalMemory);
                meterRegistry.gauge("memory.usage.percent", 
                    (double) usedMemory / totalMemory * 100);
            } catch (Exception e) {
                logger.error("记录内存使用指标失败", e);
            }
        }

        /**
         * 记录线程池状态
         */
        public void recordThreadPoolStatus(String poolName, int activeThreads, int queueSize) {
            try {
                meterRegistry.gauge("threadpool.active.threads",
                    Arrays.asList(Tag.of("pool", poolName)), activeThreads);
                meterRegistry.gauge("threadpool.queue.size",
                    Arrays.asList(Tag.of("pool", poolName)), queueSize);
            } catch (Exception e) {
                logger.error("记录线程池状态指标失败", e);
            }
        }

        /**
         * 获取指标统计信息
         */
        public String getMetricsSummary() {
            try {
                StringBuilder summary = new StringBuilder();
                summary.append("=== 性能指标统计 ===\n");
                
                // 这里可以添加更多指标统计逻辑
                summary.append("指标收集器运行正常\n");
                
                return summary.toString();
            } catch (Exception e) {
                logger.error("获取指标统计信息失败", e);
                return "指标统计获取失败: " + e.getMessage();
            }
        }
    }
} 