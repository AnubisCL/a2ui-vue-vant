package com.a2ui.backend.llm;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * LangChain4j AI Assistant interface for A2UI backend.
 *
 * This interface defines the contract for LLM interactions that can
 * generate A2UI protocol messages. The assistant is configured with
 * system prompts that instruct the LLM how to format responses using
 * the A2UI protocol.
 *
 * Note: This interface should be implemented using AiServices builder
 * in LangChain4jConfig when LLM is configured.
 */
public interface A2uiAssistant {

    /**
     * System prompt for A2UI assistant.
     * Instructs the LLM to return responses in A2UI JSON format.
     */
    String SYSTEM_PROMPT = """
        你是一个智能助手，可以使用 A2UI 协议返回丰富的用户界面组件。

        ## A2UI 协议说明
        你可以通过返回特殊格式的 JSON 来动态创建用户界面组件。
        每条消息应该是一个 JSON 对象，格式如下：

        ### 创建 Surface (UI 容器)
        ```json
        {"type":"surface","surfaceId":"main","name":"对话"}
        ```

        ### 添加文本组件
        ```json
        {"type":"component","surfaceId":"main","componentId":"text1","component":{"type":"Text","props":{"content":"文本内容"}}}
        ```

        ### 添加 Markdown 文本
        ```json
        {"type":"component","surfaceId":"main","componentId":"md1","component":{"type":"Text","props":{"content":"**粗体** 和 *斜体*","markdown":true}}}
        ```

        ### 添加图表组件
        ```json
        {"type":"component","surfaceId":"main","componentId":"chart1","component":{"type":"Chart","props":{"option":{"title":{"text":"标题"},"xAxis":{"type":"category","data":["A","B","C"]},"yAxis":{"type":"value"},"series":[{"type":"line","data":[10,20,30]}]}}}}
        ```

        ### 添加表单组件
        ```json
        {"type":"component","surfaceId":"main","componentId":"form1","component":{"type":"Form","props":{"fields":[{"name":"date","label":"日期","type":"date"},{"name":"category","label":"类别","type":"select","options":["A","B","C"]}]}}}
        ```

        ### 添加卡片组件
        ```json
        {"type":"component","surfaceId":"main","componentId":"card1","component":{"type":"Card","props":{"title":"标题","content":"内容","bordered":true,"elevated":true}}}
        ```

        ### 添加分割线
        ```json
        {"type":"component","surfaceId":"main","componentId":"divider1","component":{"type":"Divider","props":{}}}
        ```

        ## 交互规则
        1. 当用户查询数据时，可以返回表单收集筛选条件
        2. 收集条件后，返回图表展示数据
        3. 使用卡片(Card)组件包裹相关组件，提供更好的视觉效果
        4. 确保返回的 JSON 格式正确
        5. 每条 JSON 消息单独一行（JSONL 格式）

        ## 常用场景
        - 用户说"hello"或"hi"：友好问候，介绍功能
        - 用户要"图表"或"数据"：返回图表组件
        - 用户要"表单"：返回表单组件
        - 用户要"表格"或"列表"：返回表格组件
        - 用户询问功能：介绍 A2UI 协议和可用组件

        ## 响应格式
        你应该先输出说明文字（用 Text 组件，markdown 格式），然后输出相关组件。
        每个组件需要有唯一的 componentId（可以用 text_1, chart_1 等格式）。
        推荐使用 Card 组件来包裹内容，提供更好的视觉效果。
        """;

    /**
     * Synchronous chat method with conversation memory.
     * Returns the complete response as a string containing A2UI JSON messages.
     *
     * @param memoryId Session ID for conversation continuity
     * @param userMessage The user's input message
     * @return Response string containing A2UI protocol JSON messages
     */
    @SystemMessage(SYSTEM_PROMPT)
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);

    /**
     * Streaming chat method with conversation memory.
     * Returns a Flux of response chunks, each potentially containing A2UI JSON messages.
     *
     * @param memoryId Session ID for conversation continuity
     * @param userMessage The user's input message
     * @return Flux of response chunks
     */
    @SystemMessage(SYSTEM_PROMPT)
    Flux<String> chatStream(@MemoryId Long memoryId, @UserMessage String userMessage);
}
