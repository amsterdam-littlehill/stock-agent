package com.jd.genie.agent.orchestrator.controller;

import com.jd.genie.agent.orchestrator.model.AnalysisTask;
import com.jd.genie.agent.orchestrator.model.OrchestrationResult;
import com.jd.genie.agent.orchestrator.service.OrchestrationService;
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
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体协调REST API控制器
 * 为前端提供股票分析的HTTP接口
 * 
 * 功能：
 * - 提供多种分析模式的REST API
 * - 支持同步和异步调用
 * - 统一的响应格式和异常处理
 * - API文档和参数验证
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "股票分析API", description = "智能体协调分析接口")
public class OrchestrationController {
    
    @Autowired
    private OrchestrationService orchestrationService;
    
    /**
     * 快速股票分析
     */
    @GetMapping("/quick/{stockCode}")
    @Operation(summary = "快速股票分析", description = "使用核心分析师进行快速分析")
    public CompletableFuture<ResponseEntity<ApiResponse<OrchestrationResult>>> quickAnalysis(
            @Parameter(description = "股票代码", example = "AAPL")
            @PathVariable @NotBlank String stockCode) {
        
        return orchestrationService.quickAnalysis(stockCode)
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("快速分析API调用失败: {}", stockCode, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分析失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 深度股票分析
     */
    @GetMapping("/deep/{stockCode}")
    @Operation(summary = "深度股票分析", description = "使用所有分析师进行全面分析")
    public CompletableFuture<ResponseEntity<ApiResponse<OrchestrationResult>>> deepAnalysis(
            @Parameter(description = "股票代码", example = "AAPL")
            @PathVariable @NotBlank String stockCode) {
        
        return orchestrationService.deepAnalysis(stockCode)
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("深度分析API调用失败: {}", stockCode, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分析失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 实时股票分析
     */
    @GetMapping("/realtime/{stockCode}")
    @Operation(summary = "实时股票分析", description = "快速响应模式分析")
    public CompletableFuture<ResponseEntity<ApiResponse<OrchestrationResult>>> realTimeAnalysis(
            @Parameter(description = "股票代码", example = "AAPL")
            @PathVariable @NotBlank String stockCode) {
        
        return orchestrationService.realTimeAnalysis(stockCode)
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("实时分析API调用失败: {}", stockCode, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分析失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 自定义股票分析
     */
    @PostMapping("/custom")
    @Operation(summary = "自定义股票分析", description = "用户指定分析师和参数进行个性化分析")
    public CompletableFuture<ResponseEntity<ApiResponse<OrchestrationResult>>> customAnalysis(
            @Valid @RequestBody CustomAnalysisRequest request) {
        
        return orchestrationService.customAnalysis(
                request.getStockCode(), 
                request.getAnalysts(), 
                request.getContext()
            )
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("自定义分析API调用失败: {}", request.getStockCode(), throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分析失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 批量股票分析
     */
    @PostMapping("/batch")
    @Operation(summary = "批量股票分析", description = "并行分析多只股票")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, OrchestrationResult>>>> batchAnalysis(
            @Valid @RequestBody BatchAnalysisRequest request) {
        
        return orchestrationService.batchAnalysis(request.getStockCodes())
            .thenApply(results -> ResponseEntity.ok(ApiResponse.success(results)))
            .exceptionally(throwable -> {
                log.error("批量分析API调用失败", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量分析失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 同步快速分析
     */
    @GetMapping("/sync/quick/{stockCode}")
    @Operation(summary = "同步快速分析", description = "阻塞等待快速分析结果")
    public ResponseEntity<ApiResponse<OrchestrationResult>> quickAnalysisSync(
            @Parameter(description = "股票代码", example = "AAPL")
            @PathVariable @NotBlank String stockCode) {
        
        try {
            OrchestrationResult result = orchestrationService.quickAnalysisSync(stockCode);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("同步快速分析API调用失败: {}", stockCode, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("分析失败: " + e.getMessage()));
        }
    }
    
    /**
     * 同步深度分析
     */
    @GetMapping("/sync/deep/{stockCode}")
    @Operation(summary = "同步深度分析", description = "阻塞等待深度分析结果")
    public ResponseEntity<ApiResponse<OrchestrationResult>> deepAnalysisSync(
            @Parameter(description = "股票代码", example = "AAPL")
            @PathVariable @NotBlank String stockCode) {
        
        try {
            OrchestrationResult result = orchestrationService.deepAnalysisSync(stockCode);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("同步深度分析API调用失败: {}", stockCode, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("分析失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建自定义分析任务
     */
    @PostMapping("/task")
    @Operation(summary = "创建自定义分析任务", description = "创建具有详细配置的分析任务")
    public CompletableFuture<ResponseEntity<ApiResponse<OrchestrationResult>>> createCustomTask(
            @Valid @RequestBody CustomTaskRequest request) {
        
        return orchestrationService.createCustomTask(
                request.getStockCode(),
                request.getAnalysisType(),
                request.getAnalysts(),
                request.getContext(),
                request.getPriority()
            )
            .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)))
            .exceptionally(throwable -> {
                log.error("自定义任务API调用失败: {}", request.getStockCode(), throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("任务创建失败: " + throwable.getMessage()));
            });
    }
    
    /**
     * 获取协调器状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取协调器状态", description = "查看智能体协调器的运行状态")
    public ResponseEntity<ApiResponse<OrchestrationResult.OrchestratorStatus>> getStatus() {
        try {
            OrchestrationResult.OrchestratorStatus status = orchestrationService.getOrchestratorStatus();
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("获取协调器状态失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查智能体协调器是否正常运行")
    public ResponseEntity<ApiResponse<HealthStatus>> healthCheck() {
        try {
            boolean isHealthy = orchestrationService.isHealthy();
            HealthStatus health = new HealthStatus(isHealthy ? "UP" : "DOWN", 
                System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success(health));
        } catch (Exception e) {
            log.error("健康检查失败", e);
            HealthStatus health = new HealthStatus("DOWN", System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success(health));
        }
    }
    
    // ==================== 请求/响应模型 ====================
    
    /**
     * 自定义分析请求
     */
    @Data
    public static class CustomAnalysisRequest {
        @NotBlank(message = "股票代码不能为空")
        private String stockCode;
        
        @NotEmpty(message = "分析师列表不能为空")
        @Size(min = 1, max = 6, message = "分析师数量必须在1-6之间")
        private List<String> analysts;
        
        private Map<String, Object> context;
    }
    
    /**
     * 批量分析请求
     */
    @Data
    public static class BatchAnalysisRequest {
        @NotEmpty(message = "股票代码列表不能为空")
        @Size(min = 1, max = 100, message = "批量分析最多支持100只股票")
        private List<String> stockCodes;
    }
    
    /**
     * 自定义任务请求
     */
    @Data
    public static class CustomTaskRequest {
        @NotBlank(message = "股票代码不能为空")
        private String stockCode;
        
        private AnalysisTask.AnalysisType analysisType = AnalysisTask.AnalysisType.CUSTOM;
        
        @NotEmpty(message = "分析师列表不能为空")
        private List<String> analysts;
        
        private Map<String, Object> context;
        
        private int priority = 5; // 默认优先级
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
    
    /**
     * 健康状态
     */
    @Data
    public static class HealthStatus {
        private String status;
        private long timestamp;
        
        public HealthStatus(String status, long timestamp) {
            this.status = status;
            this.timestamp = timestamp;
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
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("API调用发生未知错误", e);
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("服务器内部错误: " + e.getMessage()));
    }
}