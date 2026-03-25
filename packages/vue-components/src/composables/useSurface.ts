/**
 * useSurface Composable
 *
 * Provides access to a specific surface's state
 */

import { computed, ref, type Ref, watchEffect } from 'vue'
import type { Surface, ComponentId, ComponentUpdate } from '@a2ui/core'

export interface UseSurfaceOptions {
  surfaceId: string
}

export interface UseSurfaceReturn {
  surface: Ref<Surface | undefined>
  components: Ref<Map<ComponentId, ComponentUpdate>>
  dataModel: Ref<Record<string, unknown>>
  rootId: Ref<ComponentId | undefined>
  active: Ref<boolean>
  hasComponent: (id: ComponentId) => boolean
  getComponent: (id: ComponentId) => ComponentUpdate | undefined
}

// Global surface manager reference (will be set by the plugin)
let surfaceManager: any = null

export function setSurfaceManager(manager: any) {
  surfaceManager = manager
}

/**
 * Composable for working with a specific surface
 */
export function useSurface(options: UseSurfaceOptions): UseSurfaceReturn {
  const surface = ref<Surface | undefined>(
    surfaceManager?.get(options.surfaceId)
  ) as Ref<Surface | undefined>

  const components = ref<Map<ComponentId, ComponentUpdate>>(
    surface.value?.components ?? new Map()
  )

  const dataModel = ref<Record<string, unknown>>(
    surface.value?.dataModel.getData() ?? {}
  )

  const rootId = ref<ComponentId | undefined>(surface.value?.rootId)

  const active = computed(() => surface.value?.isActive() ?? false)

  const hasComponent = (id: ComponentId): boolean => {
    return components.value.has(id)
  }

  const getComponent = (id: ComponentId): ComponentUpdate | undefined => {
    return components.value.get(id)
  }

  // Subscribe to surface updates
  if (surfaceManager) {
    const unsubscribe = surfaceManager.subscribe((event: any) => {
      if (event.surfaceId === options.surfaceId) {
        const updatedSurface = surfaceManager.get(options.surfaceId)
        if (updatedSurface) {
          surface.value = updatedSurface
          components.value = updatedSurface.components
          dataModel.value = updatedSurface.dataModel.getData()
          rootId.value = updatedSurface.rootId
        }
      }
    })

    // Cleanup on unmount
    watchEffect((onCleanup) => {
      onCleanup(unsubscribe)
    })
  }

  return {
    surface,
    components,
    dataModel,
    rootId,
    active,
    hasComponent,
    getComponent,
  }
}
