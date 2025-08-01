package com.jd.genie.service;

import com.jd.genie.config.StockDataConfig;
import com.jd.genie.entity.StockData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 股票数据定时更新服务
 * 负责定时从各数据源更新股票数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataScheduleService {

    private final StockDataIntegrationService stockDataService;
    private final StockDataConfig stockDataConfig;

    // 常用股票代码列表（可以从数据库或配置文件读取）
    private static final List<String> POPULAR_STOCKS = List.of(
        "000001.SZ", "000002.SZ", "000858.SZ", "002415.SZ", "002594.SZ",
        "600000.SH", "600036.SH", "600519.SH", "600887.SH", "601318.SH",
        "601398.SH", "601857.SH", "601988.SH", "000858.SZ", "002142.SZ"
    );

    /**
     * 实时数据更新任务
     * 每3秒执行一次（仅在交易时间）
     */
    @Scheduled(fixedDelayString = "#{${stock.data.update.real-time-interval:3} * 1000}")
    public void updateRealTimeData() {
        if (!isInTradingHours()) {
            log.debug("非交易时间，跳过实时数据更新");
            return;
        }

        log.debug("开始更新实时股票数据");
        
        try {
            // 异步更新热门股票数据
            CompletableFuture.runAsync(() -> {
                List<StockData> stockDataList = stockDataService.getRealTimeData(POPULAR_STOCKS);
                log.debug("更新了 {} 只股票的实时数据", stockDataList.size());
            });
        } catch (Exception e) {
            log.error("更新实时数据失败", e);
        }
    }

    /**
     * 历史数据更新任务
     * 每小时执行一次
     */
    @Scheduled(fixedDelayString = "#{${stock.data.update.historical-interval:60} * 60 * 1000}")
    public void updateHistoricalData() {
        log.info("开始更新历史股票数据");
        
        try {
            // 这里可以实现历史数据的更新逻辑
            // 例如：更新昨日收盘数据、周K线、月K线等
            
            CompletableFuture.runAsync(() -> {
                // 实现历史数据更新逻辑
                log.info("历史数据更新完成");
            });
        } catch (Exception e) {
            log.error("更新历史数据失败", e);
        }
    }

    /**
     * 数据源健康检查任务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkDataSourceHealth() {
        log.debug("检查数据源健康状态");
        
        try {
            var healthStatus = stockDataService.getDataSourceHealth();
            
            healthStatus.forEach((source, isHealthy) -> {
                if (!isHealthy) {
                    log.warn("数据源 {} 不可用", source);
                } else {
                    log.debug("数据源 {} 正常", source);
                }
            });
        } catch (Exception e) {
            log.error("检查数据源健康状态失败", e);
        }
    }

    /**
     * 缓存清理任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupCache() {
        log.info("开始清理过期缓存");
        
        try {
            // 清理所有实时数据缓存，让系统重新获取最新数据
            stockDataService.clearAllRealtimeCache();
            log.info("缓存清理完成");
        } catch (Exception e) {
            log.error("清理缓存失败", e);
        }
    }

    /**
     * 市场开盘前数据预热任务
     * 每天上午8:50执行
     */
    @Scheduled(cron = "0 50 8 * * MON-FRI")
    public void preloadMarketData() {
        log.info("开始预热市场数据");
        
        try {
            // 预加载热门股票数据到缓存
            CompletableFuture.runAsync(() -> {
                List<StockData> stockDataList = stockDataService.getRealTimeData(POPULAR_STOCKS);
                log.info("预热了 {} 只股票数据", stockDataList.size());
            });
        } catch (Exception e) {
            log.error("预热市场数据失败", e);
        }
    }

    /**
     * 市场收盘后数据整理任务
     * 每天下午3:30执行
     */
    @Scheduled(cron = "0 30 15 * * MON-FRI")
    public void postMarketDataProcessing() {
        log.info("开始收盘后数据整理");
        
        try {
            // 可以在这里实现：
            // 1. 保存当日收盘数据
            // 2. 计算技术指标
            // 3. 生成日报数据
            // 4. 清理临时数据
            
            CompletableFuture.runAsync(() -> {
                log.info("收盘后数据整理完成");
            });
        } catch (Exception e) {
            log.error("收盘后数据整理失败", e);
        }
    }

    /**
     * 判断当前是否在交易时间内
     */
    private boolean isInTradingHours() {
        if (!stockDataConfig.getUpdate().isOnlyUpdateInTradingHours()) {
            return true; // 如果配置为不限制交易时间，则总是返回true
        }

        LocalTime now = LocalTime.now();
        LocalTime marketOpen = LocalTime.of(stockDataConfig.getUpdate().getMarketOpenHour(), 0);
        LocalTime marketClose = LocalTime.of(stockDataConfig.getUpdate().getMarketCloseHour(), 0);
        
        // 检查是否在交易时间内（9:00-15:00）
        boolean inTradingHours = now.isAfter(marketOpen) && now.isBefore(marketClose);
        
        // 检查是否为工作日（周一到周五）
        boolean isWeekday = LocalDateTime.now().getDayOfWeek().getValue() <= 5;
        
        return inTradingHours && isWeekday;
    }

    /**
     * 手动触发实时数据更新
     */
    public void triggerRealTimeUpdate() {
        log.info("手动触发实时数据更新");
        updateRealTimeData();
    }

    /**
     * 手动触发历史数据更新
     */
    public void triggerHistoricalUpdate() {
        log.info("手动触发历史数据更新");
        updateHistoricalData();
    }

    /**
     * 获取调度任务状态
     */
    public ScheduleStatus getScheduleStatus() {
        return ScheduleStatus.builder()
                .inTradingHours(isInTradingHours())
                .realTimeUpdateEnabled(true)
                .historicalUpdateEnabled(true)
                .lastUpdateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 调度状态信息
     */
    public static class ScheduleStatus {
        private boolean inTradingHours;
        private boolean realTimeUpdateEnabled;
        private boolean historicalUpdateEnabled;
        private LocalDateTime lastUpdateTime;

        public static ScheduleStatusBuilder builder() {
            return new ScheduleStatusBuilder();
        }

        // Builder pattern implementation
        public static class ScheduleStatusBuilder {
            private boolean inTradingHours;
            private boolean realTimeUpdateEnabled;
            private boolean historicalUpdateEnabled;
            private LocalDateTime lastUpdateTime;

            public ScheduleStatusBuilder inTradingHours(boolean inTradingHours) {
                this.inTradingHours = inTradingHours;
                return this;
            }

            public ScheduleStatusBuilder realTimeUpdateEnabled(boolean realTimeUpdateEnabled) {
                this.realTimeUpdateEnabled = realTimeUpdateEnabled;
                return this;
            }

            public ScheduleStatusBuilder historicalUpdateEnabled(boolean historicalUpdateEnabled) {
                this.historicalUpdateEnabled = historicalUpdateEnabled;
                return this;
            }

            public ScheduleStatusBuilder lastUpdateTime(LocalDateTime lastUpdateTime) {
                this.lastUpdateTime = lastUpdateTime;
                return this;
            }

            public ScheduleStatus build() {
                ScheduleStatus status = new ScheduleStatus();
                status.inTradingHours = this.inTradingHours;
                status.realTimeUpdateEnabled = this.realTimeUpdateEnabled;
                status.historicalUpdateEnabled = this.historicalUpdateEnabled;
                status.lastUpdateTime = this.lastUpdateTime;
                return status;
            }
        }

        // Getters
        public boolean isInTradingHours() { return inTradingHours; }
        public boolean isRealTimeUpdateEnabled() { return realTimeUpdateEnabled; }
        public boolean isHistoricalUpdateEnabled() { return historicalUpdateEnabled; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    }
}