<template>
  <div class="a2ui-choicepicker flex flex-col gap-1 w-full">
    <label v-if="label" class="text-sm font-medium text-neutral-700">{{ pickerLabel }}</label>

    <!-- Dropdown style -->
    <select
      v-if="style === 'dropdown'"
      v-model="selectedValue"
      class="a2ui-textfield__input"
      :disabled="isDisabled"
      :multiple="mode === 'multiple'"
      @change="handleChange"
    >
      <option v-for="choice in availableChoices" :key="choice.value" :value="choice.value">
        {{ choice.label }}
      </option>
    </select>

    <!-- Chips style -->
    <div v-else-if="style === 'chips'" class="flex flex-wrap gap-2">
      <label
        v-for="choice in availableChoices"
        :key="choice.value"
        :class="{ 'a2ui-choicepicker__chip--selected': isSelected(choice.value) }"
        class="a2ui-choicepicker__chip"
      >
        <input
          type="checkbox"
          :checked="isSelected(choice.value)"
          :disabled="isDisabled || choice.disabled"
          @change="toggleChoice(choice.value)"
        />
        <span v-if="choice.icon" class="inline-flex items-center">{{ choice.icon }}</span>
        <span>{{ choice.label }}</span>
      </label>
    </div>

    <!-- List style -->
    <div v-else-if="style === 'list'" class="flex flex-col gap-1">
      <label
        v-for="choice in availableChoices"
        :key="choice.value"
        :class="{ 'bg-primary/10 border-primary': isSelected(choice.value) }"
        class="flex items-start gap-2 p-2 border border-neutral-200 rounded cursor-pointer transition-all hover:bg-neutral-100"
      >
        <input
          :type="mode === 'multiple' ? 'checkbox' : 'radio'"
          :checked="isSelected(choice.value)"
          :disabled="isDisabled || choice.disabled"
          @change="toggleChoice(choice.value)"
        />
        <span class="flex flex-col gap-0.5">
          <span class="font-medium">{{ choice.label }}</span>
          <span v-if="choice.description" class="text-sm text-neutral-500">{{ choice.description }}</span>
        </span>
      </label>
    </div>

    <!-- Buttons style -->
    <div v-else-if="style === 'buttons'" class="flex flex-wrap gap-2">
      <button
        v-for="choice in availableChoices"
        :key="choice.value"
        :class="{ 'bg-primary text-white border-primary': isSelected(choice.value) }"
        class="inline-flex items-center gap-1.5 px-4 py-2 border border-neutral-300 rounded bg-white cursor-pointer transition-all text-base font-normal"
        :disabled="isDisabled || choice.disabled"
        @click="toggleChoice(choice.value)"
        type="button"
      >
        <span v-if="choice.icon">{{ choice.icon }}</span>
        <span>{{ choice.label }}</span>
      </button>
    </div>

    <span v-if="hint" class="text-xs text-neutral-500">{{ hintText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ChoicePickerProps, Choice } from '@a2ui/core'

const props = withDefaults(defineProps<ChoicePickerProps>(), {
  mode: 'single',
  style: 'dropdown',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: string | string[]): void
  (e: 'change', value: string | string[]): void
}>()

const pickerLabel = computed(() => {
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

const availableChoices = computed((): Choice[] => {
  return typeof props.choices === 'object' && 'path' in props.choices ? [] : props.choices
})

const selectedValue = computed({
  get: () => {
    const value = typeof props.value === 'object' && 'path' in props.value ? undefined : props.value
    if (props.mode === 'multiple') {
      return Array.isArray(value) ? value : []
    }
    return value ?? ''
  },
  set: (value) => {
    emit('update:value', value)
  },
})

const isSelected = (choiceValue: string): boolean => {
  const current = selectedValue.value
  if (props.mode === 'multiple') {
    return Array.isArray(current) && current.includes(choiceValue)
  }
  return current === choiceValue
}

const toggleChoice = (choiceValue: string) => {
  if (props.mode === 'multiple') {
    const current = Array.isArray(selectedValue.value) ? selectedValue.value : []
    const index = current.indexOf(choiceValue)
    if (index > -1) {
      emit('update:value', current.filter((v) => v !== choiceValue))
    } else {
      emit('update:value', [...current, choiceValue])
    }
  } else {
    emit('update:value', choiceValue)
  }
  emit('change', selectedValue.value)
}

const handleChange = () => {
  emit('change', selectedValue.value)
}
</script>
