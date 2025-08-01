package com.jd.genie.agent.mcp;

import com.jd.genie.agent.dto.tool.McpToolInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP工具管理器
 * 负责MCP工具的自动发现、注册和生命周期管理
 * 
 * 功能：
 * - 自动发现可用的MCP工具
 * - 工具注册和配置管理
 * - 工具分类和标签管理
 * - 工具依赖关系管理
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class McpToolManager {
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    // 工具分类映射
    private final Map<String, List<String>> toolCategories = new ConcurrentHashMap<>();
    
    // 工具标签映射
    private final Map<String, Set<String>> toolTags = new ConcurrentHashMap<>();
    
    // 工具依赖关系
    private final Map<String, Set<String>> toolDependencies = new ConcurrentHashMap<>();
    
    // 工具配置
    private final Map<String, ToolConfig> toolConfigs = new ConcurrentHashMap<>();
    
    /**
     * 应用启动后自动初始化MCP工具
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeMcpTools() {
        log.info("开始初始化MCP工具管理器");
        
        try {
            // 发现并注册内置工具
            discoverAndRegisterBuiltinTools();
            
            // 发现并注册外部工具
            discoverAndRegisterExternalTools();
            
            // 验证工具依赖关系
            validateToolDependencies();
            
            log.info("MCP工具管理器初始化完成，已注册 {} 个工具", 
                mcpIntegrationService.getRegisteredTools().size());
            
        } catch (Exception e) {
            log.error("MCP工具管理器初始化失败", e);
        }
    }
    
    /**
     * 发现并注册内置工具
     */
    private void discoverAndRegisterBuiltinTools() {
        log.info("开始注册内置MCP工具");
        
        // 数据分析工具
        registerDataAnalysisTools();
        
        // 市场数据工具
        registerMarketDataTools();
        
        // 技术分析工具
        registerTechnicalAnalysisTools();
        
        // 风险管理工具
        registerRiskManagementTools();
        
        // 报告生成工具
        registerReportGenerationTools();
        
        // 通知工具
        registerNotificationTools();
        
        log.info("内置MCP工具注册完成");
    }
    
    /**
     * 注册数据分析工具
     */
    private void registerDataAnalysisTools() {
        // 股票数据获取工具
        McpToolInfo stockDataTool = McpToolInfo.builder()
            .name("stock_data_fetcher")
            .description("获取股票基础数据和历史价格")
            .version("1.0.0")
            .category("data_analysis")
            .tags(Set.of("stock", "data", "price", "history"))
            .parameters(Map.of(
                "symbol", Map.of("type", "string", "required", true, "description", "股票代码"),
                "period", Map.of("type", "string", "required", false, "description", "时间周期"),
                "start_date", Map.of("type", "string", "required", false, "description", "开始日期"),
                "end_date", Map.of("type", "string", "required", false, "description", "结束日期")
            ))
            .build();
        
        registerToolWithConfig(stockDataTool, ToolConfig.builder()
            .enabled(true)
            .priority(10)
            .timeoutSeconds(30)
            .retryCount(3)
            .build());
        
        // 财务数据分析工具
        McpToolInfo financialAnalysisTool = McpToolInfo.builder()
            .name("financial_analyzer")
            .description("分析公司财务数据和指标")
            .version("1.0.0")
            .category("data_analysis")
            .tags(Set.of("financial", "analysis", "metrics", "ratios"))
            .parameters(Map.of(
                "symbol", Map.of("type", "string", "required", true, "description", "股票代码"),
                "metrics", Map.of("type", "array", "required", false, "description", "指标列表"),
                "period", Map.of("type", "string", "required", false, "description", "分析周期")
            ))
            .build();
        
        registerToolWithConfig(financialAnalysisTool, ToolConfig.builder()
            .enabled(true)
            .priority(9)
            .timeoutSeconds(45)
            .retryCount(2)
            .build());
        
        addToCategory("data_analysis", Arrays.asList("stock_data_fetcher", "financial_analyzer"));
    }
    
    /**
     * 注册市场数据工具
     */
    private void registerMarketDataTools() {
        // 实时行情工具
        McpToolInfo realtimeQuoteTool = McpToolInfo.builder()
            .name("realtime_quote")
            .description("获取实时股票行情数据")
            .version("1.0.0")
            .category("market_data")
            .tags(Set.of("realtime", "quote", "price", "volume"))
            .parameters(Map.of(
                "symbols", Map.of("type", "array", "required", true, "description", "股票代码列表"),
                "fields", Map.of("type", "array", "required", false, "description", "数据字段")
            ))
            .build();
        
        registerToolWithConfig(realtimeQuoteTool, ToolConfig.builder()
            .enabled(true)
            .priority(10)
            .timeoutSeconds(10)
            .retryCount(5)
            .build());
        
        // 市场新闻工具
        McpToolInfo marketNewsTool = McpToolInfo.builder()
            .name("market_news")
            .description("获取市场新闻和公告")
            .version("1.0.0")
            .category("market_data")
            .tags(Set.of("news", "announcement", "market", "sentiment"))
            .parameters(Map.of(
                "symbol", Map.of("type", "string", "required", false, "description", "股票代码"),
                "category", Map.of("type", "string", "required", false, "description", "新闻分类"),
                "limit", Map.of("type", "integer", "required", false, "description", "返回数量")
            ))
            .build();
        
        registerToolWithConfig(marketNewsTool, ToolConfig.builder()
            .enabled(true)
            .priority(8)
            .timeoutSeconds(20)
            .retryCount(3)
            .build());
        
        addToCategory("market_data", Arrays.asList("realtime_quote", "market_news"));
    }
    
    /**
     * 注册技术分析工具
     */
    private void registerTechnicalAnalysisTools() {
        // 技术指标计算工具
        McpToolInfo technicalIndicatorTool = McpToolInfo.builder()
            .name("technical_indicators")
            .description("计算各种技术分析指标")
            .version("1.0.0")
            .category("technical_analysis")
            .tags(Set.of("technical", "indicators", "ma", "rsi", "macd"))
            .parameters(Map.of(
                "symbol", Map.of("type", "string", "required", true, "description", "股票代码"),
                "indicators", Map.of("type", "array", "required", true, "description", "指标列表"),
                "period", Map.of("type", "integer", "required", false, "description", "计算周期")
            ))
            .build();
        
        registerToolWithConfig(technicalIndicatorTool, ToolConfig.builder()
            .enabled(true)
            .priority(9)
            .timeoutSeconds(30)
            .retryCount(2)
            .dependencies(Set.of("stock_data_fetcher"))
            .build());
        
        // 图表模式识别工具
        McpToolInfo patternRecognitionTool = McpToolInfo.builder()
            .name("pattern_recognition")
            .description("识别股价图表模式")
            .version("1.0.0")
            .category("technical_analysis")
            .tags(Set.of("pattern", "chart", "recognition", "candlestick"))
            .parameters(Map.of(
                "symbol", Map.of("type", "string", "required", true, "description", "股票代码"),
                "patterns", Map.of("type", "array", "required", false, "description", "模式类型"),
                "timeframe", Map.of("type", "string", "required", false, "description", "时间框架")
            ))
            .build();
        
        registerToolWithConfig(patternRecognitionTool, ToolConfig.builder()
            .enabled(true)
            .priority(7)
            .timeoutSeconds(60)
            .retryCount(2)
            .dependencies(Set.of("stock_data_fetcher"))
            .build());
        
        addToCategory("technical_analysis", Arrays.asList("technical_indicators", "pattern_recognition"));
    }
    
    /**
     * 注册风险管理工具
     */
    private void registerRiskManagementTools() {
        // 风险评估工具
        McpToolInfo riskAssessmentTool = McpToolInfo.builder()
            .name("risk_assessment")
            .description("评估投资组合风险")
            .version("1.0.0")
            .category("risk_management")
            .tags(Set.of("risk", "assessment", "var", "volatility"))
            .parameters(Map.of(
                "portfolio", Map.of("type", "object", "required", true, "description", "投资组合"),
                "timeframe", Map.of("type", "string", "required", false, "description", "时间框架"),
                "confidence_level", Map.of("type", "number", "required", false, "description", "置信水平")
            ))
            .build();
        
        registerToolWithConfig(riskAssessmentTool, ToolConfig.builder()
            .enabled(true)
            .priority(10)
            .timeoutSeconds(45)
            .retryCount(2)
            .dependencies(Set.of("stock_data_fetcher"))
            .build());
        
        addToCategory("risk_management", Arrays.asList("risk_assessment"));
    }
    
    /**
     * 注册报告生成工具
     */
    private void registerReportGenerationTools() {
        // 分析报告生成工具
        McpToolInfo reportGeneratorTool = McpToolInfo.builder()
            .name("report_generator")
            .description("生成股票分析报告")
            .version("1.0.0")
            .category("reporting")
            .tags(Set.of("report", "analysis", "pdf", "export"))
            .parameters(Map.of(
                "analysis_data", Map.of("type", "object", "required", true, "description", "分析数据"),
                "template", Map.of("type", "string", "required", false, "description", "报告模板"),
                "format", Map.of("type", "string", "required", false, "description", "输出格式")
            ))
            .build();
        
        registerToolWithConfig(reportGeneratorTool, ToolConfig.builder()
            .enabled(true)
            .priority(6)
            .timeoutSeconds(120)
            .retryCount(1)
            .build());
        
        addToCategory("reporting", Arrays.asList("report_generator"));
    }
    
    /**
     * 注册通知工具
     */
    private void registerNotificationTools() {
        // 邮件通知工具
        McpToolInfo emailNotificationTool = McpToolInfo.builder()
            .name("email_notification")
            .description("发送邮件通知")
            .version("1.0.0")
            .category("notification")
            .tags(Set.of("email", "notification", "alert"))
            .parameters(Map.of(
                "to", Map.of("type", "array", "required", true, "description", "收件人列表"),
                "subject", Map.of("type", "string", "required", true, "description", "邮件主题"),
                "content", Map.of("type", "string", "required", true, "description", "邮件内容"),
                "attachments", Map.of("type", "array", "required", false, "description", "附件列表")
            ))
            .build();
        
        registerToolWithConfig(emailNotificationTool, ToolConfig.builder()
            .enabled(true)
            .priority(5)
            .timeoutSeconds(30)
            .retryCount(3)
            .build());
        
        addToCategory("notification", Arrays.asList("email_notification"));
    }
    
    /**
     * 发现并注册外部工具
     */
    private void discoverAndRegisterExternalTools() {
        log.info("开始发现外部MCP工具");
        
        // 这里可以实现从配置文件、环境变量或外部服务发现工具
        // 暂时跳过外部工具发现
        
        log.info("外部MCP工具发现完成");
    }
    
    /**
     * 验证工具依赖关系
     */
    private void validateToolDependencies() {
        log.info("开始验证工具依赖关系");
        
        for (Map.Entry<String, Set<String>> entry : toolDependencies.entrySet()) {
            String toolName = entry.getKey();
            Set<String> dependencies = entry.getValue();
            
            for (String dependency : dependencies) {
                if (!mcpIntegrationService.isToolRegistered(dependency)) {
                    log.warn("工具 {} 的依赖 {} 未注册", toolName, dependency);
                }
            }
        }
        
        log.info("工具依赖关系验证完成");
    }
    
    /**
     * 注册工具并配置
     */
    private void registerToolWithConfig(McpToolInfo toolInfo, ToolConfig config) {
        mcpIntegrationService.registerTool(toolInfo);
        toolConfigs.put(toolInfo.getName(), config);
        
        // 添加标签
        if (toolInfo.getTags() != null) {
            toolTags.put(toolInfo.getName(), new HashSet<>(toolInfo.getTags()));
        }
        
        // 添加依赖关系
        if (config.getDependencies() != null) {
            toolDependencies.put(toolInfo.getName(), new HashSet<>(config.getDependencies()));
        }
    }
    
    /**
     * 添加到分类
     */
    private void addToCategory(String category, List<String> toolNames) {
        toolCategories.computeIfAbsent(category, k -> new ArrayList<>()).addAll(toolNames);
    }
    
    /**
     * 根据分类获取工具
     * 
     * @param category 分类名称
     * @return 工具列表
     */
    public List<String> getToolsByCategory(String category) {
        return toolCategories.getOrDefault(category, new ArrayList<>());
    }
    
    /**
     * 根据标签获取工具
     * 
     * @param tag 标签
     * @return 工具列表
     */
    public List<String> getToolsByTag(String tag) {
        return toolTags.entrySet().stream()
            .filter(entry -> entry.getValue().contains(tag))
            .map(Map.Entry::getKey)
            .toList();
    }
    
    /**
     * 获取工具配置
     * 
     * @param toolName 工具名称
     * @return 工具配置
     */
    public ToolConfig getToolConfig(String toolName) {
        return toolConfigs.get(toolName);
    }
    
    /**
     * 获取所有分类
     * 
     * @return 分类列表
     */
    public Set<String> getAllCategories() {
        return toolCategories.keySet();
    }
    
    /**
     * 获取所有标签
     * 
     * @return 标签列表
     */
    public Set<String> getAllTags() {
        return toolTags.values().stream()
            .flatMap(Set::stream)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 获取工具依赖关系
     * 
     * @param toolName 工具名称
     * @return 依赖工具列表
     */
    public Set<String> getToolDependencies(String toolName) {
        return toolDependencies.getOrDefault(toolName, new HashSet<>());
    }
    
    /**
     * 批量健康检查所有工具
     * 
     * @return 健康检查结果
     */
    public CompletableFuture<Map<String, McpIntegrationService.ToolHealth>> performBatchHealthCheck() {
        return mcpIntegrationService.batchHealthCheck();
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 工具配置
     */
    @Data
    public static class ToolConfig {
        private boolean enabled = true;
        private int priority = 5;
        private long timeoutSeconds = 30;
        private int retryCount = 2;
        private Set<String> dependencies = new HashSet<>();
        private Map<String, Object> customConfig = new HashMap<>();
        
        public static ToolConfigBuilder builder() {
            return new ToolConfigBuilder();
        }
        
        public static class ToolConfigBuilder {
            private final ToolConfig config = new ToolConfig();
            
            public ToolConfigBuilder enabled(boolean enabled) {
                config.enabled = enabled;
                return this;
            }
            
            public ToolConfigBuilder priority(int priority) {
                config.priority = priority;
                return this;
            }
            
            public ToolConfigBuilder timeoutSeconds(long timeoutSeconds) {
                config.timeoutSeconds = timeoutSeconds;
                return this;
            }
            
            public ToolConfigBuilder retryCount(int retryCount) {
                config.retryCount = retryCount;
                return this;
            }
            
            public ToolConfigBuilder dependencies(Set<String> dependencies) {
                config.dependencies = dependencies;
                return this;
            }
            
            public ToolConfigBuilder customConfig(Map<String, Object> customConfig) {
                config.customConfig = customConfig;
                return this;
            }
            
            public ToolConfig build() {
                return config;
            }
        }
    }
}