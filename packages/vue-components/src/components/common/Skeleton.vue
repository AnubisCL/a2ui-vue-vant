<template>
  <van-skeleton
    v-if="loading !== false"
    :title="props.title"
    :avatar="props.avatar"
    :avatar-size="vantAvatarSize"
    :row="props.paragraphRows || 3"
    :animate="true"
    :avatar-shape="'round'"
  >
    <template v-if="$slots.default" #default>
      <slot />
    </template>
  </van-skeleton>
  <div v-else>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface SkeletonProps {
  avatar?: boolean
  avatarSize?: number | 'small' | 'medium' | 'large'
  title?: boolean
  titleWidth?: number | string
  paragraph?: boolean
  paragraphRows?: number
  paragraphWidth?: number | string | Array<number | string>
  loading?: boolean
}

const props = withDefaults(defineProps<SkeletonProps>(), {
  avatar: false,
  avatarSize: 'medium',
  title: true,
  paragraphRows: 3,
  loading: true,
})

// 映射头像尺寸
const vantAvatarSize = computed(() => {
  if (typeof props.avatarSize === 'number') {
    return props.avatarSize
  }

  const sizeMap: Record<string, number> = {
    small: 32,
    medium: 40,
    large: 48,
  }

  return sizeMap[props.avatarSize] || 40
})
</script>
