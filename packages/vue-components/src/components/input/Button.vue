<template>
  <button
    class="a2ui-button a2ui-btn-base"
    :class="classes"
    :disabled="isDisabled"
    type="button"
    @click="handleClick"
  >
    <span v-if="icon && iconPosition === 'left'" class="inline-flex items-center">
      <slot name="icon">{{ icon }}</slot>
    </span>
    <span class="flex-1">{{ buttonLabel }}</span>
    <span v-if="icon && iconPosition === 'right'" class="inline-flex items-center">
      <slot name="icon">{{ icon }}</slot>
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ButtonProps } from '@a2ui/core'

const props = withDefaults(defineProps<ButtonProps>(), {
  variant: 'primary',
  size: 'medium',
  disabled: false,
  iconPosition: 'left',
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const classes = computed(() => [
  // Size classes
  {
    'a2ui-button--small': props.size === 'small',
    'a2ui-button--medium': props.size === 'medium',
    'a2ui-button--large': props.size === 'large',
  },
  // Variant classes
  {
    'a2ui-button--primary': props.variant === 'primary',
    'a2ui-button--secondary': props.variant === 'secondary',
    'a2ui-button--borderless': props.variant === 'borderless',
    'a2ui-button--danger': props.variant === 'danger',
  },
  // Disabled state
  { 'opacity-60 cursor-not-allowed': isDisabled.value },
])

const isDisabled = computed(() => {
  return typeof props.disabled === 'object' && 'path' in props.disabled
    ? false
    : props.disabled
})

const buttonLabel = computed(() => {
  return typeof props.label === 'object' && 'path' in props.label ? '' : props.label
})

const handleClick = (event: MouseEvent) => {
  if (!isDisabled.value) {
    emit('click', event)
  }
}
</script>
