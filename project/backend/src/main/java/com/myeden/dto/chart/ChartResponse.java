package com.myeden.dto.chart;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 图表生成响应对象
 */
public class ChartResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（失败时）
     */
    private String error;
    
    /**
     * 输出文件路径
     */
    @JsonProperty("output_path")
    private String outputPath;
    
    /**
     * 相对路径
     */
    @JsonProperty("relative_path")
    private String relativePath;
    
    /**
     * 文件名
     */
    private String filename;
    
    /**
     * 文件大小（字节）
     */
    @JsonProperty("file_size")
    private Long fileSize;
    
    /**
     * 图表类型
     */
    @JsonProperty("chart_type")
    private String chartType;
    
    /**
     * 图表引擎
     */
    private String engine;
    
    /**
     * 会话ID
     */
    @JsonProperty("session_id")
    private String sessionId;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 错误堆栈信息（调试用）
     */
    private String traceback;
    
    public ChartResponse() {}
    
    public static ChartResponse success(String outputPath, String filename) {
        ChartResponse response = new ChartResponse();
        response.success = true;
        response.outputPath = outputPath;
        response.filename = filename;
        return response;
    }
    
    public static ChartResponse error(String error) {
        ChartResponse response = new ChartResponse();
        response.success = false;
        response.error = error;
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getChartType() {
        return chartType;
    }
    
    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
    
    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
    
    public String getTraceback() {
        return traceback;
    }
    
    public void setTraceback(String traceback) {
        this.traceback = traceback;
    }
    
    @Override
    public String toString() {
        return "ChartResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", filename='" + filename + '\'' +
                ", fileSize=" + fileSize +
                ", chartType='" + chartType + '\'' +
                ", engine='" + engine + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", processingTime=" + processingTime +
                '}';
    }
}