<template>
  <div class="backend-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">🔌 后端对接演示</h1>
      <p class="text-sm text-gray-500">连接 Java Spring Boot 后端，测试 HTTP Streaming 和 WebSocket</p>
    </div>

    <!-- Transport Config -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="mb-4">
        <label class="text-sm text-gray-600 mb-2 block">传输协议:</label>
        <van-radio-group v-model="transport" direction="horizontal">
          <van-radio name="sse">HTTP Streaming (SSE)</van-radio>
          <van-radio name="websocket">WebSocket</van-radio>
        </van-radio-group>
      </div>

      <van-field
        v-model="backendUrl"
        label="后端地址"
        placeholder="http://localhost:8080"
        :disabled="connected"
      />
    </div>

    <!-- Connection Status -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div
            class="w-3 h-3 rounded-full"
            :class="connected ? 'bg-green-500' : 'bg-gray-300'"
          />
          <span :class="connected ? 'text-green-600' : 'text-gray-500'" class="text-sm font-medium">
            {{ connected ? '已连接' : '未连接' }}
          </span>
          <span v-if="connected" class="text-xs text-gray-400">
            ({{ transport === 'sse' ? 'HTTP SSE' : 'WebSocket' }})
          </span>
        </div>
        <div class="flex gap-2">
          <van-button
            v-if="!connected"
            type="primary"
            size="small"
            :loading="connecting"
            @click="connect"
          >
            连接
          </van-button>
          <van-button
            v-else
            type="danger"
            size="small"
            @click="disconnect"
          >
            断开
          </van-button>
          <van-button
            size="small"
            @click="clearAll"
          >
            清空
          </van-button>
        </div>
      </div>
    </div>

    <!-- A2UI Renderer -->
    <div class="bg-white rounded-xl shadow-sm p-4 min-h-[300px] mb-4">
      <div class="flex items-center justify-between mb-2">
        <h3 class="font-semibold text-gray-700">UI 渲染区</h3>
        <span class="text-xs text-gray-400">Surface: {{ surfaceId }}</span>
      </div>
      <A2uiRenderer :surface-id="surfaceId" @event="handleEvent" />
    </div>

    <!-- Message Log -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex items-center justify-between mb-2">
        <h3 class="font-semibold text-gray-700">消息日志</h3>
        <span class="text-xs text-gray-400">共 {{ messageLogs.length }} 条</span>
      </div>
      <div class="bg-gray-900 rounded-lg p-3 max-h-[250px] overflow-auto">
        <pre v-for="(log, index) in messageLogs" :key="index" class="text-xs font-mono mb-1" :class="log.type === 'sent' ? 'text-yellow-400' : 'text-green-400'">
[{{ log.time }}] {{ log.type === 'sent' ? 'SEND' : 'RECV' }}: {{ log.message }}
        </pre>
        <pre v-if="messageLogs.length === 0" class="text-xs text-gray-500">等待消息...</pre>
      </div>
    </div>

    <!-- Input Area -->
    <div class="bg-white rounded-xl shadow-sm p-4">
      <van-field
        v-model="userInput"
        placeholder="输入消息发送到后端..."
        type="textarea"
        rows="2"
        autosize
        :disabled="!connected"
        @keyup.enter.ctrl="sendMessage"
      />
      <div class="flex justify-end mt-3">
        <van-button
          type="primary"
          size="small"
          :disabled="!connected || !userInput.trim()"
          :loading="sending"
          @click="sendMessage"
        >
          发送 (Ctrl+Enter)
        </van-button>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-xl shadow-sm p-4 mt-4">
      <h3 class="font-semibold text-gray-700 mb-3">快速测试</h3>
      <div class="flex flex-wrap gap-2">
        <van-button
          v-for="action in quickActions"
          :key="action.label"
          size="small"
          :loading="sending"
          @click="sendQuickMessage(action.message)"
        >
          {{ action.label }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

// State
const transport = ref<'sse' | 'websocket'>('sse')
const backendUrl = ref('http://localhost:8080')
const connected = ref(false)
const connecting = ref(false)
const sending = ref(false)
const userInput = ref('')
const surfaceId = ref('main')
const messageLogs = ref<Array<{ time: string; type: 'sent' | 'recv'; message: string }>>([])

// WebSocket instance
let ws: WebSocket | null = null

// A2UI Hooks
const { handleMessage, clearSurface } = useA2UI()

// Computed WebSocket URL
const wsUrl = computed(() => {
  const url = backendUrl.value.replace(/^http/, 'ws')
  return url
})

// Quick actions
const quickActions = [
  { label: '查询订单', message: '查询订单状态' },
  { label: '数据统计', message: '显示数据统计' },
  { label: '生成图表', message: '生成销售图表' },
]

// Log message
const logMessage = (type: 'sent' | 'recv', message: string) => {
  const time = new Date().toLocaleTimeString()
  // Truncate long messages
  const displayMessage = message.length > 150 ? message.substring(0, 150) + '...' : message
  messageLogs.value.unshift({ time, type, message: displayMessage })
  if (messageLogs.value.length > 100) {
    messageLogs.value.pop()
  }
}

// Clear all
const clearAll = () => {
  messageLogs.value = []
  clearSurface(surfaceId.value)
  showToast('已清空')
}

// Connect
const connect = async () => {
  if (connected.value) return

  connecting.value = true

  try {
    if (transport.value === 'websocket') {
      connectWebSocket()
    } else {
      // For SSE, we test connection with health check
      await testSSEConnection()
    }
  } catch (error) {
    showToast('连接失败: ' + (error as Error).message)
    connecting.value = false
  }
}

// Test SSE connection
const testSSEConnection = async () => {
  try {
    const response = await fetch(`${backendUrl.value}/api/chat/health`)
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const data = await response.json()
    connected.value = true
    connecting.value = false
    showToast(`已连接 (LLM: ${data.llm})`)
    logMessage('recv', `Connected: ${JSON.stringify(data)}`)
  } catch (error) {
    connecting.value = false
    throw error
  }
}

// Connect WebSocket
const connectWebSocket = () => {
  const url = `${wsUrl.value}/ws/chat`
  logMessage('sent', `Connecting to ${url}`)

  ws = new WebSocket(url)

  ws.onopen = () => {
    connected.value = true
    connecting.value = false
    showToast('WebSocket 已连接')
    logMessage('recv', 'WebSocket connected')
  }

  ws.onmessage = (event) => {
    const data = event.data
    logMessage('recv', data)

    // Handle A2UI message
    if (data && data.trim()) {
      // Split by newlines in case of multiple messages
      const lines = data.split('\n')
      for (const line of lines) {
        if (line.trim()) {
          handleMessage(line + '\n')
        }
      }
    }
  }

  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
    logMessage('recv', 'WebSocket error')
    showToast('WebSocket 错误')
  }

  ws.onclose = () => {
    connected.value = false
    logMessage('recv', 'WebSocket disconnected')
  }
}

// Disconnect
const disconnect = () => {
  if (ws) {
    ws.close()
    ws = null
  }
  connected.value = false
  showToast('已断开连接')
}

// Send message via SSE
const sendSSE = async (message: string) => {
  sending.value = true
  logMessage('sent', message)

  try {
    const response = await fetch(
      `${backendUrl.value}/api/chat/stream?message=${encodeURIComponent(message)}`
    )

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const text = decoder.decode(value)

      // Parse SSE format: id:..., event:..., data:{...}
      const lines = text.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const json = line.substring(5).trim()
          if (json) {
            logMessage('recv', json)
            handleMessage(json + '\n')
          }
        }
        // Ignore other SSE lines (id:, event:, empty lines)
      }
    }
  } catch (error) {
    showToast('发送失败: ' + (error as Error).message)
    logMessage('recv', `Error: ${(error as Error).message}`)
  } finally {
    sending.value = false
  }
}

// Send message via WebSocket
const sendWS = (message: string) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    showToast('WebSocket 未连接')
    return
  }

  const payload = JSON.stringify({ message })
  ws.send(payload)
  logMessage('sent', payload)
}

// Send message
const sendMessage = async () => {
  if (!userInput.value.trim()) return

  const message = userInput.value.trim()
  userInput.value = ''

  if (transport.value === 'websocket') {
    sendWS(message)
  } else {
    await sendSSE(message)
  }
}

// Send quick message
const sendQuickMessage = async (message: string) => {
  // Auto-connect if not connected
  if (!connected.value) {
    await connect()
  }

  if (transport.value === 'websocket') {
    sendWS(message)
  } else {
    await sendSSE(message)
  }
}

// Handle A2UI events
const handleEvent = async (componentId: string, eventType: string, payload?: unknown) => {
  logMessage('sent', `Event: ${componentId}.${eventType}`)
  console.log('A2UI Event:', componentId, eventType, payload)

  // Handle form submission events
  if (eventType === 'data-query-submit' || eventType === 'form-submit') {
    // Send form data back to backend
    const formMessage = JSON.stringify({
      type: 'form-submit',
      action: eventType,
      data: payload
    })

    if (transport.value === 'websocket') {
      sendWS(formMessage)
    } else {
      await sendSSE(formMessage)
    }
  }
}

// Handle form submission from custom event
const handleFormSubmit = async (event: CustomEvent) => {
  const { action, data } = event.detail
  logMessage('sent', `Form Submit: ${action}`)

  // Convert form data to a message that backend can understand
  const message = `${action}: ${JSON.stringify(data)}`

  if (transport.value === 'websocket') {
    sendWS(message)
  } else {
    await sendSSE(message)
  }
}

// Lifecycle
onMounted(() => {
  // Initialize surface
  handleMessage(
    JSON.stringify({
      type: 'surface',
      surfaceId: surfaceId.value,
      name: '后端对接演示'
    }) + '\n'
  )

  // Listen for form submit events
  window.addEventListener('a2ui-form-submit', handleFormSubmit as unknown as EventListener)
})

onUnmounted(() => {
  disconnect()
  window.removeEventListener('a2ui-form-submit', handleFormSubmit as unknown as EventListener)
})
</script>

<style scoped>
.backend-demo {
  max-width: 100%;
  margin: 0 auto;
}

pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
