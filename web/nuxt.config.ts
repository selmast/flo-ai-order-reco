// web/nuxt.config.ts
export default defineNuxtConfig({
  devtools: { enabled: true },

  devServer: { host: 'localhost', port: 5173 },

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8081',
    },
  },

  typescript: { shim: false },

  app: {
    head: {
      title: 'FLO AI – Order Tracking & Recommendations',
      meta: [{ name: 'viewport', content: 'width=device-width, initial-scale=1' }],
    },
  },

  css: ['~/assets/css/tailwind.css'],

  // 👇 NEW: use Tailwind’s v4 PostCSS plugin
  postcss: {
    plugins: {
      '@tailwindcss/postcss': {},
      autoprefixer: {},
    },
  },
})