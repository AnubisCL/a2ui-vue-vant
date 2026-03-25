import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetUno,
  transformerDirectives,
  transformerVariantGroup,
  presetWebFonts,
} from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
  ],
  transformers: [
    transformerDirectives(),
    transformerVariantGroup(),
  ],
  theme: {
    // A2UI Design Tokens
    colors: {
      // Brand colors
      primary: {
        DEFAULT: '#0066cc',
        dark: '#0052a3',
        light: '#3388dd',
      },
      secondary: '#666666',
      success: '#10b981',
      warning: '#f59e0b',
      danger: '#dc3545',
      info: '#3b82f6',

      // Neutral colors
      neutral: {
        50: '#f9f9f9',
        100: '#f5f5f5',
        200: '#e0e0e0',
        300: '#cccccc',
        400: '#999999',
        500: '#666666',
        600: '#444444',
        700: '#333333',
        800: '#222222',
        900: '#111111',
      },
    },

    // Spacing scale
    spacing: {
      '0': '0',
      'xs': '4px',
      'sm': '8px',
      'md': '12px',
      'lg': '16px',
      'xl': '20px',
      '2xl': '24px',
      '3xl': '32px',
      '4xl': '40px',
    },

    // Border radius
    borderRadius: {
      'none': '0',
      'sm': '4px',
      'DEFAULT': '6px',
      'md': '8px',
      'lg': '12px',
      'xl': '16px',
      'full': '9999px',
    },

    // Font sizes
    fontSize: {
      'xs': ['0.75rem', { lineHeight: '1rem' }],
      'sm': ['0.875rem', { lineHeight: '1.25rem' }],
      'base': ['1rem', { lineHeight: '1.5rem' }],
      'lg': ['1.125rem', { lineHeight: '1.75rem' }],
      'xl': ['1.25rem', { lineHeight: '1.75rem' }],
      '2xl': ['1.5rem', { lineHeight: '2rem' }],
      '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
      '4xl': ['2.25rem', { lineHeight: '2.5rem' }],
    },

    // Box shadow
    boxShadow: {
      'sm': '0 1px 2px 0 rgb(0 0 0 / 0.05)',
      'DEFAULT': '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
      'md': '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
      'lg': '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
      'xl': '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
      'elevated': '0 2px 8px rgba(0, 0, 0, 0.1)',
    },

    // Z-index scale
    zIndex: {
      'dropdown': 1000,
      'sticky': 1020,
      'fixed': 1030,
      'modal-backdrop': 1040,
      'modal': 1050,
      'popover': 1060,
      'tooltip': 1070,
    },
  },

  shortcuts: {
    // Component-specific shortcuts
    'a2ui-btn-base': 'inline-flex items-center justify-center gap-2 border-none rounded cursor-pointer transition-all',
    'a2ui-input-base': 'w-full px-3 py-2 border border-neutral-300 rounded text-base transition-colors box-border focus:outline-none focus:border-primary',
    'a2ui-card-base': 'bg-white border border-neutral-200 rounded box-border',
    'a2ui-modal-overlay': 'fixed top-0 left-0 right-0 bottom-0 bg-black/50 flex items-center justify-center z-1050 p-5',
  },

  rules: [
    // A2UI specific utility classes
    [/^a2ui-gap-(.+)$/, ([, c]) => ({ gap: `${c}px` })],
    [/^a2ui-m-(.+)$/, ([, c]) => ({ margin: `${c}px` })],
    [/^a2ui-p-(.+)$/, ([, c]) => ({ padding: `${c}px` })],
    [/^a2ui-text-(.+)$/, ([, c]) => ({ fontSize: `${c}px` })],
  ],

  safelist: [
    // Dynamic values that should be preserved
    'a2ui-checkbox--checked',
    'a2ui-choicepicker__chip--selected',
    'a2ui-choicepicker__button--selected',
    'a2ui-modal--open',
    'a2ui-tabs__tab--active',
  ],
})
