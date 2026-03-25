# A2UI Vue SDK

> A2UI (Agent to UI) Vue SDK - 用于 Vue 3 的声明式 UI 协议实现

[English](./README.md)

A2UI 是 Google 提出的声明式 UI 协议，用于 AI Agent 与用户的交互。本 SDK 使 Vue 3 应用能够接收、解析和渲染来自 AI Agent 的 A2UI 消息流。

## 特性

- 🚀 **Vue 3 原生** - 基于 Vue 3 Composition API 和 TypeScript 构建
- 📦 **基于 Vant 4** - 移动端优先的 UI 组件库
- 🎨 **28+ 组件** - 完整的组件库，覆盖所有使用场景
- 🔄 **响应式数据绑定** - 支持 JSON Pointer 的双向数据绑定
- 🌊 **流式支持** - 实时流式协议支持
- 📝 **Markdown 支持** - 通过 markdown-it 完整支持 Markdown
- 🎨 **UnoCSS 驱动** - 原子化 CSS，性能优异
- 📱 **移动端优先** - 专为移动端 H5 应用优化

## 包结构

| 包名 | 描述 |
|------|------|
| `@a2ui/core` | 框架无关的核心 SDK（类型、解析器、存储） |
| `@a2ui/vue-components` | 基于 Vant 4 的 Vue 3 组件实现 |
| `@a2ui/vue-plugin` | Vue 插件，便于快速集成 |
| `@a2ui/dev` | 开发和示例应用 |

## 安装

```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 构建所有包
pnpm build

# 运行测试
pnpm test
```

## 快速开始

### 1. 安装插件

```typescript
// main.ts
import { createApp } from 'vue'
import App from './App.vue'
import { createA2UI } from '@a2ui/vue-plugin'
import '@a2ui/vue-components/style.css'
import 'vant/lib/index.css'

const app = createApp(App)
app.use(createA2UI())
app.mount('#app')
```

### 2. 使用渲染器

```vue
<template>
  <A2uiRenderer :surface-id="surfaceId" @event="handleEvent" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const surfaceId = ref('main')
const { connect, handleMessage } = useA2UI()

// 连接 WebSocket 以接收流式数据
const ws = new WebSocket('ws://your-agent-endpoint')
ws.onmessage = (event) => {
  handleMessage(event.data)
}
```

### 3. Mock 流式演示

```typescript
import { useA2UI } from '@a2ui/vue-plugin'

const { handleMessage } = useA2UI()

// 模拟流式消息
async function mockStreamingChat(message: string) {
  const responses = [
    // Surface 定义
    '{"type":"surface","surfaceId":"chat","name":"聊天界面"}\n',

    // 组件更新（流式）
    '{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"正在思考..."}}}\n',

    // 更新组件（流式文本）
    '{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"你好！有什么可以帮助你的？"}}}\n',

    // 添加输入组件
    '{"type":"component","componentId":"input1","surfaceId":"chat","component":{"type":"TextField","props":{"placeholder":"输入你的消息...","label":"消息"}}}\n',
  ]

  for (const msg of responses) {
    await new Promise(r => setTimeout(r, 500))
    handleMessage(msg)
  }
}
```

## 组件库

### 布局组件
| 组件 | Vant 基础 | 描述 |
|------|-----------|------|
| Row | - | 水平 flex 容器 (UnoCSS) |
| Column | - | 垂直 flex 容器 (UnoCSS) |
| List | van-list | 可滚动列表，支持懒加载 |

### 展示组件
| 组件 | Vant 基础 | 描述 |
|------|-----------|------|
| Text | - | 文本展示，支持 Markdown |
| Image | van-image | 图片展示，支持懒加载 |
| Icon | van-icon | 图标展示 |
| Video | 原生 | 视频播放器 |
| AudioPlayer | 原生 | 音频播放器，带控制条 |
| Divider | van-divider | 水平/垂直分割线 |

### 输入组件
| 组件 | Vant 基础 | 描述 |
|------|-----------|------|
| Button | van-button | 可点击按钮 |
| TextField | van-field | 文本输入框 |
| CheckBox | van-checkbox | 复选框 |
| Slider | van-slider | 滑动条 |
| ChoicePicker | van-radio-group | 单选/多选 |
| DateTimeInput | van-datetime-picker | 日期时间选择器 |

### 容器组件
| 组件 | Vant 基础 | 描述 |
|------|-----------|------|
| Card | van-cell-group | 卡片容器 |
| Tabs | van-tabs | 标签页容器 |
| Modal | van-popup | 模态弹窗 |

### 通用组件
| 组件 | Vant 基础 | 描述 |
|------|-----------|------|
| Badge | van-badge | 徽标/计数 |
| Avatar | van-image | 用户头像 |
| Progress | van-progress | 进度条 |
| Spinner | van-loading | 加载指示器 |
| Tag | van-tag | 标签 |
| Alert | van-notice-bar | 警告提示 |
| Switch | van-switch | 开关 |
| Tooltip | van-popover | 悬浮提示 |
| Skeleton | van-skeleton | 骨架屏 |
| Empty | van-empty | 空状态 |

## A2UI 协议

### 消息类型

```typescript
// Surface 定义
{
  "type": "surface",
  "surfaceId": "main",
  "name": "主界面"
}

// 组件创建/更新
{
  "type": "component",
  "componentId": "button1",
  "surfaceId": "main",
  "component": {
    "type": "Button",
    "props": {
      "label": "点击我",
      "variant": "primary"
    }
  }
}

// 数据模型更新
{
  "type": "data",
  "path": "/user/name",
  "value": "张三"
}
```

### 流式格式

消息以 JSONL 格式（JSON Lines）发送：
```
{"type":"surface","surfaceId":"chat","name":"聊天"}
{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"你好"}}}
{"type":"data","path":"/count","value":42}
```

## Props 映射

A2UI Props 自动映射到 Vant Props：

| A2UI Prop | Vant Prop | 说明 |
|-----------|-----------|------|
| `variant: 'primary'` | `type: 'primary'` | 按钮类型 |
| `size: 'medium'` | `size: 'normal'` | 组件尺寸 |
| `disabled` | `disabled` | 直接映射 |
| `label` | `label` | 表单标签 |

## 开发

```bash
# 启动开发服务器
pnpm dev

# 构建库
pnpm build

# 类型检查
pnpm typecheck

# 运行测试
pnpm test
```

## 浏览器支持

- Chrome >= 80
- Safari >= 13.1
- Firefox >= 78
- iOS Safari >= 13.4
- Android Chrome >= 80

## 许可证

MIT

## 相关链接

- [Vant 4 文档](https://vant-ui.github.io/vant/#/zh-CN)
- [A2UI 协议规范](https://github.com/google/a2ui)
- [UnoCSS 文档](https://unocss.dev/)
