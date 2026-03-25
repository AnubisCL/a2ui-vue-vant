<template>
  <div class="a2ui-slider flex flex-col gap-2 w-full">
    <div class="flex justify-between items-center">
      <label v-if="label" class="text-sm font-medium text-neutral-700">{{ sliderLabel }}</label>
      <span v-if="showValue" class="text-sm font-semibold text-primary">{{ displayValue }}</span>
    </div>
    <input
      type="range"
      v-model="sliderValue"
      class="a2ui-slider__input"
      :disabled="isDisabled"
      :min="min"
      :max="max"
      :step="step"
      @input="handleInput"
      @change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SliderProps } from '@a2ui/core'

const props = withDefaults(defineProps<SliderProps>(), {
  step: 1,
  showValue: true,
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: number): void
  (e: 'change', value: number): void
}>()

const sliderLabel = computed(() => {
  return typeof props.label === 'object' && 'path' in props.label ? '' : props.label
})

const isDisabled = computed(() => {
  return typeof props.disabled === 'object' && 'path' in props.disabled
    ? false
    : props.disabled
})

const sliderValue = computed({
  get: () => {
    return typeof props.value === 'object' && 'path' in props.value ? 0 : props.value
  },
  set: (value) => {
    emit('update:value', Number(value))
  },
})

const displayValue = computed(() => {
  return Math.round(sliderValue.value * 100) / 100
})

const handleInput = () => {
  // Continuous updates
}

const handleChange = () => {
  emit('change', sliderValue.value)
}
</script>
