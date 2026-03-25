/**
 * useComponentRegistry Composable
 *
 * Provides access to the component registry
 */

import type { ComponentRegistry, ComponentMetadata } from '@a2ui/core'

export interface UseComponentRegistryReturn {
  register: (type: string, component: any, metadata?: Partial<ComponentMetadata>) => void
  unregister: (type: string) => void
  get: (type: string) => any
  has: (type: string) => boolean
  getTypes: () => string[]
  getMetadata: (type: string) => ComponentMetadata | undefined
  getByCategory: (category: string) => string[]
}

// Global registry reference (will be set by the plugin)
let componentRegistry: ComponentRegistry | null = null

export function setComponentRegistry(registry: ComponentRegistry) {
  componentRegistry = registry
}

/**
 * Composable for working with the component registry
 */
export function useComponentRegistry(): UseComponentRegistryReturn {
  if (!componentRegistry) {
    throw new Error('Component registry not initialized. Make sure A2UI plugin is installed.')
  }

  const register = (
    type: string,
    component: any,
    metadata?: Partial<ComponentMetadata>
  ): void => {
    componentRegistry!.register(type, component, metadata)
  }

  const unregister = (type: string): void => {
    componentRegistry!.unregister(type)
  }

  const get = (type: string): any => {
    return componentRegistry!.get(type)
  }

  const has = (type: string): boolean => {
    return componentRegistry!.has(type)
  }

  const getTypes = (): string[] => {
    return componentRegistry!.getTypes()
  }

  const getMetadata = (type: string): ComponentMetadata | undefined => {
    return componentRegistry!.getMetadata(type)
  }

  const getByCategory = (category: string): string[] => {
    if ('getByCategory' in componentRegistry!) {
      return (componentRegistry as any).getByCategory(category)
    }
    return []
  }

  return {
    register,
    unregister,
    get,
    has,
    getTypes,
    getMetadata,
    getByCategory,
  }
}
