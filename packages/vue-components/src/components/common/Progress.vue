<template>
  <div class="a2ui-progress w-full" :class="classes">
    <div v-if="showLabel && label" class="a2ui-progress__label mb-2.5 text-sm font-medium text-gray-700">
      {{ label }}
    </div>
    <div class="a2ui-progress__track bg-gray-100 overflow-hidden rounded-full shadow-inner">
      <div
        class="a2ui-progress__bar relative overflow-hidden rounded-full transition-all duration-500 ease-out flex items-center justify-end pr-2"
        :class="barClasses"
        :style="barStyle"
      >
        <div class="a2ui-progress__shine absolute inset-0 -translate-x-full animate-shine" />
        <span
          v-if="showPercentage && !isIndeterminate && size === 'large'"
          class="a2ui-progress__text relative z-10 text-xs font-bold text-white"
        >
          {{ percentage }}%
        </span>
      </div>
    </div>
    <div v-if="showInfo && !isIndeterminate" class="a2ui-progress__info mt-2 text-xs font-medium text-gray-500 flex justify-between">
      <span>{{ valueText }}</span>
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
  indeterminate?: boolean
}

const props = withDefaults(defineProps<ProgressProps>(), {
  max: 100,
  variant: 'primary',
  size: 'medium',
  showLabel: false,
  showPercentage: false,
  showInfo: false,
  indeterminate: false,
})

const isIndeterminate = computed(() => props.indeterminate)

const percentage = computed(() => {
  if (isIndeterminate.value) return 0
  return Math.min(Math.max((props.value || 0) / props.max * 100, 0), 100)
})

const valueText = computed(() => {
  return `${props.value || 0} / ${props.max}`
})

const classes = computed(() => [
  `a2ui-progress--${props.size}`,
])

const barClasses = computed(() => [
  `a2ui-progress__bar--${props.variant}`,
  {
    'a2ui-progress__bar--indeterminate': isIndeterminate.value,
  },
])

const barStyle = computed(() => {
  if (isIndeterminate.value) {
    return {}
  }
  return {
    width: `${percentage.value}%`,
  }
})
</script>

<style scoped>
.a2ui-progress__track {
  height: 8px;
}

.a2ui-progress--small .a2ui-progress__track {
  height: 4px;
}

.a2ui-progress--large .a2ui-progress__track {
  height: 12px;
}

.a2ui-progress__bar {
  min-width: 4px;
  position: relative;
}

.a2ui-progress__bar--default {
  background: linear-gradient(90deg, #6b7280 0%, #4b5563 100%);
}

.a2ui-progress__bar--primary {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.5);
}

.a2ui-progress__bar--success {
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
}

.a2ui-progress__bar--warning {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 0 10px rgba(245, 158, 11, 0.5);
}

.a2ui-progress__bar--danger {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 0 10px rgba(239, 68, 68, 0.5);
}

.a2ui-progress__bar--indeterminate {
  width: 50% !important;
  animation: indeterminate-slide 1.5s ease-in-out infinite;
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 50%, #3b82f6 100%);
  background-size: 200% 100%;
}

@keyframes indeterminate-slide {
  0% {
    transform: translateX(-100%);
  }
  50% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

@keyframes shine {
  100% {
    transform: translateX(100%);
  }
}

.a2ui-progress__shine {
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
}

.a2ui-progress__bar:hover .a2ui-progress__shine {
  animation: shine 0.75s;
}
</style>
