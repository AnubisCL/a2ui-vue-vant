/**
 * Simple router for the dev app
 */

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import BasicExample from '../examples/BasicExample.vue'
import FormExample from '../examples/FormExample.vue'
import MessagePlayground from '../components/MessagePlayground.vue'
import CommonComponents from '../examples/CommonComponents.vue'
import StreamingDemo from '../examples/StreamingDemo.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: BasicExample,
  },
  {
    path: '/basic',
    name: 'basic',
    component: BasicExample,
  },
  {
    path: '/form',
    name: 'form',
    component: FormExample,
  },
  {
    path: '/playground',
    name: 'playground',
    component: MessagePlayground,
  },
  {
    path: '/common',
    name: 'common',
    component: CommonComponents,
  },
  {
    path: '/streaming',
    name: 'streaming',
    component: StreamingDemo,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
