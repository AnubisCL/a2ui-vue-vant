/**
 * A2UI Data Model Store Types
 */

/**
 * JSON Pointer path (RFC 6901)
 */
export type JSONPointer = string

/**
 * Data model store interface
 */
export interface DataModelStore {
  /** Get a value by JSON Pointer path */
  get(path: JSONPointer): unknown

  /** Set a value by JSON Pointer path */
  set(path: JSONPointer, value: unknown): void

  /** Update multiple values */
  update(updates: Record<JSONPointer, unknown>): void

  /** Delete a value by JSON Pointer path */
  delete(path: JSONPointer): void

  /** Check if a path exists */
  has(path: JSONPointer): boolean

  /** Get all data */
  getData(): Record<string, unknown>

  /** Reset the store */
  reset(data?: Record<string, unknown>): void

  /** Subscribe to changes */
  subscribe(callback: DataModelCallback): () => void

  /** Clear all subscriptions */
  clearSubscriptions(): void
}

/**
 * Data model change event
 */
export interface DataModelChangeEvent {
  /** Path that changed */
  path: JSONPointer

  /** New value */
  value: unknown

  /** Old value (if available) */
  oldValue?: unknown

  /** Type of change */
  type: 'set' | 'delete'
}

/**
 * Data model change callback
 */
export type DataModelCallback = (event: DataModelChangeEvent) => void
