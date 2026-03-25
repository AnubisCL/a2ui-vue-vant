<template>
  <div class="streaming-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">🌊 流式协议演示</h1>
      <p class="text-sm text-gray-500">模拟 AI Agent 的实时流式响应</p>
    </div>

    <!-- Controls -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex flex-wrap gap-2 mb-4">
        <van-button type="primary" size="small" @click="startChatDemo">
          💬 聊天演示
        </van-button>
        <van-button type="success" size="small" @click="startFormDemo">
          📝 表单演示
        </van-button>
        <van-button type="warning" size="small" @click="startCardDemo">
          🃏 卡片演示
        </van-button>
        <van-button type="default" size="small" @click="startStreamingText">
          ✨ 流式文本
        </van-button>
      </div>

      <div class="flex items-center gap-4">
        <label class="text-sm text-gray-600">流式速度:</label>
        <van-slider v-model="streamSpeed" :min="50" :max="500" :step="50" class="flex-1" />
        <span class="text-sm text-gray-500 w-16">{{ streamSpeed }}ms</span>
      </div>
    </div>

    <!-- Status -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex items-center gap-2">
        <van-loading v-if="isStreaming" type="spinner" size="16" />
        <span :class="isStreaming ? 'text-blue-500' : 'text-gray-400'" class="text-sm">
          {{ isStreaming ? '正在接收流式数据...' : '等待开始' }}
        </span>
        <span v-if="messageCount > 0" class="text-xs text-gray-400 ml-auto">
          已接收 {{ messageCount }} 条消息
        </span>
      </div>
    </div>

    <!-- A2UI Renderer -->
    <div class="bg-white rounded-xl shadow-sm p-4 min-h-[400px]">
      <A2uiRenderer :surface-id="currentSurface" @event="handleComponentEvent" />
    </div>

    <!-- Message Log -->
    <div class="bg-white rounded-xl shadow-sm p-4 mt-4">
      <div class="flex items-center justify-between mb-2">
        <h3 class="font-semibold text-gray-700">消息日志</h3>
        <van-button size="mini" @click="clearLog">清空</van-button>
      </div>
      <div class="bg-gray-900 rounded-lg p-3 max-h-[300px] overflow-auto">
        <pre v-for="(log, index) in messageLogs" :key="index" class="text-xs text-green-400 font-mono mb-1">{{ log }}</pre>
        <pre v-if="messageLogs.length === 0" class="text-xs text-gray-500">等待消息...</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

// State
const streamSpeed = ref(200)
const isStreaming = ref(false)
const messageCount = ref(0)
const currentSurface = ref('demo')
const messageLogs = ref<string[]>([])

// A2UI Hooks
const { handleMessage, clearSurface } = useA2UI()

// 日志记录
const logMessage = (msg: string) => {
  const timestamp = new Date().toLocaleTimeString()
  messageLogs.value.unshift(`[${timestamp}] ${msg}`)
  if (messageLogs.value.length > 50) {
    messageLogs.value.pop()
  }
}

// 发送流式消息
const sendStreamingMessage = async (message: string) => {
  isStreaming.value = true
  logMessage(`发送: ${message.substring(0, 50)}...`)
  handleMessage(message)
  messageCount.value++
  await new Promise(r => setTimeout(r, streamSpeed.value))
}

// 聊天演示
const startChatDemo = async () => {
  clearSurface('demo')
  messageCount.value = 0
  isStreaming.value = true

  const messages = [
    // Surface 定义
    JSON.stringify({
      type: 'surface',
      surfaceId: 'demo',
      name: '聊天演示'
    }) + '\n',

    // 欢迎消息
    JSON.stringify({
      type: 'component',
      componentId: 'header',
      surfaceId: 'demo',
      component: {
        type: 'Text',
        props: {
          content: '# 🤖 AI 助手\n\n欢迎使用 A2UI 流式聊天演示！',
          size: 'large'
        }
      }
    }) + '\n',

    // 聊天记录
    JSON.stringify({
      type: 'component',
      componentId: 'chat-list',
      surfaceId: 'demo',
      component: {
        type: 'List',
        props: {
          gap: 12,
          scrollable: true
        }
      }
    }) + '\n',

    // AI 消息（流式更新）
    JSON.stringify({
      type: 'component',
      componentId: 'ai-msg-1',
      surfaceId: 'demo',
      parentId: 'chat-list',
      component: {
        type: 'Card',
        props: {
          title: '🤖 AI 助手',
          content: '正在思考...'
        }
      }
    }) + '\n',
  ]

  for (const msg of messages) {
    await sendStreamingMessage(msg)
  }

  // 模拟流式文本更新
  const streamingTexts = [
    '你好',
    '你好！我是',
    '你好！我是 AI 助手',
    '你好！我是 AI 助手，很高兴',
    '你好！我是 AI 助手，很高兴见到你。',
    '你好！我是 AI 助手，很高兴见到你。\n\n有什么我可以帮助你的吗？'
  ]

  for (const text of streamingTexts) {
    await sendStreamingMessage(
      JSON.stringify({
        type: 'component',
        componentId: 'ai-msg-1',
        surfaceId: 'demo',
        component: {
          type: 'Card',
          props: {
            title: '🤖 AI 助手',
            content: text
          }
        }
      }) + '\n'
    )
  }

  // 添加用户输入
  await sendStreamingMessage(
    JSON.stringify({
      type: 'component',
      componentId: 'user-input',
      surfaceId: 'demo',
      component: {
        type: 'TextField',
        props: {
          label: '你的消息',
          placeholder: '输入你的问题...',
          type: 'shortText'
        }
      }
    }) + '\n'
  )

  // 添加发送按钮
  await sendStreamingMessage(
    JSON.stringify({
      type: 'component',
      componentId: 'send-btn',
      surfaceId: 'demo',
      component: {
        type: 'Button',
        props: {
          label: '发送',
          variant: 'primary',
          icon: 'arrow'
        }
      }
    }) + '\n'
  )

  isStreaming.value = false
  showToast('演示完成！')
}

// 表单演示
const startFormDemo = async () => {
  clearSurface('demo')
  messageCount.value = 0
  isStreaming.value = true

  const messages = [
    // Surface
    JSON.stringify({ type: 'surface', surfaceId: 'demo', name: '表单演示' }) + '\n',

    // 标题
    JSON.stringify({
      type: 'component',
      componentId: 'title',
      surfaceId: 'demo',
      component: {
        type: 'Text',
        props: { content: '# 📝 用户信息表单', size: 'large' }
      }
    }) + '\n',

    // 姓名输入
    JSON.stringify({
      type: 'component',
      componentId: 'name-field',
      surfaceId: 'demo',
      component: {
        type: 'TextField',
        props: { label: '姓名', placeholder: '请输入姓名', required: true }
      }
    }) + '\n',

    // 邮箱输入
    JSON.stringify({
      type: 'component',
      componentId: 'email-field',
      surfaceId: 'demo',
      component: {
        type: 'TextField',
        props: { label: '邮箱', placeholder: '请输入邮箱', type: 'shortText' }
      }
    }) + '\n',

    // 年龄滑块
    JSON.stringify({
      type: 'component',
      componentId: 'age-slider',
      surfaceId: 'demo',
      component: {
        type: 'Slider',
        props: { label: '年龄', min: 1, max: 100, showValue: true }
      }
    }) + '\n',

    // 性别选择
    JSON.stringify({
      type: 'component',
      componentId: 'gender-picker',
      surfaceId: 'demo',
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
    }) + '\n',

    // 同意协议
    JSON.stringify({
      type: 'component',
      componentId: 'agree-checkbox',
      surfaceId: 'demo',
      component: {
        type: 'CheckBox',
        props: { label: '我同意用户协议' }
      }
    }) + '\n',

    // 提交按钮
    JSON.stringify({
      type: 'component',
      componentId: 'submit-btn',
      surfaceId: 'demo',
      component: {
        type: 'Button',
        props: { label: '提交', variant: 'primary', size: 'large' }
      }
    }) + '\n',
  ]

  for (const msg of messages) {
    await sendStreamingMessage(msg)
  }

  isStreaming.value = false
  showToast('表单演示完成！')
}

// 卡片演示
const startCardDemo = async () => {
  clearSurface('demo')
  messageCount.value = 0
  isStreaming.value = true

  const messages = [
    // Surface
    JSON.stringify({ type: 'surface', surfaceId: 'demo', name: '卡片演示' }) + '\n',

    // 标题
    JSON.stringify({
      type: 'component',
      componentId: 'title',
      surfaceId: 'demo',
      component: {
        type: 'Text',
        props: { content: '# 🃏 商品卡片展示', size: 'large' }
      }
    }) + '\n',
  ]

  // 添加商品卡片
  const products = [
    { name: 'iPhone 15 Pro', price: '¥8,999', image: '📱' },
    { name: 'MacBook Pro', price: '¥14,999', image: '💻' },
    { name: 'AirPods Pro', price: '¥1,899', image: '🎧' },
  ]

  for (const product of products) {
    messages.push(
      JSON.stringify({
        type: 'component',
        componentId: `card-${product.name}`,
        surfaceId: 'demo',
        component: {
          type: 'Card',
          props: {
            title: `${product.image} ${product.name}`,
            subtitle: product.price,
            elevated: true,
            actions: [
              { label: '加入购物车', action: 'add-to-cart', primary: true },
              { label: '收藏', action: 'favorite' }
            ]
          }
        }
      }) + '\n'
    )
  }

  for (const msg of messages) {
    await sendStreamingMessage(msg)
  }

  isStreaming.value = false
  showToast('卡片演示完成！')
}

// 流式文本演示
const startStreamingText = async () => {
  clearSurface('demo')
  messageCount.value = 0
  isStreaming.value = true

  // 发送初始消息
  await sendStreamingMessage(
    JSON.stringify({ type: 'surface', surfaceId: 'demo', name: '流式文本' }) + '\n'
  )

  await sendStreamingMessage(
    JSON.stringify({
      type: 'component',
      componentId: 'stream-text',
      surfaceId: 'demo',
      component: {
        type: 'Text',
        props: { content: '', markdown: true }
      }
    }) + '\n'
  )

  // 模拟 AI 流式生成文本
  const fullText = `# A2UI 协议介绍

A2UI (Agent to UI) 是一个声明式 UI 协议，专为 AI Agent 与用户交互而设计。

## 核心特性

1. **声明式语法** - 使用 JSON 描述 UI 结构
2. **流式传输** - 支持实时增量更新
3. **响应式绑定** - JSON Pointer 数据引用
4. **跨平台** - 支持多种前端框架

## 使用场景

- 🤖 AI 助手对话界面
- 📊 数据可视化仪表盘
- 📝 动态表单生成
- 🎮 交互式游戏界面

感谢使用 A2UI！
`

  // 逐字流式输出
  let currentText = ''
  const chars = fullText.split('')

  for (let i = 0; i < chars.length; i += 3) {
    currentText += chars.slice(i, i + 3).join('')

    await sendStreamingMessage(
      JSON.stringify({
        type: 'component',
        componentId: 'stream-text',
        surfaceId: 'demo',
        component: {
          type: 'Text',
          props: { content: currentText, markdown: true }
        }
      }) + '\n'
    )
  }

  isStreaming.value = false
  showToast('流式文本演示完成！')
}

// 事件处理
const handleComponentEvent = (event: any) => {
  logMessage(`事件: ${JSON.stringify(event)}`)
  showToast(`收到事件: ${event.type || 'click'}`)
}

// 清空日志
const clearLog = () => {
  messageLogs.value = []
}

// 生命周期
onMounted(() => {
  // 初始化一个空的 surface
  handleMessage(JSON.stringify({ type: 'surface', surfaceId: 'demo', name: '演示' }) + '\n')
})
</script>

<style scoped>
.streaming-demo {
  max-width: 100%;
  margin: 0 auto;
}

pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
