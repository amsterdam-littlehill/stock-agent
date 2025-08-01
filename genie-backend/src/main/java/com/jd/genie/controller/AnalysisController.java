package com.jd.genie.controller;

import com.jd.genie.entity.AnalysisTask;
import com.jd.genie.entity.AnalysisResult;
import com.jd.genie.service.AnalysisTaskService;
import com.jd.genie.service.AnalysisResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分析任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "股票分析", description = "股票分析任务管理API")
public class AnalysisController {
    
    private final AnalysisTaskService analysisTaskService;
    private final AnalysisResultService analysisResultService;
    
    /**
     * 创建分析任务请求DTO
     */
    public static class CreateAnalysisRequest {
        @NotBlank(message = "股票代码不能为空")
        private String stockCode;
        
        @NotBlank(message = "用户ID不能为空")
        private String userId;
        
        @NotBlank(message = "分析类型不能为空")
        private String analysisType;
        
        private String analysisDepth = "STANDARD";
        private String timePeriod = "30D";
        private Map<String, Object> parameters;
        
        // Getters and Setters
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
        public String getAnalysisDepth() { return analysisDepth; }
        public void setAnalysisDepth(String analysisDepth) { this.analysisDepth = analysisDepth; }
        public String getTimePeriod() { return timePeriod; }
        public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }
    
    /**
     * 创建分析任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建分析任务", description = "创建新的股票分析任务")
    public ResponseEntity<AnalysisTask> createAnalysisTask(
            @Parameter(description = "分析任务请求")
            @Valid @RequestBody CreateAnalysisRequest request) {
        
        log.info("创建分析任务: {} {} {}", request.getStockCode(), request.getUserId(), request.getAnalysisType());
        
        AnalysisTask task = analysisTaskService.createAnalysisTask(
                request.getStockCode(),
                request.getUserId(),
                request.getAnalysisType(),
                request.getAnalysisDepth(),
                request.getTimePeriod(),
                request.getParameters()
        );
        
        return ResponseEntity.ok(task);
    }
    
    /**
     * 获取分析任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取分析任务详情", description = "根据任务ID获取分析任务的详细信息")
    public ResponseEntity<AnalysisTask> getAnalysisTask(
            @Parameter(description = "任务ID", example = "1")
            @PathVariable @NotNull Long taskId) {
        
        log.info("获取分析任务详情: {}", taskId);
        
        Optional<AnalysisTask> task = analysisTaskService.getAnalysisTask(taskId);
        
        return task.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据请求ID获取任务
     */
    @GetMapping("/tasks/request/{requestId}")
    @Operation(summary = "根据请求ID获取任务", description = "根据请求ID获取分析任务")
    public ResponseEntity<AnalysisTask> getAnalysisTaskByRequestId(
            @Parameter(description = "请求ID", example = "TASK_1234567890_ABCD1234")
            @PathVariable @NotBlank String requestId) {
        
        log.info("根据请求ID获取任务: {}", requestId);
        
        Optional<AnalysisTask> task = analysisTaskService.getAnalysisTaskByRequestId(requestId);
        
        return task.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取用户任务列表
     */
    @GetMapping("/tasks/user/{userId}")
    @Operation(summary = "获取用户任务列表", description = "获取指定用户的分析任务列表")
    public ResponseEntity<Page<AnalysisTask>> getUserTasks(
            @Parameter(description = "用户ID", example = "user123")
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("获取用户任务列表: {} page={} size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisTask> tasks = analysisTaskService.getUserTasks(userId, pageable);
        
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 获取股票分析历史
     */
    @GetMapping("/tasks/stock/{stockCode}")
    @Operation(summary = "获取股票分析历史", description = "获取指定股票的分析历史")
    public ResponseEntity<Page<AnalysisTask>> getStockAnalysisHistory(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("获取股票分析历史: {} page={} size={}", stockCode, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisTask> tasks = analysisTaskService.getStockAnalysisHistory(stockCode, pageable);
        
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 搜索分析任务
     */
    @GetMapping("/tasks/search")
    @Operation(summary = "搜索分析任务", description = "根据条件搜索分析任务")
    public ResponseEntity<Page<AnalysisTask>> searchTasks(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "任务状态")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "分析类型")
            @RequestParam(required = false) String analysisType,
            
            @Parameter(description = "开始时间")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            
            @Parameter(description = "结束时间")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("搜索分析任务: keyword={} status={} type={}", keyword, status, analysisType);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisTask> tasks = analysisTaskService.searchTasks(
                keyword, status, analysisType, startTime, endTime, pageable);
        
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 取消任务
     */
    @PutMapping("/tasks/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消指定的分析任务")
    public ResponseEntity<Map<String, Object>> cancelTask(
            @Parameter(description = "任务ID", example = "1")
            @PathVariable @NotNull Long taskId,
            
            @Parameter(description = "用户ID", example = "user123")
            @RequestParam @NotBlank String userId) {
        
        log.info("取消任务: {} by {}", taskId, userId);
        
        boolean success = analysisTaskService.cancelTask(taskId, userId);
        
        Map<String, Object> result = Map.of(
                "success", success,
                "message", success ? "任务取消成功" : "任务取消失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重试任务
     */
    @PutMapping("/tasks/{taskId}/retry")
    @Operation(summary = "重试任务", description = "重试失败的分析任务")
    public ResponseEntity<Map<String, Object>> retryTask(
            @Parameter(description = "任务ID", example = "1")
            @PathVariable @NotNull Long taskId) {
        
        log.info("重试任务: {}", taskId);
        
        boolean success = analysisTaskService.retryTask(taskId);
        
        Map<String, Object> result = Map.of(
                "success", success,
                "message", success ? "任务重试成功" : "任务重试失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/tasks/statistics")
    @Operation(summary = "获取任务统计", description = "获取分析任务的统计信息")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        
        log.info("获取任务统计");
        
        Map<String, Object> statistics = analysisTaskService.getTaskStatistics();
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取用户任务统计
     */
    @GetMapping("/tasks/statistics/user/{userId}")
    @Operation(summary = "获取用户任务统计", description = "获取指定用户的任务统计信息")
    public ResponseEntity<Map<String, Object>> getUserTaskStatistics(
            @Parameter(description = "用户ID", example = "user123")
            @PathVariable @NotBlank String userId) {
        
        log.info("获取用户任务统计: {}", userId);
        
        Map<String, Object> statistics = analysisTaskService.getUserTaskStatistics(userId);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取热门分析股票
     */
    @GetMapping("/popular-stocks")
    @Operation(summary = "获取热门分析股票", description = "获取最近被分析最多的股票")
    public ResponseEntity<List<Map<String, Object>>> getPopularAnalysisStocks(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("获取热门分析股票: top {}", limit);
        
        List<Map<String, Object>> popularStocks = analysisTaskService.getPopularAnalysisStocks(limit);
        
        return ResponseEntity.ok(popularStocks);
    }
    
    // ==================== 分析结果相关API ====================
    
    /**
     * 获取任务分析结果
     */
    @GetMapping("/tasks/{taskId}/results")
    @Operation(summary = "获取任务分析结果", description = "获取指定任务的所有分析结果")
    public ResponseEntity<List<AnalysisResult>> getTaskResults(
            @Parameter(description = "任务ID", example = "1")
            @PathVariable @NotNull Long taskId) {
        
        log.info("获取任务分析结果: {}", taskId);
        
        List<AnalysisResult> results = analysisResultService.getTaskResults(taskId);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取股票最新分析结果
     */
    @GetMapping("/results/stock/{stockCode}/latest")
    @Operation(summary = "获取股票最新分析结果", description = "获取指定股票的最新分析结果")
    public ResponseEntity<List<AnalysisResult>> getLatestStockAnalysis(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "返回数量", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("获取股票最新分析结果: {} limit {}", stockCode, limit);
        
        List<AnalysisResult> results = analysisResultService.getLatestStockAnalysis(stockCode, limit);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取股票综合分析
     */
    @GetMapping("/results/stock/{stockCode}/comprehensive")
    @Operation(summary = "获取股票综合分析", description = "获取指定股票的综合分析结果")
    public ResponseEntity<Map<String, Object>> getComprehensiveAnalysis(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode) {
        
        log.info("获取股票综合分析: {}", stockCode);
        
        Map<String, Object> analysis = analysisResultService.getComprehensiveAnalysis(stockCode);
        
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * 根据分析类型获取结果
     */
    @GetMapping("/results/stock/{stockCode}/type/{analysisType}")
    @Operation(summary = "根据分析类型获取结果", description = "获取指定股票和分析类型的结果")
    public ResponseEntity<List<AnalysisResult>> getAnalysisByType(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "分析类型", example = "TECHNICAL")
            @PathVariable @NotBlank String analysisType,
            
            @Parameter(description = "返回数量", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("根据分析类型获取结果: {} {} limit {}", stockCode, analysisType, limit);
        
        List<AnalysisResult> results = analysisResultService.getAnalysisByType(stockCode, analysisType, limit);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取分析结果统计
     */
    @GetMapping("/results/statistics")
    @Operation(summary = "获取分析结果统计", description = "获取分析结果的统计信息")
    public ResponseEntity<Map<String, Object>> getAnalysisResultStatistics() {
        
        log.info("获取分析结果统计");
        
        Map<String, Object> statistics = analysisResultService.getAnalysisResultStatistics();
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取股票分析统计
     */
    @GetMapping("/results/statistics/stock/{stockCode}")
    @Operation(summary = "获取股票分析统计", description = "获取指定股票的分析统计信息")
    public ResponseEntity<Map<String, Object>> getStockAnalysisStatistics(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode) {
        
        log.info("获取股票分析统计: {}", stockCode);
        
        Map<String, Object> statistics = analysisResultService.getStockAnalysisStatistics(stockCode);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 搜索分析结果
     */
    @GetMapping("/results/search")
    @Operation(summary = "搜索分析结果", description = "根据条件搜索分析结果")
    public ResponseEntity<Page<AnalysisResult>> searchAnalysisResults(
            @Parameter(description = "股票代码")
            @RequestParam(required = false) String stockCode,
            
            @Parameter(description = "分析类型")
            @RequestParam(required = false) String analysisType,
            
            @Parameter(description = "智能体类型")
            @RequestParam(required = false) String agentType,
            
            @Parameter(description = "投资建议")
            @RequestParam(required = false) String recommendation,
            
            @Parameter(description = "风险等级")
            @RequestParam(required = false) String riskLevel,
            
            @Parameter(description = "开始时间")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            
            @Parameter(description = "结束时间")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("搜索分析结果: stock={} type={} agent={}", stockCode, analysisType, agentType);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisResult> results = analysisResultService.searchAnalysisResults(
                stockCode, analysisType, agentType, recommendation, riskLevel, startTime, endTime, pageable);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取投资建议分布
     */
    @GetMapping("/results/recommendations/distribution")
    @Operation(summary = "获取投资建议分布", description = "获取投资建议的分布统计")
    public ResponseEntity<Map<String, Object>> getRecommendationDistribution(
            @Parameter(description = "股票代码")
            @RequestParam(required = false) String stockCode,
            
            @Parameter(description = "天数", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        
        log.info("获取投资建议分布: stock={} days={}", stockCode, days);
        
        Map<String, Object> distribution = analysisResultService.getRecommendationDistribution(stockCode, days);
        
        return ResponseEntity.ok(distribution);
    }
}