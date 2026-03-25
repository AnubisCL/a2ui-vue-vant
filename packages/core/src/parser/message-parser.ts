/**
 * A2UI Message Parser
 *
 * Implements JSONL (JSON Lines) stream parser for A2UI v0.9 messages
 */

import type {
  A2UIMessage,
  MessageEvent,
  ParseError,
  ParseOptions,
} from '../types'
import { A2UI_VERSION } from '../types'

/**
 * Message parser interface
 */
export interface IMessageParser {
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

/**
 * Default parse options
 */
const DEFAULT_OPTIONS: ParseOptions = {
  validateVersion: true,
  maxMessageSize: 1024 * 1024, // 1MB
  skipInvalid: false,
}

/**
 * A2UI Message Parser Implementation
 */
export class MessageParser implements IMessageParser {
  private buffer = ''
  private lineIndex = 0
  private subscriptions = new Set<(event: MessageEvent) => void>()
  private options: ParseOptions

  constructor(options: ParseOptions = {}) {
    this.options = { ...DEFAULT_OPTIONS, ...options }
  }

  /**
   * Parse a JSONL string and emit events
   */
  parse(input: string): void {
    const lines = input.split(/\n/)
    let partialLine = this.buffer

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      const fullLine = i === 0 && partialLine ? partialLine + line : line

      // If this is the last line and it doesn't end with a newline, save it
      if (i === lines.length - 1 && !input.endsWith('\n')) {
        this.buffer = fullLine
        continue
      }

      this.buffer = ''

      // Skip empty lines
      if (!fullLine.trim()) {
        continue
      }

      // Process the line
      this.processLine(fullLine)
      this.lineIndex++
    }
  }

  /**
   * Parse a chunk of data (for streaming)
   */
  parseChunk(chunk: string): void {
    // Add chunk to buffer and process complete lines
    this.buffer += chunk

    // Find the last complete line
    let lastNewlineIndex = -1
    for (let i = this.buffer.length - 1; i >= 0; i--) {
      if (this.buffer[i] === '\n') {
        lastNewlineIndex = i
        break
      }
    }

    if (lastNewlineIndex === -1) {
      // No complete lines yet, just keep buffering
      return
    }

    const completeChunk = this.buffer.slice(0, lastNewlineIndex + 1)
    this.buffer = this.buffer.slice(lastNewlineIndex + 1)

    this.parse(completeChunk)
  }

  /**
   * Reset the parser state
   */
  reset(): void {
    this.buffer = ''
    this.lineIndex = 0
  }

  /**
   * Subscribe to message events
   */
  subscribe(callback: (event: MessageEvent) => void): () => void {
    this.subscriptions.add(callback)
    return () => this.subscriptions.delete(callback)
  }

  /**
   * Clear all subscriptions
   */
  clearSubscriptions(): void {
    this.subscriptions.clear()
  }

  /**
   * Get the current line buffer
   */
  getBuffer(): string {
    return this.buffer
  }

  /**
   * Set whether to validate message version
   */
  setValidateVersion(validate: boolean): void {
    this.options.validateVersion = validate
  }

  /**
   * Process a single line
   */
  private processLine(line: string): void {
    const trimmed = line.trim()

    // Check message size
    if (this.options.maxMessageSize && trimmed.length > this.options.maxMessageSize) {
      this.handleError(
        {
          message: `Message exceeds maximum size of ${this.options.maxMessageSize} bytes`,
          line: this.lineIndex,
          column: trimmed.length,
          raw: line,
          code: 'MESSAGE_TOO_LARGE',
        },
        line
      )
      return
    }

    let message: A2UIMessage

    try {
      message = JSON.parse(trimmed)
    } catch (error) {
      this.handleError(
        {
          message: error instanceof Error ? error.message : 'Invalid JSON',
          line: this.lineIndex,
          column: 0,
          raw: line,
          code: 'INVALID_JSON',
        },
        line
      )
      return
    }

    // Validate message structure
    const validationError = this.validateMessage(message)
    if (validationError) {
      this.handleError(validationError, line)
      return
    }

    // Emit the message event
    this.emit({
      message: message as any, // Type assertion after validation
      index: this.lineIndex,
      raw: line,
    })
  }

  /**
   * Validate a parsed message
   */
  private validateMessage(message: unknown): ParseError | null {
    if (!message || typeof message !== 'object') {
      return {
        message: 'Message must be an object',
        line: this.lineIndex,
        column: 0,
        raw: JSON.stringify(message),
        code: 'INVALID_JSON',
      }
    }

    const msg = message as Record<string, unknown>

    // Check version field
    if (typeof msg.version !== 'string') {
      return {
        message: 'Missing or invalid "version" field',
        line: this.lineIndex,
        column: 0,
        raw: JSON.stringify(message),
        code: 'MISSING_FIELD',
      }
    }

    // Validate version if enabled
    if (this.options.validateVersion && msg.version !== A2UI_VERSION) {
      return {
        message: `Unsupported version: ${msg.version}. Expected: ${A2UI_VERSION}`,
        line: this.lineIndex,
        column: 0,
        raw: JSON.stringify(message),
        code: 'INVALID_VERSION',
      }
    }

    // Check type field
    if (typeof msg.type !== 'string') {
      return {
        message: 'Missing or invalid "type" field',
        line: this.lineIndex,
        column: 0,
        raw: JSON.stringify(message),
        code: 'MISSING_FIELD',
      }
    }

    // Validate type
    const validTypes = [
      'createSurface',
      'updateComponents',
      'updateDataModel',
      'deleteSurface',
      'userAction',
    ]

    if (!validTypes.includes(msg.type)) {
      return {
        message: `Unknown message type: ${msg.type}`,
        line: this.lineIndex,
        column: 0,
        raw: JSON.stringify(message),
        code: 'INVALID_TYPE',
      }
    }

    return null
  }

  /**
   * Handle an error
   */
  private handleError(error: ParseError, raw: string): void {
    if (this.options.onError) {
      this.options.onError(error, raw)
    }

    if (!this.options.skipInvalid) {
      this.emit({
        message: {
          version: 'v0.9' as const,
          type: 'error',
          error,
        } as any,
        index: this.lineIndex,
        raw,
      })
    }
  }

  /**
   * Emit a message event
   */
  private emit(event: MessageEvent): void {
    this.subscriptions.forEach((callback) => {
      try {
        callback(event)
      } catch (error) {
        console.error('Error in message subscription:', error)
      }
    })
  }
}

/**
 * Create a new message parser
 */
export function createMessageParser(options?: ParseOptions): MessageParser {
  return new MessageParser(options)
}
