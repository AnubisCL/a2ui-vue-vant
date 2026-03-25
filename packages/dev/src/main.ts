import { createApp } from 'vue'
import { createA2UI } from '@a2ui/vue-plugin'
import App from './App.vue'
import router from './router'

import './main.css'
import 'uno.css'
import 'vant/lib/index.css'

const app = createApp(App)

app.use(createA2UI())
app.use(router)

app.mount('#app')
