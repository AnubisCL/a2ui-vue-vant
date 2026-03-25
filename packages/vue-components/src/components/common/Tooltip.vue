<template>
  <div class="a2ui-tooltip relative inline-block" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
    <slot />
    <Transition name="a2ui-tooltip">
      <div
        v-if="visible"
        class="a2ui-tooltip__popper absolute z-50 rounded-lg px-3 py-2 text-xs font-medium text-white whitespace-nowrap shadow-xl"
        :class="popperClasses"
        :style="popperStyle"
      >
        {{ content }}
        <div class="a2ui-tooltip__arrow absolute w-2 h-2" :class="arrowClasses" :style="arrowStyle" />
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

export interface TooltipProps {
  content: string
  placement?: 'top' | 'bottom' | 'left' | 'right'
  delay?: number
}

const props = withDefaults(defineProps<TooltipProps>(), {
  placement: 'top',
  delay: 200,
})

const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const popperClasses = computed(() => [
  `a2ui-tooltip__popper--${props.placement}`,
])

const popperStyle = computed(() => {
  const styles: Record<string, string> = {
    backgroundColor: '#1f2937',
  }

  switch (props.placement) {
    case 'top':
      styles.bottom = 'calc(100% + 8px)'
      styles.left = '50%'
      styles.transform = 'translateX(-50%)'
      break
    case 'bottom':
      styles.top = 'calc(100% + 8px)'
      styles.left = '50%'
      styles.transform = 'translateX(-50%)'
      break
    case 'left':
      styles.right = 'calc(100% + 8px)'
      styles.top = '50%'
      styles.transform = 'translateY(-50%)'
      break
    case 'right':
      styles.left = 'calc(100% + 8px)'
      styles.top = '50%'
      styles.transform = 'translateY(-50%)'
      break
  }

  return styles
})

const arrowClasses = computed(() => [
  `a2ui-tooltip__arrow--${props.placement}`,
])

const arrowStyle = computed(() => {
  const styles: Record<string, string> = {
    backgroundColor: '#1f2937',
  }

  switch (props.placement) {
    case 'top':
      styles.bottom = '-5px'
      styles.left = '50%'
      styles.transform = 'translateX(-50%) rotate(45deg)'
      break
    case 'bottom':
      styles.top = '-5px'
      styles.left = '50%'
      styles.transform = 'translateX(-50%) rotate(45deg)'
      break
    case 'left':
      styles.right = '-5px'
      styles.top = '50%'
      styles.transform = 'translateY(-50%) rotate(45deg)'
      break
    case 'right':
      styles.left = '-5px'
      styles.top = '50%'
      styles.transform = 'translateY(-50%) rotate(45deg)'
      break
  }

  return styles
})

const handleMouseEnter = () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    visible.value = true
  }, props.delay)
}

const handleMouseLeave = () => {
  if (timer) clearTimeout(timer)
  visible.value = false
}
</script>

<style scoped>
.a2ui-tooltip__popper {
  pointer-events: none;
  backdrop-filter: blur(8px);
}

.a2ui-tooltip-fade-enter-active,
.a2ui-tooltip-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.a2ui-tooltip-fade-enter-from,
.a2ui-tooltip-fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(4px);
}

.a2ui-tooltip__arrow {
  box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}
</style>
