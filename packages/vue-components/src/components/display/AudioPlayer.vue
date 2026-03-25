<template>
  <audio class="a2ui-audio" controls>
    <source :src="audioSrc" type="audio/mpeg" />
    Your browser does not support the audio tag.
  </audio>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AudioPlayerProps } from '@a2ui/core'

const props = withDefaults(defineProps<AudioPlayerProps>(), {
  autoplay: false,
  loop: false,
})

const audioSrc = computed(() => {
  return typeof props.src === 'object' && 'path' in props.src ? '' : props.src
})
</script>

<style scoped>
.a2ui-audio {
  width: 100%;
  max-width: 500px;
}
</style>
