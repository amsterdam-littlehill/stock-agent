package com.jd.genie.controller;

import com.jd.genie.entity.FinancialNews;
import com.jd.genie.service.FinancialNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财经新闻控制器
 * 提供新闻相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FinancialNewsController {

    private final FinancialNewsService newsService;

    /**
     * 获取最新新闻列表
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<FinancialNews>> getLatestNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getLatestNews(page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取最新新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热点新闻
     */
    @GetMapping("/hot")
    public ResponseEntity<Page<FinancialNews>> getHotNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getHotNews(page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取热点新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取今日热点新闻
     */
    @GetMapping("/today-hot")
    public ResponseEntity<List<FinancialNews>> getTodayHotNews(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FinancialNews> news = newsService.getTodayHotNews(limit);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取今日热点新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取趋势新闻
     */
    @GetMapping("/trending")
    public ResponseEntity<List<FinancialNews>> getTrendingNews(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FinancialNews> news = newsService.getTrendingNews(limit);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取趋势新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据股票代码获取相关新闻
     */
    @GetMapping("/stock/{stockCode}")
    public ResponseEntity<Page<FinancialNews>> getNewsByStockCode(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getNewsByStockCode(stockCode, page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取股票相关新闻失败: {}", stockCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据分类获取新闻
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<FinancialNews>> getNewsByCategory(
            @PathVariable FinancialNews.NewsCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getNewsByCategory(category, page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取分类新闻失败: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索新闻
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FinancialNews>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Page<FinancialNews> news = newsService.searchNews(keyword.trim(), page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("搜索新闻失败: {}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取正面新闻
     */
    @GetMapping("/positive")
    public ResponseEntity<Page<FinancialNews>> getPositiveNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getPositiveNews(page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取正面新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取负面新闻
     */
    @GetMapping("/negative")
    public ResponseEntity<Page<FinancialNews>> getNegativeNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FinancialNews> news = newsService.getNegativeNews(page, size);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("获取负面新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取新闻详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<FinancialNews> getNewsDetail(@PathVariable Long id) {
        try {
            // 增加阅读量
            newsService.incrementViewCount(id);
            
            // 这里需要添加获取新闻详情的方法
            // 暂时返回404，实际应该从service获取
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取新闻详情失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 点赞新闻
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> likeNews(@PathVariable Long id) {
        try {
            newsService.incrementLikeCount(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "点赞成功"
            ));
        } catch (Exception e) {
            log.error("点赞新闻失败: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "点赞失败"
            ));
        }
    }

    /**
     * 创建新闻（管理员功能）
     */
    @PostMapping
    public ResponseEntity<FinancialNews> createNews(@RequestBody FinancialNews news) {
        try {
            FinancialNews savedNews = newsService.saveNews(news);
            if (savedNews != null) {
                return ResponseEntity.ok(savedNews);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("创建新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量创建新闻（管理员功能）
     */
    @PostMapping("/batch")
    public ResponseEntity<List<FinancialNews>> createNewsBatch(@RequestBody List<FinancialNews> newsList) {
        try {
            List<FinancialNews> savedNews = newsService.saveNewsBatch(newsList);
            return ResponseEntity.ok(savedNews);
        } catch (Exception e) {
            log.error("批量创建新闻失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取新闻统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<FinancialNewsService.NewsStatistics> getNewsStatistics() {
        try {
            FinancialNewsService.NewsStatistics statistics = newsService.getNewsStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取新闻统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除新闻缓存（管理员功能）
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearNewsCache() {
        try {
            newsService.clearNewsCache();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "缓存清除成功"
            ));
        } catch (Exception e) {
            log.error("清除新闻缓存失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "缓存清除失败"
            ));
        }
    }

    /**
     * 处理未处理的新闻（管理员功能）
     */
    @PostMapping("/process-unprocessed")
    public ResponseEntity<Map<String, Object>> processUnprocessedNews() {
        try {
            newsService.processUnprocessedNews();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "开始处理未处理的新闻"
            ));
        } catch (Exception e) {
            log.error("处理未处理新闻失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "处理失败"
            ));
        }
    }

    /**
     * 删除过期新闻（管理员功能）
     */
    @DeleteMapping("/old")
    public ResponseEntity<Map<String, Object>> deleteOldNews(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        try {
            newsService.deleteOldNews(daysToKeep);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("删除 %d 天前的新闻成功", daysToKeep)
            ));
        } catch (Exception e) {
            log.error("删除过期新闻失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "删除失败"
            ));
        }
    }

    /**
     * 获取新闻分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getNewsCategories() {
        try {
            List<Map<String, Object>> categories = List.of(
                Map.of("value", FinancialNews.NewsCategory.MARKET_NEWS, "label", "市场资讯"),
                Map.of("value", FinancialNews.NewsCategory.COMPANY_NEWS, "label", "公司新闻"),
                Map.of("value", FinancialNews.NewsCategory.POLICY_NEWS, "label", "政策新闻"),
                Map.of("value", FinancialNews.NewsCategory.ECONOMIC_DATA, "label", "经济数据"),
                Map.of("value", FinancialNews.NewsCategory.INDUSTRY_NEWS, "label", "行业新闻"),
                Map.of("value", FinancialNews.NewsCategory.INTERNATIONAL_NEWS, "label", "国际新闻"),
                Map.of("value", FinancialNews.NewsCategory.RESEARCH_REPORT, "label", "研究报告"),
                Map.of("value", FinancialNews.NewsCategory.ANNOUNCEMENT, "label", "公告"),
                Map.of("value", FinancialNews.NewsCategory.OTHER, "label", "其他")
            );
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("获取新闻分类列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取重要性等级列表
     */
    @GetMapping("/importance-levels")
    public ResponseEntity<List<Map<String, Object>>> getImportanceLevels() {
        try {
            List<Map<String, Object>> levels = List.of(
                Map.of("value", FinancialNews.ImportanceLevel.CRITICAL, "label", "重大", "level", 5),
                Map.of("value", FinancialNews.ImportanceLevel.HIGH, "label", "重要", "level", 4),
                Map.of("value", FinancialNews.ImportanceLevel.MEDIUM, "label", "一般", "level", 3),
                Map.of("value", FinancialNews.ImportanceLevel.LOW, "label", "较低", "level", 2),
                Map.of("value", FinancialNews.ImportanceLevel.MINIMAL, "label", "最低", "level", 1)
            );
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            log.error("获取重要性等级列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}