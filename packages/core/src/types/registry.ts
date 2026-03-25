/**
 * A2UI Registry and Catalog Types
 */

import type { ComponentType } from './components'

/**
 * Component catalog - defines available component types
 */
export interface ComponentCatalog {
  /** Catalog name */
  name: string
  /** Catalog version */
  version: string
  /** Available component types */
  components: ComponentType[]
}

/**
 * Basic catalog - all standard A2UI components
 */
export const BASIC_CATALOG: ComponentCatalog = {
  name: 'basic',
  version: '1.0.0',
  components: [
    // Layout
    'Row',
    'Column',
    'List',

    // Display
    'Text',
    'Image',
    'Icon',
    'Video',
    'AudioPlayer',
    'Divider',

    // Interaction
    'Button',
    'TextField',
    'CheckBox',
    'DateTimeInput',
    'ChoicePicker',
    'Slider',

    // Container
    'Card',
    'Tabs',
    'Modal',
  ],
}

/**
 * Component metadata
 */
export interface ComponentMetadata {
  /** Component type */
  type: ComponentType
  /** Display name */
  displayName: string
  /** Component category */
  category: 'layout' | 'display' | 'input' | 'container'
  /** Component description */
  description?: string
  /** Whether the component supports children */
  supportsChildren: boolean
  /** Whether the component supports templates */
  supportsTemplates: boolean
}

/**
 * Component registry interface
 */
export interface ComponentRegistry {
  /** Register a component */
  register(type: string, component: unknown, metadata?: Partial<ComponentMetadata>): void

  /** Register multiple components */
  registerBatch(components: Record<string, unknown>): void

  /** Unregister a component */
  unregister(type: string): void

  /** Get a component by type */
  get(type: string): unknown | undefined

  /** Check if a component is registered */
  has(type: string): boolean

  /** Get all registered component types */
  getTypes(): string[]

  /** Get component metadata */
  getMetadata(type: string): ComponentMetadata | undefined

  /** Clear all registered components */
  clear(): void

  /** Get the catalog */
  getCatalog(): ComponentCatalog
}

/**
 * Custom catalog interface
 */
export interface CustomCatalog {
  name: string
  version: string
  components: ComponentMetadata[]
}
