<template>
  <van-checkbox
    v-model="checkedValue"
    :disabled="isDisabled"
    shape="square"
    :label-disabled="false"
    @change="handleChange"
  >
    {{ checkBoxLabel }}
  </van-checkbox>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CheckBoxProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<CheckBoxProps>(), {
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:checked', value: boolean): void
  (e: 'change', value: boolean): void
}>()

// 解析禁用状态
const isDisabled = computed(() => {
  return resolveBooleanValue(props.disabled, false)
})

// 解析标签
const checkBoxLabel = computed(() => {
  return resolveStringValue(props.label, '')
})

// 双向绑定
const checkedValue = computed({
  get: () => {
    if (typeof props.checked === 'object' && 'path' in props.checked) {
      return false
    }
    return props.checked
  },
  set: (value) => {
    emit('update:checked', value)
  },
})

// 变更事件
const handleChange = (value: boolean) => {
  emit('change', value)
}
</script>
