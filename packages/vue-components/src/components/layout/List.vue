<template>
  <van-list
    v-model:loading="loading"
    :finished="finished"
    :immediate-check="false"
    @load="handleLoad"
  >
    <div class="a2ui-list" :style="listStyle">
      <slot />
    </div>
  </van-list>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ListProps } from '@a2ui/core'

const props = withDefaults(defineProps<ListProps>(), {
  gap: 8,
  scrollable: true,
})

const emit = defineEmits<{
  (e: 'load'): void
  (e: 'itemRender', index: number, item: unknown): void
}>()

// 加载状态
const loading = ref(false)
const finished = ref(false)

// 列表样式
const listStyle = computed(() => ({
  gap: `${props.gap}px`,
  overflow: props.scrollable ? 'auto' : 'visible',
  maxHeight: props.scrollable ? '100%' : 'none',
}))

// 加载更多
const handleLoad = () => {
  emit('load')
  // 由父组件控制 loading 和 finished 状态
}

// 暴露方法
defineExpose({
  setLoading: (value: boolean) => {
    loading.value = value
  },
  setFinished: (value: boolean) => {
    finished.value = value
  },
})
</script>

<style scoped>
.a2ui-list {
  display: flex;
  flex-direction: column;
  width: 100%;
}
</style>
