<template>
  <div :class="classes" :data-position="position">
    <div class="flex gap-1 border-b border-neutral-200 bg-neutral-50" role="tablist">
      <button
        v-for="tab in availableTabs"
        :key="tab.id"
        :class="tabClasses(tab)"
        :disabled="tab.disabled"
        @click="selectTab(tab.id)"
        role="tab"
        :aria-selected="isActive(tab.id)"
        type="button"
      >
        <span v-if="tab.icon" class="inline-flex items-center">{{ tab.icon }}</span>
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <div class="p-4 flex-1">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TabsProps, Tab } from '@a2ui/core'

const props = withDefaults(defineProps<TabsProps>(), {
  position: 'top',
})

const emit = defineEmits<{
  (e: 'update:value', value: string): void
  (e: 'change', value: string): void
}>()

const classes = computed(() => [
  'a2ui-tabs flex flex-col w-full',
  `a2ui-tabs--${props.position}`,
])

const availableTabs = computed((): Tab[] => {
  return typeof props.tabs === 'object' && 'path' in props.tabs ? [] : props.tabs
})

const tabValue = computed({
  get: () => {
    return typeof props.value === 'object' && 'path' in props.value ? '' : props.value
  },
  set: (value) => {
    emit('update:value', value)
  },
})

const isActive = (tabId: string): boolean => {
  return tabValue.value === tabId
}

const tabClasses = (tab: Tab) => [
  'a2ui-tabs__tab',
  {
    'a2ui-tabs__tab--active': isActive(tab.id),
    'opacity-50 cursor-not-allowed': tab.disabled,
  },
]

const selectTab = (tabId: string) => {
  const tab = availableTabs.value.find((t) => t.id === tabId)
  if (tab && !tab.disabled) {
    emit('update:value', tabId)
    emit('change', tabId)
  }
}
</script>

<style scoped>
.a2ui-tabs--left,
.a2ui-tabs--right {
  flex-direction: row;
}

.a2ui-tabs--left .a2ui-tabs__list,
.a2ui-tabs--right .a2ui-tabs__list {
  flex-direction: column;
  border-bottom: none;
  border-right: 1px solid #e0e0e0;
}

.a2ui-tabs--right .a2ui-tabs__list {
  border-right: none;
  border-left: 1px solid #e0e0e0;
  order: 1;
}
</style>
