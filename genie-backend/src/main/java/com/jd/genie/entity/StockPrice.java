package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票价格数据实体类
 */
@Entity
@Table(name = "stock_price", indexes = {
    @Index(name = "idx_stock_code_time", columnList = "stock_code, price_time"),
    @Index(name = "idx_price_time", columnList = "price_time"),
    @Index(name = "idx_price_type", columnList = "price_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;
    
    /**
     * 价格时间
     */
    @Column(name = "price_time", nullable = false)
    private LocalDateTime priceTime;
    
    /**
     * 价格类型 (REAL_TIME-实时, DAILY-日线, WEEKLY-周线, MONTHLY-月线)
     */
    @Column(name = "price_type", nullable = false, length = 20)
    private String priceType;
    
    /**
     * 开盘价
     */
    @Column(name = "open_price", precision = 12, scale = 4)
    private BigDecimal openPrice;
    
    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 12, scale = 4)
    private BigDecimal highPrice;
    
    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 12, scale = 4)
    private BigDecimal lowPrice;
    
    /**
     * 收盘价
     */
    @Column(name = "close_price", precision = 12, scale = 4)
    private BigDecimal closePrice;
    
    /**
     * 前收盘价
     */
    @Column(name = "prev_close", precision = 12, scale = 4)
    private BigDecimal prevClose;
    
    /**
     * 成交量(股)
     */
    @Column(name = "volume", precision = 20, scale = 0)
    private BigDecimal volume;
    
    /**
     * 成交额(元)
     */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;
    
    /**
     * 换手率(%)
     */
    @Column(name = "turnover_rate", precision = 8, scale = 4)
    private BigDecimal turnoverRate;
    
    /**
     * 涨跌额
     */
    @Column(name = "change_amount", precision = 12, scale = 4)
    private BigDecimal changeAmount;
    
    /**
     * 涨跌幅(%)
     */
    @Column(name = "change_percent", precision = 8, scale = 4)
    private BigDecimal changePercent;
    
    /**
     * 振幅(%)
     */
    @Column(name = "amplitude", precision = 8, scale = 4)
    private BigDecimal amplitude;
    
    /**
     * 市盈率
     */
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;
    
    /**
     * 市净率
     */
    @Column(name = "pb_ratio", precision = 10, scale = 2)
    private BigDecimal pbRatio;
    
    /**
     * 总市值(万元)
     */
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;
    
    /**
     * 流通市值(万元)
     */
    @Column(name = "float_market_cap", precision = 20, scale = 2)
    private BigDecimal floatMarketCap;
    
    /**
     * 委比(%)
     */
    @Column(name = "bid_ask_ratio", precision = 8, scale = 4)
    private BigDecimal bidAskRatio;
    
    /**
     * 量比
     */
    @Column(name = "volume_ratio", precision = 8, scale = 4)
    private BigDecimal volumeRatio;
    
    /**
     * 买一价
     */
    @Column(name = "bid1_price", precision = 12, scale = 4)
    private BigDecimal bid1Price;
    
    /**
     * 买一量
     */
    @Column(name = "bid1_volume", precision = 15, scale = 0)
    private BigDecimal bid1Volume;
    
    /**
     * 卖一价
     */
    @Column(name = "ask1_price", precision = 12, scale = 4)
    private BigDecimal ask1Price;
    
    /**
     * 卖一量
     */
    @Column(name = "ask1_volume", precision = 15, scale = 0)
    private BigDecimal ask1Volume;
    
    /**
     * 52周最高价
     */
    @Column(name = "week52_high", precision = 12, scale = 4)
    private BigDecimal week52High;
    
    /**
     * 52周最低价
     */
    @Column(name = "week52_low", precision = 12, scale = 4)
    private BigDecimal week52Low;
    
    /**
     * 交易状态 (TRADING-交易中, SUSPENDED-停牌, CLOSED-休市)
     */
    @Column(name = "trading_status", length = 20)
    @Builder.Default
    private String tradingStatus = "TRADING";
    
    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 计算涨跌额和涨跌幅
     */
    public void calculateChange() {
        if (closePrice != null && prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0) {
            this.changeAmount = closePrice.subtract(prevClose);
            this.changePercent = changeAmount.divide(prevClose, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }
    
    /**
     * 计算振幅
     */
    public void calculateAmplitude() {
        if (highPrice != null && lowPrice != null && prevClose != null && 
            prevClose.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal range = highPrice.subtract(lowPrice);
            this.amplitude = range.divide(prevClose, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }
    
    /**
     * 判断是否上涨
     */
    public boolean isRising() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 判断是否下跌
     */
    public boolean isFalling() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * 判断是否平盘
     */
    public boolean isFlat() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 判断是否涨停
     */
    public boolean isLimitUp() {
        return changePercent != null && changePercent.compareTo(new BigDecimal("9.9")) >= 0;
    }
    
    /**
     * 判断是否跌停
     */
    public boolean isLimitDown() {
        return changePercent != null && changePercent.compareTo(new BigDecimal("-9.9")) <= 0;
    }
    
    /**
     * 获取价格类型显示名称
     */
    public String getPriceTypeDisplayName() {
        switch (priceType) {
            case "REAL_TIME": return "实时";
            case "DAILY": return "日线";
            case "WEEKLY": return "周线";
            case "MONTHLY": return "月线";
            default: return priceType;
        }
    }
    
    /**
     * 获取交易状态显示名称
     */
    public String getTradingStatusDisplayName() {
        switch (tradingStatus) {
            case "TRADING": return "交易中";
            case "SUSPENDED": return "停牌";
            case "CLOSED": return "休市";
            default: return tradingStatus;
        }
    }
    
    /**
     * 获取涨跌状态
     */
    public String getChangeStatus() {
        if (isRising()) {
            return "UP";
        } else if (isFalling()) {
            return "DOWN";
        } else {
            return "FLAT";
        }
    }
    
    /**
     * 获取涨跌状态显示名称
     */
    public String getChangeStatusDisplayName() {
        switch (getChangeStatus()) {
            case "UP": return "上涨";
            case "DOWN": return "下跌";
            case "FLAT": return "平盘";
            default: return "未知";
        }
    }
    
    /**
     * 更新实时价格数据
     */
    public void updateRealTimeData(BigDecimal currentPrice, BigDecimal volume, 
                                  BigDecimal turnover, BigDecimal bid1Price, 
                                  BigDecimal bid1Volume, BigDecimal ask1Price, 
                                  BigDecimal ask1Volume) {
        this.closePrice = currentPrice;
        this.volume = volume;
        this.turnover = turnover;
        this.bid1Price = bid1Price;
        this.bid1Volume = bid1Volume;
        this.ask1Price = ask1Price;
        this.ask1Volume = ask1Volume;
        this.priceTime = LocalDateTime.now();
        
        // 重新计算涨跌额和涨跌幅
        calculateChange();
    }
    
    /**
     * 设置OHLC数据
     */
    public void setOHLCData(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        this.openPrice = open;
        this.highPrice = high;
        this.lowPrice = low;
        this.closePrice = close;
        
        // 重新计算相关指标
        calculateChange();
        calculateAmplitude();
    }
}