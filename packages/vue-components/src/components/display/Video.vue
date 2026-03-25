<template>
  <video class="a2ui-video block max-w-full" :class="classes" controls>
    <source :src="videoSrc" type="video/mp4" />
    Your browser does not support the video tag.
  </video>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { VideoProps } from '@a2ui/core'

const props = withDefaults(
  defineProps<VideoProps>(),
  {
    autoplay: false,
    controls: true,
    loop: false,
    muted: false,
  }
)

const classes = computed(() => [
  widthClass.value,
  heightClass.value,
])

const videoSrc = computed(() => {
  return typeof props.src === 'object' && 'path' in props.src ? '' : props.src
})

const widthClass = computed(() => {
  if (!props.width) return ''
  return typeof props.width === 'number' ? `w-[${props.width}px]` : `w-[${props.width}]`
})

const heightClass = computed(() => {
  if (!props.height) return ''
  return typeof props.height === 'number' ? `h-[${props.height}px]` : `h-[${props.height}]`
})
</script>
