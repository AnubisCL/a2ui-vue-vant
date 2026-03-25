<template>
  <van-field
    v-model="inputValue"
    :label="fieldLabel"
    :placeholder="placeholderText"
    :disabled="isDisabled"
    :readonly="isReadonly"
    :type="vantType"
    :maxlength="maxLength"
    :rows="props.type === 'longText' ? 3 : undefined"
    :autosize="props.type === 'longText'"
    clearable
    :error="hasError"
    :error-message="errorMessage"
    @update:model-value="handleInput"
    @change="handleChange"
    @keypress.enter="handleSubmit"
  >
    <template v-if="hintText" #extra>
      <span class="text-xs text-neutral-500">{{ hintText }}</span>
    </template>
  </van-field>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { TextFieldProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<TextFieldProps>(), {
  type: 'shortText',
  disabled: false,
  readonly: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: string | number): void
  (e: 'change', value: string | number): void
  (e: 'submit'): void
}>()

// 错误状态
const hasError = ref(false)
const errorMessage = ref('')

// 映射 A2UI type 到 Vant type
const vantType = computed(() => {
  const map: Record<string, string> = {
    shortText: 'text',
    longText: 'textarea',
    number: 'digit',
    obscured: 'password',
  }
  return map[props.type] || 'text'
})

// 解析标签
const fieldLabel = computed(() => {
  return resolveStringValue(props.label, '')
})

// 解析提示
const hintText = computed(() => {
  return resolveStringValue(props.hint, '')
})

// 解析占位符
const placeholderText = computed(() => {
  return resolveStringValue(props.placeholder, '')
})

// 解析禁用状态
const isDisabled = computed(() => {
  return resolveBooleanValue(props.disabled, false)
})

// 解析只读状态
const isReadonly = computed(() => {
  return resolveBooleanValue(props.readonly, false)
})

// 双向绑定
const inputValue = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return ''
    }
    return props.value?.toString() ?? ''
  },
  set: (val) => {
    const finalValue = props.type === 'number' ? Number(val) : val
    emit('update:value', finalValue)
  },
})

// 输入事件
const handleInput = (val: string | number) => {
  // 清除错误状态
  hasError.value = false
  errorMessage.value = ''
}

// 变更事件
const handleChange = () => {
  emit('change', inputValue.value)
}

// 提交事件 (Enter 键)
const handleSubmit = (event: KeyboardEvent) => {
  // textarea 不触发提交
  if (props.type !== 'longText') {
    emit('submit')
  }
}
</script>
