package com.jd.genie.service;

import com.jd.genie.entity.AnalysisTask;
import com.jd.genie.entity.AnalysisResult;
import com.jd.genie.entity.Agent;
import com.jd.genie.repository.AnalysisTaskRepository;
import com.jd.genie.repository.AnalysisResultRepository;
import com.jd.genie.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 分析任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisTaskService {
    
    private final AnalysisTaskRepository analysisTaskRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AgentRepository agentRepository;
    private final StockDataService stockDataService;
    private final AgentService agentService;
    
    /**
     * 创建分析任务
     */
    @Transactional
    public AnalysisTask createAnalysisTask(String stockCode, String userId, String analysisType, 
                                         String analysisDepth, String timePeriod, Map<String, Object> parameters) {
        log.info("创建分析任务: {} {} {} {}", stockCode, userId, analysisType, analysisDepth);
        
        // 验证股票代码
        if (!stockDataService.isValidStockCode(stockCode)) {
            throw new IllegalArgumentException("无效的股票代码: " + stockCode);
        }
        
        // 获取股票信息
        var stockInfo = stockDataService.getStockInfo(stockCode)
                .orElseThrow(() -> new IllegalArgumentException("股票信息不存在: " + stockCode));
        
        // 创建任务
        AnalysisTask task = new AnalysisTask();
        task.setRequestId(generateRequestId());
        task.setStockCode(stockCode);
        task.setStockName(stockInfo.getStockName());
        task.setUserId(userId);
        task.setAnalysisType(analysisType);
        task.setAnalysisDepth(analysisDepth);
        task.setTimePeriod(timePeriod);
        task.setTaskStatus("PENDING");
        task.setPriority(calculatePriority(analysisType, analysisDepth));
        task.setProgress(0);
        task.setCurrentStep("初始化");
        task.setAnalysisParameters(parameters);
        task.setDataSource("SYSTEM");
        task.setRetryCount(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // 分配智能体
        List<String> agents = assignAgents(analysisType, analysisDepth);
        task.setParticipatingAgents(agents);
        
        // 保存任务
        task = analysisTaskRepository.save(task);
        
        // 异步执行分析
        executeAnalysisAsync(task.getId());
        
        return task;
    }
    
    /**
     * 获取分析任务
     */
    @Cacheable(value = "analysis-task", key = "#taskId")
    public Optional<AnalysisTask> getAnalysisTask(Long taskId) {
        log.debug("获取分析任务: {}", taskId);
        return analysisTaskRepository.findById(taskId);
    }
    
    /**
     * 根据请求ID获取任务
     */
    @Cacheable(value = "analysis-task-by-request", key = "#requestId")
    public Optional<AnalysisTask> getAnalysisTaskByRequestId(String requestId) {
        log.debug("根据请求ID获取任务: {}", requestId);
        return analysisTaskRepository.findByRequestId(requestId);
    }
    
    /**
     * 获取用户任务列表
     */
    public Page<AnalysisTask> getUserTasks(String userId, Pageable pageable) {
        log.debug("获取用户任务列表: {} {}", userId, pageable);
        return analysisTaskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取股票分析历史
     */
    public Page<AnalysisTask> getStockAnalysisHistory(String stockCode, Pageable pageable) {
        log.debug("获取股票分析历史: {} {}", stockCode, pageable);
        return analysisTaskRepository.findByStockCodeOrderByCreatedAtDesc(stockCode, pageable);
    }
    
    /**
     * 搜索分析任务
     */
    public Page<AnalysisTask> searchTasks(String keyword, String status, String analysisType, 
                                        LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("搜索分析任务: {} {} {} {} {}", keyword, status, analysisType, startTime, endTime);
        
        if (StringUtils.hasText(keyword)) {
            return analysisTaskRepository.searchByKeyword(keyword, pageable);
        }
        
        if (StringUtils.hasText(status) && StringUtils.hasText(analysisType)) {
            return analysisTaskRepository.findByTaskStatusAndAnalysisTypeOrderByCreatedAtDesc(status, analysisType, pageable);
        }
        
        if (StringUtils.hasText(status)) {
            return analysisTaskRepository.findByTaskStatusOrderByCreatedAtDesc(status, pageable);
        }
        
        if (StringUtils.hasText(analysisType)) {
            return analysisTaskRepository.findByAnalysisTypeOrderByCreatedAtDesc(analysisType, pageable);
        }
        
        if (startTime != null && endTime != null) {
            return analysisTaskRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startTime, endTime, pageable);
        }
        
        return analysisTaskRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 更新任务状态
     */
    @Transactional
    @CacheEvict(value = {"analysis-task", "analysis-task-by-request"}, key = "#taskId")
    public void updateTaskStatus(Long taskId, String status, String currentStep, Integer progress) {
        log.debug("更新任务状态: {} {} {} {}", taskId, status, currentStep, progress);
        
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            AnalysisTask task = taskOpt.get();
            task.setTaskStatus(status);
            if (StringUtils.hasText(currentStep)) {
                task.setCurrentStep(currentStep);
            }
            if (progress != null) {
                task.setProgress(progress);
            }
            task.setUpdatedAt(LocalDateTime.now());
            
            if ("COMPLETED".equals(status)) {
                task.completeTask();
            } else if ("FAILED".equals(status)) {
                task.failTask("分析失败");
            }
            
            analysisTaskRepository.save(task);
        }
    }
    
    /**
     * 更新任务进度
     */
    @Transactional
    @CacheEvict(value = {"analysis-task", "analysis-task-by-request"}, key = "#taskId")
    public void updateTaskProgress(Long taskId, Integer progress, String currentStep) {
        log.debug("更新任务进度: {} {} {}", taskId, progress, currentStep);
        
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            AnalysisTask task = taskOpt.get();
            task.updateProgress(progress, currentStep);
            analysisTaskRepository.save(task);
        }
    }
    
    /**
     * 取消任务
     */
    @Transactional
    @CacheEvict(value = {"analysis-task", "analysis-task-by-request"}, key = "#taskId")
    public boolean cancelTask(Long taskId, String userId) {
        log.info("取消任务: {} by {}", taskId, userId);
        
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            AnalysisTask task = taskOpt.get();
            
            // 检查权限
            if (!task.getUserId().equals(userId)) {
                log.warn("用户 {} 无权取消任务 {}", userId, taskId);
                return false;
            }
            
            // 只能取消待处理或运行中的任务
            if ("PENDING".equals(task.getTaskStatus()) || "RUNNING".equals(task.getTaskStatus())) {
                task.cancelTask();
                analysisTaskRepository.save(task);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 重试任务
     */
    @Transactional
    @CacheEvict(value = {"analysis-task", "analysis-task-by-request"}, key = "#taskId")
    public boolean retryTask(Long taskId) {
        log.info("重试任务: {}", taskId);
        
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            AnalysisTask task = taskOpt.get();
            
            // 检查是否可以重试
            if (task.canRetry()) {
                task.retry();
                analysisTaskRepository.save(task);
                
                // 异步执行分析
                executeAnalysisAsync(taskId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取任务统计信息
     */
    @Cacheable(value = "task-statistics")
    public Map<String, Object> getTaskStatistics() {
        log.debug("获取任务统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总任务数
        long totalTasks = analysisTaskRepository.countTotalTasks();
        stats.put("totalTasks", totalTasks);
        
        // 各状态任务数
        long completedTasks = analysisTaskRepository.countCompletedTasks();
        long failedTasks = analysisTaskRepository.countFailedTasks();
        long runningTasks = analysisTaskRepository.countRunningTasks();
        long pendingTasks = analysisTaskRepository.countPendingTasks();
        
        stats.put("completedTasks", completedTasks);
        stats.put("failedTasks", failedTasks);
        stats.put("runningTasks", runningTasks);
        stats.put("pendingTasks", pendingTasks);
        
        // 成功率
        double successRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        stats.put("successRate", BigDecimal.valueOf(successRate).setScale(2, BigDecimal.ROUND_HALF_UP));
        
        // 各分析类型统计
        List<Object[]> typeStats = analysisTaskRepository.countByAnalysisType();
        Map<String, Long> analysisTypeStats = typeStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("analysisTypeStats", analysisTypeStats);
        
        // 平均处理时间
        Double avgProcessingTime = analysisTaskRepository.getAverageProcessingTime();
        stats.put("avgProcessingTime", avgProcessingTime);
        
        return stats;
    }
    
    /**
     * 获取用户任务统计
     */
    @Cacheable(value = "user-task-statistics", key = "#userId")
    public Map<String, Object> getUserTaskStatistics(String userId) {
        log.debug("获取用户任务统计: {}", userId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总任务数
        long totalTasks = analysisTaskRepository.countByUserId(userId);
        stats.put("totalTasks", totalTasks);
        
        // 各状态任务数
        long completedTasks = analysisTaskRepository.countByUserIdAndTaskStatus(userId, "COMPLETED");
        long failedTasks = analysisTaskRepository.countByUserIdAndTaskStatus(userId, "FAILED");
        long runningTasks = analysisTaskRepository.countByUserIdAndTaskStatus(userId, "RUNNING");
        long pendingTasks = analysisTaskRepository.countByUserIdAndTaskStatus(userId, "PENDING");
        
        stats.put("completedTasks", completedTasks);
        stats.put("failedTasks", failedTasks);
        stats.put("runningTasks", runningTasks);
        stats.put("pendingTasks", pendingTasks);
        
        // 今日任务数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayTasks = analysisTaskRepository.countTodayTasks(userId, todayStart);
        stats.put("todayTasks", todayTasks);
        
        return stats;
    }
    
    /**
     * 获取热门分析股票
     */
    @Cacheable(value = "popular-analysis-stocks", key = "#limit")
    public List<Map<String, Object>> getPopularAnalysisStocks(int limit) {
        log.debug("获取热门分析股票: top {}", limit);
        
        LocalDateTime fromTime = LocalDateTime.now().minusDays(7); // 最近7天
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Object[]> results = analysisTaskRepository.findPopularAnalysisStocks(fromTime, pageable);
        
        return results.stream()
                .map(arr -> {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("stockCode", arr[0]);
                    stock.put("stockName", arr[1]);
                    stock.put("analysisCount", arr[2]);
                    return stock;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 清理过期任务
     */
    @Transactional
    public int cleanupExpiredTasks(int daysToKeep) {
        log.info("清理{}天前的过期任务", daysToKeep);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        return analysisTaskRepository.deleteExpiredTasks(cutoffTime);
    }
    
    /**
     * 获取待处理任务
     */
    public List<AnalysisTask> getPendingTasks(int limit) {
        log.debug("获取待处理任务: limit {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by("priority").descending().and(Sort.by("createdAt")));
        return analysisTaskRepository.findPendingTasks(pageable);
    }
    
    /**
     * 获取超时任务
     */
    public List<AnalysisTask> getTimeoutTasks(int timeoutMinutes) {
        log.debug("获取超时任务: {}分钟", timeoutMinutes);
        
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return analysisTaskRepository.findTimeoutTasks(timeoutTime);
    }
    
    /**
     * 异步执行分析
     */
    @Async
    public CompletableFuture<Void> executeAnalysisAsync(Long taskId) {
        log.info("异步执行分析任务: {}", taskId);
        
        try {
            Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                AnalysisTask task = taskOpt.get();
                
                // 开始任务
                task.startTask();
                analysisTaskRepository.save(task);
                
                // 执行分析
                executeAnalysis(task);
                
                // 完成任务
                task.completeTask();
                analysisTaskRepository.save(task);
            }
        } catch (Exception e) {
            log.error("执行分析任务失败: {}", taskId, e);
            
            // 标记任务失败
            Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                AnalysisTask task = taskOpt.get();
                task.failTask(e.getMessage());
                analysisTaskRepository.save(task);
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "TASK_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 计算任务优先级
     */
    private Integer calculatePriority(String analysisType, String analysisDepth) {
        int priority = 5; // 默认优先级
        
        // 根据分析类型调整优先级
        switch (analysisType) {
            case "REAL_TIME":
                priority += 3;
                break;
            case "TECHNICAL":
                priority += 2;
                break;
            case "FUNDAMENTAL":
                priority += 1;
                break;
            case "COMPREHENSIVE":
                priority -= 1;
                break;
        }
        
        // 根据分析深度调整优先级
        switch (analysisDepth) {
            case "QUICK":
                priority += 2;
                break;
            case "STANDARD":
                priority += 1;
                break;
            case "DEEP":
                priority -= 1;
                break;
        }
        
        return Math.max(1, Math.min(10, priority)); // 限制在1-10之间
    }
    
    /**
     * 分配智能体
     */
    private List<String> assignAgents(String analysisType, String analysisDepth) {
        List<String> agents = new ArrayList<>();
        
        // 根据分析类型分配智能体
        switch (analysisType) {
            case "TECHNICAL":
                agents.add("TECHNICAL_ANALYST");
                break;
            case "FUNDAMENTAL":
                agents.add("FUNDAMENTAL_ANALYST");
                break;
            case "SENTIMENT":
                agents.add("SENTIMENT_ANALYST");
                break;
            case "COMPREHENSIVE":
                agents.add("TECHNICAL_ANALYST");
                agents.add("FUNDAMENTAL_ANALYST");
                agents.add("SENTIMENT_ANALYST");
                if ("DEEP".equals(analysisDepth)) {
                    agents.add("RISK_ANALYST");
                    agents.add("MARKET_ANALYST");
                }
                break;
            default:
                agents.add("GENERAL_ANALYST");
        }
        
        return agents;
    }
    
    /**
     * 执行分析
     */
    private void executeAnalysis(AnalysisTask task) {
        log.info("执行分析: {} {}", task.getId(), task.getStockCode());
        
        List<String> agents = task.getParticipatingAgents();
        int totalSteps = agents.size();
        int currentStep = 0;
        
        for (String agentType : agents) {
            currentStep++;
            
            // 更新进度
            int progress = (int) ((double) currentStep / totalSteps * 100);
            task.updateProgress(progress, "执行" + agentType + "分析");
            analysisTaskRepository.save(task);
            
            // 执行智能体分析
            try {
                AnalysisResult result = agentService.executeAnalysis(agentType, task);
                if (result != null) {
                    analysisResultRepository.save(result);
                }
            } catch (Exception e) {
                log.error("智能体分析失败: {} {}", agentType, task.getId(), e);
                // 继续执行其他智能体
            }
            
            // 模拟处理时间
            try {
                Thread.sleep(1000); // 1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 生成综合分析结果
        generateComprehensiveResult(task);
    }
    
    /**
     * 生成综合分析结果
     */
    private void generateComprehensiveResult(AnalysisTask task) {
        log.info("生成综合分析结果: {}", task.getId());
        
        // 获取所有分析结果
        List<AnalysisResult> results = analysisResultRepository.findByTaskIdOrderByCreatedAtDesc(task.getId());
        
        if (!results.isEmpty()) {
            // 计算综合评分和建议
            double avgConfidence = results.stream()
                    .filter(r -> r.getConfidenceScore() != null)
                    .mapToDouble(r -> r.getConfidenceScore().doubleValue())
                    .average()
                    .orElse(0.0);
            
            // 统计投资建议
            Map<String, Long> recommendations = results.stream()
                    .filter(r -> r.getInvestmentRecommendation() != null)
                    .collect(Collectors.groupingBy(
                            AnalysisResult::getInvestmentRecommendation,
                            Collectors.counting()
                    ));
            
            // 确定最终建议
            String finalRecommendation = recommendations.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("HOLD");
            
            // 更新任务结果
            task.setAnalysisResult("综合分析完成");
            task.setInvestmentRecommendation(finalRecommendation);
            task.setConfidenceScore(BigDecimal.valueOf(avgConfidence).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            // 计算目标价格（简化逻辑）
            var currentPrice = stockDataService.getRealTimePrice(task.getStockCode());
            if (currentPrice.isPresent()) {
                BigDecimal current = currentPrice.get().getClosePrice();
                task.setCurrentPrice(current);
                
                // 根据建议调整目标价格
                BigDecimal targetPrice = current;
                switch (finalRecommendation) {
                    case "BUY":
                        targetPrice = current.multiply(BigDecimal.valueOf(1.1)); // +10%
                        break;
                    case "STRONG_BUY":
                        targetPrice = current.multiply(BigDecimal.valueOf(1.2)); // +20%
                        break;
                    case "SELL":
                        targetPrice = current.multiply(BigDecimal.valueOf(0.9)); // -10%
                        break;
                    case "STRONG_SELL":
                        targetPrice = current.multiply(BigDecimal.valueOf(0.8)); // -20%
                        break;
                }
                task.setTargetPrice(targetPrice);
            }
        }
    }
}