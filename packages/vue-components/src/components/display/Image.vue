<template>
  <img
    class="a2ui-image block max-w-full transition-all"
    :class="classes"
    :src="imageSrc"
    :alt="altText"
    @click="handleClick"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ImageProps } from '@a2ui/core'

const props = withDefaults(
  defineProps<ImageProps>(),
  {
    fit: 'cover',
    borderRadius: 0,
  }
)

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const classes = computed(() => [
  // Width
  widthClass.value,
  // Height
  heightClass.value,
  // Object fit
  {
    'object-cover': props.fit === 'cover',
    'object-contain': props.fit === 'contain',
    'object-fill': props.fit === 'fill',
    'object-none': props.fit === 'none',
  },
  // Border radius
  `rounded-[${props.borderRadius}px]`,
  // Cursor
  { 'cursor-pointer': props.onClick },
])

const imageSrc = computed(() => {
  return typeof props.src === 'object' && 'path' in props.src ? '' : props.src
})

const altText = computed(() => {
  if (!props.alt) return ''
  return typeof props.alt === 'object' && 'path' in props.alt ? '' : props.alt
})

const widthClass = computed(() => {
  if (!props.width) return ''
  return typeof props.width === 'number' ? `w-[${props.width}px]` : `w-[${props.width}]`
})

const heightClass = computed(() => {
  if (!props.height) return ''
  return typeof props.height === 'number' ? `h-[${props.height}px]` : `h-[${props.height}]`
})

const handleClick = (event: MouseEvent) => {
  if (props.onClick) {
    emit('click', event)
  }
}
</script>
