<template>
  <div class="message-playground">
    <h2>Message Playground</h2>
    <p class="hint">Enter A2UI messages (JSONL format) to see them rendered</p>

    <div class="playground-container">
      <div class="editor-section">
        <h3>Messages (JSONL)</h3>
        <textarea
          v-model="messageInput"
          class="message-editor"
          placeholder="Enter A2UI messages here..."
          @input="handleInput"
        />
        <div class="editor-actions">
          <A2uiButton :label="'Clear'" :variant="'secondary'" @click="clearMessages" />
          <A2uiButton :label="'Render'" :variant="'primary'" @click="renderMessages" />
        </div>
      </div>

      <div class="preview-section">
        <h3>Preview</h3>
        <div class="preview-content">
          <A2uiRenderer
            v-if="currentSurfaceId"
            :surface-id="currentSurfaceId"
            @event="handleEvent"
          />
          <div v-else class="empty-preview">
            <A2uiText :content="'No surface to render. Enter messages and click Render.'" />
          </div>
        </div>
      </div>
    </div>

    <A2uiCard title="Example Messages">
      <A2uiColumn :gap="8">
        <A2uiText :content="'Click an example below to load it:'" />
        <A2uiRow :gap="8">
          <A2uiButton
            v-for="example in examples"
            :key="example.name"
            :label="example.name"
            :variant="'borderless'"
            @click="loadExample(example)"
          />
        </A2uiRow>
      </A2uiColumn>
    </A2uiCard>

    <A2uiCard v-if="lastEvent" title="Last Event">
      <A2uiColumn :gap="8">
        <A2uiText :content="'Component: ' + lastEvent.componentId" />
        <A2uiText :content="'Event: ' + lastEvent.eventType" />
        <A2uiText :content="'Payload: ' + JSON.stringify(lastEvent.payload)" />
      </A2uiColumn>
    </A2uiCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useA2UI } from '@a2ui/vue-plugin'

const messageInput = ref('')
const currentSurfaceId = ref('playground')
const lastEvent = ref<any>(null)

const { handleMessage, sendAction } = useA2UI()

const examples = [
  {
    name: 'Hello World',
    messages: `{"version":"v0.9","type":"createSurface","surfaceId":"playground","components":[{"componentId":"root","type":"Column","props":{"gap":16},"children":[{"type":"text","content":"Hello, World!"},{"type":"component","componentId":"btn1"}]}]},{"version":"v0.9","type":"updateComponents","surfaceId":"playground","updates":[{"componentId":"btn1","type":"Button","props":{"label":"Click Me","variant":"primary"}}]}`,
  },
  {
    name: 'Counter',
    messages: `{"version":"v0.9","type":"createSurface","surfaceId":"playground","dataModel":{"count":0},"components":[{"componentId":"root","type":"Column","props":{"gap":16,"align":"center"},"children":[{"type":"component","componentId":"title"},{"type":"component","componentId":"counter"},{"type":"component","componentId":"btnInc"},{"type":"component","componentId":"btnDec"}]}]},{"version":"v0.9","type":"updateComponents","surfaceId":"playground","updates":[{"componentId":"title","type":"Text","props":{"content":"Counter Example","size":"large"}},{"componentId":"counter","type":"Text","props":{"content":{"path":"/count"},"size":"xlarge"}},{"componentId":"btnInc","type":"Button","props":{"label":"Increment"}},{"componentId":"btnDec","type":"Button","props":{"label":"Decrement"}}]}`,
  },
  {
    name: 'Card Layout',
    messages: `{"version":"v0.9","type":"createSurface","surfaceId":"playground","components":[{"componentId":"root","type":"Column","props":{"gap":16},"children":[{"type":"component","componentId":"card1"}]}]},{"version":"v0.9","type":"updateComponents","surfaceId":"playground","updates":[{"componentId":"card1","type":"Card","props":{"title":"Welcome","subtitle":"This is a card component","content":"Cards are great for grouping related content.","actions":[{"label":"Learn More","action":"learn","primary":true},{"label":"Cancel","action":"cancel"}]}}]}`,
  },
]

const handleInput = () => {
  // Auto-parse on input for better UX
}

const clearMessages = () => {
  messageInput.value = ''
  currentSurfaceId.value = ''
}

const renderMessages = () => {
  try {
    const lines = messageInput.value.trim().split('\n')

    for (const line of lines) {
      if (line.trim()) {
        const message = JSON.parse(line)
        handleMessage(message)
      }
    }
  } catch (error) {
    alert('Invalid JSON: ' + (error as Error).message)
  }
}

const loadExample = (example: { messages: string }) => {
  messageInput.value = example.messages
  renderMessages()
}

const handleEvent = (componentId: string, eventType: string, payload?: unknown) => {
  lastEvent.value = { componentId, eventType, payload }

  // Create user action message
  const action = {
    version: 'v0.9' as const,
    type: 'userAction' as const,
    surfaceId: currentSurfaceId.value,
    action: {
      componentId,
      event: eventType,
      payload,
      timestamp: Date.now(),
    },
  }

  sendAction(action)
}
</script>

<style scoped>
.message-playground {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 8px;
}

.hint {
  color: #666;
  margin-bottom: 16px;
}

.playground-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.editor-section,
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.message-editor {
  flex: 1;
  min-height: 300px;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  resize: vertical;
}

.message-editor:focus {
  outline: none;
  border-color: #0066cc;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.preview-content {
  min-height: 300px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: #f9f9f9;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
  color: #999;
}

@media (max-width: 768px) {
  .playground-container {
    grid-template-columns: 1fr;
  }
}
</style>
