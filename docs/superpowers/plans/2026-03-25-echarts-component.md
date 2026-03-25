# ECharts 组件实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 A2UI Vue SDK 添加 ECharts 图表组件，支持 AI Agent 通过声明式协议动态渲染和更新图表。

**Architecture:** Chart 组件封装 ECharts 库，接收完整的 option 配置对象，支持响应式数据更新和自动尺寸调整。组件通过 A2UI 协议的流式消息机制实现数据更新。

**Tech Stack:** Vue 3 Composition API, ECharts 5.x, TypeScript

---

## 文件结构

### 新增文件
- `packages/vue-components/src/components/display/Chart.vue` - Chart 组件实现
- `packages/dev/src/examples/ChartExample.vue` - Chart 示例页面

### 修改文件
- `packages/core/src/types/components.ts` - 添加 ChartProps 类型
- `packages/core/src/registry/component-registry.ts` - 添加 Chart 元数据
- `packages/vue-components/src/components/display/index.ts` - 导出 Chart 组件
- `packages/vue-plugin/src/plugin.ts` - 注册 Chart 组件
- `packages/dev/src/router/index.ts` - 添加示例路由
- `docs/INTEGRATION.md` - 添加组件文档
- `README.md` - 更新组件列表

---

## Task 1: 添加 ChartProps 类型定义

**Files:**
- Modify: `packages/core/src/types/components.ts`

- [ ] **Step 1: 添加 ECharts 类型导入和 ChartProps 接口**

在 `packages/core/src/types/components.ts` 文件末尾的 `ComponentPropsMap` 接口之前添加：

```typescript
/**
 * ========================================
 * CHART COMPONENT
 * ========================================
 */

/**
 * ECharts option type (loose typing for flexibility)
 */
export type EChartsOption = Record<string, unknown>

/**
 * Chart component - ECharts wrapper
 */
export interface ChartProps {
  /** ECharts complete option configuration */
  option: ValueReference<EChartsOption>
  /** Chart width, default '100%' */
  width?: string | number
  /** Chart height, default '300px' */
  height?: string | number
  /** Auto resize on container size change, default true */
  autoResize?: boolean
  /** Chart theme */
  theme?: 'light' | 'dark' | string
  /** Renderer type */
  renderer?: 'canvas' | 'svg'
}
```

- [ ] **Step 2: 更新 ComponentPropsMap 添加 Chart 映射**

在 `ComponentPropsMap` 接口中添加 Chart 类型映射：

```typescript
export interface ComponentPropsMap {
  // ... existing types ...

  // Chart
  Chart: ChartProps
}
```

- [ ] **Step 3: 验证类型编译**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/core typecheck`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add packages/core/src/types/components.ts
git commit -m "feat(core): 添加 ChartProps 类型定义

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 添加 Chart 组件元数据

**Files:**
- Modify: `packages/core/src/registry/component-registry.ts`

- [ ] **Step 1: 在 BASIC_COMPONENT_METADATA 中添加 Chart 元数据**

在 `BASIC_COMPONENT_METADATA` 对象的 Modal 之后添加：

```typescript
  Modal: {
    type: 'Modal',
    displayName: 'Modal',
    category: 'container',
    description: 'Modal dialog',
    supportsChildren: true,
    supportsTemplates: false,
  },
  // Chart
  Chart: {
    type: 'Chart',
    displayName: 'Chart',
    category: 'display',
    description: 'ECharts-based data visualization component',
    supportsChildren: false,
    supportsTemplates: false,
  },
```

- [ ] **Step 2: 验证编译**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/core typecheck`
Expected: No errors

- [ ] **Step 3: Commit**

```bash
git add packages/core/src/registry/component-registry.ts
git commit -m "feat(core): 添加 Chart 组件元数据

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 实现 Chart.vue 组件

**Files:**
- Create: `packages/vue-components/src/components/display/Chart.vue`

- [ ] **Step 1: 创建 Chart.vue 组件文件**

```vue
<template>
  <div
    ref="chartRef"
    class="a2ui-chart"
    :style="chartStyle"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts'
import type { ChartProps } from '@a2ui/core'
import { resolveObjectValue } from '../../utils'

const props = withDefaults(defineProps<ChartProps>(), {
  width: '100%',
  height: '300px',
  autoResize: true,
  theme: 'light',
  renderer: 'canvas',
})

const emit = defineEmits<{
  (e: 'ready', chart: echarts.ECharts): void
  (e: 'click', event: unknown): void
}>()

// DOM reference
const chartRef = ref<HTMLDivElement | null>(null)

// ECharts instance (shallow ref to avoid deep reactivity)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

// Resize observer
let resizeObserver: ResizeObserver | null = null

// Chart style
const chartStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
}))

// Initialize chart
const initChart = () => {
  if (!chartRef.value) return

  // Dispose existing instance
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  // Create new instance
  const renderer = props.renderer === 'svg' ? 'svg' : 'canvas'
  chartInstance.value = echarts.init(chartRef.value, props.theme, {
    renderer,
  })

  // Set initial option
  const option = resolveObjectValue(props.option, {})
  if (Object.keys(option).length > 0) {
    chartInstance.value.setOption(option, { notMerge: true })
  }

  // Emit ready event
  emit('ready', chartInstance.value)

  // Setup click event
  chartInstance.value.on('click', (event) => {
    emit('click', event)
  })
}

// Update chart option
const updateChart = () => {
  if (!chartInstance.value) return

  const option = resolveObjectValue(props.option, {})
  if (Object.keys(option).length > 0) {
    chartInstance.value.setOption(option, { notMerge: false })
  }
}

// Handle resize
const handleResize = () => {
  chartInstance.value?.resize()
}

// Watch option changes
watch(
  () => props.option,
  () => {
    updateChart()
  },
  { deep: true }
)

// Watch theme/renderer changes
watch(
  () => [props.theme, props.renderer],
  () => {
    initChart()
  }
)

// Lifecycle
onMounted(() => {
  initChart()

  // Setup resize observer
  if (props.autoResize && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.observe(chartRef.value)
  }
})

onUnmounted(() => {
  // Cleanup resize observer
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  // Dispose chart instance
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})

// Expose methods
defineExpose({
  getChartInstance: () => chartInstance.value,
  resize: handleResize,
})
</script>

<style scoped>
.a2ui-chart {
  min-height: 200px;
}
</style>
```

- [ ] **Step 2: 在 resolveObjectValue 工具函数中确保支持对象解析**

检查 `packages/vue-components/src/utils/value-reference.ts`，确保有 `resolveObjectValue` 函数。如果没有，添加：

```typescript
/**
 * Resolve object value reference
 */
export function resolveObjectValue<T extends Record<string, unknown>>(
  value: T | { path: string } | undefined,
  defaultValue: T
): T {
  if (!value) return defaultValue
  if ('path' in value && typeof value.path === 'string') {
    // For now, return default - actual resolution would need data store context
    return defaultValue
  }
  return value as T
}
```

- [ ] **Step 3: 验证组件编译**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/vue-components typecheck`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add packages/vue-components/src/components/display/Chart.vue
git add packages/vue-components/src/utils/value-reference.ts
git commit -m "feat(components): 实现 Chart 组件

- 基于 ECharts 的数据可视化组件
- 支持完整 option 配置
- 支持自动 resize
- 支持主题和渲染器切换
- 支持流式数据更新

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 导出 Chart 组件

**Files:**
- Modify: `packages/vue-components/src/components/display/index.ts`

- [ ] **Step 1: 在 display/index.ts 中添加 Chart 导出**

```typescript
/**
 * Display Components
 */

export { default as Text } from './Text.vue'
export { default as Image } from './Image.vue'
export { default as Icon } from './Icon.vue'
export { default as Video } from './Video.vue'
export { default as AudioPlayer } from './AudioPlayer.vue'
export { default as Divider } from './Divider.vue'
export { default as Chart } from './Chart.vue'
```

- [ ] **Step 2: 验证导出**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/vue-components typecheck`
Expected: No errors

- [ ] **Step 3: Commit**

```bash
git add packages/vue-components/src/components/display/index.ts
git commit -m "feat(components): 导出 Chart 组件

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 在插件中注册 Chart 组件

**Files:**
- Modify: `packages/vue-plugin/src/plugin.ts`

- [ ] **Step 1: 在默认组件列表中添加 Chart**

在 `defaultComponents` 对象中添加 Chart：

```typescript
const defaultComponents: Record<string, any> = {
  // Layout
  Row: Components.Row,
  Column: Components.Column,
  List: Components.List,

  // Display
  Text: Components.Text,
  Image: Components.Image,
  Icon: Components.Icon,
  Video: Components.Video,
  AudioPlayer: Components.AudioPlayer,
  Divider: Components.Divider,
  Chart: Components.Chart,  // 新增

  // ... rest of components
}
```

- [ ] **Step 2: 在全局组件注册中添加 Chart**

在 `app.component` 注册部分添加：

```typescript
app.component('A2uiChart', Components.Chart)
```

- [ ] **Step 3: 验证编译**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/vue-plugin typecheck`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add packages/vue-plugin/src/plugin.ts
git commit -m "feat(plugin): 注册 Chart 组件到插件

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 创建 Chart 示例页面

**Files:**
- Create: `packages/dev/src/examples/ChartExample.vue`

- [ ] **Step 1: 创建 ChartExample.vue 文件**

```vue
<template>
  <div class="chart-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">📊 ECharts 图表演示</h1>
      <p class="text-sm text-gray-500">通过 A2UI 协议动态渲染图表</p>
    </div>

    <!-- Controls -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex flex-wrap gap-2 mb-4">
        <van-button type="primary" size="small" @click="startLineChartDemo">
          📈 折线图
        </van-button>
        <van-button type="success" size="small" @click="startBarChartDemo">
          📊 柱状图
        </van-button>
        <van-button type="warning" size="small" @click="startPieChartDemo">
          🥧 饼图
        </van-button>
        <van-button type="default" size="small" @click="startStreamingDemo">
          🌊 流式更新
        </van-button>
      </div>
    </div>

    <!-- A2UI Renderer -->
    <div class="bg-white rounded-xl shadow-sm p-4 min-h-[400px]">
      <A2uiRenderer :surface-id="currentSurface" @event="handleComponentEvent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const currentSurface = ref('chart-demo')
const { handleMessage, clearSurface } = useA2UI()

const sendMessage = (message: object) => {
  handleMessage(JSON.stringify(message) + '\n')
}

// 折线图演示
const startLineChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '折线图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 📈 销售趋势折线图', size: 'large' }
    }
  })

  sendMessage({
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

  showToast('折线图演示已加载')
}

// 柱状图演示
const startBarChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '柱状图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 📊 产品销量柱状图', size: 'large' }
    }
  })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'bar-chart',
    component: {
      type: 'Chart',
      props: {
        height: '350px',
        option: {
          title: { text: '各产品销量对比', left: 'center' },
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

  showToast('柱状图演示已加载')
}

// 饼图演示
const startPieChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '饼图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 🥧 市场份额饼图', size: 'large' }
    }
  })

  sendMessage({
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

  showToast('饼图演示已加载')
}

// 流式更新演示
const startStreamingDemo = async () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '流式更新演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 🌊 实时数据流式更新', size: 'large' }
    }
  })

  // 初始图表
  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'stream-chart',
    component: {
      type: 'Chart',
      props: {
        height: '350px',
        option: {
          title: { text: '实时数据监控', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: [] },
          yAxis: { type: 'value' },
          series: [{ type: 'line', smooth: true, data: [] }]
        }
      }
    }
  })

  // 模拟流式数据更新
  const categories: string[] = []
  const data: number[] = []

  for (let i = 1; i <= 10; i++) {
    await new Promise(r => setTimeout(r, 500))
    categories.push(`${i}秒`)
    data.push(Math.floor(Math.random() * 100) + 50)

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'stream-chart',
      component: {
        type: 'Chart',
        props: {
          height: '350px',
          option: {
            xAxis: { type: 'category', data: [...categories] },
            series: [{ type: 'line', smooth: true, data: [...data], itemStyle: { color: '#1989fa' } }]
          }
        }
      }
    })
  }

  showToast('流式更新演示完成')
}

// 事件处理
const handleComponentEvent = (componentId: string, eventType: string, payload?: unknown) => {
  console.log('Event:', componentId, eventType, payload)
}

// 初始化
onMounted(() => {
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图表演示' })
  startLineChartDemo()
})
</script>

<style scoped>
.chart-demo {
  max-width: 100%;
  margin: 0 auto;
}
</style>
```

- [ ] **Step 2: 添加路由**

修改 `packages/dev/src/router/index.ts`：

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import StreamingDemo from '../examples/StreamingDemo.vue'
import ChartExample from '../examples/ChartExample.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'streaming',
    component: StreamingDemo,
  },
  {
    path: '/chart',
    name: 'chart',
    component: ChartExample,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
```

- [ ] **Step 3: 安装 echarts 依赖到 dev 包**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/dev add echarts`

- [ ] **Step 4: 验证示例编译**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm --filter @a2ui/dev typecheck`
Expected: No errors

- [ ] **Step 5: Commit**

```bash
git add packages/dev/src/examples/ChartExample.vue
git add packages/dev/src/router/index.ts
git add packages/dev/package.json
git add pnpm-lock.yaml
git commit -m "feat(dev): 添加 Chart 组件示例页面

- 折线图、柱状图、饼图演示
- 流式数据更新演示

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 更新 INTEGRATION.md 文档

**Files:**
- Modify: `docs/INTEGRATION.md`

- [ ] **Step 1: 在组件清单前添加图表组件文档**

在 `## 组件清单` 部分之前插入新章节：

```markdown
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

```

- [ ] **Step 2: 在组件清单表格中添加 Chart 组件**

在 `## 组件清单` 的 **展示** 类别中添加：

```markdown
| | Chart | 图表（ECharts）|
```

- [ ] **Step 3: Commit**

```bash
git add docs/INTEGRATION.md
git commit -m "docs: 添加 Chart 组件文档

- 基础配置说明
- 折线图、饼图示例
- 流式更新说明
- 依赖说明

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 更新 README.md

**Files:**
- Modify: `README.md`

- [ ] **Step 1: 更新组件数量**

将 `🎨 **28+ Components**` 改为 `🎨 **29+ Components**`

- [ ] **Step 2: 在 Display Components 表格中添加 Chart**

在 Display Components 表格中添加 Chart 行：

```markdown
| Chart | - | ECharts-based data visualization |
```

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: 更新 README 添加 Chart 组件

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 验证和测试

**Files:**
- All modified files

- [ ] **Step 1: 运行完整类型检查**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm typecheck`
Expected: No errors

- [ ] **Step 2: 启动开发服务器验证**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm dev`

打开浏览器访问:
- `http://localhost:5173/` - 流式演示
- `http://localhost:5173/chart` - Chart 示例

Expected: 图表正常渲染，交互正常

- [ ] **Step 3: 构建验证**

Run: `cd /Users/anubis/404net/a2ui-vue && pnpm build`
Expected: 构建成功，无错误

- [ ] **Step 4: 最终 Commit（如有未提交更改）**

```bash
git add .
git commit -m "chore: 验证完成，确保所有更改已提交

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## 完成标准

- [ ] Chart 组件可通过 A2UI 协议正常使用
- [ ] 支持完整的 ECharts option 配置
- [ ] 支持流式数据更新
- [ ] 文档完整，包含使用示例
- [ ] 示例页面可正常运行
- [ ] 类型检查通过
- [ ] 构建成功
