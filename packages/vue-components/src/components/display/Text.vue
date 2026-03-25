<template>
  <component
    :is="tag"
    class="a2ui-text"
    :class="classes"
    :style="textStyle"
    v-html="renderedContent"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import type { TextProps } from '@a2ui/core'
import { resolveStringValue } from '../../utils'

const props = withDefaults(defineProps<TextProps>(), {
  size: 'medium',
  weight: 'normal',
  markdown: true,
  maxLines: undefined,
  align: 'left',
  margin: 0,
})

// 初始化 Markdown 解析器
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
})

// 根据大小决定 HTML 标签
const tag = computed(() => {
  const sizeTagMap: Record<string, string> = {
    small: 'span',
    medium: 'p',
    large: 'h3',
    xlarge: 'h2',
  }
  return sizeTagMap[props.size || 'medium'] || 'p'
})

// CSS 类
const classes = computed(() => [
  // 尺寸
  {
    'text-xs': props.size === 'small',
    'text-sm': props.size === 'medium',
    'text-xl': props.size === 'large',
    'text-2xl': props.size === 'xlarge',
  },
  // 字重
  {
    'font-normal': props.weight === 'normal',
    'font-medium': props.weight === 'medium',
    'font-semibold': props.weight === 'semibold',
    'font-bold': props.weight === 'bold',
  },
  // 对齐
  {
    'text-left': props.align === 'left',
    'text-center': props.align === 'center',
    'text-right': props.align === 'right',
    'text-justify': props.align === 'justify',
  },
  // 行高
  'leading-relaxed',
  // 换行
  'break-words',
])

// 内联样式
const textStyle = computed(() => {
  const style: Record<string, string | number | undefined> = {}

  // 颜色
  if (props.color) {
    style.color = props.color
  }

  // 边距
  if (props.margin) {
    if (Array.isArray(props.margin)) {
      const [top, right, bottom, left] = props.margin
      style.margin = `${top}px ${right}px ${bottom}px ${left}px`
    } else {
      style.margin = `${props.margin}px`
    }
  }

  // 最大行数
  if (props.maxLines) {
    style.display = '-webkit-box'
    style.webkitLineClamp = props.maxLines
    style.webkitBoxOrient = 'vertical'
    style.overflow = 'hidden'
  }

  return style
})

// 渲染内容
const renderedContent = computed(() => {
  const content = resolveStringValue(props.content, '')

  if (props.markdown && typeof content === 'string') {
    return md.render(content)
  }

  return content
})
</script>

<style scoped>
.a2ui-text {
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.a2ui-text :deep(p) {
  margin: 0;
}

.a2ui-text :deep(h1),
.a2ui-text :deep(h2),
.a2ui-text :deep(h3),
.a2ui-text :deep(h4) {
  margin: 0;
  font-weight: 600;
}

.a2ui-text :deep(a) {
  color: var(--van-primary-color, #1989fa);
  text-decoration: none;
}

.a2ui-text :deep(a:hover) {
  text-decoration: underline;
}

.a2ui-text :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.a2ui-text :deep(pre) {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.a2ui-text :deep(ul),
.a2ui-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.a2ui-text :deep(blockquote) {
  border-left: 4px solid #e0e0e0;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
}
</style>
