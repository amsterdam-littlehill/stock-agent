package com.jd.genie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资组合持仓实体
 * 表示投资组合中的具体持仓信息
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Entity
@Table(name = "portfolio_holdings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioHolding {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 投资组合ID
     */
    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;
    
    /**
     * 股票名称
     */
    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;
    
    /**
     * 持仓数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * 可用数量
     */
    @Column(name = "available_quantity")
    private Integer availableQuantity;
    
    /**
     * 冻结数量
     */
    @Column(name = "frozen_quantity")
    @Builder.Default
    private Integer frozenQuantity = 0;
    
    /**
     * 平均成本价
     */
    @Column(name = "average_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal averagePrice;
    
    /**
     * 当前价格
     */
    @Column(name = "current_price", precision = 12, scale = 4)
    private BigDecimal currentPrice;
    
    /**
     * 总成本
     */
    @Column(name = "total_cost", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalCost;
    
    /**
     * 当前市值
     */
    @Column(name = "market_value", precision = 15, scale = 2)
    private BigDecimal marketValue;
    
    /**
     * 浮动盈亏
     */
    @Column(name = "unrealized_pnl", precision = 15, scale = 2)
    private BigDecimal unrealizedPnl;
    
    /**
     * 浮动盈亏率(%)
     */
    @Column(name = "unrealized_pnl_percent", precision = 8, scale = 4)
    private BigDecimal unrealizedPnlPercent;
    
    /**
     * 已实现盈亏
     */
    @Column(name = "realized_pnl", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    /**
     * 已实现盈亏率(%)
     */
    @Column(name = "realized_pnl_percent", precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal realizedPnlPercent = BigDecimal.ZERO;
    
    /**
     * 今日盈亏
     */
    @Column(name = "today_pnl", precision = 15, scale = 2)
    private BigDecimal todayPnl;
    
    /**
     * 今日盈亏率(%)
     */
    @Column(name = "today_pnl_percent", precision = 8, scale = 4)
    private BigDecimal todayPnlPercent;
    
    /**
     * 权重(%)
     */
    @Column(name = "weight", precision = 8, scale = 4)
    private BigDecimal weight;
    
    /**
     * 目标权重(%)
     */
    @Column(name = "target_weight", precision = 8, scale = 4)
    private BigDecimal targetWeight;
    
    /**
     * 行业分类
     */
    @Column(name = "sector", length = 50)
    private String sector;
    
    /**
     * 行业代码
     */
    @Column(name = "sector_code", length = 20)
    private String sectorCode;
    
    /**
     * 市场类型 (SH-上海, SZ-深圳, HK-香港, US-美国)
     */
    @Column(name = "market", length = 10)
    private String market;
    
    /**
     * 货币类型 (CNY-人民币, HKD-港币, USD-美元)
     */
    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";
    
    /**
     * 持仓状态 (NORMAL-正常, SUSPENDED-停牌, DELISTED-退市)
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "NORMAL";
    
    /**
     * 首次买入时间
     */
    @Column(name = "first_buy_date")
    private LocalDateTime firstBuyDate;
    
    /**
     * 最后交易时间
     */
    @Column(name = "last_trade_date")
    private LocalDateTime lastTradeDate;
    
    /**
     * 持仓天数
     */
    @Column(name = "holding_days")
    private Integer holdingDays;
    
    /**
     * 买入次数
     */
    @Column(name = "buy_count")
    @Builder.Default
    private Integer buyCount = 0;
    
    /**
     * 卖出次数
     */
    @Column(name = "sell_count")
    @Builder.Default
    private Integer sellCount = 0;
    
    /**
     * 分红金额
     */
    @Column(name = "dividend_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal dividendAmount = BigDecimal.ZERO;
    
    /**
     * 分红次数
     */
    @Column(name = "dividend_count")
    @Builder.Default
    private Integer dividendCount = 0;
    
    /**
     * 股息率(%)
     */
    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;
    
    /**
     * 最高价格
     */
    @Column(name = "highest_price", precision = 12, scale = 4)
    private BigDecimal highestPrice;
    
    /**
     * 最低价格
     */
    @Column(name = "lowest_price", precision = 12, scale = 4)
    private BigDecimal lowestPrice;
    
    /**
     * 止损价格
     */
    @Column(name = "stop_loss_price", precision = 12, scale = 4)
    private BigDecimal stopLossPrice;
    
    /**
     * 止盈价格
     */
    @Column(name = "take_profit_price", precision = 12, scale = 4)
    private BigDecimal takeProfitPrice;
    
    /**
     * 风险等级 (LOW-低风险, MEDIUM-中等风险, HIGH-高风险)
     */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    /**
     * 投资评级 (BUY-买入, HOLD-持有, SELL-卖出)
     */
    @Column(name = "rating", length = 20)
    private String rating;
    
    /**
     * 目标价格
     */
    @Column(name = "target_price", precision = 12, scale = 4)
    private BigDecimal targetPrice;
    
    /**
     * 价格更新时间
     */
    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 备注
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    /**
     * 扩展属性 (JSON格式)
     */
    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes;
    
    /**
     * 是否启用止损
     */
    @Column(name = "stop_loss_enabled")
    @Builder.Default
    private Boolean stopLossEnabled = false;
    
    /**
     * 是否启用止盈
     */
    @Column(name = "take_profit_enabled")
    @Builder.Default
    private Boolean takeProfitEnabled = false;
    
    /**
     * 是否核心持仓
     */
    @Column(name = "is_core_holding")
    @Builder.Default
    private Boolean isCoreHolding = false;
    
    /**
     * 建仓原因
     */
    @Column(name = "buy_reason", length = 500)
    private String buyReason;
    
    /**
     * 投资逻辑
     */
    @Column(name = "investment_thesis", columnDefinition = "TEXT")
    private String investmentThesis;
}