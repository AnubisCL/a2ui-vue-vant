<template>
  <div class="a2ui-video-wrapper">
    <video
      ref="videoRef"
      class="a2ui-video block w-full"
      :style="videoStyle"
      :autoplay="props.autoplay"
      :loop="props.loop"
      :muted="props.muted"
      :controls="props.controls !== false"
      playsinline
      webkit-playsinline
    >
      <source :src="videoSrc" :type="videoType" />
      您的浏览器不支持视频播放
    </video>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { VideoProps } from '@a2ui/core'
import { resolveStringValue } from '../../utils'

const props = withDefaults(defineProps<VideoProps>(), {
  autoplay: false,
  controls: true,
  loop: false,
  muted: false,
})

const videoRef = ref<HTMLVideoElement>()

// 解析视频源
const videoSrc = computed(() => {
  return resolveStringValue(props.src, '')
})

// 推断视频 MIME 类型
const videoType = computed(() => {
  const src = videoSrc.value
  if (src.endsWith('.mp4')) return 'video/mp4'
  if (src.endsWith('.webm')) return 'video/webm'
  if (src.endsWith('.ogg')) return 'video/ogg'
  if (src.endsWith('.mov')) return 'video/quicktime'
  return 'video/mp4'
})

// 视频样式
const videoStyle = computed(() => ({
  width: props.width ? (typeof props.width === 'number' ? `${props.width}px` : props.width) : '100%',
  height: props.height ? (typeof props.height === 'number' ? `${props.height}px` : props.height) : 'auto',
  borderRadius: '8px',
}))

// 暴露方法
defineExpose({
  play: () => videoRef.value?.play(),
  pause: () => videoRef.value?.pause(),
  stop: () => {
    if (videoRef.value) {
      videoRef.value.pause()
      videoRef.value.currentTime = 0
    }
  },
})
</script>

<style scoped>
.a2ui-video-wrapper {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 8px;
  background: #000;
}

.a2ui-video {
  display: block;
  width: 100%;
  max-width: 100%;
}
</style>
