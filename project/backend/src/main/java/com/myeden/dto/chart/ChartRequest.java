package com.myeden.dto.chart;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 图表生成请求对象
 */
public class ChartRequest {
    
    /**
     * 图表引擎 (matplotlib, plotly, seaborn)
     */
    private String engine;
    
    /**
     * 图表类型 (line, bar, pie, scatter, heatmap)
     */
    @JsonProperty("chart_type")
    private String chartType;
    
    /**
     * 图表数据
     */
    private Map<String, Object> data;
    
    /**
     * 图表配置
     */
    @JsonProperty("chart_config")
    private Map<String, Object> chartConfig;
    
    /**
     * 会话ID（用于文件管理）
     */
    @JsonProperty("session_id")
    private String sessionId;
    
    public ChartRequest() {}
    
    public ChartRequest(String engine, String chartType, Map<String, Object> data, Map<String, Object> chartConfig) {
        this.engine = engine;
        this.chartType = chartType;
        this.data = data;
        this.chartConfig = chartConfig;
    }
    
    // Getters and Setters
    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    public String getChartType() {
        return chartType;
    }
    
    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Map<String, Object> getChartConfig() {
        return chartConfig;
    }
    
    public void setChartConfig(Map<String, Object> chartConfig) {
        this.chartConfig = chartConfig;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    @Override
    public String toString() {
        return "ChartRequest{" +
                "engine='" + engine + '\'' +
                ", chartType='" + chartType + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", dataKeys=" + (data != null ? data.keySet() : null) +
                ", configKeys=" + (chartConfig != null ? chartConfig.keySet() : null) +
                '}';
    }
}