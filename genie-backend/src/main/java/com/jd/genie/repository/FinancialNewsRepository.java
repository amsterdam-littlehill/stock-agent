package com.jd.genie.repository;

import com.jd.genie.entity.FinancialNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 财经新闻数据访问接口
 */
@Repository
public interface FinancialNewsRepository extends JpaRepository<FinancialNews, Long> {

    /**
     * 根据标题查找新闻（用于去重）
     */
    Optional<FinancialNews> findByTitle(String title);

    /**
     * 根据原始URL查找新闻（用于去重）
     */
    Optional<FinancialNews> findByOriginalUrl(String originalUrl);

    /**
     * 查找指定时间范围内的新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.publishTime BETWEEN :startTime AND :endTime ORDER BY n.publishTime DESC")
    Page<FinancialNews> findByPublishTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 根据新闻来源查找
     */
    Page<FinancialNews> findBySourceOrderByPublishTimeDesc(String source, Pageable pageable);

    /**
     * 根据新闻分类查找
     */
    Page<FinancialNews> findByCategoryOrderByPublishTimeDesc(FinancialNews.NewsCategory category, Pageable pageable);

    /**
     * 根据重要性等级查找
     */
    Page<FinancialNews> findByImportanceLevelOrderByPublishTimeDesc(FinancialNews.ImportanceLevel importanceLevel, Pageable pageable);

    /**
     * 查找热点新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.isHot = true ORDER BY n.hotScore DESC, n.publishTime DESC")
    Page<FinancialNews> findHotNews(Pageable pageable);

    /**
     * 根据股票代码查找相关新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.relatedStockCodes LIKE %:stockCode% ORDER BY n.publishTime DESC")
    Page<FinancialNews> findByRelatedStockCode(@Param("stockCode") String stockCode, Pageable pageable);

    /**
     * 根据多个股票代码查找相关新闻
     */
    @Query("SELECT DISTINCT n FROM FinancialNews n WHERE " +
           "EXISTS (SELECT 1 FROM (SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(n.relatedStockCodes, ',', numbers.n), ',', -1)) as code " +
           "FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) numbers " +
           "WHERE CHAR_LENGTH(n.relatedStockCodes) - CHAR_LENGTH(REPLACE(n.relatedStockCodes, ',', '')) >= numbers.n - 1) codes " +
           "WHERE codes.code IN :stockCodes) " +
           "ORDER BY n.publishTime DESC")
    Page<FinancialNews> findByRelatedStockCodes(@Param("stockCodes") List<String> stockCodes, Pageable pageable);

    /**
     * 根据情感分析得分范围查找
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.sentimentScore BETWEEN :minScore AND :maxScore ORDER BY n.publishTime DESC")
    Page<FinancialNews> findBySentimentScoreBetween(
        @Param("minScore") Double minScore,
        @Param("maxScore") Double maxScore,
        Pageable pageable
    );

    /**
     * 查找正面新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.sentimentScore > 0.1 ORDER BY n.sentimentScore DESC, n.publishTime DESC")
    Page<FinancialNews> findPositiveNews(Pageable pageable);

    /**
     * 查找负面新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.sentimentScore < -0.1 ORDER BY n.sentimentScore ASC, n.publishTime DESC")
    Page<FinancialNews> findNegativeNews(Pageable pageable);

    /**
     * 根据关键词搜索新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE " +
           "n.title LIKE %:keyword% OR n.content LIKE %:keyword% OR n.summary LIKE %:keyword% OR n.tags LIKE %:keyword% " +
           "ORDER BY n.publishTime DESC")
    Page<FinancialNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找未处理的新闻（用于AI分析）
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.processed = false ORDER BY n.publishTime DESC")
    Page<FinancialNews> findUnprocessedNews(Pageable pageable);

    /**
     * 查找最新的N条新闻
     */
    @Query("SELECT n FROM FinancialNews n ORDER BY n.publishTime DESC")
    List<FinancialNews> findLatestNews(Pageable pageable);

    /**
     * 根据热度分数排序查找新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.hotScore > :minScore ORDER BY n.hotScore DESC, n.publishTime DESC")
    Page<FinancialNews> findByHotScoreGreaterThan(@Param("minScore") Double minScore, Pageable pageable);

    /**
     * 统计指定时间范围内的新闻数量
     */
    @Query("SELECT COUNT(n) FROM FinancialNews n WHERE n.publishTime BETWEEN :startTime AND :endTime")
    Long countByPublishTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各分类的新闻数量
     */
    @Query("SELECT n.category, COUNT(n) FROM FinancialNews n GROUP BY n.category")
    List<Object[]> countByCategory();

    /**
     * 统计各来源的新闻数量
     */
    @Query("SELECT n.source, COUNT(n) FROM FinancialNews n GROUP BY n.source ORDER BY COUNT(n) DESC")
    List<Object[]> countBySource();

    /**
     * 查找指定股票的最新新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.relatedStockCodes LIKE %:stockCode% " +
           "ORDER BY n.publishTime DESC")
    List<FinancialNews> findLatestNewsByStockCode(@Param("stockCode") String stockCode, Pageable pageable);

    /**
     * 查找今日热点新闻
     */
    @Query("SELECT n FROM FinancialNews n WHERE " +
           "n.publishTime >= :todayStart AND n.publishTime < :tomorrowStart " +
           "ORDER BY n.hotScore DESC, n.publishTime DESC")
    List<FinancialNews> findTodayHotNews(
        @Param("todayStart") LocalDateTime todayStart,
        @Param("tomorrowStart") LocalDateTime tomorrowStart,
        Pageable pageable
    );

    /**
     * 更新新闻处理状态
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.processed = :processed WHERE n.id = :id")
    void updateProcessedStatus(@Param("id") Long id, @Param("processed") Boolean processed);

    /**
     * 批量更新新闻处理状态
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.processed = :processed WHERE n.id IN :ids")
    void updateProcessedStatusBatch(@Param("ids") List<Long> ids, @Param("processed") Boolean processed);

    /**
     * 更新新闻AI分析结果
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.aiAnalysis = :analysis, n.processed = true WHERE n.id = :id")
    void updateAiAnalysis(@Param("id") Long id, @Param("analysis") String analysis);

    /**
     * 更新新闻情感分析得分
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.sentimentScore = :score WHERE n.id = :id")
    void updateSentimentScore(@Param("id") Long id, @Param("score") Double score);

    /**
     * 更新新闻热度分数
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.hotScore = :score, n.isHot = :isHot WHERE n.id = :id")
    void updateHotScore(@Param("id") Long id, @Param("score") Double score, @Param("isHot") Boolean isHot);

    /**
     * 增加新闻阅读量
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加新闻点赞数
     */
    @Modifying
    @Query("UPDATE FinancialNews n SET n.likeCount = n.likeCount + 1 WHERE n.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 删除指定时间之前的新闻（数据清理）
     */
    @Modifying
    @Query("DELETE FROM FinancialNews n WHERE n.publishTime < :cutoffTime")
    void deleteOldNews(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找重复新闻（基于标题相似度）
     */
    @Query("SELECT n FROM FinancialNews n WHERE n.title LIKE %:titlePattern% AND n.id != :excludeId")
    List<FinancialNews> findSimilarNewsByTitle(@Param("titlePattern") String titlePattern, @Param("excludeId") Long excludeId);

    /**
     * 获取新闻统计信息
     */
    @Query("SELECT " +
           "COUNT(n) as totalCount, " +
           "COUNT(CASE WHEN n.isHot = true THEN 1 END) as hotCount, " +
           "COUNT(CASE WHEN n.processed = true THEN 1 END) as processedCount, " +
           "AVG(n.sentimentScore) as avgSentiment, " +
           "MAX(n.hotScore) as maxHotScore " +
           "FROM FinancialNews n WHERE n.publishTime >= :startTime")
    Object[] getNewsStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 查找趋势新闻（基于热度变化）
     */
    @Query("SELECT n FROM FinancialNews n WHERE " +
           "n.publishTime >= :recentTime AND n.hotScore > :minHotScore " +
           "ORDER BY (n.hotScore / TIMESTAMPDIFF(HOUR, n.publishTime, NOW()) + 1) DESC")
    List<FinancialNews> findTrendingNews(
        @Param("recentTime") LocalDateTime recentTime,
        @Param("minHotScore") Double minHotScore,
        Pageable pageable
    );
}