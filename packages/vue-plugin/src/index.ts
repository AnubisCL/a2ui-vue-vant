/**
 * A2UI Vue Plugin
 *
 * Main entry point for the A2UI Vue plugin
 */

export { createA2UI, default as A2UIPlugin } from './plugin'
export { default as A2uiRenderer } from './A2uiRenderer.vue'

export type { A2UIPluginOptions } from './plugin'

// Re-export composables for convenience
export {
  useA2UI,
  useSurface,
  useDataModel,
  useComponentRegistry,
  setGlobalState,
  setSurfaceManager,
  setComponentRegistry,
} from '@a2ui/vue-components'

export type {
  UseA2UIOptions,
  UseA2UIReturn,
  UseSurfaceOptions,
  UseSurfaceReturn,
  UseDataModelOptions,
  UseDataModelReturn,
} from '@a2ui/vue-components'
