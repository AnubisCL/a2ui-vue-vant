<template>
  <div class="components-demo min-h-screen bg-gray-50 p-4">
    <!-- Header -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <h1 class="text-xl font-bold text-gray-800 mb-2">🧩 A2UI 组件演示</h1>
      <p class="text-sm text-gray-500">展示所有 A2UI 组件的使用方式</p>
    </div>

    <!-- Category Tabs -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
      <van-tabs v-model:active="activeCategory" shrink sticky>
        <van-tab v-for="cat in categories" :key="cat.id" :title="cat.name" :name="cat.id">
          <div class="py-4">
            <div class="grid gap-3">
              <van-button
                v-for="demo in cat.demos"
                :key="demo.id"
                :type="currentDemo === demo.id ? 'primary' : 'default'"
                size="small"
                @click="runDemo(demo.id)"
              >
                {{ demo.name }}
              </van-button>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <!-- A2UI Renderer -->
    <div class="bg-white rounded-xl shadow-sm p-4 min-h-[400px]">
      <A2uiRenderer :surface-id="currentSurface" @event="handleComponentEvent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { A2uiRenderer, useA2UI } from '@a2ui/vue-plugin'

const currentSurface = ref('components-demo')
const activeCategory = ref('display')
const currentDemo = ref('')
const { handleMessage, clearSurface } = useA2UI()

const sendMessage = (message: object) => {
  handleMessage(JSON.stringify(message) + '\n')
}

// Demo definitions
const categories = reactive([
  {
    id: 'display',
    name: '展示',
    demos: [
      { id: 'text', name: '文本' },
      { id: 'image', name: '图片' },
      { id: 'icon', name: '图标' },
      { id: 'divider', name: '分割线' },
    ]
  },
  {
    id: 'input',
    name: '输入',
    demos: [
      { id: 'button', name: '按钮' },
      { id: 'textfield', name: '文本框' },
      { id: 'checkbox', name: '复选框' },
      { id: 'slider', name: '滑块' },
      { id: 'picker', name: '选择器' },
    ]
  },
  {
    id: 'container',
    name: '容器',
    demos: [
      { id: 'card', name: '卡片' },
      { id: 'tabs', name: '标签页' },
      { id: 'modal', name: '弹窗' },
    ]
  },
  {
    id: 'layout',
    name: '布局',
    demos: [
      { id: 'row', name: '水平布局' },
      { id: 'column', name: '垂直布局' },
      { id: 'list', name: '列表' },
    ]
  },
  {
    id: 'common',
    name: '通用',
    demos: [
      { id: 'badge', name: '徽标' },
      { id: 'progress', name: '进度条' },
      { id: 'tag', name: '标签' },
      { id: 'spinner', name: '加载' },
      { id: 'alert', name: '警告' },
    ]
  },
  {
    id: 'chart',
    name: '图表',
    demos: [
      { id: 'chart-line', name: '折线图' },
      { id: 'chart-bar', name: '柱状图' },
      { id: 'chart-pie', name: '饼图' },
    ]
  },
])

// Demo implementations
const demos: Record<string, () => void> = {
  // Display demos
  text: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '文本演示' })

    // Text with Markdown
    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-1',
      component: {
        type: 'Text',
        props: {
          content: '# 标题文本\n\n这是一段**加粗**和*斜体*的文本。\n\n- 列表项 1\n- 列表项 2',
          size: 'medium',
          markdown: true
        }
      }
    })

    // Different sizes
    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-2',
      component: {
        type: 'Row',
        props: { gap: 16, valign: 'center' }
      }
    })

    ;['small', 'medium', 'large', 'xlarge'].forEach((size, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `size-${i}`,
        parentId: 'text-2',
        component: {
          type: 'Text',
          props: { content: `${size} size`, size }
        }
      })
    })
  },

  image: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图片演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'img-1',
      component: {
        type: 'Image',
        props: {
          src: 'https://picsum.photos/400/200',
          width: '100%',
          height: 200,
          fit: 'cover'
        }
      }
    })
  },

  icon: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '图标演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'icon-row',
      component: { type: 'Row', props: { gap: 16, valign: 'center' } }
    })

    ;['home-o', 'user-o', 'star-o', 'like-o', 'setting-o'].forEach((name, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `icon-${i}`,
        parentId: 'icon-row',
        component: {
          type: 'Icon',
          props: { name, size: 'xlarge', color: '#1989fa' }
        }
      })
    })
  },

  divider: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '分割线演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-above',
      component: { type: 'Text', props: { content: '上方内容' } }
    })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'divider-1',
      component: { type: 'Divider', props: { margin: 16 } }
    })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'text-below',
      component: { type: 'Text', props: { content: '下方内容' } }
    })
  },

  // Input demos
  button: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '按钮演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'btn-row',
      component: { type: 'Row', props: { gap: 12, wrap: true } }
    })

    ;[
      { variant: 'primary', label: '主要按钮' },
      { variant: 'secondary', label: '次要按钮' },
      { variant: 'borderless', label: '无边框' },
      { variant: 'danger', label: '危险按钮' }
    ].forEach((btn, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `btn-${i}`,
        parentId: 'btn-row',
        component: { type: 'Button', props: btn }
      })
    })

    // Sizes
    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'size-row',
      component: { type: 'Row', props: { gap: 12, margin: [16, 0, 0, 0], valign: 'center' } }
    })

    ;[
      { size: 'small', label: '小', variant: 'primary' },
      { size: 'medium', label: '中', variant: 'primary' },
      { size: 'large', label: '大', variant: 'primary' }
    ].forEach((btn, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `size-btn-${i}`,
        parentId: 'size-row',
        component: { type: 'Button', props: btn }
      })
    })
  },

  textfield: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '文本框演示' })

    ;[
      { id: 'tf1', label: '普通文本', placeholder: '请输入文本' },
      { id: 'tf2', label: '数字输入', type: 'number', placeholder: '请输入数字' },
      { id: 'tf3', label: '密码输入', type: 'obscured', placeholder: '请输入密码' },
      { id: 'tf4', label: '多行文本', type: 'longText', placeholder: '请输入多行文本' }
    ].forEach(field => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: field.id,
        component: { type: 'TextField', props: field }
      })
    })
  },

  checkbox: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '复选框演示' })

    ;[
      { label: '未选中', checked: false },
      { label: '已选中', checked: true },
      { label: '禁用状态', checked: false, disabled: true }
    ].forEach((cb, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `cb-${i}`,
        component: { type: 'CheckBox', props: cb }
      })
    })
  },

  slider: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '滑块演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'slider-1',
      component: {
        type: 'Slider',
        props: { label: '基础滑块', min: 0, max: 100, showValue: true }
      }
    })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'slider-2',
      component: {
        type: 'Slider',
        props: { label: '步长为10', min: 0, max: 100, step: 10, showValue: true }
      }
    })
  },

  picker: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '选择器演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'picker-1',
      component: {
        type: 'ChoicePicker',
        props: {
          label: '单选（按钮样式）',
          mode: 'single',
          style: 'buttons',
          choices: [
            { value: '1', label: '选项A' },
            { value: '2', label: '选项B' },
            { value: '3', label: '选项C' }
          ]
        }
      }
    })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'picker-2',
      component: {
        type: 'ChoicePicker',
        props: {
          label: '多选（标签样式）',
          mode: 'multiple',
          style: 'chips',
          choices: [
            { value: '1', label: '标签1' },
            { value: '2', label: '标签2' },
            { value: '3', label: '标签3' }
          ]
        }
      }
    })
  },

  // Container demos
  card: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '卡片演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'card-1',
      component: {
        type: 'Card',
        props: {
          title: '基础卡片',
          content: '这是一个简单的卡片组件，用于展示内容。',
          elevated: true
        }
      }
    })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'card-2',
      component: {
        type: 'Card',
        props: {
          title: '带操作卡片',
          subtitle: '副标题',
          content: '这个卡片带有操作按钮。',
          elevated: true,
          actions: [
            { label: '确认', action: 'confirm', primary: true },
            { label: '取消', action: 'cancel' }
          ]
        }
      }
    })
  },

  tabs: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '标签页演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'tabs-1',
      component: {
        type: 'Tabs',
        props: {
          tabs: [
            { id: 'tab1', label: '首页' },
            { id: 'tab2', label: '分类' },
            { id: 'tab3', label: '我的' }
          ]
        }
      }
    })
  },

  modal: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '弹窗演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'modal-btn',
      component: {
        type: 'Button',
        props: { label: '打开弹窗', variant: 'primary' }
      }
    })
  },

  // Layout demos
  row: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '水平布局演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'row-1',
      component: {
        type: 'Row',
        props: { gap: 12, align: 'center', valign: 'center' }
      }
    })

    ;[1, 2, 3].forEach(i => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `row-item-${i}`,
        parentId: 'row-1',
        component: {
          type: 'Card',
          props: { title: `卡片 ${i}`, content: '水平排列', elevated: true }
        }
      })
    })
  },

  column: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '垂直布局演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'col-1',
      component: {
        type: 'Column',
        props: { gap: 12, align: 'stretch' }
      }
    })

    ;[1, 2, 3].forEach(i => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `col-item-${i}`,
        parentId: 'col-1',
        component: {
          type: 'Card',
          props: { title: `卡片 ${i}`, content: '垂直排列', elevated: true }
        }
      })
    })
  },

  list: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '列表演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'list-1',
      component: {
        type: 'Column',
        props: { gap: 8 }
      }
    })

    ;['项目 A', '项目 B', '项目 C', '项目 D', '项目 E'].forEach((item, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `list-item-${i}`,
        parentId: 'list-1',
        component: {
          type: 'Card',
          props: { title: item, content: `这是列表项 ${i + 1} 的内容`, elevated: true }
        }
      })
    })
  },

  // Common demos
  badge: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '徽标演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'badge-row',
      component: { type: 'Row', props: { gap: 24, valign: 'center' } }
    })

    ;[
      { content: '5', color: '#f44336' },
      { content: '99+', color: '#ff9800' },
      { content: 'NEW', color: '#4caf50' }
    ].forEach((badge, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `badge-${i}`,
        parentId: 'badge-row',
        component: { type: 'Badge', props: badge }
      })
    })
  },

  progress: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '进度条演示' })

    ;[
      { percentage: 25, color: '#1989fa' },
      { percentage: 50, color: '#07c160' },
      { percentage: 75, color: '#ff976a' },
      { percentage: 100, color: '#4caf50' }
    ].forEach((prog, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `prog-${i}`,
        component: { type: 'Progress', props: prog }
      })
    })
  },

  tag: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '标签演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'tag-row',
      component: { type: 'Row', props: { gap: 8, wrap: true } }
    })

    ;[
      { label: '标签', type: 'default' },
      { label: '主要', type: 'primary' },
      { label: '成功', type: 'success' },
      { label: '警告', type: 'warning' },
      { label: '危险', type: 'danger' }
    ].forEach((tag, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `tag-${i}`,
        parentId: 'tag-row',
        component: { type: 'Tag', props: tag }
      })
    })
  },

  spinner: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '加载演示' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'spinner-row',
      component: { type: 'Row', props: { gap: 24, valign: 'center' } }
    })

    ;['spinner', 'circular', 'dots'].forEach((type, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `spinner-${i}`,
        parentId: 'spinner-row',
        component: { type: 'Spinner', props: { type, size: 'large' } }
      })
    })
  },

  alert: () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '警告演示' })

    ;[
      { type: 'info', message: '这是一条提示信息' },
      { type: 'success', message: '操作成功完成' },
      { type: 'warning', message: '请注意检查输入' },
      { type: 'error', message: '发生错误，请重试' }
    ].forEach((alert, i) => {
      sendMessage({
        type: 'component',
        surfaceId: currentSurface.value,
        componentId: `alert-${i}`,
        component: { type: 'Alert', props: alert }
      })
    })
  },

  // Chart demos
  'chart-line': () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '折线图' })

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
  },

  'chart-bar': () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '柱状图' })

    sendMessage({
      type: 'component',
      surfaceId: currentSurface.value,
      componentId: 'bar-chart',
      component: {
        type: 'Chart',
        props: {
          height: '350px',
          option: {
            title: { text: '产品销量对比', left: 'center' },
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
  },

  'chart-pie': () => {
    clearSurface(currentSurface.value)
    sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '饼图' })

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
  },
}

// Run demo
const runDemo = (demoId: string) => {
  currentDemo.value = demoId
  const demo = demos[demoId]
  if (demo) {
    demo()
    showToast(`已加载: ${demoId}`)
  }
}

// Event handler
const handleComponentEvent = (componentId: string, eventType: string, payload?: unknown) => {
  console.log('Event:', componentId, eventType, payload)
}

// Initialize
onMounted(() => {
  sendMessage({ type: 'surface', surfaceId: currentSurface.value, name: '组件演示' })
  // Run first demo
  runDemo('text')
})
</script>

<style scoped>
.components-demo {
  max-width: 100%;
  margin: 0 auto;
}
</style>
