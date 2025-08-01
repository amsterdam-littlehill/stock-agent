# Stock-Agent 详细设计说明书

> 基于TradingAgents协作模型 + JoyAgent-JDGenie框架的技术实现设计

## 1. 系统架构设计

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户交互层                                │
├─────────────────────────────────────────────────────────────────┤
│  Web界面     │  移动端     │  API接口    │  工作流设计器        │
│  (React)     │  (H5)       │  (REST)     │  (React Flow)        │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                        API网关层                                 │
├─────────────────────────────────────────────────────────────────┤
│  Spring Gateway │ 认证授权 │ 限流熔断 │ 路由转发 │ 监控日志    │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    JoyAgent-JDGenie核心引擎层                    │
├─────────────────────────────────────────────────────────────────┤
│  任务理解    │  智能体调度  │  协作编排   │  输出生成            │
│  (NLU)       │  (Scheduler) │  (DAG)      │  (Generator)         │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    TradingAgents协作层                           │
├─────────────────────────────────────────────────────────────────┤
│  新闻分析师  │  研究分析师  │  基本面分析师 │  技术分析师        │
│  情绪分析师  │  风险管理师  │  交易执行师   │  结构化辩论引擎    │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      用户自定义扩展层                            │
├─────────────────────────────────────────────────────────────────┤
│  自定义节点  │  工作流引擎  │  MCP工具集成 │  第三方模型        │
│  模板市场    │  插件系统    │  安全沙箱    │  用户脚本          │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                        数据服务层                                │
├─────────────────────────────────────────────────────────────────┤
│  股票数据    │  新闻数据    │  宏观数据   │  用户数据          │
│  实时行情    │  历史数据    │  技术指标   │  分析结果          │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                          存储层                                  │
├─────────────────────────────────────────────────────────────────┤
│  MySQL       │  Redis       │  InfluxDB   │  MongoDB           │
│  (业务数据)  │  (缓存)      │  (时序)     │  (工作流/模板)     │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                        外部数据源                                │
├─────────────────────────────────────────────────────────────────┤
│  新浪财经    │  腾讯财经    │  东方财富   │  Tushare Pro       │
│  财联社      │  雪球        │  各大券商   │  宏观数据API       │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 核心组件设计

#### 1.2.1 JoyAgent-JDGenie核心引擎

```java
// 任务理解组件
@Component
public class TaskUnderstandingService {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private EntityExtractionService entityService;
    
    /**
     * 自然语言任务理解
     */
    public TaskContext understandTask(String userQuery) {
        // 1. 意图识别
        Intent intent = identifyIntent(userQuery);
        
        // 2. 实体提取
        List<Entity> entities = entityService.extractEntities(userQuery);
        
        // 3. 任务分解
        List<SubTask> subTasks = decomposeTask(intent, entities);
        
        // 4. 构建任务上下文
        return TaskContext.builder()
            .intent(intent)
            .entities(entities)
            .subTasks(subTasks)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    private Intent identifyIntent(String query) {
        String prompt = """
            分析用户查询的投资分析意图，返回JSON格式：
            {
                "type": "STOCK_ANALYSIS|PORTFOLIO_ANALYSIS|MARKET_ANALYSIS",
                "scope": "FUNDAMENTAL|TECHNICAL|SENTIMENT|COMPREHENSIVE",
                "timeframe": "REALTIME|SHORT_TERM|MEDIUM_TERM|LONG_TERM",
                "confidence": 0.95
            }
            
            用户查询：{}
            """.formatted(query);
            
        return llmService.chat(prompt, Intent.class);
    }
}

// 智能体调度器
@Component
public class AgentScheduler {
    
    @Autowired
    private Map<String, TradingAgent> agents;
    
    @Autowired
    private DAGExecutor dagExecutor;
    
    /**
     * 基于任务上下文调度智能体
     */
    public CompletableFuture<AnalysisResult> scheduleAgents(TaskContext context) {
        // 1. 构建执行DAG
        DAG executionDAG = buildExecutionDAG(context);
        
        // 2. 执行DAG
        return dagExecutor.execute(executionDAG)
            .thenCompose(this::structuredDebate)
            .thenApply(this::generateFinalResult);
    }
    
    private DAG buildExecutionDAG(TaskContext context) {
        DAGBuilder builder = DAG.builder();
        
        // 阶段1：信息收集（并行）
        builder.addParallelStage("info_collection", 
            Arrays.asList("news_analyst", "research_analyst"));
        
        // 阶段2：专业分析（并行，依赖阶段1）
        builder.addParallelStage("professional_analysis", 
            Arrays.asList("fundamental_analyst", "technical_analyst", "sentiment_analyst"))
            .dependsOn("info_collection");
        
        // 阶段3：风险评估（依赖阶段2）
        builder.addStage("risk_assessment", "risk_manager")
            .dependsOn("professional_analysis");
        
        // 阶段4：结构化辩论（依赖阶段3）
        builder.addStage("structured_debate", "debate_engine")
            .dependsOn("risk_assessment");
        
        // 阶段5：决策融合（依赖阶段4）
        builder.addStage("decision_fusion", "trading_executor")
            .dependsOn("structured_debate");
        
        return builder.build();
    }
}
```

#### 1.2.2 TradingAgents协作层

```java
// 智能体基类
public abstract class TradingAgent {
    
    protected String agentId;
    protected String agentName;
    protected LLMService llmService;
    protected DataService dataService;
    
    /**
     * 执行分析任务
     */
    public abstract CompletableFuture<AgentResult> analyze(TaskContext context, Map<String, Object> inputs);
    
    /**
     * 参与结构化辩论
     */
    public abstract DebateArgument debate(DebateContext context, List<DebateArgument> previousArguments);
    
    /**
     * 更新观点置信度
     */
    public abstract void updateConfidence(DebateResult debateResult);
}

// 基本面分析师
@Component("fundamental_analyst")
public class FundamentalAnalyst extends TradingAgent {
    
    @Override
    public CompletableFuture<AgentResult> analyze(TaskContext context, Map<String, Object> inputs) {
        return CompletableFuture.supplyAsync(() -> {
            String stockCode = context.getStockCode();
            
            // 1. 获取财务数据
            FinancialData financialData = dataService.getFinancialData(stockCode);
            
            // 2. 计算财务指标
            FinancialRatios ratios = calculateFinancialRatios(financialData);
            
            // 3. 行业对比分析
            IndustryComparison comparison = performIndustryComparison(stockCode, ratios);
            
            // 4. 估值分析
            ValuationAnalysis valuation = performValuationAnalysis(financialData, ratios);
            
            // 5. LLM分析
            String analysisPrompt = buildAnalysisPrompt(financialData, ratios, comparison, valuation);
            String llmAnalysis = llmService.chat(analysisPrompt);
            
            return AgentResult.builder()
                .agentId("fundamental_analyst")
                .analysis(llmAnalysis)
                .confidence(calculateConfidence(ratios, comparison))
                .recommendation(generateRecommendation(valuation))
                .supportingData(Map.of(
                    "ratios", ratios,
                    "comparison", comparison,
                    "valuation", valuation
                ))
                .build();
        });
    }
    
    @Override
    public DebateArgument debate(DebateContext context, List<DebateArgument> previousArguments) {
        // 基于基本面数据构建辩论论据
        String prompt = """
            作为基本面分析师，基于以下财务数据和其他分析师的观点，提出你的辩论论据：
            
            财务数据：{}
            其他观点：{}
            
            请提供：
            1. 核心论据（基于财务数据）
            2. 支撑证据（具体财务指标）
            3. 对其他观点的回应
            4. 风险提示
            """.formatted(context.getFinancialData(), previousArguments);
            
        String argument = llmService.chat(prompt);
        
        return DebateArgument.builder()
            .agentId(agentId)
            .round(context.getCurrentRound())
            .argument(argument)
            .confidence(getCurrentConfidence())
            .evidenceType("FUNDAMENTAL")
            .build();
    }
}

// 结构化辩论引擎
@Component
public class StructuredDebateEngine {
    
    @Autowired
    private List<TradingAgent> agents;
    
    /**
     * 执行结构化辩论
     */
    public DebateResult conductDebate(Map<String, AgentResult> agentResults) {
        DebateContext context = initializeDebateContext(agentResults);
        
        // 3轮辩论
        for (int round = 1; round <= 3; round++) {
            context.setCurrentRound(round);
            
            switch (round) {
                case 1 -> conductRound1(context); // 观点阐述
                case 2 -> conductRound2(context); // 观点质疑
                case 3 -> conductRound3(context); // 共识构建
            }
        }
        
        return buildDebateResult(context);
    }
    
    private void conductRound1(DebateContext context) {
        // 第1轮：各智能体阐述核心观点
        List<DebateArgument> arguments = new ArrayList<>();
        
        for (TradingAgent agent : agents) {
            DebateArgument argument = agent.debate(context, arguments);
            arguments.add(argument);
            context.addArgument(argument);
        }
    }
    
    private void conductRound2(DebateContext context) {
        // 第2轮：针对对方观点进行质疑和反驳
        List<DebateArgument> round1Arguments = context.getArgumentsByRound(1);
        
        for (TradingAgent agent : agents) {
            // 过滤掉自己的观点
            List<DebateArgument> othersArguments = round1Arguments.stream()
                .filter(arg -> !arg.getAgentId().equals(agent.getAgentId()))
                .collect(Collectors.toList());
                
            DebateArgument rebuttal = agent.debate(context, othersArguments);
            context.addArgument(rebuttal);
        }
    }
    
    private void conductRound3(DebateContext context) {
        // 第3轮：寻求共识和妥协
        List<DebateArgument> allArguments = context.getAllArguments();
        
        for (TradingAgent agent : agents) {
            DebateArgument consensus = agent.debate(context, allArguments);
            context.addArgument(consensus);
        }
    }
}
```

### 1.3 数据架构设计

#### 1.3.1 数据库设计

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'PREMIUM', 'ENTERPRISE', 'ADMIN') DEFAULT 'USER',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- 股票基础信息表
CREATE TABLE stocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    market ENUM('A_STOCK', 'HK_STOCK', 'US_STOCK') NOT NULL,
    industry VARCHAR(100),
    sector VARCHAR(100),
    listing_date DATE,
    status ENUM('ACTIVE', 'SUSPENDED', 'DELISTED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_market (market),
    INDEX idx_industry (industry)
);

-- 分析任务表
CREATE TABLE analysis_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_id VARCHAR(64) UNIQUE NOT NULL,
    query_text TEXT NOT NULL,
    stock_codes JSON,
    task_type ENUM('STOCK_ANALYSIS', 'PORTFOLIO_ANALYSIS', 'MARKET_ANALYSIS') NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status)
);

-- 智能体执行记录表
CREATE TABLE agent_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    agent_id VARCHAR(50) NOT NULL,
    agent_name VARCHAR(100) NOT NULL,
    stage VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    input_data JSON,
    output_data JSON,
    confidence DECIMAL(3,2),
    execution_time_ms INT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status)
);

-- 辩论记录表
CREATE TABLE debate_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    round_number INT NOT NULL,
    agent_id VARCHAR(50) NOT NULL,
    argument_text TEXT NOT NULL,
    confidence DECIMAL(3,2),
    evidence_type ENUM('FUNDAMENTAL', 'TECHNICAL', 'SENTIMENT', 'NEWS', 'RISK') NOT NULL,
    supporting_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_round (round_number),
    INDEX idx_agent_id (agent_id)
);

-- 分析结果表
CREATE TABLE analysis_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) UNIQUE NOT NULL,
    final_recommendation ENUM('STRONG_BUY', 'BUY', 'HOLD', 'SELL', 'STRONG_SELL') NOT NULL,
    confidence_score DECIMAL(3,2) NOT NULL,
    consensus_level DECIMAL(3,2) NOT NULL,
    executive_summary TEXT NOT NULL,
    detailed_analysis JSON NOT NULL,
    risk_assessment JSON NOT NULL,
    agent_contributions JSON NOT NULL,
    debate_summary JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id)
);

-- 用户工作流表
CREATE TABLE user_workflows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    workflow_id VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    workflow_definition JSON NOT NULL,
    is_template BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    version VARCHAR(20) DEFAULT '1.0.0',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_is_template (is_template),
    INDEX idx_is_public (is_public)
);

-- MCP工具配置表
CREATE TABLE mcp_tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    tool_type ENUM('DATA_SOURCE', 'ANALYSIS', 'NOTIFICATION', 'CUSTOM') NOT NULL,
    config_schema JSON NOT NULL,
    default_config JSON,
    is_system BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tool_id (tool_id),
    INDEX idx_tool_type (tool_type),
    INDEX idx_is_system (is_system)
);
```

#### 1.3.2 时序数据设计（InfluxDB）

```sql
-- 股票行情数据
-- measurement: stock_quotes
-- tags: code, market
-- fields: open, high, low, close, volume, amount, change_pct
-- time: timestamp

-- 技术指标数据
-- measurement: technical_indicators
-- tags: code, indicator_type
-- fields: value, signal
-- time: timestamp

-- 系统性能指标
-- measurement: system_metrics
-- tags: service_name, instance_id
-- fields: cpu_usage, memory_usage, response_time, error_rate
-- time: timestamp

-- 智能体执行指标
-- measurement: agent_metrics
-- tags: agent_id, task_type
-- fields: execution_time, confidence, success_rate
-- time: timestamp
```

#### 1.3.3 缓存设计（Redis）

```yaml
# 缓存键设计规范
cache_keys:
  # 股票基础信息缓存
  stock_info: "stock:info:{code}"
  ttl: 3600  # 1小时
  
  # 实时行情缓存
  stock_quote: "stock:quote:{code}"
  ttl: 10  # 10秒
  
  # 技术指标缓存
  technical_indicator: "stock:indicator:{code}:{type}:{period}"
  ttl: 300  # 5分钟
  
  # 分析结果缓存
  analysis_result: "analysis:result:{task_id}"
  ttl: 1800  # 30分钟
  
  # 用户会话缓存
  user_session: "session:{session_id}"
  ttl: 7200  # 2小时
  
  # LLM响应缓存
  llm_response: "llm:response:{hash}"
  ttl: 86400  # 24小时
```

### 1.4 API设计

#### 1.4.1 RESTful API设计

```yaml
# API版本控制
api_version: v1
base_url: /api/v1

# 认证方式
authentication:
  type: Bearer Token
  header: Authorization
  format: "Bearer {token}"

# 通用响应格式
response_format:
  success:
    code: 200
    message: "Success"
    data: {}
    timestamp: "2024-12-01T10:00:00Z"
  error:
    code: 400/401/403/404/500
    message: "Error description"
    error: "ERROR_CODE"
    timestamp: "2024-12-01T10:00:00Z"

# API端点设计
endpoints:
  # 用户认证
  - path: /auth/login
    method: POST
    description: 用户登录
    request:
      username: string
      password: string
    response:
      token: string
      expires_in: number
      user_info: object
  
  - path: /auth/refresh
    method: POST
    description: 刷新令牌
    request:
      refresh_token: string
    response:
      token: string
      expires_in: number
  
  # 股票数据
  - path: /stocks/search
    method: GET
    description: 搜索股票
    parameters:
      q: string  # 搜索关键词
      market: string  # 市场类型
      limit: number  # 返回数量
    response:
      stocks: array
  
  - path: /stocks/{code}/quote
    method: GET
    description: 获取实时行情
    parameters:
      code: string  # 股票代码
    response:
      quote: object
  
  - path: /stocks/{code}/kline
    method: GET
    description: 获取K线数据
    parameters:
      code: string
      period: string  # 周期
      start_date: string
      end_date: string
    response:
      klines: array
  
  # 智能分析
  - path: /analysis/submit
    method: POST
    description: 提交分析任务
    request:
      query: string  # 自然语言查询
      stocks: array  # 股票代码列表
      options: object  # 分析选项
    response:
      task_id: string
      estimated_time: number
  
  - path: /analysis/{task_id}/status
    method: GET
    description: 查询分析状态
    parameters:
      task_id: string
    response:
      status: string
      progress: number
      agents: array
  
  - path: /analysis/{task_id}/result
    method: GET
    description: 获取分析结果
    parameters:
      task_id: string
    response:
      result: object
      confidence: number
      agents_output: array
      debate_summary: object
  
  # 工作流管理
  - path: /workflows
    method: GET
    description: 获取工作流列表
    parameters:
      page: number
      size: number
      type: string  # template/custom
    response:
      workflows: array
      total: number
  
  - path: /workflows
    method: POST
    description: 创建工作流
    request:
      name: string
      description: string
      definition: object
    response:
      workflow_id: string
  
  - path: /workflows/{workflow_id}
    method: PUT
    description: 更新工作流
    request:
      name: string
      description: string
      definition: object
    response:
      success: boolean
  
  - path: /workflows/{workflow_id}/execute
    method: POST
    description: 执行工作流
    request:
      inputs: object
    response:
      execution_id: string
```

#### 1.4.2 WebSocket API设计

```javascript
// WebSocket连接
const ws = new WebSocket('ws://localhost:8080/ws');

// 消息格式
const messageFormat = {
  type: 'MESSAGE_TYPE',
  data: {},
  timestamp: '2024-12-01T10:00:00Z',
  requestId: 'uuid'
};

// 消息类型
const messageTypes = {
  // 订阅实时数据
  SUBSCRIBE_QUOTES: {
    type: 'SUBSCRIBE_QUOTES',
    data: {
      stocks: ['000001', '000002']
    }
  },
  
  // 实时行情推送
  QUOTE_UPDATE: {
    type: 'QUOTE_UPDATE',
    data: {
      code: '000001',
      price: 10.50,
      change: 0.05,
      change_pct: 0.48,
      volume: 1000000,
      timestamp: '2024-12-01T10:00:00Z'
    }
  },
  
  // 分析进度推送
  ANALYSIS_PROGRESS: {
    type: 'ANALYSIS_PROGRESS',
    data: {
      task_id: 'task_123',
      status: 'RUNNING',
      progress: 60,
      current_stage: 'professional_analysis',
      agents: [
        {
          agent_id: 'fundamental_analyst',
          status: 'COMPLETED',
          confidence: 0.85
        },
        {
          agent_id: 'technical_analyst',
          status: 'RUNNING',
          progress: 70
        }
      ]
    }
  },
  
  // 辩论过程推送
  DEBATE_UPDATE: {
    type: 'DEBATE_UPDATE',
    data: {
      task_id: 'task_123',
      round: 2,
      agent_id: 'fundamental_analyst',
      argument: '基于财务数据分析...',
      confidence: 0.88
    }
  }
};
```

### 1.5 安全设计

#### 1.5.1 认证授权设计

```java
// JWT Token设计
@Component
public class JwtTokenProvider {
    
    private final String secretKey;
    private final long tokenValidityInMilliseconds;
    
    public String createToken(UserDetails userDetails) {
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities());
        claims.put("userId", ((CustomUserDetails) userDetails).getUserId());
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

// 权限控制
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/stocks/**").hasRole("USER")
                .requestMatchers("/api/v1/analysis/**").hasRole("USER")
                .requestMatchers("/api/v1/workflows/**").hasRole("PREMIUM")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}

// API限流
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientId(httpRequest);
        String endpoint = httpRequest.getRequestURI();
        
        // 基于用户角色的限流策略
        RateLimitConfig config = getRateLimitConfig(clientId, endpoint);
        
        if (isRateLimited(clientId, endpoint, config)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

#### 1.5.2 数据安全设计

```java
// 数据加密
@Component
public class DataEncryptionService {
    
    private final AESUtil aesUtil;
    
    /**
     * 敏感数据加密存储
     */
    @EventListener
    public void handleUserDataSave(UserDataSaveEvent event) {
        UserData userData = event.getUserData();
        
        // 加密敏感字段
        if (userData.getPhone() != null) {
            userData.setPhone(aesUtil.encrypt(userData.getPhone()));
        }
        if (userData.getEmail() != null) {
            userData.setEmail(aesUtil.encrypt(userData.getEmail()));
        }
    }
    
    /**
     * 数据脱敏展示
     */
    public UserDataVO maskSensitiveData(UserData userData) {
        return UserDataVO.builder()
            .id(userData.getId())
            .username(userData.getUsername())
            .phone(maskPhone(userData.getPhone()))
            .email(maskEmail(userData.getEmail()))
            .build();
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

// 审计日志
@Aspect
@Component
public class AuditLogAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Around("@annotation(Auditable)")
    public Object logAuditableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 记录操作开始
        AuditLog auditLog = AuditLog.builder()
            .userId(getCurrentUserId())
            .operation(methodName)
            .parameters(JsonUtils.toJson(args))
            .timestamp(System.currentTimeMillis())
            .build();
        
        try {
            Object result = joinPoint.proceed();
            
            // 记录操作成功
            auditLog.setStatus("SUCCESS");
            auditLog.setResult(JsonUtils.toJson(result));
            
            return result;
        } catch (Exception e) {
            // 记录操作失败
            auditLog.setStatus("FAILED");
            auditLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            auditLogService.saveAuditLog(auditLog);
        }
    }
}
```

### 1.6 性能优化设计

#### 1.6.1 缓存策略

```java
// 多级缓存设计
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats());
        return cacheManager;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}

// 缓存服务
@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Cacheable(value = "stockQuotes", key = "#code")
    public StockQuote getStockQuote(String code) {
        // 从数据源获取数据
        return dataSourceService.getStockQuote(code);
    }
    
    @CacheEvict(value = "stockQuotes", key = "#code")
    public void evictStockQuote(String code) {
        // 缓存失效
    }
    
    // LLM响应缓存
    public String getCachedLLMResponse(String prompt) {
        String cacheKey = "llm:" + DigestUtils.md5Hex(prompt);
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }
    
    public void cacheLLMResponse(String prompt, String response) {
        String cacheKey = "llm:" + DigestUtils.md5Hex(prompt);
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(24));
    }
}
```

#### 1.6.2 异步处理

```java
// 异步配置
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Analysis-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "dataFetchExecutor")
    public Executor dataFetchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("DataFetch-");
        executor.initialize();
        return executor;
    }
}

// 异步分析服务
@Service
public class AsyncAnalysisService {
    
    @Async("analysisExecutor")
    public CompletableFuture<AgentResult> executeAgentAnalysis(String agentId, TaskContext context) {
        TradingAgent agent = agentRegistry.getAgent(agentId);
        return agent.analyze(context, Collections.emptyMap());
    }
    
    @Async("dataFetchExecutor")
    public CompletableFuture<List<NewsItem>> fetchNewsData(String stockCode) {
        return CompletableFuture.supplyAsync(() -> {
            return newsDataService.getLatestNews(stockCode);
        });
    }
}
```

### 1.7 监控告警设计

#### 1.7.1 监控指标

```java
// 自定义监控指标
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter analysisRequestCounter;
    private final Timer analysisExecutionTimer;
    private final Gauge activeAgentsGauge;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.analysisRequestCounter = Counter.builder("analysis.requests.total")
            .description("Total number of analysis requests")
            .register(meterRegistry);
        this.analysisExecutionTimer = Timer.builder("analysis.execution.time")
            .description("Analysis execution time")
            .register(meterRegistry);
        this.activeAgentsGauge = Gauge.builder("agents.active.count")
            .description("Number of active agents")
            .register(meterRegistry, this, CustomMetrics::getActiveAgentsCount);
    }
    
    public void incrementAnalysisRequest(String type) {
        analysisRequestCounter.increment(Tags.of("type", type));
    }
    
    public Timer.Sample startAnalysisTimer() {
        return Timer.start(meterRegistry);
    }
    
    private double getActiveAgentsCount() {
        return agentManager.getActiveAgentsCount();
    }
}

// 健康检查
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSourceService dataSourceService;
    
    @Autowired
    private LLMService llmService;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        // 检查数据源连接
        if (dataSourceService.isHealthy()) {
            builder.up().withDetail("dataSource", "Available");
        } else {
            builder.down().withDetail("dataSource", "Unavailable");
        }
        
        // 检查LLM服务
        if (llmService.isHealthy()) {
            builder.withDetail("llmService", "Available");
        } else {
            builder.down().withDetail("llmService", "Unavailable");
        }
        
        return builder.build();
    }
}
```

#### 1.7.2 告警配置

```yaml
# Prometheus告警规则
groups:
  - name: stock-agent-alerts
    rules:
      # 系统性能告警
      - alert: HighCPUUsage
        expr: cpu_usage_percent > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage detected"
          description: "CPU usage is above 80% for more than 5 minutes"
      
      - alert: HighMemoryUsage
        expr: memory_usage_percent > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Memory usage is above 85% for more than 5 minutes"
      
      # 业务指标告警
      - alert: HighAnalysisFailureRate
        expr: rate(analysis_requests_failed_total[5m]) / rate(analysis_requests_total[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High analysis failure rate"
          description: "Analysis failure rate is above 10% for more than 2 minutes"
      
      - alert: SlowAnalysisResponse
        expr: histogram_quantile(0.95, rate(analysis_execution_time_bucket[5m])) > 120
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "Slow analysis response time"
          description: "95th percentile analysis time is above 120 seconds"
      
      # 数据源告警
      - alert: DataSourceUnavailable
        expr: up{job="data-source"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Data source is unavailable"
          description: "Data source has been down for more than 1 minute"
```

## 2. 部署架构设计

### 2.1 容器化部署

```dockerfile
# 后端服务Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY target/stock-agent-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

# 前端服务Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 2.2 Kubernetes部署

```yaml
# 后端服务部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: stock-agent-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: stock-agent-backend
  template:
    metadata:
      labels:
        app: stock-agent-backend
    spec:
      containers:
      - name: backend
        image: stock-agent/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: host
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: stock-agent-backend-service
spec:
  selector:
    app: stock-agent-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP

---
# HPA自动扩缩容
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: stock-agent-backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: stock-agent-backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 2.3 CI/CD流水线

```yaml
# GitHub Actions工作流
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
  
  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build application
      run: mvn clean package -DskipTests
    
    - name: Build Docker image
      run: |
        docker build -t stock-agent/backend:${{ github.sha }} .
        docker tag stock-agent/backend:${{ github.sha }} stock-agent/backend:latest
    
    - name: Push to registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push stock-agent/backend:${{ github.sha }}
        docker push stock-agent/backend:latest
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/stock-agent-backend backend=stock-agent/backend:${{ github.sha }}
        kubectl rollout status deployment/stock-agent-backend
```

---

**文档版本**: v1.0  
**创建日期**: 2024年12月  
**最后更新**: 2024年12月  
**文档状态**: 草案  
**审核状态**: 待审核