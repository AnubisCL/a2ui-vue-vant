<template>
  <van-tabs
    v-model:active="activeTab"
    :sticky="false"
    :swipeable="true"
    :animated="true"
    :tab-position="vantPosition"
    @change="handleTabChange"
  >
    <van-tab
      v-for="tab in availableTabs"
      :key="tab.id"
      :name="tab.id"
      :disabled="tab.disabled"
      :title="tab.label"
    >
      <template #title>
        <div class="flex items-center gap-1">
          <van-icon v-if="tab.icon" :name="tab.icon" size="16" />
          <span>{{ tab.label }}</span>
        </div>
      </template>
      <div class="p-4">
        <slot :name="tab.id">
          <slot />
        </slot>
      </div>
    </van-tab>
  </van-tabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TabsProps, Tab } from '@a2ui/core'
import { resolveStringValue, resolveArrayValue } from '../../utils'

const props = withDefaults(defineProps<TabsProps>(), {
  position: 'top',
})

const emit = defineEmits<{
  (e: 'update:value', value: string): void
  (e: 'change', value: string): void
}>()

// 解析选项卡列表
const availableTabs = computed((): Tab[] => {
  return resolveArrayValue(props.tabs, [])
})

// 当前激活的选项卡
const activeTab = computed({
  get: () => {
    if (typeof props.value === 'object' && 'path' in props.value) {
      return availableTabs.value[0]?.id || ''
    }
    return props.value
  },
  set: (value: string) => {
    emit('update:value', value)
  },
})

// 映射位置到 Vant
const vantPosition = computed(() => {
  const map: Record<string, 'top' | 'bottom' | 'left' | 'right'> = {
    top: 'top',
    bottom: 'bottom',
    left: 'left',
    right: 'right',
  }
  return map[props.position] || 'top'
})

// 处理选项卡切换
const handleTabChange = ({ name }: { name: string | number }) => {
  emit('change', String(name))
}
</script>

<style scoped>
.van-tabs {
  width: 100%;
}
</style>
