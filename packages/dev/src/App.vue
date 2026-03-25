<template>
  <div id="app" class="app">
    <header class="app-header">
      <h1>A2UI Vue SDK</h1>
      <p>Agent to UI Declarative UI Protocol for Vue 3</p>
    </header>

    <main class="app-main">
      <nav class="app-nav">
        <h2>Examples</h2>
        <ul>
          <li><router-link to="/">Home</router-link></li>
          <li><router-link to="/basic">Basic Components</router-link></li>
          <li><router-link to="/form">Form Example</router-link></li>
          <li><router-link to="/common">Common Components</router-link></li>
          <li><router-link to="/streaming">🌊 Streaming Demo</router-link></li>
          <li><router-link to="/playground">Message Playground</router-link></li>
        </ul>
      </nav>

      <div class="app-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// App component
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  text-align: center;
}

.app-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2.5rem;
}

.app-header p {
  margin: 0;
  opacity: 0.9;
}

.app-main {
  display: flex;
  flex: 1;
}

.app-nav {
  width: 250px;
  background: #f5f5f5;
  padding: 1.5rem;
  border-right: 1px solid #e0e0e0;
}

.app-nav h2 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
}

.app-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.app-nav li {
  margin-bottom: 0.5rem;
}

.app-nav a {
  color: #333;
  text-decoration: none;
  display: block;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.app-nav a:hover,
.app-nav a.router-link-active {
  background: #e0e0e0;
}

.app-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}
</style>
