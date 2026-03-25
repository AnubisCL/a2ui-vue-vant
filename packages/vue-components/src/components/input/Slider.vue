<template>
  <div class="a2ui-slider">
    <div class="flex justify-between items-center mb-2">
      <label v-if="label" class="text-sm font-medium text-neutral-700">{{ sliderLabel }}</label>
      <span v-if="showValue" class="text-sm font-semibold text-primary">{{ displayValue }}</span>
    </div>
    <van-slider
      v-model="sliderValue"
      :min="min"
      :max="max"
      :step="step"
      :disabled="isDisabled"
      bar-height="4px"
      active-color="#1989fa"
      inactive-color="#e5e5e5"
      @change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SliderProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<SliderProps>(), {
  step: 1,
  showValue: true,
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: number): void
  (e: 'change', value: number): void
}>()

// 解析标签
const sliderLabel = computed(() => {
  return resolveStringValue(props.label, '')
})

// 解析禁用状态
const isDisabled = computed(() => {
  return resolveBooleanValue(props.disabled, false)
})

// 双向绑定
const sliderValue = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return props.min
    }
    return props.value
  },
  set: (value) => {
    emit('update:value', Number(value))
  },
})

// 显示值（格式化）
const displayValue = computed(() => {
  return Math.round(sliderValue.value * 100) / 100
})

// 变更事件
const handleChange = (value: number) => {
  emit('change', value)
}
</script>

<style scoped>
.a2ui-slider {
  width: 100%;
}
</style>
