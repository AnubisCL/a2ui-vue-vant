<template>
  <div class="components-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">🧩 A2UI 组件演示</h1>
      <p class="text-sm text-gray-500">展示所有 A2UI 组件的使用方式（流式发送）</p>
    </div>

    <!-- Controls -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex items-center gap-4 mb-4">
        <label class="text-sm text-gray-600">流式速度:</label>
        <van-slider v-model="streamSpeed" :min="50" :max="500" :step="50" class="flex-1" />
        <span class="text-sm text-gray-500 w-16">{{ streamSpeed }}ms</span>
      </div>
    </div>

    <!-- Category Tabs -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <van-tabs v-model:active="activeCategory" shrink>
        <van-tab v-for="cat in categories" :key="cat.id" :title="cat.name" :name="cat.id">
          <div class="py-4">
            <div class="grid grid-cols-3 gap-2">
              <van-button
                v-for="demo in cat.demos"
                :key="demo.id"
                :type="currentDemo === demo.id ? 'primary' : 'default'"
                size="small"
                :disabled="isStreaming"
                @click="runDemo(demo.id)"
              >
                {{ demo.name }}
              </van-button>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <!-- Status -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex items-center gap-2">
        <van-loading v-if="isStreaming" type="spinner" size="16" />
        <span :class="isStreaming ? 'text-blue-500' : 'text-gray-400'" class="text-sm">
          {{ isStreaming ? '正在流式发送消息...' : '等待选择演示' }}
        </span>
        <span v-if="messageCount > 0" class="text-xs text-gray-400 ml-auto">
          已发送 {{ messageCount }} 条消息
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
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const currentSurface = ref('components-demo')
const activeCategory = ref('display')
const currentDemo = ref('')
const streamSpeed = ref(150)
const isStreaming = ref(false)
const messageCount = ref(0)
const messageLogs = ref<string[]>([])

const { handleMessage, clearSurface } = useA2UI()

// 日志记录
const logMessage = (msg: string) => {
  const timestamp = new Date().toLocaleTimeString()
  messageLogs.value.unshift(`[${timestamp}] ${msg}`)
  if (messageLogs.value.length > 50) {
    messageLogs.value.pop()
  }
}

// 清空日志
const clearLog = () => {
  messageLogs.value = []
}

// 发送流式消息
const sendStreamingMessage = async (message: object) => {
  isStreaming.value = true
  const msgStr = JSON.stringify(message)
  logMessage(msgStr.length > 100 ? msgStr.substring(0, 100) + '...' : msgStr)
  handleMessage(msgStr + '\n')
  messageCount.value++
  await new Promise(r => setTimeout(r, streamSpeed.value))
}

// Demo definitions
const categories = reactive([
  {
    id: 'display',
    name: '展示',
    demos: [
      { id: 'text', name: '文本' },
      { id: 'image', name: '图片' },
      { id: 'icon', name: '图标' },
      { id: 'divider', name: '分割线' },
    ]
  },
  {
    id: 'input',
    name: '输入',
    demos: [
      { id: 'button', name: '按钮' },
      { id: 'textfield', name: '文本框' },
      { id: 'checkbox', name: '复选框' },
      { id: 'slider', name: '滑块' },
      { id: 'picker', name: '选择器' },
    ]
  },
  {
    id: 'container',
    name: '容器',
    demos: [
      { id: 'card', name: '卡片' },
      { id: 'tabs', name: '标签页' },
    ]
  },
  {
    id: 'layout',
    name: '布局',
    demos: [
      { id: 'row', name: '水平布局' },
      { id: 'column', name: '垂直布局' },
      { id: 'list', name: '列表' },
    ]
  },
  {
    id: 'common',
    name: '通用',
    demos: [
      { id: 'badge', name: '徽标' },
      { id: 'progress', name: '进度条' },
      { id: 'tag', name: '标签' },
      { id: 'spinner', name: '加载' },
      { id: 'alert', name: '警告' },
    ]
  },
  {
    id: 'chart',
    name: '图表',
    demos: [
      { id: 'chart-line', name: '折线图' },
      { id: 'chart-bar', name: '柱状图' },
      { id: 'chart-pie', name: '饼图' },
    ]
  },
])

// Demo implementations (streaming)
const demos: Record<string, () => Promise<void>> = {
  // Display demos
  text: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '文本演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-1',
      component: {
        type: 'Text',
        props: {
          content: '# 标题文本\n\n这是一段**加粗**和*斜体*的文本。\n\n- 列表项 1\n- 列表项 2',
          size: 'medium',
          markdown: true
        }
      }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-row',
      component: { type: 'Row', props: { gap: 16, valign: 'center', margin: [16, 0, 0, 0] } }
    })

    for (const size of ['small', 'medium', 'large', 'xlarge']) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `size-${size}`,
        parentId: 'text-row',
        component: { type: 'Text', props: { content: `${size}`, size } }
      })
    }

    isStreaming.value = false
    showToast('文本演示完成')
  },

  image: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图片演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📷 图片组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'img-1',
      component: {
        type: 'Image',
        props: {
          src: 'https://picsum.photos/400/200',
          width: '100%',
          height: 200,
          fit: 'cover',
          borderRadius: 8
        }
      }
    })

    isStreaming.value = false
    showToast('图片演示完成')
  },

  icon: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图标演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🔷 图标组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'icon-row',
      component: { type: 'Row', props: { gap: 24, valign: 'center', margin: [16, 0, 0, 0] } }
    })

    for (const name of ['home-o', 'user-o', 'star-o', 'like-o', 'setting-o']) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `icon-${name}`,
        parentId: 'icon-row',
        component: {
          type: 'Icon',
          props: { name, size: 'xlarge', color: '#1989fa' }
        }
      })
    }

    isStreaming.value = false
    showToast('图标演示完成')
  },

  divider: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '分割线演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ➖ 分割线组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-above',
      component: { type: 'Text', props: { content: '上方内容' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'divider-1',
      component: { type: 'Divider', props: { margin: 16 } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-below',
      component: { type: 'Text', props: { content: '下方内容' } }
    })

    isStreaming.value = false
    showToast('分割线演示完成')
  },

  // Input demos
  button: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '按钮演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🔘 按钮组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'btn-row-1',
      component: { type: 'Row', props: { gap: 12, wrap: true, margin: [16, 0, 0, 0] } }
    })

    for (const btn of [
      { variant: 'primary', label: '主要' },
      { variant: 'secondary', label: '次要' },
      { variant: 'borderless', label: '无边框' },
      { variant: 'danger', label: '危险' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `btn-${btn.variant}`,
        parentId: 'btn-row-1',
        component: { type: 'Button', props: btn }
      })
    }

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-sizes',
      component: { type: 'Text', props: { content: '**不同尺寸：**', margin: [16, 0, 8, 0] } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'btn-row-2',
      component: { type: 'Row', props: { gap: 12, valign: 'center' } }
    })

    for (const btn of [
      { size: 'small', label: '小', variant: 'primary' },
      { size: 'medium', label: '中', variant: 'primary' },
      { size: 'large', label: '大', variant: 'primary' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `btn-size-${btn.size}`,
        parentId: 'btn-row-2',
        component: { type: 'Button', props: btn }
      })
    }

    isStreaming.value = false
    showToast('按钮演示完成')
  },

  textfield: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '文本框演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📝 文本框组件', size: 'large' } }
    })

    for (const field of [
      { id: 'tf1', label: '普通文本', placeholder: '请输入文本' },
      { id: 'tf2', label: '数字输入', type: 'number', placeholder: '请输入数字' },
      { id: 'tf3', label: '密码输入', type: 'obscured', placeholder: '请输入密码' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: field.id,
        component: { type: 'TextField', props: field }
      })
    }

    isStreaming.value = false
    showToast('文本框演示完成')
  },

  checkbox: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '复选框演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ☑️ 复选框组件', size: 'large' } }
    })

    for (const cb of [
      { label: '未选中', checked: false },
      { label: '已选中', checked: true },
      { label: '禁用状态', checked: false, disabled: true }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `cb-${cb.label}`,
        component: { type: 'CheckBox', props: cb }
      })
    }

    isStreaming.value = false
    showToast('复选框演示完成')
  },

  slider: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '滑块演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🎚️ 滑块组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'slider-1',
      component: {
        type: 'Slider',
        props: { label: '基础滑块', min: 0, max: 100, showValue: true }
      }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'slider-2',
      component: {
        type: 'Slider',
        props: { label: '步长为10', min: 0, max: 100, step: 10, showValue: true }
      }
    })

    isStreaming.value = false
    showToast('滑块演示完成')
  },

  picker: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '选择器演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🎯 选择器组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'picker-1',
      component: {
        type: 'ChoicePicker',
        props: {
          label: '单选（按钮样式）',
          mode: 'single',
          style: 'buttons',
          choices: [
            { value: '1', label: '选项A' },
            { value: '2', label: '选项B' },
            { value: '3', label: '选项C' }
          ]
        }
      }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'picker-2',
      component: {
        type: 'ChoicePicker',
        props: {
          label: '多选（标签样式）',
          mode: 'multiple',
          style: 'chips',
          choices: [
            { value: '1', label: '标签1' },
            { value: '2', label: '标签2' },
            { value: '3', label: '标签3' }
          ]
        }
      }
    })

    isStreaming.value = false
    showToast('选择器演示完成')
  },

  // Container demos
  card: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '卡片演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🃏 卡片组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'card-1',
      component: {
        type: 'Card',
        props: {
          title: '基础卡片',
          content: '这是一个简单的卡片组件，用于展示内容。',
          elevated: true
        }
      }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'card-2',
      component: {
        type: 'Card',
        props: {
          title: '带操作卡片',
          subtitle: '副标题',
          content: '这个卡片带有操作按钮。',
          elevated: true,
          actions: [
            { label: '确认', action: 'confirm', primary: true },
            { label: '取消', action: 'cancel' }
          ]
        }
      }
    })

    isStreaming.value = false
    showToast('卡片演示完成')
  },

  tabs: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '标签页演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📑 标签页组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'tabs-1',
      component: {
        type: 'Tabs',
        props: {
          tabs: [
            { id: 'tab1', label: '首页' },
            { id: 'tab2', label: '分类' },
            { id: 'tab3', label: '我的' }
          ]
        }
      }
    })

    isStreaming.value = false
    showToast('标签页演示完成')
  },

  // Layout demos
  row: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '水平布局演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ↔️ 水平布局组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'row-1',
      component: { type: 'Row', props: { gap: 12, align: 'center', valign: 'center' } }
    })

    for (let i = 1; i <= 3; i++) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `row-item-${i}`,
        parentId: 'row-1',
        component: {
          type: 'Card',
          props: { title: `卡片 ${i}`, content: '水平排列', elevated: true }
        }
      })
    }

    isStreaming.value = false
    showToast('水平布局演示完成')
  },

  column: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '垂直布局演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ↕️ 垂直布局组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'col-1',
      component: { type: 'Column', props: { gap: 12, align: 'stretch' } }
    })

    for (let i = 1; i <= 3; i++) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `col-item-${i}`,
        parentId: 'col-1',
        component: {
          type: 'Card',
          props: { title: `卡片 ${i}`, content: '垂直排列', elevated: true }
        }
      })
    }

    isStreaming.value = false
    showToast('垂直布局演示完成')
  },

  list: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '列表演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📋 列表布局', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'list-1',
      component: { type: 'Column', props: { gap: 8 } }
    })

    for (const item of ['项目 A', '项目 B', '项目 C', '项目 D', '项目 E']) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `list-item-${item}`,
        parentId: 'list-1',
        component: {
          type: 'Card',
          props: { title: item, content: '列表项内容', elevated: true }
        }
      })
    }

    isStreaming.value = false
    showToast('列表演示完成')
  },

  // Common demos
  badge: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '徽标演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🔴 徽标组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'badge-row',
      component: { type: 'Row', props: { gap: 24, valign: 'center', margin: [16, 0, 0, 0] } }
    })

    for (const badge of [
      { content: '5', color: '#f44336' },
      { content: '99+', color: '#ff9800' },
      { content: 'NEW', color: '#4caf50' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `badge-${badge.content}`,
        parentId: 'badge-row',
        component: { type: 'Badge', props: badge }
      })
    }

    isStreaming.value = false
    showToast('徽标演示完成')
  },

  progress: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '进度条演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📊 进度条组件', size: 'large' } }
    })

    for (const prog of [
      { percentage: 25, color: '#1989fa' },
      { percentage: 50, color: '#07c160' },
      { percentage: 75, color: '#ff976a' },
      { percentage: 100, color: '#4caf50' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `prog-${prog.percentage}`,
        component: { type: 'Progress', props: prog }
      })
    }

    isStreaming.value = false
    showToast('进度条演示完成')
  },

  tag: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '标签演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🏷️ 标签组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'tag-row',
      component: { type: 'Row', props: { gap: 8, wrap: true, margin: [16, 0, 0, 0] } }
    })

    for (const tag of [
      { label: '默认', type: 'default' },
      { label: '主要', type: 'primary' },
      { label: '成功', type: 'success' },
      { label: '警告', type: 'warning' },
      { label: '危险', type: 'danger' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `tag-${tag.type}`,
        parentId: 'tag-row',
        component: { type: 'Tag', props: tag }
      })
    }

    isStreaming.value = false
    showToast('标签演示完成')
  },

  spinner: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '加载演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ⏳ 加载组件', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'spinner-row',
      component: { type: 'Row', props: { gap: 24, valign: 'center', margin: [16, 0, 0, 0] } }
    })

    for (const type of ['spinner', 'circular', 'dots']) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `spinner-${type}`,
        parentId: 'spinner-row',
        component: { type: 'Spinner', props: { type, size: 'large' } }
      })
    }

    isStreaming.value = false
    showToast('加载演示完成')
  },

  alert: async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '警告演示' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## ⚠️ 警告组件', size: 'large' } }
    })

    for (const alert of [
      { type: 'info', message: '这是一条提示信息' },
      { type: 'success', message: '操作成功完成' },
      { type: 'warning', message: '请注意检查输入' },
      { type: 'error', message: '发生错误，请重试' }
    ]) {
      await sendStreamingMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `alert-${alert.type}`,
        component: { type: 'Alert', props: alert }
      })
    }

    isStreaming.value = false
    showToast('警告演示完成')
  },

  // Chart demos
  'chart-line': async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '折线图' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📈 折线图', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'line-chart',
      component: {
        type: 'Chart',
        props: {
          height: '350px',
          option: {
            title: { text: '月度销售趋势', left: 'center' },
            tooltip: { trigger: 'axis' },
            xAxis: {
              type: 'category',
              data: ['1月', '2月', '3月', '4月', '5月', '6月']
            },
            yAxis: { type: 'value', name: '销售额(万)' },
            series: [{
              name: '销售额',
              type: 'line',
              smooth: true,
              data: [120, 132, 101, 134, 190, 230],
              itemStyle: { color: '#1989fa' },
              areaStyle: { color: 'rgba(25, 137, 250, 0.2)' }
            }]
          }
        }
      }
    })

    isStreaming.value = false
    showToast('折线图演示完成')
  },

  'chart-bar': async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '柱状图' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 📊 柱状图', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'bar-chart',
      component: {
        type: 'Chart',
        props: {
          height: '350px',
          option: {
            title: { text: '产品销量对比', left: 'center' },
            tooltip: { trigger: 'axis' },
            xAxis: {
              type: 'category',
              data: ['产品A', '产品B', '产品C', '产品D', '产品E']
            },
            yAxis: { type: 'value', name: '销量' },
            series: [{
              name: '销量',
              type: 'bar',
              data: [200, 150, 80, 70, 110],
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0, y: 0, x2: 0, y2: 1,
                  colorStops: [
                    { offset: 0, color: '#1989fa' },
                    { offset: 1, color: '#87ceeb' }
                  ]
                }
              }
            }]
          }
        }
      }
    })

    isStreaming.value = false
    showToast('柱状图演示完成')
  },

  'chart-pie': async () => {
    clearSurface(currentSurface.value)
    messageCount.value = 0

    await sendStreamingMessage({ type: 'surface', surfaceId: currentSurface.value, name: '饼图' })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-title',
      component: { type: 'Text', props: { content: '## 🥧 饼图', size: 'large' } }
    })

    await sendStreamingMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'pie-chart',
      component: {
        type: 'Chart',
        props: {
          height: '400px',
          option: {
            title: { text: '市场份额分布', left: 'center' },
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            legend: { orient: 'vertical', left: 'left', top: 'center' },
            series: [{
              name: '市场份额',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
              label: { show: false, position: 'center' },
              emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
              labelLine: { show: false },
              data: [
                { value: 1048, name: '搜索引擎', itemStyle: { color: '#1989fa' } },
                { value: 735, name: '直接访问', itemStyle: { color: '#07c160' } },
                { value: 580, name: '邮件营销', itemStyle: { color: '#ff976a' } },
                { value: 484, name: '联盟广告', itemStyle: { color: '#ffc107' } },
                { value: 300, name: '视频广告', itemStyle: { color: '#9c27b0' } }
              ]
            }]
          }
        }
      }
    })

    isStreaming.value = false
    showToast('饼图演示完成')
  },
}

// Run demo
const runDemo = async (demoId: string) => {
  currentDemo.value = demoId
  const demo = demos[demoId]
  if (demo) {
    await demo()
  }
}

// Event handler
const handleComponentEvent = (componentId: string, eventType: string, _payload?: unknown) => {
  logMessage(`事件: ${componentId} - ${eventType}`)
}

// Initialize
onMounted(() => {
  handleMessage(JSON.stringify({ type: 'surface', surfaceId: currentSurface.value, name: '组件演示' }) + '\n')
})
</script>

<style scoped>
.components-demo {
  max-width: 100%;
  margin: 0 auto;
}
</style>
