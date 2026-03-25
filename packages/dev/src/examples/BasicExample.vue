<template>
  <div class="basic-example">
    <h2>Basic Components</h2>

    <A2uiColumn :gap="16">
      <!-- Text examples -->
      <A2uiCard title="Text Component">
        <A2uiColumn :gap="8">
          <A2uiText :content="plainText" />
          <A2uiText :content="markdownText" :markdown="true" />
          <A2uiText
            :content="'This is large bold text'"
            :size="'large'"
            :weight="'bold'"
            :color="'#0066cc'"
          />
        </A2uiColumn>
      </A2uiCard>

      <!-- Button examples -->
      <A2uiCard title="Buttons">
        <A2uiRow :gap="8" :align="'center'">
          <A2uiButton :label="'Primary'" :variant="'primary'" @click="handleClick" />
          <A2uiButton :label="'Secondary'" :variant="'secondary'" @click="handleClick" />
          <A2uiButton :label="'Borderless'" :variant="'borderless'" @click="handleClick" />
          <A2uiButton :label="'Danger'" :variant="'danger'" @click="handleClick" />
          <A2uiButton :label="'Disabled'" :disabled="true" />
        </A2uiRow>
      </A2uiCard>

      <!-- Input examples -->
      <A2uiCard title="Inputs">
        <A2uiColumn :gap="12">
          <A2uiTextField
            v-model="textValue"
            :label="'Text Input'"
            :placeholder="'Enter text...'"
            :hint="'This is a hint text'"
          />

          <A2uiRow :gap="8">
            <A2uiCheckBox v-model="checkedValue" :label="'Check me'" />
            <A2uiCheckBox v-model="checkedValue2" :label="'Check me too'" :disabled="true" />
          </A2uiRow>

          <A2uiChoicePicker
            v-model="selectedChoice"
            :label="'Pick one'"
            :choices="choices"
            :style="'chips'"
          />

          <A2uiSlider
            v-model="sliderValue"
            :label="'Slider'"
            :min="0"
            :max="100"
            :step="5"
          />
        </A2uiColumn>
      </A2uiCard>

      <!-- Display examples -->
      <A2uiCard title="Display">
        <A2uiColumn :gap="12">
          <A2uiRow :gap="8">
            <A2uiIcon :name="'star'" :size="'large'" />
            <A2uiIcon :name="'heart'" :size="'large'" />
            <A2uiIcon :name="'check'" :size="'large'" />
          </A2uiRow>

          <A2uiDivider />

          <A2uiText :content="'Below is an example image'" />
          <A2uiImage
            :src="'https://picsum.photos/400/200'"
            :alt="'Example image'"
            :width="400"
            :fit="'cover'"
          />
        </A2uiColumn>
      </A2uiCard>
    </A2uiColumn>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const plainText = ref('This is plain text')
const markdownText = ref(`
# Markdown Header

This is **bold** and *italic* text.

- List item 1
- List item 2
- List item 3
`)

const textValue = ref('Hello A2UI!')
const checkedValue = ref(false)
const checkedValue2 = ref(true)
const selectedChoice = ref('option1')
const sliderValue = ref(50)

const choices = ref([
  { value: 'option1', label: 'Option 1' },
  { value: 'option2', label: 'Option 2' },
  { value: 'option3', label: 'Option 3' },
])

const handleClick = () => {
  alert('Button clicked!')
}
</script>

<style scoped>
.basic-example {
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 16px;
}
</style>
