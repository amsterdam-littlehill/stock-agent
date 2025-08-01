package com.jd.genie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.TimeZone;

/**
 * Stock Agent Genie 智能体协调器应用启动类
 * 
 * 功能特性：
 * - 智能体协调和管理
 * - 用户自定义工作流引擎
 * - MCP工具集成服务
 * - 实时WebSocket通信
 * - 数据持久化和缓存
 * - 异步任务处理
 * - 定时任务调度
 * - 性能监控和健康检查
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(
    scanBasePackages = {
        "com.jd.genie.agent",
        "com.jd.genie.config",
        "com.jd.genie.common"
    }
)
@EnableJpaRepositories(
    basePackages = "com.jd.genie.agent.**.repository"
)
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableWebSocket
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
public class StockAgentGenieApplication {

    /**
     * 应用启动入口
     * 
     * @param args 启动参数
     */
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("spring.application.name", "stock-agent-genie");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.timezone", "Asia/Shanghai");
        
        // 启动应用
        SpringApplication application = new SpringApplication(StockAgentGenieApplication.class);
        
        // 设置默认配置文件
        application.setDefaultProperties(getDefaultProperties());
        
        // 启动应用
        var context = application.run(args);
        
        // 打印启动信息
        printStartupInfo(context.getEnvironment().getProperty("server.port", "8080"));
    }
    
    /**
     * 应用初始化
     */
    @PostConstruct
    public void init() {
        // 设置默认时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 打印初始化信息
        System.out.println("========================================");
        System.out.println("Stock Agent Genie Application Initializing...");
        System.out.println("Time Zone: " + TimeZone.getDefault().getID());
        System.out.println("File Encoding: " + System.getProperty("file.encoding"));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("========================================");
    }
    
    /**
     * 应用销毁前处理
     */
    @PreDestroy
    public void destroy() {
        System.out.println("========================================");
        System.out.println("Stock Agent Genie Application Shutting Down...");
        System.out.println("========================================");
    }
    
    /**
     * 获取默认配置属性
     * 
     * @return 默认配置属性
     */
    private static java.util.Properties getDefaultProperties() {
        java.util.Properties properties = new java.util.Properties();
        
        // 默认配置
        properties.setProperty("spring.profiles.default", "dev");
        properties.setProperty("spring.main.banner-mode", "console");
        properties.setProperty("spring.main.lazy-initialization", "false");
        properties.setProperty("spring.main.allow-bean-definition-overriding", "true");
        
        // JPA配置
        properties.setProperty("spring.jpa.open-in-view", "false");
        properties.setProperty("spring.jpa.hibernate.use-new-id-generator-mappings", "true");
        
        // Jackson配置
        properties.setProperty("spring.jackson.default-property-inclusion", "NON_NULL");
        properties.setProperty("spring.jackson.serialization.fail-on-empty-beans", "false");
        
        // 线程池配置
        properties.setProperty("spring.task.execution.pool.core-size", "8");
        properties.setProperty("spring.task.execution.pool.max-size", "20");
        properties.setProperty("spring.task.execution.pool.queue-capacity", "100");
        properties.setProperty("spring.task.execution.thread-name-prefix", "stock-agent-task-");
        
        // 调度线程池配置
        properties.setProperty("spring.task.scheduling.pool.size", "5");
        properties.setProperty("spring.task.scheduling.thread-name-prefix", "stock-agent-scheduling-");
        
        return properties;
    }
    
    /**
     * 打印启动信息
     * 
     * @param port 服务端口
     */
    private static void printStartupInfo(String port) {
        String banner = """
                
                ███████╗████████╗ ██████╗  ██████╗██╗  ██╗     █████╗  ██████╗ ███████╗███╗   ██╗████████╗
                ██╔════╝╚══██╔══╝██╔═══██╗██╔════╝██║ ██╔╝    ██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝
                ███████╗   ██║   ██║   ██║██║     █████╔╝     ███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   
                ╚════██║   ██║   ██║   ██║██║     ██╔═██╗     ██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   
                ███████║   ██║   ╚██████╔╝╚██████╗██║  ██╗    ██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   
                ╚══════╝   ╚═╝    ╚═════╝  ╚═════╝╚═╝  ╚═╝    ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   
                
                                        ██████╗ ███████╗███╗   ██╗██╗███████╗
                                       ██╔════╝ ██╔════╝████╗  ██║██║██╔════╝
                                       ██║  ███╗█████╗  ██╔██╗ ██║██║█████╗  
                                       ██║   ██║██╔══╝  ██║╚██╗██║██║██╔══╝  
                                       ╚██████╔╝███████╗██║ ╚████║██║███████╗
                                        ╚═════╝ ╚══════╝╚═╝  ╚═══╝╚═╝╚══════╝
                
                """;
        
        System.out.println(banner);
        System.out.println("========================================");
        System.out.println("🚀 Stock Agent Genie Started Successfully!");
        System.out.println("📊 Intelligent Stock Analysis Platform");
        System.out.println("🤖 Agent Orchestrator & Workflow Engine");
        System.out.println("========================================");
        System.out.println("🌐 Application URL: http://localhost:" + port + "/api");
        System.out.println("📚 API Documentation: http://localhost:" + port + "/api/swagger-ui.html");
        System.out.println("📈 Health Check: http://localhost:" + port + "/api/actuator/health");
        System.out.println("🔌 WebSocket Endpoints:");
        System.out.println("   - Analysis: ws://localhost:" + port + "/api/ws/analysis");
        System.out.println("   - Workflow: ws://localhost:" + port + "/api/ws/workflow");
        System.out.println("========================================");
        System.out.println("✨ Features:");
        System.out.println("   ✅ Agent Orchestration");
        System.out.println("   ✅ Custom Workflow Engine");
        System.out.println("   ✅ MCP Tool Integration");
        System.out.println("   ✅ Real-time WebSocket");
        System.out.println("   ✅ Data Persistence");
        System.out.println("   ✅ Performance Monitoring");
        System.out.println("========================================");
        System.out.println("🎯 Ready to serve intelligent stock analysis!");
        System.out.println("========================================");
    }
}