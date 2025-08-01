package com.jd.genie.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB时序数据库配置
 * 用于存储股票实时数据、K线数据等时序数据
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "influxdb")
public class InfluxDBConfig {

    /**
     * InfluxDB服务器URL
     */
    private String url = "http://localhost:8086";

    /**
     * 认证Token
     */
    private String token;

    /**
     * 组织名称
     */
    private String org = "stock-agent";

    /**
     * 数据库名称（Bucket）
     */
    private String bucket = "stock-data";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeout = 10000;

    /**
     * 是否启用InfluxDB
     */
    private boolean enabled = true;

    /**
     * 批量写入配置
     */
    private BatchConfig batch = new BatchConfig();

    /**
     * 数据保留策略配置
     */
    private RetentionConfig retention = new RetentionConfig();

    /**
     * 创建InfluxDB客户端
     */
    @Bean
    public InfluxDBClient influxDBClient() {
        if (!enabled) {
            return null;
        }

        return InfluxDBClientFactory.create(
            url,
            token != null ? token.toCharArray() : null,
            org,
            bucket
        );
    }

    /**
     * 批量写入配置
     */
    @Data
    public static class BatchConfig {
        /**
         * 批量大小
         */
        private int batchSize = 1000;

        /**
         * 刷新间隔（毫秒）
         */
        private int flushInterval = 1000;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 5000;

        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 缓冲区大小
         */
        private int bufferLimit = 10000;
    }

    /**
     * 数据保留策略配置
     */
    @Data
    public static class RetentionConfig {
        /**
         * 实时数据保留时间（天）
         */
        private int realtimeRetentionDays = 7;

        /**
         * 分钟级数据保留时间（天）
         */
        private int minuteRetentionDays = 30;

        /**
         * 小时级数据保留时间（天）
         */
        private int hourRetentionDays = 365;

        /**
         * 日级数据保留时间（天）
         */
        private int dailyRetentionDays = 3650; // 10年

        /**
         * 是否启用自动清理
         */
        private boolean autoCleanup = true;

        /**
         * 清理任务执行时间（cron表达式）
         */
        private String cleanupCron = "0 0 2 * * ?";
    }

    /**
     * 测量名称常量
     */
    public static class Measurements {
        public static final String STOCK_REALTIME = "stock_realtime";
        public static final String STOCK_KLINE = "stock_kline";
        public static final String STOCK_TICK = "stock_tick";
        public static final String MARKET_INDEX = "market_index";
        public static final String NEWS_SENTIMENT = "news_sentiment";
        public static final String TRADING_VOLUME = "trading_volume";
        public static final String PRICE_CHANGE = "price_change";
    }

    /**
     * 标签字段常量
     */
    public static class Tags {
        public static final String STOCK_CODE = "stock_code";
        public static final String MARKET = "market";
        public static final String INDUSTRY = "industry";
        public static final String PERIOD = "period";
        public static final String SOURCE = "source";
        public static final String TYPE = "type";
    }

    /**
     * 字段名称常量
     */
    public static class Fields {
        // 价格相关
        public static final String PRICE = "price";
        public static final String OPEN = "open";
        public static final String HIGH = "high";
        public static final String LOW = "low";
        public static final String CLOSE = "close";
        public static final String VOLUME = "volume";
        public static final String AMOUNT = "amount";
        
        // 变化相关
        public static final String CHANGE = "change";
        public static final String CHANGE_PERCENT = "change_percent";
        public static final String TURNOVER_RATE = "turnover_rate";
        
        // 市值相关
        public static final String MARKET_CAP = "market_cap";
        public static final String PE_RATIO = "pe_ratio";
        public static final String PB_RATIO = "pb_ratio";
        
        // 情感分析
        public static final String SENTIMENT_SCORE = "sentiment_score";
        public static final String NEWS_COUNT = "news_count";
        
        // 技术指标
        public static final String MA5 = "ma5";
        public static final String MA10 = "ma10";
        public static final String MA20 = "ma20";
        public static final String RSI = "rsi";
        public static final String MACD = "macd";
    }
}