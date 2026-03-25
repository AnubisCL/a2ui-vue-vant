/**
 * Router for the dev app
 */

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import StreamingDemo from '../examples/StreamingDemo.vue'
import ChartExample from '../examples/ChartExample.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'streaming',
    component: StreamingDemo,
  },
  {
    path: '/chart',
    name: 'chart',
    component: ChartExample,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
