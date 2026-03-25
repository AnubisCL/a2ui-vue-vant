<template>
  <span class="a2ui-badge inline-flex items-center justify-center font-medium transition-all duration-200" :class="classes">
    <slot>{{ displayContent }}</slot>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface BadgeProps {
  content?: string | number
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'small' | 'medium' | 'large'
  dot?: boolean
  count?: number
  maxCount?: number
}

const props = withDefaults(defineProps<BadgeProps>(), {
  variant: 'default',
  size: 'medium',
  maxCount: 99,
})

const classes = computed(() => [
  // Variant
  {
    'a2ui-badge--default': props.variant === 'default',
    'a2ui-badge--primary': props.variant === 'primary',
    'a2ui-badge--success': props.variant === 'success',
    'a2ui-badge--warning': props.variant === 'warning',
    'a2ui-badge--danger': props.variant === 'danger',
    'a2ui-badge--info': props.variant === 'info',
  },
  // Size
  {
    'a2ui-badge--small': props.size === 'small',
    'a2ui-badge--medium': props.size === 'medium',
    'a2ui-badge--large': props.size === 'large',
  },
  // Dot style
  {
    'a2ui-badge--dot': props.dot,
  },
])

const displayContent = computed(() => {
  if (props.dot) return ''
  if (props.count !== undefined) {
    return props.count > props.maxCount ? `${props.maxCount}+` : props.count
  }
  return props.content
})
</script>

<style scoped>
.a2ui-badge {
  padding: 4px 10px;
  font-size: 12px;
  line-height: 1;
  border-radius: 6px;
  letter-spacing: 0.025em;
}

.a2ui-badge--small {
  padding: 2px 7px;
  font-size: 11px;
}

.a2ui-badge--large {
  padding: 6px 14px;
  font-size: 13px;
}

/* Default - Neutral gray with subtle border */
.a2ui-badge--default {
  background-color: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
}

/* Primary - Modern blue */
.a2ui-badge--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.3);
}

/* Success - Fresh green */
.a2ui-badge--success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(16, 185, 129, 0.3);
}

/* Warning - Warm orange */
.a2ui-badge--warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(245, 158, 11, 0.3);
}

/* Danger - Modern red */
.a2ui-badge--danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(239, 68, 68, 0.3);
}

/* Info - Sky blue */
.a2ui-badge--info {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(14, 165, 233, 0.3);
}

/* Dot style */
.a2ui-badge--dot {
  width: 8px;
  height: 8px;
  padding: 0;
  border-radius: 50%;
  box-shadow: 0 0 0 2px white, 0 0 0 4px currentColor;
}

.a2ui-badge--dot.a2ui-badge--small {
  width: 6px;
  height: 6px;
  box-shadow: 0 0 0 1.5px white, 0 0 0 3px currentColor;
}

.a2ui-badge--dot.a2ui-badge--large {
  width: 10px;
  height: 10px;
  box-shadow: 0 0 0 2.5px white, 0 0 0 5px currentColor;
}
</style>
