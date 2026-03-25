/**
 * Vant Props 映射工具
 *
 * 将 A2UI Props 映射到 Vant 组件 Props
 */

import type {
  ButtonProps,
  TextFieldProps,
  CheckBoxProps,
  SliderProps,
  ChoicePickerProps,
  DateTimeInputProps,
  ImageProps,
  IconProps,
  DividerProps,
  CardProps,
  TabsProps,
  ModalProps,
} from '@a2ui/core'
import {
  resolveStringValue,
  resolveBooleanValue,
  resolveNumberValue,
  isValueReference,
} from './value-reference'

// ============================================
// Button Props 映射
// ============================================

export type VantButtonType = 'primary' | 'success' | 'default' | 'warning' | 'danger'
export type VantButtonSize = 'large' | 'normal' | 'small' | 'mini'

export function mapButtonVariant(variant?: string): VantButtonType {
  const map: Record<string, VantButtonType> = {
    primary: 'primary',
    secondary: 'default',
    borderless: 'default',
    danger: 'danger',
  }
  return map[variant || 'primary'] || 'primary'
}

export function mapButtonSize(size?: string): VantButtonSize {
  const map: Record<string, VantButtonSize> = {
    small: 'small',
    medium: 'normal',
    large: 'large',
  }
  return map[size || 'medium'] || 'normal'
}

export function mapButtonProps(props: ButtonProps) {
  return {
    type: mapButtonVariant(props.variant),
    size: mapButtonSize(props.size),
    disabled: resolveBooleanValue(props.disabled, false),
    icon: props.icon,
    loading: false,
    plain: props.variant === 'secondary' || props.variant === 'borderless',
    hairline: props.variant === 'borderless',
  }
}

// ============================================
// TextField Props 映射
// ============================================

export type VantFieldType = 'text' | 'tel' | 'digit' | 'number' | 'password' | 'textarea'

export function mapTextFieldType(type?: string): VantFieldType {
  const map: Record<string, VantFieldType> = {
    shortText: 'text',
    longText: 'textarea',
    number: 'digit',
    obscured: 'password',
  }
  return map[type || 'shortText'] || 'text'
}

export function mapTextFieldProps(props: TextFieldProps) {
  return {
    label: resolveStringValue(props.label, ''),
    placeholder: resolveStringValue(props.placeholder, ''),
    disabled: resolveBooleanValue(props.disabled, false),
    readonly: resolveBooleanValue(props.readonly, false),
    type: mapTextFieldType(props.type),
    maxlength: props.maxLength,
    formatter: props.type === 'number' ? (val: string) => val.replace(/\D/g, '') : undefined,
  }
}

// ============================================
// CheckBox Props 映射
// ============================================

export function mapCheckBoxProps(props: CheckBoxProps) {
  return {
    shape: 'square' as const,
    disabled: resolveBooleanValue(props.disabled, false),
  }
}

// ============================================
// Slider Props 映射
// ============================================

export function mapSliderProps(props: SliderProps) {
  return {
    min: props.min,
    max: props.max,
    step: props.step || 1,
    disabled: resolveBooleanValue(props.disabled, false),
    barHeight: 4,
    activeColor: '#1989fa',
    inactiveColor: '#e5e5e5',
  }
}

// ============================================
// ChoicePicker Props 映射
// ============================================

export function mapChoicePickerProps(props: ChoicePickerProps) {
  return {
    direction: 'vertical' as const,
    disabled: resolveBooleanValue(props.disabled, false),
  }
}

// ============================================
// DateTimeInput Props 映射
// ============================================

export type VantDatePickerType = 'date' | 'time' | 'year-month' | 'month-day' | 'datehour'

export function mapDateTimeType(type?: string): VantDatePickerType {
  const map: Record<string, VantDatePickerType> = {
    date: 'date',
    time: 'time',
    datetime: 'datehour',
  }
  return map[type || 'date'] || 'date'
}

export function mapDateTimeInputProps(props: DateTimeInputProps) {
  return {
    type: mapDateTimeType(props.type),
    title: resolveStringValue(props.label, '选择日期'),
    disabled: resolveBooleanValue(props.disabled, false),
    minDate: props.min ? new Date(props.min) : undefined,
    maxDate: props.max ? new Date(props.max) : undefined,
  }
}

// ============================================
// Image Props 映射
// ============================================

export function mapImageFit(fit?: string): 'contain' | 'cover' | 'fill' | 'none' | 'scale-down' {
  const validFits = ['contain', 'cover', 'fill', 'none', 'scale-down'] as const
  return validFits.includes(fit as any) ? (fit as any) : 'cover'
}

export function mapImageProps(props: ImageProps) {
  return {
    src: resolveStringValue(props.src, ''),
    alt: resolveStringValue(props.alt, ''),
    fit: mapImageFit(props.fit),
    width: props.width,
    height: props.height,
    radius: props.borderRadius,
    lazy: true,
  }
}

// ============================================
// Icon Props 映射
// ============================================

export function mapIconSize(size?: number | string): string | undefined {
  if (typeof size === 'number') {
    return `${size}px`
  }
  if (typeof size === 'string') {
    const map: Record<string, string> = {
      small: '16',
      medium: '24',
      large: '32',
      xlarge: '48',
    }
    return map[size] || size
  }
  return '24'
}

// ============================================
// Divider Props 映射
// ============================================

export function mapDividerProps(props: DividerProps) {
  return {
    dashed: false,
    hairline: true,
    contentPosition: 'center' as const,
  }
}

// ============================================
// Card Props 映射
// ============================================

export function mapCardProps(props: CardProps) {
  return {
    title: resolveStringValue(props.title, ''),
    thumb: '',
    centered: false,
  }
}

// ============================================
// Tabs Props 映射
// ============================================

export function mapTabPosition(position?: string): 'top' | 'bottom' | 'left' | 'right' {
  const validPositions = ['top', 'bottom', 'left', 'right'] as const
  return validPositions.includes(position as any) ? (position as any) : 'top'
}

export function mapTabsProps(props: TabsProps) {
  return {
    sticky: false,
    swipeable: true,
    animated: true,
    swipeThreshold: 4,
  }
}

// ============================================
// Modal Props 映射
// ============================================

export function mapModalProps(props: ModalProps) {
  return {
    show: resolveBooleanValue(props.open, false),
    round: true,
    closeable: props.showClose !== false,
    closeOnPopstate: true,
    closeOnClickOverlay: props.closeOnBackdropClick !== false,
    teleport: 'body',
    position: 'center' as const,
    style: {
      width: typeof props.width === 'number' ? `${props.width}px` : props.width || '80%',
      maxHeight: typeof props.height === 'number' ? `${props.height}px` : props.height || '80vh',
    },
  }
}

// ============================================
// 通用工具函数
// ============================================

/**
 * 获取标签文本
 */
export function getLabelText(label?: string | { path: string }): string {
  return resolveStringValue(label as any, '')
}

/**
 * 获取提示文本
 */
export function getHintText(hint?: string | { path: string }): string {
  return resolveStringValue(hint as any, '')
}
