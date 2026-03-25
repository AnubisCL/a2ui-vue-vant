<template>
  <van-icon
    :name="vantIconName"
    :size="iconSize"
    :color="props.color"
    :class="{ 'cursor-pointer': props.onClick }"
    @click="handleClick"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { IconProps } from '@a2ui/core'
import { resolveStringValue } from '../../utils'

const props = withDefaults(defineProps<IconProps>(), {
  size: 'medium',
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

// 解析图标名称
const iconName = computed(() => {
  return resolveStringValue(props.name, '')
})

// 映射 A2UI 图标名到 Vant 图标名
const vantIconName = computed(() => {
  const name = iconName.value

  // 映射常用图标名称
  const iconMap: Record<string, string> = {
    // 基础图标
    'check': 'success',
    'close': 'cross',
    'arrow-right': 'arrow',
    'arrow-left': 'arrow-left',
    'arrow-up': 'arrow-up',
    'arrow-down': 'arrow-down',

    // 操作图标
    'search': 'search',
    'settings': 'setting',
    'home': 'home-o',
    'user': 'user-o',
    'star': 'star-o',
    'heart': 'like-o',
    'trash': 'delete-o',
    'edit': 'edit',
    'plus': 'plus',
    'minus': 'minus',
    'info': 'info-o',
    'warning': 'warning-o',
    'error': 'cross',
    'success': 'success',

    // 其他常用图标
    'calendar': 'calendar-o',
    'clock': 'clock-o',
    'location': 'location-o',
    'phone': 'phone-o',
    'email': 'envelop-o',
    'share': 'share-o',
    'refresh': 'replay',
    'menu': 'bars',
    'more': 'ellipsis',
    'filter': 'filter-o',
    'sort': 'sort',
    'lock': 'lock',
    'unlock': 'unlock',
    'eye': 'eye-o',
    'eye-off': 'closed-eye',
    'copy': 'description',
    'download': 'down',
    'upload': 'upgrade',
    'link': 'link-o',
    'qr': 'qr',
    'scan': 'scan',
    'photo': 'photo-o',
    'video': 'video-o',
    'music': 'music-o',
    'file': 'description',
    'folder': 'folder-o',
    'chat': 'chat-o',
    'comment': 'comment-o',
    'notification': 'bell',
    'question': 'question-o',
    'fire': 'fire-o',
    'gift': 'gift-o',
    'coupon': 'coupon-o',
    'cart': 'shopping-cart-o',
    'shop': 'shop-o',
    'balance': 'balance-list-o',
    'cash': 'gold-coin-o',
    'vip': 'diamond-o',
  }

  return iconMap[name] || name
})

// 图标大小
const iconSize = computed(() => {
  if (typeof props.size === 'number') {
    return `${props.size}px`
  }

  const sizeMap: Record<string, string> = {
    small: '16',
    medium: '24',
    large: '32',
    xlarge: '48',
  }

  return sizeMap[props.size] || '24'
})

// 点击事件
const handleClick = (event: MouseEvent) => {
  if (props.onClick) {
    emit('click', event)
  }
}
</script>
