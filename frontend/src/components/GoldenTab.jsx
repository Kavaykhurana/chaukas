import { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';

function CopyBox({ t, title, text, subject }) {
  const [copied, setCopied] = useState(false);
  async function copy() {
    const full = subject ? `${t('subject')}: ${subject}\n\n${text}` : text;
    try {
      await navigator.clipboard.writeText(full);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch {
      /* clipboard blocked: user can still select the text */
    }
  }
  return (
    <div className="rounded-xl border border-stone bg-paper">
      <div className="flex items-center justify-between border-b border-stone px-3 py-2">
        <h4 className="text-sm font-semibold">{title}</h4>
        <button type="button" className="btn-ghost !px-3 !py-1 text-xs" onClick={copy}>{copied ? t('copied') : t('copy')}</button>
      </div>
      {subject && <p className="border-b border-stone px-3 py-2 text-xs text-slatem">{t('subject')}: <span className="text-ink">{subject}</span></p>}
      <pre className="max-h-72 overflow-auto whitespace-pre-wrap break-words px-3 py-3 font-sans text-sm leading-relaxed text-ink">{text}</pre>
    </div>
  );
}

function Countdown({ t, incident }) {
  // Server gives minutesLeft at fetch time; tick it down locally every 30 s.
  const [left, setLeft] = useState(incident.minutesLeft);
  const start = useRef(Date.now());
  useEffect(() => {
    start.current = Date.now();
    setLeft(incident.minutesLeft);
    const id = setInterval(() => {
      const passed = Math.floor((Date.now() - start.current) / 60000);
      setLeft(Math.max(0, incident.minutesLeft - passed));
    }, 30000);
    return () => clearInterval(id);
  }, [incident.id, incident.minutesLeft]);

  if (left <= 0) {
    return <div className="rounded-xl border border-ink bg-manilla p-3 text-sm text-ink">{t('windowOver')}</div>;
  }
  const pct = Math.min(100, Math.round((left / 60) * 100));
  return (
    <div className="note !rounded-xl">
      <div className="flex items-baseline gap-2">
        <span className="font-serif text-[61px] leading-none text-ink">{left}</span>
        <span className="text-sm text-ink">{t('minLeft')}</span>
      </div>
      <div className="mt-3 h-2 overflow-hidden rounded-full bg-stone" role="progressbar" aria-valuenow={left} aria-valuemin={0} aria-valuemax={60}>
        <div className="h-full rounded-full bg-ink transition-all" style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

function Fact({ label, value, missing, miss }) {
  return (
    <div className="rounded-xl border border-stone bg-paper p-3">
      <p className="label !mb-0.5">{label}</p>
      <p className={`break-all font-mono text-sm ${missing ? 'text-ink' : 'text-ink'}`}>{missing ? miss : value}</p>
    </div>
  );
}

export default function GoldenTab({ t, lang }) {
  const [narrative, setNarrative] = useState('');
  const [images, setImages] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [inc, setInc] = useState(null);
  const [share, setShare] = useState(true);
  const fileRef = useRef(null);

  // Resume the latest open incident so the countdown survives a page reload.
  useEffect(() => {
    api.incidents().then((list) => {
      const open = list.find((i) => i.status === 'OPEN');
      if (open) setInc(open);
    }).catch(() => {});
  }, []);

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      setInc(await api.createIncident({ narrative: narrative.trim(), images, lang }));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function markReported() {
    try {
      setInc(await api.reportIncident(inc.id, share));
    } catch (err) {
      setError(err.message);
    }
  }

  if (inc) {
    const f = inc.facts;
    const miss = (k) => inc.missing.includes(k);
    return (
      <div className="space-y-5">
        <Countdown t={t} incident={inc} />

        <section className="card space-y-3">
          <h2 className="font-sans text-[24px] font-semibold leading-[1.3]">{t('facts')}</h2>
          <div className="grid gap-2 sm:grid-cols-2">
            <Fact label={t('amount')} value={f.amount != null ? `₹${Number(f.amount).toLocaleString('en-IN')}` : ''} missing={miss('amount')} miss={t('missing')} />
            <Fact label={t('utr')} value={f.utr} missing={miss('utr')} miss={t('missing')} />
            <Fact label={t('payee')} value={f.payeeUpi || f.payeePhone} missing={miss('payee')} miss={t('missing')} />
            <Fact label={t('bank')} value={f.bank} missing={miss('bank')} miss={t('missing')} />
          </div>
          <p className="text-xs text-slatem">{t('paidAt')}: {new Date(f.paidAt).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' })} IST</p>
        </section>

        <section className="space-y-3">
          <h2 className="font-sans text-[24px] font-semibold leading-[1.3]">{t('stepsTitle')}</h2>
          <p className="text-sm text-slatem">{t('s1')}</p>
          <CopyBox t={t} title="1930" text={inc.kit.callScript} />
          <p className="text-sm text-slatem">{t('s2')}</p>
          <CopyBox t={t} title="cybercrime.gov.in" text={inc.kit.complaintText} />
          <a className="btn-ghost" href="https://cybercrime.gov.in" target="_blank" rel="noreferrer noopener">{t('openPortal')} ↗</a>
          <p className="text-sm text-slatem">{t('s3')}</p>
          <CopyBox t={t} title="Bank" subject={inc.kit.bankEmailSubject} text={inc.kit.bankEmailBody} />
          <p className="text-sm text-slatem">{t('s4')}</p>
          <CopyBox t={t} title="UPI / PSP" text={inc.kit.scammerReport} />
          {inc.kit.checklist?.length > 0 && (
            <ul className="list-disc space-y-1 pl-5 text-sm text-slatem">
              {inc.kit.checklist.map((c, i) => <li key={i}>{c}</li>)}
            </ul>
          )}
        </section>

        <section className="card space-y-3">
          <p className="text-xs text-slatem">{t('nothingFiled')}</p>
          {inc.status === 'REPORTED' ? (
            <p className="text-sm font-semibold text-ink">Done — {t('reported')}</p>
          ) : (
            <>
              <label className="flex items-start gap-2 text-sm text-slatem">
                <input type="checkbox" className="mt-1" checked={share} onChange={(e) => setShare(e.target.checked)} />
                {t('shareIntel')}
              </label>
              <button className="btn-primary" onClick={markReported}>{t('markReported')}</button>
            </>
          )}
          {error && <p role="alert" className="text-sm text-ink">{error}</p>}
          <button className="btn-ghost text-xs" onClick={() => { setInc(null); setNarrative(''); setImages([]); }}>＋</button>
        </section>
      </div>
    );
  }

  return (
    <form onSubmit={submit} className="card space-y-4">
      <div>
        <h2 className="font-sans text-[24px] font-semibold leading-[1.3]">{t('goldenTitle')}</h2>
        <p className="mt-1 text-sm text-slatem">{t('goldenHint')}</p>
      </div>
      <div>
        <label className="label" htmlFor="narr">{t('narrative')}</label>
        <textarea id="narr" className="field min-h-[140px]" placeholder={t('narrativePh')} value={narrative} maxLength={4000} onChange={(e) => setNarrative(e.target.value)} />
      </div>
      <div>
        <button type="button" className="btn-ghost" onClick={() => fileRef.current?.click()}>{t('addShots')}</button>
        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          multiple
          className="hidden"
          onChange={(e) => setImages(Array.from(e.target.files || []).slice(0, 4))}
        />
        {images.length > 0 && <p className="mt-2 text-xs text-slatem">{images.map((i) => i.name).join(', ')}</p>}
      </div>
      {error && <p role="alert" className="text-sm text-ink">{error}</p>}
      <button className="btn-primary" disabled={busy || (!narrative.trim() && images.length === 0)}>{busy ? t('preparing') : t('prepare')}</button>
    </form>
  );
}
