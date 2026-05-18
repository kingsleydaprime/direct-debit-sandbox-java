// ── State ──────────────────────────────────────────────────────────────────
let spec = null;
let authValues = {};
let activeKey = null;  // "method::path"
let panelOpen = false;

// ── Boot ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadAuth();
  wireModal();
  wireTryPanel();
  wireSearch();
  wireSidebarToggle();
  fetchSpec();
});

// ── Spec loading ───────────────────────────────────────────────────────────
async function fetchSpec() {
  const url = window.SPEC_URL || '/v3/api-docs';
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    spec = await res.json();
    renderSidebar();
    renderWelcome();
  } catch (err) {
    document.getElementById('sidebar-nav').innerHTML =
      `<p style="padding:16px;color:#dc2626;font-size:13px">Could not load spec: ${err.message}</p>`;
    document.getElementById('content').innerHTML =
      `<div style="padding:40px;color:#dc2626">Server not reachable. Start the app first.</div>`;
  }
}

// ── Auth ───────────────────────────────────────────────────────────────────
function loadAuth() {
  try { authValues = JSON.parse(sessionStorage.getItem('itc_auth') || '{}'); }
  catch { authValues = {}; }
  refreshAuthDot();
}

function persistAuth() {
  sessionStorage.setItem('itc_auth', JSON.stringify(authValues));
  refreshAuthDot();
}

function refreshAuthDot() {
  const dot = document.getElementById('auth-dot');
  const filled = Object.values(authValues).some(v => v && v.trim());
  dot.className = 'auth-dot' + (filled ? ' filled' : '');
}

function wireModal() {
  document.getElementById('btn-authorize').addEventListener('click', openAuthModal);
  document.getElementById('modal-x').addEventListener('click', closeAuthModal);
  document.getElementById('overlay').addEventListener('click', e => {
    if (e.target.id === 'overlay') closeAuthModal();
  });
  document.getElementById('btn-save-auth').addEventListener('click', () => {
    document.querySelectorAll('.auth-input').forEach(inp => {
      const v = inp.value.trim();
      if (v) authValues[inp.dataset.name] = v;
      else delete authValues[inp.dataset.name];
    });
    persistAuth();
    closeAuthModal();
    // refresh try panel auth section if open
    if (panelOpen && activeKey) {
      const [method, ...parts] = activeKey.split('::');
      refreshTryAuth();
    }
  });
  document.getElementById('btn-clear-auth').addEventListener('click', () => {
    authValues = {};
    persistAuth();
    renderAuthModal();  // re-render with empty values
  });
}

function openAuthModal() {
  renderAuthModal();
  document.getElementById('overlay').classList.remove('hidden');
}

function closeAuthModal() {
  document.getElementById('overlay').classList.add('hidden');
}

function renderAuthModal() {
  if (!spec) return;
  const body = document.getElementById('auth-modal-body');
  const fields = buildAuthFields();
  if (!fields.length) {
    body.innerHTML = '<p style="color:#9ca3af;font-size:13px">No security schemes defined.</p>';
    return;
  }
  body.innerHTML = fields.map(({ headerName, label, meta, description }) => `
    <div class="auth-field">
      <div class="auth-field-head">
        <span class="auth-field-name">${label}</span>
        <span class="auth-field-meta">${meta}</span>
      </div>
      ${description ? `<p class="auth-field-desc">${escHtml(description)}</p>` : ''}
      <input class="text-input auth-input" type="text"
        data-name="${headerName}" placeholder="Value…"
        value="${escHtml(authValues[headerName] || '')}"/>
    </div>
  `).join('');
}

function buildAuthFields() {
  const fields = [];
  const seen = new Set();

  // Security schemes — use the actual header name (s.name) as the key
  const schemes = spec.components?.securitySchemes || {};
  for (const [, s] of Object.entries(schemes)) {
    const headerName = s.name || s.paramName;
    if (!headerName || seen.has(headerName)) continue;
    seen.add(headerName);
    fields.push({ headerName, label: headerName, meta: `${s.in || 'header'} · ${s.type || 'apiKey'}`, description: s.description || '' });
  }

  // Global header parameters (x-transflowId, x-country, etc.)
  const params = spec.components?.parameters || {};
  for (const [, p] of Object.entries(params)) {
    if (p.in !== 'header' || seen.has(p.name)) continue;
    seen.add(p.name);
    fields.push({ headerName: p.name, label: p.name, meta: 'header · required', description: p.description || '' });
  }

  return fields;
}

// ── Sidebar toggle ─────────────────────────────────────────────────────────
function wireSidebarToggle() {
  const btn = document.getElementById('btn-sidebar-toggle');
  const layout = document.getElementById('layout');
  btn.addEventListener('click', () => {
    const hidden = layout.classList.toggle('sidebar-hidden');
    btn.classList.toggle('active', hidden);
    btn.title = hidden ? 'Show sidebar' : 'Hide sidebar';
  });
}

// ── Sidebar ────────────────────────────────────────────────────────────────
function renderSidebar() {
  const nav = document.getElementById('sidebar-nav');
  const groups = groupByTag(spec);
  const info = spec.info || {};
  nav.innerHTML = `
    <div class="intro-item active" id="intro-item" onclick="selectIntro()">
      <span class="intro-icon">☰</span>
      <span class="intro-label">${info.title || 'Introduction'}</span>
    </div>
  ` + Object.entries(groups).map(([tag, ops]) => `
    <div class="tag-group">
      <div class="tag-header" onclick="toggleTag(this)">
        <span class="tag-name">${tag}</span>
        <span class="tag-count">${ops.length}</span>
        <span class="tag-arrow">▾</span>
      </div>
      <div class="tag-ops">
        ${ops.map(({ path, method, op }) => `
          <div class="op-item"
            data-key="${method}::${path}"
            data-search="${(op.summary || path).toLowerCase()}"
            onclick="selectOp('${method}','${esc(path)}')">
            <span class="method-badge method-${method}">${method.toUpperCase()}</span>
            <span class="op-summary">${op.summary || path}</span>
          </div>
        `).join('')}
      </div>
    </div>
  `).join('');
}

function groupByTag(spec) {
  const groups = {};
  for (const [path, methods] of Object.entries(spec.paths || {})) {
    for (const [method, op] of Object.entries(methods)) {
      if (!['get','post','put','patch','delete'].includes(method)) continue;
      const tag = op.tags?.[0] || 'Other';
      (groups[tag] = groups[tag] || []).push({ path, method, op });
    }
  }
  return groups;
}

function toggleTag(header) {
  const ops = header.nextElementSibling;
  const arrow = header.querySelector('.tag-arrow');
  const collapsed = ops.classList.toggle('collapsed');
  arrow.textContent = collapsed ? '▸' : '▾';
}

// ── Search ─────────────────────────────────────────────────────────────────
function wireSearch() {
  document.getElementById('search-input').addEventListener('input', function() {
    const q = this.value.toLowerCase().trim();
    document.querySelectorAll('.op-item').forEach(el => {
      el.classList.toggle('hidden', q && !el.dataset.search.includes(q));
    });
  });
}

// ── Welcome ────────────────────────────────────────────────────────────────
function renderWelcome() {
  const info = spec.info || {};
  document.getElementById('content').innerHTML = `
    <div class="welcome">
      <div class="welcome-title-row">
        <h1>${info.title || 'API'}</h1>
        <span class="version-pill">v${info.version || '1.0'}</span>
      </div>
      <div class="welcome-desc">${info.description ? marked.parse(info.description) : ''}</div>
    </div>
  `;
}

function selectIntro() {
  document.querySelectorAll('.op-item').forEach(el => el.classList.remove('active'));
  const intro = document.getElementById('intro-item');
  if (intro) intro.classList.add('active');
  activeKey = null;
  closeTryPanel();
  renderWelcome();
}

// ── Operation detail ───────────────────────────────────────────────────────
function selectOp(method, path) {
  document.querySelectorAll('.op-item').forEach(el => el.classList.remove('active'));
  const intro = document.getElementById('intro-item');
  if (intro) intro.classList.remove('active');
  const key = `${method}::${path}`;
  const item = document.querySelector(`.op-item[data-key="${key}"]`);
  if (item) item.classList.add('active');

  if (activeKey === key && panelOpen) {
    // already selected + panel open — just refresh panel
  } else {
    closeTryPanel();
  }
  activeKey = key;
  renderOp(method, path, spec.paths[path][method]);
}

function renderOp(method, path, op) {
  const bodyHtml = buildBodySection(op);
  const responsesHtml = buildResponsesSection(op);

  document.getElementById('content').innerHTML = `
    <div class="op-detail">
      <div class="op-detail-top">
        <div>
          <div class="op-tag-breadcrumb">${op.tags?.[0] || ''}</div>
          <div class="op-path-row">
            <span class="method-badge method-${method} method-badge-lg">${method.toUpperCase()}</span>
            <code class="op-path-code">${path}</code>
          </div>
        </div>
        <button class="btn-try" id="btn-try"
          onclick="openTryPanel('${method}','${esc(path)}')">
          Try it out ▶
        </button>
      </div>

      ${op.summary ? `<h1 class="op-title">${op.summary}</h1>` : ''}
      ${op.description ? `<div class="op-desc">${marked.parse(op.description)}</div>` : ''}

      ${bodyHtml}
      ${responsesHtml}
    </div>
  `;
}

function buildBodySection(op) {
  if (!op.requestBody) return '';
  const schema = extractBodySchema(op);
  return `
    <div class="op-section">
      <div class="op-section-title">Request body</div>
      <div class="content-type-label">application/json</div>
      ${schema ? renderSchemaTable(schema) : '<p style="color:#9ca3af;font-size:13px">No schema.</p>'}
    </div>
  `;
}

function buildResponsesSection(op) {
  const responses = op.responses || {};
  if (!Object.keys(responses).length) return '';
  const rows = Object.entries(responses).map(([code, r]) => {
    const family = code.startsWith('2') ? 's2' : code.startsWith('4') ? 's4' :
                   code.startsWith('5') ? 's5' : 's0';
    const rowCls = code.startsWith('2') ? 'response-row-2xx' :
                   code.startsWith('4') ? 'response-row-4xx' :
                   code.startsWith('5') ? 'response-row-5xx' : '';
    const desc = r.description || '';
    // render description markdown inline (tables, bullets)
    const descHtml = desc.includes('\n') ? marked.parse(desc) :
                     `<span style="font-size:13px;color:var(--text-2)">${desc}</span>`;
    return `<tr class="${rowCls}">
      <td style="width:90px;vertical-align:top;padding-top:12px">
        <span class="response-status-pill ${family}">${code}</span>
      </td>
      <td style="font-size:13px">${descHtml}</td>
    </tr>`;
  }).join('');
  return `
    <div class="op-section">
      <div class="op-section-title">Responses</div>
      <table class="schema-table">
        <thead><tr><th style="width:90px">Code</th><th>Description</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    </div>
  `;
}

// ── Schema helpers ─────────────────────────────────────────────────────────
function deref(schemaOrRef) {
  if (!schemaOrRef || !schemaOrRef.$ref) return schemaOrRef;
  const parts = schemaOrRef.$ref.replace('#/', '').split('/');
  let node = spec;
  for (const p of parts) node = node?.[p];
  return node ? deref(node) : null;
}

function extractBodySchema(op) {
  const schema = op.requestBody?.content?.['application/json']?.schema;
  return schema ? deref(schema) : null;
}

function renderSchemaTable(schema) {
  const props = schema.properties || {};
  const required = new Set(schema.required || []);
  const keys = Object.keys(props);
  if (!keys.length) return '<p style="color:#9ca3af;font-size:13px">No properties.</p>';

  const rows = keys.map(name => {
    const p = deref(props[name]);
    return `<tr>
      <td class="field-name">
        <code>${name}</code>${required.has(name) ? '<span class="req-star">*</span>' : ''}
      </td>
      <td><span class="type-pill">${fieldTypeName(p)}</span></td>
      <td style="font-size:13px;color:#4b5563">${p?.description || ''}</td>
    </tr>`;
  }).join('');

  return `
    <table class="schema-table">
      <thead><tr><th>Field</th><th>Type</th><th>Description</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
  `;
}

function fieldTypeName(schema) {
  if (!schema) return 'any';
  if (schema.enum) return schema.enum.slice(0,4).join(' | ') + (schema.enum.length > 4 ? '…' : '');
  if (schema.type === 'array') return `array[${fieldTypeName(deref(schema.items))}]`;
  return schema.type || 'any';
}

function buildExampleFromSchema(schema, depth = 0) {
  if (depth > 4 || !schema) return {};
  const props = schema.properties || {};
  const out = {};
  for (const [name, rawProp] of Object.entries(props)) {
    const p = deref(rawProp);
    if (!p) continue;
    if (p.example !== undefined) {
      let v = p.example;
      if (typeof v === 'string') { try { v = JSON.parse(v); } catch {} }
      out[name] = v;
      continue;
    }
    if (p.enum?.length)           { out[name] = p.enum[0]; continue; }
    switch (p.type) {
      case 'boolean': out[name] = false; break;
      case 'integer':
      case 'number':  out[name] = 0; break;
      case 'array': {
        const items = deref(p.items);
        out[name] = items ? [buildExampleFromSchema(items, depth + 1)] : [];
        break;
      }
      case 'object': out[name] = buildExampleFromSchema(p, depth + 1); break;
      default: out[name] = ''; break;
    }
  }
  return out;
}

// ── Try-it panel ───────────────────────────────────────────────────────────
function wireTryPanel() {
  document.getElementById('try-close').addEventListener('click', closeTryPanel);
}

function openTryPanel(method, path) {
  const op = spec.paths[path][method];
  panelOpen = true;

  document.getElementById('layout').classList.add('panel-open');
  document.getElementById('try-panel').classList.remove('hidden');

  const btnTry = document.getElementById('btn-try');
  if (btnTry) btnTry.classList.add('active');

  // Header
  document.getElementById('try-panel-title').innerHTML = `
    <div class="try-panel-head-inner">
      <span class="method-badge method-${method}">${method.toUpperCase()}</span>
      <code style="font-size:13px;font-family:monospace">${path}</code>
    </div>
  `;

  renderTryBody(method, path, op);
}

function renderTryBody(method, path, op) {
  const authFields = buildAuthFields();
  const authSection = authFields.length ? `
    <div class="try-section">
      <div class="try-section-head">
        Authorization
        <button class="link-btn" onclick="openAuthModal()">Edit</button>
      </div>
      <div id="try-auth-rows">
        ${buildAuthRows()}
      </div>
    </div>
  ` : '';

  let bodySection = '';
  if (op.requestBody) {
    const schema = extractBodySchema(op);
    const example = schema ? buildExampleFromSchema(schema) : {};
    bodySection = `
      <div class="try-section">
        <div class="try-section-head">Request body</div>
        <textarea id="try-body" class="try-body-editor"
          spellcheck="false">${JSON.stringify(example, null, 2)}</textarea>
      </div>
    `;
  }

  document.getElementById('try-panel-body').innerHTML = `
    ${authSection}
    ${bodySection}
    <button class="btn-execute" id="btn-execute"
      onclick="executeReq('${method}','${esc(path)}')">
      Execute
    </button>
    <div id="try-response"></div>
  `;
}

function buildAuthRows() {
  return buildAuthFields().map(({ headerName }) => {
    const val = authValues[headerName];
    return `<div class="auth-row">
      <span class="auth-row-name">${headerName}</span>
      <span class="auth-row-value ${val ? 'set' : ''}">${val ? '••••••••' : 'not set'}</span>
    </div>`;
  }).join('');
}

function refreshTryAuth() {
  const el = document.getElementById('try-auth-rows');
  if (!el) return;
  el.innerHTML = buildAuthRows();
}

function closeTryPanel() {
  panelOpen = false;
  document.getElementById('layout').classList.remove('panel-open');
  document.getElementById('try-panel').classList.add('hidden');
  const btnTry = document.getElementById('btn-try');
  if (btnTry) btnTry.classList.remove('active');
}

async function executeReq(method, path) {
  const btn = document.getElementById('btn-execute');
  const responseDiv = document.getElementById('try-response');
  btn.disabled = true;
  responseDiv.innerHTML = '<div class="try-loading">Sending…</div>';

  const headers = { 'Content-Type': 'application/json' };
  // Inject all auth headers (security schemes + global header parameters)
  for (const { headerName } of buildAuthFields()) {
    if (authValues[headerName]) headers[headerName] = authValues[headerName];
  }

  let body;
  const bodyEl = document.getElementById('try-body');
  if (bodyEl) {
    try { JSON.parse(bodyEl.value); body = bodyEl.value; }
    catch {
      responseDiv.innerHTML = '<div class="try-error">❌ Invalid JSON in request body.</div>';
      btn.disabled = false;
      return;
    }
  }

  const t0 = Date.now();
  try {
    const res = await fetch(path, {
      method: method.toUpperCase(),
      headers,
      body: body || undefined
    });
    const elapsed = Date.now() - t0;
    const text = await res.text();
    let pretty = text;
    try { pretty = JSON.stringify(JSON.parse(text), null, 2); } catch {}

    const cls = res.status < 300 ? 's2' : res.status < 500 ? 's4' : 's5';

    const curlHeaders = Object.entries(headers)
      .map(([k, v]) => `-H '${k}: ${v}'`).join(' \\\n     ');
    const curlBody = body ? `\\\n     -d '${body.replace(/'/g, "'\\''")}'` : '';
    const curlCmd = `curl -X ${method.toUpperCase()} '${window.location.origin}${path}' \\\n     ${curlHeaders} ${curlBody}`;

    const isJson = res.headers.get('content-type')?.includes('json');
    const bodyHtml = isJson ? highlightJson(pretty) : escHtml(pretty);

    responseDiv.innerHTML = `
      <div class="response-status-row">
        <span class="response-badge ${cls}">${res.status} ${res.statusText}</span>
        <span class="response-time">${elapsed} ms</span>
      </div>
      <div class="try-section">
        <div class="try-section-head">Response body</div>
        <pre class="response-pre">${bodyHtml}</pre>
      </div>
      <div class="try-section">
        <div class="try-section-head">Curl</div>
        <pre class="response-pre">${highlightCurl(curlCmd)}</pre>
      </div>
    `;
  } catch (err) {
    responseDiv.innerHTML = `<div class="try-error">❌ Request failed: ${escHtml(err.message)}</div>`;
  } finally {
    btn.disabled = false;
  }
}

// ── Utilities ──────────────────────────────────────────────────────────────
function escHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// escape for inline onclick string attributes
function esc(s) {
  return s.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

// Curl syntax highlighting for dark pre blocks
function highlightCurl(raw) {
  let s = raw.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

  // curl keyword
  s = s.replace(/^curl\b/, '<span class="curl-cmd">curl</span>');

  // -X METHOD
  s = s.replace(/-X ([A-Z]+)/g, (_, m) =>
    `<span class="curl-flag">-X</span> <span class="curl-method method-${m.toLowerCase()}">${m}</span>`
  );

  // URL in single quotes
  s = s.replace(/'(https?:\/\/[^']+)'/g, `'<span class="curl-url">$1</span>'`);

  // -H 'Name: Value'  (name and value split on first colon+space)
  s = s.replace(/-H '([^:]+):\s*([^']*)'/g, (_, name, val) =>
    `<span class="curl-flag">-H</span> '<span class="curl-hname">${name}</span>: <span class="curl-hval">${val}</span>'`
  );

  // -d 'body'
  s = s.replace(/-d '([^']*)'/gs, (_, body) =>
    `<span class="curl-flag">-d</span> '<span class="curl-body">${body}</span>'`
  );

  // backslash line continuations
  s = s.replace(/\\\n/g, '<span class="curl-bs">\\</span>\n');

  return s;
}

// JSON syntax highlighting for dark pre blocks
// Escape & < > but keep " as-is so the regex can tokenise quoted strings.
function highlightJson(raw) {
  const safe = raw.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  return safe
    // keys: "name":
    .replace(/"([^"\n]*)"(\s*):/g, '<span class="json-key">"$1"</span>$2:')
    // string values: : "..."
    .replace(/:\s*"([^"\n]*)"/g, (m, v) => `: <span class="json-str">"${v}"</span>`)
    // array string items: standalone quoted strings on their own line / after [
    .replace(/(?<=[\[,]\s*)"([^"\n]*)"/g, (m, v) => `<span class="json-str">"${v}"</span>`)
    // numbers
    .replace(/:\s*(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)/g,
      (m, v) => `: <span class="json-num">${v}</span>`)
    // booleans and null
    .replace(/:\s*(true|false)/g, (m, v) => `: <span class="json-bool">${v}</span>`)
    .replace(/:\s*(null)/g, `: <span class="json-null">null</span>`);
}
