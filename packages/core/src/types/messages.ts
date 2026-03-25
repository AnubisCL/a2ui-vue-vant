/**
 * A2UI Message Types (v0.9)
 * Based on Google's A2UI specification
 */

/**
 * A2UI protocol version
 */
export const A2UI_VERSION = 'v0.9' as const
export type A2UIVersion = typeof A2UI_VERSION

/**
 * Base message interface
 */
export interface A2UIMessage {
  version: A2UIVersion
}

/**
 * Surface ID - unique identifier for a surface
 */
export type SurfaceId = string

/**
 * Component ID - unique identifier for a component within a surface
 */
export type ComponentId = string

/**
 * JSON Pointer path (RFC 6901)
 */
export type JSONPointer = string

/**
 * Scoped path for template-based dynamic children
 */
export interface ScopedPath {
  path: JSONPointer
  scope?: ComponentId
}

/**
 * Value reference - can be a direct value or a path reference
 */
export type ValueReference<T = unknown> = T | { path: JSONPointer; scope?: ComponentId }

/**
 * ========================================
 * SERVER TO CLIENT MESSAGES
 * ========================================
 */

/**
 * Create a new surface
 */
export interface CreateSurfaceMessage extends A2UIMessage {
  type: 'createSurface'
  surfaceId: SurfaceId
  components?: ComponentUpdate[]
  dataModel?: Record<string, unknown>
}

/**
 * Update components on a surface
 */
export interface UpdateComponentsMessage extends A2UIMessage {
  type: 'updateComponents'
  surfaceId: SurfaceId
  updates: ComponentUpdate[]
}

/**
 * Component update definition
 */
export interface ComponentUpdate {
  componentId: ComponentId
  type: string
  props?: Record<string, unknown>
  children?: ComponentChildUpdate[]
}

/**
 * Component child update
 */
export type ComponentChildUpdate =
  | { type: 'component'; componentId: ComponentId }
  | { type: 'text'; content: string }
  | { type: 'template'; template: ComponentTemplate }

/**
 * Component template for dynamic children
 */
export interface ComponentTemplate {
  componentId: ComponentId
  scopedPath: ScopedPath
}

/**
 * Update data model on a surface
 */
export interface UpdateDataModelMessage extends A2UIMessage {
  type: 'updateDataModel'
  surfaceId: SurfaceId
  model: DataModelUpdate
}

/**
 * Data model update
 */
export type DataModelUpdate = {
  [path: JSONPointer]: unknown
}

/**
 * Delete a surface
 */
export interface DeleteSurfaceMessage extends A2UIMessage {
  type: 'deleteSurface'
  surfaceId: SurfaceId
}

/**
 * Union of all server-to-client message types
 */
export type ServerMessage =
  | CreateSurfaceMessage
  | UpdateComponentsMessage
  | UpdateDataModelMessage
  | DeleteSurfaceMessage

/**
 * ========================================
 * CLIENT TO SERVER MESSAGES
 * ========================================
 */

/**
 * User action event
 */
export interface UserActionMessage extends A2UIMessage {
  type: 'userAction'
  surfaceId: SurfaceId
  action: UserAction
}

/**
 * User action definition
 */
export interface UserAction {
  componentId: ComponentId
  event: string
  payload?: unknown
  timestamp?: number
}

/**
 * Union of all client-to-server message types
 */
export type ClientMessage = UserActionMessage

/**
 * ========================================
 * MESSAGE UNION
 * ========================================
 */

/**
 * Message parser event
 */
export interface MessageEvent {
  message: ServerMessage | ClientMessage
  index: number
  raw: string
}
