/**
 * A2UI Catalog Validator
 *
 * Validates component types and properties against catalog
 */

import type { ComponentCatalog, ComponentType } from '../types'
import { BASIC_CATALOG } from '../types'

/**
 * Validation error
 */
export interface ValidationError {
  /** Error message */
  message: string

  /** Component type */
  componentType: string

  /** Property name (if applicable) */
  property?: string

  /** Invalid value */
  value?: unknown
}

/**
 * Validation result
 */
export interface ValidationResult {
  /** Whether validation passed */
  valid: boolean

  /** Validation errors */
  errors: ValidationError[]
}

/**
 * Catalog Validator Implementation
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
   * Validate component props
   */
  validateProps(type: string, props?: Record<string, unknown>): ValidationResult {
    const errors: ValidationError[] = []

    // Check if type is valid
    if (!this.validateType(type)) {
      errors.push({
        message: `Unknown component type: "${type}"`,
        componentType: type,
      })
      return { valid: false, errors }
    }

    // Type-specific validation could be added here
    // For now, we just validate the type itself

    return {
      valid: errors.length === 0,
      errors,
    }
  }

  /**
   * Validate multiple component updates
   */
  validateComponents(
    components: Array<{ type: string; props?: Record<string, unknown> }>
  ): ValidationResult {
    const allErrors: ValidationError[] = []

    for (const component of components) {
      const result = this.validateProps(component.type, component.props)
      allErrors.push(...result.errors)
    }

    return {
      valid: allErrors.length === 0,
      errors: allErrors,
    }
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
 * Create a catalog validator
 */
export function createCatalogValidator(catalog?: ComponentCatalog): CatalogValidator {
  return new CatalogValidator(catalog)
}
