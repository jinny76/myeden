package com.myeden.service;

import com.myeden.dto.chart.ChartRequest;
import com.myeden.dto.chart.ChartResponse;

/**
 * 图表生成服务接口
 */
public interface ChartGenerationService {
    
    /**
     * 生成图表
     * 
     * @param request 图表请求对象
     * @return 图表响应对象
     */
    ChartResponse generateChart(ChartRequest request);
    
    /**
     * 验证图表配置
     * 
     * @param request 图表请求对象
     * @return 验证结果
     */
    boolean validateChartConfig(ChartRequest request);
    
    /**
     * 检查Python环境和依赖
     * 
     * @return 环境检查结果
     */
    boolean checkPythonEnvironment();
    
    /**
     * 获取支持的图表类型
     * 
     * @return 支持的图表类型列表
     */
    String[] getSupportedChartTypes();
    
    /**
     * 获取支持的图表引擎
     * 
     * @return 支持的图表引擎列表
     */
    String[] getSupportedEngines();
}