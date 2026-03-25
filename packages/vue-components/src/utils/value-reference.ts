/**
 * ValueReference 处理工具
 *
 * A2UI 协议使用 ValueReference 来支持静态值和动态数据绑定
 */

import type { ValueReference } from '@a2ui/core'

/**
 * 检查是否为 ValueReference (动态数据路径)
 */
export function isValueReference(value: unknown): value is ValueReference<unknown> {
  return typeof value === 'object' && value !== null && 'path' in value
}

/**
 * 解析 ValueReference 或静态值
 *
 * @param value - 可能是静态值或 ValueReference
 * @param defaultValue - 当是 ValueReference 时的默认值（因为动态值需要框架处理）
 * @returns 解析后的值
 */
export function resolveValue<T>(value: T | ValueReference<T>, defaultValue: T): T {
  if (isValueReference(value)) {
    // 动态值需要通过 DataModel 处理，这里返回默认值
    // 实际的动态绑定由 useDataModel composable 处理
    return defaultValue
  }
  return value
}

/**
 * 解析字符串值
 */
export function resolveStringValue(value: string | ValueReference<string>, defaultValue = ''): string {
  return resolveValue(value, defaultValue)
}

/**
 * 解析布尔值
 */
export function resolveBooleanValue(value: boolean | ValueReference<boolean>, defaultValue = false): boolean {
  return resolveValue(value, defaultValue)
}

/**
 * 解析数值
 */
export function resolveNumberValue(value: number | ValueReference<number>, defaultValue = 0): number {
  return resolveValue(value, defaultValue)
}

/**
 * 解析数组值
 */
export function resolveArrayValue<T>(value: T[] | ValueReference<T[]>, defaultValue: T[] = []): T[] {
  return resolveValue(value, defaultValue)
}

/**
 * 创建响应式值绑定
 *
 * 用于需要双向绑定的场景，如表单输入
 */
export function useValueBinding<T>(
  props: { value: T | ValueReference<T> },
  emit: (event: 'update:value', value: T) => void,
  defaultValue: T
) {
  const currentValue = computed({
    get: () => resolveValue(props.value, defaultValue),
    set: (val: T) => emit('update:value', val),
  })

  return currentValue
}

// 需要从 vue 导入 computed
import { computed } from 'vue'
