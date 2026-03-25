<template>
  <div class="a2ui-empty flex flex-col items-center justify-center p-8 text-center">
    <div class="a2ui-empty__image mb-5">
      <slot name="image">
        <div class="a2ui-empty__icon" :class="`a2ui-empty__icon--${imageType}`">
          <svg v-if="imageType === 'default'" width="120" height="120" viewBox="0 0 120 120" fill="none">
            <circle cx="60" cy="60" r="50" fill="url(#gradient1)" opacity="0.1"/>
            <path d="M45 55C45 49.4772 49.4772 45 55 45H65C70.5228 45 75 49.4772 75 55V65C75 70.5228 70.5228 75 65 75H55C49.4772 75 45 70.5228 45 65V55Z" fill="url(#gradient2)" opacity="0.2"/>
            <path d="M40 50H50M40 60H50M40 70H50M70 50H80M70 60H80M70 70H80" stroke="url(#gradient3)" stroke-width="2" stroke-linecap="round"/>
            <defs>
              <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#667eea"/>
                <stop offset="100%" style="stop-color:#764ba2"/>
              </linearGradient>
              <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.3"/>
                <stop offset="100%" style="stop-color:#764ba2;stop-opacity:0.3"/>
              </linearGradient>
              <linearGradient id="gradient3" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#667eea"/>
                <stop offset="100%" style="stop-color:#764ba2"/>
              </linearGradient>
            </defs>
          </svg>
          <span v-else-if="imageType === 'search'" class="text-6xl">🔍</span>
          <span v-else-if="imageType === 'network'" class="text-6xl">🌐</span>
          <span v-else-if="imageType === 'error'" class="text-6xl">⚠️</span>
        </div>
      </slot>
    </div>
    <div v-if="title" class="a2ui-empty__title text-lg font-semibold mb-2 text-gray-800">
      {{ title }}
    </div>
    <div v-if="description" class="a2ui-empty__description text-sm text-gray-500 mb-5 max-w-md">
      {{ description }}
    </div>
    <div v-if="$slots.default" class="a2ui-empty__actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
export interface EmptyProps {
  image?: string
  imageType?: 'default' | 'search' | 'network' | 'error'
  title?: string
  description?: string
}

defineProps<EmptyProps>()
</script>

<style scoped>
.a2ui-empty {
  min-height: 240px;
}

.a2ui-empty__image {
  opacity: 0.8;
  transition: transform 0.3s ease;
}

.a2ui-empty:hover .a2ui-empty__image {
  transform: scale(1.05);
}

.a2ui-empty__icon--default svg {
  filter: drop-shadow(0 4px 12px rgba(102, 126, 234, 0.2));
}

.a2ui-empty__title {
  font-weight: 600;
  color: #1f2937;
}

.a2ui-empty__description {
  color: #6b7280;
  line-height: 1.6;
}
</style>
