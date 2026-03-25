<template>
  <div class="a2ui-list flex flex-col" :class="classes">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ListProps } from '@a2ui/core'

const props = withDefaults(defineProps<ListProps>(), {
  gap: 8,
  scrollable: true,
})

const emit = defineEmits<{
  (e: 'itemRender', index: number, item: unknown): void
}>()

const classes = computed(() => [
  `gap-[${props.gap}px]`,
  {
    'overflow-auto': props.scrollable,
    'overflow-visible': !props.scrollable,
    'max-h-full': props.scrollable,
  },
])
</script>
