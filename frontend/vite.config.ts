import { fileURLToPath, URL } from 'node:url'

import { loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default (mode: string) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) }
  const config = {
    plugins: [
      vue(),
      vueJsx(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      proxy: {
        '^/api/.*': {
          // target: `http://${process.env.VITE_BACKEND_URL}`,
          target: `http://${process.env.BACKEND_URL}`,
          // target: `http://localhost:8080`,
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/api/, ''),
        },
        //port: parseInt(process.env.VITE_PORT),
      },
      allowedHosts: true
    }
  };
  
  // For more detailed logging:
  // console.log('Vite config (formatted):', JSON.stringify(config, null, 2));
  
  return config;
}
