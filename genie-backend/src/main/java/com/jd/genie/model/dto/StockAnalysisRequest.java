package com.jd.genie.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * 股票分析请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisRequest {

    /**
     * 股票代码（必填）
     */
    @NotBlank(message = "股票代码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{1,10}(\\.[A-Z]{2})?$", message = "股票代码格式不正确")
    private String stockCode;

    /**
     * 分析深度
     * summary: 简要分析
     * normal: 常规分析
     * detailed: 详细分析
     */
    @Builder.Default
    private String depth = "normal";

    /**
     * 时间周期
     * 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M
     */
    @Builder.Default
    private String timeframe = "1d";

    /**
     * 分析类型列表
     * technical: 技术分析
     * fundamental: 基本面分析
     * sentiment: 情绪分析
     * risk: 风险分析
     */
    private List<String> analysisTypes;

    /**
     * 是否启用实时数据
     */
    @Builder.Default
    private Boolean realTimeData = true;

    /**
     * 是否包含历史对比
     */
    @Builder.Default
    private Boolean includeHistoricalComparison = false;

    /**
     * 是否包含行业对比
     */
    @Builder.Default
    private Boolean includeIndustryComparison = false;

    /**
     * 自定义分析参数
     */
    private Map<String, Object> customParameters;

    /**
     * 用户ID（可选，用于个性化分析）
     */
    private String userId;

    /**
     * 分析优先级
     * low: 低优先级
     * normal: 普通优先级
     * high: 高优先级
     */
    @Builder.Default
    private String priority = "normal";

    /**
     * 回调URL（异步分析时使用）
     */
    private String callbackUrl;

    /**
     * 分析标签（用于分类和检索）
     */
    private List<String> tags;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 验证分析类型是否有效
     */
    public boolean isValidAnalysisType(String type) {
        return type != null && (
                "technical".equals(type) ||
                "fundamental".equals(type) ||
                "sentiment".equals(type) ||
                "risk".equals(type)
        );
    }

    /**
     * 验证时间周期是否有效
     */
    public boolean isValidTimeframe() {
        return timeframe != null && (
                "1m".equals(timeframe) ||
                "5m".equals(timeframe) ||
                "15m".equals(timeframe) ||
                "30m".equals(timeframe) ||
                "1h".equals(timeframe) ||
                "4h".equals(timeframe) ||
                "1d".equals(timeframe) ||
                "1w".equals(timeframe) ||
                "1M".equals(timeframe)
        );
    }

    /**
     * 验证分析深度是否有效
     */
    public boolean isValidDepth() {
        return depth != null && (
                "summary".equals(depth) ||
                "normal".equals(depth) ||
                "detailed".equals(depth)
        );
    }

    /**
     * 获取分析类型的显示名称
     */
    public String getAnalysisTypeDisplayName(String type) {
        switch (type) {
            case "technical":
                return "技术分析";
            case "fundamental":
                return "基本面分析";
            case "sentiment":
                return "情绪分析";
            case "risk":
                return "风险分析";
            default:
                return type;
        }
    }

    /**
     * 获取时间周期的显示名称
     */
    public String getTimeframeDisplayName() {
        switch (timeframe) {
            case "1m":
                return "1分钟";
            case "5m":
                return "5分钟";
            case "15m":
                return "15分钟";
            case "30m":
                return "30分钟";
            case "1h":
                return "1小时";
            case "4h":
                return "4小时";
            case "1d":
                return "日线";
            case "1w":
                return "周线";
            case "1M":
                return "月线";
            default:
                return timeframe;
        }
    }

    /**
     * 获取分析深度的显示名称
     */
    public String getDepthDisplayName() {
        switch (depth) {
            case "summary":
                return "简要分析";
            case "normal":
                return "常规分析";
            case "detailed":
                return "详细分析";
            default:
                return depth;
        }
    }

    /**
     * 检查是否需要特定类型的分析
     */
    public boolean needsAnalysisType(String type) {
        return analysisTypes == null || analysisTypes.isEmpty() || analysisTypes.contains(type);
    }

    /**
     * 添加分析类型
     */
    public void addAnalysisType(String type) {
        if (analysisTypes == null) {
            analysisTypes = new java.util.ArrayList<>();
        }
        if (isValidAnalysisType(type) && !analysisTypes.contains(type)) {
            analysisTypes.add(type);
        }
    }

    /**
     * 移除分析类型
     */
    public void removeAnalysisType(String type) {
        if (analysisTypes != null) {
            analysisTypes.remove(type);
        }
    }

    /**
     * 添加自定义参数
     */
    public void addCustomParameter(String key, Object value) {
        if (customParameters == null) {
            customParameters = new java.util.HashMap<>();
        }
        customParameters.put(key, value);
    }

    /**
     * 获取自定义参数
     */
    public Object getCustomParameter(String key) {
        return customParameters != null ? customParameters.get(key) : null;
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    /**
     * 检查是否包含特定标签
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * 获取优先级数值
     */
    public int getPriorityLevel() {
        switch (priority) {
            case "high":
                return 3;
            case "normal":
                return 2;
            case "low":
                return 1;
            default:
                return 2;
        }
    }

    /**
     * 是否为高优先级
     */
    public boolean isHighPriority() {
        return "high".equals(priority);
    }

    /**
     * 是否需要异步处理
     */
    public boolean needsAsyncProcessing() {
        return callbackUrl != null && !callbackUrl.trim().isEmpty();
    }

    /**
     * 获取请求摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("股票: ").append(stockCode);
        summary.append(", 深度: ").append(getDepthDisplayName());
        summary.append(", 周期: ").append(getTimeframeDisplayName());
        
        if (analysisTypes != null && !analysisTypes.isEmpty()) {
            summary.append(", 类型: ");
            for (int i = 0; i < analysisTypes.size(); i++) {
                if (i > 0) summary.append(", ");
                summary.append(getAnalysisTypeDisplayName(analysisTypes.get(i)));
            }
        }
        
        return summary.toString();
    }

    /**
     * 验证请求的完整性
     */
    public boolean isValid() {
        return stockCode != null && !stockCode.trim().isEmpty() &&
               isValidDepth() &&
               isValidTimeframe() &&
               (analysisTypes == null || analysisTypes.stream().allMatch(this::isValidAnalysisType));
    }

    /**
     * 获取估计的处理时间（秒）
     */
    public int getEstimatedProcessingTime() {
        int baseTime = 10; // 基础时间10秒
        
        // 根据分析深度调整
        switch (depth) {
            case "summary":
                baseTime = 5;
                break;
            case "detailed":
                baseTime = 20;
                break;
        }
        
        // 根据分析类型数量调整
        if (analysisTypes != null) {
            baseTime += analysisTypes.size() * 5;
        }
        
        // 根据是否包含对比调整
        if (Boolean.TRUE.equals(includeHistoricalComparison)) {
            baseTime += 5;
        }
        if (Boolean.TRUE.equals(includeIndustryComparison)) {
            baseTime += 5;
        }
        
        return baseTime;
    }
}