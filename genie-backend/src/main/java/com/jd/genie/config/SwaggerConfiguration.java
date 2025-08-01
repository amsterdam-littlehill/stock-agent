package com.jd.genie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger API文档配置
 * 为Stock Agent Genie系统提供完整的API文档
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfiguration {

    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * 配置OpenAPI基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * API基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("Stock Agent Genie API")
                .description("""
                        # Stock Agent Genie 智能体协调器 API 文档
                        
                        ## 系统概述
                        Stock Agent Genie 是一个智能股票分析平台，集成了多个AI智能体和自定义工作流引擎。
                        
                        ## 核心功能
                        - 🤖 **智能体协调器**: 统一管理和调度多个AI分析师
                        - 🔄 **工作流引擎**: 支持用户自定义分析流程
                        - 🛠️ **MCP工具集成**: 丰富的数据分析和处理工具
                        - 📡 **实时通信**: WebSocket支持实时数据推送
                        - 📊 **数据持久化**: 完整的执行历史和统计分析
                        
                        ## 智能体类型
                        - **基本面分析师**: 分析公司财务数据和基本面指标
                        - **技术分析师**: 进行技术指标分析和图表分析
                        - **市场分析师**: 分析市场趋势和宏观经济因素
                        - **风险分析师**: 评估投资风险和风险管理
                        - **情感分析师**: 分析市场情绪和新闻舆情
                        - **投资顾问**: 综合各方面分析提供投资建议
                        
                        ## 工作流节点类型
                        - **开始节点**: 工作流入口
                        - **结束节点**: 工作流出口
                        - **智能体节点**: 调用AI智能体进行分析
                        - **工具节点**: 调用MCP工具处理数据
                        - **条件节点**: 根据条件分支执行
                        - **脚本节点**: 执行自定义脚本
                        - **延迟节点**: 延迟等待
                        - **通知节点**: 发送通知消息
                        
                        ## 认证方式
                        系统支持JWT Token认证，请在请求头中添加 `Authorization: Bearer <token>`
                        
                        ## 错误码说明
                        - `200`: 成功
                        - `400`: 请求参数错误
                        - `401`: 未授权
                        - `403`: 禁止访问
                        - `404`: 资源不存在
                        - `500`: 服务器内部错误
                        
                        ## 联系我们
                        如有问题请联系开发团队。
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Stock Agent Team")
                        .email("team@stockagent.com")
                        .url("https://github.com/stock-agent/genie"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 服务器列表
     */
    private List<Server> serverList() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("开发环境"),
                new Server()
                        .url("https://api.stockagent.com")
                        .description("生产环境")
        );
    }

    /**
     * 安全组件配置
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT认证Token"));
    }

    /**
     * 安全要求
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    /**
     * 智能体管理API分组
     */
    @Bean
    public GroupedOpenApi agentApi() {
        return GroupedOpenApi.builder()
                .group("agent")
                .displayName("智能体管理")
                .pathsToMatch("/agent/**")
                .build();
    }

    /**
     * 协调器API分组
     */
    @Bean
    public GroupedOpenApi orchestratorApi() {
        return GroupedOpenApi.builder()
                .group("orchestrator")
                .displayName("协调器管理")
                .pathsToMatch("/orchestrator/**")
                .build();
    }

    /**
     * 工作流API分组
     */
    @Bean
    public GroupedOpenApi workflowApi() {
        return GroupedOpenApi.builder()
                .group("workflow")
                .displayName("工作流管理")
                .pathsToMatch("/workflow/**")
                .build();
    }

    /**
     * MCP工具API分组
     */
    @Bean
    public GroupedOpenApi toolApi() {
        return GroupedOpenApi.builder()
                .group("tool")
                .displayName("MCP工具管理")
                .pathsToMatch("/tool/**")
                .build();
    }

    /**
     * 系统管理API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统管理")
                .pathsToMatch("/system/**", "/actuator/**")
                .build();
    }

    /**
     * 用户管理API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("用户管理")
                .pathsToMatch("/user/**", "/auth/**")
                .build();
    }

    /**
     * 数据分析API分组
     */
    @Bean
    public GroupedOpenApi analysisApi() {
        return GroupedOpenApi.builder()
                .group("analysis")
                .displayName("数据分析")
                .pathsToMatch("/analysis/**", "/stock/**", "/market/**")
                .build();
    }

    /**
     * 监控统计API分组
     */
    @Bean
    public GroupedOpenApi monitoringApi() {
        return GroupedOpenApi.builder()
                .group("monitoring")
                .displayName("监控统计")
                .pathsToMatch("/monitoring/**", "/stats/**", "/health/**")
                .build();
    }
}