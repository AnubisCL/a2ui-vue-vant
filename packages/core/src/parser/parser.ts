/**
 * A2UI Message Parser Types
 */

import type { MessageEvent } from '../types'

/**
 * Message parser interface
 */
export interface MessageParser {
  /** Parse a JSONL string and emit events */
  parse(input: string): void

  /** Parse a chunk of data (for streaming) */
  parseChunk(chunk: string): void

  /** Reset the parser state */
  reset(): void

  /** Subscribe to message events */
  subscribe(callback: (event: MessageEvent) => void): () => void

  /** Clear all subscriptions */
  clearSubscriptions(): void

  /** Get the current line buffer */
  getBuffer(): string

  /** Set whether to validate message version */
  setValidateVersion(validate: boolean): void
}
