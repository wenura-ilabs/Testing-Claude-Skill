/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // The backend runs on :8080; proxying keeps API calls same-origin in development.
    proxy: { '/api': 'http://localhost:8080' },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.ts',
    reporters: ['default', 'junit'],
    outputFile: { junit: './test-results/junit.xml' },
  },
})
