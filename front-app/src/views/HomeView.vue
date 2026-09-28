<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">Dashboard</h1>
      <p class="page-sub">Arquitectura Cloud Native · GDG Lima Meetup</p>
    </div>

    <!-- BFF offline alert -->
    <div v-if="!bffAvailable" class="alert alert-error">
      <span>⚠️</span>
      <div>
        <strong>BFF no disponible.</strong> Asegúrate de exponer el ingress gateway:<br />
        <code class="code" style="margin-top:6px;display:inline-block">
          kubectl -n istio-ingress port-forward svc/apps-gateway-istio 8080:80
        </code>
      </div>
    </div>

    <!-- Architecture flow card -->
    <div class="card arch-card">
      <div class="label" style="margin-bottom:16px">Flujo de la arquitectura</div>
      <div class="arch-grid">
        <!-- Cliente: centrado, origen del flujo -->
        <div class="arch-node arch-client arch-cell-span" style="grid-column:1">
          <div class="arch-icon">💻</div>
          <div class="arch-name">Cliente / Browser</div>
          <div class="arch-port">:5000</div>
          <div class="arch-tag">front (Vite)</div>
        </div>

        <div class="arch-arrow arch-cell-span" style="grid-column:2">
          →
          <div class="istio-badge" title="El proxy de Vite reenvía /bff al ingress gateway">🔒 proxy</div>
        </div>

        <!-- Ingress gateway: entrada única a la malla -->
        <div class="arch-node arch-gw arch-cell-span" style="grid-column:3">
          <div class="arch-icon">🚪</div>
          <div class="arch-name">Istio Gateway</div>
          <div class="arch-port">Gateway API · :80</div>
          <div class="arch-tag">apps-gateway</div>
        </div>

        <div class="arch-arrow arch-cell-span" style="grid-column:4">
          →
          <div class="istio-badge" title="Solo el gateway puede llamar al BFF (experience-allow-gateway)">🔒 allow gateway</div>
        </div>

        <!-- BFF: punto de fan-out hacia ambos servicios -->
        <div class="arch-node arch-bff arch-cell-span" style="grid-column:5">
          <div class="arch-icon">🌐</div>
          <div class="arch-name">BFF</div>
          <div class="arch-port">:8082</div>
          <div class="arch-tag">experience-order-tracker</div>
        </div>

        <!-- Fila superior: BFF → order-service → PostgreSQL -->
        <div class="arch-diag arch-diag-top" style="grid-column:6;grid-row:1">
          ↗
          <div class="istio-badge" title="order-allow-experience: solo el BFF puede llamar a order-service">🔒 allow BFF</div>
        </div>
        <div class="arch-node arch-svc" style="grid-column:7;grid-row:1">
          <div class="arch-icon">📦</div>
          <div class="arch-name">order-service</div>
          <div class="arch-port">:8080</div>
        </div>
        <div class="arch-arrow" style="grid-column:8;grid-row:1">
          →
          <div class="istio-badge" title="Persistencia JDBC hacia PostgreSQL">🗄️ JDBC</div>
        </div>
        <div class="arch-node arch-db" style="grid-column:9;grid-row:1">
          <div class="arch-icon">🗄️</div>
          <div class="arch-name">PostgreSQL</div>
          <div class="arch-port">orders DB</div>
        </div>

        <!-- Fila inferior: BFF → tracking-service → Nominatim -->
        <div class="arch-diag arch-diag-bottom" style="grid-column:6;grid-row:2">
          ↘
          <div class="istio-badge" title="tracking-allow-experience: solo el BFF puede llamar a tracking-service">🔒 allow BFF</div>
        </div>
        <div class="arch-node arch-svc" style="grid-column:7;grid-row:2">
          <div class="arch-icon">📍</div>
          <div class="arch-name">tracking-service</div>
          <div class="arch-port">:8081</div>
        </div>
        <div class="arch-arrow" style="grid-column:8;grid-row:2">
          →
          <div class="istio-badge" title="Reverse geocoding vía HTTPS">🗺️ egress</div>
        </div>
        <div class="arch-node arch-ext" style="grid-column:9;grid-row:2">
          <div class="arch-icon">🗺️</div>
          <div class="arch-name">Nominatim</div>
          <div class="arch-port">geocoding</div>
        </div>
      </div>

      <div class="label" style="margin:24px 0 14px">Seguridad de la malla (Istio)</div>
      <div class="sec-grid">
        <div class="sec-item">
          <div class="sec-id">mTLS</div>
          <div>
            <div class="sec-name">PeerAuthentication/default</div>
            <div class="sec-desc">mTLS STRICT en el namespace apps: todo el tráfico service-to-service va cifrado y autenticado.</div>
          </div>
        </div>
        <div class="sec-item">
          <div class="sec-id">AuthZ</div>
          <div>
            <div class="sec-name">experience-allow-gateway</div>
            <div class="sec-desc">El BFF solo acepta llamadas del ingress gateway (SA apps-gateway-istio).</div>
          </div>
        </div>
        <div class="sec-item">
          <div class="sec-id">AuthZ</div>
          <div>
            <div class="sec-name">order-allow-experience</div>
            <div class="sec-desc">order-service solo acepta llamadas del BFF; se permite /q/health/* para las probes.</div>
          </div>
        </div>
        <div class="sec-item">
          <div class="sec-id">AuthZ</div>
          <div>
            <div class="sec-name">tracking-allow-experience</div>
            <div class="sec-desc">tracking-service solo acepta llamadas del BFF; se permite /q/health/* para las probes.</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stack versions -->
    <div class="card">
      <div class="label" style="margin-bottom:14px">Stack del proyecto</div>
      <div class="stack-row">
        <div class="stack-item">
          <div class="stack-icon">⚡</div>
          <div>
            <div class="stack-name">Quarkus</div>
            <div class="stack-version">3.33 LTS</div>
          </div>
        </div>
        <div class="stack-item">
          <div class="stack-icon">☕</div>
          <div>
            <div class="stack-name">Java</div>
            <div class="stack-version">21</div>
          </div>
        </div>
        <div class="stack-item">
          <div class="stack-icon">☸️</div>
          <div>
            <div class="stack-name">Kubernetes (kind)</div>
            <div class="stack-version">1.36</div>
          </div>
        </div>
        <div class="stack-item">
          <div class="stack-icon">🛡️</div>
          <div>
            <div class="stack-name">Istio + Gateway API</div>
            <div class="stack-version">1.31.1</div>
          </div>
        </div>
        <div class="stack-item">
          <div class="stack-icon">🐘</div>
          <div>
            <div class="stack-name">PostgreSQL</div>
            <div class="stack-version">16</div>
          </div>
        </div>
        <div class="stack-item">
          <div class="stack-icon">🔭</div>
          <div>
            <div class="stack-name">OpenTelemetry</div>
            <div class="stack-version">OTLP</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="card">
      <div class="label" style="margin-bottom:14px">Acciones rápidas</div>
      <div class="actions">
        <button class="btn-primary" @click="$router.push('/orders')">📦 Crear Orden</button>
        <button class="btn-ghost" @click="$router.push('/orders')">🔍 Ver Experiencia de Orden</button>
      </div>
    </div>

    <!-- Scenarios guide -->
    <div class="card">
      <div class="label" style="margin-bottom:14px">Escenarios de demo</div>
      <div class="scenarios">
        <div class="scenario">
          <div class="scenario-num">1</div>
          <div>
            <strong>Zero-trust de Istio</strong>
            <p class="text-muted">Un pod con un ServiceAccount no autorizado intenta llamar directamente a
               <code class="code">order-service:8080</code> saltándose el BFF. La <em>AuthorizationPolicy</em>
               <code class="code">order-allow-experience</code> lo rechaza con
               <span class="badge badge-error">403</span> — solo el BFF puede hablar con order-service. El
               tráfico SÍ autorizado (vía BFF) sigue respondiendo <span class="badge badge-ok">200</span>.</p>
            <pre class="code-block" style="margin-top:8px">kubectl -n apps run authz-test --image=curlimages/curl:8.10.1 --restart=Never --command -- sleep 300
kubectl -n apps exec authz-test -- curl -s -o /dev/null -w '%{http_code}\n' http://order-service.apps.svc.cluster.local:8080/orders</pre>
          </div>
        </div>
        <div class="scenario">
          <div class="scenario-num">2</div>
          <div>
            <strong>order-service → tracking-service denegado</strong>
            <p class="text-muted">El endpoint de demo <code class="code">GET /orders/{orderId}/tracking-status</code>
               hace que order-service llame directo a tracking-service (un flujo que nunca debería ocurrir: el
               camino real es BFF → tracking). La <em>AuthorizationPolicy</em>
               <code class="code">tracking-allow-experience</code> deniega al SA de order-service, así que el
               endpoint responde <span class="badge badge-error">503</span> con el motivo del bloqueo.</p>
            <pre class="code-block" style="margin-top:8px">kubectl -n apps port-forward svc/order-service 8080:8080
curl -s http://localhost:8080/orders/&lt;UUID&gt;/tracking-status</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/client.js'

const bffAvailable = ref(true)

onMounted(async () => {
  try {
    await api.listOrders()
  } catch {
    bffAvailable.value = false
  }
})
</script>

<style scoped>
/* ── Architecture diagram ────────────────────────────────────────────────── */
/* Flujo de izquierda a derecha: Cliente → Istio Gateway → BFF → {order-service, tracking-service} → {PostgreSQL, Nominatim}.
   Cliente, Gateway y BFF ocupan las 2 filas (centrados); las flechas diagonales representan
   el fan-out del BFF hacia cada servicio. */
.arch-grid {
  display: grid;
  grid-template-columns: repeat(9, auto);
  grid-auto-rows: auto;
  align-items: center;
  justify-items: center;
  column-gap: 10px;
  row-gap: 22px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.arch-arrow {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  font-size: 20px; color: var(--muted); flex-shrink: 0;
}
.arch-diag {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  font-size: 22px; color: var(--muted); flex-shrink: 0;
}
.arch-cell-span { grid-row: 1 / 3; align-self: center; }
.arch-node {
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 12px 16px;
  text-align: center;
  min-width: 130px;
  flex-shrink: 0;
}
.arch-client { border-color: var(--muted); }
.arch-gw     { border-color: var(--warning); background: rgba(245,158,11,.07); }
.arch-bff    { border-color: var(--primary); background: rgba(99,102,241,.07); }
.arch-svc    { border-color: var(--info);    background: rgba(56,189,248,.07); }
.arch-db     { border-color: var(--border);  background: rgba(255,255,255,.03); }
.arch-ext    { border-color: var(--warning); background: rgba(245,158,11,.07); }
.arch-icon { font-size: 20px; margin-bottom: 6px; }
.arch-name { font-size: 12px; font-weight: 600; margin-bottom: 2px; }
.arch-port { font-size: 11px; font-family: var(--mono); color: var(--muted); }
.arch-tag  { font-size: 10px; color: var(--primary-l); margin-top: 3px; }

/* ── Istio badges on connectors ──────────────────────────────────────────── */
.istio-badge {
  display: inline-block;
  margin-top: 2px;
  font-size: 9px;
  font-family: var(--mono);
  font-weight: 600;
  color: var(--warning);
  background: rgba(245,158,11,.1);
  border: 1px solid rgba(245,158,11,.35);
  border-radius: 999px;
  padding: 1px 7px;
  white-space: nowrap;
  cursor: help;
}

/* ── Mesh security list/grid ─────────────────────────────────────────────── */
.sec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}
.sec-item {
  display: flex; gap: 10px; align-items: flex-start;
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 10px 12px;
  background: rgba(255,255,255,.02);
}
.sec-id {
  flex-shrink: 0;
  min-width: 32px; height: 28px; border-radius: 6px;
  background: rgba(245,158,11,.12); color: var(--warning);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 700; font-family: var(--mono);
  padding: 0 6px;
}
.sec-name { font-size: 12px; font-weight: 600; margin-bottom: 3px; font-family: var(--mono); }
.sec-desc { font-size: 12px; color: var(--muted); line-height: 1.4; }

/* ── Stack versions ──────────────────────────────────────────────────────── */
.stack-row { display: flex; gap: 16px; flex-wrap: wrap; }
.stack-item {
  flex: 1; min-width: 200px;
  display: flex; align-items: center; gap: 12px;
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 12px 16px;
}
.stack-icon { font-size: 22px; flex-shrink: 0; }
.stack-name { font-size: 12px; font-weight: 600; }
.stack-version { font-size: 12px; font-family: var(--mono); color: var(--muted); margin-top: 2px; }

/* ── Quick actions ───────────────────────────────────────────────────────── */
.actions { display: flex; gap: 10px; flex-wrap: wrap; }

/* ── Scenarios ───────────────────────────────────────────────────────────── */
.scenarios { display: flex; flex-direction: column; gap: 20px; }
.scenario { display: flex; gap: 16px; }
.scenario-num {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0; margin-top: 2px;
}
.scenario strong { display: block; margin-bottom: 6px; }

/* ── Spacing overrides ───────────────────────────────────────────────────── */
.card { margin-bottom: 20px; }
.arch-card { overflow: visible; }
</style>
