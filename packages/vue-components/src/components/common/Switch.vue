<template>
  <van-switch
    v-model="checked"
    :loading="loading"
    :disabled="disabled"
    :size="vantSize"
    :active-color="activeColorValue"
    inactive-color="#e5e7eb"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface SwitchProps {
  modelValue?: boolean
  disabled?: boolean
  loading?: boolean
  size?: 'small' | 'medium' | 'large'
  activeColor?: string
}

const props = withDefaults(defineProps<SwitchProps>(), {
  modelValue: false,
  disabled: false,
  loading: false,
  size: 'medium',
  activeColor: '#1989fa',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'change', value: boolean): void
}>()

// 双向绑定
const checked = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
    emit('change', value)
  },
})

// 映射尺寸
const vantSize = computed(() => {
  const sizeMap: Record<string, string> = {
    small: '18',
    medium: '24',
    large: '30',
  }
  return sizeMap[props.size] || '24'
})

// 激活颜色
const activeColorValue = computed(() => {
  return props.activeColor || '#1989fa'
})
</script>
