# A2UI Vue SDK 接入说明文档

## 目录

- [快速开始](#快速开始)
- [组件定义](#组件定义)
- [样式系统](#样式系统)
- [事件处理](#事件处理)
- [流式协议](#流式协议)
- [完整示例](#完整示例)

---

## 快速开始

### 1. 安装

```bash
pnpm install @a2ui/vue-plugin @a2ui/core vant
```

### 2. 在 Vue 应用中引入

```typescript
// main.ts
import { createApp } from 'vue'
import { createA2UI } from '@a2ui/vue-plugin'
import App from './App.vue'

// 引入样式
import 'uno.css'
import 'vant/lib/index.css'

const app = createApp(App)

// 安装 A2UI 插件
app.use(createA2UI())

app.mount('#app')
```

### 3. 使用渲染器

```vue
<template>
  <A2uiRenderer
    :surface-id="surfaceId"
    @event="handleEvent"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const surfaceId = ref('main')
const { handleMessage, sendAction } = useA2UI()

// 处理用户交互事件
const handleEvent = (componentId: string, eventType: string, payload: unknown) => {
  // 发送事件到后端
  sendAction({
    type: 'userAction',
    version: 'v0.9',
    surfaceId: surfaceId.value,
    action: {
      componentId,
      event: eventType,
      payload,
      timestamp: Date.now(),
    },
  })
}
</script>
```

---

## 组件定义

### 消息格式

A2UI 使用 JSONL (JSON Lines) 格式传输消息，每行一个 JSON 对象。

### Surface 定义

Surface 是组件的容器，代表一个独立的 UI 界面。

```json
{
  "type": "surface",
  "surfaceId": "chat",
  "name": "聊天界面"
}
```

### 组件定义

```json
{
  "type": "component",
  "surfaceId": "chat",
  "componentId": "submit-btn",
  "parentId": "button-container",
  "component": {
    "type": "Button",
    "props": {
      "label": "发送",
      "variant": "primary",
      "size": "medium"
    }
  }
}
```

### 数据模型更新

```json
{
  "type": "data",
  "surfaceId": "chat",
  "path": "/user/name",
  "value": "张三"
}
```

---

## 样式系统

A2UI 使用 **Vant 4 + UnoCSS** 的混合样式方案。

### 组件级样式（通过 Props）

每个组件都有内置的样式属性：

#### 通用样式属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `margin` | `number \| [top, right, bottom, left]` | 外边距 |
| `width` | `string \| number` | 宽度 |
| `height` | `string \| number` | 高度 |

#### 布局组件样式

**Row（水平布局）**
```json
{
  "type": "Row",
  "props": {
    "align": "center",
    "valign": "center",
    "gap": 12,
    "margin": [0, 0, 16, 0]
  }
}
```

**Column（垂直布局）**
```json
{
  "type": "Column",
  "props": {
    "align": "stretch",
    "valign": "start",
    "gap": 8,
    "scrollable": true
  }
}
```

#### 文本样式

```json
{
  "type": "Text",
  "props": {
    "content": "# 标题\n\n正文内容",
    "size": "large",
    "weight": "bold",
    "color": "#333333",
    "align": "left",
    "markdown": true,
    "maxLines": 3
  }
}
```

| size | 说明 |
|------|------|
| `small` | 小号文字 |
| `medium` | 中号文字（默认）|
| `large` | 大号文字 |
| `xlarge` | 超大号文字 |

| weight | 说明 |
|--------|------|
| `normal` | 正常 |
| `medium` | 中等 |
| `semibold` | 半粗 |
| `bold` | 粗体 |

#### 按钮样式

```json
{
  "type": "Button",
  "props": {
    "label": "提交",
    "variant": "primary",
    "size": "medium"
  }
}
```

| variant | 说明 | Vant 映射 |
|---------|------|-----------|
| `primary` | 主要按钮 | `type="primary"` |
| `secondary` | 次要按钮 | `type="default" plain` |
| `borderless` | 无边框按钮 | `type="default" hairline` |
| `danger` | 危险按钮 | `type="danger"` |

| size | 说明 | Vant 映射 |
|------|------|-----------|
| `small` | 小号 | `size="small"` |
| `medium` | 中号 | `size="normal"` |
| `large` | 大号 | `size="large"` |

#### 卡片样式

```json
{
  "type": "Card",
  "props": {
    "title": "卡片标题",
    "subtitle": "副标题",
    "content": "卡片内容",
    "elevated": true,
    "borderRadius": 8,
    "padding": 16,
    "actions": [
      { "label": "确认", "action": "confirm", "primary": true },
      { "label": "取消", "action": "cancel" }
    ]
  }
}
```

### 自定义主题

通过 CSS 变量覆盖 Vant 主题：

```css
:root {
  --van-primary-color: #0066cc;
  --van-success-color: #10b981;
  --van-danger-color: #dc3545;
  --van-warning-color: #f59e0b;
}
```

---

## 事件处理

### 事件流程

```
用户交互 → 组件触发事件 → A2uiRenderer 捕获 → sendAction 发送到后端 → 后端处理返回新消息
```

### 事件类型

| 事件类型 | 触发场景 | 组件 |
|----------|----------|------|
| `click` | 点击 | Button, Card, Icon, Image |
| `change` | 值变化 | TextField, CheckBox, ChoicePicker, Slider, DateTimeInput |
| `submit` | 提交（Enter键）| TextField |
| `action` | 卡片操作 | Card |
| `close` | 关闭 | Modal |

### 组件事件定义

在组件 props 中定义事件处理：

```json
{
  "type": "Button",
  "props": {
    "label": "提交表单",
    "variant": "primary",
    "onClick": "submit-form"
  }
}
```

```json
{
  "type": "TextField",
  "props": {
    "label": "用户名",
    "placeholder": "请输入用户名",
    "onChange": "username-changed",
    "onSubmit": "username-submit"
  }
}
```

### 前端事件处理

```typescript
import { useA2UI } from '@a2ui/vue-plugin'
import { A2UI_VERSION } from '@a2ui/core'

const { sendAction } = useA2UI()

// 处理组件事件
const handleEvent = (componentId: string, eventType: string, payload: unknown) => {
  sendAction({
    type: 'userAction',
    version: A2UI_VERSION,
    surfaceId: 'main',
    action: {
      componentId,
      event: eventType,
      payload,
      timestamp: Date.now(),
    },
  })
}
```

### UserActionMessage 格式

```typescript
interface UserActionMessage {
  type: 'userAction'
  version: 'v0.9'
  surfaceId: string
  action: {
    componentId: string
    event: string
    payload?: unknown
    timestamp?: number
  }
}
```

---

## 流式协议

### 连接建立

```typescript
const { handleMessage } = useA2UI()

// WebSocket 连接
const ws = new WebSocket('ws://your-agent-endpoint')

ws.onmessage = (event) => {
  // 处理每一行 JSON 消息
  const lines = event.data.split('\n')
  for (const line of lines) {
    if (line.trim()) {
      handleMessage(line)
    }
  }
}
```

### 流式更新示例

后端流式发送消息：

```
{"type":"surface","surfaceId":"chat","name":"聊天"}
{"type":"component","surfaceId":"chat","componentId":"msg1","component":{"type":"Text","props":{"content":"思考中..."}}}
{"type":"component","surfaceId":"chat","componentId":"msg1","component":{"type":"Text","props":{"content":"你好！"}}}
{"type":"component","surfaceId":"chat","componentId":"msg1","component":{"type":"Text","props":{"content":"你好！有什么可以帮助你的？"}}}
```

---

## 完整示例

### 聊天界面

```typescript
// 模拟流式聊天
async function startChat() {
  const messages = [
    // 1. 创建 Surface
    JSON.stringify({
      type: 'surface',
      surfaceId: 'chat',
      name: 'AI 聊天'
    }),

    // 2. 添加标题
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'header',
      component: {
        type: 'Text',
        props: {
          content: '# 🤖 AI 助手',
          size: 'large',
          weight: 'bold'
        }
      }
    }),

    // 3. 添加消息列表容器
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'messages',
      component: {
        type: 'Column',
        props: {
          gap: 12,
          scrollable: true
        }
      }
    }),

    // 4. 添加 AI 消息（流式更新）
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'ai-msg',
      parentId: 'messages',
      component: {
        type: 'Card',
        props: {
          title: '🤖 AI',
          content: '正在思考...'
        }
      }
    }),

    // 5. 流式更新内容
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'ai-msg',
      component: {
        type: 'Card',
        props: {
          title: '🤖 AI',
          content: '你好！'
        }
      }
    }),

    // 6. 添加输入框
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'input',
      component: {
        type: 'TextField',
        props: {
          label: '消息',
          placeholder: '输入你的问题...',
          type: 'shortText'
        }
      }
    }),

    // 7. 添加发送按钮
    JSON.stringify({
      type: 'component',
      surfaceId: 'chat',
      componentId: 'send-btn',
      component: {
        type: 'Button',
        props: {
          label: '发送',
          variant: 'primary',
          size: 'large'
        }
      }
    })
  ]

  for (const msg of messages) {
    handleMessage(msg + '\n')
    await sleep(300) // 模拟流式延迟
  }
}
```

### 表单示例

```typescript
async function createForm() {
  handleMessage(JSON.stringify({
    type: 'surface',
    surfaceId: 'form',
    name: '用户注册'
  }) + '\n')

  // 表单字段
  const fields = [
    {
      id: 'name',
      label: '姓名',
      placeholder: '请输入姓名',
      required: true
    },
    {
      id: 'email',
      label: '邮箱',
      placeholder: '请输入邮箱',
      type: 'shortText'
    },
    {
      id: 'age',
      label: '年龄',
      type: 'number',
      min: 1,
      max: 120
    }
  ]

  for (const field of fields) {
    handleMessage(JSON.stringify({
      type: 'component',
      surfaceId: 'form',
      componentId: `field-${field.id}`,
      component: {
        type: 'TextField',
        props: field
      }
    }) + '\n')
  }

  // 性别选择
  handleMessage(JSON.stringify({
    type: 'component',
    surfaceId: 'form',
    componentId: 'gender',
    component: {
      type: 'ChoicePicker',
      props: {
        label: '性别',
        mode: 'single',
        style: 'buttons',
        choices: [
          { value: 'male', label: '男' },
          { value: 'female', label: '女' },
          { value: 'other', label: '其他' }
        ]
      }
    }
  }) + '\n')

  // 同意协议
  handleMessage(JSON.stringify({
    type: 'component',
    surfaceId: 'form',
    componentId: 'agree',
    component: {
      type: 'CheckBox',
      props: {
        label: '我同意用户协议',
        checked: false
      }
    }
  }) + '\n')

  // 提交按钮
  handleMessage(JSON.stringify({
    type: 'component',
    surfaceId: 'form',
    componentId: 'submit',
    component: {
      type: 'Button',
      props: {
        label: '注册',
        variant: 'primary',
        size: 'large'
      }
    }
  }) + '\n')
}
```

---

## 图表组件

Chart 组件基于 ECharts 实现，支持完整的数据可视化功能。

### 基础配置

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| option | object | - | ECharts 完整配置对象（必填）|
| width | string\|number | '100%' | 图表宽度 |
| height | string\|number | '300px' | 图表高度 |
| autoResize | boolean | true | 容器尺寸变化时自动调整 |
| theme | string | 'light' | 图表主题 ('light' \| 'dark' \| 自定义) |
| renderer | 'canvas'\|'svg' | 'canvas' | 渲染器类型 |

### 使用示例

#### 折线图

```json
{
  "type": "component",
  "surfaceId": "dashboard",
  "componentId": "line-chart",
  "component": {
    "type": "Chart",
    "props": {
      "height": "300px",
      "option": {
        "title": { "text": "销售趋势" },
        "xAxis": { "type": "category", "data": ["1月", "2月", "3月"] },
        "yAxis": { "type": "value" },
        "series": [{ "type": "line", "data": [100, 200, 150] }]
      }
    }
  }
}
```

#### 饼图

```json
{
  "type": "component",
  "surfaceId": "dashboard",
  "componentId": "pie-chart",
  "component": {
    "type": "Chart",
    "props": {
      "height": "400px",
      "option": {
        "series": [{
          "type": "pie",
          "radius": "50%",
          "data": [
            { "value": 1048, "name": "搜索引擎" },
            { "value": 735, "name": "直接访问" },
            { "value": 580, "name": "邮件营销" }
          ]
        }]
      }
    }
  }
}
```

### 流式更新

Chart 组件完全支持 A2UI 的流式协议。通过重复发送相同 componentId 的消息，可以实现图表数据的实时更新：

```json
// 初始创建图表
{"type":"component","surfaceId":"dashboard","componentId":"realtime-chart","component":{"type":"Chart","props":{"option":{"xAxis":{"data":[]},"series":[{"data":[]}]}}}}

// 流式更新数据（增量）
{"type":"component","surfaceId":"dashboard","componentId":"realtime-chart","component":{"type":"Chart","props":{"option":{"xAxis":{"data":["1秒","2秒","3秒"]},"series":[{"data":[10,20,15]}]}}}}
```

### 依赖说明

使用 Chart 组件需要安装 echarts：

```bash
pnpm install echarts
```

### 事件

| 事件 | 说明 | 回调参数 |
|------|------|----------|
| ready | 图表初始化完成 | ECharts 实例 |
| click | 点击图表元素 | 事件对象 |

---

## 组件清单

| 类别 | 组件 | 说明 |
|------|------|------|
| **布局** | Row | 水平布局容器 |
| | Column | 垂直布局容器 |
| | List | 列表（支持模板渲染）|
| **展示** | Text | 文本（支持 Markdown）|
| | Image | 图片 |
| | Icon | 图标 |
| | Video | 视频 |
| | AudioPlayer | 音频播放器 |
| | Chart | 图表（ECharts）|
| | Divider | 分割线 |
| **输入** | Button | 按钮 |
| | TextField | 文本输入框 |
| | CheckBox | 复选框 |
| | ChoicePicker | 单选/多选 |
| | DateTimeInput | 日期时间选择 |
| | Slider | 滑动条 |
| **容器** | Card | 卡片 |
| | Tabs | 标签页 |
| | Modal | 模态框 |
| **通用** | Badge | 徽标 |
| | Avatar | 头像 |
| | Progress | 进度条 |
| | Spinner | 加载指示器 |
| | Tag | 标签 |
| | Alert | 警告提示 |
| | Switch | 开关 |
| | Tooltip | 悬浮提示 |
| | Skeleton | 骨架屏 |
| | Empty | 空状态 |

---

## 常见问题

### Q: 组件不显示？

1. 确保已正确引入 Vant 样式：`import 'vant/lib/index.css'`
2. 确保 Surface 已创建
3. 检查 componentId 是否唯一

### Q: 事件不触发？

1. 确保 `A2uiRenderer` 绑定了 `@event` 处理器
2. 检查组件是否支持该事件类型
3. 确认 `sendAction` 正确调用

### Q: 样式不生效？

1. 检查 props 是否正确传递
2. 确认 Vant 样式已引入
3. 使用浏览器开发者工具检查样式覆盖

---

## 相关链接

- [Vant 4 文档](https://vant-ui.github.io/vant/#/zh-CN)
- [UnoCSS 文档](https://unocss.dev/)
- [A2UI 协议规范](https://github.com/google/a2ui)
