# A2UI Backend 设计文档

> 文档版本: 1.0
> 最后更新: 2026-03-26
> 维护者: A2UI Team

---

## 目录

1. [概述](#1-概述)
2. [技术架构](#2-技术架构)
3. [目录结构](#3-目录结构)
4. [核心组件设计](#4-核心组件设计)
5. [协议层设计](#5-协议层设计)
6. [传输层设计](#6-传输层设计)
7. [LLM 集成](#7-llm-集成)
8. [演示场景](#8-演示场景)
9. [工具系统](#9-工具系统)
10. [配置管理](#10-配置管理)
11. [部署说明](#11-部署说明)
12. [当前设计问题](#12-当前设计问题)
13. [改进建议](#13-改进建议)

---

## 1. 概述

### 1.1 项目定位

A2UI Backend 是一个基于 Java Spring Boot 的后端演示项目，用于展示 A2UI 协议的完整对接流程。它集成了大语言模型（通过 LangChain4j），支持通过 HTTP SSE 和 WebSocket 两种传输方式实时推送 A2UI 协议消息。

### 1.2 核心能力

| 能力 | 描述 |
|------|------|
| **A2UI 协议解析** | 支持 surface、component、dataModel、deleteSurface 四种消息类型 |
| **双传输支持** | HTTP SSE 和 WebSocket 全双工通信 |
| **LLM 集成** | 通过 LangChain4j 接入智谱 GLM 模型 |
| **流式输出** | 支持 token 级别的流式响应 |
| **对话记忆** | 基于 MessageWindowChatMemory 的多轮对话 |
| **演示场景** | 无 LLM 时提供预定义的演示场景 |
| **AI 工具** | 预留 DataQueryTool 和 ChartGeneratorTool |

### 1.3 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2.5 |
| 语言 | Java 17 |
| 构建 | Maven |
| LLM | LangChain4j 1.12.2 |
| 协议 | A2UI Protocol (JSONL) |
| 传输 | SSE + WebSocket |
| JSON | Jackson |
| 异步 | Project Reactor |

---

## 2. 技术架构

### 2.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              Frontend (Vue)                              │
│                     a2ui-vue / Any A2UI Client                          │
└──────────────┬───────────────────────────────────────┬──────────────────┘
               │                                       │
               │  text/event-stream / websocket        │
               ▼                                       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                          A2UI Backend                                     │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                    Transport Layer                                   │  │
│  │  ┌─────────────────────┐    ┌─────────────────────────────────┐   │  │
│  │  │  A2uiHttpController │    │    A2uiWebSocketHandler          │   │  │
│  │  │  GET /api/chat/stream│    │    WebSocket /ws/chat            │   │  │
│  │  └──────────┬──────────┘    └────────────────┬────────────────┘   │  │
│  └─────────────┼───────────────────────────────┼────────────────────┘  │
│                │                               │                        │
│                └──────────────┬────────────────┘                        │
│                               ▼                                           │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                    StreamingHandler                                  │  │
│  │  ┌────────────────────────────────────────────────────────────┐   │  │
│  │  │  Priority: 1) DemoScenario > 2) A2uiAssistant > 3) Fallback │   │  │
│  │  └────────────────────────────────────────────────────────────┘   │  │
│  └─────────────────────────────┬──────────────────────────────────────┘  │
│                                │                                          │
│  ┌─────────────────────────────┼──────────────────────────────────────┐  │
│  │                    LLM Layer                                        │  │
│  │  ┌──────────────────────────┼───────────────────────────────────┐   │  │
│  │  │      A2uiAssistant (LangChain4j AiServices)               │   │  │
│  │  │  ┌────────────┐  ┌─────────────┐  ┌────────────────────┐  │   │  │
│  │  │  │ SystemMsg  │  │ MemoryProvider│  │ StreamingChatModel │  │   │  │
│  │  │  │ + Prompt   │  │ + 100 msgs   │  │ + OpenAiStreaming  │  │   │  │
│  │  │  └────────────┘  └─────────────┘  └────────────────────┘  │   │  │
│  │  └───────────────────────────────────────────────────────────┘   │  │
│  │                                                                     │  │
│  │  ┌───────────────────────────────────────────────────────────┐   │  │
│  │  │  Tools (Not Wired Yet)                                     │   │  │
│  │  │  ┌─────────────────┐    ┌─────────────────────────┐       │   │  │
│  │  │  │ DataQueryTool   │    │ ChartGeneratorTool       │       │   │  │
│  │  │  └─────────────────┘    └─────────────────────────┘       │   │  │
│  │  └───────────────────────────────────────────────────────────┘   │  │
│  └─────────────────────────────┬────────────────────────────────────┘  │
│                                │                                         │
│                                ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    External: GLM API                                 │
│  │               https://open.bigmodel.cn/api/coding/paas/v4           │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 请求流程

```
用户消息
    │
    ▼
┌─────────────────────────────────────────┐
│  HTTP: /api/chat/stream?message=xxx    │
│  WS: WebSocket /ws/chat                 │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│         StreamingHandler                │
│  generateResponse(message, sessionId)   │
└────────────────┬────────────────────────┘
                 │
         ┌───────┴───────┐
         │               │
         ▼               ▼
┌─────────────┐  ┌─────────────────┐
│ DemoScenario │  │ A2uiAssistant   │
│ (highest)   │  │ (LLM fallback)  │
└─────────────┘  └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ LLM Streaming   │
                 │ Flux<String>   │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ extractA2uiMsgs│
                 │ parseAssistant  │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ Flux<A2uiMessage> │
                 └────────┬────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│SurfaceMsg   │  │ComponentMsg │  │DataModelMsg │
└─────────────┘  └─────────────┘  └─────────────┘
         │                │                │
         └────────────────┼────────────────┘
                          ▼
                 ┌─────────────────┐
                 │ SSE / WebSocket │
                 │ ServerSentEvent  │
                 └────────┬────────┘
                          │
                          ▼
                    Frontend
```

---

## 3. 目录结构

```
dev-backend/
├── pom.xml                                    # Maven 配置
├── DESIGN.md                                  # 本文档
├── README.md                                  # 项目说明
└── src/main/
    ├── java/com/a2ui/backend/
    │   ├── A2uiBackendApplication.java         # 启动入口
    │   │
    │   ├── config/                             # 配置层
    │   │   ├── A2uiProperties.java              # 配置属性 (YAML 映射)
    │   │   ├── LangChain4jConfig.java           # LangChain4j Bean 配置
    │   │   └── CorsConfig.java                  # CORS 跨域配置
    │   │
    │   ├── protocol/                           # 协议层
    │   │   ├── model/                          # 消息模型
    │   │   │   ├── A2uiMessage.java            # 基类
    │   │   │   ├── SurfaceMessage.java         # Surface 消息
    │   │   │   ├── ComponentMessage.java       # Component 消息
    │   │   │   ├── DataModelMessage.java       # DataModel 消息
    │   │   │   └── DeleteSurfaceMessage.java   # 删除 Surface
    │   │   ├── builder/                        # 构建器
    │   │   │   ├── ComponentBuilder.java       # Component 构建器
    │   │   │   └── A2uiMessageBuilder.java     # 消息构建器
    │   │   └── A2uiEncoder.java                # JSON 编解码器
    │   │
    │   ├── transport/                         # 传输层
    │   │   ├── http/
    │   │   │   └── A2uiHttpController.java     # SSE HTTP 控制器
    │   │   └── websocket/
    │   │       ├── A2uiWebSocketHandler.java   # WebSocket 处理器
    │   │       └── WebSocketConfig.java        # WebSocket 配置
    │   │
    │   ├── llm/                               # LLM 层
    │   │   ├── A2uiAssistant.java             # AI 助手接口
    │   │   └── StreamingHandler.java           # 流式响应处理器
    │   │
    │   ├── demo/                              # 演示场景
    │   │   ├── DemoScenario.java               # 场景接口
    │   │   ├── ScenarioManager.java            # 场景管理器
    │   │   ├── DataQueryScenario.java          # 数据查询场景
    │   │   └── OrderScenario.java              # 订单管理场景
    │   │
    │   └── tools/                             # AI 工具 (未集成)
    │       ├── DataQueryTool.java              # 数据查询工具
    │       └── ChartGeneratorTool.java         # 图表生成工具
    │
    └── resources/
        └── application.yml                    # 应用配置
```

---

## 4. 核心组件设计

### 4.1 A2uiProperties

配置属性类，映射 `application.yml` 中的 `a2ui.*` 配置：

```java
@ConfigurationProperties(prefix = "a2ui")
public class A2uiProperties {
    private LlmConfig llm;
    private DemoConfig demo;
    private TransportConfig transport;
    private CorsConfig cors;
}
```

### 4.2 StreamingHandler

流式响应的核心分发器，采用**优先级策略**：

```
Priority 1: DemoScenario  (最高优先，匹配关键词)
    ↓ 不匹配
Priority 2: A2uiAssistant (LLM 生成)
    ↓ LLM 不可用
Priority 3: DemoResponse  (兜底回复)
```

关键方法：
- `generateResponse(userMessage, sessionId)` → `Flux<A2uiMessage>`
- `isLlmAvailable()` → `boolean`

### 4.3 LangChain4jConfig

LangChain4j Bean 配置：

```java
@Bean
@ConditionalOnProperty(prefix = "a2ui.llm", name = "api-key")
public A2uiAssistant a2uiAssistant() {
    // 1. 构建 HTTP 客户端 (含超时配置)
    JdkHttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder()
        .connectTimeout(timeout)
        .readTimeout(timeout);

    // 2. 构建流式模型
    StreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
        .httpClientBuilder(httpClientBuilder)
        .apiKey(apiKey)
        .modelName(model)
        .baseUrl(baseUrl)
        .temperature(temperature)
        .build();

    // 3. 构建 AiServices
    return AiServices.builder(A2uiAssistant.class)
        .streamingChatModel(streamingModel)
        .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
        .build();
}
```

---

## 5. 协议层设计

### 5.1 消息类型

| 类型 | 类 | 用途 |
|------|-----|------|
| `surface` | SurfaceMessage | 创建/更新 UI 容器 |
| `component` | ComponentMessage | 添加/更新/替换组件 |
| `dataModel` | DataModelMessage | 更新响应式数据模型 |
| `deleteSurface` | DeleteSurfaceMessage | 删除 Surface |

### 5.2 ComponentSpec 支持的组件类型

| 类型 | 用途 | 示例场景 |
|------|------|----------|
| Text | 文本/Markdown 显示 | 消息、说明文字 |
| Chart | ECharts 图表 | 销售趋势、统计数据 |
| Card | 卡片容器 | 订单卡片、产品卡片 |
| Form | 表单输入 | 筛选条件、用户输入 |
| Button | 按钮 | 操作按钮、提交按钮 |
| Divider | 分割线 | 内容分隔 |
| Image | 图片 | 产品图片 |
| Spacer | 空白间距 | 布局调整 |
| Tabs | 标签页 | 多视图切换 |
| Table | 表格 | 数据列表 |
| List | 列表 | 订单列表 |

### 5.3 ComponentMessage Position 策略

```java
public static final String POSITION_APPEND = "append";    // 末尾追加
public static final String POSITION_PREPEND = "prepend";   // 头部插入
public static final String POSITION_REPLACE = "replace";   // 替换已有
```

### 5.4 JSON 编码

使用 Jackson 进行 JSON 序列化，支持：
- `encode()` - 紧凑格式
- `encodePretty()` - 格式化输出
- `encodeAsJsonl()` - JSONL 格式 (每行一个 JSON)

---

## 6. 传输层设计

### 6.1 HTTP SSE

**端点**: `GET /api/chat/stream`

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> stream(
    @RequestParam String message,
    @RequestParam(required = false) String sessionId
)
```

SSE 事件格式：
```
id: 0
event: message
data: {"type":"surface","surfaceId":"main","name":"Chat"}

id: 1
event: message
data: {"type":"component","surfaceId":"main","componentId":"text1",...}
```

### 6.2 WebSocket

**端点**: `/ws/chat`

支持双向通信，可发送消息和接收流式响应。使用 Reactor 的 `Disposable` 管理订阅生命周期。

---

## 7. LLM 集成

### 7.1 A2uiAssistant 系统提示词

系统提示词指导 LLM 输出 A2UI JSON 格式：

```
你是一个智能助手，可以使用 A2UI 协议返回丰富的用户界面组件。

## A2UI 协议说明
当用户查询数据时，可以返回表单收集筛选条件
收集条件后，返回图表展示数据

## 响应格式
你应该先输出说明文字（用 Text 组件，markdown 格式），
然后输出相关组件。每个组件需要有唯一的 componentId。
```

### 7.2 对话记忆

使用 `MessageWindowChatMemory`，最大保留 100 条消息：

```java
.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
```

### 7.3 当前 LLM 输出问题

**问题**: GLM 模型当前逐 token 返回，每个字符被作为单独的 Text 组件发送：

```json
{"type":"component","surfaceId":"main","componentId":"text_xxx","component":{"type":"Text","props":{"markdown":true,"content":"j"}}}
{"type":"component","surfaceId":"main","componentId":"text_xxx","component":{"type":"Text","props":{"markdown":true,"content":"so"}}}
```

这导致前端收到大量碎片化组件，需要累积 buffer 后再渲染。

---

## 8. 演示场景

### 8.1 DataQueryScenario

**关键词**: sales, statistics, report, chart, 销售, 统计, 报表, 图表, 数据

**流程**:
1. 显示加载动画
2. 显示筛选表单 (日期范围 + 类别选择)
3. 显示销售图表 (ECharts)
4. 显示成功提示

### 8.2 OrderScenario

**关键词**: order, purchase, product, buy, 订单, 购买, 商品

**流程**:
1. 显示订单列表 (Card 组件)
2. 支持点击查看订单详情

---

## 9. 工具系统

### 9.1 DataQueryTool

提供示例销售数据查询：
- `querySalesData(startDate, endDate, category)`
- `queryMonthlyTrends(year)`
- `getCategories()`
- `getTopProducts(limit)`

### 9.2 ChartGeneratorTool

生成 ECharts 配置：
- `generateChart(chartType, title, dataJson)`
- `generateSalesChart(title, months, sales, orders)`
- `generatePieChart(title, categories, values)`

### 9.3 工具未集成 ⚠️

**当前状态**: Tools 已定义但未注册到 AiServices，需要在 `LangChain4jConfig` 中添加：

```java
@Autowired
private DataQueryTool dataQueryTool;

@Autowired
private ChartGeneratorTool chartGeneratorTool;

return AiServices.builder(A2uiAssistant.class)
    .streamingChatModel(streamingModel)
    .chatMemoryProvider(...)
    .tools(dataQueryTool, chartGeneratorTool)  // 未添加
    .build();
```

---

## 10. 配置管理

### 10.1 application.yml 核心配置

```yaml
a2ui:
  llm:
    provider: glm
    api-key: ${GLM_AI_API_KEY}        # 环境变量
    model: glm-5
    base-url: https://open.bigmodel.cn/api/coding/paas/v4
    temperature: 0.7
    max-tokens: 4096
    timeout: 120000

  demo:
    enabled: true
    streaming-delay-ms: 100

  transport:
    sse:
      enabled: true
      endpoint: /api/chat/stream
    websocket:
      enabled: true
      endpoint: /ws/chat

  cors:
    allowed-origins:
      - http://localhost:5173
```

---

## 11. 部署说明

### 11.1 启动方式

```bash
cd packages/dev-backend
export GLM_AI_API_KEY=你的API密钥
mvn spring-boot:run
```

### 11.2 健康检查

```bash
# SSE 传输
curl http://localhost:8080/api/chat/health

# WebSocket
ws://localhost:8080/ws/chat
```

### 11.3 测试流式接口

```bash
curl -N "http://localhost:8080/api/chat/stream?message=hello"
```

---

## 12. 当前设计问题

### 问题 1: LLM 输出碎片化 🔴 严重

**现象**: GLM 模型逐 token 返回，导致每个字符成为单独的 Text 组件。

**影响**: 前端收到大量碎片化组件，无法正确渲染完整内容。

**根因**: `extractA2uiMessages()` 方法对每个 token 都创建新组件。

**建议**: 累积 buffer，直到收到完整的 JSON 对象或句子再发送。

---

### 问题 2: Tools 未集成 🟡 中等

**现象**: `DataQueryTool` 和 `ChartGeneratorTool` 已定义但未注册到 AiServices。

**影响**: LLM 无法使用工具获取真实数据，只能依靠预训练知识。

**根因**: `LangChain4jConfig` 中未调用 `.tools()` 方法。

---

### 问题 3: ComponentMessage 位置语义混乱 🟡 中等

**现象**: `parseAssistantStream()` 中每个 token 都创建新的 componentId，导致组件堆积。

**影响**: 前端会渲染大量重复组件。

**根因**: LLM 输出的 JSON 被逐字符切割，无法正确识别 JSON 边界。

---

### 问题 4: 缺少 JSON 解析缓冲 🟡 中等

**现象**: `extractA2uiMessages()` 逐字符扫描 JSON，只能找到完整的 `{...}` 对象。

**影响**: 如果一个 JSON 对象跨多个 SSE 事件到达，可能无法正确组装。

**根因**: 没有实现跨 chunk 的 JSON buffer。

---

### 问题 5: Demo 场景与 LLM 冲突 🟡 中等

**现象**: DemoScenario 优先级高于 LLM，导致配置了 API Key 后仍然优先使用演示场景。

**影响**: 无法测试 LLM 的真实能力。

**根因**: `StreamingHandler.generateResponse()` 中 DemoScenario 检查在前。

---

### 问题 6: 缺少错误重试机制 🟢 轻微

**现象**: LLM 调用失败后直接返回 fallback，没有重试。

**影响**: 偶发网络错误无法恢复。

---

### 问题 7: 缺少请求限流 🟢 轻微

**现象**: 没有对并发请求数进行限制。

**影响**: 可能被恶意请求打满资源。

---

### 问题 8: WebSocket Session 内存泄漏风险 🟢 轻微

**现象**: `activeStreams` 的清理依赖于 `afterConnectionClosed` 和 `cancelActiveStream`。

**影响**: 如果连接异常断开，可能导致 `Disposable` 未及时清理。

---

### 问题 9: ComponentSpec 与前端 Props 不同步 🟢 轻微

**现象**: 后端的 ComponentSpec 支持的组件类型和前端实际实现的可能不一致。

**影响**: 后端返回的组件类型前端可能无法渲染。

---

### 问题 10: 缺少端到端测试 🟢 轻微

**现象**: 没有集成测试验证 LLM → 后端 → 前端的完整流程。

**影响**: 协议格式变更无法及时发现。

---

## 13. 改进建议

### 13.1 高优先级 (应立即修复)

#### 修复 1: LLM 输出累积缓冲

在 `StreamingHandler` 中实现输出缓冲：

```java
private static class OutputBuffer {
    private final StringBuilder builder = new StringBuilder();
    private final List<A2uiMessage> pending = new ArrayList<>();

    public void append(String chunk) {
        builder.append(chunk);
        // 检测完整的 JSON 对象
        extractCompleteMessages();
    }

    private void extractCompleteMessages() {
        String text = builder.toString();
        // 使用栈匹配括号，找到完整的 JSON 对象
        // 找到完整对象后，解析并添加到 pending
    }

    public List<A2uiMessage> drainPending() {
        List<A2uiMessage> result = new ArrayList<>(pending);
        pending.clear();
        return result;
    }
}
```

#### 修复 2: 集成 Tools

```java
@Autowired
private DataQueryTool dataQueryTool;

@Autowired
private ChartGeneratorTool chartGeneratorTool;

return AiServices.builder(A2uiAssistant.class)
    .streamingChatModel(streamingModel)
    .chatMemoryProvider(...)
    .tools(dataQueryTool, chartGeneratorTool)
    .build();
```

### 13.2 中优先级

#### 改进 1: 调整 Demo/LLM 优先级

建议让用户可配置优先级，或添加 bypass 参数。

#### 改进 2: 添加 JSON 跨 Chunk 组装

确保跨多个 SSE 事件的 JSON 能正确组装。

### 13.3 低优先级

- 添加请求限流
- 添加端到端测试
- 优化 ComponentSpec 与前端同步机制

---

## 附录 A: 文件清单

| 文件 | 行数 | 职责 |
|------|------|------|
| A2uiBackendApplication.java | ~20 | 启动入口 |
| A2uiProperties.java | ~120 | 配置映射 |
| LangChain4jConfig.java | ~85 | LLM Bean |
| CorsConfig.java | ~60 | CORS 配置 |
| A2uiMessage.java | ~50 | 消息基类 |
| SurfaceMessage.java | ~60 | Surface 消息 |
| ComponentMessage.java | ~130 | Component 消息 |
| DataModelMessage.java | ~70 | DataModel 消息 |
| DeleteSurfaceMessage.java | ~40 | 删除消息 |
| ComponentSpec.java | ~200 | 组件规范 |
| A2uiEncoder.java | ~100 | JSON 编解码 |
| ComponentBuilder.java | ~250 | 组件构建器 |
| A2uiMessageBuilder.java | ~300 | 消息构建器 |
| A2uiHttpController.java | ~135 | HTTP 控制器 |
| A2uiWebSocketHandler.java | ~195 | WebSocket |
| WebSocketConfig.java | ~30 | WS 配置 |
| A2uiAssistant.java | ~110 | AI 接口 |
| StreamingHandler.java | ~470 | 流式处理器 |
| DemoScenario.java | ~30 | 场景接口 |
| ScenarioManager.java | ~80 | 场景管理 |
| DataQueryScenario.java | ~300 | 查询场景 |
| OrderScenario.java | ~350 | 订单场景 |
| DataQueryTool.java | ~200 | 数据工具 |
| ChartGeneratorTool.java | ~250 | 图表工具 |

**总计**: ~24 个 Java 文件，约 3500+ 行代码
