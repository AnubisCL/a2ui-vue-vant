<template>
  <div class="a2ui-progress">
    <div v-if="props.showLabel && props.label" class="mb-2 text-sm font-medium text-neutral-700">
      {{ props.label }}
    </div>

    <van-progress
      :percentage="percentage"
      :stroke-width="strokeWidth"
      :color="progressColor"
      track-color="#e5e5e5"
      :show-pivot="props.showPercentage"
      :pivot-text="props.showPercentage ? `${percentage}%` : undefined"
    />

    <div v-if="props.showInfo" class="mt-2 text-xs text-neutral-500 flex justify-between">
      <span>{{ props.value || 0 }} / {{ props.max }}</span>
      <span>{{ percentage }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface ProgressProps {
  value?: number
  max?: number
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger'
  size?: 'small' | 'medium' | 'large'
  label?: string
  showLabel?: boolean
  showPercentage?: boolean
  showInfo?: boolean
}

const props = withDefaults(defineProps<ProgressProps>(), {
  max: 100,
  variant: 'primary',
  size: 'medium',
  showLabel: false,
  showPercentage: false,
  showInfo: false,
})

// 计算百分比
const percentage = computed(() => {
  return Math.min(Math.max((props.value || 0) / props.max * 100, 0), 100)
})

// 进度条粗细
const strokeWidth = computed(() => {
  const sizeMap: Record<string, number> = {
    small: 4,
    medium: 8,
    large: 12,
  }
  return sizeMap[props.size] || 8
})

// 进度条颜色
const progressColor = computed(() => {
  const colorMap: Record<string, string> = {
    default: '#6b7280',
    primary: '#1989fa',
    success: '#07c160',
    warning: '#ff976a',
    danger: '#ee0a24',
  }
  return colorMap[props.variant] || '#1989fa'
})
</script>

<style scoped>
.a2ui-progress {
  width: 100%;
}
</style>
