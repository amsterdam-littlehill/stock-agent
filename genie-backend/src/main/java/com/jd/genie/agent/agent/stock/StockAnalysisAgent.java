package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.dto.tool.ToolCall;
import com.jd.genie.agent.enums.AgentState;
import com.jd.genie.agent.enums.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 股票分析智能体基类
 * 为所有股票分析相关的智能体提供通用功能
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class StockAnalysisAgent extends BaseAgent {

    // 股票代码
    protected String stockCode;
    
    // 分析类型
    protected String analysisType;
    
    // 置信度阈值
    protected double confidenceThreshold = 0.7;
    
    // 分析结果
    protected StockAnalysisResult analysisResult;

    /**
     * 执行股票分析的抽象方法
     * 子类需要实现具体的分析逻辑
     */
    public abstract StockAnalysisResult performAnalysis(String stockCode, Map<String, Object> parameters);

    /**
     * 验证分析结果的置信度
     */
    protected boolean isResultConfident(StockAnalysisResult result) {
        return result != null && result.getConfidenceScore() >= confidenceThreshold;
    }

    /**
     * 格式化分析结果为可读文本
     */
    protected String formatAnalysisResult(StockAnalysisResult result) {
        if (result == null) {
            return "分析失败：无法获取有效结果";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s分析报告 ===\n", getAnalysisType()));
        sb.append(String.format("股票代码: %s\n", result.getStockCode()));
        sb.append(String.format("分析时间: %s\n", result.getAnalysisTime()));
        sb.append(String.format("置信度: %.2f%%\n", result.getConfidenceScore() * 100));
        sb.append(String.format("分析结论: %s\n", result.getConclusion()));
        
        if (result.getRecommendation() != null) {
            sb.append(String.format("投资建议: %s\n", result.getRecommendation()));
        }
        
        if (result.getRiskLevel() != null) {
            sb.append(String.format("风险等级: %s\n", result.getRiskLevel()));
        }
        
        if (result.getKeyPoints() != null && !result.getKeyPoints().isEmpty()) {
            sb.append("关键要点:\n");
            for (String point : result.getKeyPoints()) {
                sb.append(String.format("- %s\n", point));
            }
        }
        
        return sb.toString();
    }

    /**
     * 执行单步分析
     */
    @Override
    public String step() {
        try {
            setState(AgentState.RUNNING);
            
            // 获取最新的用户消息
            String userQuery = getMemory().getLastUserMessage();
            if (userQuery == null || userQuery.trim().isEmpty()) {
                setState(AgentState.ERROR);
                return "错误：未找到有效的分析请求";
            }
            
            // 解析股票代码
            String extractedStockCode = extractStockCode(userQuery);
            if (extractedStockCode == null) {
                setState(AgentState.ERROR);
                return "错误：无法从请求中提取股票代码";
            }
            
            this.stockCode = extractedStockCode;
            
            // 执行具体分析
            log.info("{} {} 开始分析股票: {}", getContext().getRequestId(), getName(), stockCode);
            
            Map<String, Object> analysisParams = extractAnalysisParameters(userQuery);
            StockAnalysisResult result = performAnalysis(stockCode, analysisParams);
            
            if (result == null) {
                setState(AgentState.ERROR);
                return "分析失败：无法获取分析结果";
            }
            
            this.analysisResult = result;
            
            // 格式化结果
            String formattedResult = formatAnalysisResult(result);
            
            // 更新记忆
            updateMemory(RoleType.ASSISTANT, formattedResult, null);
            
            setState(AgentState.FINISHED);
            
            log.info("{} {} 完成股票分析: {}, 置信度: {:.2f}", 
                    getContext().getRequestId(), getName(), stockCode, result.getConfidenceScore());
            
            return formattedResult;
            
        } catch (Exception e) {
            setState(AgentState.ERROR);
            log.error("{} {} 分析过程中发生错误", getContext().getRequestId(), getName(), e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 从用户查询中提取股票代码
     */
    protected String extractStockCode(String query) {
        // 简单的股票代码提取逻辑
        // 支持格式：000001, 000001.SZ, SH600000, AAPL等
        String[] patterns = {
            "\\b\\d{6}\\.(SZ|SH)\\b",  // 000001.SZ, 600000.SH
            "\\b(SZ|SH)\\d{6}\\b",      // SZ000001, SH600000
            "\\b\\d{6}\\b",             // 000001, 600000
            "\\b[A-Z]{1,5}\\b"          // AAPL, MSFT
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(query.toUpperCase());
            if (m.find()) {
                return m.group();
            }
        }
        
        return null;
    }

    /**
     * 从用户查询中提取分析参数
     */
    protected Map<String, Object> extractAnalysisParameters(String query) {
        Map<String, Object> params = new java.util.HashMap<>();
        
        // 提取时间范围
        if (query.contains("日线") || query.contains("日K")) {
            params.put("timeframe", "1d");
        } else if (query.contains("周线") || query.contains("周K")) {
            params.put("timeframe", "1w");
        } else if (query.contains("月线") || query.contains("月K")) {
            params.put("timeframe", "1M");
        } else {
            params.put("timeframe", "1d"); // 默认日线
        }
        
        // 提取分析深度
        if (query.contains("详细") || query.contains("深度")) {
            params.put("depth", "detailed");
        } else if (query.contains("简单") || query.contains("概要")) {
            params.put("depth", "summary");
        } else {
            params.put("depth", "normal");
        }
        
        return params;
    }

    /**
     * 获取分析类型名称
     */
    public String getAnalysisType() {
        return analysisType != null ? analysisType : "股票分析";
    }

    /**
     * 设置分析类型
     */
    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    /**
     * 获取当前分析结果
     */
    public StockAnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    /**
     * 检查是否有有效的分析结果
     */
    public boolean hasValidResult() {
        return analysisResult != null && isResultConfident(analysisResult);
    }
}