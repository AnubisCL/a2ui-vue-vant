<template>
  <span class="a2ui-icon inline-flex items-center justify-center user-select-none" :class="classes" :style="styles" @click="handleClick">
    <slot>{{ iconDisplay }}</slot>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { IconProps } from '@a2ui/core'

const props = withDefaults(defineProps<IconProps>(), {
  size: 'medium',
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const classes = computed(() => [
  // Size classes
  {
    'text-base': props.size === 'small',
    'text-xl': props.size === 'medium',
    'text-2xl': props.size === 'large',
    'text-3xl': props.size === 'xlarge',
  },
  {
    'text-base': typeof props.size === 'number',
  },
  // Cursor
  { 'cursor-pointer': props.onClick },
])

const styles = computed(() => {
  const style: Record<string, string> = {}

  if (props.color) {
    style.color = props.color
  }

  if (props.onClick) {
    style.cursor = 'pointer'
  }

  if (typeof props.size === 'number') {
    style.fontSize = `${props.size}px`
  }

  return style
})

const iconDisplay = computed(() => {
  const name = typeof props.name === 'object' && 'path' in props.name ? '' : props.name

  // Map common icon names to emoji/material icons
  const iconMap: Record<string, string> = {
    'check': '✓',
    'close': '✕',
    'arrow-right': '→',
    'arrow-left': '←',
    'arrow-up': '↑',
    'arrow-down': '↓',
    'search': '🔍',
    'settings': '⚙',
    'home': '🏠',
    'user': '👤',
    'star': '⭐',
    'heart': '♥',
    'trash': '🗑',
    'edit': '✎',
    'plus': '+',
    'minus': '−',
    'info': 'ℹ',
    'warning': '⚠',
    'error': '✖',
    'success': '✓',
  }

  return iconMap[name] || name
})

const handleClick = (event: MouseEvent) => {
  if (props.onClick) {
    emit('click', event)
  }
}
</script>
