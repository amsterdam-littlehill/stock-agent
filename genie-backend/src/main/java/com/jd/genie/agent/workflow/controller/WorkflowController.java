package com.jd.genie.agent.workflow.controller;

import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import com.jd.genie.agent.workflow.service.WorkflowService;
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
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工作流控制器
 * 提供工作流管理和执行的REST API
 * 
 * 功能：
 * - 工作流定义管理（CRUD）
 * - 工作流执行控制
 * - 执行历史查询
 * - 统计和监控
 * - 模板管理
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow")
@Tag(name = "工作流管理", description = "工作流定义管理和执行控制API")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
    // ==================== 工作流定义管理 ====================
    
    /**
     * 创建工作流定义
     */
    @PostMapping("/definitions")
    @Operation(summary = "创建工作流定义", description = "创建新的工作流定义")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request) {
        
        try {
            WorkflowDefinition workflow = request.toWorkflowDefinition();
            WorkflowDefinition createdWorkflow = workflowService.createWorkflow(workflow);
            
            return ResponseEntity.ok(ApiResponse.success(createdWorkflow, "工作流创建成功"));
        } catch (Exception e) {
            log.error("创建工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新工作流定义
     */
    @PutMapping("/definitions/{workflowId}")
    @Operation(summary = "更新工作流定义", description = "更新指定的工作流定义")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> updateWorkflow(
            @PathVariable String workflowId,
            @Valid @RequestBody UpdateWorkflowRequest request) {
        
        try {
            WorkflowDefinition workflow = request.toWorkflowDefinition();
            WorkflowDefinition updatedWorkflow = workflowService.updateWorkflow(workflowId, workflow);
            
            return ResponseEntity.ok(ApiResponse.success(updatedWorkflow, "工作流更新成功"));
        } catch (Exception e) {
            log.error("更新工作流失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除工作流定义
     */
    @DeleteMapping("/definitions/{workflowId}")
    @Operation(summary = "删除工作流定义", description = "删除指定的工作流定义")
    public ResponseEntity<ApiResponse<Boolean>> deleteWorkflow(
            @PathVariable String workflowId) {
        
        try {
            boolean deleted = workflowService.deleteWorkflow(workflowId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success(true, "工作流删除成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除工作流失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取工作流定义
     */
    @GetMapping("/definitions/{workflowId}")
    @Operation(summary = "获取工作流定义", description = "获取指定的工作流定义详情")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getWorkflow(
            @PathVariable String workflowId) {
        
        try {
            WorkflowDefinition workflow = workflowService.getWorkflow(workflowId);
            if (workflow != null) {
                return ResponseEntity.ok(ApiResponse.success(workflow));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取工作流失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取所有工作流定义
     */
    @GetMapping("/definitions")
    @Operation(summary = "获取所有工作流定义", description = "获取所有工作流定义列表")
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> getAllWorkflows() {
        
        try {
            List<WorkflowDefinition> workflows = workflowService.getAllWorkflows();
            return ResponseEntity.ok(ApiResponse.success(workflows));
        } catch (Exception e) {
            log.error("获取工作流列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 搜索工作流定义
     */
    @GetMapping("/definitions/search")
    @Operation(summary = "搜索工作流定义", description = "根据条件搜索工作流定义")
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> searchWorkflows(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "工作流类型") @RequestParam(required = false) WorkflowDefinition.WorkflowType type,
            @Parameter(description = "工作流状态") @RequestParam(required = false) WorkflowDefinition.WorkflowStatus status,
            @Parameter(description = "创建者") @RequestParam(required = false) String createdBy) {
        
        try {
            List<WorkflowDefinition> workflows = workflowService.searchWorkflows(keyword, type, status, createdBy);
            return ResponseEntity.ok(ApiResponse.success(workflows));
        } catch (Exception e) {
            log.error("搜索工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 克隆工作流定义
     */
    @PostMapping("/definitions/{workflowId}/clone")
    @Operation(summary = "克隆工作流定义", description = "克隆指定的工作流定义")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> cloneWorkflow(
            @PathVariable String workflowId,
            @Valid @RequestBody CloneWorkflowRequest request) {
        
        try {
            WorkflowDefinition clonedWorkflow = workflowService.cloneWorkflow(
                workflowId, request.getNewName(), request.getCreatedBy());
            
            return ResponseEntity.ok(ApiResponse.success(clonedWorkflow, "工作流克隆成功"));
        } catch (Exception e) {
            log.error("克隆工作流失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // ==================== 工作流执行 ====================
    
    /**
     * 执行工作流
     */
    @PostMapping("/executions")
    @Operation(summary = "执行工作流", description = "执行指定的工作流")
    public ResponseEntity<ApiResponse<ExecutionResponse>> executeWorkflow(
            @Valid @RequestBody ExecuteWorkflowRequest request) {
        
        try {
            CompletableFuture<WorkflowExecution> future = workflowService.executeWorkflow(
                request.getWorkflowId(), request.getInputParameters(), request.getExecutedBy());
            
            // 异步执行，立即返回执行ID
            WorkflowExecution execution = future.getNow(null);
            if (execution != null) {
                ExecutionResponse response = new ExecutionResponse();
                response.setExecutionId(execution.getExecutionId());
                response.setWorkflowId(execution.getWorkflowId());
                response.setStatus(execution.getStatus());
                response.setStartTime(execution.getStartTime());
                response.setAsync(true);
                
                return ResponseEntity.ok(ApiResponse.success(response, "工作流开始执行"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("工作流执行启动失败"));
            }
        } catch (Exception e) {
            log.error("执行工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 快速股票分析
     */
    @PostMapping("/executions/quick-analysis")
    @Operation(summary = "快速股票分析", description = "执行快速股票分析工作流")
    public ResponseEntity<ApiResponse<ExecutionResponse>> quickStockAnalysis(
            @Valid @RequestBody StockAnalysisRequest request) {
        
        try {
            CompletableFuture<WorkflowExecution> future = workflowService.quickStockAnalysis(
                request.getStockCode(), request.getExecutedBy());
            
            WorkflowExecution execution = future.getNow(null);
            if (execution != null) {
                ExecutionResponse response = new ExecutionResponse();
                response.setExecutionId(execution.getExecutionId());
                response.setWorkflowId(execution.getWorkflowId());
                response.setStatus(execution.getStatus());
                response.setStartTime(execution.getStartTime());
                response.setAsync(true);
                
                return ResponseEntity.ok(ApiResponse.success(response, "快速分析开始执行"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("快速分析启动失败"));
            }
        } catch (Exception e) {
            log.error("快速股票分析失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 深度股票分析
     */
    @PostMapping("/executions/deep-analysis")
    @Operation(summary = "深度股票分析", description = "执行深度股票分析工作流")
    public ResponseEntity<ApiResponse<ExecutionResponse>> deepStockAnalysis(
            @Valid @RequestBody StockAnalysisRequest request) {
        
        try {
            CompletableFuture<WorkflowExecution> future = workflowService.deepStockAnalysis(
                request.getStockCode(), request.getExecutedBy());
            
            WorkflowExecution execution = future.getNow(null);
            if (execution != null) {
                ExecutionResponse response = new ExecutionResponse();
                response.setExecutionId(execution.getExecutionId());
                response.setWorkflowId(execution.getWorkflowId());
                response.setStatus(execution.getStatus());
                response.setStartTime(execution.getStartTime());
                response.setAsync(true);
                
                return ResponseEntity.ok(ApiResponse.success(response, "深度分析开始执行"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("深度分析启动失败"));
            }
        } catch (Exception e) {
            log.error("深度股票分析失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 市场监控
     */
    @PostMapping("/executions/market-monitoring")
    @Operation(summary = "市场监控", description = "执行市场监控工作流")
    public ResponseEntity<ApiResponse<ExecutionResponse>> marketMonitoring(
            @Valid @RequestBody MarketMonitoringRequest request) {
        
        try {
            CompletableFuture<WorkflowExecution> future = workflowService.marketMonitoring(
                request.getStockCodes(), request.getExecutedBy());
            
            WorkflowExecution execution = future.getNow(null);
            if (execution != null) {
                ExecutionResponse response = new ExecutionResponse();
                response.setExecutionId(execution.getExecutionId());
                response.setWorkflowId(execution.getWorkflowId());
                response.setStatus(execution.getStatus());
                response.setStartTime(execution.getStartTime());
                response.setAsync(true);
                
                return ResponseEntity.ok(ApiResponse.success(response, "市场监控开始执行"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("市场监控启动失败"));
            }
        } catch (Exception e) {
            log.error("市场监控失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 取消工作流执行
     */
    @PostMapping("/executions/{executionId}/cancel")
    @Operation(summary = "取消工作流执行", description = "取消正在执行的工作流")
    public ResponseEntity<ApiResponse<Boolean>> cancelExecution(
            @PathVariable String executionId) {
        
        try {
            boolean cancelled = workflowService.cancelExecution(executionId);
            if (cancelled) {
                return ResponseEntity.ok(ApiResponse.success(true, "工作流已取消"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("取消工作流失败"));
            }
        } catch (Exception e) {
            log.error("取消工作流执行失败: {}", executionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 暂停工作流执行
     */
    @PostMapping("/executions/{executionId}/pause")
    @Operation(summary = "暂停工作流执行", description = "暂停正在执行的工作流")
    public ResponseEntity<ApiResponse<Boolean>> pauseExecution(
            @PathVariable String executionId) {
        
        try {
            boolean paused = workflowService.pauseExecution(executionId);
            if (paused) {
                return ResponseEntity.ok(ApiResponse.success(true, "工作流已暂停"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("暂停工作流失败"));
            }
        } catch (Exception e) {
            log.error("暂停工作流执行失败: {}", executionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 恢复工作流执行
     */
    @PostMapping("/executions/{executionId}/resume")
    @Operation(summary = "恢复工作流执行", description = "恢复暂停的工作流")
    public ResponseEntity<ApiResponse<Boolean>> resumeExecution(
            @PathVariable String executionId) {
        
        try {
            boolean resumed = workflowService.resumeExecution(executionId);
            if (resumed) {
                return ResponseEntity.ok(ApiResponse.success(true, "工作流已恢复"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("恢复工作流失败"));
            }
        } catch (Exception e) {
            log.error("恢复工作流执行失败: {}", executionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // ==================== 执行历史查询 ====================
    
    /**
     * 获取执行状态
     */
    @GetMapping("/executions/{executionId}")
    @Operation(summary = "获取执行状态", description = "获取工作流执行状态和详情")
    public ResponseEntity<ApiResponse<WorkflowExecution>> getExecutionStatus(
            @PathVariable String executionId) {
        
        try {
            WorkflowExecution execution = workflowService.getExecutionStatus(executionId);
            if (execution != null) {
                return ResponseEntity.ok(ApiResponse.success(execution));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取执行状态失败: {}", executionId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取工作流执行历史
     */
    @GetMapping("/definitions/{workflowId}/executions")
    @Operation(summary = "获取工作流执行历史", description = "获取指定工作流的执行历史")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getWorkflowExecutionHistory(
            @PathVariable String workflowId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        
        try {
            List<WorkflowExecution> executions = workflowService.getWorkflowExecutionHistory(workflowId, limit);
            return ResponseEntity.ok(ApiResponse.success(executions));
        } catch (Exception e) {
            log.error("获取工作流执行历史失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户执行历史
     */
    @GetMapping("/executions/user/{userId}")
    @Operation(summary = "获取用户执行历史", description = "获取指定用户的执行历史")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getUserExecutionHistory(
            @PathVariable String userId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        
        try {
            List<WorkflowExecution> executions = workflowService.getUserExecutionHistory(userId, limit);
            return ResponseEntity.ok(ApiResponse.success(executions));
        } catch (Exception e) {
            log.error("获取用户执行历史失败: {}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取正在执行的工作流
     */
    @GetMapping("/executions/running")
    @Operation(summary = "获取正在执行的工作流", description = "获取当前正在执行的工作流列表")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getRunningExecutions() {
        
        try {
            List<WorkflowExecution> executions = workflowService.getRunningExecutions();
            return ResponseEntity.ok(ApiResponse.success(executions));
        } catch (Exception e) {
            log.error("获取正在执行的工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // ==================== 统计和监控 ====================
    
    /**
     * 获取工作流统计信息
     */
    @GetMapping("/definitions/{workflowId}/stats")
    @Operation(summary = "获取工作流统计信息", description = "获取指定工作流的统计信息")
    public ResponseEntity<ApiResponse<WorkflowService.WorkflowStats>> getWorkflowStats(
            @PathVariable String workflowId) {
        
        try {
            WorkflowService.WorkflowStats stats = workflowService.getWorkflowStats(workflowId);
            if (stats != null) {
                return ResponseEntity.ok(ApiResponse.success(stats));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取工作流统计信息失败: {}", workflowId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats/system")
    @Operation(summary = "获取系统统计信息", description = "获取工作流系统的整体统计信息")
    public ResponseEntity<ApiResponse<WorkflowService.SystemStats>> getSystemStats() {
        
        try {
            WorkflowService.SystemStats stats = workflowService.getSystemStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取系统统计信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // ==================== 请求/响应模型 ====================
    
    /**
     * 创建工作流请求
     */
    @Data
    public static class CreateWorkflowRequest {
        @NotBlank(message = "工作流ID不能为空")
        private String workflowId;
        
        @NotBlank(message = "工作流名称不能为空")
        private String name;
        
        private String description;
        
        @NotBlank(message = "版本不能为空")
        private String version;
        
        @NotBlank(message = "创建者不能为空")
        private String createdBy;
        
        @NotNull(message = "工作流类型不能为空")
        private WorkflowDefinition.WorkflowType type;
        
        private boolean isPublic = false;
        private String category;
        private List<String> tags;
        
        @NotEmpty(message = "节点列表不能为空")
        private List<WorkflowDefinition.WorkflowNode> nodes;
        
        @NotEmpty(message = "连接列表不能为空")
        private List<WorkflowDefinition.WorkflowConnection> connections;
        
        private WorkflowDefinition.ExecutionConfig executionConfig;
        
        public WorkflowDefinition toWorkflowDefinition() {
            WorkflowDefinition workflow = WorkflowDefinition.builder()
                .workflowId(workflowId)
                .name(name)
                .description(description)
                .version(version)
                .createdBy(createdBy)
                .type(type)
                .isPublic(isPublic)
                .category(category)
                .build();
            
            if (tags != null) {
                workflow.setTags(Set.of(tags.toArray(new String[0])));
            }
            
            workflow.setNodes(nodes);
            workflow.setConnections(connections);
            workflow.setExecutionConfig(executionConfig);
            
            return workflow;
        }
    }
    
    /**
     * 更新工作流请求
     */
    @Data
    public static class UpdateWorkflowRequest {
        @NotBlank(message = "工作流名称不能为空")
        private String name;
        
        private String description;
        
        @NotBlank(message = "版本不能为空")
        private String version;
        
        @NotNull(message = "工作流类型不能为空")
        private WorkflowDefinition.WorkflowType type;
        
        @NotNull(message = "工作流状态不能为空")
        private WorkflowDefinition.WorkflowStatus status;
        
        private boolean isPublic = false;
        private String category;
        private List<String> tags;
        
        @NotEmpty(message = "节点列表不能为空")
        private List<WorkflowDefinition.WorkflowNode> nodes;
        
        @NotEmpty(message = "连接列表不能为空")
        private List<WorkflowDefinition.WorkflowConnection> connections;
        
        private WorkflowDefinition.ExecutionConfig executionConfig;
        
        public WorkflowDefinition toWorkflowDefinition() {
            WorkflowDefinition workflow = WorkflowDefinition.builder()
                .name(name)
                .description(description)
                .version(version)
                .type(type)
                .status(status)
                .isPublic(isPublic)
                .category(category)
                .build();
            
            if (tags != null) {
                workflow.setTags(Set.of(tags.toArray(new String[0])));
            }
            
            workflow.setNodes(nodes);
            workflow.setConnections(connections);
            workflow.setExecutionConfig(executionConfig);
            
            return workflow;
        }
    }
    
    /**
     * 克隆工作流请求
     */
    @Data
    public static class CloneWorkflowRequest {
        @NotBlank(message = "新工作流名称不能为空")
        private String newName;
        
        @NotBlank(message = "创建者不能为空")
        private String createdBy;
    }
    
    /**
     * 执行工作流请求
     */
    @Data
    public static class ExecuteWorkflowRequest {
        @NotBlank(message = "工作流ID不能为空")
        private String workflowId;
        
        @NotBlank(message = "执行者不能为空")
        private String executedBy;
        
        private Map<String, Object> inputParameters;
    }
    
    /**
     * 股票分析请求
     */
    @Data
    public static class StockAnalysisRequest {
        @NotBlank(message = "股票代码不能为空")
        private String stockCode;
        
        @NotBlank(message = "执行者不能为空")
        private String executedBy;
    }
    
    /**
     * 市场监控请求
     */
    @Data
    public static class MarketMonitoringRequest {
        @NotEmpty(message = "股票代码列表不能为空")
        private List<String> stockCodes;
        
        @NotBlank(message = "执行者不能为空")
        private String executedBy;
    }
    
    /**
     * 执行响应
     */
    @Data
    public static class ExecutionResponse {
        private String executionId;
        private String workflowId;
        private WorkflowExecution.ExecutionStatus status;
        private LocalDateTime startTime;
        private boolean async;
    }
    
    /**
     * 统一API响应格式
     */
    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;
        
        public static <T> ApiResponse<T> success(T data) {
            return success(data, "操作成功");
        }
        
        public static <T> ApiResponse<T> success(T data, String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage(message);
            response.setData(data);
            response.setTimestamp(LocalDateTime.now());
            return response;
        }
        
        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(message);
            response.setTimestamp(LocalDateTime.now());
            return response;
        }
    }
}