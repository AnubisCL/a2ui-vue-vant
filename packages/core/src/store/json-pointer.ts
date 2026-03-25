/**
 * JSON Pointer Implementation (RFC 6901)
 *
 * Provides utilities for resolving and manipulating JSON Pointer paths
 */

import type { JSONPointer, JSONPointerResolver } from '../types'

/**
 * Escape special characters in a JSON Pointer token
 */
function escapeToken(token: string): string {
  return token.replace(/~/g, '~0').replace(/\//g, '~1')
}

/**
 * Unescape special characters in a JSON Pointer token
 */
function unescapeToken(token: string): string {
  return token.replace(/~1/g, '/').replace(/~0/g, '~')
}

/**
 * Parse a JSON Pointer path into tokens
 */
export function parsePointer(path: JSONPointer): string[] {
  if (path === '') {
    return []
  }

  if (path[0] !== '/') {
    throw new Error(`Invalid JSON Pointer: "${path}" - must start with "/" or be empty`)
  }

  return path.slice(1).split('/').map(unescapeToken)
}

/**
 * Convert tokens to a JSON Pointer path
 */
export function stringifyPointer(tokens: string[]): JSONPointer {
  if (tokens.length === 0) {
    return '' as JSONPointer
  }
  return '/' + tokens.map(escapeToken).join('/') as JSONPointer
}

/**
 * Validate a JSON Pointer path
 */
export function validatePointer(path: string): path is JSONPointer {
  try {
    parsePointer(path as JSONPointer)
    return true
  } catch {
    return false
  }
}

/**
 * Resolve a JSON Pointer path on a data object
 */
export function resolvePointer(data: unknown, path: JSONPointer): unknown {
  const tokens = parsePointer(path)
  let current: any = data

  for (const token of tokens) {
    if (current === null || current === undefined) {
      throw new Error(
        `Cannot resolve token "${token}" - value is ${current === null ? 'null' : 'undefined'}`
      )
    }

    if (typeof current !== 'object') {
      throw new Error(
        `Cannot resolve token "${token}" - parent is not an object (type: ${typeof current})`
      )
    }

    current = current[token]
  }

  return current
}

/**
 * Set a value at a JSON Pointer path
 */
export function setPointer(
  data: Record<string, unknown>,
  path: JSONPointer,
  value: unknown
): void {
  const tokens = parsePointer(path)

  if (tokens.length === 0) {
    // Setting root - replace all data
    Object.assign(data, value as Record<string, unknown>)
    return
  }

  let current: any = data

  // Traverse to parent of target
  for (let i = 0; i < tokens.length - 1; i++) {
    const token = tokens[i]

    if (current[token] === undefined) {
      // Create intermediate object
      current[token] = {}
    } else if (typeof current[token] !== 'object' || current[token] === null) {
      throw new Error(
        `Cannot set value at "${path}" - intermediate path at "${token}" is not an object`
      )
    }

    current = current[token]
  }

  // Set the final value
  const lastToken = tokens[tokens.length - 1]
  current[lastToken] = value
}

/**
 * Delete a value at a JSON Pointer path
 */
export function deletePointer(data: Record<string, unknown>, path: JSONPointer): void {
  const tokens = parsePointer(path)

  if (tokens.length === 0) {
    // Clear root object
    for (const key of Object.keys(data)) {
      delete data[key]
    }
    return
  }

  let current: any = data

  // Traverse to parent of target
  for (let i = 0; i < tokens.length - 1; i++) {
    const token = tokens[i]

    if (current[token] === undefined || typeof current[token] !== 'object') {
      throw new Error(`Cannot delete at "${path}" - intermediate path "${token}" does not exist`)
    }

    current = current[token]
  }

  // Delete the final value
  const lastToken = tokens[tokens.length - 1]

  if (Array.isArray(current)) {
    if (lastToken === '-') {
      // Delete last element
      current.pop()
    } else {
      const index = parseInt(lastToken, 10)
      if (isNaN(index) || index < 0 || index >= current.length) {
        throw new Error(`Cannot delete array index "${lastToken}" - out of bounds`)
      }
      current.splice(index, 1)
    }
  } else {
    if (!(lastToken in current)) {
      throw new Error(`Cannot delete "${lastToken}" - property does not exist`)
    }
    delete current[lastToken]
  }
}

/**
 * JSON Pointer Resolver Implementation
 */
export class JSONPointerResolverImpl implements JSONPointerResolver {
  resolve(data: unknown, path: JSONPointer): unknown {
    return resolvePointer(data, path)
  }

  set(data: Record<string, unknown>, path: JSONPointer, value: unknown): void {
    setPointer(data, path, value)
  }

  delete(data: Record<string, unknown>, path: JSONPointer): void {
    deletePointer(data, path)
  }

  validate(path: string): boolean {
    return validatePointer(path)
  }
}

/**
 * Create a JSON Pointer resolver
 */
export function createJSONPointerResolver(): JSONPointerResolver {
  return new JSONPointerResolverImpl()
}

/**
 * Utility: Get parent path and key from a path
 */
export function getParentPath(path: JSONPointer): { parent: JSONPointer; key: string } {
  const tokens = parsePointer(path)

  if (tokens.length === 0) {
    return { parent: '' as JSONPointer, key: '' }
  }

  const key = tokens[tokens.length - 1]
  const parentTokens = tokens.slice(0, -1)
  const parent = stringifyPointer(parentTokens)

  return { parent, key }
}
