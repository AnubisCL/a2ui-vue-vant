/**
 * useA2UI Composable
 *
 * Main entry point for using A2UI in Vue components
 */

import { ref, computed, type Ref } from 'vue'
import type { SurfaceId, UserActionMessage } from '@a2ui/core'

type A2UIMessageType = UserActionMessage | {
  type: 'surface' | 'component' | 'data' | 'createSurface' | 'updateComponents' | 'updateDataModel' | 'deleteSurface'
  surfaceId?: string
  componentId?: string
  component?: any
  path?: string
  value?: any
  [key: string]: any
}

export interface UseA2UIOptions {
  surfaceId?: SurfaceId
}

export interface UseA2UIReturn {
  surfaces: Ref<SurfaceId[]>
  activeSurface: Ref<SurfaceId | null>
  isReady: Ref<boolean>
  connect: (endpoint: string) => void
  disconnect: () => void
  handleMessage: (message: A2UIMessageType | string) => void
  sendAction: (action: UserActionMessage) => void
  setActiveSurface: (surfaceId: SurfaceId | null) => void
  clearSurface: (surfaceId: SurfaceId) => void
  componentStore: Ref<Record<string, Record<string, any>>>
}

// Global state (will be managed by the plugin)
const globalState = {
  surfaces: ref<SurfaceId[]>([]),
  activeSurface: ref<SurfaceId | null>(null),
  isReady: ref(false),
  surfaceManager: null as any,
  messageParser: null as any,
  // Simple component store for demo
  componentStore: ref<Record<string, Record<string, any>>>({}),
}

export function setGlobalState(state: {
  surfaceManager: any
  messageParser: any
}) {
  globalState.surfaceManager = state.surfaceManager
  globalState.messageParser = state.messageParser
}

/**
 * Parse JSONL message
 */
function parseMessage(message: string): A2UIMessageType | null {
  try {
    return JSON.parse(message.trim())
  } catch (e) {
    console.error('Failed to parse message:', message.substring(0, 200), '...')
    return null
  }
}

/**
 * Main composable for A2UI functionality
 */
export function useA2UI(options: UseA2UIOptions = {}): UseA2UIReturn {
  const surfaces = computed(() => globalState.surfaces.value)
  const activeSurface = computed(() => globalState.activeSurface.value)
  const isReady = computed(() => globalState.isReady.value)

  const connect = (endpoint: string): void => {
    console.log('Connecting to:', endpoint)
    globalState.isReady.value = true
  }

  const disconnect = (): void => {
    console.log('Disconnecting')
    globalState.isReady.value = false
  }

  const handleMessage = (message: A2UIMessageType | string): void => {
    // Parse string message if needed
    const parsedMsg = typeof message === 'string' ? parseMessage(message) : message
    if (!parsedMsg) return

    console.log('[A2UI] Received message:', parsedMsg.type, parsedMsg.surfaceId, parsedMsg.componentId || '')

    const msg = parsedMsg as A2UIMessageType & { surfaceId: string; componentId?: string }

    // Route message to appropriate handler
    switch (msg.type) {
      case 'surface':
      case 'createSurface':
        // Create surface
        if (msg.surfaceId) {
          // Only clear components if this is a NEW surface (not an update)
          const isNewSurface = !globalState.surfaces.value.includes(msg.surfaceId)
          if (isNewSurface) {
            // Clear existing surface components first - create new object to trigger reactivity
            globalState.componentStore.value = {
              ...globalState.componentStore.value,
              [msg.surfaceId]: {},
            }
            globalState.surfaces.value = [...globalState.surfaces.value, msg.surfaceId]
          } else {
            console.log('[A2UI] Surface exists, preserving components')
          }

          if (!globalState.activeSurface.value) {
            globalState.activeSurface.value = msg.surfaceId
          }

          // Use surface manager if available
          if (globalState.surfaceManager) {
            // Delete existing surface if it exists
            if (globalState.surfaceManager.has(msg.surfaceId)) {
              try {
                globalState.surfaceManager.delete(msg.surfaceId)
              } catch (e) {
                console.warn('Failed to delete existing surface:', e)
              }
            }
            globalState.surfaceManager.create({
              id: msg.surfaceId,
              name: msg.name,
            })
          }
        }
        break

      case 'component':
        // Add/update component
        if (msg.surfaceId && msg.componentId && msg.component) {
          console.log('[A2UI] Adding component:', msg.componentId, 'type:', msg.component.type)
          // Create new object to trigger Vue reactivity
          const currentSurface = globalState.componentStore.value[msg.surfaceId] || {}
          globalState.componentStore.value = {
            ...globalState.componentStore.value,
            [msg.surfaceId]: {
              ...currentSurface,
              [msg.componentId]: {
                id: msg.componentId,
                ...msg.component,
                parentId: msg.parentId || null,
              },
            },
          }
          console.log('[A2UI] Component store now has:', Object.keys(globalState.componentStore.value[msg.surfaceId] || {}).length, 'components')

          // Use surface manager if available
          if (globalState.surfaceManager) {
            globalState.surfaceManager.updateComponents(msg.surfaceId, [{
              componentId: msg.componentId,
              type: msg.component.type,
              props: msg.component.props,
            }])
          }
        }
        break

      case 'data':
        // Update data model
        if (globalState.surfaceManager && msg.path !== undefined) {
          globalState.surfaceManager.updateDataModel(
            msg.surfaceId || globalState.activeSurface.value || '',
            msg.path,
            msg.value
          )
        }
        break

      case 'updateComponents':
        if (globalState.surfaceManager && msg.surfaceId) {
          globalState.surfaceManager.updateComponents(msg.surfaceId, msg.updates)
        }
        break

      case 'updateDataModel':
        if (globalState.surfaceManager && msg.surfaceId) {
          globalState.surfaceManager.updateDataModel(msg.surfaceId, msg.model)
        }
        break

      case 'deleteSurface':
        if (msg.surfaceId) {
          delete globalState.componentStore.value[msg.surfaceId]
          globalState.surfaces.value = globalState.surfaces.value.filter(
            (id) => id !== msg.surfaceId
          )
          if (globalState.activeSurface.value === msg.surfaceId) {
            globalState.activeSurface.value = globalState.surfaces.value[0] || null
          }
          if (globalState.surfaceManager) {
            globalState.surfaceManager.delete(msg.surfaceId)
          }
        }
        break
    }
  }

  const sendAction = (action: UserActionMessage): void => {
    // TODO: Send action to agent via WebSocket/SSE
    console.log('Sending action:', action)
  }

  const setActiveSurface = (surfaceId: SurfaceId | null): void => {
    globalState.activeSurface.value = surfaceId
  }

  const clearSurface = (surfaceId: SurfaceId): void => {
    // Clear all components in the surface
    if (globalState.componentStore.value[surfaceId]) {
      globalState.componentStore.value[surfaceId] = {}
    }

    // Use surface manager if available - delete the surface entirely
    if (globalState.surfaceManager && globalState.surfaceManager.has(surfaceId)) {
      try {
        globalState.surfaceManager.delete(surfaceId)
      } catch (e) {
        console.warn('Failed to delete surface:', e)
      }
    }
  }

  return {
    surfaces,
    activeSurface,
    isReady,
    connect,
    disconnect,
    handleMessage,
    sendAction,
    setActiveSurface,
    clearSurface,
    componentStore: globalState.componentStore,
  }
}

/**
 * Get components for a specific surface
 */
export function useSurfaceComponents(surfaceId: SurfaceId) {
  return computed(() => {
    const components = globalState.componentStore.value[surfaceId] || {}
    return Object.values(components)
  })
}
