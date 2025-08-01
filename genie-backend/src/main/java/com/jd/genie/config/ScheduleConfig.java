package com.jd.genie.config;

import com.jd.genie.service.AnalysisResultService;
import com.jd.genie.service.AnalysisTaskService;
import com.jd.genie.service.StockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduleConfig {
    
    private final AnalysisTaskService analysisTaskService;
    private final AnalysisResultService analysisResultService;
    private final StockDataService stockDataService;
    
    /**
     * 清理过期分析任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTasks() {
        log.info("开始清理过期分析任务");
        try {
            int cleanedCount = analysisTaskService.cleanExpiredTasks(30); // 清理30天前的任务
            log.info("清理过期分析任务完成，清理数量: {}", cleanedCount);
        } catch (Exception e) {
            log.error("清理过期分析任务失败", e);
        }
    }
    
    /**
     * 清理过期分析结果
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredResults() {
        log.info("开始清理过期分析结果");
        try {
            int cleanedCount = analysisResultService.deleteExpiredResults(90); // 清理90天前的结果
            log.info("清理过期分析结果完成，清理数量: {}", cleanedCount);
        } catch (Exception e) {
            log.error("清理过期分析结果失败", e);
        }
    }
    
    /**
     * 清理过期股票价格数据
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanExpiredPriceData() {
        log.info("开始清理过期股票价格数据");
        try {
            int cleanedCount = stockDataService.cleanExpiredPriceData(365); // 清理1年前的价格数据
            log.info("清理过期股票价格数据完成，清理数量: {}", cleanedCount);
        } catch (Exception e) {
            log.error("清理过期股票价格数据失败", e);
        }
    }
    
    /**
     * 检查超时任务
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void checkTimeoutTasks() {
        log.debug("开始检查超时任务");
        try {
            int timeoutCount = analysisTaskService.handleTimeoutTasks();
            if (timeoutCount > 0) {
                log.info("处理超时任务完成，处理数量: {}", timeoutCount);
            }
        } catch (Exception e) {
            log.error("检查超时任务失败", e);
        }
    }
    
    /**
     * 系统健康检查
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void systemHealthCheck() {
        log.debug("开始系统健康检查");
        try {
            // 检查数据库连接
            long totalStocks = stockDataService.getTotalStockCount();
            log.debug("数据库连接正常，股票总数: {}", totalStocks);
            
            // 检查任务队列
            long pendingTasks = analysisTaskService.getPendingTaskCount();
            if (pendingTasks > 100) {
                log.warn("待处理任务数量较多: {}", pendingTasks);
            }
            
            // 检查运行中任务
            long runningTasks = analysisTaskService.getRunningTaskCount();
            if (runningTasks > 50) {
                log.warn("运行中任务数量较多: {}", runningTasks);
            }
            
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
        }
    }
    
    /**
     * 更新股票基础信息
     * 每天早上6点执行
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void updateStockInfo() {
        log.info("开始更新股票基础信息");
        try {
            int updatedCount = stockDataService.updateStockBasicInfo();
            log.info("更新股票基础信息完成，更新数量: {}", updatedCount);
        } catch (Exception e) {
            log.error("更新股票基础信息失败", e);
        }
    }
    
    /**
     * 生成每日统计报告
     * 每天晚上11点执行
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailyReport() {
        log.info("开始生成每日统计报告");
        try {
            // 统计今日任务
            long todayTasks = analysisTaskService.getTodayTaskCount();
            long completedTasks = analysisTaskService.getTodayCompletedTaskCount();
            long failedTasks = analysisTaskService.getTodayFailedTaskCount();
            
            // 统计今日分析结果
            long todayResults = analysisResultService.getTodayResultCount();
            
            log.info("每日统计报告 - 任务总数: {}, 完成: {}, 失败: {}, 分析结果: {}", 
                    todayTasks, completedTasks, failedTasks, todayResults);
            
        } catch (Exception e) {
            log.error("生成每日统计报告失败", e);
        }
    }
    
    /**
     * 缓存预热
     * 每天早上7点执行
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void warmUpCache() {
        log.info("开始缓存预热");
        try {
            // 预热热门股票数据
            stockDataService.preloadHotStocks();
            
            // 预热统计数据
            analysisTaskService.preloadStatistics();
            
            log.info("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    /**
     * 数据库优化
     * 每周日凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * SUN")
    public void optimizeDatabase() {
        log.info("开始数据库优化");
        try {
            // 执行数据库优化操作
            stockDataService.optimizeDatabase();
            log.info("数据库优化完成");
        } catch (Exception e) {
            log.error("数据库优化失败", e);
        }
    }
}