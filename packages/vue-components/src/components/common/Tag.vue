<template>
  <span class="a2ui-tag inline-flex items-center gap-1.5 border-0 rounded-lg font-medium transition-all duration-200 cursor-default" :class="classes">
    <slot>
      <span v-if="icon" class="a2ui-tag__icon text-sm">{{ icon }}</span>
      <span class="a2ui-tag__text">{{ text }}</span>
    </slot>
    <button
      v-if="closable"
      type="button"
      class="a2ui-tag__close ml-0.5 cursor-pointer border-0 bg-transparent p-0.5 rounded transition-all duration-200 hover:bg-black/10"
      @click="handleClose"
    >
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
        <path
          d="M9 3L3 9M3 3L9 9"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </button>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface TagProps {
  text?: string
  icon?: string
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'small' | 'medium' | 'large'
  closable?: boolean
}

const props = withDefaults(defineProps<TagProps>(), {
  variant: 'default',
  size: 'medium',
  closable: false,
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

const classes = computed(() => [
  `a2ui-tag--${props.variant}`,
  `a2ui-tag--${props.size}`,
])

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.a2ui-tag {
  padding: 5px 11px;
  font-size: 13px;
  line-height: 1.25;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.a2ui-tag--small {
  padding: 3px 8px;
  font-size: 11px;
}

.a2ui-tag--large {
  padding: 7px 14px;
  font-size: 14px;
}

/* Default - Soft gray */
.a2ui-tag--default {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.a2ui-tag--default:hover {
  background-color: #e5e7eb;
}

/* Primary - Soft blue */
.a2ui-tag--primary {
  background-color: #dbeafe;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

.a2ui-tag--primary:hover {
  background-color: #bfdbfe;
}

/* Success - Soft green */
.a2ui-tag--success {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.a2ui-tag--success:hover {
  background-color: #a7f3d0;
}

/* Warning - Soft orange */
.a2ui-tag--warning {
  background-color: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.a2ui-tag--warning:hover {
  background-color: #fde68a;
}

/* Danger - Soft red */
.a2ui-tag--danger {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.a2ui-tag--danger:hover {
  background-color: #fecaca;
}

/* Info - Soft sky */
.a2ui-tag--info {
  background-color: #e0f2fe;
  color: #075985;
  border: 1px solid #bae6fd;
}

.a2ui-tag--info:hover {
  background-color: #bae6fd;
}

.a2ui-tag__close {
  color: currentColor;
  opacity: 0.6;
}

.a2ui-tag__close:hover {
  opacity: 1;
}
</style>
