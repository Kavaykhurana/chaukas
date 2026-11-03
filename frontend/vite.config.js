import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev: proxy /api to the Spring Boot server so the browser sees one origin.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: { '/api': 'http://localhost:8080' } },
});
