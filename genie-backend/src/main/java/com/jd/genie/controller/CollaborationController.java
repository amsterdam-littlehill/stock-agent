package com.jd.genie.controller;

import com.jd.genie.service.collaboration.CollaborationEngine;
import com.jd.genie.service.collaboration.CollaborationEngineService;
import com.jd.genie.service.collaboration.CollaborationModels.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体协作控制器
 * 提供智能体协作相关的REST API接口
 * 
 * @author Stock-Agent Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/collaboration")
@CrossOrigin(origins = "*")
public class CollaborationController {
    
    private static final Logger logger = LoggerFactory.getLogger(CollaborationController.class);
    
    @Autowired
    private CollaborationEngineService collaborationService;
    
    /**
     * 注册智能体
     * POST /api/collaboration/agents
     */
    @PostMapping("/agents")
    public ResponseEntity<?> registerAgent(@RequestBody AgentInfo agentInfo) {
        try {
            logger.info("注册智能体请求: {}", agentInfo.getAgentId());
            
            boolean success = collaborationService.registerAgent(agentInfo);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "智能体注册成功",
                        "agentId", agentInfo.getAgentId()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "智能体注册失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("注册智能体失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 注销智能体
     * DELETE /api/collaboration/agents/{agentId}
     */
    @DeleteMapping("/agents/{agentId}")
    public ResponseEntity<?> unregisterAgent(@PathVariable String agentId) {
        try {
            logger.info("注销智能体请求: {}", agentId);
            
            boolean success = collaborationService.unregisterAgent(agentId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "智能体注销成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "智能体不存在或注销失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("注销智能体失败: {}", agentId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取已注册智能体列表
     * GET /api/collaboration/agents
     */
    @GetMapping("/agents")
    public ResponseEntity<?> getRegisteredAgents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String specialization) {
        try {
            List<AgentInfo> agents;
            
            if (type != null) {
                AgentType agentType = AgentType.valueOf(type.toUpperCase());
                agents = collaborationService.getAgentsByType(agentType);
            } else if (specialization != null) {
                agents = collaborationService.getAgentsBySpecialization(specialization);
            } else {
                agents = collaborationService.getRegisteredAgents();
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", agents,
                    "count", agents.size()
            ));
            
        } catch (Exception e) {
            logger.error("获取智能体列表失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 创建协作会话
     * POST /api/collaboration/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<?> createCollaborationSession(@RequestBody CollaborationRequest request) {
        try {
            logger.info("创建协作会话请求，模式: {}, 主题: {}", 
                    request.getCollaborationMode(), request.getTopic());
            
            String sessionId = collaborationService.createCollaborationSession(request);
            
            if (sessionId != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "协作会话创建成功",
                        "sessionId", sessionId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "协作会话创建失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("创建协作会话失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 执行协作
     * POST /api/collaboration/sessions/{sessionId}/execute
     */
    @PostMapping("/sessions/{sessionId}/execute")
    public ResponseEntity<?> executeCollaboration(@PathVariable String sessionId) {
        try {
            logger.info("执行协作会话请求: {}", sessionId);
            
            // 异步执行协作
            CompletableFuture<CollaborationResult> future = 
                    collaborationService.executeCollaboration(sessionId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "协作执行已启动",
                    "sessionId", sessionId,
                    "status", "RUNNING"
            ));
            
        } catch (Exception e) {
            logger.error("执行协作失败: {}", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取协作会话状态
     * GET /api/collaboration/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> getSessionStatus(@PathVariable String sessionId) {
        try {
            CollaborationSession session = collaborationService.getSessionStatus(sessionId);
            
            if (session != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", session
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("获取会话状态失败: {}", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取协作历史
     * GET /api/collaboration/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getCollaborationHistory(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<CollaborationHistory> history = 
                    collaborationService.getCollaborationHistory(limit);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", history,
                    "count", history.size()
            ));
            
        } catch (Exception e) {
            logger.error("获取协作历史失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取协作指标
     * GET /api/collaboration/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getCollaborationMetrics() {
        try {
            CollaborationMetrics metrics = collaborationService.getCollaborationMetrics();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", metrics
            ));
            
        } catch (Exception e) {
            logger.error("获取协作指标失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 清除缓存
     * POST /api/collaboration/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        try {
            collaborationService.clearCache();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "缓存清除成功"
            ));
            
        } catch (Exception e) {
            logger.error("清除缓存失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取支持的协作模式
     * GET /api/collaboration/modes
     */
    @GetMapping("/modes")
    public ResponseEntity<?> getCollaborationModes() {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "STRUCTURED_DEBATE", Map.of(
                                    "name", "结构化辩论",
                                    "description", "多个智能体就同一问题进行结构化辩论，通过观点碰撞达成共识",
                                    "minParticipants", 2,
                                    "maxParticipants", 8,
                                    "estimatedDuration", "2-5分钟"
                            ),
                            "PARALLEL_ANALYSIS", Map.of(
                                    "name", "并行分析",
                                    "description", "多个智能体并行分析同一问题的不同方面",
                                    "minParticipants", 2,
                                    "maxParticipants", 10,
                                    "estimatedDuration", "1-3分钟"
                            ),
                            "SEQUENTIAL_PIPELINE", Map.of(
                                    "name", "顺序流水线",
                                    "description", "智能体按顺序处理问题，每个智能体的输出作为下一个的输入",
                                    "minParticipants", 2,
                                    "maxParticipants", 6,
                                    "estimatedDuration", "2-4分钟"
                            ),
                            "CONSENSUS_BUILDING", Map.of(
                                    "name", "共识构建",
                                    "description", "通过多轮交互逐步构建智能体间的共识",
                                    "minParticipants", 3,
                                    "maxParticipants", 8,
                                    "estimatedDuration", "3-6分钟"
                            )
                    )
            ));
            
        } catch (Exception e) {
            logger.error("获取协作模式失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取智能体类型
     * GET /api/collaboration/agent-types
     */
    @GetMapping("/agent-types")
    public ResponseEntity<?> getAgentTypes() {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "ANALYSIS", Map.of(
                                    "name", "分析型智能体",
                                    "description", "专注于数据分析、模式识别和趋势分析",
                                    "capabilities", List.of("历史数据分析", "统计建模", "趋势预测")
                            ),
                            "PREDICTION", Map.of(
                                    "name", "预测型智能体",
                                    "description", "专注于未来预测和风险评估",
                                    "capabilities", List.of("机器学习预测", "风险建模", "场景分析")
                            ),
                            "DECISION", Map.of(
                                    "name", "决策型智能体",
                                    "description", "专注于决策制定和策略规划",
                                    "capabilities", List.of("策略制定", "风险控制", "资源优化")
                            ),
                            "MONITORING", Map.of(
                                    "name", "监控型智能体",
                                    "description", "专注于实时监控和异常检测",
                                    "capabilities", List.of("实时监控", "异常检测", "告警管理")
                            )
                    )
            ));
            
        } catch (Exception e) {
            logger.error("获取智能体类型失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 批量注册智能体
     * POST /api/collaboration/agents/batch
     */
    @PostMapping("/agents/batch")
    public ResponseEntity<?> batchRegisterAgents(@RequestBody List<AgentInfo> agents) {
        try {
            logger.info("批量注册智能体请求，数量: {}", agents.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (AgentInfo agent : agents) {
                boolean success = collaborationService.registerAgent(agent);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量注册完成",
                    "successCount", successCount,
                    "failCount", failCount,
                    "totalCount", agents.size()
            ));
            
        } catch (Exception e) {
            logger.error("批量注册智能体失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 快速协作（一键执行）
     * POST /api/collaboration/quick
     */
    @PostMapping("/quick")
    public ResponseEntity<?> quickCollaboration(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            String mode = (String) request.get("mode");
            @SuppressWarnings("unchecked")
            List<String> participantIds = (List<String>) request.get("participantIds");
            
            logger.info("快速协作请求，主题: {}, 模式: {}", topic, mode);
            
            // 创建协作请求
            CollaborationRequest collaborationRequest = CollaborationRequest.builder()
                    .topic(topic)
                    .collaborationMode(CollaborationMode.valueOf(mode.toUpperCase()))
                    .participantIds(participantIds)
                    .context((Map<String, Object>) request.get("context"))
                    .build();
            
            // 创建会话
            String sessionId = collaborationService.createCollaborationSession(collaborationRequest);
            
            if (sessionId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "协作会话创建失败"
                ));
            }
            
            // 立即执行协作
            CompletableFuture<CollaborationResult> future = 
                    collaborationService.executeCollaboration(sessionId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "快速协作已启动",
                    "sessionId", sessionId,
                    "status", "RUNNING"
            ));
            
        } catch (Exception e) {
            logger.error("快速协作失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取API帮助文档
     * GET /api/collaboration/help
     */
    @GetMapping("/help")
    public ResponseEntity<?> getApiHelp() {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "title", "智能体协作API文档",
                            "version", "1.0",
                            "endpoints", Map.of(
                                    "POST /api/collaboration/agents", "注册智能体",
                                    "DELETE /api/collaboration/agents/{agentId}", "注销智能体",
                                    "GET /api/collaboration/agents", "获取智能体列表",
                                    "POST /api/collaboration/sessions", "创建协作会话",
                                    "POST /api/collaboration/sessions/{sessionId}/execute", "执行协作",
                                    "GET /api/collaboration/sessions/{sessionId}", "获取会话状态",
                                    "GET /api/collaboration/history", "获取协作历史",
                                    "GET /api/collaboration/metrics", "获取协作指标",
                                    "POST /api/collaboration/quick", "快速协作"
                            ),
                            "examples", Map.of(
                                    "registerAgent", Map.of(
                                            "agentId", "analysis_001",
                                            "agentName", "股票分析智能体",
                                            "agentType", "ANALYSIS",
                                            "specialization", "股票技术分析",
                                            "experienceLevel", 5,
                                            "status", "ACTIVE"
                                    ),
                                    "createSession", Map.of(
                                            "topic", "分析AAPL股票投资价值",
                                            "collaborationMode", "STRUCTURED_DEBATE",
                                            "participantIds", List.of("analysis_001", "prediction_002", "decision_003")
                                    )
                            )
                    )
            ));
            
        } catch (Exception e) {
            logger.error("获取API帮助失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "服务器内部错误: " + e.getMessage()
            ));
        }
    }
}