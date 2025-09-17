<!-- web/pages/order/[id].vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRuntimeConfig } from '#app'

type TrackingItemDto = {
  productId: number
  name: string | null
  brand: string | null
  category: string | null
  qty: number
}

type RecommendationItemDto = {
  productId: number
  name: string | null
  brand: string | null
  category: string | null
  score: number
}

type TrackingOrderDto = {
  id: number
  status: string
  items: TrackingItemDto[]
}

type TrackingPageDto = {
  order: TrackingOrderDto
  recommendations: RecommendationItemDto[]
}

const route = useRoute()
const { public: { apiBase } } = useRuntimeConfig()

const orderId = String(route.params.id ?? '')
const page = ref<TrackingPageDto | null>(null)
const loading = ref(true)
const err = ref<string | null>(null)

async function load() {
  loading.value = true
  err.value = null
  try {
    page.value = await $fetch<TrackingPageDto>(`/tracking/${orderId}`, {
      baseURL: apiBase,
      query: { limit: 12 }
    })
  } catch (e: any) {
    err.value = String(e?.message ?? e)
  } finally {
    loading.value = false
  }
}

async function addToCart(productId: number) {
  // record feedback event (only "added_to_cart")
  try {
    await $fetch(`/recommendations/${orderId}/feedback`, {
      baseURL: apiBase,
      method: 'POST',
      body: { productId, action: 'added_to_cart' }
    })
  } catch { /* ignore fire-and-forget */ }
  // (optional) toast or visual feedback could go here
}

onMounted(load)
</script>

<template>
  <main class="min-h-screen bg-gray-50 text-gray-900">
    <section class="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <header class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Track your order</h1>
          <p class="mt-1 text-sm text-gray-500">Order placed • status updated recently</p>
        </div>
      </header>

      <!-- errors / loading -->
      <div v-if="err" class="rounded-md border border-red-200 bg-red-50 p-3 text-red-700">
        {{ err }}
      </div>
      <div v-else-if="loading" class="text-gray-500">Loading…</div>

      <template v-else-if="page">
        <!-- your order items (chips) -->
        <h2 class="mb-3 text-lg font-semibold">Your order</h2>
        <div
            class="mb-8 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3"
            role="list"
            aria-label="Items in this order"
        >
          <div
              v-for="it in page.order.items"
              :key="it.productId"
              class="flex items-center justify-between rounded-2xl border border-gray-200 bg-white px-4 py-3 shadow-sm"
          >
            <div class="min-w-0">
              <p class="truncate font-medium">{{ it.name ?? 'Item' }}</p>
              <p class="truncate text-sm text-gray-500">
                {{ it.brand || '—' }} • {{ it.category || '—' }}
              </p>
            </div>
            <span class="rounded-full border border-gray-300 px-2 py-0.5 text-xs text-gray-600">Qty {{ it.qty }}</span>
          </div>
        </div>

        <!-- recommendations: single row, horizontal scroll -->
        <div class="mb-3 flex items-center justify-between">
          <h2 class="text-lg font-semibold">You may also like</h2>
        </div>

        <div
            class="no-scrollbar -mx-2 flex snap-x snap-mandatory gap-4 overflow-x-auto px-2 pb-2"
            aria-label="Recommended products"
        >
          <article
              v-for="rec in page.recommendations"
              :key="rec.productId"
              class="min-w-[320px] max-w-[360px] snap-start rounded-2xl border border-gray-200 bg-white p-4 shadow-sm"
          >
            <div class="flex items-start justify-between gap-3">
              <h3 class="line-clamp-1 text-lg font-semibold">{{ rec.name }}</h3>
              <div class="flex items-center gap-1 text-xs text-gray-500 shrink-0">
                <span aria-hidden="true">★</span>
                <span>{{ rec.score.toFixed(2) }}</span>
              </div>
            </div>

            <p class="mt-1 text-sm text-gray-500">
              {{ rec.brand || '—' }} • {{ rec.category || '—' }}
            </p>

            <div class="mt-4 flex gap-2">
              <button
                  type="button"
                  class="rounded-md bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800 active:bg-black"
                  @click="addToCart(rec.productId)"
              >
                Add to cart
              </button>
            </div>
          </article>
        </div>
      </template>
    </section>
  </main>
</template>

<style>
/* hide scrollbars for a cleaner carousel (still scrollable) */
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
