package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票实时数据实体
 */
@Data
@Entity
@Table(name = "stock_data", indexes = {
    @Index(name = "idx_symbol_time", columnList = "symbol,updateTime"),
    @Index(name = "idx_update_time", columnList = "updateTime")
})
@EqualsAndHashCode(callSuper = true)
public class StockData extends BaseEntity {

    /**
     * 股票代码（如：000001.SZ）
     */
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    /**
     * 股票名称
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * 当前价格
     */
    @Column(name = "current_price", precision = 10, scale = 3)
    private BigDecimal currentPrice;

    /**
     * 开盘价
     */
    @Column(name = "open_price", precision = 10, scale = 3)
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 10, scale = 3)
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 10, scale = 3)
    private BigDecimal lowPrice;

    /**
     * 昨收价
     */
    @Column(name = "prev_close", precision = 10, scale = 3)
    private BigDecimal prevClose;

    /**
     * 涨跌额
     */
    @Column(name = "change_amount", precision = 10, scale = 3)
    private BigDecimal changeAmount;

    /**
     * 涨跌幅（%）
     */
    @Column(name = "change_percent", precision = 8, scale = 4)
    private BigDecimal changePercent;

    /**
     * 成交量（手）
     */
    @Column(name = "volume")
    private Long volume;

    /**
     * 成交额（元）
     */
    @Column(name = "turnover", precision = 15, scale = 2)
    private BigDecimal turnover;

    /**
     * 换手率（%）
     */
    @Column(name = "turnover_rate", precision = 8, scale = 4)
    private BigDecimal turnoverRate;

    /**
     * 市盈率
     */
    @Column(name = "pe_ratio", precision = 10, scale = 4)
    private BigDecimal peRatio;

    /**
     * 市净率
     */
    @Column(name = "pb_ratio", precision = 10, scale = 4)
    private BigDecimal pbRatio;

    /**
     * 总市值（元）
     */
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    /**
     * 流通市值（元）
     */
    @Column(name = "circulating_market_cap", precision = 20, scale = 2)
    private BigDecimal circulatingMarketCap;

    /**
     * 数据更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 数据源（SINA, TENCENT, TUSHARE, EASTMONEY）
     */
    @Column(name = "data_source", length = 20)
    private String dataSource;

    /**
     * 交易状态（TRADING, SUSPENDED, CLOSED）
     */
    @Column(name = "trading_status", length = 20)
    private String tradingStatus;

    /**
     * 是否为最新数据
     */
    @Column(name = "is_latest")
    private Boolean isLatest = true;
}