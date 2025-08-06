package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.dto.chart.ChartRequest;
import com.myeden.dto.chart.ChartResponse;
import com.myeden.service.ChartGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图表生成服务实现类
 * 支持多种调用方式：命令行、HTTP API
 */
@Service
public class ChartGenerationServiceImpl implements ChartGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChartGenerationServiceImpl.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Python可执行文件路径
     */
    @Value("${chart.python.executable:python}")
    private String pythonExecutable;
    
    /**
     * Python脚本路径
     */
    @Value("${chart.python.script.path:../project/pytools/src/cli.py}")
    private String pythonScriptPath;
    
    /**
     * API服务器URL
     */
    @Value("${chart.api.url:http://localhost:5000}")
    private String apiUrl;
    
    /**
     * 输出目录
     */
    @Value("${chart.output.dir:output/charts}")
    private String outputDir;
    
    /**
     * 调用方式：cli（命令行）或 api（HTTP API）
     */
    @Value("${chart.call.mode:cli}")
    private String callMode;
    
    /**
     * 命令超时时间（秒）
     */
    @Value("${chart.timeout.seconds:60}")
    private long timeoutSeconds;
    
    @Override
    public ChartResponse generateChart(ChartRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 确保输出目录存在
            ensureOutputDirectory();
            
            // 生成会话ID
            if (request.getSessionId() == null) {
                request.setSessionId(UUID.randomUUID().toString());
            }
            
            ChartResponse response;
            if ("api".equalsIgnoreCase(callMode)) {
                response = generateChartViaAPI(request);
            } else {
                response = generateChartViaCLI(request);
            }
            
            // 设置处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            
            logger.info("图表生成完成: {}, 耗时: {}ms", response.getFilename(), processingTime);
            return response;
            
        } catch (Exception e) {
            logger.error("图表生成失败", e);
            ChartResponse errorResponse = ChartResponse.error("图表生成失败: " + e.getMessage());
            errorResponse.setProcessingTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }
    
    /**
     * 通过命令行调用Python脚本生成图表
     */
    private ChartResponse generateChartViaCLI(ChartRequest request) throws Exception {
        // 创建临时配置文件
        Path tempConfigFile = createTempConfigFile(request);
        
        try {
            // 构建命令
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(pythonScriptPath);
            command.add("create");
            command.add("--engine");
            command.add(request.getEngine());
            command.add("--type");
            command.add(request.getChartType());
            command.add("--config");
            command.add(tempConfigFile.toString());
            command.add("--output");
            command.add(outputDir);
            command.add("--format");
            command.add("json");
            
            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            // 设置环境变量以确保UTF-8输出
            Map<String, String> env = processBuilder.environment();
            env.put("PYTHONIOENCODING", "utf-8");
            env.put("PYTHONUNBUFFERED", "1");
            
            logger.info("执行Python命令: {}", String.join(" ", command));
            
            Process process = processBuilder.start();
            
            // 读取输出，明确指定UTF-8编码
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Python脚本执行超时");
            }
            
            int exitCode = process.exitValue();
            String outputStr = output.toString().trim();
            
            logger.debug("Python脚本输出: {}", outputStr);
            
            if (exitCode == 0) {
                // 查找JSON开始位置，跳过可能的非JSON输出
                int jsonStart = outputStr.indexOf("{");
                if (jsonStart > 0) {
                    outputStr = outputStr.substring(jsonStart);
                }
                
                // 解析JSON输出
                ChartResponse response;
                try {
                    response = objectMapper.readValue(outputStr, ChartResponse.class);
                } catch (Exception e) {
                    logger.error("JSON解析失败, 原始输出: {}", outputStr);
                    return ChartResponse.error("JSON解析失败: " + e.getMessage());
                }
                
                // 验证文件是否存在
                if (response.getOutputPath() != null && !Files.exists(Paths.get(response.getOutputPath()))) {
                    return ChartResponse.error("生成的文件不存在: " + response.getOutputPath());
                }
                
                return response;
            } else {
                return ChartResponse.error("Python脚本执行失败 (退出代码: " + exitCode + "): " + outputStr);
            }
            
        } finally {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempConfigFile);
            } catch (IOException e) {
                logger.warn("删除临时配置文件失败: {}", tempConfigFile, e);
            }
        }
    }
    
    /**
     * 通过HTTP API调用生成图表
     */
    private ChartResponse generateChartViaAPI(ChartRequest request) throws Exception {
        // 使用RestTemplate或其他HTTP客户端调用API
        // 这里提供一个简单的实现示例
        
        try {
            // 将请求对象转换为JSON
            String jsonRequest = objectMapper.writeValueAsString(request);
            
            // 创建HTTP请求 (这里需要你的项目中有HTTP客户端配置)
            // 例如使用RestTemplate、OkHttp等
            
            // 示例代码（需要根据你的HTTP客户端实现）
            /*
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (request.getSessionId() != null) {
                headers.set("X-Session-ID", request.getSessionId());
            }
            
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
            ResponseEntity<ChartResponse> response = restTemplate.postForEntity(
                apiUrl + "/api/charts/create", entity, ChartResponse.class);
            
            return response.getBody();
            */
            
            // 临时返回错误，提示需要实现HTTP客户端
            return ChartResponse.error("HTTP API调用模式需要配置HTTP客户端");
            
        } catch (Exception e) {
            logger.error("HTTP API调用失败", e);
            return ChartResponse.error("HTTP API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建临时配置文件
     */
    private Path createTempConfigFile(ChartRequest request) throws IOException {
        Path tempFile = Files.createTempFile("chart_config_", ".json");
        
        // 构建配置对象 - 使用正确的JSON结构
        ChartConfigFile config = new ChartConfigFile();
        config.setData(request.getData());
        config.setChartConfig(request.getChartConfig());
        
        // 写入文件，使用UTF-8编码和格式化
        try (java.io.FileWriter writer = new java.io.FileWriter(tempFile.toFile(), java.nio.charset.StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, config);
        }
        
        logger.debug("创建临时配置文件: {}", tempFile);
        logger.debug("配置文件内容: data keys={}, config keys={}", 
                    request.getData() != null ? request.getData().keySet() : null,
                    request.getChartConfig() != null ? request.getChartConfig().keySet() : null);
        return tempFile;
    }
    
    /**
     * 确保输出目录存在
     */
    private void ensureOutputDirectory() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            logger.info("创建输出目录: {}", outputPath);
        }
    }
    
    @Override
    public boolean validateChartConfig(ChartRequest request) {
        try {
            // 基本验证
            if (request == null) {
                return false;
            }
            
            if (request.getEngine() == null || request.getChartType() == null) {
                return false;
            }
            
            if (request.getData() == null || request.getData().isEmpty()) {
                return false;
            }
            
            if (request.getChartConfig() == null) {
                return false;
            }
            
            // 验证引擎和图表类型是否支持
            String[] supportedEngines = getSupportedEngines();
            String[] supportedTypes = getSupportedChartTypes();
            
            boolean engineSupported = false;
            for (String engine : supportedEngines) {
                if (engine.equals(request.getEngine())) {
                    engineSupported = true;
                    break;
                }
            }
            
            boolean typeSupported = false;
            for (String type : supportedTypes) {
                if (type.equals(request.getChartType())) {
                    typeSupported = true;
                    break;
                }
            }
            
            return engineSupported && typeSupported;
            
        } catch (Exception e) {
            logger.error("验证图表配置时出错", e);
            return false;
        }
    }
    
    @Override
    public boolean checkPythonEnvironment() {
        try {
            // 检查Python是否可用
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, "--version");
            Process process = processBuilder.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return false;
            }
            
            // 检查脚本文件是否存在
            Path scriptPath = Paths.get(pythonScriptPath);
            if (!Files.exists(scriptPath)) {
                logger.error("Python脚本文件不存在: {}", scriptPath);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("检查Python环境时出错", e);
            return false;
        }
    }
    
    @Override
    public String[] getSupportedChartTypes() {
        return new String[]{"line", "bar", "pie", "scatter", "heatmap"};
    }
    
    @Override
    public String[] getSupportedEngines() {
        return new String[]{"matplotlib", "plotly", "seaborn"};
    }
    
    /**
     * 配置文件对象（用于JSON序列化）
     */
    private static class ChartConfigFile {
        private Object data;
        
        @com.fasterxml.jackson.annotation.JsonProperty("chart_config")
        private Object chartConfig;
        
        public Object getData() {
            return data;
        }
        
        public void setData(Object data) {
            this.data = data;
        }
        
        public Object getChartConfig() {
            return chartConfig;
        }
        
        public void setChartConfig(Object chartConfig) {
            this.chartConfig = chartConfig;
        }
    }
}