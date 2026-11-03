/** Refero Parchment tokens: paper, ink and one clay accent. No shadows, no extra hues. */
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#141413',
        slatem: '#3d3d3a',
        cloud: '#87867f',
        cloudl: '#b0aea5',
        stone: '#cccbc8',
        oat: '#e3dacc',
        manilla: '#f5e3c7',
        ivory: '#f0eee6',
        paper: '#faf9f5',
        clay: '#d97757',
        claydeep: '#c6613f',
      },
      fontFamily: {
        serif: ['"Source Serif 4"', 'Charter', 'Georgia', 'Noto Sans Devanagari', 'Noto Sans Gurmukhi', 'serif'],
        sans: ['Inter', 'Noto Sans Devanagari', 'Noto Sans Gurmukhi', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'Menlo', 'monospace'],
      },
      boxShadow: { none: 'none' },
    },
  },
  corePlugins: {},
  plugins: [],
};
