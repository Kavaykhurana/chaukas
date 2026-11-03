import { useEffect, useMemo, useRef, useState } from 'react';
import { api } from '../api.js';
import { SAMPLE_MESSAGE } from '../i18n.js';

const RISK = {
  HIGH: { ring: 'border-danger/60', bg: 'bg-danger/10', text: 'text-danger', dot: 'bg-danger' },
  MEDIUM: { ring: 'border-warn/60', bg: 'bg-warn/10', text: 'text-warn', dot: 'bg-warn' },
  LOW: { ring: 'border-calm/60', bg: 'bg-calm/10', text: 'text-calm', dot: 'bg-calm' },
};

function escapeRe(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/** Splits text into plain and highlighted parts so every flagged phrase is shown in context. */
function Highlighted({ text, phrases }) {
  const parts = useMemo(() => {
    const uniq = [...new Set(phrases.filter(Boolean))].sort((a, b) => b.length - a.length);
    if (!text || uniq.length === 0) return [{ s: text || '', hl: false }];
    const re = new RegExp(`(${uniq.map(escapeRe).join('|')})`, 'gi');
    return text.split(re).filter((s) => s !== '').map((s) => ({ s, hl: uniq.some((p) => p.toLowerCase() === s.toLowerCase()) }));
  }, [text, phrases]);
  return (
    <p className="whitespace-pre-wrap break-words text-sm leading-relaxed text-slate-200">
      {parts.map((p, i) => (p.hl ? <mark key={i} className="hl">{p.s}</mark> : <span key={i}>{p.s}</span>))}
    </p>
  );
}

export default function CheckTab({ t, lang, aiMode }) {
  const [text, setText] = useState('');
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);
  const [checkedText, setCheckedText] = useState('');
  const fileRef = useRef(null);
  const resultRef = useRef(null);

  useEffect(() => {
    if (!image) return setPreview(null);
    const url = URL.createObjectURL(image);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [image]);

  useEffect(() => {
    if (result && resultRef.current) resultRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }, [result]);

  async function submit(e) {
    e.preventDefault();
    if (!text.trim() && !image) return;
    setBusy(true);
    setError('');
    try {
      const res = await api.check({ text: text.trim(), image, lang });
      setCheckedText(text.trim());
      setResult(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const v = result?.verdict;
  const style = v ? RISK[v.risk] : null;
  const phrases = v ? [...v.signals.map((s) => s.phrase)] : [];

  return (
    <div className="space-y-5">
      <form onSubmit={submit} className="card space-y-3">
        <div>
          <h2 className="text-lg font-semibold">{t('checkTitle')}</h2>
          <p className="mt-1 text-sm text-slate-400">{t('checkHint')}</p>
        </div>
        <textarea
          className="field min-h-[130px] resize-y"
          placeholder={t('placeholder')}
          value={text}
          maxLength={6000}
          onChange={(e) => setText(e.target.value)}
          aria-label={t('checkTitle')}
        />
        {preview && (
          <div className="flex items-center gap-3 rounded-xl border border-edge p-2">
            <img src={preview} alt="screenshot preview" className="h-16 w-16 rounded-lg object-cover" />
            <span className="flex-1 truncate text-xs text-slate-400">{image.name}</span>
            <button type="button" className="btn-ghost !px-3 !py-1.5 text-xs" onClick={() => { setImage(null); if (fileRef.current) fileRef.current.value = ''; }}>
              {t('removeShot')}
            </button>
          </div>
        )}
        <div className="flex flex-wrap gap-2">
          <button className="btn-primary" disabled={busy || (!text.trim() && !image)}>{busy ? t('checking') : t('checkBtn')}</button>
          <button type="button" className="btn-ghost" onClick={() => fileRef.current?.click()}>{t('addShot')}</button>
          <button type="button" className="btn-ghost" onClick={() => setText(SAMPLE_MESSAGE)}>{t('sampleBtn')}</button>
          <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={(e) => setImage(e.target.files?.[0] || null)} />
        </div>
        {error && <p role="alert" className="text-sm text-danger">{error}</p>}
      </form>

      {v && (
        <section ref={resultRef} aria-live="polite" className={`card border-2 ${style.ring} ${style.bg}`}>
          <div className="flex items-start gap-3">
            <span className={`mt-1.5 h-3 w-3 shrink-0 rounded-full ${style.dot}`} />
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <span className={`text-xs font-bold uppercase tracking-wider ${style.text}`}>{v.risk}</span>
                {v.scamType !== 'NONE' && <span className="rounded-full border border-edge px-2 py-0.5 text-xs text-slate-300">{v.scamLabel}</span>}
                <span className="ml-auto font-mono text-xs text-slate-500">{v.score}/100</span>
              </div>
              <h3 className="mt-1 text-lg font-semibold leading-snug">{v.headline}</h3>
              <p className="mt-2 text-sm text-slate-200">{v.advice}</p>
            </div>
          </div>

          {v.seenBefore.length > 0 && (
            <div className="mt-4 rounded-xl border border-danger/40 bg-danger/10 p-3">
              <p className="text-xs font-bold uppercase tracking-wide text-danger">{t('seenBefore')}</p>
              <ul className="mt-1 space-y-1 text-sm">
                {v.seenBefore.map((h) => (
                  <li key={h.kind + h.value}>
                    <span className="font-mono text-slate-100">{h.value}</span>{' '}
                    <span className="text-slate-400">· {h.reports} {t('reportsWord')}{h.confirmed ? ` · ${t('confirmed')}` : ''}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {v.signals.length > 0 && (
            <div className="mt-4">
              <p className="label">{t('why')}</p>
              {checkedText && <div className="mb-3 rounded-xl border border-edge bg-ink/60 p-3"><Highlighted text={checkedText} phrases={phrases} /></div>}
              <ul className="space-y-2">
                {v.signals.map((s, i) => (
                  <li key={s.id + i} className="flex gap-2 text-sm">
                    <mark className="hl h-fit shrink-0 break-all font-mono text-xs">{s.phrase}</mark>
                    <span className="text-slate-300">{s.reason}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="mt-4 border-t border-edge pt-3 text-xs text-slate-400">
            {v.aiUsed ? (
              <p><span className="font-semibold text-slate-300">{t('aiNote')}:</span> {v.aiExplanation || '—'}{v.aiConfidence != null && ` (${Math.round(v.aiConfidence * 100)}%)`}</p>
            ) : (
              <p>{t('rulesOnly')}</p>
            )}
            <p className="mt-1">{t('noSafe')}</p>
          </div>
        </section>
      )}
    </div>
  );
}
