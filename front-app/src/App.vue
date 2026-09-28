<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-icon">🌐</span>
        <div>
          <div class="brand-title">GDG Lima Meetup</div>
          <div class="brand-sub">Arquitectura Cloud Native</div>
        </div>
      </div>

      <nav class="nav">
        <RouterLink to="/" class="nav-link" :class="{ 'nav-link--active': $route.path === '/' }">
          <span>📊</span> Dashboard
        </RouterLink>
        <RouterLink to="/orders" class="nav-link" :class="{ 'nav-link--active': $route.path === '/orders' }">
          <span>📦</span> Órdenes
        </RouterLink>
      </nav>

      <div class="sidebar-info">
        <div class="svc-row">
          <span class="svc-label">BFF</span>
          <span class="svc-port">:8082</span>
        </div>
        <div class="svc-row">
          <span class="svc-label">order-service</span>
          <span class="svc-port">:8080</span>
        </div>
        <div class="svc-row">
          <span class="svc-label">tracking-service</span>
          <span class="svc-port">:8081</span>
        </div>
      </div>

      <div class="status-footer">
        <span class="dot" :class="bffOnline ? 'dot--on' : 'dot--off'"></span>
        <div>
          <div class="status-name">experience-order-tracker</div>
          <div class="status-url">via gateway → :8080</div>
        </div>
      </div>
    </aside>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from './api/client.js'

const bffOnline = ref(false)

onMounted(async () => {
  try {
    await api.listOrders()
    bffOnline.value = true
  } catch {
    bffOnline.value = false
  }
})
</script>

<style>
/* ── Reset + tokens globales ─────────────────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

:root {
  --bg:          #0f172a;
  --bg-card:     #1e293b;
  --bg-hover:    #263045;
  --bg-input:    #162032;
  --border:      #334155;
  --text:        #e2e8f0;
  --muted:       #94a3b8;
  --primary:     #6366f1;
  --primary-l:   #818cf8;
  --success:     #22c55e;
  --success-bg:  rgba(34,197,94,.12);
  --error:       #ef4444;
  --error-bg:    rgba(239,68,68,.12);
  --warning:     #f59e0b;
  --warning-bg:  rgba(245,158,11,.12);
  --info:        #38bdf8;
  --info-bg:     rgba(56,189,248,.12);
  --r:           8px;
  --shadow:      0 4px 12px rgba(0,0,0,.5);
  --mono:        'JetBrains Mono', monospace;
}

body {
  background: var(--bg);
  color: var(--text);
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 14px;
  line-height: 1.5;
}

a { text-decoration: none; color: inherit; }

/* ── Inputs ──────────────────────────────────────────────────────────────── */
input, textarea, select {
  background: var(--bg-input);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 8px 12px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  width: 100%;
  transition: border-color .15s;
}
input:focus, textarea:focus, select:focus { border-color: var(--primary); }
input::placeholder, textarea::placeholder { color: var(--muted); }

/* ── Buttons ─────────────────────────────────────────────────────────────── */
button { cursor: pointer; font-size: 14px; font-family: inherit; border: none; border-radius: var(--r); padding: 9px 18px; font-weight: 500; transition: all .15s; }
.btn-primary  { background: var(--primary);  color: #fff; }
.btn-primary:hover:not(:disabled)  { background: var(--primary-l); }
.btn-primary:disabled  { opacity: .45; cursor: not-allowed; }
.btn-ghost { background: transparent; color: var(--muted); border: 1px solid var(--border); padding: 6px 12px; font-size: 12px; }
.btn-ghost:hover { background: var(--bg-hover); color: var(--text); }
.btn-danger { background: var(--error); color: #fff; }
.btn-sm { padding: 5px 10px; font-size: 12px; }

/* ── Cards ───────────────────────────────────────────────────────────────── */
.card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 20px;
  box-shadow: var(--shadow);
}

/* ── Badges ──────────────────────────────────────────────────────────────── */
.badge {
  display: inline-flex; align-items: center; padding: 2px 10px;
  border-radius: 999px; font-size: 11px; font-weight: 600; white-space: nowrap;
}
.badge-created   { background: var(--info-bg);    color: var(--info); }
.badge-confirmed { background: var(--success-bg); color: var(--success); }
.badge-cancelled { background: var(--error-bg);   color: var(--error); }
.badge-ok        { background: var(--success-bg); color: var(--success); }
.badge-error     { background: var(--error-bg);   color: var(--error); }
.badge-slow      { background: var(--warning-bg); color: var(--warning); }

/* ── Labels ──────────────────────────────────────────────────────────────── */
.label {
  font-size: 11px; color: var(--muted); font-weight: 600;
  text-transform: uppercase; letter-spacing: .06em; margin-bottom: 6px;
}

/* ── Misc ────────────────────────────────────────────────────────────────── */
.form-group { margin-bottom: 14px; }
hr { border: none; border-top: 1px solid var(--border); margin: 18px 0; }
.code { font-family: var(--mono); font-size: 12px; background: rgba(0,0,0,.3); padding: 3px 7px; border-radius: 4px; word-break: break-all; }
pre.code-block {
  font-family: var(--mono); font-size: 12px;
  background: rgba(0,0,0,.4); border: 1px solid var(--border);
  padding: 14px 16px; border-radius: var(--r); overflow-x: auto;
  color: var(--info);
}
.text-muted  { color: var(--muted); font-size: 13px; }
.text-success{ color: var(--success); }
.text-error  { color: var(--error); }
.text-warn   { color: var(--warning); }
.text-mono   { font-family: var(--mono); }
.gap-8  { gap: 8px; }
.gap-12 { gap: 12px; }
.flex   { display: flex; }
.flex-col { display: flex; flex-direction: column; }
.items-center { align-items: center; }
.wrap   { flex-wrap: wrap; }

/* ── Page skeleton ───────────────────────────────────────────────────────── */
.page { padding: 28px 32px; max-width: 1100px; }
.page-header { margin-bottom: 24px; }
.page-title  { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.page-sub    { color: var(--muted); font-size: 13px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.grid-1 { display: grid; grid-template-columns: 1fr; gap: 20px; }

/* ── Spinner ─────────────────────────────────────────────────────────────── */
.spinner {
  width: 18px; height: 18px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Alert banner ────────────────────────────────────────────────────────── */
.alert {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 14px 16px; border-radius: var(--r);
  border-left: 3px solid;
  margin-bottom: 20px; font-size: 13px;
}
.alert-error   { background: var(--error-bg);   border-color: var(--error);   color: var(--error); }
.alert-success { background: var(--success-bg); border-color: var(--success); color: var(--success); }
.alert-warn    { background: var(--warning-bg); border-color: var(--warning); color: var(--warning); }
.alert-info    { background: var(--info-bg);    border-color: var(--info);    color: var(--info); }

/* ── Empty state ─────────────────────────────────────────────────────────── */
.empty { text-align: center; padding: 40px 20px; color: var(--muted); }
.empty-icon { font-size: 36px; margin-bottom: 12px; }
</style>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  height: 100vh;
  overflow: hidden;
}

/* ── Sidebar ─────────────────────────────────────────────────────────────── */
.sidebar {
  background: #080f1f;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.brand {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 18px;
  border-bottom: 1px solid var(--border);
}
.brand-icon  { font-size: 26px; }
.brand-title { font-size: 13px; font-weight: 700; }
.brand-sub   { font-size: 10px; color: var(--muted); margin-top: 1px; }

.nav {
  flex: 1;
  padding: 14px 10px;
  display: flex; flex-direction: column; gap: 2px;
}
.nav-link {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 10px;
  border-radius: var(--r);
  color: var(--muted);
  font-size: 13px;
  font-weight: 500;
  transition: all .15s;
}
.nav-link:hover { background: var(--bg-hover); color: var(--text); }
.nav-link--active { background: rgba(99,102,241,.15); color: var(--primary-l); }

.sidebar-info {
  padding: 14px 18px;
  border-top: 1px solid var(--border);
  display: flex; flex-direction: column; gap: 6px;
}
.svc-row { display: flex; justify-content: space-between; align-items: center; }
.svc-label { font-size: 11px; color: var(--muted); }
.svc-port  { font-size: 11px; font-family: var(--mono); color: var(--primary-l); }

.status-footer {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 18px;
  border-top: 1px solid var(--border);
}
.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot--on  { background: var(--success); box-shadow: 0 0 6px var(--success); }
.dot--off { background: var(--error);   box-shadow: 0 0 6px var(--error);   animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100% { opacity: 1 } 50% { opacity: .4 } }
.status-name { font-size: 11px; font-weight: 600; }
.status-url  { font-size: 10px; color: var(--muted); }

/* ── Main content ─────────────────────────────────────────────────────────── */
.content { overflow-y: auto; }
</style>
