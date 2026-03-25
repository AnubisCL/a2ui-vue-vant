<template>
  <component :is="tag" class="a2ui-text leading-1.5 break-words" :class="classes" v-html="renderedContent" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import type { TextProps } from '@a2ui/core'

const props = withDefaults(defineProps<TextProps>(), {
  size: 'medium',
  weight: 'normal',
  markdown: true,
  maxLines: undefined,
  align: 'left',
  margin: 0,
})

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
})

const tag = computed(() => {
  const sizeTagMap: Record<NonNullable<TextProps['size']>, 'span' | 'p' | 'h3' | 'h2'> = {
    small: 'span',
    medium: 'p',
    large: 'h3',
    xlarge: 'h2',
  }
  return sizeTagMap[props.size]
})

const classes = computed(() => [
  // Size classes
  {
    'text-xs': props.size === 'small',
    'text-sm': props.size === 'medium',
    'text-xl': props.size === 'large',
    'text-2xl': props.size === 'xlarge',
  },
  // Weight classes
  {
    'font-normal': props.weight === 'normal',
    'font-medium': props.weight === 'medium',
    'font-semibold': props.weight === 'semibold',
    'font-bold': props.weight === 'bold',
  },
  // Align classes
  {
    'text-left': props.align === 'left',
    'text-center': props.align === 'center',
    'text-right': props.align === 'right',
    'text-justify': props.align === 'justify',
  },
  // Margin
  marginClass.value,
  // Max lines (requires inline styles for webkit-line-clamp)
  maxLinesStyle.value,
])

const marginClass = computed(() => {
  if (Array.isArray(props.margin)) {
    const [top, right, bottom, left] = props.margin
    return `m-[${top}px_${right}px_${bottom}px_${left}px]`
  }
  return `m-[${props.margin}px]`
})

const maxLinesStyle = computed(() => {
  if (!props.maxLines) return {}
  return {
    display: '-webkit-box',
    WebkitLineClamp: props.maxLines,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
  }
})

const renderedContent = computed(() => {
  const content = typeof props.content === 'object' && 'path' in props.content
    ? '' // Will be resolved via data binding
    : props.content

  if (props.markdown && typeof content === 'string') {
    return md.render(content)
  }

  return content
})
</script>

<style scoped>
.a2ui-text {
  color: v-bind('props.color || "inherit"');
}
</style>
