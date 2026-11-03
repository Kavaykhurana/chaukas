/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0b1220',
        panel: '#121a2b',
        edge: '#223049',
        saffron: '#ff9933',
        danger: '#ef4444',
        warn: '#f59e0b',
        calm: '#38bdf8',
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans Devanagari', 'Noto Sans Gurmukhi', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'ui-monospace', 'monospace'],
      },
    },
  },
  plugins: [],
};
