<template>
  <div class="a2ui-datetime flex flex-col gap-1 w-full">
    <label v-if="label" class="text-sm font-medium text-neutral-700">{{ inputLabel }}</label>
    <input
      v-model="inputValue"
      class="a2ui-textfield__input"
      :type="inputType"
      :disabled="isDisabled"
      :min="min"
      :max="max"
      @change="handleChange"
    />
    <span v-if="hint" class="text-xs text-neutral-500">{{ hintText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DateTimeInputProps } from '@a2ui/core'

const props = withDefaults(defineProps<DateTimeInputProps>(), {
  type: 'datetime',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: string): void
  (e: 'change', value: string): void
}>()

const inputType = computed(() => {
  const typeMap: Record<NonNullable<DateTimeInputProps['type']>, string> = {
    date: 'date',
    time: 'time',
    datetime: 'datetime-local',
  }
  return typeMap[props.type]
})

const inputLabel = computed(() => {
  return typeof props.label === 'object' && 'path' in props.label ? '' : props.label
})

const hintText = computed(() => {
  return typeof props.hint === 'object' && 'path' in props.hint ? '' : props.hint
})

const isDisabled = computed(() => {
  return typeof props.disabled === 'object' && 'path' in props.disabled
    ? false
    : props.disabled
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
</script>
