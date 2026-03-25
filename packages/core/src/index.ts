// Core SDK entry point
// Re-export types
export * from './types/messages'
export * from './types/components'
export * from './types/registry'
export * from './types/surface'
export * from './types/tree-builder'
export * from './types/parser'

// Re-export implementations with aliases to avoid conflicts
export { createMessageParser } from './parser/message-parser'
export { MessageParser } from './parser/message-parser'
export type { IMessageParser } from './parser/message-parser'

export { createComponentRegistry, createCatalogValidator, BASIC_COMPONENT_METADATA } from './registry/component-registry'
export { ComponentRegistryImpl as ComponentRegistry } from './registry/component-registry'
export type { ValidationError, ValidationResult } from './registry/catalog-validator'

export type { DataModelStore, JSONPointer } from './store/store'
export { createDataModelStore, createScopedPathResolver } from './store/data-model-store'
export { DataModelStoreImpl } from './store/data-model-store'
export { ScopedPathResolver } from './store/data-model-store'
export { parsePointer, stringifyPointer, validatePointer, resolvePointer, setPointer, deletePointer, getParentPath, createJSONPointerResolver } from './store/json-pointer'
export { JSONPointerResolverImpl } from './store/json-pointer'

export { createSurface } from './surface/surface'
export { SurfaceImpl as Surface } from './surface/surface'
export { createSurfaceManager } from './surface/surface-manager'
export { SurfaceManager as SurfaceManagerImpl } from './surface/surface-manager'

export { createTreeBuilder, buildComponentTree, findNode } from './tree-builder/tree-builder'
export { TreeBuilder } from './tree-builder/tree-builder'
