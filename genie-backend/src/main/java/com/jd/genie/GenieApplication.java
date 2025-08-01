package com.jd.genie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Stock Agent 主应用类
 * 基于JoyAgent-JDGenie框架的股票分析智能体系统
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务
@EnableCaching     // 启用缓存
@EnableAsync       // 启用异步处理
public class GenieApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenieApplication.class, args);
    }
}