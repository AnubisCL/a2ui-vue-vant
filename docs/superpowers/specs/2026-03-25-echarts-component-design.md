# ECharts 组件设计文档

## 概述

为 A2UI Vue SDK 添加 ECharts 图表组件，支持 AI Agent 通过声明式协议动态渲染和更新图表。

## 目标

- 支持完整的 ECharts option 配置
- 支持 A2UI 流式协议更新图表数据
- 提供直观的 props 接口
- 完善的文档和示例

## 组件设计

### Props 定义

```typescript
export interface ChartProps {
  /** ECharts 完整配置对象 */
  option: ValueReference<EChartsOption>
  /** 图表宽度，默认 '100%' */
  width?: string | number
  /** 图表高度，默认 '300px' */
  height?: string | number
  /** 是否自动 resize，默认 true */
  autoResize?: boolean
  /** 图表主题，默认 'light' */
  theme?: 'light' | 'dark' | string
  /** 渲染器类型，默认 'canvas' */
  renderer?: 'canvas' | 'svg'
}
```

### 组件分类

Chart 组件归类为 **display** 类别，因为它主要用于数据可视化展示。

### 事件支持

- `onReady`: 图表实例初始化完成
- `onClick`: 点击事件（通过 ECharts 事件系统）

## 技术方案

### 依赖

- `echarts`: ^5.4.0 (peer dependency)

### 实现要点

1. **DOM 引用**: 使用 `ref` 获取图表容器 DOM 元素
2. **实例管理**: 使用 `echarts.init()` 初始化，`onUnmounted` 时调用 `dispose()`
3. **响应式更新**: 使用 `watch` 监听 option 变化，调用 `setOption({ ...newOption, notMerge: false })`
4. **尺寸响应**: 使用 `ResizeObserver` 监听容器尺寸变化，调用 `resize()`
5. **主题支持**: 支持内置主题和自定义主题

## 文件结构

### 新增文件

```
packages/
├── core/src/types/components.ts        # 添加 ChartProps 类型
├── vue-components/
│   └── src/components/display/
│       ├── Chart.vue                   # 新增组件实现
│       └── index.ts                    # 导出 Chart
├── vue-plugin/src/plugin.ts            # 注册 Chart 组件
└── dev/src/examples/
    └── ChartExample.vue                # 新增示例页面

docs/
├── INTEGRATION.md                      # 更新文档
└── README.md                           # 更新文档
```

### 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `packages/core/src/types/components.ts` | 添加 ChartProps 接口和类型映射 |
| `packages/core/src/types/index.ts` | 导出 ChartProps |
| `packages/core/src/registry/component-registry.ts` | 添加 Chart 元数据 |
| `packages/vue-components/src/components/display/index.ts` | 导出 Chart 组件 |
| `packages/vue-plugin/src/plugin.ts` | 注册 Chart 组件 |
| `packages/dev/src/router/index.ts` | 添加 Chart 示例路由 |
| `docs/INTEGRATION.md` | 添加 Chart 组件文档 |
| `README.md` | 更新组件列表 |
| `README.zh-CN.md` | 更新组件列表（如存在） |

## 使用示例

### 基础使用

```json
{
  "type": "component",
  "componentId": "sales-chart",
  "surfaceId": "dashboard",
  "component": {
    "type": "Chart",
    "props": {
      "option": {
        "title": { "text": "销售趋势" },
        "xAxis": { "type": "category", "data": ["Mon", "Tue", "Wed", "Thu", "Fri"] },
        "yAxis": { "type": "value" },
        "series": [{ "type": "line", "data": [120, 200, 150, 80, 70] }]
      },
      "height": "300px"
    }
  }
}
```

### 流式更新

```json
// 初始创建
{"type":"component","surfaceId":"dashboard","componentId":"chart1","component":{"type":"Chart","props":{"option":{...}}}}

// 流式更新数据
{"type":"component","surfaceId":"dashboard","componentId":"chart1","component":{"type":"Chart","props":{"option":{"series":[{"data":[150,230,180]}]}}}}
```

## 文档更新

### INTEGRATION.md 新增章节

```markdown
### 图表组件

Chart 组件基于 ECharts 实现，支持完整的 option 配置。

#### 基础配置

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| option | object | - | ECharts 配置对象 |
| width | string\|number | '100%' | 图表宽度 |
| height | string\|number | '300px' | 图表高度 |
| autoResize | boolean | true | 自动调整尺寸 |
| theme | string | 'light' | 图表主题 |
| renderer | 'canvas'\|'svg' | 'canvas' | 渲染器类型 |

#### 使用示例

[示例代码...]

#### 流式更新

Chart 组件支持通过 A2UI 协议流式更新图表数据...
```

## 示例页面

### ChartExample.vue 内容

1. **折线图演示**: 展示基本折线图配置
2. **柱状图演示**: 展示柱状图配置
3. **饼图演示**: 展示饼图配置
4. **流式更新演示**: 模拟实时数据更新场景

## 测试要点

- [ ] 组件正常渲染
- [ ] option 变化时图表正确更新
- [ ] 容器尺寸变化时图表自动调整
- [ ] 组件销毁时资源正确释放
- [ ] 流式消息正确处理

## 依赖说明

用户需要自行安装 echarts：

```bash
pnpm install echarts
```

## 成功标准

1. Chart 组件可通过 A2UI 协议正常使用
2. 支持完整的 ECharts option 配置
3. 支持流式数据更新
4. 文档完整，包含使用示例
5. 示例页面可运行
