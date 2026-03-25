<template>
  <div v-if="renderNodes.length > 0" class="a2ui-renderer">
    <component
      :is="getComponentType(node.type)"
      v-for="(node, index) in renderNodes"
      :key="node.id || index"
      v-bind="node.props"
      @click="handleEvent(node, 'click', $event)"
      @change="handleEvent(node, 'change', $event)"
      @submit="handleEvent(node, 'submit', $event)"
      @action="handleEvent(node, 'action', $event)"
      @close="handleEvent(node, 'close', $event)"
    >
      <template v-if="node.children && node.children.length > 0">
        <A2uiRendererNode
          v-for="(child, childIndex) in node.children"
          :key="child.id || childIndex"
          :node="child"
          @event="handleEvent(child, $event.type, $event.payload)"
        />
      </template>
    </component>
  </div>
  <div v-else class="a2ui-renderer--empty">
    <slot name="empty">
      <p>No surface to render</p>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, h, type Component } from 'vue'
import type { SurfaceId, ComponentTreeNode } from '@a2ui/core'
import { useA2UI } from '@a2ui/vue-components'

interface Props {
  surfaceId: SurfaceId
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'event', componentId: string, eventType: string, payload?: unknown): void
}>()

const componentRegistry = inject<any>('a2ui-registry')

// Use the reactive componentStore from useA2UI
const { componentStore } = useA2UI()

const renderNodes = computed((): ComponentTreeNode[] => {
  const components = componentStore.value[props.surfaceId]
  if (!components || Object.keys(components).length === 0) return []

  // For now, return all components as a flat list
  // In production, this would build a proper tree
  const nodes: ComponentTreeNode[] = []

  for (const component of Object.values(components)) {
    nodes.push({
      id: component.id,
      type: component.type,
      props: component.props ?? {},
      children: [],
    })
  }

  return nodes
})

const getComponentType = (type: string): Component | string => {
  const component = componentRegistry?.get(type)
  return component || type
}

const handleEvent = (node: ComponentTreeNode, eventType: string, payload: unknown) => {
  emit('event', node.id, eventType, payload)
}
</script>

<script lang="ts">
// Helper component for recursive rendering
export const A2uiRendererNode = {
  name: 'A2uiRendererNode',
  props: {
    node: {
      type: Object as () => ComponentTreeNode,
      required: true,
    },
  },
  emits: ['event'],
  setup(props: any, { emit }: any) {
    const componentRegistry = inject<any>('a2ui-registry')

    const getComponentType = (type: string): Component | string => {
      const component = componentRegistry?.get(type)
      return component || type
    }

    const handleEvent = (node: ComponentTreeNode, eventType: string, payload: unknown) => {
      emit('event', { type: eventType, payload })
    }

    return () => {
      const node = props.node as ComponentTreeNode

      if (!node) return null

      return h(
        getComponentType(node.type),
        {
          ...node.props,
          onClick: (e: any) => handleEvent(node, 'click', e),
          onChange: (e: any) => handleEvent(node, 'change', e),
          onSubmit: (e: any) => handleEvent(node, 'submit', e),
          onAction: (e: any) => handleEvent(node, 'action', e),
          onClose: (e: any) => handleEvent(node, 'close', e),
        },
        node.children?.map((child: ComponentTreeNode) =>
          h(A2uiRendererNode, {
            key: child.id,
            node: child,
            onEvent: (e: any) => handleEvent(child, e.type, e.payload),
          })
        )
      )
    }
  },
}
</script>

<style scoped>
.a2ui-renderer {
  width: 100%;
  height: 100%;
}

.a2ui-renderer--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #999;
  font-style: italic;
}
</style>
