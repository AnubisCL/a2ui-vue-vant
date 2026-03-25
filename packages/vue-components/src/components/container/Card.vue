<template>
  <van-cell-group
    :inset="props.elevated"
    :class="cardClasses"
    :style="cardStyles"
  >
    <!-- 标题区域 -->
    <template v-if="cardTitle || cardSubtitle">
      <van-cell :center="true" :border="false">
        <template #title>
          <div class="font-semibold text-base">{{ cardTitle }}</div>
          <div v-if="cardSubtitle" class="text-xs text-neutral-500 mt-1">
            {{ cardSubtitle }}
          </div>
        </template>
      </van-cell>
    </template>

    <!-- 内容区域 -->
    <div class="card-content">
      <slot>{{ cardContent }}</slot>
    </div>

    <!-- 操作区域 -->
    <van-cell-group v-if="hasActions" :border="false">
      <van-cell :center="true">
        <template #value>
          <van-space>
            <van-button
              v-for="(action, index) in cardActions"
              :key="index"
              :type="action.primary ? 'primary' : 'default'"
              size="small"
              @click="handleAction(action)"
            >
              {{ action.label }}
            </van-button>
          </van-space>
        </template>
      </van-cell>
    </van-cell-group>
  </van-cell-group>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CardProps, CardAction } from '@a2ui/core'
import { resolveStringValue, resolveArrayValue } from '../../utils'

const props = withDefaults(defineProps<CardProps>(), {
  elevated: false,
  borderRadius: 8,
  padding: 16,
})

const emit = defineEmits<{
  (e: 'action', action: string): void
}>()

// 卡片类名
const cardClasses = computed(() => [
  'a2ui-card',
  { 'a2ui-card--elevated': props.elevated },
])

// 卡片样式
const cardStyles = computed(() => ({
  borderRadius: `${props.borderRadius}px`,
  padding: `${props.padding}px`,
}))

// 解析标题
const cardTitle = computed(() => {
  return resolveStringValue(props.title, '')
})

// 解析副标题
const cardSubtitle = computed(() => {
  return resolveStringValue(props.subtitle, '')
})

// 解析内容
const cardContent = computed(() => {
  return resolveStringValue(props.content, '')
})

// 解析操作
const cardActions = computed((): CardAction[] => {
  return resolveArrayValue(props.actions, [])
})

// 是否有操作
const hasActions = computed(() => cardActions.value.length > 0)

// 处理操作点击
const handleAction = (action: CardAction) => {
  emit('action', action.action)
}
</script>

<style scoped>
.a2ui-card {
  background: #fff;
  margin: 12px;
}

.card-content {
  padding: 12px 0;
}
</style>
