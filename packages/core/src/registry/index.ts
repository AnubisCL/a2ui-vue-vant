/**
 * A2UI Component Registry Module
 */

export {
  ComponentRegistryImpl,
  CatalogValidator,
  createComponentRegistry,
  createCatalogValidator,
  BASIC_COMPONENT_METADATA,
} from './component-registry'

export type { ComponentRegistry } from '../types'
export type { ValidationError, ValidationResult } from './catalog-validator'
