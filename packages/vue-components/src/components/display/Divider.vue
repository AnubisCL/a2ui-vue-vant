<template>
  <van-divider
    :dashed="false"
    :hairline="props.thickness === 1"
    :style="dividerStyle"
  >
    <slot />
  </van-divider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DividerProps } from '@a2ui/core'

const props = withDefaults(defineProps<DividerProps>(), {
  orientation: 'horizontal',
  thickness: 1,
  color: '#e0e0e0',
  margin: 16,
})

// 分隔线样式
const dividerStyle = computed(() => {
  const marginValue = Array.isArray(props.margin)
    ? `${props.margin[0]}px ${props.margin[1]}px ${props.margin[2]}px ${props.margin[3]}px`
    : `${props.margin}px 0`

  return {
    borderColor: props.color,
    margin: marginValue,
    borderWidth: props.thickness > 1 ? `${props.thickness}px` : undefined,
  }
})
</script>
