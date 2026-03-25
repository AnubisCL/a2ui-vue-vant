/**
 * A2UI Tree Builder Types
 */

import type { ComponentId, ComponentUpdate } from './messages'
import type { Surface } from './surface'

/**
 * Component tree node
 */
export interface ComponentTreeNode {
  /** Component ID */
  id: ComponentId

  /** Component type */
  type: string

  /** Component props */
  props: Record<string, unknown>

  /** Child nodes */
  children: ComponentTreeNode[]

  /** Template children (for lists/dynamic content) */
  templates?: TemplateNode[]
}

/**
 * Template node for dynamic children
 */
export interface TemplateNode {
  /** Component ID */
  componentId: ComponentId

  /** Scoped path */
  scopedPath: {
    path: string
    scope?: ComponentId
  }

  /** Template instance nodes (generated from data) */
  instances: ComponentTreeNode[]
}

/**
 * Tree builder interface
 */
export interface TreeBuilder {
  /** Build a component tree from a surface */
  build(surface: Surface): ComponentTreeNode | null

  /** Update the tree with component updates */
  update(tree: ComponentTreeNode, updates: ComponentUpdate[]): ComponentTreeNode

  /** Find a node by component ID */
  find(tree: ComponentTreeNode, componentId: ComponentId): ComponentTreeNode | null

  /** Validate the tree structure */
  validate(tree: ComponentTreeNode): boolean
}

/**
 * Tree build options
 */
export interface TreeBuildOptions {
  /** Whether to include template resolution */
  resolveTemplates?: boolean

  /** Maximum depth for tree traversal */
  maxDepth?: number

  /** Custom node resolver */
  nodeResolver?: (component: ComponentUpdate) => ComponentTreeNode | null
}
