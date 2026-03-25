# A2UI Vue SDK

> A2UI (Agent to UI) Vue SDK - Declarative UI protocol implementation for Vue 3

A2UI is Google's declarative UI protocol for AI Agent to user interaction. This SDK enables Vue 3 applications to receive, parse, and render A2UI message streams from AI Agents.

## Features

- 🚀 **Vue 3 Native** - Built with Vue 3 Composition API and TypeScript
- 📦 **Complete Component Library** - All basic catalog components supported
- 🎨 **Common Components** - Frequently used UI components (Badge, Avatar, Progress, etc.)
- 🔄 **Reactive Data Binding** - Two-way data binding with JSON Pointer support
- 🎨 **UnoCSS Powered** - Atomic CSS for optimal performance and customization
- ✨ **Customizable** - Easy component registration and customization
- 📝 **Markdown Support** - Full Markdown support via markdown-it
- 🔌 **Easy Integration** - Vue plugin for quick setup

## Packages

- `@a2ui/core` - Framework-agnostic core SDK
- `@a2ui/vue-components` - Vue component implementations
- `@a2ui/vue-plugin` - Vue plugin for easy integration
- `@a2ui/dev` - Development and example app

## Installation

```bash
pnpm install
```

## Development

```bash
# Start development server
pnpm dev

# Run tests
pnpm test

# Build all packages
pnpm build

# Run linting
pnpm lint
```

## Quick Start

```vue
<template>
  <A2uiRenderer :surface-id="surfaceId" />
</template>

<script setup lang="ts">
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const { connect, handleMessage } = useA2UI()
const surfaceId = ref('main')

// Connect to your agent and handle messages
connect('ws://your-agent-endpoint')
</script>
```

## Component Library

### A2UI Basic Components (18 components)

**Layout:** Row, Column, List
**Display:** Text, Image, Icon, Video, AudioPlayer, Divider
**Input:** Button, TextField, CheckBox, DateTimeInput, ChoicePicker, Slider
**Container:** Card, Tabs, Modal

### Common Components (10 components)

**UI Components:**
- **Badge** - Display count or status badge
- **Avatar** - User avatar with fallback
- **Progress** - Progress bar with percentage
- **Spinner** - Loading indicator
- **Tag** - Label or category tag
- **Alert** - Alert/notification box
- **Switch** - Toggle switch
- **Tooltip** - Hover tooltip
- **Skeleton** - Loading placeholder
- **Empty** - Empty state display

See [COMMON_COMPONENTS.md](./COMMON_COMPONENTS.md) for detailed documentation.

## Documentation

Full documentation coming soon.

## License

MIT
