/**
 * A2UI Component Registry
 *
 * Manages component registration and metadata
 */

import type {
  ComponentCatalog,
  ComponentMetadata,
  ComponentRegistry,
  ComponentType,
} from '../types'
import { BASIC_CATALOG } from '../types'

/**
 * Default metadata for basic components
 */
export const BASIC_COMPONENT_METADATA: Record<ComponentType, ComponentMetadata> = {
  // Layout
  Row: {
    type: 'Row',
    displayName: 'Row',
    category: 'layout',
    description: 'Horizontal flex layout container',
    supportsChildren: true,
    supportsTemplates: false,
  },
  Column: {
    type: 'Column',
    displayName: 'Column',
    category: 'layout',
    description: 'Vertical flex layout container',
    supportsChildren: true,
    supportsTemplates: false,
  },
  List: {
    type: 'List',
    displayName: 'List',
    category: 'layout',
    description: 'Scrollable list with template support for dynamic items',
    supportsChildren: false,
    supportsTemplates: true,
  },

  // Display
  Text: {
    type: 'Text',
    displayName: 'Text',
    category: 'display',
    description: 'Text display with Markdown support',
    supportsChildren: false,
    supportsTemplates: false,
  },
  Image: {
    type: 'Image',
    displayName: 'Image',
    category: 'display',
    description: 'Image display',
    supportsChildren: false,
    supportsTemplates: false,
  },
  Icon: {
    type: 'Icon',
    displayName: 'Icon',
    category: 'display',
    description: 'Icon display',
    supportsChildren: false,
    supportsTemplates: false,
  },
  Video: {
    type: 'Video',
    displayName: 'Video',
    category: 'display',
    description: 'Video player',
    supportsChildren: false,
    supportsTemplates: false,
  },
  AudioPlayer: {
    type: 'AudioPlayer',
    displayName: 'Audio Player',
    category: 'display',
    description: 'Audio player',
    supportsChildren: false,
    supportsTemplates: false,
  },
  Divider: {
    type: 'Divider',
    displayName: 'Divider',
    category: 'display',
    description: 'Horizontal or vertical divider line',
    supportsChildren: false,
    supportsTemplates: false,
  },

  // Input
  Button: {
    type: 'Button',
    displayName: 'Button',
    category: 'input',
    description: 'Clickable button',
    supportsChildren: false,
    supportsTemplates: false,
  },
  TextField: {
    type: 'TextField',
    displayName: 'Text Field',
    category: 'input',
    description: 'Text input field',
    supportsChildren: false,
    supportsTemplates: false,
  },
  CheckBox: {
    type: 'CheckBox',
    displayName: 'Check Box',
    category: 'input',
    description: 'Checkbox input',
    supportsChildren: false,
    supportsTemplates: false,
  },
  DateTimeInput: {
    type: 'DateTimeInput',
    displayName: 'Date/Time Input',
    category: 'input',
    description: 'Date and time picker',
    supportsChildren: false,
    supportsTemplates: false,
  },
  ChoicePicker: {
    type: 'ChoicePicker',
    displayName: 'Choice Picker',
    category: 'input',
    description: 'Single or multiple choice selection',
    supportsChildren: false,
    supportsTemplates: false,
  },
  Slider: {
    type: 'Slider',
    displayName: 'Slider',
    category: 'input',
    description: 'Numeric slider input',
    supportsChildren: false,
    supportsTemplates: false,
  },

  // Container
  Card: {
    type: 'Card',
    displayName: 'Card',
    category: 'container',
    description: 'Card container with title and actions',
    supportsChildren: true,
    supportsTemplates: false,
  },
  Tabs: {
    type: 'Tabs',
    displayName: 'Tabs',
    category: 'container',
    description: 'Tabbed container',
    supportsChildren: true,
    supportsTemplates: false,
  },
  Modal: {
    type: 'Modal',
    displayName: 'Modal',
    category: 'container',
    description: 'Modal dialog',
    supportsChildren: true,
    supportsTemplates: false,
  },

  // Chart
  Chart: {
    type: 'Chart',
    displayName: 'Chart',
    category: 'display',
    description: 'ECharts wrapper for data visualization',
    supportsChildren: false,
    supportsTemplates: false,
  },
}

/**
 * Component Registry Implementation
 */
export class ComponentRegistryImpl implements ComponentRegistry {
  private components = new Map<string, unknown>()
  private metadata = new Map<string, ComponentMetadata>()
  private catalog: ComponentCatalog

  constructor(catalog: ComponentCatalog = BASIC_CATALOG) {
    this.catalog = catalog
  }

  /**
   * Register a component
   */
  register(type: string, component: unknown, metadata?: Partial<ComponentMetadata>): void {
    this.components.set(type, component)

    // Set or merge metadata
    if (metadata) {
      this.metadata.set(type, {
        type: type as ComponentType,
        displayName: metadata.displayName || type,
        category: metadata.category || 'display',
        description: metadata.description,
        supportsChildren: metadata.supportsChildren ?? false,
        supportsTemplates: metadata.supportsTemplates ?? false,
      })
    } else if (BASIC_CATALOG.components.includes(type as ComponentType)) {
      // Use default metadata for basic components
      this.metadata.set(type, BASIC_COMPONENT_METADATA[type as ComponentType])
    }
  }

  /**
   * Register multiple components
   */
  registerBatch(components: Record<string, unknown>): void {
    for (const [type, component] of Object.entries(components)) {
      this.register(type, component)
    }
  }

  /**
   * Unregister a component
   */
  unregister(type: string): void {
    this.components.delete(type)
    this.metadata.delete(type)
  }

  /**
   * Get a component by type
   */
  get(type: string): unknown | undefined {
    return this.components.get(type)
  }

  /**
   * Check if a component is registered
   */
  has(type: string): boolean {
    return this.components.has(type)
  }

  /**
   * Get all registered component types
   */
  getTypes(): string[] {
    return Array.from(this.components.keys())
  }

  /**
   * Get component metadata
   */
  getMetadata(type: string): ComponentMetadata | undefined {
    return this.metadata.get(type)
  }

  /**
   * Clear all registered components
   */
  clear(): void {
    this.components.clear()
    this.metadata.clear()
  }

  /**
   * Get the catalog
   */
  getCatalog(): ComponentCatalog {
    return this.catalog
  }

  /**
   * Validate a component type against the catalog
   */
  validateType(type: string): boolean {
    return this.catalog.components.includes(type as ComponentType)
  }

  /**
   * Get components by category
   */
  getByCategory(category: ComponentMetadata['category']): string[] {
    const result: string[] = []

    for (const [type, metadata] of this.metadata.entries()) {
      if (metadata.category === category) {
        result.push(type)
      }
    }

    return result
  }
}

/**
 * Catalog Validator
 */
export class CatalogValidator {
  private catalog: ComponentCatalog

  constructor(catalog: ComponentCatalog = BASIC_CATALOG) {
    this.catalog = catalog
  }

  /**
   * Validate a component type
   */
  validateType(type: string): boolean {
    return this.catalog.components.includes(type as ComponentType)
  }

  /**
   * Get validation errors for a component
   */
  validate(type: string, props?: Record<string, unknown>): string[] {
    const errors: string[] = []

    // Check if type is valid
    if (!this.validateType(type)) {
      errors.push(`Unknown component type: "${type}"`)
      return errors
    }

    // Basic validation could be extended here
    // For now, we just check the type

    return errors
  }

  /**
   * Update the catalog
   */
  setCatalog(catalog: ComponentCatalog): void {
    this.catalog = catalog
  }

  /**
   * Get the current catalog
   */
  getCatalog(): ComponentCatalog {
    return this.catalog
  }
}

/**
 * Create a new component registry
 */
export function createComponentRegistry(
  catalog?: ComponentCatalog
): ComponentRegistryImpl {
  return new ComponentRegistryImpl(catalog)
}

/**
 * Create a catalog validator
 */
export function createCatalogValidator(catalog?: ComponentCatalog): CatalogValidator {
  return new CatalogValidator(catalog)
}
