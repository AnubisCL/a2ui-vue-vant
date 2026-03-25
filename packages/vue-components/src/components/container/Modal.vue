<template>
  <van-popup
    v-model:show="isOpen"
    :position="vantPosition"
    :round="true"
    :closeable="props.showClose"
    :close-on-click-overlay="props.closeOnBackdropClick"
    :teleport="'body'"
    :style="popupStyle"
    @close="handleClose"
  >
    <!-- 标题 -->
    <div v-if="modalTitle" class="text-lg font-semibold p-4 border-b border-neutral-200">
      {{ modalTitle }}
    </div>

    <!-- 内容 -->
    <div class="p-4 overflow-auto" :style="contentStyle">
      <slot>{{ modalContent }}</slot>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ModalProps } from '@a2ui/core'
import { resolveStringValue, resolveBooleanValue } from '../../utils'

const props = withDefaults(defineProps<ModalProps>(), {
  showClose: true,
  closeOnBackdropClick: true,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'close'): void
}>()

// 解析打开状态
const isOpen = computed({
  get: () => resolveBooleanValue(props.open, false),
  set: (value) => emit('update:open', value),
})

// 解析标题
const modalTitle = computed(() => {
  return resolveStringValue(props.title, '')
})

// 解析内容
const modalContent = computed(() => {
  return resolveStringValue(props.content, '')
})

// 弹窗位置
const vantPosition = computed(() => 'center')

// 弹窗样式
const popupStyle = computed(() => {
  const style: Record<string, string> = {}

  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  } else {
    style.width = '80%'
  }

  style.maxWidth = '90vw'

  return style
})

// 内容区域样式
const contentStyle = computed(() => {
  const style: Record<string, string> = {}

  if (props.height) {
    style.maxHeight = typeof props.height === 'number' ? `${props.height}px` : props.height
  } else {
    style.maxHeight = '80vh'
  }

  return style
})

// 关闭事件
const handleClose = () => {
  emit('close')
}
</script>
