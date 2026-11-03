import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';

export default function FamilyTab({ t, onRead }) {
  const [family, setFamily] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [code, setCode] = useState('');
  const [mine, setMine] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      const [f, a] = await Promise.all([api.family(), api.alerts()]);
      setFamily(f);
      setAlerts(a);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function run(fn) {
    setBusy(true);
    setError('');
    try {
      await fn();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const createInvite = () => run(async () => setMine((await api.invite()).code));
  const accept = (e) => { e.preventDefault(); run(async () => { await api.accept(code); setCode(''); }); };
  const markRead = () => run(async () => { await api.markAlertsRead(); onRead?.(); });

  const statusLabel = (s) => (s === 'ACTIVE' ? t('active') : t('pending'));

  return (
    <div className="space-y-5">
      <section className="card space-y-3">
        <h2 className="font-sans text-[24px] font-semibold leading-[1.3]">{t('familyTitle')}</h2>
        <p className="text-sm text-slatem">{t('familyHint')}</p>
        <div className="flex flex-wrap items-center gap-3">
          <button className="btn-primary" onClick={createInvite} disabled={busy}>{t('invite')}</button>
          {mine && (
            <div className="rounded-xl border border-ink bg-manilla px-4 py-2">
              <p className="text-xs text-slatem">{t('yourCode')}</p>
              <p className="font-mono text-2xl tracking-[0.25em] text-ink">{mine}</p>
            </div>
          )}
        </div>
        <form onSubmit={accept} className="flex flex-wrap items-end gap-2 border-t border-stone pt-3">
          <div className="min-w-[180px] flex-1">
            <label htmlFor="code" className="label">{t('enterCode')}</label>
            <input id="code" className="field font-mono uppercase tracking-widest" maxLength={16} value={code} onChange={(e) => setCode(e.target.value)} />
          </div>
          <button className="btn-ghost" disabled={busy || !code.trim()}>{t('accept')}</button>
        </form>
        {error && <p role="alert" className="text-sm text-ink">{error}</p>}
      </section>

      {family && (family.myGuardians.length > 0 || family.myWards.length > 0) && (
        <section className="grid gap-4 sm:grid-cols-2">
          <div className="card">
            <h3 className="label">{t('myGuardians')}</h3>
            <ul className="space-y-1 text-sm">
              {family.myGuardians.map((l) => (
                <li key={l.id} className="flex justify-between gap-2">
                  <span>{l.guardianName || <span className="font-mono text-slatem">{l.code}</span>}</span>
                  <span className="text-xs text-slatem">{statusLabel(l.status)}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="card">
            <h3 className="label">{t('myWards')}</h3>
            <ul className="space-y-1 text-sm">
              {family.myWards.map((l) => (
                <li key={l.id} className="flex justify-between gap-2">
                  <span>{l.wardName}</span>
                  <span className="text-xs text-slatem">{statusLabel(l.status)}</span>
                </li>
              ))}
            </ul>
          </div>
        </section>
      )}

      <section className="card">
        <div className="mb-2 flex items-center justify-between">
          <h3 className="text-base font-semibold">{t('alerts')}</h3>
          {alerts.some((a) => !a.read) && <button className="btn-ghost !px-3 !py-1 text-xs" onClick={markRead}>{t('markRead')}</button>}
        </div>
        {alerts.length === 0 ? (
          <p className="text-sm text-slatem">{t('noAlerts')}</p>
        ) : (
          <ul className="space-y-2">
            {alerts.map((a) => (
              <li key={a.id} className={`rounded-xl border p-3 text-sm ${a.read ? 'border-stone text-slatem' : 'border-ink bg-manilla text-ink'}`}>
                <p>{a.message}</p>
                <p className="mt-1 text-xs text-slatem">{new Date(a.createdAt).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' })}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
