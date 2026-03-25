<template>
  <van-empty
    :image="imageType"
    :description="description || ''"
  >
    <template v-if="title" #description>
      <div class="text-center">
        <div class="text-base font-semibold text-neutral-800 mb-1">{{ title }}</div>
        <div v-if="description" class="text-sm text-neutral-500">{{ description }}</div>
      </div>
    </template>
    <slot />
  </van-empty>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface EmptyProps {
  image?: string
  imageType?: 'default' | 'error' | 'search' | 'network'
  title?: string
  description?: string
}

const props = withDefaults(defineProps<EmptyProps>(), {
  imageType: 'default',
})

// 映射图片类型到 Vant
const imageType = computed(() => {
  const imageMap: Record<string, 'default' | 'error' | 'search' | 'network'> = {
    default: 'default',
    error: 'error',
    search: 'search',
    network: 'network',
  }
  return imageMap[props.imageType] || 'default'
})
</script>
