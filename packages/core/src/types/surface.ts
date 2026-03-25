/**
 * A2UI Surface Types
 */

import type { ComponentId, ComponentUpdate, DataModelUpdate, SurfaceId } from './messages'
import type { DataModelStore } from './store'

/**
 * Surface state
 */
export interface Surface {
  /** Surface ID */
  id: SurfaceId

  /** Component tree */
  components: Map<ComponentId, ComponentUpdate>

  /** Data model */
  dataModel: DataModelStore

  /** Root component ID */
  rootId?: ComponentId

  /** Whether the surface is active */
  active: boolean
}

/**
 * Surface manager interface
 */
export interface SurfaceManager {
  /** Create a new surface */
  create(options: SurfaceCreateOptions): Surface

  /** Get a surface by ID */
  get(surfaceId: SurfaceId): Surface | undefined

  /** Check if a surface exists */
  has(surfaceId: SurfaceId): boolean

  /** Update surface components */
  updateComponents(surfaceId: SurfaceId, updates: ComponentUpdate[]): void

  /** Update surface data model */
  updateDataModel(surfaceId: SurfaceId, updates: DataModelUpdate): void

  /** Delete a surface */
  delete(surfaceId: SurfaceId): void

  /** Get all surface IDs */
  getSurfaceIds(): SurfaceId[]

  /** Clear all surfaces */
  clear(): void

  /** Subscribe to surface events */
  subscribe(callback: SurfaceEventCallback): () => void
}

/**
 * Surface creation options
 */
export interface SurfaceCreateOptions {
  /** Surface ID */
  id: SurfaceId

  /** Initial components */
  components?: ComponentUpdate[]

  /** Initial data model */
  dataModel?: Record<string, unknown>

  /** Root component ID */
  rootId?: ComponentId
}

/**
 * Surface event
 */
export interface SurfaceEvent {
  /** Event type */
  type: 'create' | 'update' | 'delete'

  /** Surface ID */
  surfaceId: SurfaceId

  /** Event data */
  data?: unknown
}

/**
 * Surface event callback
 */
export type SurfaceEventCallback = (event: SurfaceEvent) => void
