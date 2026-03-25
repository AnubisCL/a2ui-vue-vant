/**
 * A2UI Surface
 *
 * Represents a single rendering surface with its own component tree and data model
 */

import type {
  SurfaceId,
  ComponentId,
  ComponentUpdate,
} from '../types'
import type { DataModelStore } from '../store'
import { createDataModelStore } from '../store'

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
 * Surface Implementation
 */
export class SurfaceImpl implements Surface {
  public readonly id: SurfaceId
  public readonly components: Map<ComponentId, ComponentUpdate>
  public readonly dataModel: DataModelStore
  public rootId?: ComponentId
  public active = true

  constructor(
    id: SurfaceId,
    initialData?: {
      components?: ComponentUpdate[]
      dataModel?: Record<string, unknown>
      rootId?: ComponentId
    }
  ) {
    this.id = id
    this.components = new Map()
    this.dataModel = createDataModelStore(initialData?.dataModel)

    // Initialize components if provided
    if (initialData?.components) {
      for (const component of initialData.components) {
        this.components.set(component.componentId, component)
      }
    }

    // Set root component
    if (initialData?.rootId) {
      this.rootId = initialData.rootId
    }
  }

  /**
   * Add or update a component
   */
  setComponent(component: ComponentUpdate): void {
    this.components.set(component.componentId, component)
  }

  /**
   * Remove a component
   */
  removeComponent(componentId: ComponentId): void {
    this.components.delete(componentId)
  }

  /**
   * Get a component by ID
   */
  getComponent(componentId: ComponentId): ComponentUpdate | undefined {
    return this.components.get(componentId)
  }

  /**
   * Check if a component exists
   */
  hasComponent(componentId: ComponentId): boolean {
    return this.components.has(componentId)
  }

  /**
   * Get all component IDs
   */
  getComponentIds(): ComponentId[] {
    return Array.from(this.components.keys())
  }

  /**
   * Set the root component ID
   */
  setRoot(rootId: ComponentId): void {
    if (!this.components.has(rootId)) {
      throw new Error(`Cannot set root: component "${rootId}" does not exist`)
    }
    this.rootId = rootId
  }

  /**
   * Clear all components
   */
  clearComponents(): void {
    this.components.clear()
    this.rootId = undefined
  }

  /**
   * Deactivate the surface
   */
  deactivate(): void {
    this.active = false
  }

  /**
   * Activate the surface
   */
  activate(): void {
    this.active = true
  }

  /**
   * Check if the surface is active
   */
  isActive(): boolean {
    return this.active
  }
}

/**
 * Create a new surface
 */
export function createSurface(
  id: string,
  initialData?: {
    components?: ComponentUpdate[]
    dataModel?: Record<string, unknown>
    rootId?: ComponentId
  }
): SurfaceImpl {
  return new SurfaceImpl(id, initialData)
}
