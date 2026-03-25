<template>
  <Teleport to="body">
    <Transition name="a2ui-modal">
      <div v-if="isOpen" class="a2ui-modal--overlay" @click="handleBackdropClick">
        <div
          class="bg-white rounded shadow-xl max-w-[90vw] max-h-[90vh] flex flex-col"
          :style="contentStyles"
          @click.stop
          role="dialog"
          aria-modal="true"
        >
          <div class="flex justify-between items-center p-4 border-b border-neutral-200">
            <h2 v-if="title" class="text-xl font-semibold text-neutral-800 m-0">{{ modalTitle }}</h2>
            <button
              v-if="showClose"
              class="bg-none border-none text-xl cursor-pointer text-neutral-600 p-1 -my-1 hover:text-neutral-800 transition-colors leading-none"
              @click="close"
              type="button"
              aria-label="Close"
            >
              ✕
            </button>
          </div>

          <div class="p-5 overflow-y-auto flex-1">
            <slot>{{ modalContent }}</slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import type { ModalProps } from '@a2ui/core'

const props = withDefaults(defineProps<ModalProps>(), {
  showClose: true,
  closeOnBackdropClick: true,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'close'): void
}>()

const contentStyles = computed(() => {
  const styles: Record<string, string> = {}

  if (props.width) {
    styles.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }

  if (props.height) {
    styles.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }

  return styles
})

const isOpen = computed({
  get: () => {
    return typeof props.open === 'object' && 'path' in props.open ? false : props.open
  },
  set: (value) => {
    emit('update:open', value)
  },
})

const modalTitle = computed(() => {
  return typeof props.title === 'object' && 'path' in props.title ? '' : props.title
})

const modalContent = computed(() => {
  return typeof props.content === 'object' && 'path' in props.content ? '' : props.content
})

const close = () => {
  isOpen.value = false
  emit('close')
}

const handleBackdropClick = () => {
  if (props.closeOnBackdropClick) {
    close()
  }
}

// Handle escape key
watch(isOpen, (newValue) => {
  if (newValue) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    close()
  }
}
</script>

<style scoped>
/* Transition */
.a2ui-modal-enter-active,
.a2ui-modal-leave-active {
  transition: opacity 0.3s ease;
}

.a2ui-modal-enter-active > div,
.a2ui-modal-leave-active > div {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.a2ui-modal-enter-from,
.a2ui-modal-leave-to {
  opacity: 0;
}

.a2ui-modal-enter-from > div,
.a2ui-modal-leave-to > div {
  transform: scale(0.9);
  opacity: 0;
}
</style>
