<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">📦 Órdenes</h1>
      <p class="page-sub">Crear órdenes y consultar la vista compuesta (orden + tracking) vía BFF</p>
    </div>

    <div class="two-col">
      <!-- ── Panel izquierdo: Listado + detalle ──────────────────────────── -->
      <div class="left-panel">

        <!-- Búsqueda por ID -->
        <div class="card">
          <div class="label" style="margin-bottom:12px">Ver experiencia de orden</div>
          <div class="search-row">
            <input v-model="searchId" placeholder="UUID de la orden…" @keyup.enter="fetchOrder" />
            <button class="btn-primary btn-sm" :disabled="loadingOrder || !searchId.trim()" @click="fetchOrder">
              <span v-if="loadingOrder" class="spinner" style="width:14px;height:14px"></span>
              <span v-else>Buscar</span>
            </button>
          </div>
          <p class="text-muted" style="margin-top:6px;font-size:12px">
            Devuelve la composición BFF: orden + tracking geocodificado.
          </p>
          <div v-if="orderError" class="alert alert-error" style="margin-top:10px;margin-bottom:0">
            <span>❌</span>
            <div>
              <strong>Error al obtener la orden</strong><br />
              <span class="code">{{ orderError }}</span>
            </div>
          </div>
        </div>

        <!-- Lista de órdenes -->
        <div class="card">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:14px">
            <div style="display:flex;align-items:center;gap:8px">
              <div class="label" style="margin-bottom:0">Todas las órdenes</div>
              <span class="badge badge-created">{{ orders.length }}</span>
            </div>
            <button class="btn-ghost btn-sm" :disabled="loadingList" @click="fetchOrders">
              <span v-if="loadingList" class="spinner" style="width:12px;height:12px"></span>
              <span v-else>↺ Refrescar</span>
            </button>
          </div>
          <div v-if="orders.length === 0 && !loadingList" class="empty">
            <div class="empty-icon">📭</div>
            <p>No hay órdenes aún</p>
          </div>
          <div v-else class="order-list">
            <div
              v-for="o in orders" :key="o.orderId"
              class="order-row"
              :class="{ selected: searchId === o.orderId }"
            >
              <div class="order-row-content" @click="selectOrder(o.orderId)">
                <div class="order-row-id">
                  <span class="code">{{ o.orderId.substring(0, 8) }}…</span>
                  <span class="badge" :class="statusBadge(o.status)">{{ o.status }}</span>
                </div>
                <div class="order-row-meta">
                  <span>👤 {{ o.customerId }}</span>
                  <span>📦 {{ o.itemCount }} item{{ o.itemCount !== 1 ? 's' : '' }}</span>
                  <span class="amount">$ {{ o.totalAmount }}</span>
                </div>
              </div>
              <button
                class="btn-icon-cancel"
                title="Cancelar orden"
                @click.stop="askCancelOrder(o)"
              >🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Panel derecho: Crear orden ─────────────────────────────────── -->
      <div class="right-panel">
        <div class="card">
          <!-- Encabezado con selector de endpoint -->
          <div class="form-header">
            <div class="label" style="margin-bottom:0">Crear nueva orden</div>
            <div class="endpoint-toggle">
              <button
                class="toggle-btn"
                :class="{ 'toggle-btn--active': !includeDestination }"
                @click="includeDestination = false"
              >
                Solo orden
              </button>
              <button
                class="toggle-btn"
                :class="{ 'toggle-btn--active': includeDestination }"
                @click="includeDestination = true"
              >
                📍 Con destino
              </button>
            </div>
          </div>

          <!-- Indicador del endpoint que se va a llamar -->
          <div class="endpoint-badge">
            <span class="endpoint-method">POST</span>
            <span class="endpoint-path">{{ includeDestination ? '/experience/orders/with-destination' : '/experience/orders' }}</span>
          </div>

          <hr style="margin-bottom:16px" />

          <!-- Customer ID -->
          <div class="form-group">
            <div class="label">Customer ID</div>
            <input v-model="form.customerId" placeholder="Ej: cliente-demo-001" />
          </div>

          <!-- Productos -->
          <div class="label" style="margin-bottom:8px">Productos ({{ form.items.length }})</div>
          <div v-for="(item, idx) in form.items" :key="idx" class="item-form">
            <div class="item-form-header">
              <span style="font-size:12px;font-weight:600">Producto {{ idx + 1 }}</span>
              <button v-if="form.items.length > 1" class="btn-ghost btn-sm" @click="removeItem(idx)">✕</button>
            </div>
            <div class="form-group">
              <div class="label">Product ID</div>
              <input v-model="item.productId" placeholder="Ej: PROD-001" />
            </div>
            <div class="form-group">
              <div class="label">Nombre</div>
              <input v-model="item.productName" placeholder="Ej: Laptop Gaming" />
            </div>
            <div class="item-row2">
              <div class="form-group">
                <div class="label">Cantidad</div>
                <input v-model.number="item.quantity" type="number" min="1" />
              </div>
              <div class="form-group">
                <div class="label">Precio unitario</div>
                <input v-model.number="item.unitPrice" type="number" min="0" step="0.01" />
              </div>
            </div>
          </div>

          <button class="btn-ghost" style="width:100%;margin-bottom:14px" @click="addItem">+ Agregar producto</button>

          <!-- Presets de orden -->
          <div class="label" style="margin-bottom:8px">Presets rápidos de orden</div>
          <div class="presets">
            <button class="btn-ghost btn-sm" @click="applyPreset('laptop')">💻 Laptop</button>
            <button class="btn-ghost btn-sm" @click="applyPreset('phone')">📱 Smartphone</button>
            <button class="btn-ghost btn-sm" @click="applyPreset('multi')">📦 Multi-item</button>
          </div>

          <!-- ── Sección de destino (solo visible cuando está activada) ── -->
          <template v-if="includeDestination">
            <hr />
            <div class="destination-section">
              <div class="destination-title">
                <span>📍</span>
                <span>Dirección de destino</span>
              </div>
              <p class="text-muted" style="font-size:12px;margin-bottom:14px">
                Las coordenadas se geocodifican vía Nominatim. El BFF las registra en
                tracking-service usando el orderId como shipmentId.
              </p>

              <div class="coord-row">
                <div class="form-group">
                  <div class="label">Latitud</div>
                  <input v-model.number="form.destinationLatitude" type="number" step="0.0001" placeholder="-12.0464" />
                </div>
                <div class="form-group">
                  <div class="label">Longitud</div>
                  <input v-model.number="form.destinationLongitude" type="number" step="0.0001" placeholder="-77.0428" />
                </div>
              </div>

              <div class="label" style="margin-bottom:8px">Ubicaciones rápidas 🇵🇪</div>
              <div class="presets">
                <button
                  v-for="loc in locations" :key="loc.name"
                  class="btn-ghost btn-sm preset-loc"
                  :class="{ 'preset-loc--active': isActiveLocation(loc) }"
                  @click="setLocation(loc)"
                >
                  {{ loc.name }}
                </button>
              </div>
            </div>
          </template>

          <hr />

          <button
            class="btn-primary"
            style="width:100%"
            :disabled="creatingOrder || !formValid"
            @click="submitCreateOrder"
          >
            <span v-if="creatingOrder" class="spinner" style="width:14px;height:14px;margin-right:8px"></span>
            {{ includeDestination ? 'Crear Orden + Registrar Destino' : 'Crear Orden' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ── Modal de resultado ─────────────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showModal && createResult" class="modal-overlay">
        <div class="modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="badge badge-ok" style="font-size:12px">201 Created</span>
              <span style="font-weight:700;font-size:16px">Orden creada</span>
            </div>
          </div>

          <div class="modal-body">
            <!-- Timing -->
            <div class="result-timing">
              Tiempo de respuesta:
              <span class="badge" :class="timingBadge(createTiming)">{{ createTiming }} ms</span>
            </div>

            <hr />

            <!-- Order ID -->
            <div class="dfield" style="margin-bottom:14px">
              <div class="label">Order ID</div>
              <div class="copy-row">
                <span class="code">{{ createResult.orderId }}</span>
                <button class="btn-ghost btn-sm" @click="copyId(createResult.orderId)">📋 Copiar</button>
              </div>
            </div>

            <!-- Resumen de la orden -->
            <div class="detail-grid">
              <div class="dfield">
                <div class="label">Cliente</div>
                <div>{{ createResult.customerId }}</div>
              </div>
              <div class="dfield">
                <div class="label">Status</div>
                <span class="badge" :class="statusBadge(createResult.status)">{{ createResult.status }}</span>
              </div>
              <div class="dfield">
                <div class="label">Items</div>
                <div>{{ createResult.itemCount }}</div>
              </div>
              <div class="dfield">
                <div class="label">Total</div>
                <div class="amount">$ {{ createResult.totalAmount }}</div>
              </div>
            </div>

            <!-- Destino geocodificado (solo para el endpoint with-destination) -->
            <template v-if="'destinationRegistered' in createResult">
              <hr />
              <div class="label" style="margin-bottom:10px">
                Destino de envío
                <span
                  class="badge"
                  :class="createResult.destinationRegistered ? 'badge-ok' : 'badge-error'"
                  style="margin-left:8px"
                >
                  {{ createResult.destinationRegistered ? 'registrado' : 'no registrado' }}
                </span>
              </div>

              <div v-if="createResult.destinationRegistered && createResult.destination" class="destination-result">
                <div class="destination-result-icon">🗺️</div>
                <div>
                  <div class="destination-result-addr">{{ createResult.destination.address }}</div>
                  <div class="destination-result-meta">
                    <span v-if="createResult.destination.city">{{ createResult.destination.city }}</span>
                    <span v-if="createResult.destination.country"> · {{ createResult.destination.country }}</span>
                  </div>
                  <div class="destination-result-coords">
                    {{ createResult.destination.latitude.toFixed(5) }},
                    {{ createResult.destination.longitude.toFixed(5) }}
                  </div>
                </div>
              </div>

              <div v-else class="alert alert-warn" style="margin:0">
                <span>⚠️</span>
                <span>tracking-service no disponible — la orden se creó pero el destino no se registró (degradación controlada)</span>
              </div>
            </template>
          </div>

          <div class="modal-footer">
            <button class="btn-primary" @click="closeModal">Aceptar</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── Modal de error al crear orden ────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showErrorModal && createError" class="modal-overlay">
        <div class="modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="badge badge-error" style="font-size:12px">Error</span>
              <span style="font-weight:700;font-size:16px">No se pudo crear la orden</span>
            </div>
          </div>

          <div class="modal-body">
            <div class="alert alert-error" style="margin:0">
              <span>❌</span>
              <span class="code">{{ createError }}</span>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-primary" @click="closeErrorModal">Aceptar</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── Modal de experiencia de orden ────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showDetailModal && orderDetail" class="modal-overlay" @click.self="closeDetailModal">
        <div class="modal modal--wide">
          <div class="modal-header">
            <div class="modal-title">
              <span class="badge" :class="statusBadge(orderDetail.status)">{{ orderDetail.status }}</span>
              <span style="font-weight:700;font-size:16px">Experiencia de orden</span>
            </div>
            <button class="modal-close" @click="closeDetailModal" title="Cerrar">✕</button>
          </div>

          <div class="modal-body">
            <!-- ID + resumen -->
            <div class="dfield" style="margin-bottom:14px">
              <div class="label">Order ID</div>
              <div class="copy-row">
                <span class="code">{{ orderDetail.orderId }}</span>
                <button class="btn-ghost btn-sm" @click="copyId(orderDetail.orderId)">📋 Copiar</button>
              </div>
            </div>
            <div class="detail-grid">
              <div class="dfield">
                <div class="label">Cliente</div>
                <div>{{ orderDetail.customerId }}</div>
              </div>
              <div class="dfield">
                <div class="label">Total</div>
                <div class="amount">$ {{ orderDetail.totalAmount }}</div>
              </div>
            </div>

            <!-- Items -->
            <div v-if="orderDetail.items?.length" style="margin-top:14px">
              <div class="label" style="margin-bottom:8px">Líneas ({{ orderDetail.items.length }})</div>
              <div class="items-table">
                <div class="items-header">
                  <span>Producto</span><span>Qty</span><span>Precio unit.</span><span>Subtotal</span>
                </div>
                <div class="items-row" v-for="(item, i) in orderDetail.items" :key="i">
                  <span>{{ item.productName }}</span>
                  <span>{{ item.quantity }}</span>
                  <span>$ {{ item.unitPrice }}</span>
                  <span>$ {{ item.subtotal }}</span>
                </div>
              </div>
            </div>

            <!-- Tracking -->
            <hr />
            <div class="label" style="margin-bottom:10px">
              Tracking
              <span class="badge" :class="orderDetail.tracking?.available ? 'badge-ok' : 'badge-error'" style="margin-left:8px">
                {{ orderDetail.tracking?.available ? 'disponible' : 'no disponible' }}
              </span>
            </div>
            <div v-if="orderDetail.tracking?.available && orderDetail.tracking?.currentLocation">
              <div class="location-card">
                <div class="location-icon">📍</div>
                <div>
                  <div class="location-addr">{{ orderDetail.tracking.currentLocation.address }}</div>
                  <div class="location-coords">
                    {{ orderDetail.tracking.currentLocation.latitude.toFixed(5) }},
                    {{ orderDetail.tracking.currentLocation.longitude.toFixed(5) }}
                  </div>
                </div>
              </div>
              <div class="label" style="margin:12px 0 8px">
                Historial de tracking ({{ orderDetail.tracking.history?.length ?? 0 }} eventos)
              </div>
              <div class="history-list">
                <div class="history-item" v-for="(pt, i) in orderDetail.tracking.history" :key="i">
                  <div class="history-dot"></div>
                  <div class="history-body">
                    <div class="history-addr">{{ pt.address }}</div>
                    <div class="history-meta">
                      <span v-if="pt.city">{{ pt.city }}</span>
                      <span v-if="pt.country"> · {{ pt.country }}</span>
                      <span class="text-muted"> · {{ formatDate(pt.occurredAt) }}</span>
                    </div>
                    <div class="history-coords text-muted">{{ pt.latitude.toFixed(4) }}, {{ pt.longitude.toFixed(4) }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else-if="!orderDetail.tracking?.available" class="alert alert-warn" style="margin:0">
              <span>⚠️</span>
              <span>tracking-service no disponible — degradación controlada (BFF devolvió la orden sin tracking)</span>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-primary" @click="closeDetailModal">Cerrar</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── Modal de confirmacion: cancelar orden ────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showCancelConfirm && orderToCancel" class="modal-overlay" @click.self="closeCancelConfirm">
        <div class="modal">
          <div class="modal-header">
            <div class="modal-title">
              <span style="font-size:20px">⚠️</span>
              <span style="font-weight:700;font-size:16px">Cancelar orden</span>
            </div>
            <button class="modal-close" @click="closeCancelConfirm" title="Cerrar">✕</button>
          </div>

          <div class="modal-body">
            <p>¿Seguro que deseas cancelar esta orden? Esta acción no se puede deshacer.</p>

            <div class="dfield" style="margin-top:14px">
              <div class="label">Order ID</div>
              <span class="code">{{ orderToCancel.orderId }}</span>
            </div>
            <div class="detail-grid" style="margin-top:10px">
              <div class="dfield">
                <div class="label">Cliente</div>
                <div>{{ orderToCancel.customerId }}</div>
              </div>
              <div class="dfield">
                <div class="label">Total</div>
                <div class="amount">$ {{ orderToCancel.totalAmount }}</div>
              </div>
            </div>

            <div v-if="cancelError" class="alert alert-error" style="margin-top:14px;margin-bottom:0">
              <span>❌</span>
              <div>
                <strong>Error al cancelar la orden</strong><br />
                <span class="code">{{ cancelError }}</span>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-ghost" :disabled="cancelling" @click="closeCancelConfirm">Volver</button>
            <button
              class="btn-primary"
              style="background:var(--error);border-color:var(--error)"
              :disabled="cancelling"
              @click="confirmCancelOrder"
            >
              <span v-if="cancelling" class="spinner" style="width:14px;height:14px;margin-right:8px"></span>
              Sí, cancelar orden
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api/client.js'

// ── Ubicaciones peruanas de demo ─────────────────────────────────────────
const locations = [
  { name: 'Lima Centro',  latitude: -12.0464, longitude: -77.0428 },
  { name: 'Miraflores',   latitude: -12.1219, longitude: -77.0296 },
  { name: 'San Isidro',   latitude: -12.1007, longitude: -77.0366 },
  { name: 'Callao',       latitude: -12.0565, longitude: -77.1186 },
  { name: 'Cusco',        latitude: -13.5319, longitude: -71.9675 },
  { name: 'Arequipa',     latitude: -16.4090, longitude: -71.5375 },
  { name: 'Trujillo',     latitude:  -8.1091, longitude: -79.0215 },
  { name: 'Iquitos',      latitude:  -3.7491, longitude: -73.2538 },
]

// ── Estado de lista ──────────────────────────────────────────────────────
const orders = ref([])
const loadingList = ref(false)

async function fetchOrders() {
  loadingList.value = true
  try {
    const res = await api.listOrders()
    orders.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loadingList.value = false
  }
}

// ── Estado de detalle ────────────────────────────────────────────────────
const searchId = ref('')
const orderDetail = ref(null)
const orderError = ref('')
const loadingOrder = ref(false)
const showDetailModal = ref(false)

function closeDetailModal() { showDetailModal.value = false }

function selectOrder(id) {
  searchId.value = id
  fetchOrder()
}

async function fetchOrder() {
  const id = searchId.value.trim()
  if (!id) return
  loadingOrder.value = true
  orderDetail.value = null
  orderError.value = ''
  try {
    const res = await api.getOrderWithTracking(id)
    orderDetail.value = res.data
    showDetailModal.value = true
  } catch (e) {
    const msg = e.response?.data?.message ?? e.response?.data ?? e.message
    orderError.value = typeof msg === 'string' ? msg : JSON.stringify(msg)
  } finally {
    loadingOrder.value = false
  }
}

// ── Estado del formulario ────────────────────────────────────────────────
const includeDestination = ref(false)

const form = ref({
  customerId: '',
  items: [emptyItem()],
  destinationLatitude: -12.0464,
  destinationLongitude: -77.0428
})

function emptyItem() {
  return { productId: '', productName: '', quantity: 1, unitPrice: 0 }
}

const formValid = computed(() => {
  if (!form.value.customerId.trim()) return false
  const itemsOk = form.value.items.every(
    i => i.productId.trim() && i.productName.trim() && i.quantity > 0 && i.unitPrice >= 0
  )
  if (!itemsOk) return false
  if (includeDestination.value) {
    return form.value.destinationLatitude != null && form.value.destinationLongitude != null
  }
  return true
})

function addItem() { form.value.items.push(emptyItem()) }
function removeItem(idx) { form.value.items.splice(idx, 1) }

function setLocation(loc) {
  form.value.destinationLatitude = loc.latitude
  form.value.destinationLongitude = loc.longitude
}

function isActiveLocation(loc) {
  return form.value.destinationLatitude === loc.latitude &&
         form.value.destinationLongitude === loc.longitude
}

function applyPreset(type) {
  const destinations = { ...form.value }
  if (type === 'laptop') {
    Object.assign(form.value, {
      customerId: 'cliente-demo-001',
      items: [{ productId: 'LAPTOP-001', productName: 'Laptop Gaming 16"', quantity: 1, unitPrice: 1299.99 }]
    })
  } else if (type === 'phone') {
    Object.assign(form.value, {
      customerId: 'cliente-demo-002',
      items: [{ productId: 'PHONE-001', productName: 'Smartphone Pro Max', quantity: 2, unitPrice: 899.99 }]
    })
  } else if (type === 'multi') {
    Object.assign(form.value, {
      customerId: 'cliente-demo-003',
      items: [
        { productId: 'HEADSET-001', productName: 'Headset Inalámbrico', quantity: 1, unitPrice: 249.99 },
        { productId: 'MOUSE-001',   productName: 'Mouse Ergonómico',    quantity: 2, unitPrice:  79.99 },
        { productId: 'PAD-001',     productName: 'Mouse Pad XL',        quantity: 1, unitPrice:  34.99 }
      ]
    })
  }
  // Mantiene los valores de destino que el usuario haya elegido
  form.value.destinationLatitude = destinations.destinationLatitude
  form.value.destinationLongitude = destinations.destinationLongitude
}

// ── Crear orden ───────────────────────────────────────────────────────────
const creatingOrder = ref(false)
const createResult = ref(null)
const createError = ref('')
const createTiming = ref(0)
const showModal = ref(false)
const showErrorModal = ref(false)

function closeModal() { showModal.value = false }
function closeErrorModal() { showErrorModal.value = false; createError.value = '' }

async function submitCreateOrder() {
  creatingOrder.value = true
  createResult.value = null
  createError.value = ''
  const t0 = Date.now()

  const orderPayload = {
    customerId: form.value.customerId,
    items: form.value.items.map(i => ({
      productId: i.productId,
      productName: i.productName,
      quantity: i.quantity,
      unitPrice: i.unitPrice
    }))
  }

  try {
    let res
    if (includeDestination.value) {
      res = await api.createOrderWithDestination({
        ...orderPayload,
        destinationLatitude: form.value.destinationLatitude,
        destinationLongitude: form.value.destinationLongitude
      })
    } else {
      res = await api.createOrder(orderPayload)
    }
    createTiming.value = Date.now() - t0
    createResult.value = res.data
    showModal.value = true
    await fetchOrders()
  } catch (e) {
    const msg = e.response?.data?.message ?? e.response?.data ?? e.message
    createError.value = typeof msg === 'string' ? msg : JSON.stringify(msg)
    showErrorModal.value = true
  } finally {
    creatingOrder.value = false
  }
}

// ── Cancelar orden ───────────────────────────────────────────────────────
const showCancelConfirm = ref(false)
const orderToCancel = ref(null)
const cancelling = ref(false)
const cancelError = ref('')

function askCancelOrder(order) {
  orderToCancel.value = order
  cancelError.value = ''
  showCancelConfirm.value = true
}

function closeCancelConfirm() {
  if (cancelling.value) return
  showCancelConfirm.value = false
  orderToCancel.value = null
}

async function confirmCancelOrder() {
  if (!orderToCancel.value) return
  cancelling.value = true
  cancelError.value = ''
  try {
    await api.deleteOrder(orderToCancel.value.orderId)
    showCancelConfirm.value = false
    orderToCancel.value = null
    await fetchOrders()
  } catch (e) {
    const msg = e.response?.data?.message ?? e.response?.data ?? e.message
    cancelError.value = typeof msg === 'string' ? msg : JSON.stringify(msg)
  } finally {
    cancelling.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────
function statusBadge(status) {
  const map = { CREATED: 'badge-created', CONFIRMED: 'badge-confirmed', CANCELLED: 'badge-cancelled' }
  return map[status] ?? 'badge-created'
}

function timingBadge(ms) {
  if (ms < 500) return 'badge-ok'
  if (ms < 2000) return 'badge-slow'
  return 'badge-error'
}

function formatDate(iso) {
  if (!iso) return '–'
  return new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'medium' })
}

function copyId(id) {
  navigator.clipboard.writeText(id).catch(() => {})
}

onMounted(fetchOrders)
</script>

<style scoped>
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; align-items: start; }
.left-panel, .right-panel { display: flex; flex-direction: column; gap: 16px; }

/* ── Search ──────────────────────────────────────────────────────────────── */
.search-row { display: flex; gap: 8px; }
.search-row input { flex: 1; }

/* ── Order list ──────────────────────────────────────────────────────────── */
.order-list { display: flex; flex-direction: column; gap: 8px; max-height: 360px; overflow-y: auto; }
.order-row {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border: 1px solid var(--border); border-radius: var(--r);
  transition: all .15s;
}
.order-row:hover { background: var(--bg-hover); border-color: var(--primary); }
.order-row.selected { border-color: var(--primary); background: rgba(99,102,241,.07); }
.order-row-content { flex: 1; min-width: 0; cursor: pointer; }
.order-row-id { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.order-row-meta { display: flex; gap: 12px; font-size: 12px; color: var(--muted); }

.btn-icon-cancel {
  flex-shrink: 0; width: 30px; height: 30px; border-radius: 50%;
  background: transparent; border: 1px solid var(--border); color: var(--muted);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; cursor: pointer; transition: all .15s; padding: 0;
}
.btn-icon-cancel:hover { background: var(--error); border-color: var(--error); color: #fff; }

/* ── Order detail ─────────────────────────────────────────────────────────── */
.order-detail-card { border-color: var(--primary); }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.dfield .label { margin-bottom: 2px; }
.amount { font-family: var(--mono); font-size: 16px; font-weight: 700; color: var(--success); }
.copy-row { display: flex; align-items: center; gap: 8px; }

/* ── Items table ─────────────────────────────────────────────────────────── */
.items-table { border: 1px solid var(--border); border-radius: var(--r); overflow: hidden; font-size: 12px; }
.items-header, .items-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; }
.items-header { background: rgba(255,255,255,.04); padding: 6px 10px; color: var(--muted); font-weight: 600; }
.items-row { padding: 7px 10px; border-top: 1px solid var(--border); }
.items-row:hover { background: var(--bg-hover); }

/* ── Tracking (vista de detalle) ─────────────────────────────────────────── */
.location-card { display: flex; gap: 12px; padding: 12px; background: rgba(34,197,94,.06); border: 1px solid rgba(34,197,94,.2); border-radius: var(--r); }
.location-icon { font-size: 20px; }
.location-addr { font-weight: 600; font-size: 13px; margin-bottom: 3px; }
.location-coords { font-family: var(--mono); font-size: 11px; color: var(--muted); }
.history-list { display: flex; flex-direction: column; max-height: 240px; overflow-y: auto; }
.history-item { display: flex; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--border); }
.history-item:last-child { border-bottom: none; }
.history-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); margin-top: 5px; flex-shrink: 0; }
.history-addr { font-size: 12px; font-weight: 500; margin-bottom: 2px; }
.history-meta { font-size: 11px; color: var(--muted); margin-bottom: 2px; }
.history-coords { font-family: var(--mono); font-size: 10px; }

/* ── Form header + endpoint toggle ──────────────────────────────────────── */
.form-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.endpoint-toggle { display: flex; border: 1px solid var(--border); border-radius: var(--r); overflow: hidden; }
.toggle-btn { padding: 5px 12px; font-size: 12px; font-weight: 500; background: transparent; color: var(--muted); border: none; border-radius: 0; cursor: pointer; transition: all .15s; }
.toggle-btn:hover { background: var(--bg-hover); color: var(--text); }
.toggle-btn--active { background: var(--primary); color: #fff; }

.endpoint-badge { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.endpoint-method { font-family: var(--mono); font-size: 11px; font-weight: 700; background: var(--primary); color: #fff; padding: 2px 7px; border-radius: 4px; }
.endpoint-path { font-family: var(--mono); font-size: 11px; color: var(--muted); }

/* ── Item form ───────────────────────────────────────────────────────────── */
.item-form { border: 1px solid var(--border); border-radius: var(--r); padding: 12px; margin-bottom: 10px; }
.item-form-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.item-row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.presets { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 4px; }

/* ── Destination section ─────────────────────────────────────────────────── */
.destination-section { padding: 16px; background: rgba(56,189,248,.05); border: 1px solid rgba(56,189,248,.2); border-radius: var(--r); margin-top: 4px; }
.destination-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 13px; margin-bottom: 8px; color: var(--info); }
.coord-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.preset-loc { transition: all .15s; }
.preset-loc--active { border-color: var(--info); color: var(--info); background: rgba(56,189,248,.1); }

/* ── Result card ─────────────────────────────────────────────────────────── */
.result-card { border-color: var(--success); }
.result-timing { font-size: 12px; color: var(--muted); margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }

.destination-result { display: flex; gap: 14px; padding: 14px; background: rgba(34,197,94,.07); border: 1px solid rgba(34,197,94,.25); border-radius: var(--r); }
.destination-result-icon { font-size: 24px; }
.destination-result-addr { font-size: 13px; font-weight: 600; margin-bottom: 4px; }
.destination-result-meta { font-size: 12px; color: var(--muted); margin-bottom: 3px; }
.destination-result-coords { font-family: var(--mono); font-size: 11px; color: var(--muted); }

.card { margin-bottom: 0; }

/* ── Modal ───────────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0, 0, 0, .65);
  backdrop-filter: blur(3px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.modal {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 24px 48px rgba(0,0,0,.6);
  width: 100%; max-width: 520px;
  max-height: 90vh;
  display: flex; flex-direction: column;
}
.modal--wide { max-width: 680px; }
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.modal-title { display: flex; align-items: center; gap: 12px; }
.modal-close {
  width: 32px; height: 32px; border-radius: 50%;
  background: transparent; color: var(--muted);
  border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; cursor: pointer; transition: all .15s; padding: 0;
}
.modal-close:hover { background: var(--error); border-color: var(--error); color: #fff; }
.modal-body { padding: 20px; overflow-y: auto; flex: 1; }
.modal-footer {
  display: flex; gap: 10px; justify-content: flex-end;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

/* ── Transición del modal ─────────────────────────────────────────────────── */
.modal-enter-active, .modal-leave-active { transition: opacity .2s ease; }
.modal-enter-active .modal, .modal-leave-active .modal { transition: transform .2s ease, opacity .2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal, .modal-leave-to .modal { transform: translateY(-16px) scale(.97); opacity: 0; }
</style>
