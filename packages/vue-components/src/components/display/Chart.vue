<template>
  <div
    ref="chartRef"
    class="a2ui-chart"
    :style="chartStyle"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts'
import type { ChartProps } from '@a2ui/core'
import { resolveObjectValue } from '../../utils'

const props = withDefaults(defineProps<ChartProps>(), {
  width: '100%',
  height: '300px',
  autoResize: true,
  theme: 'light',
  renderer: 'canvas',
})

const emit = defineEmits<{
  (e: 'ready', chart: echarts.ECharts): void
  (e: 'click', event: unknown): void
}>()

// DOM reference
const chartRef = ref<HTMLDivElement | null>(null)

// ECharts instance (shallow ref to avoid deep reactivity)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

// Resize observer
let resizeObserver: ResizeObserver | null = null

// Chart style
const chartStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
}))

// Initialize chart
const initChart = () => {
  if (!chartRef.value) return

  // Dispose existing instance
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  // Create new instance
  const renderer = props.renderer === 'svg' ? 'svg' : 'canvas'
  chartInstance.value = echarts.init(chartRef.value, props.theme, {
    renderer,
  })

  // Set initial option
  const option = resolveObjectValue(props.option, {})
  if (Object.keys(option).length > 0) {
    chartInstance.value.setOption(option, { notMerge: true })
  }

  // Emit ready event
  emit('ready', chartInstance.value)

  // Setup click event
  chartInstance.value.on('click', (event) => {
    emit('click', event)
  })
}

// Update chart option
const updateChart = () => {
  if (!chartInstance.value) return

  const option = resolveObjectValue(props.option, {})
  if (Object.keys(option).length > 0) {
    chartInstance.value.setOption(option, { notMerge: false })
  }
}

// Handle resize
const handleResize = () => {
  chartInstance.value?.resize()
}

// Watch option changes
watch(
  () => props.option,
  () => {
    updateChart()
  },
  { deep: true }
)

// Watch theme/renderer changes
watch(
  () => [props.theme, props.renderer],
  () => {
    initChart()
  }
)

// Lifecycle
onMounted(() => {
  initChart()

  // Setup resize observer
  if (props.autoResize && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.observe(chartRef.value)
  }
})

onUnmounted(() => {
  // Cleanup resize observer
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  // Dispose chart instance
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})

// Expose methods
defineExpose({
  getChartInstance: () => chartInstance.value,
  resize: handleResize,
})
</script>

<style scoped>
.a2ui-chart {
  min-height: 200px;
}
</style>
