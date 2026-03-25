# 常用组件文档

本文档介绍 A2UI Vue SDK 中新增的常用组件。

## 📦 组件列表

### 1. Badge 徽章

用于显示数量、状态或标签的徽章组件。

**Props:**
- `content?: string | number` - 显示内容
- `variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger'` - 颜色变体
- `size?: 'small' | 'medium' | 'large'` - 尺寸
- `dot?: boolean` - 是否显示为圆点
- `count?: number` - 数量（会自动格式化，如 99+）
- `maxCount?: number` - 最大显示数量（默认 99）

**示例:**
```vue
<Badge content="新" />
<Badge :count="150" :maxCount="99" />
<Badge dot variant="primary" />
```

---

### 2. Avatar 头像

用于显示用户头像或头像占位符。

**Props:**
- `src?: string` - 图片 URL
- `alt?: string` - 替代文本
- `size?: number | 'small' | 'medium' | 'large'` - 尺寸
- `shape?: 'circle' | 'square'` - 形状
- `name?: string` - 名称（用于生成首字母头像）
- `color?: string` - 自定义背景颜色

**示例:**
```vue
<Avatar name="张三" />
<Avatar src="/avatar.jpg" alt="用户头像" />
<Avatar :size="60" name="大头像" shape="square" />
```

---

### 3. Progress 进度条

显示任务完成进度。

**Props:**
- `value?: number` - 当前进度值
- `max?: number` - 最大值（默认 100）
- `variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger'` - 颜色变体
- `size?: 'small' | 'medium' | 'large'` - 尺寸
- `label?: string` - 标签文本
- `showLabel?: boolean` - 是否显示标签
- `showPercentage?: boolean` - 是否显示百分比
- `showInfo?: boolean` - 是否显示进度信息
- `indeterminate?: boolean` - 是否为不确定进度（加载动画）

**示例:**
```vue
<Progress :value="75" label="下载中" showPercentage />
<Progress :value="60" variant="success" showInfo />
<Progress indeterminate label="加载中..." />
```

---

### 4. Spinner 加载器

旋转的加载指示器。

**Props:**
- `size?: number | 'small' | 'medium' | 'large'` - 尺寸
- `color?: string` - 颜色（默认 #0066cc）

**示例:**
```vue
<Spinner />
<Spinner size="large" color="#10b981" />
<Spinner :size="40" />
```

---

### 5. Tag 标签

用于标记、分类或展示元信息。

**Props:**
- `text?: string` - 标签文本
- `icon?: string` - 图标
- `variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'` - 颜色变体
- `size?: 'small' | 'medium' | 'large'` - 尺寸
- `closable?: boolean` - 是否可关闭

**事件:**
- `@close` - 关闭时触发

**示例:**
```vue
<Tag text="默认" />
<Tag text="主要" variant="primary" />
<Tag text="可关闭" variant="success" closable @close="handleClose" />
```

---

### 6. Alert 警告框

用于显示重要信息、警告或错误。

**Props:**
- `title?: string` - 标题
- `message?: string` - 消息内容
- `variant?: 'info' | 'success' | 'warning' | 'error'` - 类型
- `showIcon?: boolean` - 是否显示图标（默认 true）
- `closable?: boolean` - 是否可关闭

**事件:**
- `@close` - 关闭时触发

**示例:**
```vue
<Alert
  title="操作成功"
  message="您的操作已成功完成"
  variant="success"
/>
<Alert
  title="错误"
  message="操作失败，请重试"
  variant="error"
  :closable="true"
/>
```

---

### 7. Switch 开关

切换开关控件。

**Props:**
- `modelValue?: boolean` - 绑定值
- `disabled?: boolean` - 是否禁用
- `size?: 'small' | 'medium' | 'large'` - 尺寸
- `color?: 'primary' | 'success' | 'warning' | 'danger'` - 颜色

**事件:**
- `@update:modelValue` - 值变化时触发
- `@change` - 值变化时触发

**示例:**
```vue
<Switch v-model="enabled" />
<Switch v-model="notifications" color="success" />
<Switch :modelValue="true" :disabled="true" />
```

---

### 8. Tooltip 提示框

鼠标悬停时显示的提示信息。

**Props:**
- `content: string` - 提示内容（必需）
- `placement?: 'top' | 'bottom' | 'left' | 'right'` - 显示位置
- `delay?: number` - 延迟显示时间（毫秒，默认 200）

**示例:**
```vue
<Tooltip content="这是提示信息">
  <Button>悬停查看</Button>
</Tooltip>
<Tooltip content="下方提示" placement="bottom">
  <span>悬停我</span>
</Tooltip>
```

---

### 9. Skeleton 骨架屏

内容加载占位符。

**Props:**
- `avatar?: boolean` - 是否显示头像占位
- `avatarSize?: number | 'small' | 'medium' | 'large'` - 头像尺寸
- `title?: boolean` - 是否显示标题占位
- `titleWidth?: number | string` - 标题宽度
- `paragraph?: boolean` - 是否显示段落占位
- `paragraphRows?: number` - 段落行数（默认 3）
- `paragraphWidth?: number | string | Array<number | string>` - 段落宽度
- `loading?: boolean` - 是否显示（默认 true）

**示例:**
```vue
<Skeleton />
<Skeleton :avatar="true" />
<Skeleton
  :avatar="true"
  :title="true"
  :paragraphRows="4"
  :paragraphWidth="[100, 80, 60, 40]"
/>
```

---

### 10. Empty 空状态

空状态展示。

**Props:**
- `image?: string` - 自定义图片
- `imageType?: 'default' | 'search' | 'network' | 'error'` - 预设图标类型
- `title?: string` - 标题（默认 "暂无数据"）
- `description?: string` - 描述（默认 "这里什么都没有"）

**插槽:**
- `image` - 自定义图片
- `default` - 操作按钮区域

**示例:**
```vue
<Empty />
<Empty
  imageType="search"
  title="没有搜索结果"
  description="尝试使用其他关键词搜索"
>
  <Button label="重新搜索" />
</Empty>
```

---

## 🎨 设计规范

### 颜色变体

所有组件都遵循统一的设计系统：

| 变体 | 颜色 | 用途 |
|------|------|------|
| default | 灰色 | 默认状态 |
| primary | 蓝色 (#0066cc) | 主要操作 |
| success | 绿色 (#10b981) | 成功状态 |
| warning | 橙色 (#f59e0b) | 警告状态 |
| danger | 红色 (#dc3545) | 危险/错误 |
| info | 浅蓝 (#3b82f6) | 信息提示 |

### 尺寸规范

| 尺寸 | 高度 | 字体大小 |
|------|------|----------|
| small | 24px | 10-12px |
| medium | 40px | 14px |
| large | 56px | 16-18px |

---

## 📝 使用指南

### 安装

```bash
pnpm add @a2ui/vue-components
```

### 导入组件

```typescript
import {
  Badge,
  Avatar,
  Progress,
  Spinner,
  Tag,
  Alert,
  Switch,
  Tooltip,
  Skeleton,
  Empty,
} from '@a2ui/vue-components'
```

### 全局注册

```typescript
import { createA2UI } from '@a2ui/vue-plugin'

app.use(createA2UI())
```

### 使用样式

```typescript
import '@a2ui/vue-components/styles'
```

---

## 🎯 最佳实践

1. **Badge** - 不要在徽章中放太多文字，保持在 4 个字符以内
2. **Avatar** - 使用真实头像图片时，建议提供 name 作为回退
3. **Progress** - 长时间任务使用 `indeterminate` 模式
4. **Spinner** - 仅用于加载状态，不要用于装饰
5. **Tag** - 使用合适的颜色变体传达正确的语义
6. **Alert** - 重要的、需要用户注意的信息使用 Alert
7. **Switch** - 用于状态切换，不要用于提交操作
8. **Tooltip** - 提供补充信息，不要重复已有内容
9. **Skeleton** - 尽量与实际内容结构相似
10. **Empty** - 提供清晰的操作指引

---

## 🔗 相关链接

- [A2UI 官方文档](https://github.com/google/agent-to-ui)
- [UnoCSS 文档](https://unocss.dev/)
- [Vue 3 文档](https://vuejs.org/)
