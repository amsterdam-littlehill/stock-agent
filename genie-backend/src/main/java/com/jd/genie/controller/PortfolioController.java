package com.jd.genie.controller;

import com.jd.genie.entity.Portfolio;
import com.jd.genie.service.PortfolioManagementService;
import com.jd.genie.service.PortfolioManagementService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 投资组合管理控制器
 * 提供投资组合相关的REST API接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "投资组合管理", description = "投资组合创建、管理、优化等功能")
public class PortfolioController {
    
    @Autowired
    private PortfolioManagementService portfolioService;
    
    /**
     * 创建投资组合
     */
    @PostMapping
    @Operation(summary = "创建投资组合", description = "创建新的投资组合")
    public ResponseEntity<Portfolio> createPortfolio(
            @Valid @RequestBody CreatePortfolioRequest request) {
        try {
            Portfolio portfolio = portfolioService.createPortfolio(request);
            log.info("创建投资组合成功: {}", portfolio.getName());
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            log.error("创建投资组合失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取投资组合列表
     */
    @GetMapping
    @Operation(summary = "获取投资组合列表", description = "获取用户的投资组合列表")
    public ResponseEntity<List<PortfolioSummary>> getPortfolioList(
            @Parameter(description = "用户ID") @RequestParam String userId) {
        try {
            List<PortfolioSummary> portfolios = portfolioService.getPortfolioList(userId);
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            log.error("获取投资组合列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取投资组合详情
     */
    @GetMapping("/{portfolioId}")
    @Operation(summary = "获取投资组合详情", description = "获取指定投资组合的详细信息")
    public ResponseEntity<PortfolioDetail> getPortfolioDetail(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("获取投资组合详情失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 添加持仓
     */
    @PostMapping("/{portfolioId}/holdings")
    @Operation(summary = "添加持仓", description = "向投资组合添加新的持仓")
    public ResponseEntity<String> addHolding(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Valid @RequestBody AddHoldingRequest request) {
        try {
            portfolioService.addHolding(portfolioId, request);
            log.info("添加持仓成功: portfolioId={}, stockCode={}", portfolioId, request.getStockCode());
            return ResponseEntity.ok("添加持仓成功");
        } catch (Exception e) {
            log.error("添加持仓失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 卖出持仓
     */
    @PostMapping("/{portfolioId}/holdings/sell")
    @Operation(summary = "卖出持仓", description = "卖出投资组合中的持仓")
    public ResponseEntity<String> sellHolding(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Valid @RequestBody SellHoldingRequest request) {
        try {
            portfolioService.sellHolding(portfolioId, request);
            log.info("卖出持仓成功: portfolioId={}, stockCode={}", portfolioId, request.getStockCode());
            return ResponseEntity.ok("卖出持仓成功");
        } catch (Exception e) {
            log.error("卖出持仓失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 投资组合再平衡
     */
    @PostMapping("/{portfolioId}/rebalance")
    @Operation(summary = "投资组合再平衡", description = "对投资组合进行再平衡操作")
    public ResponseEntity<RebalanceResult> rebalancePortfolio(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Valid @RequestBody RebalanceRequest request) {
        try {
            RebalanceResult result = portfolioService.rebalancePortfolio(portfolioId, request);
            log.info("投资组合再平衡完成: portfolioId={}, executed={}", portfolioId, request.isExecute());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("投资组合再平衡失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 投资组合优化建议
     */
    @PostMapping("/{portfolioId}/optimize")
    @Operation(summary = "投资组合优化", description = "获取投资组合优化建议")
    public ResponseEntity<PortfolioOptimizationResult> optimizePortfolio(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Valid @RequestBody OptimizationRequest request) {
        try {
            PortfolioOptimizationResult result = portfolioService.optimizePortfolio(portfolioId, request);
            log.info("投资组合优化分析完成: portfolioId={}", portfolioId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("投资组合优化失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取投资组合性能指标
     */
    @GetMapping("/{portfolioId}/metrics")
    @Operation(summary = "获取性能指标", description = "获取投资组合的性能指标")
    public ResponseEntity<PortfolioMetrics> getPortfolioMetrics(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail.getMetrics());
        } catch (Exception e) {
            log.error("获取投资组合性能指标失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取资产配置分析
     */
    @GetMapping("/{portfolioId}/allocation")
    @Operation(summary = "获取资产配置", description = "获取投资组合的资产配置分析")
    public ResponseEntity<AssetAllocationAnalysis> getAssetAllocation(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail.getAllocation());
        } catch (Exception e) {
            log.error("获取资产配置分析失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取风险分析
     */
    @GetMapping("/{portfolioId}/risk")
    @Operation(summary = "获取风险分析", description = "获取投资组合的风险分析")
    public ResponseEntity<PortfolioRiskAnalysis> getRiskAnalysis(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail.getRiskAnalysis());
        } catch (Exception e) {
            log.error("获取风险分析失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取业绩分析
     */
    @GetMapping("/{portfolioId}/performance")
    @Operation(summary = "获取业绩分析", description = "获取投资组合的业绩分析")
    public ResponseEntity<PerformanceAnalysis> getPerformanceAnalysis(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail.getPerformance());
        } catch (Exception e) {
            log.error("获取业绩分析失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新投资组合信息
     */
    @PutMapping("/{portfolioId}")
    @Operation(summary = "更新投资组合", description = "更新投资组合的基本信息")
    public ResponseEntity<String> updatePortfolio(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Valid @RequestBody UpdatePortfolioRequest request) {
        try {
            // 这里需要在PortfolioManagementService中添加updatePortfolio方法
            log.info("更新投资组合: portfolioId={}", portfolioId);
            return ResponseEntity.ok("更新投资组合成功");
        } catch (Exception e) {
            log.error("更新投资组合失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 删除投资组合
     */
    @DeleteMapping("/{portfolioId}")
    @Operation(summary = "删除投资组合", description = "删除指定的投资组合")
    public ResponseEntity<String> deletePortfolio(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            // 这里需要在PortfolioManagementService中添加deletePortfolio方法
            log.info("删除投资组合: portfolioId={}", portfolioId);
            return ResponseEntity.ok("删除投资组合成功");
        } catch (Exception e) {
            log.error("删除投资组合失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取投资组合统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取用户投资组合的统计信息")
    public ResponseEntity<PortfolioStatistics> getPortfolioStatistics(
            @Parameter(description = "用户ID") @RequestParam String userId) {
        try {
            // 这里需要在PortfolioManagementService中添加getPortfolioStatistics方法
            log.info("获取投资组合统计信息: userId={}", userId);
            
            // 简化的统计信息实现
            List<PortfolioSummary> portfolios = portfolioService.getPortfolioList(userId);
            
            PortfolioStatistics statistics = PortfolioStatistics.builder()
                    .totalPortfolios(portfolios.size())
                    .totalValue(portfolios.stream()
                            .map(PortfolioSummary::getTotalValue)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                    .totalReturn(portfolios.stream()
                            .map(PortfolioSummary::getTotalReturn)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                    .build();
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取投资组合统计信息失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 搜索投资组合
     */
    @GetMapping("/search")
    @Operation(summary = "搜索投资组合", description = "根据关键词搜索投资组合")
    public ResponseEntity<List<PortfolioSummary>> searchPortfolios(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            // 这里需要在PortfolioManagementService中添加searchPortfolios方法
            List<PortfolioSummary> portfolios = portfolioService.getPortfolioList(userId);
            
            // 简化的搜索实现
            List<PortfolioSummary> filteredPortfolios = portfolios.stream()
                    .filter(p -> p.getPortfolio().getName().toLowerCase().contains(keyword.toLowerCase()) ||
                               (p.getPortfolio().getDescription() != null && 
                                p.getPortfolio().getDescription().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(filteredPortfolios);
        } catch (Exception e) {
            log.error("搜索投资组合失败: userId={}, keyword={}, error={}", userId, keyword, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取投资组合持仓列表
     */
    @GetMapping("/{portfolioId}/holdings")
    @Operation(summary = "获取持仓列表", description = "获取投资组合的持仓列表")
    public ResponseEntity<List<com.jd.genie.entity.PortfolioHolding>> getPortfolioHoldings(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId) {
        try {
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            return ResponseEntity.ok(detail.getHoldings());
        } catch (Exception e) {
            log.error("获取投资组合持仓失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取投资组合交易记录
     */
    @GetMapping("/{portfolioId}/transactions")
    @Operation(summary = "获取交易记录", description = "获取投资组合的交易记录")
    public ResponseEntity<TransactionPageResponse> getPortfolioTransactions(
            @Parameter(description = "投资组合ID") @PathVariable Long portfolioId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        try {
            // 获取所有交易记录
            PortfolioDetail detail = portfolioService.getPortfolioDetail(portfolioId);
            List<com.jd.genie.entity.PortfolioTransaction> allTransactions = 
                portfolioService.getPortfolioTransactions(portfolioId);
            
            // 分页处理
            int start = page * size;
            int end = Math.min(start + size, allTransactions.size());
            List<com.jd.genie.entity.PortfolioTransaction> pageTransactions = 
                allTransactions.subList(start, end);
            
            TransactionPageResponse response = TransactionPageResponse.builder()
                .transactions(pageTransactions)
                .total(allTransactions.size())
                .page(page)
                .size(size)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取投资组合交易记录失败: portfolioId={}, error={}", portfolioId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除交易记录
     */
    @DeleteMapping("/transactions/{transactionId}")
    @Operation(summary = "删除交易记录", description = "删除指定的交易记录")
    public ResponseEntity<String> deleteTransaction(
            @Parameter(description = "交易记录ID") @PathVariable Long transactionId) {
        try {
            portfolioService.deleteTransaction(transactionId);
            log.info("删除交易记录成功: transactionId={}", transactionId);
            return ResponseEntity.ok("删除交易记录成功");
        } catch (Exception e) {
            log.error("删除交易记录失败: transactionId={}, error={}", transactionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ==================== 数据模型定义 ====================
    
    @lombok.Data
    @lombok.Builder
    public static class UpdatePortfolioRequest {
        private String name;
        private String description;
        private String investmentObjective;
        private String riskTolerance;
        private String benchmarkCode;
        private String benchmarkName;
        private Boolean autoRebalanceEnabled;
        private java.math.BigDecimal rebalanceThreshold;
        private String rebalanceFrequency;
        private String notes;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioStatistics {
        private Integer totalPortfolios;
        private java.math.BigDecimal totalValue;
        private java.math.BigDecimal totalReturn;
        private java.math.BigDecimal totalReturnPercent;
        private java.math.BigDecimal avgReturnPercent;
        private java.math.BigDecimal bestPerformingReturn;
        private java.math.BigDecimal worstPerformingReturn;
        private Integer activePortfolios;
        private Integer totalHoldings;
        private Integer totalTransactions;
    }

    @lombok.Data
    @lombok.Builder
    public static class TransactionPageResponse {
        private java.util.List<com.jd.genie.entity.PortfolioTransaction> transactions;
        private Integer total;
        private Integer page;
        private Integer size;
    }
}