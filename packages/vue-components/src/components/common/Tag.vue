<template>
  <van-tag
    :type="vantType"
    :size="vantSize"
    :plain="props.plain"
    :round="props.round"
    :closeable="props.closable"
    @close="handleClose"
  >
    <span v-if="props.icon" class="mr-1">{{ props.icon }}</span>
    <slot>{{ props.text }}</slot>
  </van-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface TagProps {
  text?: string
  icon?: string
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'info'
  size?: 'small' | 'medium' | 'large'
  plain?: boolean
  round?: boolean
  closable?: boolean
}

const props = withDefaults(defineProps<TagProps>(), {
  variant: 'default',
  size: 'medium',
  plain: false,
  round: false,
  closable: false,
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

// 映射类型
const vantType = computed(() => {
  const typeMap: Record<string, string> = {
    default: 'default',
    primary: 'primary',
    success: 'success',
    warning: 'warning',
    danger: 'danger',
    info: 'primary', // Vant 没有 info 类型，映射到 primary
  }
  return typeMap[props.variant] || 'default'
})

// 映射尺寸
const vantSize = computed(() => {
  const sizeMap: Record<string, string> = {
    small: 'small',
    medium: 'medium',
    large: 'large',
  }
  return sizeMap[props.size] || 'medium'
})

// 关闭事件
const handleClose = () => {
  emit('close')
}
</script>
