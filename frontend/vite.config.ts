import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {

  const env = loadEnv(mode, process.cwd());

  const PORT = `${env.VITE_PORT ?? '8080'}`;

  return {
    plugins: [tailwindcss(), react()],
    server: {
      proxy: {
        "/api": {
          target: `http://localhost:${PORT}`,
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
    }
  }
})
