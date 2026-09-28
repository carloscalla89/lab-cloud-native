import axios from 'axios'

const http = axios.create({
  baseURL: '/bff',
  timeout: 20000,
  headers: { 'Content-Type': 'application/json' }
})

export default {
  // ── Órdenes ──────────────────────────────────────────────────────────────
  listOrders: () =>
    http.get('/experience/orders'),

  getOrderWithTracking: (orderId) =>
    http.get(`/experience/orders/${orderId}`),

  createOrder: (payload) =>
    http.post('/experience/orders', payload),

  createOrderWithDestination: (payload) =>
    http.post('/experience/orders/with-destination', payload),

  deleteOrder: (orderId) =>
    http.delete(`/experience/orders/${orderId}`)
}
