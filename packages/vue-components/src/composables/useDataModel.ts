/**
 * useDataModel Composable
 *
 * Provides access to the data model for a surface
 */

import { ref, type Ref } from 'vue'
import type { DataModelStore, JSONPointer } from '@a2ui/core'

export interface UseDataModelOptions {
  surfaceId: string
}

export interface UseDataModelReturn {
  data: Ref<Record<string, unknown>>
  get: (path: JSONPointer) => unknown
  set: (path: JSONPointer, value: unknown) => void
  update: (updates: Record<JSONPointer, unknown>) => void
  delete: (path: JSONPointer) => void
  has: (path: JSONPointer) => boolean
  reset: (data?: Record<string, unknown>) => void
  subscribe: (callback: (event: any) => void) => () => void
}

/**
 * Composable for working with the data model
 */
export function useDataModel(
  store: DataModelStore,
  options: UseDataModelOptions
): UseDataModelReturn {
  const data = ref(store.getData()) as Ref<Record<string, unknown>>

  const get = (path: JSONPointer): unknown => {
    return store.get(path)
  }

  const set = (path: JSONPointer, value: unknown): void => {
    store.set(path, value)
    data.value = store.getData()
  }

  const update = (updates: Record<JSONPointer, unknown>): void => {
    store.update(updates)
    data.value = store.getData()
  }

  const deletePath = (path: JSONPointer): void => {
    store.delete(path)
    data.value = store.getData()
  }

  const has = (path: JSONPointer): boolean => {
    return store.has(path)
  }

  const reset = (newData?: Record<string, unknown>): void => {
    store.reset(newData)
    data.value = store.getData()
  }

  const subscribe = (callback: (event: any) => void): (() => void) => {
    return store.subscribe((event) => {
      data.value = store.getData()
      callback(event)
    })
  }

  return {
    data,
    get,
    set,
    update,
    delete: deletePath,
    has,
    reset,
    subscribe,
  }
}
