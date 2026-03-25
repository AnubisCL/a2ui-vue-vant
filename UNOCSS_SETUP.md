# UnoCSS Integration Guide

## Overview

This project now uses UnoCSS for atomic CSS styling, providing better performance and developer experience.

## Configuration

### Root Config (`uno.config.ts`)

The root UnoCSS configuration includes:
- **Preset Uno**: Base utilities
- **Preset Attributify**: Attribute mode support
- **Transformers**: Directives and variant groups
- **Theme**: Custom design tokens

### Theme Design Tokens

```typescript
colors: {
  primary: '#0066cc',
  secondary: '#666666',
  success: '#10b981',
  warning: '#f59e0b',
  danger: '#dc3545',
  // ... more colors
}
```

## Component Styling

### Before (Inline Styles)

```vue
<template>
  <div :style="{ display: 'flex', flexDirection: 'row', gap: '8px' }">
    <slot />
  </div>
</template>
```

### After (UnoCSS)

```vue
<template>
  <div class="flex flex-row gap-2">
    <slot />
  </div>
</template>
```

## Available Utility Classes

### Layout
- `flex` - Display flex
- `flex-row` / `flex-col` - Flex direction
- `gap-{size}` - Gap between items
- `p-{size}` - Padding
- `m-{size}` - Margin

### Colors
- `text-{color}` - Text color
- `bg-{color}` - Background color
- `border-{color}` - Border color

### Spacing
- `p-0`, `p-1`, `p-2`... - Padding
- `m-0`, `m-1`, `m-2`... - Margin
- `px`, `py`, `pt`, `pb`... - Specific edges

### Typography
- `text-xs`, `text-sm`, `text-base`... - Font size
- `font-normal`, `font-medium`, `font-bold`... - Font weight
- `text-left`, `text-center`, `text-right`... - Alignment

### Borders
- `border` - Add border
- `rounded-{size}` - Border radius
- `border-{color}` - Border color

### Interactive
- `cursor-pointer` - Pointer cursor
- `hover:{class}` - Hover state
- `disabled:opacity-50` - Disabled state

## Dynamic Classes

For dynamic values, use bracket notation:

```vue
<template>
  <div class="gap-[{{ gap }}px]">
  <!-- or -->
  <div :class="`gap-[${gap}px]`">
</template>
```

## Component-Specific Classes

### Button
- `a2ui-button` - Base button
- `a2ui-button--{size}` - Size variant
- `a2ui-button--{variant}` - Color variant

### Input
- `a2ui-textfield__input` - Text input
- `a2ui-checkbox` - Checkbox
- `a2ui-slider__input` - Slider input

### Card
- `a2ui-card` - Base card
- `a2ui-card--elevated - Elevated shadow

## Building

```bash
# Build all packages
pnpm build

# Watch for changes
pnpm --filter vue-components dev
```

## Benefits

1. **Smaller Bundle Size**: Only used CSS is included
2. **Faster Development**: No context switching between CSS and JS
3. **Type Safety**: Full autocomplete for classes
4. **Consistency**: Atomic classes ensure design system adherence
5. **Performance**: No runtime CSS-in-JS overhead
