<template>
  <div class="a2ui-datetime">
    <!-- 输入框显示 -->
    <van-cell
      is-link
      :title="fieldLabel"
      :value="displayValue"
      :disabled="isDisabled"
      @click="handleOpen"
    />

    <!-- 日期时间选择器弹窗 -->
    <van-popup
      v-model:show="showPicker"
      position="bottom"
      round
      :teleport="'body'"
    >
      <van-date-picker
        v-model="currentDate"
        :title="fieldLabel || '选择日期'"
        :type="vantPickerType"
        :min-date="minDateValue"
        :max-date="maxDateValue"
        @confirm="handleConfirm"
        @cancel="showPicker = false"
      />
    </van-popup>

    <!-- 提示文字 -->
    <div v-if="hint" class="text-xs text-neutral-500 mt-1 px-4">
      {{ hintText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { DateTimeInputProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<DateTimeInputProps>(), {
  type: 'date',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: string): void
  (e: 'change', value: string): void
}>()

// 弹窗状态
const showPicker = ref(false)

// 解析标签
const fieldLabel = computed(() => {
  return resolveStringValue(props.label, '')
})

// 解析提示
const hintText = computed(() => {
  return resolveStringValue(props.hint, '')
})

// 解析禁用状态
const isDisabled = computed(() => {
  return resolveBooleanValue(props.disabled, false)
})

// 映射 A2UI type 到 Vant type
const vantPickerType = computed(() => {
  const map: Record<string, string> = {
    date: 'date',
    time: 'time',
    datetime: 'datetime',
  }
  return map[props.type] || 'date'
})

// 最小日期
const minDateValue = computed(() => {
  if (!props.min) return new Date(1970, 0, 1)
  return new Date(props.min)
})

// 最大日期
const maxDateValue = computed(() => {
  if (!props.max) return new Date(2100, 11, 31)
  return new Date(props.max)
})

// 当前选中的日期 (用于 picker)
const currentDate = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return []
    }
    // 将字符串值转换为 Vant DatePicker 需要的格式
    if (!props.value) {
      return []
    }
    // 根据类型解析
    if (props.type === 'time') {
      // time 格式: HH:mm
      return props.value.split(':')
    } else {
      // date/datetime 格式
      const date = new Date(props.value)
      return [date.getFullYear(), date.getMonth() + 1, date.getDate()]
    }
  },
  set: () => {
    // 由 handleConfirm 处理
  },
})

// 显示值
const displayValue = computed(() => {
  if (typeof props.value === 'object' && 'path' in props.value) {
    return '请选择'
  }
  if (!props.value) {
    return '请选择'
  }
  return props.value
})

// 打开选择器
const handleOpen = () => {
  if (!isDisabled.value) {
    showPicker.value = true
  }
}

// 确认选择
const handleConfirm = ({ selectedValues }: { selectedValues: number[] }) => {
  let result = ''

  if (props.type === 'time') {
    // time 格式: HH:mm
    const hours = String(selectedValues[0]).padStart(2, '0')
    const minutes = String(selectedValues[1]).padStart(2, '0')
    result = `${hours}:${minutes}`
  } else if (props.type === 'datetime') {
    // datetime 格式: YYYY-MM-DD HH:mm
    const date = new Date(selectedValues.join('-'))
    result = date.toISOString().slice(0, 16).replace('T', ' ')
  } else {
    // date 格式: YYYY-MM-DD
    const year = selectedValues[0]
    const month = String(selectedValues[1]).padStart(2, '0')
    const day = String(selectedValues[2]).padStart(2, '0')
    result = `${year}-${month}-${day}`
  }

  emit('update:value', result)
  emit('change', result)
  showPicker.value = false
}
</script>
