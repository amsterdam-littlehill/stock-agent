package com.jd.genie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资组合实体
 * 表示用户创建的投资组合
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Entity
@Table(name = "portfolios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    /**
     * 组合名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 组合描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 投资目标 (GROWTH-成长型, INCOME-收益型, BALANCED-平衡型, CONSERVATIVE-保守型)
     */
    @Column(name = "investment_objective", length = 20)
    private String investmentObjective;
    
    /**
     * 风险承受能力 (LOW-低风险, MEDIUM-中等风险, HIGH-高风险)
     */
    @Column(name = "risk_tolerance", length = 20)
    private String riskTolerance;
    
    /**
     * 初始资金
     */
    @Column(name = "initial_capital", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialCapital;
    
    /**
     * 当前总价值
     */
    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;
    
    /**
     * 现金余额
     */
    @Column(name = "cash_balance", precision = 15, scale = 2)
    private BigDecimal cashBalance;
    
    /**
     * 总收益
     */
    @Column(name = "total_return", precision = 15, scale = 2)
    private BigDecimal totalReturn;
    
    /**
     * 总收益率(%)
     */
    @Column(name = "total_return_percent", precision = 8, scale = 4)
    private BigDecimal totalReturnPercent;
    
    /**
     * 今日收益
     */
    @Column(name = "today_return", precision = 15, scale = 2)
    private BigDecimal todayReturn;
    
    /**
     * 今日收益率(%)
     */
    @Column(name = "today_return_percent", precision = 8, scale = 4)
    private BigDecimal todayReturnPercent;
    
    /**
     * 年化收益率(%)
     */
    @Column(name = "annualized_return", precision = 8, scale = 4)
    private BigDecimal annualizedReturn;
    
    /**
     * 夏普比率
     */
    @Column(name = "sharpe_ratio", precision = 8, scale = 4)
    private BigDecimal sharpeRatio;
    
    /**
     * 最大回撤(%)
     */
    @Column(name = "max_drawdown", precision = 8, scale = 4)
    private BigDecimal maxDrawdown;
    
    /**
     * 波动率(%)
     */
    @Column(name = "volatility", precision = 8, scale = 4)
    private BigDecimal volatility;
    
    /**
     * Beta系数
     */
    @Column(name = "beta", precision = 8, scale = 4)
    private BigDecimal beta;
    
    /**
     * 组合状态 (ACTIVE-活跃, INACTIVE-非活跃, CLOSED-已关闭)
     */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
    
    /**
     * 基准指数代码
     */
    @Column(name = "benchmark_code", length = 20)
    private String benchmarkCode;
    
    /**
     * 基准指数名称
     */
    @Column(name = "benchmark_name", length = 100)
    private String benchmarkName;
    
    /**
     * 相对基准收益(%)
     */
    @Column(name = "relative_return", precision = 8, scale = 4)
    private BigDecimal relativeReturn;
    
    /**
     * 信息比率
     */
    @Column(name = "information_ratio", precision = 8, scale = 4)
    private BigDecimal informationRatio;
    
    /**
     * 跟踪误差(%)
     */
    @Column(name = "tracking_error", precision = 8, scale = 4)
    private BigDecimal trackingError;
    
    /**
     * 持仓数量
     */
    @Column(name = "holding_count")
    private Integer holdingCount;
    
    /**
     * 交易次数
     */
    @Column(name = "transaction_count")
    private Integer transactionCount;
    
    /**
     * 胜率(%)
     */
    @Column(name = "win_rate", precision = 8, scale = 4)
    private BigDecimal winRate;
    
    /**
     * 平均持仓天数
     */
    @Column(name = "avg_holding_days")
    private Integer avgHoldingDays;
    
    /**
     * 换手率(%)
     */
    @Column(name = "turnover_rate", precision = 8, scale = 4)
    private BigDecimal turnoverRate;
    
    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    /**
     * 最后再平衡时间
     */
    @Column(name = "last_rebalanced")
    private LocalDateTime lastRebalanced;
    
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
     * 配置参数 (JSON格式)
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;
    
    /**
     * 标签 (JSON格式)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 是否公开
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;
    
    /**
     * 是否启用自动再平衡
     */
    @Column(name = "auto_rebalance_enabled")
    @Builder.Default
    private Boolean autoRebalanceEnabled = false;
    
    /**
     * 再平衡阈值(%)
     */
    @Column(name = "rebalance_threshold", precision = 8, scale = 4)
    private BigDecimal rebalanceThreshold;
    
    /**
     * 再平衡频率 (DAILY-每日, WEEKLY-每周, MONTHLY-每月, QUARTERLY-每季度)
     */
    @Column(name = "rebalance_frequency", length = 20)
    private String rebalanceFrequency;
}