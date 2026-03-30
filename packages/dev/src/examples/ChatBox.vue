<template>
  <div class="chat-box">
    <!-- Header -->
    <div class="chat-header">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
          </svg>
        </div>
        <div class="header-info">
          <h1>A2UI Chat</h1>
          <span class="status" :class="{ connected: isConnected }">
            {{ isConnected ? '已连接' : '未连接' }}
          </span>
        </div>
      </div>
      <div class="header-actions">
        <button class="icon-btn" @click="clearChat" title="清空对话">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Messages Area -->
    <div class="messages-container" ref="messagesContainer">
      <div class="messages">
        <!-- Welcome Message -->
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
            </svg>
          </div>
          <h2>A2UI Chat 测试工具</h2>
          <p>用于测试后端 LangChain4j Agent 功能</p>
          <div class="quick-prompts">
            <button
              v-for="prompt in quickPrompts"
              :key="prompt"
              class="quick-prompt-btn"
              @click="sendQuickPrompt(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </div>

        <!-- Message List -->
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message"
          :class="message.role"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="avatar user-avatar">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </div>
            <div v-else class="avatar assistant-avatar">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
              </svg>
            </div>
          </div>
          <div class="message-content">
            <!-- User message -->
            <div v-if="message.role === 'user'" class="text">
              {{ message.content }}
            </div>
            <!-- Assistant message with A2UI components -->
            <template v-else>
              <!-- Loading indicator -->
              <div v-if="message.loading" class="loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <!-- A2UI Rendered content -->
              <A2uiRenderer
                v-else
                :surface-id="message.surfaceId"
                @event="(componentId: string, eventType: string, payload: unknown) => handleComponentEvent(message.surfaceId, componentId, eventType, payload)"
              />
              <!-- Raw text fallback -->
              <div v-if="message.text" class="text markdown" v-html="renderMarkdown(message.text)"></div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="input-area">
      <div class="input-container">
        <textarea
          ref="inputRef"
          v-model="inputText"
          placeholder="输入消息..."
          rows="1"
          :disabled="isLoading"
          @keydown.enter.exact.prevent="sendMessage"
          @input="autoResize"
        />
        <button
          class="send-btn"
          :disabled="!inputText.trim() || isLoading"
          @click="sendMessage"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
          <svg v-else class="spinner" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 4V2C6.48 2 2 6.48 2 12h2c0-4.42 3.58-8 8-8z"/>
          </svg>
        </button>
      </div>
      <div class="input-hint">
        按 Enter 发送，Shift + Enter 换行
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

// Types
interface Message {
  role: 'user' | 'assistant'
  content?: string
  text?: string
  surfaceId: string
  loading?: boolean
}

// State
const messages = ref<Message[]>([])
const inputText = ref('')
const isLoading = ref(false)
const isConnected = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLTextAreaElement | null>(null)

// Config
const backendUrl = 'http://localhost:8080'
let messageSurfaceId = 0

// A2UI
const { handleMessage } = useA2UI()

// Quick prompts
const quickPrompts = [
  '查询本季度销售数据并生成柱状图',
  '用饼图展示各类别销售占比',
  '查询月度销售趋势并画折线图',
  '显示销量前5的产品',
]

// Auto resize textarea
const autoResize = () => {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
    inputRef.value.style.height = Math.min(inputRef.value.scrollHeight, 150) + 'px'
  }
}

// Scroll to bottom
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Clear chat
const clearChat = () => {
  messages.value = []
  messageSurfaceId = 0
}

// Add user message
const addUserMessage = (content: string) => {
  messages.value.push({
    role: 'user',
    content,
    surfaceId: `user_${Date.now()}`
  })
  scrollToBottom()
}

// Add assistant message
const addAssistantMessage = () => {
  const surfaceId = `assistant_${++messageSurfaceId}`
  messages.value.push({
    role: 'assistant',
    surfaceId,
    loading: true
  })
  scrollToBottom()
  return surfaceId
}

// Update assistant message
const updateAssistantMessage = (surfaceId: string) => {
  const msg = messages.value.find(m => m.surfaceId === surfaceId)
  if (msg) {
    msg.loading = false
  }
}

// Send message
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  inputText.value = ''
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }

  addUserMessage(text)
  isLoading.value = true

  const surfaceId = addAssistantMessage()

  try {
    await sendToBackend(text, surfaceId)
    isConnected.value = true
  } catch (error) {
    console.error('Send error:', error)
    const msg = messages.value.find(m => m.surfaceId === surfaceId)
    if (msg) {
      msg.loading = false
      msg.text = `**错误:** ${(error as Error).message}`
    }
    isConnected.value = false
  } finally {
    isLoading.value = false
    updateAssistantMessage(surfaceId)
    scrollToBottom()
  }
}

// Send quick prompt
const sendQuickPrompt = (prompt: string) => {
  inputText.value = prompt
  sendMessage()
}

// Send to backend via SSE
const sendToBackend = async (message: string, surfaceId: string) => {
  const response = await fetch(
    `${backendUrl}/api/chat/stream?message=${encodeURIComponent(message)}`
  )

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }

  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  // Initialize surface
  handleMessage(JSON.stringify({
    type: 'surface',
    surfaceId,
    name: 'Response'
  }) + '\n')

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    const text = decoder.decode(value)
    buffer += text

    // Process complete lines
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const json = line.substring(5).trim()
        if (json) {
          try {
            // Update surfaceId in the message to match our surface
            const data = JSON.parse(json)
            if (data.surfaceId) {
              data.surfaceId = surfaceId
            }
            handleMessage(JSON.stringify(data) + '\n')
          } catch {
            // If not valid JSON, treat as text
            console.warn('Failed to parse:', json)
          }
        }
      }
    }
  }

  // Process remaining buffer
  if (buffer.startsWith('data:')) {
    const json = buffer.substring(5).trim()
    if (json) {
      try {
        const data = JSON.parse(json)
        if (data.surfaceId) {
          data.surfaceId = surfaceId
        }
        handleMessage(JSON.stringify(data) + '\n')
      } catch {
        console.warn('Failed to parse remaining:', json)
      }
    }
  }
}

// Handle component events
const handleComponentEvent = (surfaceId: string, componentId: string, eventType: string, payload: unknown) => {
  console.log('Component event:', { surfaceId, componentId, eventType, payload })
}

// Simple markdown renderer
const renderMarkdown = (text: string): string => {
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

// Focus input on mount
onMounted(() => {
  inputRef.value?.focus()
})
</script>

<style scoped>
.chat-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f7f7f8;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.header-info h1 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #202123;
}

.header-info .status {
  font-size: 12px;
  color: #999;
}

.header-info .status.connected {
  color: #10a37f;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  transition: background 0.2s;
}

.icon-btn:hover {
  background: #f0f0f0;
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.messages {
  max-width: 768px;
  margin: 0 auto;
  padding: 20px;
}

.welcome {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.welcome h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #202123;
}

.welcome p {
  font-size: 14px;
  color: #666;
  margin: 0 0 32px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.quick-prompt-btn {
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-prompt-btn:hover {
  background: #f0f0f0;
  border-color: #ccc;
}

/* Message */
.message {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  background: #5436da;
  color: white;
}

.assistant-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message .text {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message.user .text {
  background: #5436da;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .text {
  background: #fff;
  color: #202123;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message.assistant .text.markdown {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

/* Loading dots */
.loading {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 16px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.loading .dot {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
}

.loading .dot:nth-child(1) { animation-delay: 0s; }
.loading .dot:nth-child(2) { animation-delay: 0.2s; }
.loading .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* Input */
.input-area {
  padding: 16px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
}

.input-container {
  max-width: 768px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f7f7f8;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 8px 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-container:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.input-container textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  outline: none;
  max-height: 150px;
  font-family: inherit;
}

.input-container textarea::placeholder {
  color: #999;
}

.send-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #5436da;
  border-radius: 8px;
  cursor: pointer;
  color: white;
  transition: background 0.2s, opacity 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #4a2fc9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn .spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-hint {
  max-width: 768px;
  margin: 8px auto 0;
  font-size: 12px;
  color: #999;
  text-align: center;
}
</style>
