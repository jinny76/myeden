package com.myeden.controller;

import com.myeden.dto.chart.ChartRequest;
import com.myeden.dto.chart.ChartResponse;
import com.myeden.service.ChartGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 图表生成控制器
 */
@RestController
@RequestMapping("/api/v1/charts")
@Tag(name = "图表生成", description = "图表生成和下载接口")
public class ChartController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChartController.class);
    
    private final ChartGenerationService chartGenerationService;
    
    @Autowired
    public ChartController(ChartGenerationService chartGenerationService) {
        this.chartGenerationService = chartGenerationService;
    }
    
    /**
     * 生成图表
     */
    @Operation(summary = "生成图表", description = "根据提供的数据和配置生成图表")
    @ApiResponse(responseCode = "200", description = "图表生成成功")
    @PostMapping("/generate")
    public ResponseEntity<?> generateChart(@RequestBody ChartRequest request) {
        try {
            logger.info("收到图表生成请求: {}", request);
            
            // 验证请求
            if (!chartGenerationService.validateChartConfig(request)) {
                return ResponseEntity.badRequest()
                    .body(EventResponse.error("图表配置验证失败"));
            }
            
            // 生成图表
            ChartResponse response = chartGenerationService.generateChart(request);
            
            if (response.isSuccess()) {
                logger.info("图表生成成功: {}", response.getFilename());
                return ResponseEntity.ok(EventResponse.success(response, "图表生成成功"));
            } else {
                logger.error("图表生成失败: {}", response.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EventResponse.error("图表生成失败: " + response.getError()));
            }
            
        } catch (Exception e) {
            logger.error("图表生成时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventResponse.error("图表生成时出现异常: " + e.getMessage()));
        }
    }
    
    /**
     * 批量生成图表
     */
    @Operation(summary = "批量生成图表", description = "批量生成多个图表")
    @ApiResponse(responseCode = "200", description = "批量生成完成")
    @PostMapping("/batch")
    public ResponseEntity<?> generateBatchCharts(@RequestBody BatchChartRequest batchRequest) {
        try {
            logger.info("收到批量图表生成请求，数量: {}", batchRequest.getCharts().size());
            
            List<ChartResponse> results = new ArrayList<>();
            String sessionId = UUID.randomUUID().toString();
            
            for (int i = 0; i < batchRequest.getCharts().size(); i++) {
                ChartRequest request = batchRequest.getCharts().get(i);
                request.setSessionId(sessionId);
                
                try {
                    if (chartGenerationService.validateChartConfig(request)) {
                        ChartResponse response = chartGenerationService.generateChart(request);
                        response.setSessionId(sessionId);
                        results.add(response);
                    } else {
                        ChartResponse errorResponse = ChartResponse.error("配置验证失败");
                        errorResponse.setSessionId(sessionId);
                        results.add(errorResponse);
                    }
                } catch (Exception e) {
                    ChartResponse errorResponse = ChartResponse.error("生成失败: " + e.getMessage());
                    errorResponse.setSessionId(sessionId);
                    results.add(errorResponse);
                }
            }
            
            int successful = (int) results.stream().filter(ChartResponse::isSuccess).count();
            int failed = results.size() - successful;
            
            BatchChartResponse batchResponse = new BatchChartResponse();
            batchResponse.setSessionId(sessionId);
            batchResponse.setTotal(results.size());
            batchResponse.setSuccessful(successful);
            batchResponse.setFailed(failed);
            batchResponse.setResults(results);
            
            logger.info("批量图表生成完成: 成功={}, 失败={}", successful, failed);
            
            return ResponseEntity.ok(EventResponse.success(batchResponse, "批量生成完成"));
            
        } catch (Exception e) {
            logger.error("批量图表生成时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventResponse.error("批量生成时出现异常: " + e.getMessage()));
        }
    }
    
    /**
     * 下载图表文件
     */
    @Operation(summary = "下载图表文件", description = "根据文件路径下载生成的图表文件")
    @ApiResponse(responseCode = "200", description = "文件下载成功")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadChart(
            @Parameter(description = "文件相对路径", required = true)
            @RequestParam String filePath) {
        try {
            logger.info("下载图表文件: {}", filePath);
            
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String filename = file.getFileName().toString();
                String contentType = getContentType(filename);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                logger.error("文件不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("下载文件时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 验证图表配置
     */
    @Operation(summary = "验证图表配置", description = "验证图表配置是否正确")
    @ApiResponse(responseCode = "200", description = "验证完成")
    @PostMapping("/validate")
    public ResponseEntity<?> validateChart(@RequestBody ChartRequest request) {
        try {
            boolean isValid = chartGenerationService.validateChartConfig(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            result.put("message", isValid ? "配置有效" : "配置无效");
            
            if (isValid) {
                return ResponseEntity.ok(EventResponse.success(result, "验证通过"));
            } else {
                return ResponseEntity.badRequest()
                    .body(EventResponse.error("配置验证失败"));
            }
            
        } catch (Exception e) {
            logger.error("验证配置时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventResponse.error("验证时出现异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取系统状态
     */
    @Operation(summary = "获取系统状态", description = "获取图表生成系统的状态信息")
    @ApiResponse(responseCode = "200", description = "状态获取成功")
    @GetMapping("/status")
    public ResponseEntity<?> getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 检查Python环境
            boolean pythonAvailable = chartGenerationService.checkPythonEnvironment();
            status.put("python_available", pythonAvailable);
            
            // 获取支持的图表类型和引擎
            status.put("supported_engines", Arrays.asList(chartGenerationService.getSupportedEngines()));
            status.put("supported_chart_types", Arrays.asList(chartGenerationService.getSupportedChartTypes()));
            
            // 系统信息
            status.put("java_version", System.getProperty("java.version"));
            status.put("os_name", System.getProperty("os.name"));
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(EventResponse.success(status, "状态获取成功"));
            
        } catch (Exception e) {
            logger.error("获取系统状态时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventResponse.error("获取状态时出现异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取图表配置模板
     */
    @Operation(summary = "获取图表配置模板", description = "获取不同类型图表的配置模板")
    @ApiResponse(responseCode = "200", description = "模板获取成功")
    @GetMapping("/templates")
    public ResponseEntity<?> getChartTemplates(
            @Parameter(description = "图表类型", required = false)
            @RequestParam(required = false) String chartType) {
        try {
            Map<String, Object> templates = new HashMap<>();
            
            if (chartType == null) {
                // 返回所有模板
                templates.put("line", createLineChartTemplate());
                templates.put("bar", createBarChartTemplate());
                templates.put("pie", createPieChartTemplate());
                templates.put("scatter", createScatterChartTemplate());
                templates.put("heatmap", createHeatmapChartTemplate());
            } else {
                // 返回指定类型的模板
                switch (chartType) {
                    case "line":
                        templates.put("line", createLineChartTemplate());
                        break;
                    case "bar":
                        templates.put("bar", createBarChartTemplate());
                        break;
                    case "pie":
                        templates.put("pie", createPieChartTemplate());
                        break;
                    case "scatter":
                        templates.put("scatter", createScatterChartTemplate());
                        break;
                    case "heatmap":
                        templates.put("heatmap", createHeatmapChartTemplate());
                        break;
                    default:
                        return ResponseEntity.badRequest()
                            .body(EventResponse.error("不支持的图表类型: " + chartType));
                }
            }
            
            return ResponseEntity.ok(EventResponse.success(templates, "模板获取成功"));
            
        } catch (Exception e) {
            logger.error("获取图表模板时出现异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventResponse.error("获取模板时出现异常: " + e.getMessage()));
        }
    }
    
    // 辅助方法
    
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "svg":
                return "image/svg+xml";
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }
    
    private ChartRequest createLineChartTemplate() {
        ChartRequest template = new ChartRequest();
        template.setEngine("matplotlib");
        template.setChartType("line");
        
        Map<String, Object> data = new HashMap<>();
        data.put("x", Arrays.asList(1, 2, 3, 4, 5));
        data.put("y", Arrays.asList(10, 20, 15, 25, 30));
        template.setData(data);
        
        Map<String, Object> config = new HashMap<>();
        config.put("x_column", "x");
        config.put("y_columns", "y");
        config.put("title", "折线图示例");
        config.put("xlabel", "X轴");
        config.put("ylabel", "Y轴");
        template.setChartConfig(config);
        
        return template;
    }
    
    private ChartRequest createBarChartTemplate() {
        ChartRequest template = new ChartRequest();
        template.setEngine("seaborn");
        template.setChartType("bar");
        
        Map<String, Object> data = new HashMap<>();
        data.put("category", Arrays.asList("A", "B", "C", "D"));
        data.put("values", Arrays.asList(23, 45, 56, 78));
        template.setData(data);
        
        Map<String, Object> config = new HashMap<>();
        config.put("x_column", "category");
        config.put("y_column", "values");
        config.put("title", "柱状图示例");
        template.setChartConfig(config);
        
        return template;
    }
    
    private ChartRequest createPieChartTemplate() {
        ChartRequest template = new ChartRequest();
        template.setEngine("matplotlib");
        template.setChartType("pie");
        
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("苹果", "香蕉", "橙子", "葡萄"));
        data.put("values", Arrays.asList(30, 25, 20, 25));
        template.setData(data);
        
        Map<String, Object> config = new HashMap<>();
        config.put("labels_column", "labels");
        config.put("values_column", "values");
        config.put("title", "饼图示例");
        template.setChartConfig(config);
        
        return template;
    }
    
    private ChartRequest createScatterChartTemplate() {
        ChartRequest template = new ChartRequest();
        template.setEngine("plotly");
        template.setChartType("scatter");
        
        Map<String, Object> data = new HashMap<>();
        data.put("x", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        data.put("y", Arrays.asList(2, 5, 3, 8, 7, 10, 12, 6, 4, 9));
        template.setData(data);
        
        Map<String, Object> config = new HashMap<>();
        config.put("x_column", "x");
        config.put("y_column", "y");
        config.put("title", "散点图示例");
        template.setChartConfig(config);
        
        return template;
    }
    
    private ChartRequest createHeatmapChartTemplate() {
        ChartRequest template = new ChartRequest();
        template.setEngine("seaborn");
        template.setChartType("heatmap");
        
        // 创建示例矩阵数据
        List<List<Double>> matrix = Arrays.asList(
            Arrays.asList(1.0, 0.8, 0.3, 0.1),
            Arrays.asList(0.8, 1.0, 0.5, 0.2),
            Arrays.asList(0.3, 0.5, 1.0, 0.7),
            Arrays.asList(0.1, 0.2, 0.7, 1.0)
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("data", matrix);
        template.setData(data);
        
        Map<String, Object> config = new HashMap<>();
        config.put("title", "热力图示例");
        config.put("cmap", "coolwarm");
        template.setChartConfig(config);
        
        return template;
    }
    
    // 内部类
    
    public static class BatchChartRequest {
        private List<ChartRequest> charts;
        
        public List<ChartRequest> getCharts() {
            return charts;
        }
        
        public void setCharts(List<ChartRequest> charts) {
            this.charts = charts;
        }
    }
    
    public static class BatchChartResponse {
        private String sessionId;
        private int total;
        private int successful;
        private int failed;
        private List<ChartResponse> results;
        
        // Getters and Setters
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public int getSuccessful() {
            return successful;
        }
        
        public void setSuccessful(int successful) {
            this.successful = successful;
        }
        
        public int getFailed() {
            return failed;
        }
        
        public void setFailed(int failed) {
            this.failed = failed;
        }
        
        public List<ChartResponse> getResults() {
            return results;
        }
        
        public void setResults(List<ChartResponse> results) {
            this.results = results;
        }
    }
}