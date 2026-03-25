/**
 * Router for the dev app
 */

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import StreamingDemo from '../examples/StreamingDemo.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'streaming',
    component: StreamingDemo,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
