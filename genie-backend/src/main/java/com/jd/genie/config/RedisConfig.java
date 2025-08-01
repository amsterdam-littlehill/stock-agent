package com.jd.genie.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    
    @Value("${spring.redis.host:localhost}")
    private String host;
    
    @Value("${spring.redis.port:6379}")
    private int port;
    
    @Value("${spring.redis.password:}")
    private String password;
    
    @Value("${spring.redis.database:0}")
    private int database;
    
    /**
     * Redis连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        
        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }
        
        return new LettuceConnectionFactory(config);
    }
    
    /**
     * RedisTemplate配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // JSON序列化配置
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        
        // String序列化配置
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        // value采用jackson的序列化方式
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                .disableCachingNullValues();
        
        // 不同缓存的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 股票基础信息缓存 - 6小时
        cacheConfigurations.put("stock-info", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6)));
        
        // 实时价格缓存 - 1分钟
        cacheConfigurations.put("real-time-price", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(1)));
        
        // K线数据缓存 - 30分钟
        cacheConfigurations.put("kline-data", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 分析任务缓存 - 2小时
        cacheConfigurations.put("analysis-task", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)));
        
        // 分析结果缓存 - 4小时
        cacheConfigurations.put("analysis-result", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(4)));
        
        // 智能体信息缓存 - 12小时
        cacheConfigurations.put("agent-info", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(12)));
        
        // 统计数据缓存 - 30分钟
        cacheConfigurations.put("statistics", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 搜索结果缓存 - 15分钟
        cacheConfigurations.put("search-results", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(15)));
        
        // 市场数据缓存 - 5分钟
        cacheConfigurations.put("market-data", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
        
        // 行业分析缓存 - 1小时
        cacheConfigurations.put("industry-analysis", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // 任务结果缓存 - 2小时
        cacheConfigurations.put("task-results", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)));
        
        // 最新股票分析缓存 - 30分钟
        cacheConfigurations.put("latest-stock-analysis", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 综合分析缓存 - 1小时
        cacheConfigurations.put("comprehensive-analysis", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // 按类型分析缓存 - 45分钟
        cacheConfigurations.put("analysis-by-type", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(45)));
        
        // 分析结果统计缓存 - 30分钟
        cacheConfigurations.put("analysis-result-statistics", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 股票分析统计缓存 - 1小时
        cacheConfigurations.put("stock-analysis-statistics", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // 推荐分布缓存 - 30分钟
        cacheConfigurations.put("recommendation-distribution", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 智能体性能统计缓存 - 1小时
        cacheConfigurations.put("agent-performance-stats", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // 分析趋势缓存 - 30分钟
        cacheConfigurations.put("analysis-trend", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * 自定义缓存key生成器
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(".");
            sb.append(method.getName()).append("(");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                if (params[i] != null) {
                    sb.append(params[i].toString());
                } else {
                    sb.append("null");
                }
            }
            sb.append(")");
            return sb.toString();
        };
    }
}