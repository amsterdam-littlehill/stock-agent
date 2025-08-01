package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票基础信息实体类
 */
@Entity
@Table(name = "stock_info", indexes = {
    @Index(name = "idx_stock_code", columnList = "stock_code", unique = true),
    @Index(name = "idx_market", columnList = "market"),
    @Index(name = "idx_industry", columnList = "industry"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false, unique = true, length = 20)
    private String stockCode;
    
    /**
     * 股票名称
     */
    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;
    
    /**
     * 市场类型 (SH-上海, SZ-深圳, HK-香港, US-美股)
     */
    @Column(name = "market", nullable = false, length = 10)
    private String market;
    
    /**
     * 行业分类
     */
    @Column(name = "industry", length = 100)
    private String industry;
    
    /**
     * 所属板块
     */
    @Column(name = "sector", length = 100)
    private String sector;
    
    /**
     * 公司全称
     */
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    /**
     * 上市日期
     */
    @Column(name = "list_date")
    private LocalDateTime listDate;
    
    /**
     * 总股本(万股)
     */
    @Column(name = "total_shares", precision = 20, scale = 2)
    private BigDecimal totalShares;
    
    /**
     * 流通股本(万股)
     */
    @Column(name = "float_shares", precision = 20, scale = 2)
    private BigDecimal floatShares;
    
    /**
     * 市值(万元)
     */
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;
    
    /**
     * 流通市值(万元)
     */
    @Column(name = "float_market_cap", precision = 20, scale = 2)
    private BigDecimal floatMarketCap;
    
    /**
     * 每股净资产
     */
    @Column(name = "book_value_per_share", precision = 10, scale = 4)
    private BigDecimal bookValuePerShare;
    
    /**
     * 每股收益(TTM)
     */
    @Column(name = "earnings_per_share", precision = 10, scale = 4)
    private BigDecimal earningsPerShare;
    
    /**
     * 市盈率(TTM)
     */
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;
    
    /**
     * 市净率
     */
    @Column(name = "pb_ratio", precision = 10, scale = 2)
    private BigDecimal pbRatio;
    
    /**
     * 股息率(%)
     */
    @Column(name = "dividend_yield", precision = 6, scale = 2)
    private BigDecimal dividendYield;
    
    /**
     * ROE(净资产收益率)
     */
    @Column(name = "roe", precision = 6, scale = 2)
    private BigDecimal roe;
    
    /**
     * ROA(总资产收益率)
     */
    @Column(name = "roa", precision = 6, scale = 2)
    private BigDecimal roa;
    
    /**
     * 毛利率(%)
     */
    @Column(name = "gross_margin", precision = 6, scale = 2)
    private BigDecimal grossMargin;
    
    /**
     * 净利率(%)
     */
    @Column(name = "net_margin", precision = 6, scale = 2)
    private BigDecimal netMargin;
    
    /**
     * 资产负债率(%)
     */
    @Column(name = "debt_ratio", precision = 6, scale = 2)
    private BigDecimal debtRatio;
    
    /**
     * 流动比率
     */
    @Column(name = "current_ratio", precision = 6, scale = 2)
    private BigDecimal currentRatio;
    
    /**
     * 速动比率
     */
    @Column(name = "quick_ratio", precision = 6, scale = 2)
    private BigDecimal quickRatio;
    
    /**
     * 股票状态 (ACTIVE-正常交易, SUSPENDED-停牌, DELISTED-退市)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    
    /**
     * 是否为ST股票
     */
    @Column(name = "is_st")
    @Builder.Default
    private Boolean isSt = false;
    
    /**
     * 描述信息
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;
    
    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 获取完整的股票代码(包含市场前缀)
     */
    public String getFullStockCode() {
        if (market == null || stockCode == null) {
            return stockCode;
        }
        return market.toLowerCase() + stockCode;
    }
    
    /**
     * 判断是否为A股
     */
    public boolean isAStock() {
        return "SH".equals(market) || "SZ".equals(market);
    }
    
    /**
     * 判断是否为港股
     */
    public boolean isHKStock() {
        return "HK".equals(market);
    }
    
    /**
     * 判断是否为美股
     */
    public boolean isUSStock() {
        return "US".equals(market);
    }
    
    /**
     * 获取市场显示名称
     */
    public String getMarketDisplayName() {
        switch (market) {
            case "SH": return "上海证券交易所";
            case "SZ": return "深圳证券交易所";
            case "HK": return "香港交易所";
            case "US": return "美国股市";
            default: return market;
        }
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case "ACTIVE": return "正常交易";
            case "SUSPENDED": return "停牌";
            case "DELISTED": return "退市";
            default: return status;
        }
    }
    
    /**
     * 更新财务数据
     */
    public void updateFinancialData(BigDecimal totalShares, BigDecimal floatShares, 
                                   BigDecimal marketCap, BigDecimal floatMarketCap,
                                   BigDecimal bookValuePerShare, BigDecimal earningsPerShare,
                                   BigDecimal peRatio, BigDecimal pbRatio) {
        this.totalShares = totalShares;
        this.floatShares = floatShares;
        this.marketCap = marketCap;
        this.floatMarketCap = floatMarketCap;
        this.bookValuePerShare = bookValuePerShare;
        this.earningsPerShare = earningsPerShare;
        this.peRatio = peRatio;
        this.pbRatio = pbRatio;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新盈利能力指标
     */
    public void updateProfitabilityRatios(BigDecimal roe, BigDecimal roa, 
                                         BigDecimal grossMargin, BigDecimal netMargin) {
        this.roe = roe;
        this.roa = roa;
        this.grossMargin = grossMargin;
        this.netMargin = netMargin;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 更新偿债能力指标
     */
    public void updateSolvencyRatios(BigDecimal debtRatio, BigDecimal currentRatio, 
                                    BigDecimal quickRatio) {
        this.debtRatio = debtRatio;
        this.currentRatio = currentRatio;
        this.quickRatio = quickRatio;
        this.lastUpdated = LocalDateTime.now();
    }
}