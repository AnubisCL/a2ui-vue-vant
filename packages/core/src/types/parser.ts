/**
 * Parse options
 */
export interface ParseOptions {
  /** Whether to validate message version */
  validateVersion?: boolean

  /** Maximum message size (in bytes) */
  maxMessageSize?: number

  /** Whether to skip invalid messages */
  skipInvalid?: boolean

  /** Custom error handler */
  onError?: (error: ParseError, raw: string) => void
}

/**
 * Parse error
 */
export interface ParseError {
  /** Error message */
  message: string

  /** Line number where error occurred */
  line: number

  /** Column number where error occurred */
  column: number

  /** Raw input that caused the error */
  raw: string

  /** Error code */
  code: ParseErrorCode
}

/**
 * Parse error codes
 */
export type ParseErrorCode =
  | 'INVALID_JSON'
  | 'INVALID_VERSION'
  | 'INVALID_TYPE'
  | 'MISSING_FIELD'
  | 'MESSAGE_TOO_LARGE'
  | 'UNKNOWN_ERROR'
