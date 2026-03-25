<template>
  <label class="a2ui-checkbox inline-flex items-center gap-2" :class="{ 'opacity-60 cursor-not-allowed': isDisabled }">
    <input
      type="checkbox"
      v-model="checkedValue"
      class="a2ui-checkbox__input"
      :disabled="isDisabled"
      @change="handleChange"
    />
    <span v-if="label" class="select-none">{{ checkBoxLabel }}</span>
  </label>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CheckBoxProps } from '@a2ui/core'

const props = withDefaults(defineProps<CheckBoxProps>(), {
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:checked', value: boolean): void
  (e: 'change', value: boolean): void
}>()

const isDisabled = computed(() => {
  return typeof props.disabled === 'object' && 'path' in props.disabled
    ? false
    : props.disabled
})

const checkBoxLabel = computed(() => {
  return typeof props.label === 'object' && 'path' in props.label ? '' : props.label
})

const checkedValue = computed({
  get: () => {
    return typeof props.checked === 'object' && 'path' in props.checked ? false : props.checked
  },
  set: (value) => {
    emit('update:checked', value)
  },
})

const handleChange = () => {
  emit('change', checkedValue.value)
}
</script>
