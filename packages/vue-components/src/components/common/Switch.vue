<template>
  <button
    type="button"
    class="a2ui-switch relative inline-flex flex-shrink-0 cursor-pointer rounded-full border-0 transition-all duration-300 ease-out p-0"
    :class="classes"
    :disabled="disabled"
    role="switch"
    :aria-checked="checked"
    @click="handleClick"
  >
    <span
      class="a2ui-switch__handle inline-block rounded-full bg-white shadow-md transition-transform duration-300 ease-out pointer-events-none"
      :class="{ 'translate-x-full': checked }"
    />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface SwitchProps {
  modelValue?: boolean
  disabled?: boolean
  size?: 'small' | 'medium' | 'large'
  color?: 'primary' | 'success' | 'warning' | 'danger'
}

const props = withDefaults(defineProps<SwitchProps>(), {
  modelValue: false,
  disabled: false,
  size: 'medium',
  color: 'primary',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'change', value: boolean): void
}>()

const checked = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
    emit('change', value)
  },
})

const classes = computed(() => [
  `a2ui-switch--${props.size}`,
  `a2ui-switch--${props.color}`,
  {
    'a2ui-switch--checked': checked.value,
    'a2ui-switch--disabled': props.disabled,
  },
])

const handleClick = () => {
  if (!props.disabled) {
    checked.value = !checked.value
  }
}
</script>

<style scoped>
.a2ui-switch {
  position: relative;
  background-color: #e5e7eb;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.a2ui-switch:hover:not(.a2ui-switch--disabled) {
  box-shadow: 0 0 0 4px rgba(0, 0, 0, 0.05);
}

.a2ui-switch--small {
  width: 36px;
  height: 20px;
  padding: 2px;
}

.a2ui-switch--small .a2ui-switch__handle {
  width: 16px;
  height: 16px;
}

.a2ui-switch--small .a2ui-switch__handle.translate-x-full {
  transform: translateX(16px);
}

.a2ui-switch--medium {
  width: 44px;
  height: 24px;
  padding: 2px;
}

.a2ui-switch--medium .a2ui-switch__handle {
  width: 20px;
  height: 20px;
}

.a2ui-switch--medium .a2ui-switch__handle.translate-x-full {
  transform: translateX(20px);
}

.a2ui-switch--large {
  width: 56px;
  height: 30px;
  padding: 3px;
}

.a2ui-switch--large .a2ui-switch__handle {
  width: 24px;
  height: 24px;
}

.a2ui-switch--large .a2ui-switch__handle.translate-x-full {
  transform: translateX(24px);
}

/* Color variants */
.a2ui-switch--checked.a2ui-switch--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.a2ui-switch--checked.a2ui-switch--success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.a2ui-switch--checked.a2ui-switch--warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.a2ui-switch--checked.a2ui-switch--danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

/* Disabled state */
.a2ui-switch--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.a2ui-switch--disabled:hover {
  box-shadow: none;
}

.a2ui-switch__handle {
  position: relative;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.a2ui-switch--checked .a2ui-switch__handle {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
