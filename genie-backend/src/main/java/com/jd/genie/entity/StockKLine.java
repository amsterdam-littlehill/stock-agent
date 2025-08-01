package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票K线数据实体
 */
@Data
@Entity
@Table(name = "stock_kline", indexes = {
    @Index(name = "idx_symbol_date_period", columnList = "symbol,tradeDate,period", unique = true),
    @Index(name = "idx_trade_date", columnList = "tradeDate"),
    @Index(name = "idx_symbol_period", columnList = "symbol,period")
})
@EqualsAndHashCode(callSuper = true)
public class StockKLine extends BaseEntity {

    /**
     * 股票代码
     */
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    /**
     * 交易日期
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /**
     * K线周期（1m, 5m, 15m, 30m, 1h, 1d, 1w, 1M）
     */
    @Column(name = "period", nullable = false, length = 10)
    private String period;

    /**
     * 开盘价
     */
    @Column(name = "open_price", nullable = false, precision = 10, scale = 3)
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @Column(name = "high_price", nullable = false, precision = 10, scale = 3)
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @Column(name = "low_price", nullable = false, precision = 10, scale = 3)
    private BigDecimal lowPrice;

    /**
     * 收盘价
     */
    @Column(name = "close_price", nullable = false, precision = 10, scale = 3)
    private BigDecimal closePrice;

    /**
     * 成交量（手）
     */
    @Column(name = "volume", nullable = false)
    private Long volume;

    /**
     * 成交额（元）
     */
    @Column(name = "turnover", precision = 15, scale = 2)
    private BigDecimal turnover;

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
     * 振幅（%）
     */
    @Column(name = "amplitude", precision = 8, scale = 4)
    private BigDecimal amplitude;

    /**
     * 换手率（%）
     */
    @Column(name = "turnover_rate", precision = 8, scale = 4)
    private BigDecimal turnoverRate;

    /**
     * 数据时间戳
     */
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    /**
     * 数据源
     */
    @Column(name = "data_source", length = 20)
    private String dataSource;

    /**
     * 前复权因子
     */
    @Column(name = "adj_factor", precision = 10, scale = 6)
    private BigDecimal adjFactor;

    /**
     * 是否停牌
     */
    @Column(name = "is_suspended")
    private Boolean isSuspended = false;

    /**
     * 是否ST股票
     */
    @Column(name = "is_st")
    private Boolean isSt = false;
}