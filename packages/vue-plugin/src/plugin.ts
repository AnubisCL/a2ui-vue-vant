/**
 * A2UI Vue Plugin
 *
 * Provides easy integration for Vue 3 applications
 */

import type { App } from 'vue'
import {
  ComponentRegistry,
  createComponentRegistry,
  SurfaceManager,
  createSurfaceManager,
  MessageParser,
  createMessageParser,
} from '@a2ui/core'

// Import all components
import * as Components from '@a2ui/vue-components'

// Set up global state
import {
  setGlobalState,
  setSurfaceManager,
  setComponentRegistry,
} from '@a2ui/vue-components'

export interface A2UIPluginOptions {
  /**
   * Custom components to register
   */
  components?: Record<string, any>

  /**
   * Whether to register all default components
   */
  registerDefaults?: boolean
}

/**
 * A2UI Vue Plugin
 */
export function createA2UI(options: A2UIPluginOptions = {}) {
  const { components = {}, registerDefaults = true } = options

  // Create core instances
  const componentRegistry = createComponentRegistry()
  const surfaceManager = createSurfaceManager()
  const messageParser = createMessageParser()

  // Register default components
  if (registerDefaults) {
    const defaultComponents: Record<string, any> = {
      // Layout
      Row: Components.Row,
      Column: Components.Column,
      List: Components.List,

      // Display
      Text: Components.Text,
      Image: Components.Image,
      Icon: Components.Icon,
      Video: Components.Video,
      AudioPlayer: Components.AudioPlayer,
      Divider: Components.Divider,

      // Input
      Button: Components.Button,
      TextField: Components.TextField,
      CheckBox: Components.CheckBox,
      DateTimeInput: Components.DateTimeInput,
      ChoicePicker: Components.ChoicePicker,
      Slider: Components.Slider,

      // Container
      Card: Components.Card,
      Tabs: Components.Tabs,
      Modal: Components.Modal,
    }

    componentRegistry.registerBatch(defaultComponents)
  }

  // Register custom components
  if (Object.keys(components).length > 0) {
    componentRegistry.registerBatch(components)
  }

  // Set up global state for composables
  setGlobalState({
    surfaceManager,
    messageParser,
  })
  setSurfaceManager(surfaceManager)
  setComponentRegistry(componentRegistry)

  // Plugin instance
  const plugin = {
    install(app: App) {
      // Provide core instances
      app.provide('a2ui-registry', componentRegistry)
      app.provide('a2ui-surface-manager', surfaceManager)
      app.provide('a2ui-message-parser', messageParser)

      // Register all components globally
      if (registerDefaults) {
        app.component('A2uiRow', Components.Row)
        app.component('A2uiColumn', Components.Column)
        app.component('A2uiList', Components.List)
        app.component('A2uiText', Components.Text)
        app.component('A2uiImage', Components.Image)
        app.component('A2uiIcon', Components.Icon)
        app.component('A2uiVideo', Components.Video)
        app.component('A2uiAudioPlayer', Components.AudioPlayer)
        app.component('A2uiDivider', Components.Divider)
        app.component('A2uiButton', Components.Button)
        app.component('A2uiTextField', Components.TextField)
        app.component('A2uiCheckBox', Components.CheckBox)
        app.component('A2uiDateTimeInput', Components.DateTimeInput)
        app.component('A2uiChoicePicker', Components.ChoicePicker)
        app.component('A2uiSlider', Components.Slider)
        app.component('A2uiCard', Components.Card)
        app.component('A2uiTabs', Components.Tabs)
        app.component('A2uiModal', Components.Modal)
      }

      // Register custom components
      for (const [name, component] of Object.entries(components)) {
        app.component(name, component)
      }
    },
  }

  return plugin
}

/**
 * Default plugin export
 */
export default createA2UI()

// Export core types
export type { ComponentRegistry, SurfaceManager, MessageParser }
