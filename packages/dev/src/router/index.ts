/**
 * Router for the dev app
 */

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import ChatBox from '../examples/ChatBox.vue'
import ChartExample from '../examples/ChartExample.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'chat',
    component: ChatBox,
    meta: { title: 'AI 聊天' },
  },
  {
    path: '/chart',
    name: 'chart',
    component: ChartExample,
    meta: { title: '图表演示' },
  },
  {
    path: '/components',
    name: 'components',
    component: () => import('../examples/ComponentsDemo.vue'),
    meta: { title: '组件演示' },
  },
  {
    path: '/backend',
    name: 'backend',
    component: () => import('../examples/BackendDemo.vue'),
    meta: { title: '后端对接' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
