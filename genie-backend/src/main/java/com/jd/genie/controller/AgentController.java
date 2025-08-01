package com.jd.genie.controller;

import com.jd.genie.entity.Agent;
import com.jd.genie.entity.AnalysisResult;
import com.jd.genie.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 智能体控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "智能体管理", description = "智能体相关API")
public class AgentController {
    
    private final AgentService agentService;
    
    /**
     * 获取所有智能体
     */
    @GetMapping
    @Operation(summary = "获取所有智能体", description = "获取系统中所有智能体的列表")
    public ResponseEntity<List<Agent>> getAllAgents() {
        log.debug("获取所有智能体");
        List<Agent> agents = agentService.getAllAgents();
        return ResponseEntity.ok(agents);
    }
    
    /**
     * 获取可用智能体
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用智能体", description = "获取当前可用的智能体列表")
    public ResponseEntity<List<Agent>> getAvailableAgents() {
        log.debug("获取可用智能体");
        List<Agent> agents = agentService.getAvailableAgents();
        return ResponseEntity.ok(agents);
    }
    
    /**
     * 根据类型获取智能体
     */
    @GetMapping("/type/{agentType}")
    @Operation(summary = "根据类型获取智能体", description = "根据智能体类型获取智能体列表")
    public ResponseEntity<List<Agent>> getAgentsByType(
            @Parameter(description = "智能体类型") @PathVariable String agentType) {
        log.debug("根据类型获取智能体: {}", agentType);
        List<Agent> agents = agentService.getAgentsByType(agentType);
        return ResponseEntity.ok(agents);
    }
    
    /**
     * 根据名称获取智能体
     */
    @GetMapping("/name/{agentName}")
    @Operation(summary = "根据名称获取智能体", description = "根据智能体名称获取智能体信息")
    public ResponseEntity<Agent> getAgentByName(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        log.debug("根据名称获取智能体: {}", agentName);
        Agent agent = agentService.getAgentByName(agentName);
        if (agent != null) {
            return ResponseEntity.ok(agent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取最佳智能体
     */
    @GetMapping("/best")
    @Operation(summary = "获取最佳智能体", description = "根据分析类型获取最佳智能体")
    public ResponseEntity<Agent> getBestAgent(
            @Parameter(description = "分析类型") @RequestParam String analysisType) {
        log.debug("获取最佳智能体: {}", analysisType);
        Agent agent = agentService.getBestAgent(analysisType);
        if (agent != null) {
            return ResponseEntity.ok(agent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 分页查询智能体
     */
    @GetMapping("/search")
    @Operation(summary = "分页查询智能体", description = "根据条件分页查询智能体")
    public ResponseEntity<Page<Agent>> searchAgents(
            @Parameter(description = "智能体类型") @RequestParam(required = false) String agentType,
            @Parameter(description = "智能体状态") @RequestParam(required = false) String status,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("分页查询智能体: type={} status={} enabled={} page={} size={}", 
                agentType, status, enabled, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Agent> agents = agentService.searchAgents(agentType, status, enabled, pageable);
        return ResponseEntity.ok(agents);
    }
    
    /**
     * 执行分析
     */
    @PostMapping("/analyze")
    @Operation(summary = "执行分析", description = "使用指定智能体执行股票分析")
    public ResponseEntity<AnalysisResult> executeAnalysis(
            @Parameter(description = "智能体名称") @RequestParam String agentName,
            @Parameter(description = "股票代码") @RequestParam String stockCode,
            @Parameter(description = "分析类型") @RequestParam String analysisType,
            @Parameter(description = "分析参数") @RequestParam(required = false) Map<String, Object> parameters) {
        
        log.debug("执行分析: agent={} stock={} type={}", agentName, stockCode, analysisType);
        
        try {
            AnalysisResult result = agentService.executeAnalysis(agentName, stockCode, analysisType, parameters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行分析失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 创建智能体
     */
    @PostMapping
    @Operation(summary = "创建智能体", description = "创建新的智能体")
    public ResponseEntity<Agent> createAgent(@Valid @RequestBody Agent agent) {
        log.debug("创建智能体: {}", agent.getName());
        
        try {
            Agent createdAgent = agentService.createAgent(agent);
            return ResponseEntity.ok(createdAgent);
        } catch (Exception e) {
            log.error("创建智能体失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新智能体
     */
    @PutMapping("/{agentName}")
    @Operation(summary = "更新智能体", description = "更新智能体信息")
    public ResponseEntity<Agent> updateAgent(
            @Parameter(description = "智能体名称") @PathVariable String agentName,
            @Valid @RequestBody Agent agent) {
        
        log.debug("更新智能体: {}", agentName);
        
        try {
            agent.setName(agentName);
            Agent updatedAgent = agentService.updateAgent(agent);
            if (updatedAgent != null) {
                return ResponseEntity.ok(updatedAgent);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("更新智能体失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 启用智能体
     */
    @PostMapping("/{agentName}/enable")
    @Operation(summary = "启用智能体", description = "启用指定的智能体")
    public ResponseEntity<Void> enableAgent(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        
        log.debug("启用智能体: {}", agentName);
        
        boolean success = agentService.enableAgent(agentName);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 禁用智能体
     */
    @PostMapping("/{agentName}/disable")
    @Operation(summary = "禁用智能体", description = "禁用指定的智能体")
    public ResponseEntity<Void> disableAgent(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        
        log.debug("禁用智能体: {}", agentName);
        
        boolean success = agentService.disableAgent(agentName);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 重置智能体统计
     */
    @PostMapping("/{agentName}/reset-stats")
    @Operation(summary = "重置智能体统计", description = "重置智能体的统计数据")
    public ResponseEntity<Void> resetAgentStats(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        
        log.debug("重置智能体统计: {}", agentName);
        
        boolean success = agentService.resetAgentStats(agentName);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 执行健康检查
     */
    @PostMapping("/{agentName}/health-check")
    @Operation(summary = "执行健康检查", description = "对指定智能体执行健康检查")
    public ResponseEntity<Map<String, Object>> performHealthCheck(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        
        log.debug("执行健康检查: {}", agentName);
        
        Map<String, Object> healthStatus = agentService.performHealthCheck(agentName);
        if (healthStatus != null) {
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取智能体性能统计
     */
    @GetMapping("/performance")
    @Operation(summary = "获取智能体性能统计", description = "获取所有智能体的性能统计信息")
    public ResponseEntity<Map<String, Object>> getAgentPerformance() {
        log.debug("获取智能体性能统计");
        Map<String, Object> performance = agentService.getAgentPerformance();
        return ResponseEntity.ok(performance);
    }
    
    /**
     * 获取系统统计
     */
    @GetMapping("/system-stats")
    @Operation(summary = "获取系统统计", description = "获取智能体系统的整体统计信息")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        log.debug("获取系统统计");
        Map<String, Object> stats = agentService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取智能体分析历史
     */
    @GetMapping("/{agentName}/analysis-history")
    @Operation(summary = "获取智能体分析历史", description = "获取指定智能体的分析历史记录")
    public ResponseEntity<Page<AnalysisResult>> getAgentAnalysisHistory(
            @Parameter(description = "智能体名称") @PathVariable String agentName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("获取智能体分析历史: {} page={} size={}", agentName, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisResult> history = agentService.getAgentAnalysisHistory(agentName, pageable);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 获取智能体详细信息
     */
    @GetMapping("/{agentName}/details")
    @Operation(summary = "获取智能体详细信息", description = "获取智能体的详细信息和统计数据")
    public ResponseEntity<Map<String, Object>> getAgentDetails(
            @Parameter(description = "智能体名称") @PathVariable String agentName) {
        
        log.debug("获取智能体详细信息: {}", agentName);
        
        Agent agent = agentService.getAgentByName(agentName);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> details = Map.of(
                "agent", agent,
                "performance", agentService.getAgentPerformance(agentName),
                "healthStatus", agentService.performHealthCheck(agentName)
        );
        
        return ResponseEntity.ok(details);
    }
    
    /**
     * 批量启用智能体
     */
    @PostMapping("/batch/enable")
    @Operation(summary = "批量启用智能体", description = "批量启用多个智能体")
    public ResponseEntity<Map<String, Object>> batchEnableAgents(
            @Parameter(description = "智能体名称列表") @RequestBody List<String> agentNames) {
        
        log.debug("批量启用智能体: {}", agentNames);
        
        Map<String, Object> result = agentService.batchEnableAgents(agentNames);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量禁用智能体
     */
    @PostMapping("/batch/disable")
    @Operation(summary = "批量禁用智能体", description = "批量禁用多个智能体")
    public ResponseEntity<Map<String, Object>> batchDisableAgents(
            @Parameter(description = "智能体名称列表") @RequestBody List<String> agentNames) {
        
        log.debug("批量禁用智能体: {}", agentNames);
        
        Map<String, Object> result = agentService.batchDisableAgents(agentNames);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取智能体类型列表
     */
    @GetMapping("/types")
    @Operation(summary = "获取智能体类型列表", description = "获取系统支持的所有智能体类型")
    public ResponseEntity<List<String>> getAgentTypes() {
        log.debug("获取智能体类型列表");
        List<String> types = agentService.getAgentTypes();
        return ResponseEntity.ok(types);
    }
    
    /**
     * 获取分析类型列表
     */
    @GetMapping("/analysis-types")
    @Operation(summary = "获取分析类型列表", description = "获取系统支持的所有分析类型")
    public ResponseEntity<List<String>> getAnalysisTypes() {
        log.debug("获取分析类型列表");
        List<String> types = agentService.getAnalysisTypes();
        return ResponseEntity.ok(types);
    }
}