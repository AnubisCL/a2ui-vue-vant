<template>
  <div class="a2ui-alert border rounded-xl overflow-hidden transition-all duration-200" :class="classes" role="alert">
    <div class="flex items-start gap-3 p-4">
      <div v-if="showIcon" class="a2ui-alert__icon flex-shrink-0 text-xl flex items-center justify-center w-6 h-6 rounded-full">
        {{ icon }}
      </div>
      <div class="flex-1 min-w-0">
        <div v-if="title" class="a2ui-alert__title font-semibold mb-1">{{ title }}</div>
        <div class="a2ui-alert__message text-sm leading-relaxed">
          <slot>{{ message }}</slot>
        </div>
      </div>
      <button
        v-if="closable"
        type="button"
        class="a2ui-alert__close flex-shrink-0 cursor-pointer border-0 bg-transparent p-1 rounded transition-all duration-200 hover:bg-black/10 opacity-60 hover:opacity-100"
        @click="handleClose"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path
            d="M12 4L4 12M4 4L12 12"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface AlertProps {
  title?: string
  message?: string
  variant?: 'info' | 'success' | 'warning' | 'error'
  showIcon?: boolean
  closable?: boolean
}

const props = withDefaults(defineProps<AlertProps>(), {
  variant: 'info',
  showIcon: true,
  closable: false,
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

const iconMap = {
  info: 'ℹ️',
  success: '✓',
  warning: '⚠',
  error: '✕',
}

const icon = computed(() => iconMap[props.variant])

const classes = computed(() => [
  `a2ui-alert--${props.variant}`,
])

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.a2ui-alert {
  background-color: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Info - Blue */
.a2ui-alert--info {
  border-color: #3b82f6;
  background: linear-gradient(to bottom, #eff6ff, #ffffff);
}

.a2ui-alert--info .a2ui-alert__icon {
  background-color: #dbeafe;
  color: #1e40af;
}

.a2ui-alert--info .a2ui-alert__title {
  color: #1e40af;
}

.a2ui-alert--info .a2ui-alert__message {
  color: #1e3a8a;
}

/* Success - Green */
.a2ui-alert--success {
  border-color: #10b981;
  background: linear-gradient(to bottom, #f0fdf4, #ffffff);
}

.a2ui-alert--success .a2ui-alert__icon {
  background-color: #d1fae5;
  color: #065f46;
}

.a2ui-alert--success .a2ui-alert__title {
  color: #065f46;
}

.a2ui-alert--success .a2ui-alert__message {
  color: #064e3b;
}

/* Warning - Orange */
.a2ui-alert--warning {
  border-color: #f59e0b;
  background: linear-gradient(to bottom, #fffbeb, #ffffff);
}

.a2ui-alert--warning .a2ui-alert__icon {
  background-color: #fef3c7;
  color: #92400e;
}

.a2ui-alert--warning .a2ui-alert__title {
  color: #92400e;
}

.a2ui-alert--warning .a2ui-alert__message {
  color: #78350f;
}

/* Error - Red */
.a2ui-alert--error {
  border-color: #ef4444;
  background: linear-gradient(to bottom, #fef2f2, #ffffff);
}

.a2ui-alert--error .a2ui-alert__icon {
  background-color: #fee2e2;
  color: #991b1b;
}

.a2ui-alert--error .a2ui-alert__title {
  color: #991b1b;
}

.a2ui-alert--error .a2ui-alert__message {
  color: #7f1d1d;
}
</style>
