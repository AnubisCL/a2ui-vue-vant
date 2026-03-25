import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import dts from 'vite-plugin-dts'

export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
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
      name: 'A2UIVuePlugin',
      formats: ['es'],
      fileName: 'index',
    },
    rollupOptions: {
      external: ['vue', '@a2ui/core', '@a2ui/vue-components', 'unocss'],
      output: {
        globals: {
          vue: 'Vue',
        },
      },
    },
    target: 'es2020',
    minify: false,
    sourcemap: true,
  },
})
