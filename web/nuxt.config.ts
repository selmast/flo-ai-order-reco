// web/nuxt.config.ts
export default defineNuxtConfig({
  devtools: { enabled: true },
  runtimeConfig: {
    // Server-only (if needed later)
    // apiSecret: process.env.API_SECRET,
    public: {
      // Used by the browser: read from .env
      apiBase: process.env.API_BASE || "http://localhost:8080",
    },
  },
  app: {
    head: {
      title: 'FLO AI – Order Tracking & Recommendations',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  },
})
