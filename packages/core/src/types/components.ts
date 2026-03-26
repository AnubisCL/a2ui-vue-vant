/**
 * A2UI Component Types (Basic Catalog)
 * Based on Google's A2UI specification
 */

import type { ValueReference } from './messages'

/**
 * ========================================
 * LAYOUT COMPONENTS
 * ========================================
 */

/**
 * Row component - horizontal flex layout
 */
export interface RowProps {
  /** Horizontal alignment */
  align?: 'start' | 'center' | 'end' | 'spaceBetween' | 'spaceAround' | 'spaceEvenly'
  /** Vertical alignment */
  valign?: 'start' | 'center' | 'end' | 'stretch'
  /** Gap between children */
  gap?: number
  /** Margin */
  margin?: number | [number, number, number, number] // [top, right, bottom, left]
  /** Width */
  width?: string | number
  /** Whether to scroll when content overflows */
  scrollable?: boolean
}

/**
 * Column component - vertical flex layout
 */
export interface ColumnProps {
  /** Horizontal alignment */
  align?: 'start' | 'center' | 'end' | 'stretch'
  /** Vertical alignment */
  valign?: 'start' | 'center' | 'end' | 'spaceBetween' | 'spaceAround' | 'spaceEvenly'
  /** Gap between children */
  gap?: number
  /** Margin */
  margin?: number | [number, number, number, number]
  /** Height */
  height?: string | number
  /** Width */
  width?: string | number
  /** Whether to scroll when content overflows */
  scrollable?: boolean
}

/**
 * List component - scrollable list with template support
 */
export interface ListProps {
  /** Items to render (array or path to array) */
  items: ValueReference<unknown[]>
  /** Template component to render for each item */
  itemTemplate: {
    componentId: string
    scopedPath: { path: string; scope?: string }
  }
  /** Gap between items */
  gap?: number
  /** Whether the list is scrollable */
  scrollable?: boolean
}

/**
 * ========================================
 * DISPLAY COMPONENTS
 * ========================================
 */

/**
 * Text component - with Markdown support
 */
export interface TextProps {
  /** Text content (supports Markdown) */
  content: ValueReference<string>
  /** Text size */
  size?: 'small' | 'medium' | 'large' | 'xlarge'
  /** Text weight */
  weight?: 'normal' | 'medium' | 'semibold' | 'bold'
  /** Text color */
  color?: string
  /** Whether to render as Markdown */
  markdown?: boolean
  /** Maximum lines (with ellipsis) */
  maxLines?: number
  /** Text alignment */
  align?: 'left' | 'center' | 'right' | 'justify'
  /** Margin */
  margin?: number | [number, number, number, number]
}

/**
 * Image component
 */
export interface ImageProps {
  /** Image URL */
  src: ValueReference<string>
  /** Alternative text */
  alt?: ValueReference<string>
  /** Width */
  width?: string | number
  /** Height */
  height?: string | number
  /** Object fit */
  fit?: 'cover' | 'contain' | 'fill' | 'none' | 'scale-down'
  /** Border radius */
  borderRadius?: number
  /** Click action */
  onClick?: string
}

/**
 * Icon component
 */
export interface IconProps {
  /** Icon name or identifier */
  name: ValueReference<string>
  /** Icon size */
  size?: number | 'small' | 'medium' | 'large' | 'xlarge'
  /** Icon color */
  color?: string
  /** Click action */
  onClick?: string
}

/**
 * Video component
 */
export interface VideoProps {
  /** Video URL */
  src: ValueReference<string>
  /** Whether to autoplay */
  autoplay?: boolean
  /** Whether to show controls */
  controls?: boolean
  /** Whether to loop */
  loop?: boolean
  /** Whether to mute */
  muted?: boolean
  /** Width */
  width?: string | number
  /** Height */
  height?: string | number
}

/**
 * AudioPlayer component
 */
export interface AudioPlayerProps {
  /** Audio URL */
  src: ValueReference<string>
  /** Whether to autoplay */
  autoplay?: boolean
  /** Whether to loop */
  loop?: boolean
}

/**
 * Divider component
 */
export interface DividerProps {
  /** Divider orientation */
  orientation?: 'horizontal' | 'vertical'
  /** Thickness */
  thickness?: number
  /** Color */
  color?: string
  /** Margin */
  margin?: number | [number, number, number, number]
}

/**
 * ========================================
 * INTERACTION COMPONENTS
 * ========================================
 */

/**
 * Button component
 */
export interface ButtonProps {
  /** Button label */
  label: ValueReference<string>
  /** Button variant */
  variant?: 'primary' | 'borderless' | 'secondary' | 'danger'
  /** Button size */
  size?: 'small' | 'medium' | 'large'
  /** Whether the button is disabled */
  disabled?: ValueReference<boolean>
  /** Icon to display */
  icon?: string
  /** Icon position */
  iconPosition?: 'left' | 'right'
  /** Click action */
  onClick?: string
}

/**
 * TextField component
 */
export interface TextFieldProps {
  /** Field value (two-way binding) */
  value: ValueReference<string | number>
  /** Placeholder text */
  placeholder?: ValueReference<string>
  /** Field type */
  type?: 'shortText' | 'longText' | 'number' | 'obscured'
  /** Label */
  label?: ValueReference<string>
  /** Hint text */
  hint?: ValueReference<string>
  /** Whether the field is disabled */
  disabled?: ValueReference<boolean>
  /** Whether the field is read-only */
  readonly?: ValueReference<boolean>
  /** Minimum value (for number type) */
  min?: number
  /** Maximum value (for number type) */
  max?: number
  /** Maximum length (for text types) */
  maxLength?: number
  /** Change action */
  onChange?: string
  /** Submit action (triggered on Enter) */
  onSubmit?: string
}

/**
 * CheckBox component
 */
export interface CheckBoxProps {
  /** Checkbox state (two-way binding) */
  checked: ValueReference<boolean>
  /** Label */
  label?: ValueReference<string>
  /** Whether the checkbox is disabled */
  disabled?: ValueReference<boolean>
  /** Change action */
  onChange?: string
}

/**
 * DateTimeInput component
 */
export interface DateTimeInputProps {
  /** Date/time value (two-way binding) */
  value: ValueReference<string>
  /** Input type */
  type?: 'date' | 'time' | 'datetime'
  /** Label */
  label?: ValueReference<string>
  /** Hint text */
  hint?: ValueReference<string>
  /** Whether the input is disabled */
  disabled?: ValueReference<boolean>
  /** Minimum date/time */
  min?: string
  /** Maximum date/time */
  max?: string
  /** Change action */
  onChange?: string
}

/**
 * ChoicePicker component
 */
export interface ChoicePickerProps {
  /** Selected value(s) (two-way binding) */
  value: ValueReference<string | string[]>
  /** Available choices */
  choices: ValueReference<Choice[]>
  /** Selection mode */
  mode?: 'single' | 'multiple'
  /** Display style */
  style?: 'dropdown' | 'chips' | 'list' | 'buttons'
  /** Label */
  label?: ValueReference<string>
  /** Hint text */
  hint?: ValueReference<string>
  /** Whether the picker is disabled */
  disabled?: ValueReference<boolean>
  /** Change action */
  onChange?: string
}

/**
 * Choice option
 */
export interface Choice {
  /** Choice value */
  value: string
  /** Choice label */
  label: string
  /** Optional description */
  description?: string
  /** Optional icon */
  icon?: string
  /** Whether this choice is disabled */
  disabled?: boolean
}

/**
 * Slider component
 */
export interface SliderProps {
  /** Slider value (two-way binding) */
  value: ValueReference<number>
  /** Minimum value */
  min: number
  /** Maximum value */
  max: number
  /** Step size */
  step?: number
  /** Label */
  label?: ValueReference<string>
  /** Whether to show value label */
  showValue?: boolean
  /** Whether the slider is disabled */
  disabled?: ValueReference<boolean>
  /** Change action */
  onChange?: string
}

/**
 * Form field definition
 */
export interface FormField {
  /** Field name */
  name: string
  /** Field label */
  label?: string
  /** Field type */
  type?: 'text' | 'textarea' | 'select' | 'date' | 'number' | 'checkbox'
  /** Whether the field is required */
  required?: boolean
  /** Default value */
  defaultValue?: unknown
  /** Field options (for select type) */
  options?: {
    /** Placeholder text */
    placeholder?: string
    /** Number of rows for textarea */
    rows?: number
    /** Select options */
    options?: Array<{ value: string; label: string }>
  }
}

/**
 * Form component
 */
export interface FormProps {
  /** Form fields */
  fields: ValueReference<FormField[]>
  /** Submit button label */
  submitLabel?: ValueReference<string>
  /** Form action URL */
  action?: string
  /** Submit action */
  onSubmit?: string
}

/**
 * ========================================
 * CONTAINER COMPONENTS
 * ========================================
 */

/**
 * Card component
 */
export interface CardProps {
  /** Card title */
  title?: ValueReference<string>
  /** Card subtitle */
  subtitle?: ValueReference<string>
  /** Card content */
  content?: ValueReference<string>
  /** Card actions */
  actions?: ValueReference<CardAction[]>
  /** Whether the card is elevated */
  elevated?: boolean
  /** Border radius */
  borderRadius?: number
  /** Padding */
  padding?: number
}

/**
 * Card action
 */
export interface CardAction {
  /** Action label */
  label: string
  /** Action ID */
  action: string
  /** Whether this is a primary action */
  primary?: boolean
}

/**
 * Tabs component
 */
export interface TabsProps {
  /** Active tab (two-way binding) */
  value: ValueReference<string>
  /** Tab definitions */
  tabs: ValueReference<Tab[]>
  /** Tab position */
  position?: 'top' | 'bottom' | 'left' | 'right'
  /** Change action */
  onChange?: string
}

/**
 * Tab definition
 */
export interface Tab {
  /** Tab ID */
  id: string
  /** Tab label */
  label: string
  /** Optional icon */
  icon?: string
  /** Whether the tab is disabled */
  disabled?: boolean
}

/**
 * Modal component
 */
export interface ModalProps {
  /** Whether the modal is open (two-way binding) */
  open: ValueReference<boolean>
  /** Modal title */
  title?: ValueReference<string>
  /** Modal content */
  content?: ValueReference<string>
  /** Whether to show close button */
  showClose?: boolean
  /** Whether clicking outside closes the modal */
  closeOnBackdropClick?: boolean
  /** Modal width */
  width?: string | number
  /** Modal height */
  height?: string | number
  /** Close action */
  onClose?: string
}

/**
 * ========================================
 * CHART COMPONENT
 * ========================================
 */

/**
 * ECharts option type (loose typing for flexibility)
 */
export type EChartsOption = Record<string, unknown>

/**
 * Chart component - ECharts wrapper
 */
export interface ChartProps {
  /** ECharts complete option configuration */
  option: ValueReference<EChartsOption>
  /** Chart width, default '100%' */
  width?: string | number
  /** Chart height, default '300px' */
  height?: string | number
  /** Auto resize on container size change, default true */
  autoResize?: boolean
  /** Chart theme */
  theme?: 'light' | 'dark' | string
  /** Renderer type */
  renderer?: 'canvas' | 'svg'
}

/**
 * ========================================
 * COMPONENT TYPE MAPPINGS
 * ========================================
 */

/**
 * Mapping of component type names to their props
 */
export interface ComponentPropsMap {
  // Layout
  Row: RowProps
  Column: ColumnProps
  List: ListProps

  // Display
  Text: TextProps
  Image: ImageProps
  Icon: IconProps
  Video: VideoProps
  AudioPlayer: AudioPlayerProps
  Divider: DividerProps

  // Interaction
  Button: ButtonProps
  TextField: TextFieldProps
  CheckBox: CheckBoxProps
  DateTimeInput: DateTimeInputProps
  ChoicePicker: ChoicePickerProps
  Slider: SliderProps
  Form: FormProps

  // Container
  Card: CardProps
  Tabs: TabsProps
  Modal: ModalProps

  // Chart
  Chart: ChartProps
}

/**
 * Component type names
 */
export type ComponentType = keyof ComponentPropsMap

/**
 * Get props for a component type
 */
export type GetComponentProps<T extends ComponentType> = ComponentPropsMap[T]

/**
 * Generic component definition
 */
export interface ComponentDefinition<T extends ComponentType = ComponentType> {
  type: T
  props?: GetComponentProps<T>
}
