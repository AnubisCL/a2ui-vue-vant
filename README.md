# A2UI Vue SDK

> A2UI (Agent to UI) Vue SDK - Declarative UI protocol implementation for Vue 3

[简体中文](./README.zh-CN.md)

A2UI is Google's declarative UI protocol for AI Agent to user interaction. This SDK enables Vue 3 applications to receive, parse, and render A2UI message streams from AI Agents.

## Features

- 🚀 **Vue 3 Native** - Built with Vue 3 Composition API and TypeScript
- 📦 **Based on Vant 4** - Mobile-first UI component library
- 🎨 **29+ Components** - Complete component library covering all use cases
- 🔄 **Reactive Data Binding** - Two-way data binding with JSON Pointer support
- 🌊 **Streaming Support** - Real-time streaming protocol support
- 📝 **Markdown Support** - Full Markdown support via markdown-it
- 🎨 **UnoCSS Powered** - Atomic CSS for optimal performance
- 📱 **Mobile First** - Optimized for mobile H5 applications

## Packages

| Package | Description |
|---------|-------------|
| `@a2ui/core` | Framework-agnostic core SDK (types, parser, store) |
| `@a2ui/vue-components` | Vue 3 component implementations based on Vant 4 |
| `@a2ui/vue-plugin` | Vue plugin for easy integration |
| `@a2ui/dev` | Development and example app |

## Directory Structure

```
a2ui-vue/
├── packages/                    # Monorepo packages
│   ├── core/                    # @a2ui/core - Core SDK
│   │   ├── src/
│   │   │   ├── parser/          # Message parser
│   │   │   │   ├── message-parser.ts
│   │   │   │   └── parser.ts
│   │   │   ├── registry/        # Component registry
│   │   │   │   ├── component-registry.ts
│   │   │   │   └── catalog-validator.ts
│   │   │   ├── store/           # Data store
│   │   │   │   ├── data-model-store.ts
│   │   │   │   ├── json-pointer.ts
│   │   │   │   └── store.ts
│   │   │   ├── surface/         # Surface management
│   │   │   │   ├── surface-manager.ts
│   │   │   │   └── surface.ts
│   │   │   ├── tree-builder/    # Component tree builder
│   │   │   │   └── tree-builder.ts
│   │   │   ├── types/           # TypeScript type definitions
│   │   │   │   ├── components.ts   # Component Props types
│   │   │   │   ├── messages.ts     # Message types
│   │   │   │   └── ...
│   │   │   └── index.ts
│   │   └── package.json
│   │
│   ├── vue-components/          # @a2ui/vue-components - Vue components
│   │   ├── src/
│   │   │   ├── components/      # Component implementations
│   │   │   │   ├── common/      # Common components (Badge, Tag, Progress...)
│   │   │   │   ├── container/   # Container components (Card, Tabs, Modal)
│   │   │   │   ├── display/     # Display components (Text, Image, Icon...)
│   │   │   │   ├── input/       # Input components (Button, TextField...)
│   │   │   │   └── layout/      # Layout components (Row, Column, List)
│   │   │   ├── composables/     # Vue Composables
│   │   │   │   ├── useA2UI.ts      # Main entry composable
│   │   │   │   ├── useSurface.ts
│   │   │   │   ├── useDataModel.ts
│   │   │   │   └── useComponentRegistry.ts
│   │   │   ├── utils/           # Utility functions
│   │   │   │   ├── vant-props.ts    # Vant Props mapping
│   │   │   │   └── value-reference.ts
│   │   │   ├── styles/          # Style files
│   │   │   └── index.ts
│   │   └── package.json
│   │
│   ├── vue-plugin/              # @a2ui/vue-plugin - Vue plugin
│   │   ├── src/
│   │   │   ├── A2uiRenderer.vue    # Main renderer component
│   │   │   ├── plugin.ts           # Vue plugin definition
│   │   │   └── index.ts
│   │   └── package.json
│   │
│   └── dev/                     # @a2ui/dev - Development examples
│       ├── src/
│       │   ├── examples/        # Example pages
│       │   │   ├── BasicExample.vue
│       │   │   ├── FormExample.vue
│       │   │   ├── StreamingDemo.vue   # Streaming protocol demo
│       │   │   └── CommonComponents.vue
│       │   ├── components/      # Dev components
│       │   │   └── MessagePlayground.vue
│       │   ├── router/          # Router configuration
│       │   ├── App.vue
│       │   └── main.ts
│       └── package.json
│
├── docs/                        # Documentation
│   └── INTEGRATION.md           # Integration guide (Chinese)
│
├── uno.config.ts                # UnoCSS configuration
├── tsconfig.base.json           # TypeScript base config
├── pnpm-workspace.yaml          # pnpm workspace config
└── package.json                 # Root package.json
```

### Key Files

| File | Description |
|------|-------------|
| `packages/core/src/types/components.ts` | Props type definitions for all components |
| `packages/core/src/types/messages.ts` | A2UI message protocol type definitions |
| `packages/vue-components/src/composables/useA2UI.ts` | Main entry composable, manages global state |
| `packages/vue-plugin/src/A2uiRenderer.vue` | Main renderer, responsible for rendering component tree |
| `packages/vue-plugin/src/plugin.ts` | Vue plugin, registers components and provides dependency injection |

## Installation

```bash
# Install dependencies
pnpm install

# Start development server
pnpm dev

# Build all packages
pnpm build

# Run tests
pnpm test
```

## Quick Start

### 1. Install the plugin

```typescript
// main.ts
import { createApp } from 'vue'
import App from './App.vue'
import { createA2UI } from '@a2ui/vue-plugin'
import '@a2ui/vue-components/style.css'
import 'vant/lib/index.css'

const app = createApp(App)
app.use(createA2UI())
app.mount('#app')
```

### 2. Use the renderer

```vue
<template>
  <A2uiRenderer :surface-id="surfaceId" @event="handleEvent" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const surfaceId = ref('main')
const { connect, handleMessage } = useA2UI()

// Connect to WebSocket for streaming
const ws = new WebSocket('ws://your-agent-endpoint')
ws.onmessage = (event) => {
  handleMessage(event.data)
}
```

### 3. Mock Streaming Demo

```typescript
import { useA2UI } from '@a2ui/vue-plugin'

const { handleMessage } = useA2UI()

// Simulate streaming messages
async function mockStreamingChat(message: string) {
  const responses = [
    // Surface definition
    '{"type":"surface","surfaceId":"chat","name":"Chat Surface"}\n',

    // Component updates (streaming)
    '{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"Thinking..."}}}\n',

    // Update component (streaming text)
    '{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"Hello! How can I help you?"}}}\n',

    // Add input component
    '{"type":"component","componentId":"input1","surfaceId":"chat","component":{"type":"TextField","props":{"placeholder":"Type your message...","label":"Message"}}}\n',
  ]

  for (const msg of responses) {
    await new Promise(r => setTimeout(r, 500))
    handleMessage(msg)
  }
}
```

## Component Library

### Layout Components
| Component | Vant Base | Description |
|-----------|-----------|-------------|
| Row | - | Horizontal flex container (UnoCSS) |
| Column | - | Vertical flex container (UnoCSS) |
| List | van-list | Scrollable list with lazy loading |

### Display Components
| Component | Vant Base | Description |
|-----------|-----------|-------------|
| Text | - | Text with Markdown support |
| Image | van-image | Image with lazy loading |
| Icon | van-icon | Icon display |
| Chart | - | ECharts-based data visualization |
| Video | native | Video player |
| AudioPlayer | native | Audio player with controls |
| Divider | van-divider | Horizontal/vertical divider |

### Input Components
| Component | Vant Base | Description |
|-----------|-----------|-------------|
| Button | van-button | Clickable button |
| TextField | van-field | Text input field |
| CheckBox | van-checkbox | Checkbox input |
| Slider | van-slider | Range slider |
| ChoicePicker | van-radio-group | Single/multiple choice |
| DateTimeInput | van-datetime-picker | Date/time picker |

### Container Components
| Component | Vant Base | Description |
|-----------|-----------|-------------|
| Card | van-cell-group | Card container |
| Tabs | van-tabs | Tabbed container |
| Modal | van-popup | Modal dialog |

### Common Components
| Component | Vant Base | Description |
|-----------|-----------|-------------|
| Badge | van-badge | Count or status badge |
| Avatar | van-image | User avatar |
| Progress | van-progress | Progress bar |
| Spinner | van-loading | Loading indicator |
| Tag | van-tag | Label or category tag |
| Alert | van-notice-bar | Alert notification |
| Switch | van-switch | Toggle switch |
| Tooltip | van-popover | Hover tooltip |
| Skeleton | van-skeleton | Loading placeholder |
| Empty | van-empty | Empty state display |

## A2UI Protocol

### Message Types

```typescript
// Surface definition
{
  "type": "surface",
  "surfaceId": "main",
  "name": "Main Surface"
}

// Component creation/update
{
  "type": "component",
  "componentId": "button1",
  "surfaceId": "main",
  "component": {
    "type": "Button",
    "props": {
      "label": "Click Me",
      "variant": "primary"
    }
  }
}

// Data model update
{
  "type": "data",
  "path": "/user/name",
  "value": "John Doe"
}
```

### Streaming Format

Messages are sent in JSONL format (JSON Lines):
```
{"type":"surface","surfaceId":"chat","name":"Chat"}
{"type":"component","componentId":"msg1","surfaceId":"chat","component":{"type":"Text","props":{"content":"Hello"}}}
{"type":"data","path":"/count","value":42}
```

## Props Mapping

A2UI props are automatically mapped to Vant props:

| A2UI Prop | Vant Prop | Notes |
|-----------|-----------|-------|
| `variant: 'primary'` | `type: 'primary'` | Button variant |
| `size: 'medium'` | `size: 'normal'` | Component size |
| `disabled` | `disabled` | Direct mapping |
| `label` | `label` | Form field label |

## Development

```bash
# Start dev server
pnpm dev

# Build library
pnpm build

# Type check
pnpm typecheck

# Run tests
pnpm test
```

## Browser Support

- Chrome >= 80
- Safari >= 13.1
- Firefox >= 78
- iOS Safari >= 13.4
- Android Chrome >= 80

## License

MIT

## Links

- [Vant 4 Documentation](https://vant-ui.github.io/vant/#/en-US)
- [A2UI Protocol Spec](https://github.com/google/a2ui)
- [UnoCSS Documentation](https://unocss.dev/)
