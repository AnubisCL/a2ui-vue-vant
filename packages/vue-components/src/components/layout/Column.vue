<template>
  <div class="a2ui-column flex flex-col" :class="classes">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ColumnProps } from '@a2ui/core'

const props = withDefaults(defineProps<ColumnProps>(), {
  align: 'start',
  valign: 'start',
  gap: 0,
  margin: 0,
  scrollable: false,
})

const classes = computed(() => [
  // Alignment classes
  {
    'items-start': props.align === 'start',
    'items-center': props.align === 'center',
    'items-end': props.align === 'end',
    'items-stretch': props.align === 'stretch',
  },
  // Cross alignment classes
  {
    'justify-start': props.valign === 'start',
    'justify-center': props.valign === 'center',
    'justify-end': props.valign === 'end',
    'justify-between': props.valign === 'spaceBetween',
    'justify-around': props.valign === 'spaceAround',
    'justify-evenly': props.valign === 'spaceEvenly',
  },
  // Gap
  `gap-[${props.gap}px]`,
  // Margin
  marginClass.value,
  // Height & Width
  heightWidthClass.value,
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

const heightWidthClass = computed(() => ({
  [typeof props.height === 'number' ? `h-[${props.height}px]` : `h-[${props.height}]`]: props.height !== undefined,
  [typeof props.width === 'number' ? `w-[${props.width}px]` : `w-[${props.width}]`]: props.width !== undefined,
}))
</script>
