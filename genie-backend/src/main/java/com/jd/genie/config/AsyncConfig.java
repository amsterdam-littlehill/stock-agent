package com.jd.genie.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    
    /**
     * 默认异步执行器
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(10);
        
        // 最大线程数
        executor.setMaxPoolSize(50);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程名前缀
        executor.setThreadNamePrefix("StockAgent-Async-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 分析任务专用执行器
     */
    @Bean(name = "analysisTaskExecutor")
    public Executor analysisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Analysis-Task-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(300);
        
        // 拒绝策略：抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        
        log.info("分析任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 数据同步专用执行器
     */
    @Bean(name = "dataSyncExecutor")
    public Executor dataSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(3);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量
        executor.setQueueCapacity(50);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Data-Sync-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(600);
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(180);
        
        executor.initialize();
        
        log.info("数据同步执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 智能体执行专用执行器
     */
    @Bean(name = "agentExecutor")
    public Executor agentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(8);
        
        // 最大线程数
        executor.setMaxPoolSize(30);
        
        // 队列容量
        executor.setQueueCapacity(150);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Agent-Executor-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(180);
        
        // 拒绝策略：丢弃最老的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(90);
        
        executor.initialize();
        
        log.info("智能体执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 通知任务专用执行器
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(2);
        
        // 最大线程数
        executor.setMaxPoolSize(5);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Notification-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(300);
        
        // 拒绝策略：丢弃任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(false);
        
        executor.initialize();
        
        log.info("通知任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}