<template>
  <div class="a2ui-spinner inline-block" :class="classes" :style="style">
    <svg class="a2ui-spinner__svg animate-spin" viewBox="0 0 50 50">
      <circle
        class="a2ui-spinner__path"
        cx="25"
        cy="25"
        r="20"
        fill="none"
        :stroke="color"
        stroke-width="4"
        stroke-linecap="round"
        stroke-dasharray="80, 150"
        stroke-dashoffset="0"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface SpinnerProps {
  size?: number | 'small' | 'medium' | 'large' | 'xlarge'
  color?: string
}

const props = withDefaults(defineProps<SpinnerProps>(), {
  size: 'medium',
  color: '#3b82f6',
})

const classes = computed(() => [])

const style = computed(() => {
  if (typeof props.size === 'number') {
    return {
      width: `${props.size}px`,
      height: `${props.size}px`,
    }
  }
  return {}
})
</script>

<style scoped>
.a2ui-spinner {
  display: inline-block;
}

.a2ui-spinner--small {
  width: 16px;
  height: 16px;
}

.a2ui-spinner--medium {
  width: 24px;
  height: 24px;
}

.a2ui-spinner--large {
  width: 32px;
  height: 32px;
}

.a2ui-spinner--xlarge {
  width: 48px;
  height: 48px;
}

.a2ui-spinner__svg {
  width: 100%;
  height: 100%;
}

.a2ui-spinner__path {
  animation: spinner-dash 1.5s ease-in-out infinite;
  filter: drop-shadow(0 0 3px currentColor);
}

@keyframes spinner-dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}
</style>
