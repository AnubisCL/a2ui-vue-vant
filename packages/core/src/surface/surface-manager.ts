/**
 * A2UI Surface Manager
 *
 * Manages multiple surfaces with isolated state
 */

import type {
  SurfaceManager as ISurfaceManager,
  SurfaceEvent,
  SurfaceEventCallback,
  SurfaceId,
  ComponentUpdate,
  DataModelUpdate,
  SurfaceCreateOptions,
} from '../types'
import { SurfaceImpl, createSurface } from './surface'

/**
 * Surface Manager Implementation
 */
export class SurfaceManager implements ISurfaceManager {
  private surfaces = new Map<SurfaceId, SurfaceImpl>()
  private subscriptions = new Set<SurfaceEventCallback>()

  /**
   * Create a new surface
   */
  create(options: SurfaceCreateOptions): SurfaceImpl {
    const { id, components, dataModel, rootId } = options

    // Check if surface already exists
    if (this.surfaces.has(id)) {
      throw new Error(`Surface "${id}" already exists`)
    }

    // Create the surface
    const surface = createSurface(id, { components, dataModel, rootId })
    this.surfaces.set(id, surface)

    // Emit create event
    this.emitEvent({
      type: 'create',
      surfaceId: id,
    })

    return surface
  }

  /**
   * Get a surface by ID
   */
  get(surfaceId: SurfaceId): SurfaceImpl | undefined {
    return this.surfaces.get(surfaceId)
  }

  /**
   * Check if a surface exists
   */
  has(surfaceId: SurfaceId): boolean {
    return this.surfaces.has(surfaceId)
  }

  /**
   * Update surface components
   */
  updateComponents(surfaceId: SurfaceId, updates: ComponentUpdate[]): void {
    const surface = this.surfaces.get(surfaceId)

    if (!surface) {
      throw new Error(`Surface "${surfaceId}" not found`)
    }

    // Apply component updates
    for (const update of updates) {
      surface.setComponent(update)
    }

    // Emit update event
    this.emitEvent({
      type: 'update',
      surfaceId,
      data: { updates },
    })
  }

  /**
   * Update surface data model
   */
  updateDataModel(surfaceId: SurfaceId, updates: DataModelUpdate): void {
    const surface = this.surfaces.get(surfaceId)

    if (!surface) {
      throw new Error(`Surface "${surfaceId}" not found`)
    }

    // Apply data model updates
    surface.dataModel.update(updates)

    // Emit update event
    this.emitEvent({
      type: 'update',
      surfaceId,
      data: { dataModel: updates },
    })
  }

  /**
   * Delete a surface
   */
  delete(surfaceId: SurfaceId): void {
    const surface = this.surfaces.get(surfaceId)

    if (!surface) {
      throw new Error(`Surface "${surfaceId}" not found`)
    }

    // Deactivate the surface
    surface.deactivate()

    // Remove from manager
    this.surfaces.delete(surfaceId)

    // Emit delete event
    this.emitEvent({
      type: 'delete',
      surfaceId,
    })
  }

  /**
   * Get all surface IDs
   */
  getSurfaceIds(): SurfaceId[] {
    return Array.from(this.surfaces.keys())
  }

  /**
   * Get all active surfaces
   */
  getActiveSurfaces(): SurfaceImpl[] {
    return Array.from(this.surfaces.values()).filter((s) => s.isActive())
  }

  /**
   * Clear all surfaces
   */
  clear(): void {
    for (const surface of this.surfaces.values()) {
      surface.deactivate()
    }
    this.surfaces.clear()
  }

  /**
   * Subscribe to surface events
   */
  subscribe(callback: SurfaceEventCallback): () => void {
    this.subscriptions.add(callback)
    return () => this.subscriptions.delete(callback)
  }

  /**
   * Get surface count
   */
  getSurfaceCount(): number {
    return this.surfaces.size
  }

  /**
   * Get active surface count
   */
  getActiveSurfaceCount(): number {
    return this.getActiveSurfaces().length
  }

  /**
   * Emit a surface event
   */
  private emitEvent(event: SurfaceEvent): void {
    this.subscriptions.forEach((callback) => {
      try {
        callback(event)
      } catch (error) {
        console.error('Error in surface event subscription:', error)
      }
    })
  }
}

/**
 * Create a new surface manager
 */
export function createSurfaceManager(): SurfaceManager {
  return new SurfaceManager()
}
