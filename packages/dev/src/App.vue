<template>
  <div id="app" class="app">
    <!-- Main Content -->
    <div class="main-content">
      <router-view />
    </div>

    <!-- Bottom Navigation -->
    <van-tabbar v-model="activeTab" fixed @change="handleTabChange">
      <van-tabbar-item name="streaming" icon="chat-o">
        流式演示
      </van-tabbar-item>
      <van-tabbar-item name="chart" icon="bar-chart-o">
        图表演示
      </van-tabbar-item>
      <van-tabbar-item name="components" icon="apps-o">
        组件演示
      </van-tabbar-item>
      <van-tabbar-item name="backend" icon="cluster-o">
        后端对接
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// Active tab based on current route
const activeTab = computed(() => {
  if (route.path === '/chart') return 'chart'
  if (route.path === '/components') return 'components'
  if (route.path === '/backend') return 'backend'
  return 'streaming'
})

// Handle tab change
const handleTabChange = (name: string) => {
  router.push({ name })
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}
</style>
