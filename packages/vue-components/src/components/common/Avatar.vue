<template>
  <div class="a2ui-avatar inline-flex items-center justify-center font-semibold transition-all duration-200 overflow-hidden select-none" :class="classes" :style="style">
    <img v-if="src && !imageError" :src="src" :alt="alt" @error="handleImageError" class="a2ui-avatar__image w-full h-full object-cover" />
    <slot v-else>
      <span class="a2ui-avatar__text">{{ initials }}</span>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

export interface AvatarProps {
  src?: string
  alt?: string
  size?: number | 'small' | 'medium' | 'large' | 'xlarge'
  shape?: 'circle' | 'square'
  name?: string
  color?: string
}

const props = withDefaults(defineProps<AvatarProps>(), {
  size: 'medium',
  shape: 'circle',
})

const imageError = ref(false)

const classes = computed(() => [
  `a2ui-avatar--${props.shape}`,
])

const style = computed(() => {
  let sizeStyle = {}

  if (typeof props.size === 'number') {
    sizeStyle = {
      width: `${props.size}px`,
      height: `${props.size}px`,
      fontSize: `${props.size * 0.4}px`,
    }
  }

  const colorStyle = props.color ? { backgroundColor: props.color } : {}

  return { ...sizeStyle, ...colorStyle }
})

const initials = computed(() => {
  if (!props.name) return '?'
  const parts = props.name.split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return props.name.slice(0, 2).toUpperCase()
})

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped>
.a2ui-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
  flex-shrink: 0;
}

.a2ui-avatar--circle {
  border-radius: 50%;
}

.a2ui-avatar--square {
  border-radius: 12px;
}

.a2ui-avatar--small {
  width: 28px;
  height: 28px;
  font-size: 11px;
}

.a2ui-avatar--medium {
  width: 40px;
  height: 40px;
  font-size: 15px;
}

.a2ui-avatar--large {
  width: 48px;
  height: 48px;
  font-size: 18px;
}

.a2ui-avatar--xlarge {
  width: 64px;
  height: 64px;
  font-size: 24px;
}
</style>
