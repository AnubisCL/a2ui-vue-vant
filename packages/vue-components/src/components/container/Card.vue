<template>
  <div :class="classes" :style="cardStyles">
    <div v-if="title || subtitle" class="mb-3">
      <h3 v-if="title" class="text-xl font-semibold text-neutral-800 m-0">{{ cardTitle }}</h3>
      <p v-if="subtitle" class="text-sm text-neutral-500 m-0">{{ cardSubtitle }}</p>
    </div>

    <div v-if="content || $slots.default" class="mb-3">
      <slot>{{ cardContent }}</slot>
    </div>

    <div v-if="hasActions" class="flex gap-2 justify-end">
      <button
        v-for="(action, index) in cardActions"
        :key="index"
        class="px-3 py-1.5 border border-neutral-300 rounded bg-white cursor-pointer transition-all text-sm"
        :class="{ 'bg-primary text-white border-primary': action.primary }"
        @click="handleAction(action)"
        type="button"
      >
        {{ action.label }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CardProps, CardAction } from '@a2ui/core'

const props = withDefaults(defineProps<CardProps>(), {
  elevated: false,
  borderRadius: 8,
  padding: 16,
})

const emit = defineEmits<{
  (e: 'action', action: string): void
}>()

const classes = computed(() => [
  'a2ui-card',
  { 'a2ui-card--elevated': props.elevated },
])

const cardStyles = computed(() => ({
  borderRadius: `${props.borderRadius}px`,
  padding: `${props.padding}px`,
}))

const cardTitle = computed(() => {
  return typeof props.title === 'object' && 'path' in props.title ? '' : props.title
})

const cardSubtitle = computed(() => {
  return typeof props.subtitle === 'object' && 'path' in props.subtitle ? '' : props.subtitle
})

const cardContent = computed(() => {
  return typeof props.content === 'object' && 'path' in props.content ? '' : props.content
})

const cardActions = computed((): CardAction[] => {
  return typeof props.actions === 'object' && 'path' in props.actions ? [] : (props.actions ?? [])
})

const hasActions = computed(() => cardActions.value.length > 0)

const handleAction = (action: CardAction) => {
  emit('action', action.action)
}
</script>
