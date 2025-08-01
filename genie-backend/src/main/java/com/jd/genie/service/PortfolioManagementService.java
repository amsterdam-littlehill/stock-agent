package com.jd.genie.service;

import com.jd.genie.entity.Portfolio;
import com.jd.genie.entity.PortfolioHolding;
import com.jd.genie.entity.PortfolioTransaction;
import com.jd.genie.repository.PortfolioRepository;
import com.jd.genie.repository.PortfolioHoldingRepository;
import com.jd.genie.repository.PortfolioTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 投资组合管理服务
 * 提供投资组合创建、管理、优化、风险评估等功能
 * 
 * 功能包括：
 * - 投资组合创建和管理
 * - 资产配置和再平衡
 * - 风险评估和控制
 * - 业绩分析和报告
 * - 投资组合优化
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class PortfolioManagementService {
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private PortfolioHoldingRepository holdingRepository;
    
    @Autowired
    private PortfolioTransactionRepository transactionRepository;
    
    @Autowired
    private StockDataMigrationService stockDataService;
    
    /**
     * 创建投资组合
     */
    @Transactional
    public Portfolio createPortfolio(CreatePortfolioRequest request) {
        log.info("创建投资组合: {}", request.getName());
        
        Portfolio portfolio = Portfolio.builder()
                .name(request.getName())
                .description(request.getDescription())
                .investmentObjective(request.getInvestmentObjective())
                .riskTolerance(request.getRiskTolerance())
                .initialCapital(request.getInitialCapital())
                .currentValue(request.getInitialCapital())
                .cashBalance(request.getInitialCapital())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return portfolioRepository.save(portfolio);
    }
    
    /**
     * 获取投资组合详情
     */
    public PortfolioDetail getPortfolioDetail(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("投资组合不存在: " + portfolioId));
        
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        List<PortfolioTransaction> transactions = transactionRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
        
        // 计算组合指标
        PortfolioMetrics metrics = calculatePortfolioMetrics(portfolio, holdings);
        
        // 资产配置分析
        AssetAllocationAnalysis allocation = analyzeAssetAllocation(holdings);
        
        // 风险分析
        PortfolioRiskAnalysis riskAnalysis = analyzePortfolioRisk(holdings);
        
        // 业绩分析
        PerformanceAnalysis performance = analyzePerformance(portfolio, transactions);
        
        return PortfolioDetail.builder()
                .portfolio(portfolio)
                .holdings(holdings)
                .recentTransactions(transactions.stream().limit(10).collect(Collectors.toList()))
                .metrics(metrics)
                .allocation(allocation)
                .riskAnalysis(riskAnalysis)
                .performance(performance)
                .build();
    }

    /**
     * 获取投资组合交易记录
     */
    public List<PortfolioTransaction> getPortfolioTransactions(Long portfolioId) {
        return transactionRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
    }

    /**
     * 删除交易记录
     */
    public void deleteTransaction(Long transactionId) {
        PortfolioTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("交易记录不存在: " + transactionId));
        
        transactionRepository.delete(transaction);
    }
    
    /**
     * 添加持仓
     */
    @Transactional
    public void addHolding(Long portfolioId, AddHoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("投资组合不存在: " + portfolioId));
        
        BigDecimal totalCost = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        
        // 检查现金余额
        if (portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("现金余额不足");
        }
        
        // 查找现有持仓
        Optional<PortfolioHolding> existingHolding = holdingRepository
                .findByPortfolioIdAndStockCode(portfolioId, request.getStockCode());
        
        if (existingHolding.isPresent()) {
            // 更新现有持仓
            PortfolioHolding holding = existingHolding.get();
            BigDecimal newQuantity = BigDecimal.valueOf(holding.getQuantity()).add(BigDecimal.valueOf(request.getQuantity()));
            BigDecimal newTotalCost = holding.getTotalCost().add(totalCost);
            BigDecimal newAvgPrice = newTotalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
            
            holding.setQuantity(newQuantity.intValue());
            holding.setAveragePrice(newAvgPrice);
            holding.setTotalCost(newTotalCost);
            holding.setUpdatedAt(LocalDateTime.now());
            
            holdingRepository.save(holding);
        } else {
            // 创建新持仓
            PortfolioHolding holding = PortfolioHolding.builder()
                    .portfolioId(portfolioId)
                    .stockCode(request.getStockCode())
                    .stockName(request.getStockName())
                    .quantity(request.getQuantity())
                    .averagePrice(request.getPrice())
                    .totalCost(totalCost)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            holdingRepository.save(holding);
        }
        
        // 记录交易
        PortfolioTransaction transaction = PortfolioTransaction.builder()
                .portfolioId(portfolioId)
                .stockCode(request.getStockCode())
                .transactionType("BUY")
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .totalAmount(totalCost)
                .createdAt(LocalDateTime.now())
                .build();
        
        transactionRepository.save(transaction);
        
        // 更新组合现金余额
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalCost));
        portfolio.setUpdatedAt(LocalDateTime.now());
        portfolioRepository.save(portfolio);
        
        log.info("添加持仓成功: 组合ID={}, 股票={}, 数量={}, 价格={}", 
                portfolioId, request.getStockCode(), request.getQuantity(), request.getPrice());
    }
    
    /**
     * 卖出持仓
     */
    @Transactional
    public void sellHolding(Long portfolioId, SellHoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("投资组合不存在: " + portfolioId));
        
        PortfolioHolding holding = holdingRepository
                .findByPortfolioIdAndStockCode(portfolioId, request.getStockCode())
                .orElseThrow(() -> new RuntimeException("持仓不存在: " + request.getStockCode()));
        
        if (holding.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("持仓数量不足");
        }
        
        BigDecimal sellAmount = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        
        // 更新持仓
        if (holding.getQuantity() == request.getQuantity()) {
            // 全部卖出，删除持仓
            holdingRepository.delete(holding);
        } else {
            // 部分卖出，更新持仓
            BigDecimal remainingQuantity = BigDecimal.valueOf(holding.getQuantity()).subtract(BigDecimal.valueOf(request.getQuantity()));
            BigDecimal soldCost = holding.getAveragePrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            
            holding.setQuantity(remainingQuantity.intValue());
            holding.setTotalCost(holding.getTotalCost().subtract(soldCost));
            holding.setUpdatedAt(LocalDateTime.now());
            
            holdingRepository.save(holding);
        }
        
        // 记录交易
        PortfolioTransaction transaction = PortfolioTransaction.builder()
                .portfolioId(portfolioId)
                .stockCode(request.getStockCode())
                .transactionType("SELL")
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .totalAmount(sellAmount)
                .createdAt(LocalDateTime.now())
                .build();
        
        transactionRepository.save(transaction);
        
        // 更新组合现金余额
        portfolio.setCashBalance(portfolio.getCashBalance().add(sellAmount));
        portfolio.setUpdatedAt(LocalDateTime.now());
        portfolioRepository.save(portfolio);
        
        log.info("卖出持仓成功: 组合ID={}, 股票={}, 数量={}, 价格={}", 
                portfolioId, request.getStockCode(), request.getQuantity(), request.getPrice());
    }
    
    /**
     * 投资组合再平衡
     */
    @Transactional
    public RebalanceResult rebalancePortfolio(Long portfolioId, RebalanceRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("投资组合不存在: " + portfolioId));
        
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        
        // 计算当前总价值
        BigDecimal totalValue = calculateTotalValue(portfolio, holdings);
        
        // 计算目标配置
        Map<String, BigDecimal> targetAllocations = request.getTargetAllocations();
        
        // 生成再平衡建议
        List<RebalanceAction> actions = generateRebalanceActions(holdings, targetAllocations, totalValue);
        
        // 执行再平衡（如果请求执行）
        if (request.isExecute()) {
            executeRebalanceActions(portfolioId, actions);
        }
        
        return RebalanceResult.builder()
                .portfolioId(portfolioId)
                .currentValue(totalValue)
                .actions(actions)
                .executed(request.isExecute())
                .rebalanceDate(LocalDateTime.now())
                .build();
    }
    
    /**
     * 投资组合优化建议
     */
    public PortfolioOptimizationResult optimizePortfolio(Long portfolioId, OptimizationRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("投资组合不存在: " + portfolioId));
        
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        
        // 风险收益分析
        RiskReturnAnalysis riskReturn = analyzeRiskReturn(holdings);
        
        // 相关性分析
        CorrelationMatrix correlation = analyzeCorrelation(holdings);
        
        // 生成优化建议
        List<OptimizationSuggestion> suggestions = generateOptimizationSuggestions(
                portfolio, holdings, riskReturn, correlation, request);
        
        // 计算优化后的预期指标
        ExpectedMetrics expectedMetrics = calculateExpectedMetrics(suggestions, riskReturn);
        
        return PortfolioOptimizationResult.builder()
                .portfolioId(portfolioId)
                .currentRiskReturn(riskReturn)
                .correlation(correlation)
                .suggestions(suggestions)
                .expectedMetrics(expectedMetrics)
                .optimizationDate(LocalDateTime.now())
                .build();
    }
    
    /**
     * 获取投资组合列表
     */
    public List<PortfolioSummary> getPortfolioList(String userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return portfolios.stream().map(portfolio -> {
            List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolio.getId());
            PortfolioMetrics metrics = calculatePortfolioMetrics(portfolio, holdings);
            
            return PortfolioSummary.builder()
                    .portfolio(portfolio)
                    .totalValue(metrics.getTotalValue())
                    .todayChange(metrics.getTodayChange())
                    .todayChangePercent(metrics.getTodayChangePercent())
                    .totalReturn(metrics.getTotalReturn())
                    .totalReturnPercent(metrics.getTotalReturnPercent())
                    .holdingCount(holdings.size())
                    .build();
        }).collect(Collectors.toList());
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 计算投资组合指标
     */
    private PortfolioMetrics calculatePortfolioMetrics(Portfolio portfolio, List<PortfolioHolding> holdings) {
        BigDecimal totalValue = calculateTotalValue(portfolio, holdings);
        BigDecimal totalCost = holdings.stream()
                .map(PortfolioHolding::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(portfolio.getCashBalance());
        
        BigDecimal totalReturn = totalValue.subtract(portfolio.getInitialCapital());
        BigDecimal totalReturnPercent = totalReturn.divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // 简化的今日变化计算
        BigDecimal todayChange = BigDecimal.ZERO;
        BigDecimal todayChangePercent = BigDecimal.ZERO;
        
        return PortfolioMetrics.builder()
                .totalValue(totalValue)
                .totalCost(totalCost)
                .totalReturn(totalReturn)
                .totalReturnPercent(totalReturnPercent)
                .todayChange(todayChange)
                .todayChangePercent(todayChangePercent)
                .cashBalance(portfolio.getCashBalance())
                .assetCount(holdings.size())
                .build();
    }
    
    /**
     * 计算总价值
     */
    private BigDecimal calculateTotalValue(Portfolio portfolio, List<PortfolioHolding> holdings) {
        BigDecimal holdingsValue = holdings.stream()
                .map(holding -> {
                    // 这里应该获取实时价格，简化为使用平均价格
                    return holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return holdingsValue.add(portfolio.getCashBalance());
    }
    
    /**
     * 分析资产配置
     */
    private AssetAllocationAnalysis analyzeAssetAllocation(List<PortfolioHolding> holdings) {
        Map<String, BigDecimal> sectorAllocation = new HashMap<>();
        Map<String, BigDecimal> stockAllocation = new HashMap<>();
        
        BigDecimal totalValue = holdings.stream()
                .map(holding -> holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (PortfolioHolding holding : holdings) {
            BigDecimal value = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
            BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            
            stockAllocation.put(holding.getStockCode(), percentage);
            
            // 简化的行业分类
            String sector = getSectorByStockCode(holding.getStockCode());
            sectorAllocation.merge(sector, percentage, BigDecimal::add);
        }
        
        return AssetAllocationAnalysis.builder()
                .sectorAllocation(sectorAllocation)
                .stockAllocation(stockAllocation)
                .concentrationRisk(calculateConcentrationRisk(stockAllocation))
                .diversificationScore(calculateDiversificationScore(sectorAllocation))
                .build();
    }
    
    /**
     * 分析投资组合风险
     */
    private PortfolioRiskAnalysis analyzePortfolioRisk(List<PortfolioHolding> holdings) {
        // 简化的风险分析实现
        double portfolioVolatility = 0.15; // 假设组合波动率
        double portfolioBeta = 1.0; // 假设组合Beta
        double var95 = 0.05; // 假设VaR
        double maxDrawdown = 0.20; // 假设最大回撤
        
        String riskLevel = determineRiskLevel(portfolioVolatility, var95);
        
        List<String> riskFactors = Arrays.asList(
                "市场风险", "行业集中度风险", "个股风险"
        );
        
        return PortfolioRiskAnalysis.builder()
                .volatility(portfolioVolatility)
                .beta(portfolioBeta)
                .var95(var95)
                .maxDrawdown(maxDrawdown)
                .riskLevel(riskLevel)
                .riskFactors(riskFactors)
                .build();
    }
    
    /**
     * 分析业绩表现
     */
    private PerformanceAnalysis analyzePerformance(Portfolio portfolio, List<PortfolioTransaction> transactions) {
        // 简化的业绩分析实现
        BigDecimal totalReturn = portfolio.getCurrentValue().subtract(portfolio.getInitialCapital());
        BigDecimal returnPercent = totalReturn.divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return PerformanceAnalysis.builder()
                .totalReturn(totalReturn)
                .totalReturnPercent(returnPercent)
                .annualizedReturn(returnPercent) // 简化
                .sharpeRatio(1.2) // 假设夏普比率
                .maxDrawdown(0.15) // 假设最大回撤
                .winRate(0.65) // 假设胜率
                .transactionCount(transactions.size())
                .build();
    }
    
    // 其他辅助方法的简化实现
    private String getSectorByStockCode(String stockCode) {
        // 简化的行业分类
        if (stockCode.startsWith("00")) return "主板";
        if (stockCode.startsWith("30")) return "创业板";
        if (stockCode.startsWith("68")) return "科创板";
        return "其他";
    }
    
    private double calculateConcentrationRisk(Map<String, BigDecimal> stockAllocation) {
        // 计算集中度风险（赫芬达尔指数）
        return stockAllocation.values().stream()
                .mapToDouble(percentage -> Math.pow(percentage.doubleValue() / 100, 2))
                .sum();
    }
    
    private double calculateDiversificationScore(Map<String, BigDecimal> sectorAllocation) {
        // 计算分散化评分
        int sectorCount = sectorAllocation.size();
        double maxAllocation = sectorAllocation.values().stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max().orElse(100.0);
        
        return Math.max(0, 100 - maxAllocation) * (sectorCount / 10.0);
    }
    
    private String determineRiskLevel(double volatility, double var95) {
        if (volatility > 0.25 || var95 > 0.08) return "高风险";
        if (volatility > 0.15 || var95 > 0.05) return "中等风险";
        return "低风险";
    }
    
    // 再平衡相关方法的简化实现
    private List<RebalanceAction> generateRebalanceActions(List<PortfolioHolding> holdings, 
                                                         Map<String, BigDecimal> targetAllocations, 
                                                         BigDecimal totalValue) {
        List<RebalanceAction> actions = new ArrayList<>();
        // 简化实现
        return actions;
    }
    
    private void executeRebalanceActions(Long portfolioId, List<RebalanceAction> actions) {
        // 执行再平衡操作
        log.info("执行投资组合再平衡: 组合ID={}, 操作数量={}", portfolioId, actions.size());
    }
    
    private RiskReturnAnalysis analyzeRiskReturn(List<PortfolioHolding> holdings) {
        // 简化的风险收益分析
        return RiskReturnAnalysis.builder()
                .expectedReturn(0.12)
                .volatility(0.18)
                .sharpeRatio(0.67)
                .build();
    }
    
    private CorrelationMatrix analyzeCorrelation(List<PortfolioHolding> holdings) {
        // 简化的相关性分析
        return CorrelationMatrix.builder()
                .averageCorrelation(0.6)
                .maxCorrelation(0.85)
                .minCorrelation(0.2)
                .build();
    }
    
    private List<OptimizationSuggestion> generateOptimizationSuggestions(Portfolio portfolio, 
                                                                        List<PortfolioHolding> holdings,
                                                                        RiskReturnAnalysis riskReturn,
                                                                        CorrelationMatrix correlation,
                                                                        OptimizationRequest request) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        // 简化实现
        return suggestions;
    }
    
    private ExpectedMetrics calculateExpectedMetrics(List<OptimizationSuggestion> suggestions, 
                                                   RiskReturnAnalysis currentRiskReturn) {
        // 简化的预期指标计算
        return ExpectedMetrics.builder()
                .expectedReturn(currentRiskReturn.getExpectedReturn() * 1.1)
                .expectedVolatility(currentRiskReturn.getVolatility() * 0.9)
                .expectedSharpeRatio(currentRiskReturn.getSharpeRatio() * 1.2)
                .build();
    }
    
    // ==================== 数据模型定义 ====================
    
    @lombok.Data
    @lombok.Builder
    public static class CreatePortfolioRequest {
        private String name;
        private String description;
        private String investmentObjective;
        private String riskTolerance;
        private BigDecimal initialCapital;
        private String userId;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AddHoldingRequest {
        private String stockCode;
        private String stockName;
        private Integer quantity;
        private BigDecimal price;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SellHoldingRequest {
        private String stockCode;
        private Integer quantity;
        private BigDecimal price;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalanceRequest {
        private Map<String, BigDecimal> targetAllocations;
        private boolean execute;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizationRequest {
        private String objective; // "MAX_RETURN", "MIN_RISK", "MAX_SHARPE"
        private BigDecimal targetReturn;
        private BigDecimal maxRisk;
        private Map<String, BigDecimal> constraints;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioDetail {
        private Portfolio portfolio;
        private List<PortfolioHolding> holdings;
        private List<PortfolioTransaction> recentTransactions;
        private PortfolioMetrics metrics;
        private AssetAllocationAnalysis allocation;
        private PortfolioRiskAnalysis riskAnalysis;
        private PerformanceAnalysis performance;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioSummary {
        private Portfolio portfolio;
        private BigDecimal totalValue;
        private BigDecimal todayChange;
        private BigDecimal todayChangePercent;
        private BigDecimal totalReturn;
        private BigDecimal totalReturnPercent;
        private Integer holdingCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioMetrics {
        private BigDecimal totalValue;
        private BigDecimal totalCost;
        private BigDecimal totalReturn;
        private BigDecimal totalReturnPercent;
        private BigDecimal todayChange;
        private BigDecimal todayChangePercent;
        private BigDecimal cashBalance;
        private Integer assetCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AssetAllocationAnalysis {
        private Map<String, BigDecimal> sectorAllocation;
        private Map<String, BigDecimal> stockAllocation;
        private Double concentrationRisk;
        private Double diversificationScore;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioRiskAnalysis {
        private Double volatility;
        private Double beta;
        private Double var95;
        private Double maxDrawdown;
        private String riskLevel;
        private List<String> riskFactors;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PerformanceAnalysis {
        private BigDecimal totalReturn;
        private BigDecimal totalReturnPercent;
        private BigDecimal annualizedReturn;
        private Double sharpeRatio;
        private Double maxDrawdown;
        private Double winRate;
        private Integer transactionCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalanceResult {
        private Long portfolioId;
        private BigDecimal currentValue;
        private List<RebalanceAction> actions;
        private Boolean executed;
        private LocalDateTime rebalanceDate;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalanceAction {
        private String stockCode;
        private String actionType; // "BUY", "SELL"
        private Integer quantity;
        private BigDecimal targetWeight;
        private BigDecimal currentWeight;
        private String reason;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioOptimizationResult {
        private Long portfolioId;
        private RiskReturnAnalysis currentRiskReturn;
        private CorrelationMatrix correlation;
        private List<OptimizationSuggestion> suggestions;
        private ExpectedMetrics expectedMetrics;
        private LocalDateTime optimizationDate;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskReturnAnalysis {
        private Double expectedReturn;
        private Double volatility;
        private Double sharpeRatio;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class CorrelationMatrix {
        private Double averageCorrelation;
        private Double maxCorrelation;
        private Double minCorrelation;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizationSuggestion {
        private String type; // "ADD", "REMOVE", "ADJUST"
        private String stockCode;
        private BigDecimal suggestedWeight;
        private String reason;
        private Double expectedImpact;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ExpectedMetrics {
        private Double expectedReturn;
        private Double expectedVolatility;
        private Double expectedSharpeRatio;
    }
}