import { useState } from 'react';
import { api } from '../api.js';

export default function AuthPanel({ t, lang, onAuth, note }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  async function submit(e, override) {
    e?.preventDefault();
    setBusy(true);
    setError('');
    try {
      const body = override || form;
      const res = mode === 'register' && !override ? await api.register({ ...body, lang }) : await api.login(body);
      onAuth(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card mx-auto max-w-md">
      {note && <p className="mb-4 text-sm text-slatem">{note}</p>}
      <form onSubmit={submit} className="space-y-3">
        {mode === 'register' && (
          <div>
            <label className="label" htmlFor="name">{t('name')}</label>
            <input id="name" className="field" required maxLength={120} value={form.name} onChange={set('name')} autoComplete="name" />
          </div>
        )}
        <div>
          <label className="label" htmlFor="email">{t('email')}</label>
          <input id="email" type="email" className="field" required value={form.email} onChange={set('email')} autoComplete="email" />
        </div>
        <div>
          <label className="label" htmlFor="pw">{t('password')}</label>
          <input id="pw" type="password" className="field" required minLength={mode === 'register' ? 8 : 1} value={form.password} onChange={set('password')} autoComplete={mode === 'register' ? 'new-password' : 'current-password'} />
        </div>
        {error && <p role="alert" className="text-sm text-ink">{error}</p>}
        <button className="btn-primary w-full" disabled={busy}>{mode === 'register' ? t('register') : t('signIn')}</button>
      </form>
      <div className="mt-4 flex flex-col gap-2 text-center text-sm">
        <button className="text-slatem underline-offset-4 hover:underline" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
          {mode === 'login' ? t('noAccount') : t('haveAccount')}
        </button>
        <button
          className="text-ink underline-offset-4 hover:underline"
          disabled={busy}
          onClick={() => submit(null, { email: 'parent@demo.chaukas.in', password: 'chaukas-demo' })}
        >
          {t('demo')}
        </button>
      </div>
    </section>
  );
}
