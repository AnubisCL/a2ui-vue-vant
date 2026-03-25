<template>
  <van-button
    :type="vantType"
    :size="vantSize"
    :disabled="isDisabled"
    :icon="icon"
    :plain="isPlain"
    :hairline="isHairline"
    :round="true"
    @click="handleClick"
  >
    {{ buttonLabel }}
  </van-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ButtonProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<ButtonProps>(), {
  variant: 'primary',
  size: 'medium',
  disabled: false,
  iconPosition: 'left',
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

// 映射 A2UI variant 到 Vant type
const vantType = computed(() => {
  const map: Record<string, string> = {
    primary: 'primary',
    secondary: 'default',
    borderless: 'default',
    danger: 'danger',
  }
  return map[props.variant] || 'primary'
})

// 映射 A2UI size 到 Vant size
const vantSize = computed(() => {
  const map: Record<string, string> = {
    small: 'small',
    medium: 'normal',
    large: 'large',
  }
  return map[props.size] || 'normal'
})

// 是否为朴素按钮
const isPlain = computed(() => {
  return props.variant === 'secondary'
})

// 是否为细边框按钮
const isHairline = computed(() => {
  return props.variant === 'borderless'
})

// 解析禁用状态
const isDisabled = computed(() => {
  return resolveBooleanValue(props.disabled, false)
})

// 解析按钮标签
const buttonLabel = computed(() => {
  return resolveStringValue(props.label, '')
})

const handleClick = (event: MouseEvent) => {
  if (!isDisabled.value) {
    emit('click', event)
  }
}
</script>
