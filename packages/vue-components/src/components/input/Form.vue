<template>
  <div class="a2ui-form">
    <van-cell-group inset>
      <template v-for="field in resolvedFields" :key="field.name">
        <!-- Select field -->
        <van-field
          v-if="field.type === 'select'"
          :label="field.label"
          :required="field.required"
          readonly
          is-link
          :model-value="getSelectDisplay(field)"
          @click="showPicker(field)"
        />

        <!-- Text input -->
        <van-field
          v-else-if="field.type === 'text'"
          :model-value="getFieldValue(field.name)"
          @update:model-value="setFieldValue(field.name, $event)"
          :label="field.label"
          :placeholder="getFieldPlaceholder(field)"
          :required="field.required"
        />

        <!-- Textarea -->
        <van-field
          v-else-if="field.type === 'textarea'"
          :model-value="getFieldValue(field.name)"
          @update:model-value="setFieldValue(field.name, $event)"
          :label="field.label"
          :placeholder="getFieldPlaceholder(field)"
          :rows="getFieldRows(field)"
          type="textarea"
          :required="field.required"
        />

        <!-- Date picker -->
        <van-field
          v-else-if="field.type === 'date'"
          :label="field.label"
          :required="field.required"
          readonly
          is-link
          :model-value="getFieldValue(field.name)"
          @click="showDatePicker(field)"
        />

        <!-- Default text input -->
        <van-field
          v-else
          :model-value="getFieldValue(field.name)"
          @update:model-value="setFieldValue(field.name, $event)"
          :label="field.label"
          :required="field.required"
        />
      </template>
    </van-cell-group>

    <!-- Submit button -->
    <div class="a2ui-form-actions mt-4">
      <van-button type="primary" block @click="handleSubmit">
        {{ submitLabel }}
      </van-button>
    </div>

    <!-- Select picker popup -->
    <van-popup v-model:show="pickerVisible" position="bottom" round>
      <van-picker
        :columns="currentPickerOptions"
        @confirm="onPickerConfirm"
        @cancel="pickerVisible = false"
      />
    </van-popup>

    <!-- Date picker popup -->
    <van-popup v-model:show="datePickerVisible" position="bottom" round>
      <van-date-picker
        v-model="currentDate"
        @confirm="onDatePickerConfirm"
        @cancel="datePickerVisible = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, inject } from 'vue'
import { showToast } from 'vant'
import type { FormProps } from '@a2ui/core'
import { resolveArrayValue, resolveStringValue } from '../../utils'

interface ResolvedField {
  name: string
  label: string
  type: string
  required?: boolean
  defaultValue?: unknown
  resolvedOptions?: {
    placeholder?: string
    rows?: number
    options?: Array<{ value: string; label: string }>
  }
}

const props = withDefaults(defineProps<FormProps>(), {
  submitLabel: 'Submit',
})

const emit = defineEmits<{
  (e: 'submit', data: Record<string, unknown>): void
  (e: 'action', action: string, data: Record<string, unknown>): void
}>()

// Get the emit function from parent context for A2UI events
const emitA2uiEvent = inject<(eventType: string, payload?: unknown) => void>('emitA2uiEvent')

// Resolve fields
const resolvedFields = computed<ResolvedField[]>(() => {
  const fields = resolveArrayValue(props.fields, [])
  return fields.map(field => {
    const rawOptions = field.options
    let resolvedOptions: ResolvedField['resolvedOptions'] = {}

    if (rawOptions && typeof rawOptions === 'object') {
      if ('options' in rawOptions) {
        // Format: { options: [...], placeholder?: string, rows?: number }
        let optionsArray = rawOptions.options
        // Fallback: parse if options is a string
        if (typeof optionsArray === 'string') {
          try { optionsArray = JSON.parse(optionsArray) } catch { optionsArray = [] }
        }
        resolvedOptions = {
          options: Array.isArray(optionsArray) ? optionsArray : [],
          placeholder: (rawOptions as any).placeholder,
          rows: (rawOptions as any).rows,
        }
      } else if (Array.isArray(rawOptions)) {
        // Format: Array of options directly
        resolvedOptions = { options: rawOptions }
      }
    }

    return {
      ...field,
      label: field.label || field.name,
      resolvedOptions,
    }
  })
})

const submitLabel = computed(() => resolveStringValue(props.submitLabel, 'Submit'))

// Form data
const formData = reactive<Record<string, unknown>>({})

// Initialize form data
resolvedFields.value.forEach(field => {
  formData[field.name] = field.defaultValue ?? ''
})

// Picker state
const pickerVisible = ref(false)
const currentField = ref<ResolvedField | null>(null)
const currentPickerOptions = computed(() => {
  const opts = currentField.value?.resolvedOptions?.options || []
  return opts.map(opt => ({
    text: opt.label || opt.value,
    value: opt.value,
  }))
})

// Date picker state
const datePickerVisible = ref(false)
const currentDateField = ref<ResolvedField | null>(null)
const currentDate = ref(['2024', '01', '01'])

// Field value helpers
const getFieldValue = (name: string): string => {
  const value = formData[name]
  return value == null ? '' : String(value)
}

const setFieldValue = (name: string, value: string): void => {
  formData[name] = value
}

const getFieldPlaceholder = (field: ResolvedField): string => {
  return field.resolvedOptions?.placeholder || ''
}

const getFieldRows = (field: ResolvedField): number => {
  return field.resolvedOptions?.rows || 3
}

// Show picker for select field
const showPicker = (field: ResolvedField) => {
  currentField.value = field
  pickerVisible.value = true
}

// Show date picker
const showDatePicker = (field: ResolvedField) => {
  currentDateField.value = field
  const currentValue = formData[field.name]
  if (typeof currentValue === 'string' && currentValue) {
    const parts = currentValue.split('-')
    currentDate.value = parts.length === 3 ? parts : ['2024', '01', '01']
  }
  datePickerVisible.value = true
}

// Get display text for select field
const getSelectDisplay = (field: ResolvedField): string => {
  const value = formData[field.name]
  if (!value) return ''

  const options = field.resolvedOptions?.options || []
  const option = options.find(o => o.value === value)
  return option?.label || option?.value || String(value)
}

// Picker confirm
const onPickerConfirm = ({ selectedOptions }: { selectedOptions: Array<{ text: string; value: string }> }) => {
  if (currentField.value && selectedOptions[0]) {
    formData[currentField.value.name] = selectedOptions[0].value
  }
  pickerVisible.value = false
}

// Date picker confirm
const onDatePickerConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  if (currentDateField.value) {
    formData[currentDateField.value.name] = selectedValues.join('-')
  }
  datePickerVisible.value = false
}

// Handle submit
const handleSubmit = () => {
  // Validate required fields
  for (const field of resolvedFields.value) {
    if (field.required && !formData[field.name]) {
      showToast(`请填写 ${field.label}`)
      return
    }
  }

  const data = { ...formData }

  // Emit local events
  emit('submit', data)
  emit('action', 'submit', data)

  // Emit A2UI event for backend communication
  const action = resolveStringValue(props.action, 'form-submit')
  if (emitA2uiEvent) {
    emitA2uiEvent(action, data)
  }

  // Also dispatch custom event for components that listen to DOM events
  window.dispatchEvent(new CustomEvent('a2ui-form-submit', {
    detail: { action, data }
  }))

  showToast('表单已提交')
}
</script>

<style scoped>
.a2ui-form {
  width: 100%;
}

.a2ui-form-actions {
  padding: 0 16px;
}
</style>
