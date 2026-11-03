// Thin fetch wrapper. VITE_API_URL is empty in dev (Vite proxies /api) and the backend origin in production.
const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

let token = null;
try {
  token = localStorage.getItem('chaukas.token');
} catch {
  /* storage unavailable: session lives in memory only */
}

export function setToken(t) {
  token = t;
  try {
    if (t) localStorage.setItem('chaukas.token', t);
    else localStorage.removeItem('chaukas.token');
  } catch {
    /* ignore */
  }
}

export const hasToken = () => !!token;

async function call(path, { method = 'GET', json, form } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  let body;
  if (json !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(json);
  } else if (form) {
    body = form;
  }

  let res;
  try {
    res = await fetch(`${BASE}${path}`, { method, headers, body });
  } catch {
    throw new Error('Cannot reach the server. Check your connection and try again.');
  }

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    /* non-JSON body */
  }
  if (!res.ok) {
    if (res.status === 401 && token) setToken(null);
    const msg = (data && data.error) || (res.status === 429 ? 'Too many requests. Wait a minute.' : `Request failed (${res.status})`);
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  register: (b) => call('/api/auth/register', { method: 'POST', json: b }),
  login: (b) => call('/api/auth/login', { method: 'POST', json: b }),
  me: () => call('/api/auth/me'),
  setLang: (lang) => call('/api/auth/lang', { method: 'PUT', json: { lang } }),
  health: () => call('/api/health'),

  check: ({ text, image, lang }) => {
    const f = new FormData();
    if (text) f.append('text', text);
    if (image) f.append('image', image);
    f.append('lang', lang);
    return call('/api/checks', { method: 'POST', form: f });
  },

  createIncident: ({ narrative, images, lang }) => {
    const f = new FormData();
    if (narrative) f.append('narrative', narrative);
    images.forEach((i) => f.append('images', i));
    f.append('lang', lang);
    return call('/api/incidents', { method: 'POST', form: f });
  },
  incidents: () => call('/api/incidents'),
  reportIncident: (id, shareToCommunity) =>
    call(`/api/incidents/${id}/report`, { method: 'POST', json: { shareToCommunity } }),

  family: () => call('/api/guardians'),
  invite: () => call('/api/guardians/invites', { method: 'POST' }),
  accept: (code) => call('/api/guardians/accept', { method: 'POST', json: { code } }),
  alerts: () => call('/api/guardians/alerts'),
  markAlertsRead: () => call('/api/guardians/alerts/read', { method: 'POST' }),
  unread: () => call('/api/guardians/alerts/unread-count'),
};
