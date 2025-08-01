package com.jd.genie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 股票数据源配置
 * 支持多个数据源的配置管理
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "stock.data")
public class StockDataConfig {

    /**
     * 新浪财经API配置
     */
    private SinaConfig sina = new SinaConfig();

    /**
     * 腾讯财经API配置
     */
    private TencentConfig tencent = new TencentConfig();

    /**
     * Tushare Pro API配置
     */
    private TushareConfig tushare = new TushareConfig();

    /**
     * 东方财富API配置
     */
    private EastmoneyConfig eastmoney = new EastmoneyConfig();

    /**
     * 数据更新配置
     */
    private UpdateConfig update = new UpdateConfig();

    @Data
    public static class SinaConfig {
        private String baseUrl = "https://hq.sinajs.cn";
        private boolean enabled = true;
        private int timeout = 5000;
        private int retryCount = 3;
    }

    @Data
    public static class TencentConfig {
        private String baseUrl = "https://qt.gtimg.cn";
        private boolean enabled = true;
        private int timeout = 5000;
        private int retryCount = 3;
    }

    @Data
    public static class TushareConfig {
        private String baseUrl = "https://api.tushare.pro";
        private String token;
        private boolean enabled = false; // 需要token才能启用
        private int timeout = 10000;
        private int retryCount = 3;
        private int pointsPerMinute = 200; // API调用频率限制
    }

    @Data
    public static class EastmoneyConfig {
        private String baseUrl = "https://push2.eastmoney.com";
        private boolean enabled = true;
        private int timeout = 5000;
        private int retryCount = 3;
    }

    @Data
    public static class UpdateConfig {
        /**
         * 实时数据更新间隔（秒）
         */
        private int realTimeInterval = 3;
        
        /**
         * 历史数据更新间隔（分钟）
         */
        private int historicalInterval = 60;
        
        /**
         * 新闻数据更新间隔（分钟）
         */
        private int newsInterval = 10;
        
        /**
         * 市场开盘时间（小时）
         */
        private int marketOpenHour = 9;
        
        /**
         * 市场收盘时间（小时）
         */
        private int marketCloseHour = 15;
        
        /**
         * 是否只在交易时间更新实时数据
         */
        private boolean onlyUpdateInTradingHours = true;
    }
}