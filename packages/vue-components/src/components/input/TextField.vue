<template>
  <div class="a2ui-textfield flex flex-col gap-1">
    <label v-if="label" class="text-sm font-medium text-neutral-700">{{ fieldLabel }}</label>
    <component
      :is="inputComponent"
      v-model="inputValue"
      class="a2ui-textfield__input"
      :placeholder="placeholderText"
      :disabled="isDisabled"
      :readonly="isReadonly"
      :type="inputType"
      :min="min"
      :max="max"
      :maxlength="maxLength"
      @change="handleChange"
      @keydown="handleKeydown"
    />
    <span v-if="hint" class="text-xs text-neutral-500">{{ hintText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TextFieldProps } from '@a2ui/core'

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

const inputComponent = computed(() => {
  return props.type === 'longText' ? 'textarea' : 'input'
})

const inputType = computed(() => {
  const typeMap: Record<NonNullable<TextFieldProps['type']>, string> = {
    shortText: 'text',
    longText: 'text',
    number: 'number',
    obscured: 'password',
  }
  return typeMap[props.type]
})

const fieldLabel = computed(() => {
  return typeof props.label === 'object' && 'path' in props.label ? '' : props.label
})

const hintText = computed(() => {
  return typeof props.hint === 'object' && 'path' in props.hint ? '' : props.hint
})

const placeholderText = computed(() => {
  return typeof props.placeholder === 'object' && 'path' in props.placeholder ? '' : props.placeholder
})

const isDisabled = computed(() => {
  return typeof props.disabled === 'object' && 'path' in props.disabled
    ? false
    : props.disabled
})

const isReadonly = computed(() => {
  return typeof props.readonly === 'object' && 'path' in props.readonly
    ? false
    : props.readonly
})

const inputValue = computed({
  get: () => {
    return typeof props.value === 'object' && 'path' in props.value ? '' : props.value
  },
  set: (value) => {
    emit('update:value', value)
  },
})

const handleChange = () => {
  emit('change', inputValue.value)
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && props.type !== 'longText') {
    emit('submit')
  }
}
</script>

<style scoped>
.a2ui-textfield__input:deep(:disabled) {
  background: #f5f5f5;
  cursor: not-allowed;
}

.a2ui-textfield--longText :deep(.a2ui-textfield__input) {
  min-height: 100px;
  resize: vertical;
}
</style>
