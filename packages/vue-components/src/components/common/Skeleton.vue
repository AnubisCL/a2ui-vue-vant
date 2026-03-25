<template>
  <div class="a2ui-skeleton" :class="classes">
    <div v-if="avatar" class="a2ui-skeleton__avatar rounded-full animate-pulse" :style="avatarStyle" />
    <div class="a2ui-skeleton__content flex-1" :class="{ 'ml-3': avatar }">
      <div
        v-if="title"
        class="a2ui-skeleton__title h-4 animate-pulse mb-2 rounded"
        :style="titleStyle"
      />
      <div
        v-for="(width, index) in paragraphLines"
        :key="index"
        class="a2ui-skeleton__paragraph h-3 animate-pulse mb-2 rounded"
        :style="{ width: typeof width === 'number' ? `${width}%` : width }"
      />
    </div>
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
  titleWidth: '40%',
  paragraph: true,
  paragraphRows: 3,
  paragraphWidth: [100, 80, 60],
  loading: true,
})

const classes = computed(() => [])

const avatarStyle = computed(() => {
  if (typeof props.avatarSize === 'number') {
    return {
      width: `${props.avatarSize}px`,
      height: `${props.avatarSize}px`,
      background: 'linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%)',
      backgroundSize: '200% 100%',
      animation: 'skeleton-shimmer 1.5s infinite',
    }
  }

  const sizeMap = {
    small: 32,
    medium: 40,
    large: 48,
  }

  const size = sizeMap[props.avatarSize] || 40
  return {
    width: `${size}px`,
    height: `${size}px`,
    background: 'linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%)',
    backgroundSize: '200% 100%',
    animation: 'skeleton-shimmer 1.5s infinite',
  }
})

const titleStyle = computed(() => ({
  width: typeof props.titleWidth === 'number' ? `${props.titleWidth}%` : props.titleWidth,
  background: 'linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%)',
  backgroundSize: '200% 100%',
  animation: 'skeleton-shimmer 1.5s infinite',
}))

const paragraphLines = computed(() => {
  if (!props.paragraph) return []

  const widths = Array.isArray(props.paragraphWidth)
    ? props.paragraphWidth.map(w => typeof w === 'number' ? `${w}%` : w)
    : [typeof props.paragraphWidth === 'number' ? `${props.paragraphWidth}%` : props.paragraphWidth]

  const lines: string[] = []
  for (let i = 0; i < props.paragraphRows; i++) {
    lines.push(widths[i % widths.length])
  }

  return lines
})
</script>

<style scoped>
.a2ui-skeleton {
  display: flex;
  align-items: flex-start;
}

.a2ui-skeleton__avatar {
  flex-shrink: 0;
}

.a2ui-skeleton__content {
  overflow: hidden;
}

.a2ui-skeleton__paragraph:last-child {
  margin-bottom: 0;
}

.a2ui-skeleton__paragraph {
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s infinite;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
