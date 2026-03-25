<template>
  <div class="common-components-demo p-8 max-w-7xl mx-auto">
    <!-- Header -->
    <div class="mb-10 text-center">
      <h1 class="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-3">
        常用组件示例
      </h1>
      <p class="text-gray-500 text-lg">现代、精美的 UI 组件库</p>
    </div>

    <!-- Badge -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-blue-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Badge 徽章</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex gap-3 items-center flex-wrap">
          <Badge content="新消息" />
          <Badge content="99+" variant="primary" />
          <Badge content="已完成" variant="success" />
          <Badge content="警告" variant="warning" />
          <Badge content="错误" variant="danger" />
          <Badge content="提示" variant="info" />
          <Badge dot variant="primary" />
          <Badge :count="150" :maxCount="99" variant="danger" />
        </div>
      </div>
    </section>

    <!-- Avatar -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-purple-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Avatar 头像</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex gap-4 items-center">
          <Avatar name="张三" />
          <Avatar name="李四" />
          <Avatar name="Jane Doe" />
          <Avatar src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="Felix" />
          <Avatar :size="56" name="大头像" />
          <Avatar size="small" name="小" />
          <Avatar name="Admin" shape="square" />
        </div>
      </div>
    </section>

    <!-- Progress -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-green-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Progress 进度条</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-5">
        <Progress :value="30" label="下载文件" showPercentage />
        <Progress :value="60" variant="success" label="上传完成" showPercentage showInfo />
        <Progress :value="85" variant="warning" label="处理中" />
        <Progress :value="45" variant="danger" label="错误进度" />
        <Progress indeterminate label="正在加载..." />
        <Progress :value="75" size="large" variant="primary" showPercentage showInfo />
      </div>
    </section>

    <!-- Spinner -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-orange-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Spinner 加载器</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex gap-6 items-center">
          <Spinner size="small" />
          <Spinner />
          <Spinner size="large" />
          <Spinner :size="40" color="#10b981" />
          <Spinner :size="48" color="#ef4444" />
          <Spinner :size="56" color="#8b5cf6" />
        </div>
      </div>
    </section>

    <!-- Tag -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-cyan-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Tag 标签</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex gap-2 flex-wrap">
          <Tag text="默认" />
          <Tag text="Vue" variant="primary" />
          <Tag text="React" variant="primary" />
          <Tag text="TypeScript" variant="success" />
          <Tag text="警告" variant="warning" />
          <Tag text="重要" variant="danger" />
          <Tag text="信息" variant="info" icon="ℹ️" />
          <Tag text="可关闭" variant="primary" closable @close="console.log('Tag closed')" />
        </div>
      </div>
    </section>

    <!-- Alert -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-red-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Alert 警告框</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-4">
        <Alert
          title="信息提示"
          message="这是一条普通的信息提示，用于展示重要内容。"
          variant="info"
        />
        <Alert
          title="操作成功"
          message="您的操作已成功完成，数据已保存。"
          variant="success"
        />
        <Alert
          title="警告"
          message="请注意，继续操作可能会导致数据丢失。"
          variant="warning"
        />
        <Alert
          title="错误"
          message="操作失败，请检查网络连接后重试。"
          variant="error"
          :closable="true"
        />
      </div>
    </section>

    <!-- Switch -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-indigo-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Switch 开关</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">启用通知</span>
            <Switch v-model="switchValue1" />
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">自动更新</span>
            <Switch v-model="switchValue2" color="success" />
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">维护模式</span>
            <Switch v-model="switchValue3" color="warning" />
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">危险操作</span>
            <Switch v-model="switchValue4" color="danger" />
          </div>
          <div class="flex items-center gap-4 pt-4 border-t border-gray-100">
            <Switch v-model="switchValue5" size="small" />
            <Switch v-model="switchValue6" size="medium" />
            <Switch v-model="switchValue7" size="large" />
            <Switch :modelValue="true" :disabled="true" />
          </div>
        </div>
      </div>
    </section>

    <!-- Tooltip -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-pink-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Tooltip 提示框</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex gap-4">
          <Tooltip content="这是上方提示">
            <button class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors">
              上方提示
            </button>
          </Tooltip>
          <Tooltip content="这是下方提示" placement="bottom">
            <button class="px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition-colors">
              下方提示
            </button>
          </Tooltip>
          <Tooltip content="这是左侧提示" placement="left">
            <button class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors">
              左侧提示
            </button>
          </Tooltip>
          <Tooltip content="这是右侧提示" placement="right">
            <button class="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors">
              右侧提示
            </button>
          </Tooltip>
        </div>
      </div>
    </section>

    <!-- Skeleton -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-gray-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Skeleton 骨架屏</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-6">
        <div class="border-b border-gray-100 pb-6">
          <p class="text-sm text-gray-500 mb-4">基础骨架屏</p>
          <Skeleton />
        </div>
        <div class="border-b border-gray-100 pb-6">
          <p class="text-sm text-gray-500 mb-4">带头像</p>
          <Skeleton :avatar="true" />
        </div>
        <div class="border-b border-gray-100 pb-6">
          <p class="text-sm text-gray-500 mb-4">完整示例</p>
          <Skeleton
            :avatar="true"
            :title="true"
            :paragraphRows="4"
            :paragraphWidth="[100, 90, 80, 70]"
          />
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-4">无头像</p>
          <Skeleton :avatar="false" titleWidth="60%" :paragraphRows="3" />
        </div>
      </div>
    </section>

    <!-- Empty -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-teal-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">Empty 空状态</h2>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <Empty />
        </div>
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <Empty
            imageType="search"
            title="没有搜索结果"
            description="尝试使用其他关键词或清除筛选条件"
          >
            <button class="mt-4 px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors">
              重新搜索
            </button>
          </Empty>
        </div>
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <Empty
            imageType="network"
            title="网络连接失败"
            description="请检查您的网络连接是否正常"
          >
            <button class="mt-4 px-6 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition-colors">
              刷新页面
            </button>
          </Empty>
        </div>
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <Empty
            imageType="error"
            title="加载失败"
            description="数据加载出错，请稍后重试"
          >
            <div class="mt-4 flex gap-2 justify-center">
              <button class="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors">
                重试
              </button>
              <button class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors">
                取消
              </button>
            </div>
          </Empty>
        </div>
      </div>
    </section>

    <!-- Combined Example -->
    <section class="mb-12">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-1 h-6 bg-gradient-to-b from-blue-500 to-purple-500 rounded-full"></div>
        <h2 class="text-2xl font-bold text-gray-800">综合示例</h2>
      </div>
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex items-start gap-4">
          <Avatar name="用户" />
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span class="font-semibold text-gray-800">张小明</span>
              <Badge text="VIP" variant="primary" size="small" />
              <Tag text="在线" variant="success" size="small" />
            </div>
            <p class="text-sm text-gray-600 mb-3">这是一条用户消息内容，展示了多个组件的组合效果。</p>
            <div class="flex items-center gap-4">
              <div class="flex-1">
                <Progress :value="75" size="small" />
              </div>
              <span class="text-xs text-gray-500">75%</span>
              <Switch v-model="notificationEnabled" size="small" />
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Badge,
  Avatar,
  Progress,
  Spinner,
  Tag,
  Alert,
  Switch,
  Tooltip,
  Skeleton,
  Empty,
} from '@a2ui/vue-components'

const switchValue1 = ref(true)
const switchValue2 = ref(false)
const switchValue3 = ref(false)
const switchValue4 = ref(true)
const switchValue5 = ref(false)
const switchValue6 = ref(true)
const switchValue7 = ref(false)
const notificationEnabled = ref(true)
</script>

<style scoped>
.common-components-demo {
  background: linear-gradient(to bottom, #f9fafb, #ffffff);
  min-height: 100vh;
}

section {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
