/**
 * A2UI Tree Builder
 *
 * Converts adjacency list representation to component tree
 */

import type {
  ComponentTreeNode,
  TemplateNode,
  TreeBuilder as ITreeBuilder,
  TreeBuildOptions,
  ComponentId,
  ComponentUpdate,
} from '../types'
import type { Surface } from '../surface'

/**
 * Default tree build options
 */
const DEFAULT_OPTIONS: TreeBuildOptions = {
  resolveTemplates: true,
  maxDepth: 100,
}

/**
 * Tree Builder Implementation
 */
export class TreeBuilder implements ITreeBuilder {
  private options: TreeBuildOptions

  constructor(options: TreeBuildOptions = {}) {
    this.options = { ...DEFAULT_OPTIONS, ...options }
  }

  /**
   * Build a component tree from a surface
   */
  build(surface: Surface): ComponentTreeNode | null {
    const { rootId, components } = surface

    if (!rootId) {
      return null
    }

    const rootComponent = components.get(rootId)
    if (!rootComponent) {
      console.warn(`Root component "${rootId}" not found`)
      return null
    }

    const maxDepth: number = this.options.maxDepth ?? DEFAULT_OPTIONS.maxDepth ?? 100
    return this.buildNode(
      rootComponent,
      components,
      new Set(),
      0,
      maxDepth
    )
  }

  /**
   * Update the tree with component updates
   */
  update(tree: ComponentTreeNode, updates: ComponentUpdate[]): ComponentTreeNode {
    // Create a map of updates
    const updateMap = new Map<ComponentId, ComponentUpdate>()
    for (const update of updates) {
      updateMap.set(update.componentId, update)
    }

    // Recursively update the tree
    return this.updateNode(tree, updateMap)
  }

  /**
   * Find a node by component ID
   */
  find(tree: ComponentTreeNode, componentId: ComponentId): ComponentTreeNode | null {
    if (tree.id === componentId) {
      return tree
    }

    for (const child of tree.children) {
      const found = this.find(child, componentId)
      if (found) {
        return found
      }
    }

    // Also check templates
    if (tree.templates) {
      for (const template of tree.templates) {
        for (const instance of template.instances) {
          const found = this.find(instance, componentId)
          if (found) {
            return found
          }
        }
      }
    }

    return null
  }

  /**
   * Validate the tree structure
   */
  validate(tree: ComponentTreeNode): boolean {
    const visited = new Set<ComponentId>()
    return this.validateNode(tree, visited, new Set())
  }

  /**
   * Build a single tree node
   */
  private buildNode(
    component: ComponentUpdate,
    components: Map<ComponentId, ComponentUpdate>,
    visited: Set<ComponentId>,
    depth: number,
    maxDepth: number
  ): ComponentTreeNode {
    // Check for cycles
    if (visited.has(component.componentId)) {
      throw new Error(`Circular reference detected: ${component.componentId}`)
    }

    // Check max depth
    if (depth > maxDepth) {
      throw new Error(`Maximum tree depth exceeded: ${depth}`)
    }

    visited.add(component.componentId)

    const node: ComponentTreeNode = {
      id: component.componentId,
      type: component.type,
      props: component.props ?? {},
      children: [],
      templates: [],
    }

    // Process children
    if (component.children) {
      for (const childUpdate of component.children) {
        if (childUpdate.type === 'component') {
          // Regular child component
          const childComponent = components.get(childUpdate.componentId)
          if (childComponent) {
            const childNode = this.buildNode(childComponent, components, new Set(visited), depth + 1, maxDepth)
            node.children.push(childNode)
          } else {
            console.warn(`Child component "${childUpdate.componentId}" not found`)
          }
        } else if (childUpdate.type === 'text') {
          // Text child - create a virtual text node
          const textNode: ComponentTreeNode = {
            id: `__text_${Date.now()}_${Math.random()}`,
            type: 'Text',
            props: { content: childUpdate.content },
            children: [],
          }
          node.children.push(textNode)
        } else if (childUpdate.type === 'template' && this.options.resolveTemplates) {
          // Template child - handle template resolution
          const maxDepth: number = this.options.maxDepth ?? DEFAULT_OPTIONS.maxDepth ?? 100
          const template = this.buildTemplate(childUpdate.template, components, depth + 1, maxDepth)
          node.templates?.push(template)
        }
      }
    }

    visited.delete(component.componentId)

    return node
  }

  /**
   * Build a template node
   */
  private buildTemplate(
    template: { componentId: ComponentId; scopedPath: { path: string; scope?: ComponentId } },
    components: Map<ComponentId, ComponentUpdate>,
    depth: number,
    maxDepth: number
  ): TemplateNode {
    const templateComponent = components.get(template.componentId)

    if (!templateComponent) {
      console.warn(`Template component "${template.componentId}" not found`)
      return {
        componentId: template.componentId,
        scopedPath: template.scopedPath,
        instances: [],
      }
    }

    // For now, create a single instance
    // Full template resolution would iterate over data in the data model
    const instanceNode = this.buildNode(
      templateComponent,
      components,
      new Set(),
      depth + 1,
      maxDepth
    )

    return {
      componentId: template.componentId,
      scopedPath: template.scopedPath,
      instances: [instanceNode],
    }
  }

  /**
   * Update a node recursively
   */
  private updateNode(
    node: ComponentTreeNode,
    updates: Map<ComponentId, ComponentUpdate>
  ): ComponentTreeNode {
    const update = updates.get(node.id)

    if (update) {
      // Apply updates to the node
      node.props = update.props ?? node.props
      node.type = update.type
    }

    // Recursively update children
    node.children = node.children.map((child) => this.updateNode(child, updates))

    // Update template instances
    if (node.templates) {
      for (const template of node.templates) {
        template.instances = template.instances.map((instance) =>
          this.updateNode(instance, updates)
        )
      }
    }

    return node
  }

  /**
   * Validate a node recursively
   */
  private validateNode(
    node: ComponentTreeNode,
    visited: Set<ComponentId>,
    path: Set<ComponentId>
  ): boolean {
    // Check for cycles
    if (path.has(node.id)) {
      console.error(`Circular reference detected: ${node.id}`)
      return false
    }

    // Check if node was already visited in a different branch
    if (visited.has(node.id)) {
      console.warn(`Component "${node.id}" appears in multiple branches`)
    }

    visited.add(node.id)
    path.add(node.id)

    // Validate children
    for (const child of node.children) {
      if (!this.validateNode(child, visited, path)) {
        return false
      }
    }

    // Validate template instances
    if (node.templates) {
      for (const template of node.templates) {
        for (const instance of template.instances) {
          if (!this.validateNode(instance, visited, path)) {
            return false
          }
        }
      }
    }

    path.delete(node.id)

    return true
  }
}

/**
 * Create a new tree builder
 */
export function createTreeBuilder(options?: TreeBuildOptions): TreeBuilder {
  return new TreeBuilder(options)
}

/**
 * Build a component tree from a surface (convenience function)
 */
export function buildComponentTree(surface: Surface): ComponentTreeNode | null {
  const builder = new TreeBuilder()
  return builder.build(surface)
}

/**
 * Find a node in a tree by component ID (convenience function)
 */
export function findNode(
  tree: ComponentTreeNode,
  componentId: ComponentId
): ComponentTreeNode | null {
  const builder = new TreeBuilder()
  return builder.find(tree, componentId)
}
