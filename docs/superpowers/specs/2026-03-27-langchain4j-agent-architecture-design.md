# LangChain4j Agent 架构设计

> 文档版本: 1.3
> 创建日期: 2026-03-27
> 状态: 已更新 (集成 langchain4j-agentic-a2a + langchain4j-skills)

---

## 关键设计决策摘要

| 决策 | 选择 |
|------|------|
| A2UIAgent 类型 | **独立 LLM Agent**，有自己的 LLM 推理、System Prompt 和记忆 |
| A2A 通信方式 | **Tool Call**，GeneralAgent 通过 `requestA2UI()` 触发 A2UIAgent |
| Structured Output | **LangChain4j 原生特性**，使用 `@UserMessage` + DTO 返回类型 |
| A2UIAgent Skills | **LLM 生成规则**（通过 System Prompt），不是 Tool |
| 架构风格 | **Spring 主导**，LangChain4j 负责 LLM 调用，Spring 负责协调 |

## 1. 概述

### 1.1 目标

基于 Spring 主导架构，重构 dev-backend 的 LangChain4j 集成，实现：

- **Structured Outputs** - 使用 LangChain4j 原生特性规范 LLM 输出
- **Skills** - 技能分层，使用 `langchain4j-skills` 模块
- **Classification** - 意图识别，区分 UI/Markdown/文本输出
- **Guardrails** - 前后拦截，输入输出校验
- **A2A** - Agent 间通信，使用 `langchain4j-agentic-a2a` 模块

### 1.2 关键设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| A2UIAgent | 独立 LLM Agent | 有自己的 LLM 推理、System Prompt 和记忆 |
| A2A 通信 | **langchain4j-agentic-a2a 原生模块** | 使用 `AgenticServices.a2aBuilder()` 创建 A2A Agent |
| Skills 系统 | **langchain4j-skills 模块** | YAML 定义 + ClassPath/FileSystem 加载 |
| Structured Output | LangChain4j 原生特性 | 使用 `@UserMessage` + DTO 返回类型 |
| 架构风格 | Spring 主导 | LangChain4j 负责 LLM 调用，Spring 负责 Agent 协调 |

### 1.3 架构方案

采用 **Spring 主导架构**：
- LangChain4j `AiServices` 定义 Agent 接口
- Spring `@Service` / `@Component` 实现 Agent
- Agent 间通过 Tool 调用实现 A2A 通信
- `AgentCoordinator` 负责总体协调

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend                              │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP SSE / WebSocket
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   AgentCoordinator                            │
│  ┌─────────────────────────────────────────────────────┐ │
│  │  负责：消息路由、Agent 协调、会话管理                  │ │
│  └─────────────────────────────────────────────────────┘ │
└──────────┬──────────────────────────────────┬─────────────┘
           │                                  │
           │ LLM 推理                         │ A2A (Tool Call)
           ▼                                  ▼
┌──────────────────────┐          ┌──────────────────────┐
│   GeneralAgent      │          │     A2UIAgent        │
│  ┌──────────────┐  │          │  ┌──────────────┐  │
│  │ AiServices    │  │          │  │ AiServices    │  │
│  │ (独立 LLM)    │  │          │  │ (独立 LLM)    │  │
│  └──────────────┘  │          │  └──────────────┘  │
│                    │          │                     │
│  Tools:           │          │  Skills:            │
│  ├── DataQueryTool │          │  ├── TextSkill     │
│  ├── AnalysisTool  │          │  ├── ChartSkill    │
│  └── A2UICallTool │          │  ├── FormSkill     │
│      (A2A 调用)    │          │  ├── CardSkill     │
└────────────────────┘          │  └── LayoutSkill    │
                               └──────────────────────┘
                                         │
                                         ▼
                               ┌──────────────────────┐
                               │   Structured Output   │
                               │   (LangChain4j 原生) │
                               └──────────────────────┘
```

### 2.1 A2A 通信说明

**A2UIAgent 是独立的 LLM Agent**，通过 `langchain4j-agentic-a2a` 原生模块实现 Agent-to-Agent 通信。

**核心组件：**

```java
// 1. A2UIAgent 使用 @Agent 注解定义
@Agent
public interface A2UIAgent {
    // Agent 能力定义
}

// 2. GeneralAgent 通过 AgenticServices.a2aBuilder() 调用
@Service
public class GeneralAgentTools {

    private final A2UIAgent a2uiAgent;

    // 使用 a2aBuilder 创建 A2A 客户端
    public GeneralAgentTools() {
        this.a2uiAgent = AgenticServices.a2aBuilder(A2A_SERVER_URL)
            .build(A2UIAgent.class);
    }

    // 通过 Tool 调用 A2UIAgent
    @Tool("请求 A2UI 组件")
    public A2UIComponent requestA2UI(
        @P("显示类型") String displayType,
        @P("数据") String data
    ) {
        // A2A 调用
        return a2uiAgent.generate(request);
    }
}
```

**A2A 通信流程：**

```
┌──────────────────────────────────────────────────────────────┐
│                     A2A Communication Flow                   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  GeneralAgent                  A2UIAgent                      │
│       │                             │                        │
│       │  1. User Request             │                        │
│       │  ──────────────────────────►│                        │
│       │                             │                        │
│       │  2. LLM 推理                 │                        │
│       │  ──────────────────────────►│                        │
│       │                             │                        │
│       │  3. 调用 requestA2UI Tool    │                        │
│       │  ──────────────────────────►│ 4. A2A 协议消息        │
│       │                             │───────────────────────►│
│       │                             │                        │
│       │                             │ 5. A2UIAgent 独立推理  │
│       │                             │  ─────────────────────►│
│       │                             │                        │
│       │  6. A2A Response            │                        │
│       │  ◄──────────────────────────│◄───────────────────────│
│       │                             │                        │
│       ▼                             ▼                        │
└──────────────────────────────────────────────────────────────┘
```

**关键特性：**
- 使用 `AgenticServices.a2aBuilder()` 创建 A2A 客户端
- 支持 Skill-scoped tools（Agent 可用的工具按 Skill 分组）
- 消息通过 A2A 协议传输，支持流式响应

---

## 3. Agent 设计

### 3.1 GeneralAgent（通用 Agent）

**职责：**
- 对话管理 + 意图识别
- 调用 DataQueryTool 查询数据
- 调用 A2UICallTool 触发 A2UIAgent
- 判断是否需要用户确认

**特性：**
- 独立的 LLM 推理
- 使用 `MessageWindowChatMemory` 管理对话历史
- 通过 Tool 调用实现 A2A

**接口定义：**

```java
public interface GeneralAgent {
    /**
     * 主对话入口
     * @param userMessage 用户消息
     * @param sessionId 会话 ID
     * @return 流式响应
     */
    Flux<String> chat(String userMessage, Long sessionId);
}
```

### 3.2 A2UIAgent（A2UI Agent）— 独立 LLM Agent

**职责：**
- 接收 A2UIRequest 请求
- 独立的 LLM 推理生成组件
- 负责组件排版布局
- 输出标准 A2UI JSON

**特性：**
- **独立的 LLM 推理** - 有自己的 System Prompt 和模型配置
- **Structured Output** - 使用 LangChain4j 原生特性
- **独立记忆** - 有自己的 ChatMemory（可共享或不共享）

**接口定义：**

```java
public interface A2UIAgent {
    /**
     * A2A 入口 - 同步生成
     * @param request A2UI 请求（包含 intent、data、displayType 等）
     * @return A2UI JSON
     */
    String generate(A2UIRequest request);

    /**
     * A2A 入口 - 流式生成
     * @param request A2UI 请求
     * @return 流式 A2UI JSON
     */
    Flux<String> generateStream(A2UIRequest request);
}
```

**A2UIRequest DTO（Structured Output 参数）：**

```java
@Data
@Builder
public class A2UIRequest {
    /** 意图描述 */
    private String intent;

    /** 要展示的数据 */
    private Object data;

    /** 显示类型: line, bar, pie, form, card */
    private String displayType;

    /** 图表标题 */
    private String title;

    /** 额外选项 */
    private Map<String, Object> options;

    /** 消息上下文 */
    private List<ChatMessage> chatHistory;
}
```

---

## 4. Skill 系统设计

### 4.1 LangChain4j Skills 概述

LangChain4j Skills 是一个为 LLM 提供可复用、自包含行为指令的机制。

**核心特性：**
- **FileSystemSkillLoader** - 从文件系统加载 Skill 定义
- **ClassPathSkillLoader** - 从 classpath 加载 Skill 定义
- **Programmatic Skill** - 以编程方式定义 Skill
- **Skill-scoped tools** - Skill 特定的工具集

### 4.2 Skill 定义示例

```yaml
# resources/skills/text-skill.yaml
name: Text Generation
description: 生成文本组件的规则
instructions: |
  当需要生成文本内容时：
  1. 使用 Text 组件类型
  2. 支持 Markdown 格式
  3. 重要信息使用 Card 包裹
  4. 保持内容简洁
```

```yaml
# resources/skills/chart-skill.yaml
name: Chart Generation
description: 生成图表组件的规则
instructions: |
  当需要生成图表时：
  1. 根据数据选择合适的图表类型
     - 趋势数据: line
     - 对比数据: bar
     - 占比数据: pie
  2. 包含标题、图例、坐标轴
  3. 提供交互提示
  4. 图表高度默认 300px
```

### 4.3 Skills 配置

```java
// 加载 Skills
Skills skills = Skills.builder()
    .loadFrom(
        FileSystemSkillLoader.from("src/main/resources/skills")
        // 或 ClassPathSkillLoader.from("skills")
    )
    .build();

// 创建带 Skills 的 Agent
A2UIAgent a2uiAgent = AiServices.builder(A2UIAgent.class)
    .streamingChatModel(streamingModel)
    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(50))
    .skills(skills)  // 注册 Skills
    .build();

// Agent 可使用 activate_skill Tool 动态切换 Skill
```

### 4.4 GeneralAgent Tools

GeneralAgent 使用 Tools 实现数据查询和 A2A 通信：

```java
@Slf4j
@Component
public class GeneralAgentTools {

    @Autowired
    private DataQueryTool dataQueryTool;

    @Autowired
    private A2UIAgent a2uiAgent;

    /**
     * 数据查询 Tool
     */
    @Tool("查询销售数据")
    public String querySalesData(
        @P("开始日期 YYYY-MM-DD") String startDate,
        @P("结束日期 YYYY-MM-DD") String endDate,
        @P("产品类别，可选") String category
    ) {
        log.info("查询销售数据: {} - {} - {}", startDate, endDate, category);
        return dataQueryTool.querySalesData(startDate, endDate, category);
    }

    /**
     * 分析数据维度 Tool
     */
    @Tool("分析数据维度")
    public String analyzeData(
        @P("数据 JSON 格式") String dataJson,
        @P("分析类型: trend, comparison, distribution, correlation") String analysisType
    ) {
        log.info("分析数据维度: type={}", analysisType);
        return "{\"recommendedType\": \"line\", \"reason\": \"趋势分析\"}";
    }

    /**
     * A2A 调用 A2UIAgent Tool
     * 这是 A2A 通信的核心 - GeneralAgent 通过此 Tool 调用 A2UIAgent
     */
    @Tool("请求 A2UI 组件")
    public A2UIComponent requestA2UI(
        @P("显示类型: chart, form, table, card") String displayType,
        @P("要显示的数据 JSON 格式") String data,
        @P("意图描述") String intent,
        @P("图表标题，可选") String title,
        @P("额外选项 JSON 格式，可选") String options
    ) {
        log.info("A2A 请求 A2UIAgent: type={}, intent={}", displayType, intent);

        A2UIRequest request = A2UIRequest.builder()
            .displayType(displayType)
            .data(parseJson(data))
            .intent(intent)
            .title(title)
            .options(parseJson(options))
            .build();

        // 调用 A2UIAgent 的 LLM 推理
        return a2uiAgent.generate(request);
    }
}
```

### 4.5 A2UIAgent Skills（LLM 生成规则）

A2UIAgent 使用 **langchain4j-skills** 模块定义组件生成规则：

**Skill 文件结构：**

```
src/main/resources/skills/
├── text-skill.yaml       # 文本组件生成规则
├── chart-skill.yaml      # 图表组件生成规则
├── form-skill.yaml       # 表单组件生成规则
├── card-skill.yaml       # 卡片组件生成规则
└── layout-skill.yaml     # 布局规则
```

**Skill 加载与注册：**

```java
@Configuration
public class SkillsConfig {

    @Bean
    public Skills a2uiSkills() {
        return Skills.builder()
            .loadFrom(
                ClassPathSkillLoader.from("skills")
            )
            // Skill 特定的工具
            .addTool(ChartGeneratorTool.class)
            .addTool(DataQueryTool.class)
            .build();
    }
}
```

**Agent 使用 Skill：**

```java
@Bean
public A2UIAgent a2uiAgent(
    StreamingChatModel streamingModel,
    Skills a2uiSkills
) {
    return AiServices.builder(A2UIAgent.class)
        .streamingChatModel(streamingModel)
        .chatMemoryProvider(memoryId ->
            MessageWindowChatMemory.withMaxMessages(50))
        .skills(a2uiSkills)
        .build();
}
```

**动态 Skill 切换：**

LLM 可以使用内置的 `activate_skill` Tool 动态切换 Skill：

```
User: 生成一个销售图表
-> Agent 调用 activate_skill("chart-skill")
-> Agent 生成 Chart 组件

User: 生成一个注册表单
-> Agent 调用 activate_skill("form-skill")
-> Agent 生成 Form 组件
```

### 4.3 A2UIAgent Structured Output

A2UIAgent 使用 LangChain4j 的 **Structured Output** 特性，返回强类型的 DTO：

```java
/**
 * A2UIAgent 接口 - 使用 AiServices 实现
 *
 * 通过 @UserMessage 指定输入格式，
 * 返回类型使用 @V 和 DTO 指定输出格式
 */
public interface A2UIAgent {

    /**
     * 生成 A2UI 组件
     * 使用 Structured Output 返回强类型
     */
    @UserMessage("""
        根据以下请求生成 A2UI 组件：

        意图: {intent}
        显示类型: {displayType}
        数据: {data}
        标题: {title}

        请生成符合 A2UI 协议的 JSON 输出。
        """)
    A2UIComponent generate(
        @V("intent") String intent,
        @V("displayType") String displayType,
        @V("data") Map<String, Object> data,
        @V("title") String title
    );

    /**
     * 流式生成 A2UI 组件
     */
    @UserMessage("""
        根据以下请求生成 A2UI 组件（流式）：

        意图: {intent}
        显示类型: {displayType}
        数据: {data}
        标题: {title}
        """)
    Flux<A2UIComponent> generateStream(
        @V("intent") String intent,
        @V("displayType") String displayType,
        @V("data") Map<String, Object> data,
        @V("title") String title
    );
}
```

### 4.4 A2UIComponent DTO（Structured Output 返回类型）

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class A2UIComponent {
    /** 消息类型: surface, component */
    private String type;

    /** Surface ID */
    private String surfaceId;

    /** 组件 ID */
    private String componentId;

    /** 组件规格 */
    private ComponentSpec component;
}
```

**注意：** 此 DTO 与现有的 `ComponentMessage` 结构完全兼容，确保前端解析正常。

---

## 5. Structured Output 设计

### 5.1 技术方案

使用 LangChain4j 的 **Structured Output** 特性：

```java
/**
 * A2UIAgent 使用 AiServices 实现
 * 结合 @UserMessage 和返回类型注解实现结构化输出
 */
@Configuration
public class A2UIAgentConfig {

    @Bean
    @ConditionalOnProperty(prefix = "a2ui.llm", name = "api-key")
    public A2UIAgent a2uiAgent(StreamingChatModel streamingModel) {
        return AiServices.builder(A2UIAgent.class)
            .streamingChatModel(streamingModel)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(50))
            .systemMessage(A2UI_SYSTEM_PROMPT)
            .build();
    }

    String A2UI_SYSTEM_PROMPT = """
        你是一个专业的 A2UI 组件生成专家。

        ## 输出规范
        1. 所有输出必须是有效的 JSON 对象
        2. 组件 ID 必须唯一
        3. surfaceId 默认使用 "main"

        ## 组件类型
        - Text: 文本内容，支持 Markdown
        - Chart: ECharts 图表配置
        - Form: 用户输入表单
        - Card: 卡片容器
        - Button: 操作按钮
        - Divider: 分割线

        ## 布局规则
        - 优先使用 Card 组件包裹内容
        - 表单放在图表之前
        - 相关组件放在一起

        ## 输出格式
        生成符合 A2UI 协议的 JSON 对象。
        """;
}
```

### 5.2 A2UI JSON Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "A2UI Component Schema",
  "type": "object",
  "properties": {
    "type": {
      "type": "string",
      "enum": ["surface", "component", "dataModel", "deleteSurface"]
    },
    "surfaceId": { "type": "string" },
    "componentId": { "type": "string" },
    "name": { "type": "string" },
    "component": {
      "type": "object",
      "properties": {
        "type": {
          "type": "string",
          "enum": ["Text", "Chart", "Form", "Card", "Button", "Divider", "Image", "Table"]
        },
        "props": { "type": "object" }
      },
      "required": ["type", "props"]
    },
    "position": {
      "type": "string",
      "enum": ["append", "prepend", "replace"]
    }
  },
  "required": ["type", "surfaceId"]
}
```

---

## 6. Guardrails 设计

### 6.1 输入校验 (InputGuardrail)

```java
@Slf4j
@Component
public class InputGuardrail {

    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final List<String> BLOCKED_PATTERNS = List.of(
        "<script", "javascript:", "onerror=", "onclick=",
        "sql注入", "';--", "${", "#{"  // 注入检测
    );

    /**
     * 校验消息长度
     */
    public ValidationResult validateLength(String message) {
        if (message == null || message.isEmpty()) {
            return ValidationResult.failure("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.failure(
                String.format("消息长度不能超过 %d 字符", MAX_MESSAGE_LENGTH));
        }
        return ValidationResult.success();
    }

    /**
     * 检测危险内容
     */
    public ValidationResult containsHarmfulContent(String message) {
        for (String pattern : BLOCKED_PATTERNS) {
            if (message.toLowerCase().contains(pattern.toLowerCase())) {
                log.warn("检测到危险内容: {}", pattern);
                return ValidationResult.failure("消息包含不允许的内容");
            }
        }
        return ValidationResult.success();
    }

    /**
     * 清理特殊字符
     */
    public String sanitize(String message) {
        if (message == null) return null;
        return message
            .replaceAll("<script[^>]*>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+=", "")
            .trim();
    }

    /**
     * 完整校验流程
     */
    public ValidationResult validate(String message) {
        ValidationResult lengthResult = validateLength(message);
        if (!lengthResult.isSuccess()) return lengthResult;

        ValidationResult harmfulResult = containsHarmfulContent(message);
        if (!harmfulResult.isSuccess()) return harmfulResult;

        return ValidationResult.success();
    }
}

@Data
@Builder
public class ValidationResult {
    private boolean success;
    private String message;

    public static ValidationResult success() {
        return ValidationResult.builder().success(true).build();
    }

    public static ValidationResult failure(String message) {
        return ValidationResult.builder().success(false).message(message).build();
    }
}
```

### 6.2 输出校验 (OutputGuardrail)

```java
@Slf4j
@Component
public class OutputGuardrail {

    private static final Set<String> ALLOWED_COMPONENT_TYPES = Set.of(
        "Text", "Chart", "Form", "Card", "Button", "Divider",
        "Image", "Table", "List", "Tabs", "Spacer"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 校验 JSON 格式
     */
    public ValidationResult isValidA2UIJson(String json) {
        if (json == null || json.isEmpty()) {
            return ValidationResult.failure("输出不能为空");
        }

        try {
            JsonNode node = objectMapper.readTree(json);

            // 必须有 type 字段
            if (!node.has("type")) {
                return ValidationResult.failure("JSON 缺少 type 字段");
            }

            // 必须有 surfaceId 字段
            if (!node.has("surfaceId")) {
                return ValidationResult.failure("JSON 缺少 surfaceId 字段");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            return ValidationResult.failure("JSON 格式错误: " + e.getMessage());
        }
    }

    /**
     * 校验组件类型白名单
     */
    public ValidationResult isAllowedComponentType(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            if (node.has("component") && node.get("component").has("type")) {
                String componentType = node.get("component").get("type").asText();
                if (!ALLOWED_COMPONENT_TYPES.contains(componentType)) {
                    log.warn("不允许的组件类型: {}", componentType);
                    return ValidationResult.failure("不支持的组件类型: " + componentType);
                }
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.success(); // 非组件消息，跳过
        }
    }

    /**
     * 内容安全检查
     */
    public ValidationResult isContentSafe(String json) {
        // 检测敏感词、恶意脚本等
        // 实现具体逻辑
        return ValidationResult.success();
    }

    /**
     * 完整校验流程
     */
    public ValidationResult validate(String json) {
        ValidationResult jsonResult = isValidA2UIJson(json);
        if (!jsonResult.isSuccess()) return jsonResult;

        ValidationResult typeResult = isAllowedComponentType(json);
        if (!typeResult.isSuccess()) return typeResult;

        ValidationResult safetyResult = isContentSafe(json);
        if (!safetyResult.isSuccess()) return safetyResult;

        return ValidationResult.success();
    }
}
```

### 6.3 速率限制 (RateLimitConfig)

```java
@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean =
            new FilterRegistrationBean<>();

        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/chat/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }
}

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Counter> ipCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> sessionCounters = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE_IP = 60;
    private static final int MAX_REQUESTS_PER_MINUTE_SESSION = 120;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip = getClientIP(request);
        String sessionId = request.getParameter("sessionId");

        // IP 限流
        if (!checkRateLimit(ip, ipCounters, MAX_REQUESTS_PER_MINUTE_IP)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"请求过于频繁\"}");
            return;
        }

        // Session 限流
        if (sessionId != null &&
            !checkRateLimit(sessionId, sessionCounters, MAX_REQUESTS_PER_MINUTE_SESSION)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"会话请求过于频繁\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean checkRateLimit(String key,
                                    Map<String, Counter> counters,
                                    int maxRequests) {
        Counter counter = counters.computeIfAbsent(key,
            k -> new Counter(maxRequests, Duration.ofMinutes(1)));

        return counter.tryIncrement();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Counter {
        private final int maxRequests;
        private final Duration window;
        private final AtomicInteger count;
        private volatile long windowStart;

        public Counter(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
            this.count = new AtomicInteger(0);
            this.windowStart = System.currentTimeMillis();
        }

        public boolean tryIncrement() {
            long now = System.currentTimeMillis();
            if (now - windowStart > window.toMillis()) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }
}
```

---

## 7. 与现有代码的集成

### 7.1 现有代码处理

| 现有文件 | 处理方式 |
|---------|---------|
| `A2uiAssistant.java` | **废弃** - 由 `GeneralAgent` 替代 |
| `StreamingHandler.java` | **重构** - 集成到 `AgentCoordinator` |
| `DataQueryTool.java` | **保留** - 作为 `GeneralAgentTools` 的依赖 |
| `ChartGeneratorTool.java` | **保留** - 在 `A2UIAgent` 内部使用 |
| `DemoScenario` | **保留** - LLM 不可用时的 fallback |

### 7.2 AgentCoordinator 集成

```java
@Service
public class AgentCoordinator {

    @Autowired
    private GeneralAgent generalAgent;

    @Autowired
    private InputGuardrail inputGuardrail;

    @Autowired
    private OutputGuardrail outputGuardrail;

    /**
     * 主入口 - 整合流式处理和 Guardrails
     */
    public Flux<A2uiMessage> processMessage(String message, String sessionId) {
        // 1. 输入校验
        ValidationResult inputResult = inputGuardrail.validate(message);
        if (!inputResult.isSuccess()) {
            return Flux.just(createErrorMessage(inputResult.getMessage()));
        }

        // 2. 清理输入
        String sanitizedMessage = inputGuardrail.sanitize(message);

        // 3. 调用 GeneralAgent
        return generalAgent.chat(sanitizedMessage, sessionId)
            .map(json -> {
                // 4. 输出校验
                ValidationResult outputResult = outputGuardrail.validate(json);
                if (!outputResult.isSuccess()) {
                    log.warn("输出校验失败: {}", outputResult.getMessage());
                    return createErrorMessage("输出格式错误");
                }
                return parseA2uiMessage(json);
            })
            .onErrorResume(e -> {
                log.error("处理失败: {}", e.getMessage());
                return generateFallback(message, sessionId);
            });
    }

    /**
     * Fallback - 当 LLM 不可用时使用 Demo
     */
    private Flux<A2uiMessage> generateFallback(String message, String sessionId) {
        // 使用现有的 DemoScenario
        return scenarioManager.findMatchingScenario(message)
            .map(scenario -> scenario.execute(message, sessionId))
            .orElse(Flux.just(createDefaultErrorMessage()));
    }
}
```

### 7.3 记忆模型

```
Session
    │
    ├── GeneralAgent Memory (100 messages)
    │       │
    │       └── 完整的对话历史
    │
    └── A2UIAgent Memory (50 messages, 可选共享)
            │
            └── A2UI 组件生成上下文
```

**共享策略：** A2UIAgent 可以访问 GeneralAgent 的部分对话历史，以便生成与上下文相关的组件。

---

## 8. 文件结构

```
dev-backend/src/main/java/com/a2ui/backend/
├── A2uiBackendApplication.java
│
├── config/
│   ├── LangChain4jConfig.java         # LangChain4j 配置
│   ├── A2uiProperties.java           # 配置属性
│   ├── CorsConfig.java                 # CORS 配置
│   └── RateLimitConfig.java            # 速率限制配置
│
├── agent/                              # Agent 层 (新增)
│   ├── AgentCoordinator.java          # Agent 协调器
│   ├── GeneralAgent.java              # 接口定义
│   ├── GeneralAgentImpl.java         # 实现 (独立 LLM Agent)
│   ├── GeneralAgentTools.java         # Tools (数据查询、A2A 调用)
│   ├── A2UIAgent.java                # 接口定义
│   ├── A2UIAgentImpl.java           # 实现 (独立 LLM Agent)
│   └── A2UIComponent.java           # Structured Output DTO
│
├── llm/                                # LLM 层
│   ├── A2uiAssistant.java             # (废弃) → 由 GeneralAgent 替代
│   └── StreamingHandler.java          # (重构) → 集成到 AgentCoordinator
│
├── protocol/                           # 协议层
│   ├── model/
│   │   ├── A2UIComponent.java        # Structured Output DTO
│   │   └── A2UIRequest.java          # A2A 请求 DTO
│   ├── builder/
│   │   ├── ComponentBuilder.java
│   │   └── A2uiMessageBuilder.java
│   └── A2uiEncoder.java
│
├── guardrail/                          # Guardrail 层 (新增)
│   ├── InputGuardrail.java            # 输入校验
│   ├── OutputGuardrail.java          # 输出校验
│   └── ValidationResult.java          # 校验结果
│
├── tools/                              # 工具层
│   ├── DataQueryTool.java             # (保留) 查询数据
│   └── ChartGeneratorTool.java        # (保留) 生成图表配置
│
├── demo/                               # 演示场景
│   ├── DemoScenario.java
│   ├── ScenarioManager.java
│   ├── DataQueryScenario.java
│   └── OrderScenario.java
│
└── transport/                          # 传输层
    ├── http/
    │   └── A2uiHttpController.java
    └── websocket/
        ├── A2uiWebSocketHandler.java
        └── WebSocketConfig.java
```

---

## 9. 实现顺序

### Phase 1: 基础架构 (1-2 天)
1. 创建 agent 包结构
2. 实现 AgentCoordinator
3. 定义 Agent 接口和 DTO

### Phase 2: GeneralAgent (1-2 天)
1. 实现 GeneralAgentImpl
2. 迁移现有 Tools 到 GeneralAgentTools
3. 实现 A2UICallTool (A2A 调用)

### Phase 3: A2UIAgent (1-2 天)
1. 实现 A2UIAgentImpl（独立 LLM Agent）
2. 配置 Structured Output（LangChain4j 原生）
3. 定义 System Prompt（Skills 规则）
4. 实现记忆模型

### Phase 4: Structured Output (1 天)
1. 定义 JSON Schema
2. 实现 JSON 解析和校验
3. 增强 System Prompt

### Phase 5: Guardrails (1 天)
1. 实现 InputGuardrail
2. 实现 OutputGuardrail
3. 配置 RateLimitFilter

### Phase 6: 测试和优化 (1 天)
1. 端到端测试
2. 性能优化
3. 文档更新

---

## 10. 验证方式

### 10.1 单元测试
```bash
mvn test
```

### 10.2 集成测试
```bash
# 启动后端
export GLM_AI_API_KEY=你的API密钥
mvn spring-boot:run

# 测试通用对话
curl -N "http://localhost:8080/api/chat/stream?message=你好"

# 测试 A2UI 组件生成
curl -N "http://localhost:8080/api/chat/stream?message=显示销售图表"

# 测试表单交互
curl -N "http://localhost:8080/api/chat/stream?message=帮我查询数据"
```

### 9.3 预期输出

**通用对话：**
```json
{"type": "component", "surfaceId": "main", "componentId": "text_xxx",
 "component": {"type": "Text", "props": {"content": "你好！有什么可以帮你的？", "markdown": true}}}
```

**A2UI 组件：**
```json
{"type": "surface", "surfaceId": "main", "name": "销售数据"}
{"type": "component", "surfaceId": "main", "componentId": "chart_xxx",
 "component": {"type": "Chart", "props": {"option": {...}, "height": 300}}}
```

---

## 11. 依赖变更

```xml
<!-- LangChain4j 核心 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>1.12.2</version>
</dependency>

<!-- LangChain4j OpenAI 支持（用于 GLM 兼容 API） -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>1.12.2</version>
</dependency>

<!-- LangChain4j Agentic A2A 模块 - Agent 间通信 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic-a2a</artifactId>
    <version>1.12.2</version>
</dependency>

<!-- LangChain4j Skills 模块 - LLM 生成规则 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-skills</artifactId>
    <version>1.12.2</version>
</dependency>

<!-- Spring Boot WebFlux（支持响应式流） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- 现有依赖保持不变 -->
```
