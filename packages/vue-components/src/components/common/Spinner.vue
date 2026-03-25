<template>
  <van-loading
    :type="loadingType"
    :size="vantSize"
    :color="props.color"
    :vertical="false"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface SpinnerProps {
  size?: number | 'small' | 'medium' | 'large' | 'xlarge'
  color?: string
  type?: 'circular' | 'spinner'
}

const props = withDefaults(defineProps<SpinnerProps>(), {
  size: 'medium',
  color: '#1989fa',
  type: 'circular',
})

// 加载类型
const loadingType = computed(() => {
  return props.type === 'spinner' ? 'spinner' : 'circular'
})

// 映射尺寸
const vantSize = computed(() => {
  if (typeof props.size === 'number') {
    return `${props.size}px`
  }

  const sizeMap: Record<string, string> = {
    small: '16',
    medium: '24',
    large: '32',
    xlarge: '48',
  }

  return sizeMap[props.size] || '24'
})
</script>
