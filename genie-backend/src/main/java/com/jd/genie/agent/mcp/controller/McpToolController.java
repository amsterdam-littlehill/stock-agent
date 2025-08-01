package com.jd.genie.agent.mcp.controller;

import com.jd.genie.agent.dto.tool.McpToolInfo;
import com.jd.genie.agent.dto.tool.ToolResult;
import com.jd.genie.agent.mcp.McpIntegrationService;
import com.jd.genie.agent.mcp.McpToolManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * MCP工具REST API控制器
 * 为前端提供MCP工具管理和调用的HTTP接口
 * 
 * 功能：
 * - MCP工具查询和管理
 * - 工具调用和结果获取
 * - 工具健康检查和监控
 * - 工具分类和标签管理
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mcp")
@Tag(name = "MCP工具API", description = "MCP工具管理和调用接口")
public class McpToolController {
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    @Autowired
    private McpToolManager mcpToolManager;
    
    /**
     * 获取所有已注册的MCP工具
     */
    @GetMapping("/tools")
    @Operation(summary = "获取所有MCP工具", description = "返回所有已注册的MCP工具列表")
    public ResponseEntity<ApiResponse<List<McpToolInfo>>> getAllTools() {
        try {
            List<McpToolInfo> tools = mcpIntegrationService.getRegisteredTools();
            return ResponseEntity.ok(ApiResponse.success(tools));
        } catch (Exception e) {
            log.error("获取MCP工具列表失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取工具列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据名称获取MCP工具信息
     */
    @GetMapping("/tools/{toolName}")
    @Operation(summary = "获取指定MCP工具信息", description = "根据工具名称获取详细信息")
    public ResponseEntity<ApiResponse<McpToolInfo>> getToolInfo(
            @Parameter(description = "工具名称", example = "stock_data_fetcher")
            @PathVariable @NotBlank String toolName) {
        
        try {
            McpToolInfo toolInfo = mcpIntegrationService.getToolInfo(toolName);
            if (toolInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(toolInfo));
        } catch (Exception e) {
            log.error("获取工具信息失败: {}", toolName, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取工具信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 调用MCP工具
     */
    @PostMapping("/tools/{toolName}/call")
    @Operation(summary = "调用MCP工具", description = "执行指定的MCP工具")
    public CompletableFuture<ResponseEntity<ApiResponse<ToolResult>>> callTool(
            @Parameter(description = "工具名称", example = "stock_data_fetcher")
            @PathVariable @NotBlank String toolName,
            @Valid @RequestBody ToolCallRequest request) {
        
        return mcpIntegrationService.callToolWithTimeout(
                toolName, 
                request.getParameters(), 
                request.getTimeoutSeconds()
            )
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("MCP工具调用失败: {}", toolName, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("工具调用失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 批量调用MCP工具
     */
    @PostMapping("/tools/batch-call")
    @Operation(summary = "批量调用MCP工具", description = "并行执行多个MCP工具")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, ToolResult>>>> batchCallTools(
            @Valid @RequestBody BatchToolCallRequest request) {
        
        List<McpIntegrationService.ToolCallRequest> toolCalls = request.getToolCalls().stream()
            .map(call -> new McpIntegrationService.ToolCallRequest(call.getToolName(), call.getParameters()))
            .toList();
        
        return mcpIntegrationService.batchCallTools(toolCalls)
            .thenApply(results -> ResponseEntity.ok(ApiResponse.success(results)))
            .exceptionally(throwable -> {
                log.error("批量工具调用失败", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量调用失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 获取工具统计信息
     */
    @GetMapping("/tools/{toolName}/stats")
    @Operation(summary = "获取工具统计信息", description = "获取指定工具的调用统计")
    public ResponseEntity<ApiResponse<McpIntegrationService.ToolStats>> getToolStats(
            @Parameter(description = "工具名称", example = "stock_data_fetcher")
            @PathVariable @NotBlank String toolName) {
        
        try {
            McpIntegrationService.ToolStats stats = mcpIntegrationService.getToolStats(toolName);
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取工具统计失败: {}", toolName, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有工具统计信息
     */
    @GetMapping("/tools/stats")
    @Operation(summary = "获取所有工具统计信息", description = "获取所有工具的调用统计")
    public ResponseEntity<ApiResponse<Map<String, McpIntegrationService.ToolStats>>> getAllToolStats() {
        try {
            Map<String, McpIntegrationService.ToolStats> stats = mcpIntegrationService.getAllToolStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取所有工具统计失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 工具健康检查
     */
    @GetMapping("/tools/{toolName}/health")
    @Operation(summary = "工具健康检查", description = "检查指定工具的健康状态")
    public CompletableFuture<ResponseEntity<ApiResponse<McpIntegrationService.ToolHealth>>> healthCheck(
            @Parameter(description = "工具名称", example = "stock_data_fetcher")
            @PathVariable @NotBlank String toolName) {
        
        return mcpIntegrationService.healthCheck(toolName)
            .thenApply(health -> ResponseEntity.ok(ApiResponse.success(health)))
            .exceptionally(throwable -> {
                log.error("工具健康检查失败: {}", toolName, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("健康检查失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 批量健康检查
     */
    @GetMapping("/tools/health")
    @Operation(summary = "批量健康检查", description = "检查所有工具的健康状态")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, McpIntegrationService.ToolHealth>>>> batchHealthCheck() {
        return mcpIntegrationService.batchHealthCheck()
            .thenApply(results -> ResponseEntity.ok(ApiResponse.success(results)))
            .exceptionally(throwable -> {
                log.error("批量健康检查失败", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量健康检查失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 根据分类获取工具
     */
    @GetMapping("/tools/category/{category}")
    @Operation(summary = "根据分类获取工具", description = "获取指定分类下的所有工具")
    public ResponseEntity<ApiResponse<List<String>>> getToolsByCategory(
            @Parameter(description = "分类名称", example = "data_analysis")
            @PathVariable @NotBlank String category) {
        
        try {
            List<String> tools = mcpToolManager.getToolsByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(tools));
        } catch (Exception e) {
            log.error("根据分类获取工具失败: {}", category, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取工具失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据标签获取工具
     */
    @GetMapping("/tools/tag/{tag}")
    @Operation(summary = "根据标签获取工具", description = "获取包含指定标签的所有工具")
    public ResponseEntity<ApiResponse<List<String>>> getToolsByTag(
            @Parameter(description = "标签名称", example = "stock")
            @PathVariable @NotBlank String tag) {
        
        try {
            List<String> tools = mcpToolManager.getToolsByTag(tag);
            return ResponseEntity.ok(ApiResponse.success(tools));
        } catch (Exception e) {
            log.error("根据标签获取工具失败: {}", tag, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取工具失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有分类", description = "获取所有工具分类列表")
    public ResponseEntity<ApiResponse<Set<String>>> getAllCategories() {
        try {
            Set<String> categories = mcpToolManager.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取分类失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    @Operation(summary = "获取所有标签", description = "获取所有工具标签列表")
    public ResponseEntity<ApiResponse<Set<String>>> getAllTags() {
        try {
            Set<String> tags = mcpToolManager.getAllTags();
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取工具配置
     */
    @GetMapping("/tools/{toolName}/config")
    @Operation(summary = "获取工具配置", description = "获取指定工具的配置信息")
    public ResponseEntity<ApiResponse<McpToolManager.ToolConfig>> getToolConfig(
            @Parameter(description = "工具名称", example = "stock_data_fetcher")
            @PathVariable @NotBlank String toolName) {
        
        try {
            McpToolManager.ToolConfig config = mcpToolManager.getToolConfig(toolName);
            if (config == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            log.error("获取工具配置失败: {}", toolName, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取工具依赖关系
     */
    @GetMapping("/tools/{toolName}/dependencies")
    @Operation(summary = "获取工具依赖关系", description = "获取指定工具的依赖工具列表")
    public ResponseEntity<ApiResponse<Set<String>>> getToolDependencies(
            @Parameter(description = "工具名称", example = "technical_indicators")
            @PathVariable @NotBlank String toolName) {
        
        try {
            Set<String> dependencies = mcpToolManager.getToolDependencies(toolName);
            return ResponseEntity.ok(ApiResponse.success(dependencies));
        } catch (Exception e) {
            log.error("获取工具依赖失败: {}", toolName, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取依赖失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索工具
     */
    @GetMapping("/tools/search")
    @Operation(summary = "搜索工具", description = "根据关键词搜索工具")
    public ResponseEntity<ApiResponse<List<McpToolInfo>>> searchTools(
            @Parameter(description = "搜索关键词", example = "stock")
            @RequestParam @NotBlank String keyword,
            @Parameter(description = "分类过滤", example = "data_analysis")
            @RequestParam(required = false) String category,
            @Parameter(description = "标签过滤", example = "realtime")
            @RequestParam(required = false) String tag) {
        
        try {
            List<McpToolInfo> allTools = mcpIntegrationService.getRegisteredTools();
            List<McpToolInfo> filteredTools = allTools.stream()
                .filter(tool -> {
                    // 关键词匹配
                    boolean keywordMatch = tool.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                         tool.getDescription().toLowerCase().contains(keyword.toLowerCase());
                    
                    // 分类过滤
                    boolean categoryMatch = category == null || 
                                          category.equals(tool.getCategory());
                    
                    // 标签过滤
                    boolean tagMatch = tag == null || 
                                     (tool.getTags() != null && tool.getTags().contains(tag));
                    
                    return keywordMatch && categoryMatch && tagMatch;
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(filteredTools));
        } catch (Exception e) {
            log.error("搜索工具失败: {}", keyword, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }
    
    // ==================== 请求/响应模型 ====================
    
    /**
     * 工具调用请求
     */
    @Data
    public static class ToolCallRequest {
        @NotEmpty(message = "参数不能为空")
        private Map<String, Object> parameters;
        
        @Positive(message = "超时时间必须大于0")
        private long timeoutSeconds = 30;
    }
    
    /**
     * 批量工具调用请求
     */
    @Data
    public static class BatchToolCallRequest {
        @NotEmpty(message = "工具调用列表不能为空")
        private List<SingleToolCall> toolCalls;
        
        @Data
        public static class SingleToolCall {
            @NotBlank(message = "工具名称不能为空")
            private String toolName;
            
            @NotEmpty(message = "参数不能为空")
            private Map<String, Object> parameters;
        }
    }
    
    /**
     * 统一API响应格式
     */
    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private long timestamp;
        
        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, "操作成功", data);
        }
        
        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, message, data);
        }
        
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
    
    // ==================== 异常处理 ====================
    
    /**
     * 全局异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("参数错误: " + e.getMessage()));
    }
    
    @ExceptionHandler(McpIntegrationService.McpToolException.class)
    public ResponseEntity<ApiResponse<Void>> handleMcpToolException(McpIntegrationService.McpToolException e) {
        log.error("MCP工具异常: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("工具异常: " + e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("API调用发生未知错误", e);
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("服务器内部错误: " + e.getMessage()));
    }
}