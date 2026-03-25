<template>
  <div class="a2ui-choicepicker">
    <!-- 标签 -->
    <div v-if="label" class="text-sm font-medium text-neutral-700 mb-2">
      {{ pickerLabel }}
    </div>

    <!-- Chips 样式 (默认) -->
    <van-space v-if="style === 'chips'" wrap>
      <template v-if="mode === 'single'">
        <van-tag
          v-for="choice in availableChoices"
          :key="choice.value"
          :type="isSelected(choice.value) ? 'primary' : 'default'"
          :disabled="isDisabled || choice.disabled"
          size="large"
          @click="toggleChoice(choice.value)"
        >
          <span v-if="choice.icon" class="mr-1">{{ choice.icon }}</span>
          {{ choice.label }}
        </van-tag>
      </template>
      <template v-else>
        <van-checkbox-group v-model="multiSelectValue" direction="horizontal">
          <van-checkbox
            v-for="choice in availableChoices"
            :key="choice.value"
            :name="choice.value"
            :disabled="isDisabled || choice.disabled"
            shape="square"
          >
            <span v-if="choice.icon" class="mr-1">{{ choice.icon }}</span>
            {{ choice.label }}
          </van-checkbox>
        </van-checkbox-group>
      </template>
    </van-space>

    <!-- 下拉选择样式 -->
    <van-picker-group
      v-else-if="style === 'dropdown'"
      :title="pickerLabel"
    >
      <template v-if="mode === 'single'">
        <van-cell
          is-link
          :title="selectedChoiceLabel"
          @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom" round>
          <van-picker
            :columns="pickerColumns"
            @confirm="onPickerConfirm"
            @cancel="showPicker = false"
          />
        </van-popup>
      </template>
      <template v-else>
        <van-cell
          is-link
          :title="selectedChoiceLabels"
          @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom" round>
          <van-checkbox-group v-model="multiSelectValue">
            <van-cell-group>
              <van-cell
                v-for="choice in availableChoices"
                :key="choice.value"
                clickable
                :title="choice.label"
                @click="toggleMultiChoice(choice.value)"
              >
                <template #right-icon>
                  <van-checkbox :name="choice.value" :disabled="choice.disabled" />
                </template>
              </van-cell>
            </van-cell-group>
          </van-checkbox-group>
        </van-popup>
      </template>
    </van-picker-group>

    <!-- 列表样式 -->
    <van-radio-group
      v-else-if="style === 'list' && mode === 'single'"
      v-model="singleSelectValue"
    >
      <van-cell-group>
        <van-cell
          v-for="choice in availableChoices"
          :key="choice.value"
          clickable
          :title="choice.label"
          :label="choice.description"
          @click="singleSelectValue = choice.value"
        >
          <template #right-icon>
            <van-radio :name="choice.value" :disabled="choice.disabled" />
          </template>
        </van-cell>
      </van-cell-group>
    </van-radio-group>
    <van-checkbox-group
      v-else-if="style === 'list' && mode === 'multiple'"
      v-model="multiSelectValue"
    >
      <van-cell-group>
        <van-cell
          v-for="choice in availableChoices"
          :key="choice.value"
          clickable
          :title="choice.label"
          :label="choice.description"
          @click="toggleMultiChoice(choice.value)"
        >
          <template #right-icon>
            <van-checkbox :name="choice.value" :disabled="choice.disabled" />
          </template>
        </van-cell>
      </van-cell-group>
    </van-checkbox-group>

    <!-- 按钮样式 -->
    <van-space v-else-if="style === 'buttons'" wrap>
      <template v-if="mode === 'single'">
        <van-radio-group v-model="singleSelectValue" direction="horizontal">
          <van-radio
            v-for="choice in availableChoices"
            :key="choice.value"
            :name="choice.value"
            :disabled="isDisabled || choice.disabled"
          >
            <span v-if="choice.icon">{{ choice.icon }}</span>
            {{ choice.label }}
          </van-radio>
        </van-radio-group>
      </template>
      <template v-else>
        <van-checkbox-group v-model="multiSelectValue" direction="horizontal">
          <van-checkbox
            v-for="choice in availableChoices"
            :key="choice.value"
            :name="choice.value"
            :disabled="isDisabled || choice.disabled"
            shape="square"
          >
            <span v-if="choice.icon">{{ choice.icon }}</span>
            {{ choice.label }}
          </van-checkbox>
        </van-checkbox-group>
      </template>
    </van-space>

    <!-- 提示文字 -->
    <div v-if="hint" class="text-xs text-neutral-500 mt-1">
      {{ hintText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ChoicePickerProps, Choice } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue, resolveArrayValue } from '../../utils'

const props = withDefaults(defineProps<ChoicePickerProps>(), {
  mode: 'single',
  style: 'dropdown',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:value', value: string | string[]): void
  (e: 'change', value: string | string[]): void
}>()

// Picker 弹出框状态
const showPicker = ref(false)

// 解析标签
const pickerLabel = computed(() => {
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

// 解析选项列表
const availableChoices = computed((): Choice[] => {
  return resolveArrayValue(props.choices, [])
})

// Picker 列格式
const pickerColumns = computed(() => {
  return availableChoices.value.map(choice => ({
    text: choice.label,
    value: choice.value,
  }))
})

// 单选值
const singleSelectValue = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return ''
    }
    const val = props.value
    return Array.isArray(val) ? val[0] || '' : val
  },
  set: (value: string) => {
    emit('update:value', value)
    emit('change', value)
  },
})

// 多选值
const multiSelectValue = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return []
    }
    const val = props.value
    return Array.isArray(val) ? val : []
  },
  set: (value: string[]) => {
    emit('update:value', value)
    emit('change', value)
  },
})

// 判断是否选中
const isSelected = (choiceValue: string): boolean => {
  if (props.mode === 'multiple') {
    return multiSelectValue.value.includes(choiceValue)
  }
  return singleSelectValue.value === choiceValue
}

// 切换选择
const toggleChoice = (choiceValue: string) => {
  if (props.mode === 'multiple') {
    toggleMultiChoice(choiceValue)
  } else {
    singleSelectValue.value = choiceValue
  }
}

// 切换多选项
const toggleMultiChoice = (choiceValue: string) => {
  const current = [...multiSelectValue.value]
  const index = current.indexOf(choiceValue)
  if (index > -1) {
    current.splice(index, 1)
  } else {
    current.push(choiceValue)
  }
  multiSelectValue.value = current
}

// Picker 确认
const onPickerConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  singleSelectValue.value = selectedValues[0]
  showPicker.value = false
}

// 当前选中项的标签 (单选)
const selectedChoiceLabel = computed(() => {
  const selected = availableChoices.value.find(c => c.value === singleSelectValue.value)
  return selected?.label || '请选择'
})

// 当前选中项的标签 (多选)
const selectedChoiceLabels = computed(() => {
  const labels = multiSelectValue.value
    .map(v => availableChoices.value.find(c => c.value === v)?.label)
    .filter(Boolean)
  return labels.length > 0 ? labels.join(', ') : '请选择'
})
</script>
