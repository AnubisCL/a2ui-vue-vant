/**
 * A2UI Data Model Store
 *
 * Implements a reactive data model store with JSON Pointer path support
 */

import type {
  DataModelCallback,
  DataModelChangeEvent,
  DataModelStore,
  JSONPointer,
} from './store'
import {
  resolvePointer,
  setPointer,
  deletePointer,
  validatePointer,
} from './json-pointer'

/**
 * Data Model Store Implementation
 */
export class DataModelStoreImpl implements DataModelStore {
  private data: Record<string, unknown> = {}
  private subscriptions = new Set<DataModelCallback>()
  private snapshotEnabled = false

  constructor(initialData?: Record<string, unknown>) {
    if (initialData) {
      this.data = { ...initialData }
    }
  }

  /**
   * Get a value by JSON Pointer path
   */
  get(path: JSONPointer): unknown {
    if (!validatePointer(path)) {
      throw new Error(`Invalid JSON Pointer: "${path}"`)
    }

    try {
      return resolvePointer(this.data, path)
    } catch (error) {
      // Path doesn't exist
      return undefined
    }
  }

  /**
   * Set a value by JSON Pointer path
   */
  set(path: JSONPointer, value: unknown): void {
    if (!validatePointer(path)) {
      throw new Error(`Invalid JSON Pointer: "${path}"`)
    }

    // Get old value if snapshot is enabled
    const oldValue = this.snapshotEnabled ? this.get(path) : undefined

    try {
      setPointer(this.data, path, value)
    } catch (error) {
      throw new Error(
        `Failed to set value at "${path}": ${error instanceof Error ? error.message : String(error)}`
      )
    }

    // Emit change event
    this.emitChange({
      path,
      value,
      oldValue,
      type: 'set',
    })
  }

  /**
   * Update multiple values
   */
  update(updates: Record<JSONPointer, unknown>): void {
    for (const [path, value] of Object.entries(updates)) {
      this.set(path as JSONPointer, value)
    }
  }

  /**
   * Delete a value by JSON Pointer path
   */
  delete(path: JSONPointer): void {
    if (!validatePointer(path)) {
      throw new Error(`Invalid JSON Pointer: "${path}"`)
    }

    // Get old value if snapshot is enabled
    const oldValue = this.snapshotEnabled ? this.get(path) : undefined

    try {
      deletePointer(this.data, path)
    } catch (error) {
      throw new Error(
        `Failed to delete value at "${path}": ${error instanceof Error ? error.message : String(error)}`
      )
    }

    // Emit change event
    this.emitChange({
      path,
      value: undefined,
      oldValue,
      type: 'delete',
    })
  }

  /**
   * Check if a path exists
   */
  has(path: JSONPointer): boolean {
    if (!validatePointer(path)) {
      return false
    }

    try {
      const value = resolvePointer(this.data, path)
      return value !== undefined
    } catch {
      return false
    }
  }

  /**
   * Get all data
   */
  getData(): Record<string, unknown> {
    return { ...this.data }
  }

  /**
   * Reset the store
   */
  reset(data?: Record<string, unknown>): void {
    // Clear all existing data
    for (const key of Object.keys(this.data)) {
      delete this.data[key]
    }

    // Set new data
    if (data) {
      Object.assign(this.data, data)
    }
  }

  /**
   * Subscribe to changes
   */
  subscribe(callback: DataModelCallback): () => void {
    this.subscriptions.add(callback)
    return () => this.subscriptions.delete(callback)
  }

  /**
   * Clear all subscriptions
   */
  clearSubscriptions(): void {
    this.subscriptions.clear()
  }

  /**
   * Enable/disable value snapshots (tracks old values)
   */
  setSnapshotEnabled(enabled: boolean): void {
    this.snapshotEnabled = enabled
  }

  /**
   * Emit a change event
   */
  private emitChange(event: DataModelChangeEvent): void {
    this.subscriptions.forEach((callback) => {
      try {
        callback(event)
      } catch (error) {
        console.error('Error in data model subscription:', error)
      }
    })
  }
}

/**
 * Scoped Path Resolver for template-based dynamic children
 */
export class ScopedPathResolver {
  private store: DataModelStore

  constructor(store: DataModelStore) {
    this.store = store
  }

  /**
   * Resolve a scoped path
   * If scope is provided, the path is resolved relative to that scope
   */
  resolve(data: Record<string, unknown>, path: string, scope?: string): unknown {
    const fullPath = this.buildScopedPath(path, scope)
    return this.store.get(fullPath)
  }

  /**
   * Set a value at a scoped path
   */
  set(data: Record<string, unknown>, path: string, value: unknown, scope?: string): void {
    const fullPath = this.buildScopedPath(path, scope)
    this.store.set(fullPath, value)
  }

  /**
   * Build a full path from a relative path and scope
   */
  private buildScopedPath(path: string, scope?: string): string {
    if (!scope) {
      return path
    }

    // If path is already absolute (starts with /), return as-is
    if (path.startsWith('/')) {
      return path
    }

    // Build scoped path: /scope/relative/path
    return `/${scope}/${path}`.replace(/\/+/g, '/')
  }
}

/**
 * Create a new data model store
 */
export function createDataModelStore(initialData?: Record<string, unknown>): DataModelStore {
  return new DataModelStoreImpl(initialData)
}

/**
 * Create a scoped path resolver
 */
export function createScopedPathResolver(store: DataModelStore): ScopedPathResolver {
  return new ScopedPathResolver(store)
}
