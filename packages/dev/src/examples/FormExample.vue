<template>
  <div class="form-example">
    <h2>Form Example</h2>

    <A2uiCard title="User Information Form">
      <A2uiColumn :gap="16">
        <A2uiTextField
          v-model="formData.name"
          :label="'Full Name'"
          :placeholder="'Enter your name'"
          :hint="'Your legal name as it appears on documents'"
        />

        <A2uiTextField
          v-model="formData.email"
          type="email"
          :label="'Email Address'"
          :placeholder="'you@example.com'"
          :hint="'We\'ll send notifications to this address'"
        />

        <A2uiDateTimeInput
          v-model="formData.birthDate"
          type="date"
          :label="'Date of Birth'"
          :max="today"
        />

        <A2uiChoicePicker
          v-model="formData.country"
          :label="'Country'"
          :choices="countries"
          :style="'dropdown'"
        />

        <A2uiChoicePicker
          v-model="formData.interests"
          :label="'Interests'"
          :choices="interests"
          :mode="'multiple'"
          :style="'chips'"
        />

        <A2uiTextField
          v-model="formData.bio"
          type="longText"
          :label="'Bio'"
          :placeholder="'Tell us about yourself...'"
          :maxLength="500"
        />

        <A2uiRow :gap="8" :align="'end'">
          <A2uiCheckBox v-model="formData.terms" :label="'I agree to the terms and conditions'" />
        </A2uiRow>

        <A2uiDivider />

        <A2uiRow :gap="8">
          <A2uiButton :label="'Submit'" :variant="'primary'" @click="handleSubmit" />
          <A2uiButton :label="'Reset'" :variant="'borderless'" @click="handleReset" />
        </A2uiRow>
      </A2uiColumn>
    </A2uiCard>

    <A2uiCard v-if="submitted" title="Form Data">
      <A2uiColumn :gap="8">
        <A2uiText v-for="(value, key) in formData" :key="key">
          <strong>{{ key }}:</strong> {{ JSON.stringify(value) }}
        </A2uiText>
      </A2uiColumn>
    </A2uiCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const formData = ref({
  name: '',
  email: '',
  birthDate: '',
  country: 'us',
  interests: [] as string[],
  bio: '',
  terms: false,
})

const submitted = ref(false)

const today = computed(() => new Date().toISOString().split('T')[0])

const countries = ref([
  { value: 'us', label: 'United States' },
  { value: 'uk', label: 'United Kingdom' },
  { value: 'ca', label: 'Canada' },
  { value: 'au', label: 'Australia' },
  { value: 'de', label: 'Germany' },
  { value: 'fr', label: 'France' },
])

const interests = ref([
  { value: 'tech', label: 'Technology' },
  { value: 'sports', label: 'Sports' },
  { value: 'music', label: 'Music' },
  { value: 'travel', label: 'Travel' },
  { value: 'food', label: 'Food' },
  { value: 'art', label: 'Art' },
])

const handleSubmit = () => {
  if (!formData.value.terms) {
    alert('Please accept the terms and conditions')
    return
  }
  submitted.value = true
  alert('Form submitted! Check below for the submitted data.')
}

const handleReset = () => {
  formData.value = {
    name: '',
    email: '',
    birthDate: '',
    country: 'us',
    interests: [],
    bio: '',
    terms: false,
  }
  submitted.value = false
}
</script>

<style scoped>
.form-example {
  max-width: 600px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 16px;
}
</style>
