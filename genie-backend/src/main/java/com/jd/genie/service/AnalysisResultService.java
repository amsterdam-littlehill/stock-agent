package com.jd.genie.service;

import com.jd.genie.entity.AnalysisResult;
import com.jd.genie.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析结果服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultService {
    
    private final AnalysisResultRepository analysisResultRepository;
    
    /**
     * 获取任务分析结果
     */
    @Cacheable(value = "task-results", key = "#taskId")
    public List<AnalysisResult> getTaskResults(Long taskId) {
        log.debug("获取任务分析结果: {}", taskId);
        return analysisResultRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
    
    /**
     * 获取股票最新分析结果
     */
    @Cacheable(value = "latest-stock-analysis", key = "#stockCode + '_' + #limit")
    public List<AnalysisResult> getLatestStockAnalysis(String stockCode, int limit) {
        log.debug("获取股票最新分析结果: {} limit {}", stockCode, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        return analysisResultRepository.findLatestAnalysisByStock(stockCode, pageable);
    }
    
    /**
     * 获取股票综合分析
     */
    @Cacheable(value = "comprehensive-analysis", key = "#stockCode")
    public Map<String, Object> getComprehensiveAnalysis(String stockCode) {
        log.debug("获取股票综合分析: {}", stockCode);
        
        Map<String, Object> analysis = new HashMap<>();
        
        // 获取最新分析结果
        List<AnalysisResult> latestResults = getLatestStockAnalysis(stockCode, 10);
        analysis.put("latestResults", latestResults);
        
        if (!latestResults.isEmpty()) {
            // 计算综合评分
            OptionalDouble avgConfidence = latestResults.stream()
                    .filter(r -> r.getConfidenceScore() != null)
                    .mapToDouble(r -> r.getConfidenceScore().doubleValue())
                    .average();
            
            analysis.put("averageConfidence", 
                    avgConfidence.isPresent() ? 
                            BigDecimal.valueOf(avgConfidence.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            
            // 统计投资建议分布
            Map<String, Long> recommendationStats = latestResults.stream()
                    .filter(r -> r.getInvestmentRecommendation() != null)
                    .collect(Collectors.groupingBy(
                            AnalysisResult::getInvestmentRecommendation,
                            Collectors.counting()
                    ));
            analysis.put("recommendationDistribution", recommendationStats);
            
            // 统计风险等级分布
            Map<String, Long> riskStats = latestResults.stream()
                    .filter(r -> r.getRiskLevel() != null)
                    .collect(Collectors.groupingBy(
                            AnalysisResult::getRiskLevel,
                            Collectors.counting()
                    ));
            analysis.put("riskDistribution", riskStats);
            
            // 获取最主流的投资建议
            String mainRecommendation = recommendationStats.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("HOLD");
            analysis.put("mainRecommendation", mainRecommendation);
            
            // 计算一致性评分（相同建议的比例）
            long mainCount = recommendationStats.getOrDefault(mainRecommendation, 0L);
            double consistency = latestResults.size() > 0 ? (double) mainCount / latestResults.size() * 100 : 0;
            analysis.put("consistencyScore", BigDecimal.valueOf(consistency).setScale(2, RoundingMode.HALF_UP));
            
            // 获取关键要点（合并所有分析的关键要点）
            List<String> allKeyPoints = latestResults.stream()
                    .filter(r -> r.getKeyPoints() != null && !r.getKeyPoints().isEmpty())
                    .flatMap(r -> r.getKeyPoints().stream())
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());
            analysis.put("keyPoints", allKeyPoints);
            
            // 获取风险提示（合并所有分析的风险提示）
            List<String> allRiskWarnings = latestResults.stream()
                    .filter(r -> r.getRiskWarnings() != null && !r.getRiskWarnings().isEmpty())
                    .flatMap(r -> r.getRiskWarnings().stream())
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());
            analysis.put("riskWarnings", allRiskWarnings);
        }
        
        return analysis;
    }
    
    /**
     * 根据分析类型获取结果
     */
    @Cacheable(value = "analysis-by-type", key = "#stockCode + '_' + #analysisType + '_' + #limit")
    public List<AnalysisResult> getAnalysisByType(String stockCode, String analysisType, int limit) {
        log.debug("根据分析类型获取结果: {} {} limit {}", stockCode, analysisType, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        return analysisResultRepository.findByStockCodeAndAnalysisTypeOrderByCreatedAtDesc(
                stockCode, analysisType, pageable);
    }
    
    /**
     * 搜索分析结果
     */
    public Page<AnalysisResult> searchAnalysisResults(String stockCode, String analysisType, 
                                                    String agentType, String recommendation, 
                                                    String riskLevel, LocalDateTime startTime, 
                                                    LocalDateTime endTime, Pageable pageable) {
        log.debug("搜索分析结果: stock={} type={} agent={}", stockCode, analysisType, agentType);
        
        // 根据不同条件组合进行查询
        if (StringUtils.hasText(stockCode) && StringUtils.hasText(analysisType)) {
            return analysisResultRepository.findByStockCodeAndAnalysisTypeOrderByCreatedAtDesc(
                    stockCode, analysisType, pageable);
        }
        
        if (StringUtils.hasText(stockCode) && StringUtils.hasText(agentType)) {
            return analysisResultRepository.findByStockCodeAndAgentTypeOrderByCreatedAtDesc(
                    stockCode, agentType, pageable);
        }
        
        if (StringUtils.hasText(stockCode) && StringUtils.hasText(recommendation)) {
            return analysisResultRepository.findByStockCodeAndInvestmentRecommendationOrderByCreatedAtDesc(
                    stockCode, recommendation, pageable);
        }
        
        if (StringUtils.hasText(stockCode) && StringUtils.hasText(riskLevel)) {
            return analysisResultRepository.findByStockCodeAndRiskLevelOrderByCreatedAtDesc(
                    stockCode, riskLevel, pageable);
        }
        
        if (StringUtils.hasText(stockCode)) {
            return analysisResultRepository.findByStockCodeOrderByCreatedAtDesc(stockCode, pageable);
        }
        
        if (StringUtils.hasText(analysisType)) {
            return analysisResultRepository.findByAnalysisTypeOrderByCreatedAtDesc(analysisType, pageable);
        }
        
        if (StringUtils.hasText(agentType)) {
            return analysisResultRepository.findByAgentTypeOrderByCreatedAtDesc(agentType, pageable);
        }
        
        if (startTime != null && endTime != null) {
            return analysisResultRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                    startTime, endTime, pageable);
        }
        
        return analysisResultRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 获取分析结果统计
     */
    @Cacheable(value = "analysis-result-statistics")
    public Map<String, Object> getAnalysisResultStatistics() {
        log.debug("获取分析结果统计");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总分析结果数
        long totalResults = analysisResultRepository.countAnalysisResults();
        stats.put("totalResults", totalResults);
        
        // 各分析类型统计
        List<Object[]> typeStats = analysisResultRepository.countByAnalysisType();
        Map<String, Long> analysisTypeStats = typeStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("analysisTypeStats", analysisTypeStats);
        
        // 各智能体类型统计
        List<Object[]> agentStats = analysisResultRepository.countByAgentType();
        Map<String, Long> agentTypeStats = agentStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("agentTypeStats", agentTypeStats);
        
        // 投资建议分布
        List<Object[]> recommendationStats = analysisResultRepository.countByInvestmentRecommendation();
        Map<String, Long> recommendationDistribution = recommendationStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("recommendationDistribution", recommendationDistribution);
        
        // 风险等级分布
        List<Object[]> riskStats = analysisResultRepository.countByRiskLevel();
        Map<String, Long> riskDistribution = riskStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("riskDistribution", riskDistribution);
        
        // 平均置信度
        Double avgConfidence = analysisResultRepository.getAverageConfidence();
        stats.put("averageConfidence", avgConfidence != null ? 
                BigDecimal.valueOf(avgConfidence).setScale(2, RoundingMode.HALF_UP) : null);
        
        // 平均处理时间
        Double avgProcessingTime = analysisResultRepository.getAverageProcessingTime();
        stats.put("averageProcessingTime", avgProcessingTime);
        
        return stats;
    }
    
    /**
     * 获取股票分析统计
     */
    @Cacheable(value = "stock-analysis-statistics", key = "#stockCode")
    public Map<String, Object> getStockAnalysisStatistics(String stockCode) {
        log.debug("获取股票分析统计: {}", stockCode);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 该股票的分析结果总数
        long totalResults = analysisResultRepository.countByStockCode(stockCode);
        stats.put("totalResults", totalResults);
        
        if (totalResults > 0) {
            // 各分析类型统计
            List<Object[]> typeStats = analysisResultRepository.countByStockCodeAndAnalysisType(stockCode);
            Map<String, Long> analysisTypeStats = typeStats.stream()
                    .collect(Collectors.toMap(
                            arr -> (String) arr[0],
                            arr -> (Long) arr[1]
                    ));
            stats.put("analysisTypeStats", analysisTypeStats);
            
            // 投资建议统计
            List<Object[]> recommendationStats = analysisResultRepository.countByStockCodeAndInvestmentRecommendation(stockCode);
            Map<String, Long> recommendationDistribution = recommendationStats.stream()
                    .collect(Collectors.toMap(
                            arr -> (String) arr[0],
                            arr -> (Long) arr[1]
                    ));
            stats.put("recommendationDistribution", recommendationDistribution);
            
            // 风险等级统计
            List<Object[]> riskStats = analysisResultRepository.countByStockCodeAndRiskLevel(stockCode);
            Map<String, Long> riskDistribution = riskStats.stream()
                    .collect(Collectors.toMap(
                            arr -> (String) arr[0],
                            arr -> (Long) arr[1]
                    ));
            stats.put("riskDistribution", riskDistribution);
            
            // 该股票的平均置信度
            Double avgConfidence = analysisResultRepository.getAverageConfidenceByStock(stockCode);
            stats.put("averageConfidence", avgConfidence != null ? 
                    BigDecimal.valueOf(avgConfidence).setScale(2, RoundingMode.HALF_UP) : null);
            
            // 该股票的平均处理时间
            Double avgProcessingTime = analysisResultRepository.getAverageProcessingTimeByStock(stockCode);
            stats.put("averageProcessingTime", avgProcessingTime);
            
            // 最新分析时间
            LocalDateTime latestAnalysisTime = analysisResultRepository.getLatestAnalysisTime(stockCode);
            stats.put("latestAnalysisTime", latestAnalysisTime);
            
            // 分析频率（最近30天）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentAnalysisCount = analysisResultRepository.countByStockCodeAndCreatedAtAfter(stockCode, thirtyDaysAgo);
            stats.put("recentAnalysisCount", recentAnalysisCount);
        }
        
        return stats;
    }
    
    /**
     * 获取投资建议分布
     */
    @Cacheable(value = "recommendation-distribution", key = "#stockCode + '_' + #days")
    public Map<String, Object> getRecommendationDistribution(String stockCode, int days) {
        log.debug("获取投资建议分布: stock={} days={}", stockCode, days);
        
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> distribution = new HashMap<>();
        
        List<Object[]> stats;
        if (StringUtils.hasText(stockCode)) {
            stats = analysisResultRepository.countRecommendationsByStockAndTimeRange(stockCode, fromTime);
        } else {
            stats = analysisResultRepository.countRecommendationsByTimeRange(fromTime);
        }
        
        Map<String, Long> recommendationCounts = stats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        
        distribution.put("recommendationCounts", recommendationCounts);
        
        // 计算总数和百分比
        long totalCount = recommendationCounts.values().stream().mapToLong(Long::longValue).sum();
        distribution.put("totalCount", totalCount);
        
        if (totalCount > 0) {
            Map<String, BigDecimal> recommendationPercentages = recommendationCounts.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> BigDecimal.valueOf((double) entry.getValue() / totalCount * 100)
                                    .setScale(2, RoundingMode.HALF_UP)
                    ));
            distribution.put("recommendationPercentages", recommendationPercentages);
        }
        
        distribution.put("timeRange", Map.of(
                "fromTime", fromTime,
                "toTime", LocalDateTime.now(),
                "days", days
        ));
        
        return distribution;
    }
    
    /**
     * 获取智能体性能统计
     */
    @Cacheable(value = "agent-performance-stats")
    public Map<String, Object> getAgentPerformanceStatistics() {
        log.debug("获取智能体性能统计");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 各智能体的分析结果数量
        List<Object[]> agentCounts = analysisResultRepository.countByAgentType();
        Map<String, Long> agentResultCounts = agentCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("agentResultCounts", agentResultCounts);
        
        // 各智能体的平均置信度
        List<Object[]> agentConfidence = analysisResultRepository.getAverageConfidenceByAgent();
        Map<String, BigDecimal> agentAvgConfidence = agentConfidence.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> BigDecimal.valueOf((Double) arr[1]).setScale(2, RoundingMode.HALF_UP)
                ));
        stats.put("agentAverageConfidence", agentAvgConfidence);
        
        // 各智能体的平均处理时间
        List<Object[]> agentProcessingTime = analysisResultRepository.getAverageProcessingTimeByAgent();
        Map<String, Double> agentAvgProcessingTime = agentProcessingTime.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Double) arr[1]
                ));
        stats.put("agentAverageProcessingTime", agentAvgProcessingTime);
        
        return stats;
    }
    
    /**
     * 保存分析结果
     */
    @Transactional
    public AnalysisResult saveAnalysisResult(AnalysisResult result) {
        log.debug("保存分析结果: {} {}", result.getStockCode(), result.getAnalysisType());
        
        result.setCreatedAt(LocalDateTime.now());
        return analysisResultRepository.save(result);
    }
    
    /**
     * 批量保存分析结果
     */
    @Transactional
    public List<AnalysisResult> batchSaveAnalysisResults(List<AnalysisResult> results) {
        log.debug("批量保存分析结果: {} 条", results.size());
        
        results.forEach(result -> result.setCreatedAt(LocalDateTime.now()));
        return analysisResultRepository.saveAll(results);
    }
    
    /**
     * 删除过期分析结果
     */
    @Transactional
    public int deleteExpiredResults(int daysToKeep) {
        log.info("删除{}天前的分析结果", daysToKeep);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        return analysisResultRepository.deleteExpiredResults(cutoffTime);
    }
    
    /**
     * 获取分析结果趋势
     */
    @Cacheable(value = "analysis-trend", key = "#stockCode + '_' + #days")
    public Map<String, Object> getAnalysisTrend(String stockCode, int days) {
        log.debug("获取分析结果趋势: {} {}天", stockCode, days);
        
        Map<String, Object> trend = new HashMap<>();
        
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);
        
        // 获取时间段内的分析结果
        List<AnalysisResult> results;
        if (StringUtils.hasText(stockCode)) {
            results = analysisResultRepository.findByStockCodeAndCreatedAtAfterOrderByCreatedAtAsc(
                    stockCode, fromTime);
        } else {
            results = analysisResultRepository.findByCreatedAtAfterOrderByCreatedAtAsc(fromTime);
        }
        
        // 按日期分组统计
        Map<String, List<AnalysisResult>> dailyResults = results.stream()
                .collect(Collectors.groupingBy(
                        result -> result.getCreatedAt().toLocalDate().toString()
                ));
        
        // 计算每日统计
        Map<String, Map<String, Object>> dailyStats = new LinkedHashMap<>();
        for (Map.Entry<String, List<AnalysisResult>> entry : dailyResults.entrySet()) {
            String date = entry.getKey();
            List<AnalysisResult> dayResults = entry.getValue();
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("count", dayResults.size());
            
            // 平均置信度
            OptionalDouble avgConfidence = dayResults.stream()
                    .filter(r -> r.getConfidenceScore() != null)
                    .mapToDouble(r -> r.getConfidenceScore().doubleValue())
                    .average();
            dayStats.put("averageConfidence", 
                    avgConfidence.isPresent() ? 
                            BigDecimal.valueOf(avgConfidence.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            
            // 投资建议分布
            Map<String, Long> recommendations = dayResults.stream()
                    .filter(r -> r.getInvestmentRecommendation() != null)
                    .collect(Collectors.groupingBy(
                            AnalysisResult::getInvestmentRecommendation,
                            Collectors.counting()
                    ));
            dayStats.put("recommendations", recommendations);
            
            dailyStats.put(date, dayStats);
        }
        
        trend.put("dailyStats", dailyStats);
        trend.put("totalResults", results.size());
        trend.put("timeRange", Map.of(
                "fromTime", fromTime,
                "toTime", LocalDateTime.now(),
                "days", days
        ));
        
        return trend;
    }
}