<template>
  <div class="chart-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">📊 ECharts 图表演示</h1>
      <p class="text-sm text-gray-500">通过 A2UI 协议动态渲染图表</p>
    </div>

    <!-- Controls -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <div class="flex flex-wrap gap-2 mb-4">
        <van-button type="primary" size="small" @click="startLineChartDemo">
          📈 折线图
        </van-button>
        <van-button type="success" size="small" @click="startBarChartDemo">
          📊 柱状图
        </van-button>
        <van-button type="warning" size="small" @click="startPieChartDemo">
          🥧 饼图
        </van-button>
        <van-button type="default" size="small" @click="startStreamingDemo">
          🌊 流式更新
        </van-button>
      </div>
    </div>

    <!-- A2UI Renderer -->
    <div class="bg-white rounded-xl shadow-sm p-4 min-h-[400px]">
      <A2uiRenderer :surface-id="currentSurface" @event="handleComponentEvent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const currentSurface = ref('chart-demo')
const { handleMessage, clearSurface } = useA2UI()

const sendMessage = (message: object) => {
  handleMessage(JSON.stringify(message) + '\n')
}

// 折线图演示
const startLineChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '折线图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 📈 销售趋势折线图', size: 'large' }
    }
  })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'line-chart',
    component: {
      type: 'Chart',
      props: {
        height: '350px',
        option: {
          title: { text: '月度销售趋势', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: {
            type: 'category',
            data: ['1月', '2月', '3月', '4月', '5月', '6月']
          },
          yAxis: { type: 'value', name: '销售额(万)' },
          series: [{
            name: '销售额',
            type: 'line',
            smooth: true,
            data: [120, 132, 101, 134, 190, 230],
            itemStyle: { color: '#1989fa' },
            areaStyle: { color: 'rgba(25, 137, 250, 0.2)' }
          }]
        }
      }
    }
  })

  showToast('折线图演示已加载')
}

// 柱状图演示
const startBarChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '柱状图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 📊 产品销量柱状图', size: 'large' }
    }
  })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'bar-chart',
    component: {
      type: 'Chart',
      props: {
        height: '350px',
        option: {
          title: { text: '各产品销量对比', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: {
            type: 'category',
            data: ['产品A', '产品B', '产品C', '产品D', '产品E']
          },
          yAxis: { type: 'value', name: '销量' },
          series: [{
            name: '销量',
            type: 'bar',
            data: [200, 150, 80, 70, 110],
            itemStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: '#1989fa' },
                  { offset: 1, color: '#87ceeb' }
                ]
              }
            }
          }]
        }
      }
    }
  })

  showToast('柱状图演示已加载')
}

// 饼图演示
const startPieChartDemo = () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '饼图演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 🥧 市场份额饼图', size: 'large' }
    }
  })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'pie-chart',
    component: {
      type: 'Chart',
      props: {
        height: '400px',
        option: {
          title: { text: '市场份额分布', left: 'center' },
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', top: 'center' },
          series: [{
            name: '市场份额',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
            labelLine: { show: false },
            data: [
              { value: 1048, name: '搜索引擎', itemStyle: { color: '#1989fa' } },
              { value: 735, name: '直接访问', itemStyle: { color: '#07c160' } },
              { value: 580, name: '邮件营销', itemStyle: { color: '#ff976a' } },
              { value: 484, name: '联盟广告', itemStyle: { color: '#ffc107' } },
              { value: 300, name: '视频广告', itemStyle: { color: '#9c27b0' } }
            ]
          }]
        }
      }
    }
  })

  showToast('饼图演示已加载')
}

// 流式更新演示
const startStreamingDemo = async () => {
  clearSurface(currentSurface.value)
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '流式更新演示' })

  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'title',
    component: {
      type: 'Text',
      props: { content: '## 🌊 实时数据流式更新', size: 'large' }
    }
  })

  // 初始图表
  sendMessage({
    type: 'component',
    surfaceId: currentSurface.value,
    componentId: 'stream-chart',
    component: {
      type: 'Chart',
      props: {
        height: '350px',
        option: {
          title: { text: '实时数据监控', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: [] },
          yAxis: { type: 'value' },
          series: [{ type: 'line', smooth: true, data: [] }]
        }
      }
    }
  })

  // 模拟流式数据更新
  const categories: string[] = []
  const data: number[] = []

  for (let i = 1; i <= 10; i++) {
    await new Promise(r => setTimeout(r, 500))
    categories.push(`${i}秒`)
    data.push(Math.floor(Math.random() * 100) + 50)

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'stream-chart',
      component: {
        type: 'Chart',
        props: {
          height: '350px',
          option: {
            xAxis: { type: 'category', data: [...categories] },
            series: [{ type: 'line', smooth: true, data: [...data], itemStyle: { color: '#1989fa' } }]
          }
        }
      }
    })
  }

  showToast('流式更新演示完成')
}

// 事件处理
const handleComponentEvent = (componentId: string, eventType: string, payload?: unknown) => {
  console.log('Event:', componentId, eventType, payload)
}

// 初始化
onMounted(() => {
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图表演示' })
  startLineChartDemo()
})
</script>

<style scoped>
.chart-demo {
  max-width: 100%;
  margin: 0 auto;
}
</style>
