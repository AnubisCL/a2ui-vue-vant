<template>
  <div class="a2ui-audio-player">
    <audio
      ref="audioRef"
      class="a2ui-audio w-full"
      :autoplay="props.autoplay"
      :loop="props.loop"
      :controls="true"
      preload="metadata"
    >
      <source :src="audioSrc" :type="audioType" />
      您的浏览器不支持音频播放
    </audio>

    <!-- 自定义播放器 UI (可选) -->
    <div v-if="showCustomUI" class="audio-custom-controls mt-2">
      <van-slider
        v-model="progress"
        :max="100"
        :step="0.1"
        active-color="#1989fa"
        @change="handleSeek"
      />
      <div class="flex justify-between text-xs text-neutral-500 mt-1">
        <span>{{ formatTime(currentTime) }}</span>
        <span>{{ formatTime(duration) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import type { AudioPlayerProps } from '@a2ui/core'
import { resolveStringValue } from '../../utils'

const props = withDefaults(defineProps<AudioPlayerProps>(), {
  autoplay: false,
  loop: false,
  showCustomUI: false,
})

const audioRef = ref<HTMLAudioElement>()
const currentTime = ref(0)
const duration = ref(0)
const progress = ref(0)

// 解析音频源
const audioSrc = computed(() => {
  return resolveStringValue(props.src, '')
})

// 推断音频 MIME 类型
const audioType = computed(() => {
  const src = audioSrc.value
  if (src.endsWith('.mp3')) return 'audio/mpeg'
  if (src.endsWith('.wav')) return 'audio/wav'
  if (src.endsWith('.ogg')) return 'audio/ogg'
  if (src.endsWith('.m4a')) return 'audio/mp4'
  if (src.endsWith('.flac')) return 'audio/flac'
  return 'audio/mpeg'
})

// 格式化时间
const formatTime = (seconds: number): string => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 处理音频事件
const handleTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
    duration.value = audioRef.value.duration || 0
    progress.value = (currentTime.value / duration.value) * 100 || 0
  }
}

const handleLoadedMetadata = () => {
  if (audioRef.value) {
    duration.value = audioRef.value.duration
  }
}

// 处理 seek
const handleSeek = (value: number) => {
  if (audioRef.value && duration.value) {
    audioRef.value.currentTime = (value / 100) * duration.value
  }
}

// 添加事件监听
onMounted(() => {
  if (audioRef.value) {
    audioRef.value.addEventListener('timeupdate', handleTimeUpdate)
    audioRef.value.addEventListener('loadedmetadata', handleLoadedMetadata)
  }
})

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.removeEventListener('timeupdate', handleTimeUpdate)
    audioRef.value.removeEventListener('loadedmetadata', handleLoadedMetadata)
  }
})

// 暴露方法
defineExpose({
  play: () => audioRef.value?.play(),
  pause: () => audioRef.value?.pause(),
  stop: () => {
    if (audioRef.value) {
      audioRef.value.pause()
      audioRef.value.currentTime = 0
    }
  },
  seek: (time: number) => {
    if (audioRef.value) {
      audioRef.value.currentTime = time
    }
  },
})
</script>

<style scoped>
.a2ui-audio-player {
  width: 100%;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.a2ui-audio {
  width: 100%;
  height: 40px;
}

.audio-custom-controls {
  padding: 8px 0;
}
</style>
