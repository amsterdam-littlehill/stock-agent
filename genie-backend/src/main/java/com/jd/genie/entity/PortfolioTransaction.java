package com.jd.genie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资组合交易记录实体
 * 记录投资组合中的所有交易历史
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Entity
@Table(name = "portfolio_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioTransaction {
    
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
     * 交易流水号
     */
    @Column(name = "transaction_id", unique = true, length = 50)
    private String transactionId;
    
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
     * 交易类型 (BUY-买入, SELL-卖出, DIVIDEND-分红, SPLIT-拆股, MERGE-合股)
     */
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;
    
    /**
     * 交易数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * 交易价格
     */
    @Column(name = "price", precision = 12, scale = 4, nullable = false)
    private BigDecimal price;
    
    /**
     * 交易金额
     */
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    /**
     * 手续费
     */
    @Column(name = "commission", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;
    
    /**
     * 印花税
     */
    @Column(name = "stamp_tax", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stampTax = BigDecimal.ZERO;
    
    /**
     * 过户费
     */
    @Column(name = "transfer_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal transferFee = BigDecimal.ZERO;
    
    /**
     * 其他费用
     */
    @Column(name = "other_fees", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal otherFees = BigDecimal.ZERO;
    
    /**
     * 总费用
     */
    @Column(name = "total_fees", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFees = BigDecimal.ZERO;
    
    /**
     * 净交易金额（扣除费用后）
     */
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;
    
    /**
     * 实现盈亏（仅卖出时有值）
     */
    @Column(name = "realized_pnl", precision = 15, scale = 2)
    private BigDecimal realizedPnl;
    
    /**
     * 实现盈亏率(%)（仅卖出时有值）
     */
    @Column(name = "realized_pnl_percent", precision = 8, scale = 4)
    private BigDecimal realizedPnlPercent;
    
    /**
     * 交易前持仓数量
     */
    @Column(name = "position_before")
    private Integer positionBefore;
    
    /**
     * 交易后持仓数量
     */
    @Column(name = "position_after")
    private Integer positionAfter;
    
    /**
     * 交易前平均成本
     */
    @Column(name = "avg_cost_before", precision = 12, scale = 4)
    private BigDecimal avgCostBefore;
    
    /**
     * 交易后平均成本
     */
    @Column(name = "avg_cost_after", precision = 12, scale = 4)
    private BigDecimal avgCostAfter;
    
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
     * 汇率（相对于基础货币）
     */
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    /**
     * 交易状态 (PENDING-待成交, FILLED-已成交, CANCELLED-已取消, FAILED-失败)
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "FILLED";
    
    /**
     * 交易来源 (MANUAL-手动, AUTO-自动, REBALANCE-再平衡, STRATEGY-策略)
     */
    @Column(name = "source", length = 20)
    @Builder.Default
    private String source = "MANUAL";
    
    /**
     * 交易原因
     */
    @Column(name = "reason", length = 500)
    private String reason;
    
    /**
     * 交易策略ID
     */
    @Column(name = "strategy_id", length = 50)
    private String strategyId;
    
    /**
     * 交易策略名称
     */
    @Column(name = "strategy_name", length = 100)
    private String strategyName;
    
    /**
     * 订单ID
     */
    @Column(name = "order_id", length = 50)
    private String orderId;
    
    /**
     * 券商交易ID
     */
    @Column(name = "broker_transaction_id", length = 50)
    private String brokerTransactionId;
    
    /**
     * 交易时间
     */
    @Column(name = "trade_time")
    private LocalDateTime tradeTime;
    
    /**
     * 结算日期
     */
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;
    
    /**
     * 交易日期（YYYY-MM-DD格式）
     */
    @Column(name = "trade_date", length = 10)
    private String tradeDate;
    
    /**
     * 交易会话 (MORNING-上午, AFTERNOON-下午, NIGHT-夜盘)
     */
    @Column(name = "trading_session", length = 20)
    private String tradingSession;
    
    /**
     * 是否为T+0交易
     */
    @Column(name = "is_day_trade")
    @Builder.Default
    private Boolean isDayTrade = false;
    
    /**
     * 是否为融资融券交易
     */
    @Column(name = "is_margin_trade")
    @Builder.Default
    private Boolean isMarginTrade = false;
    
    /**
     * 融资融券类型 (CASH-现金, MARGIN_BUY-融资买入, SHORT_SELL-融券卖出)
     */
    @Column(name = "margin_type", length = 20)
    private String marginType;
    
    /**
     * 交易确认状态
     */
    @Column(name = "confirmed")
    @Builder.Default
    private Boolean confirmed = true;
    
    /**
     * 交易确认时间
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
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
     * 关联的分析任务ID
     */
    @Column(name = "analysis_task_id")
    private Long analysisTaskId;
    
    /**
     * 关联的智能体建议ID
     */
    @Column(name = "agent_recommendation_id")
    private Long agentRecommendationId;
    
    /**
     * 执行智能体
     */
    @Column(name = "executing_agent", length = 50)
    private String executingAgent;
    
    /**
     * 风险评估结果
     */
    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;
    
    /**
     * 合规检查结果
     */
    @Column(name = "compliance_check", columnDefinition = "TEXT")
    private String complianceCheck;
    
    /**
     * 交易影响分析
     */
    @Column(name = "impact_analysis", columnDefinition = "TEXT")
    private String impactAnalysis;
}