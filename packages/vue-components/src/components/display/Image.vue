<template>
  <van-image
    :src="imageSrc"
    :alt="altText"
    :width="imageWidth"
    :height="imageHeight"
    :fit="vantFit"
    :radius="borderRadius"
    lazy-load
    @click="handleClick"
  >
    <template #error>
      <div class="flex items-center justify-center w-full h-full bg-neutral-100 text-neutral-400">
        <van-icon name="photo-o" size="32" />
      </div>
    </template>
    <template #loading>
      <van-loading type="spinner" size="20" />
    </template>
  </van-image>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ImageProps } from '@a2ui/core'
import { resolveStringValue } from '../../utils'

const props = withDefaults(defineProps<ImageProps>(), {
  fit: 'cover',
  borderRadius: 0,
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

// 解析图片源
const imageSrc = computed(() => {
  return resolveStringValue(props.src, '')
})

// 解析 alt 文本
const altText = computed(() => {
  return resolveStringValue(props.alt, '')
})

// 图片宽度
const imageWidth = computed(() => {
  if (!props.width) return undefined
  return typeof props.width === 'number' ? `${props.width}px` : props.width
})

// 图片高度
const imageHeight = computed(() => {
  if (!props.height) return undefined
  return typeof props.height === 'number' ? `${props.height}px` : props.height
})

// 映射 fit 属性到 Vant
const vantFit = computed(() => {
  const validFits = ['contain', 'cover', 'fill', 'none', 'scale-down'] as const
  return validFits.includes(props.fit as any) ? (props.fit as typeof validFits[number]) : 'cover'
})

// 点击事件
const handleClick = (event: MouseEvent) => {
  if (props.onClick) {
    emit('click', event)
  }
}
</script>
