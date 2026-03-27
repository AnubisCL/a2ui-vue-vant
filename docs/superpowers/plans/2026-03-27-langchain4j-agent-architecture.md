# LangChain4j Agent 架构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构 dev-backend 实现双 Agent 架构（GeneralAgent + A2UIAgent），集成 LangChain4j Skills、A2A 通信、Structured Output 和 Guardrails

**Architecture:** Spring 主导架构，LangChain4j 负责 LLM 调用。GeneralAgent 处理对话路由，A2UIAgent 负责 A2UI 组件生成，通过 Tool Call 实现 A2A 通信。

**Tech Stack:** Spring Boot 3.2.5, LangChain4j 1.12.2 (beta modules), Reactor, Java 17

---

## 文件结构

```
dev-backend/src/main/java/com/a2ui/backend/
├── config/
│   └── LangChain4jConfig.java              # 扩展：支持 Skills 和 A2A
│
├── agent/                                    # Agent 层 (新增)
│   ├── coordinator/
│   │   └── AgentCoordinator.java            # Agent 协调器
│   ├── general/
│   │   ├── GeneralAgent.java                # 接口
│   │   ├── GeneralAgentImpl.java           # 实现
│   │   └── GeneralAgentTools.java          # Tools (数据查询、A2A调用)
│   ├── a2ui/
│   │   ├── A2UIAgent.java                   # 接口 (@Agent)
│   │   ├── A2UIAgentImpl.java               # 实现 (独立 LLM)
│   │   └── A2UIRequest.java                 # A2A 请求 DTO
│   └── dto/
│       └── A2UIComponent.java              # Structured Output DTO
│
├── guardrail/                               # Guardrail 层 (新增)
│   ├── InputGuardrail.java                  # 输入校验
│   ├── OutputGuardrail.java                 # 输出校验
│   └── ValidationResult.java                # 校验结果
│
└── skills/                                  # Skills 定义 (新增)
    ├── text-skill.yaml
    ├── chart-skill.yaml
    ├── form-skill.yaml
    ├── card-skill.yaml
    └── layout-skill.yaml
```

---

## Phase 1: 基础架构和 DTO

### Task 1: 创建 agent 包结构和基础 DTO

**Files:**
- Create: `agent/dto/A2UIComponent.java`
- Create: `agent/a2ui/A2UIRequest.java`
- Create: `guardrail/ValidationResult.java`

- [ ] **Step 1: 创建 A2UIComponent DTO**

```java
// src/main/java/com/a2ui/backend/agent/dto/A2UIComponent.java
package com.a2ui.backend.agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class A2UIComponent {
    private String type;           // "surface" | "component"
    private String surfaceId;
    private String componentId;
    private String name;
    private ComponentSpec component;
    private String position;       // "append" | "prepend" | "replace"
}
```

- [ ] **Step 2: 创建 ComponentSpec 内部类或引用**

```java
// A2UIComponent.java 添加
@Data
@Builder
public static class ComponentSpec {
    private String type;           // "Text" | "Chart" | "Form" | "Card" | etc.
    private Map<String, Object> props;
}
```

- [ ] **Step 3: 创建 A2UIRequest DTO**

```java
// src/main/java/com/a2ui/backend/agent/a2ui/A2UIRequest.java
package com.a2ui.backend.agent.a2ui;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class A2UIRequest {
    private String intent;
    private Object data;
    private String displayType;    // "line" | "bar" | "pie" | "form" | "card"
    private String title;
    private Map<String, Object> options;
    private List<ChatMessage> chatHistory;
}
```

- [ ] **Step 4: 创建 ValidationResult**

```java
// src/main/java/com/a2ui/backend/guardrail/ValidationResult.java
package com.a2ui.backend.guardrail;

import lombok.Builder;
import lombok.Data;

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

- [ ] **Step 5: 编译验证**

```bash
cd packages/dev-backend
mvn compile -q
```

- [ ] **Step 6: Commit**

```bash
git add agent/dto/A2UIComponent.java agent/a2ui/A2UIRequest.java guardrail/ValidationResult.java
git commit -m "feat(agent): add base DTOs for agent architecture

- A2UIComponent: Structured Output DTO
- A2UIRequest: A2A request DTO
- ValidationResult: Guardrail result type

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 2: 创建 Agent 接口

**Files:**
- Create: `agent/general/GeneralAgent.java`
- Create: `agent/a2ui/A2UIAgent.java`

- [ ] **Step 1: 创建 GeneralAgent 接口**

```java
// src/main/java/com/a2ui/backend/agent/general/GeneralAgent.java
package com.a2ui.backend.agent.general;

import reactor.core.publisher.Flux;

public interface GeneralAgent {
    /**
     * 主对话入口
     * @param userMessage 用户消息
     * @param sessionId 会话 ID
     * @return 流式响应字符串
     */
    Flux<String> chat(String userMessage, Long sessionId);
}
```

- [ ] **Step 2: 创建 A2UIAgent 接口（使用 @Agent 注解）**

```java
// src/main/java/com/a2ui/backend/agent/a2ui/A2UIAgent.java
package com.a2ui.backend.agent.a2ui;

import dev.langchain4j.service.Agent;
import reactor.core.publisher.Flux;

@Agent
public interface A2UIAgent {
    /**
     * 生成 A2UI 组件
     * @param request A2UI 请求
     * @return A2UI JSON 字符串
     */
    String generate(A2UIRequest request);

    /**
     * 流式生成 A2UI 组件
     * @param request A2UI 请求
     * @return 流式 A2UI JSON
     */
    Flux<String> generateStream(A2UIRequest request);
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 4: Commit**

```bash
git add agent/general/GeneralAgent.java agent/a2ui/A2UIAgent.java
git commit -m "feat(agent): add Agent interfaces

- GeneralAgent: 对话入口接口
- A2UIAgent: @Agent 注解的 UI 生成接口

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Phase 2: Guardrails 实现

### Task 3: 实现 InputGuardrail

**Files:**
- Create: `guardrail/InputGuardrail.java`

- [ ] **Step 1: 创建 InputGuardrail**

```java
// src/main/java/com/a2ui/backend/guardrail/InputGuardrail.java
package com.a2ui.backend.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class InputGuardrail {

    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final List<String> BLOCKED_PATTERNS = List.of(
        "<script", "javascript:", "onerror=", "onclick=",
        "sql注入", "';--", "${", "#{"  // 注入检测
    );

    public ValidationResult validate(String message) {
        if (message == null || message.isEmpty()) {
            return ValidationResult.failure("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.failure(
                String.format("消息长度不能超过 %d 字符", MAX_MESSAGE_LENGTH));
        }
        for (String pattern : BLOCKED_PATTERNS) {
            if (message.toLowerCase().contains(pattern.toLowerCase())) {
                log.warn("检测到危险内容: {}", pattern);
                return ValidationResult.failure("消息包含不允许的内容");
            }
        }
        return ValidationResult.success();
    }

    public String sanitize(String message) {
        if (message == null) return null;
        return message
            .replaceAll("<script[^>]*>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+=", "")
            .trim();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit**

```bash
git add guardrail/InputGuardrail.java
git commit -m "feat(guardrail): add InputGuardrail for input validation

- Length validation (max 4000 chars)
- Harmful content detection
- HTML/script sanitization

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 4: 实现 OutputGuardrail

**Files:**
- Create: `guardrail/OutputGuardrail.java`

- [ ] **Step 1: 创建 OutputGuardrail**

```java
// src/main/java/com/a2ui/backend/guardrail/OutputGuardrail.java
package com.a2ui.backend.guardrail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutputGuardrail {

    private static final Set<String> ALLOWED_COMPONENT_TYPES = Set.of(
        "Text", "Chart", "Form", "Card", "Button", "Divider",
        "Image", "Table", "List", "Tabs", "Spacer"
    );

    private final ObjectMapper objectMapper;

    public ValidationResult validate(String json) {
        if (json == null || json.isEmpty()) {
            return ValidationResult.failure("输出不能为空");
        }

        try {
            JsonNode node = objectMapper.readTree(json);

            if (!node.has("type")) {
                return ValidationResult.failure("JSON 缺少 type 字段");
            }

            if (!node.has("surfaceId")) {
                return ValidationResult.failure("JSON 缺少 surfaceId 字段");
            }

            if (node.has("component") && node.get("component").has("type")) {
                String componentType = node.get("component").get("type").asText();
                if (!ALLOWED_COMPONENT_TYPES.contains(componentType)) {
                    log.warn("不允许的组件类型: {}", componentType);
                    return ValidationResult.failure("不支持的组件类型: " + componentType);
                }
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            return ValidationResult.failure("JSON 格式错误: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit**

```bash
git add guardrail/OutputGuardrail.java
git commit -m "feat(guardrail): add OutputGuardrail for output validation

- JSON format validation
- Required fields check (type, surfaceId)
- Component type whitelist

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Phase 3: Skills 定义

### Task 5: 创建 Skills YAML 文件

**Files:**
- Create: `resources/skills/text-skill.yaml`
- Create: `resources/skills/chart-skill.yaml`
- Create: `resources/skills/form-skill.yaml`
- Create: `resources/skills/card-skill.yaml`
- Create: `resources/skills/layout-skill.yaml`

- [ ] **Step 1: 创建 skills 目录**

```bash
mkdir -p src/main/resources/skills
```

- [ ] **Step 2: 创建 text-skill.yaml**

```yaml
# src/main/resources/skills/text-skill.yaml
name: Text Generation
description: 生成文本组件的规则
instructions: |
  当需要生成文本内容时：
  1. 使用 Text 组件类型
  2. 支持 Markdown 格式（设置 markdown: true）
  3. 重要信息使用 Card 包裹
  4. 保持内容简洁，段落分明
  5. 使用 ** 强调重要内容
```

- [ ] **Step 3: 创建 chart-skill.yaml**

```yaml
# src/main/resources/skills/chart-skill.yaml
name: Chart Generation
description: 生成图表组件的规则
instructions: |
  当需要生成图表时：
  1. 根据数据选择合适的图表类型：
     - 趋势数据（时间序列）: line
     - 对比数据（类别比较）: bar
     - 占比数据（比例分布）: pie
     - 关系数据（相关性）: scatter
  2. 必须包含：
     - title: 图表标题
     - xAxis/yAxis: 坐标轴配置
     - legend: 图例
     - tooltip: 提示框
  3. 图表高度默认 300px
  4. 使用 smooth: true 让折线图更平滑
```

- [ ] **Step 4: 创建 form-skill.yaml**

```yaml
# src/main/resources/skills/form-skill.yaml
name: Form Generation
description: 生成表单组件的规则
instructions: |
  当需要生成表单时：
  1. 使用 Form 组件类型
  2. 字段配置：
     - name: 字段名（英文，用于提交）
     - label: 显示标签（中文）
     - type: text | select | textarea | date | number
     - required: true | false
  3. select 类型必须包含 options 数组
  4. 必须包含 submitLabel（如"提交"、"查询"）
  5. 使用 action 指定表单提交标识
```

- [ ] **Step 5: 创建 card-skill.yaml**

```yaml
# src/main/resources/skills/card-skill.yaml
name: Card Generation
description: 生成卡片组件的规则
instructions: |
  当需要生成卡片时：
  1. 使用 Card 组件类型
  2. 标题：简洁，不超过一行
  3. 内容：结构化展示
  4. 可选操作按钮（Button）
  5. 卡片间距使用 spacer 调整
```

- [ ] **Step 6: 创建 layout-skill.yaml**

```yaml
# src/main/resources/skills/layout-skill.yaml
name: Layout Rules
description: 组件布局和组合规则
instructions: |
  组件布局规则：
  1. 相关组件放在一起
  2. 布局顺序：
     - 先 Surface（定义容器）
     - 再表单/筛选器
     - 后图表/列表
  3. 使用 Card 包裹重要内容
  4. 使用 Divider 分隔不同内容块
  5. 使用 Spacer 控制间距
  6. 组件 ID 必须唯一
```

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/skills/
git commit -m "feat(skills): add A2UIAgent skill definitions

- text-skill: 文本生成规则
- chart-skill: 图表生成规则
- form-skill: 表单生成规则
- card-skill: 卡片生成规则
- layout-skill: 布局规则

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Phase 4: LangChain4j 配置扩展

### Task 6: 扩展 LangChain4jConfig

**Files:**
- Modify: `config/LangChain4jConfig.java`

- [ ] **Step 1: 添加 Skills 加载配置**

```java
// 添加到 LangChain4jConfig.java
import dev.langchain4j.service.Services;
import dev.langchain4j.agent.AgentExecutor;
import dev.langchain4j.agent.Agent;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.Duration;

// 1. 添加 Skills 加载方法
private Skills loadSkills() {
    return Skills.builder()
        .loadFrom(ClassPathSkillLoader.from("skills"))
        .build();
}

// 2. 添加 A2UIAgent Bean
@Bean
public A2UIAgent a2uiAgent(StreamingChatModel streamingModel) {
    return AiServices.builder(A2UIAgent.class)
        .streamingChatModel(streamingModel)
        .chatMemoryProvider(memoryId ->
            MessageWindowChatMemory.withMaxMessages(50))
        .skills(loadSkills())
        .build();
}

// 3. 添加 GeneralAgent Bean
@Bean
public GeneralAgent generalAgent(
    StreamingChatModel streamingModel,
    GeneralAgentTools tools
) {
    return AiServices.builder(GeneralAgent.class)
        .streamingChatModel(streamingModel)
        .chatMemoryProvider(memoryId ->
            MessageWindowChatMemory.withMaxMessages(100))
        .tools(tools)
        .build();
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q 2>&1 | head -30
```

- [ ] **Step 3: Commit**

```bash
git add config/LangChain4jConfig.java
git commit -m "feat(config): extend LangChain4jConfig with Skills and Agent beans

- Add Skills loading from classpath
- Add A2UIAgent bean with Skills
- Add GeneralAgent bean with tools

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Phase 5: Agent 实现

### Task 7: 实现 GeneralAgentTools

**Files:**
- Create: `agent/general/GeneralAgentTools.java`

- [ ] **Step 1: 创建 GeneralAgentTools**

```java
// src/main/java/com/a2ui/backend/agent/general/GeneralAgentTools.java
package com.a2ui.backend.agent.general;

import com.a2ui.backend.agent.a2ui.A2UIAgent;
import com.a2ui.backend.agent.a2ui.A2UIRequest;
import com.a2ui.backend.agent.dto.A2UIComponent;
import com.a2ui.backend.tools.DataQueryTool;
import com.a2ui.backend.tools.ChartGeneratorTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.service.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralAgentTools {

    private final DataQueryTool dataQueryTool;
    private final ChartGeneratorTool chartGeneratorTool;

    @Autowired
    private A2UIAgent a2uiAgent;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("查询销售数据")
    public String querySalesData(
        @dev.langchain4j.service.metadata.P("开始日期 YYYY-MM-DD") String startDate,
        @dev.langchain4j.service.metadata.P("结束日期 YYYY-MM-DD") String endDate,
        @dev.langchain4j.service.metadata.P("产品类别，可选") String category
    ) {
        log.info("查询销售数据: {} - {} - {}", startDate, endDate, category);
        return dataQueryTool.querySalesData(startDate, endDate, category);
    }

    @Tool("生成图表配置")
    public String generateChart(
        @dev.langchain4j.service.metadata.P("图表类型: line, bar, pie") String chartType,
        @dev.langchain4j.service.metadata.P("图表标题") String title,
        @dev.langchain4j.service.metadata.P("数据 JSON") String dataJson
    ) {
        log.info("生成图表: type={}, title={}", chartType, title);
        return chartGeneratorTool.generateChart(chartType, title, dataJson);
    }

    @Tool("请求 A2UI 组件")
    public A2UIComponent requestA2UI(
        @dev.langchain4j.service.metadata.P("显示类型: chart, form, table, card") String displayType,
        @dev.langchain4j.service.metadata.P("要显示的数据 JSON") String data,
        @dev.langchain4j.service.metadata.P("意图描述") String intent,
        @dev.langchain4j.service.metadata.P("图表标题") String title
    ) {
        log.info("A2A 请求 A2UIAgent: type={}, intent={}", displayType, intent);

        A2UIRequest request = A2UIRequest.builder()
            .displayType(displayType)
            .intent(intent)
            .title(title)
            .build();

        try {
            // 处理 data 参数
            if (data != null && !data.isEmpty()) {
                Object parsedData = objectMapper.readValue(data, Object.class);
                request.setData(parsedData);
            }
            return a2uiAgent.generate(request);
        } catch (Exception e) {
            log.error("A2A 调用失败: {}", e.getMessage());
            return null;
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit**

```bash
git add agent/general/GeneralAgentTools.java
git commit -m "feat(agent): add GeneralAgentTools with Tools

- querySalesData: 查询销售数据
- generateChart: 生成图表配置
- requestA2UI: A2A 调用 A2UIAgent

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 8: 实现 AgentCoordinator

**Files:**
- Create: `agent/coordinator/AgentCoordinator.java`

- [ ] **Step 1: 创建 AgentCoordinator**

```java
// src/main/java/com/a2ui/backend/agent/coordinator/AgentCoordinator.java
package com.a2ui.backend.agent.coordinator;

import com.a2ui.backend.guardrail.InputGuardrail;
import com.a2ui.backend.guardrail.OutputGuardrail;
import com.a2ui.backend.guardrail.ValidationResult;
import com.a2ui.backend.agent.general.GeneralAgent;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentCoordinator {

    private final GeneralAgent generalAgent;
    private final InputGuardrail inputGuardrail;
    private final OutputGuardrail outputGuardrail;

    /**
     * 主入口 - 整合流式处理和 Guardrails
     */
    public Flux<A2uiMessage> processMessage(String message, String sessionId) {
        // 1. 输入校验
        ValidationResult inputResult = inputGuardrail.validate(message);
        if (!inputResult.isSuccess()) {
            log.warn("输入校验失败: {}", inputResult.getMessage());
            return Flux.just(createErrorMessage(inputResult.getMessage()));
        }

        // 2. 清理输入
        String sanitizedMessage = inputGuardrail.sanitize(message);

        // 3. 调用 GeneralAgent
        Long memoryId = sessionId.hashCode() & 0xFFFFFFFFL;

        return generalAgent.chat(sanitizedMessage, memoryId)
            .map(json -> {
                // 4. 输出校验
                ValidationResult outputResult = outputGuardrail.validate(json);
                if (!outputResult.isSuccess()) {
                    log.warn("输出校验失败: {}", outputResult.getMessage());
                    return createErrorMessage("输出格式错误");
                }
                try {
                    return A2uiEncoder.parseMessage(json);
                } catch (Exception e) {
                    log.error("解析 A2UI 消息失败: {}", e.getMessage());
                    return createErrorMessage("解析响应失败");
                }
            })
            .onErrorResume(e -> {
                log.error("处理失败: {}", e.getMessage());
                return Flux.just(createErrorMessage("处理失败: " + e.getMessage()));
            });
    }

    private A2uiMessage createErrorMessage(String content) {
        return com.a2ui.backend.protocol.model.ComponentMessage.append(
            "main",
            "error_" + System.currentTimeMillis(),
            com.a2ui.backend.protocol.model.ComponentSpec.of("Text",
                java.util.Map.of(
                    "content", "**错误:** " + content,
                    "markdown", true
                )
            )
        );
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit**

```bash
git add agent/coordinator/AgentCoordinator.java
git commit -m "feat(agent): add AgentCoordinator

- Integrates GeneralAgent, InputGuardrail, OutputGuardrail
- Main entry point for message processing
- Error handling and fallback

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Phase 6: 集成和测试

### Task 9: 集成到 StreamingHandler

**Files:**
- Modify: `llm/StreamingHandler.java`

- [ ] **Step 1: 集成 AgentCoordinator**

```java
// 在 StreamingHandler 中添加 AgentCoordinator
@Autowired
private AgentCoordinator agentCoordinator;

// 修改 generateResponse 方法
public Flux<A2uiMessage> generateResponse(String userMessage, String sessionId) {
    // 使用 AgentCoordinator 处理
    return agentCoordinator.processMessage(userMessage, sessionId);
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

- [ ] **Step 3: Commit**

```bash
git add llm/StreamingHandler.java
git commit -m "feat(integration): integrate AgentCoordinator into StreamingHandler

- Replace direct LLM call with AgentCoordinator
- Enable full agent architecture

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 10: 端到端测试

- [ ] **Step 1: 编译测试**

```bash
mvn clean compile test -q
```

- [ ] **Step 2: 启动后端测试**

```bash
export GLM_AI_API_KEY=你的API密钥
mvn spring-boot:run
```

- [ ] **Step 3: curl 测试**

```bash
# 测试通用对话
curl -N "http://localhost:8080/api/chat/stream?message=你好"

# 测试 A2UI 组件生成
curl -N "http://localhost:8080/api/chat/stream?message=显示销售图表"
```

- [ ] **Step 4: Commit 最终版本**

```bash
git add -A
git commit -m "feat: complete agent architecture implementation

- GeneralAgent + A2UIAgent dual agent architecture
- LangChain4j Skills integration
- A2A communication via Tool Call
- Input/Output Guardrails
- Structured Output support

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## 验证命令

### 编译验证
```bash
cd packages/dev-backend
mvn clean compile
```

### 启动后端
```bash
export GLM_AI_API_KEY=你的API密钥
mvn spring-boot:run
```

### API 测试
```bash
# 通用对话
curl -N "http://localhost:8080/api/chat/stream?message=你好"

# 图表生成
curl -N "http://localhost:8080/api/chat/stream?message=显示销售图表"
```
