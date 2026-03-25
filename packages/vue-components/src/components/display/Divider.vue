<template>
  <div class="a2ui-divider flex-shrink-0" :class="classes" :style="styles" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DividerProps } from '@a2ui/core'

const props = withDefaults(defineProps<DividerProps>(), {
  orientation: 'horizontal',
  thickness: 1,
  color: '#e0e0e0',
  margin: 16,
})

const classes = computed(() => [
  {
    'h-0.5': props.orientation === 'horizontal',
    'w-0.5': props.orientation === 'vertical',
  },
])

const styles = computed(() => {
  if (props.orientation === 'horizontal') {
    return {
      backgroundColor: props.color,
      margin: marginValue.value,
    }
  } else {
    return {
      backgroundColor: props.color,
      margin: marginValue.value,
    }
  }
})

const marginValue = computed(() => {
  if (Array.isArray(props.margin)) {
    const [top, right, bottom, left] = props.margin
    return `${top}px ${right}px ${bottom}px ${left}px`
  }
  return `${props.margin}px`
})
</script>
