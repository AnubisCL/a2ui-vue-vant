/**
 * useA2UI Composable
 *
 * Main entry point for using A2UI in Vue components
 */

import { ref, computed, type Ref } from 'vue'
import type { SurfaceId, UserActionMessage } from '@a2ui/core'

type A2UIMessageType = UserActionMessage | {
  type: 'createSurface' | 'updateComponents' | 'updateDataModel' | 'deleteSurface'
  surfaceId: string
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
  handleMessage: (message: A2UIMessageType) => void
  sendAction: (action: UserActionMessage) => void
  setActiveSurface: (surfaceId: SurfaceId | null) => void
}

// Global state (will be managed by the plugin)
const globalState = {
  surfaces: ref<SurfaceId[]>([]),
  activeSurface: ref<SurfaceId | null>(null),
  isReady: ref(false),
  surfaceManager: null as any,
  messageParser: null as any,
}

export function setGlobalState(state: {
  surfaceManager: any
  messageParser: any
}) {
  globalState.surfaceManager = state.surfaceManager
  globalState.messageParser = state.messageParser
}

/**
 * Main composable for A2UI functionality
 */
export function useA2UI(options: UseA2UIOptions = {}): UseA2UIReturn {
  const surfaces = computed(() => globalState.surfaces.value)
  const activeSurface = computed(() => globalState.activeSurface.value)
  const isReady = computed(() => globalState.isReady.value)

  const connect = (endpoint: string): void => {
    // TODO: Implement WebSocket/SSE connection
    console.log('Connecting to:', endpoint)
    globalState.isReady.value = true
  }

  const disconnect = (): void => {
    // TODO: Implement disconnect
    console.log('Disconnecting')
    globalState.isReady.value = false
  }

  const handleMessage = (message: A2UIMessageType): void => {
    if (!globalState.surfaceManager) {
      console.error('Surface manager not initialized')
      return
    }

    // Route message to appropriate handler
    switch (message.type) {
      case 'createSurface':
        globalState.surfaceManager.create({
          id: message.surfaceId,
          components: message.components,
          dataModel: message.dataModel,
        })
        if (!globalState.surfaces.value.includes(message.surfaceId)) {
          globalState.surfaces.value.push(message.surfaceId)
        }
        if (!globalState.activeSurface.value) {
          globalState.activeSurface.value = message.surfaceId
        }
        break

      case 'updateComponents':
        globalState.surfaceManager.updateComponents(message.surfaceId, message.updates)
        break

      case 'updateDataModel':
        globalState.surfaceManager.updateDataModel(message.surfaceId, message.model)
        break

      case 'deleteSurface':
        globalState.surfaceManager.delete(message.surfaceId)
        globalState.surfaces.value = globalState.surfaces.value.filter(
          (id) => id !== message.surfaceId
        )
        if (globalState.activeSurface.value === message.surfaceId) {
          globalState.activeSurface.value = globalState.surfaces.value[0] || null
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

  return {
    surfaces,
    activeSurface,
    isReady,
    connect,
    disconnect,
    handleMessage,
    sendAction,
    setActiveSurface,
  }
}
