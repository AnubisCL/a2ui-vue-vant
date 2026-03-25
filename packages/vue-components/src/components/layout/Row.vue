<template>
  <div class="a2ui-row flex flex-row" :class="classes">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RowProps } from '@a2ui/core'

const props = withDefaults(defineProps<RowProps>(), {
  align: 'start',
  valign: 'center',
  gap: 0,
  margin: 0,
  scrollable: false,
})

const classes = computed(() => [
  // Alignment classes
  {
    'justify-start': props.align === 'start',
    'justify-center': props.align === 'center',
    'justify-end': props.align === 'end',
    'justify-between': props.align === 'spaceBetween',
    'justify-around': props.align === 'spaceAround',
    'justify-evenly': props.align === 'spaceEvenly',
  },
  // Cross alignment classes
  {
    'items-start': props.valign === 'start',
    'items-center': props.valign === 'center',
    'items-end': props.valign === 'end',
    'items-stretch': props.valign === 'stretch',
  },
  // Gap
  `gap-[${props.gap}px]`,
  // Margin
  marginClass.value,
  // Width
  widthClass.value,
  // Scrollable
  { 'overflow-auto': props.scrollable },
  { 'overflow-visible': !props.scrollable },
])

const marginClass = computed(() => {
  if (Array.isArray(props.margin)) {
    const [top, right, bottom, left] = props.margin
    return `m-[${top}px_${right}px_${bottom}px_${left}px]`
  }
  return `m-[${props.margin}px]`
})

const widthClass = computed(() => {
  if (!props.width) return ''
  return typeof props.width === 'number' ? `w-[${props.width}px]` : `w-[${props.width}]`
})
</script>
