import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import dts from 'vite-plugin-dts'
import UnoCSS from 'unocss/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'

export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
    Components({
      resolvers: [VantResolver()],
    }),
    dts({
      include: ['src/**/*'],
      outDir: 'dist',
      insertTypesEntry: true,
      rollupTypes: true,
    }),
  ],
  build: {
    lib: {
      entry: './src/index.ts',
      name: 'A2UIVueComponents',
      formats: ['es'],
      fileName: 'index',
    },
    rollupOptions: {
      external: ['vue', '@a2ui/core', '@vueuse/core', 'markdown-it', 'unocss', 'vant'],
      output: {
        globals: {
          vue: 'Vue',
          vant: 'Vant',
        },
      },
    },
    target: 'es2020',
    minify: false,
    sourcemap: true,
  },
})
