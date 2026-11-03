import { useEffect, useMemo, useRef, useState } from 'react';
import { api } from '../api.js';
import { SAMPLE_MESSAGE } from '../i18n.js';

const RISK = {
  HIGH: { box: 'border-ink bg-manilla', rule: 'border-l-4 border-ink', label: 'High risk' },
  MEDIUM: { box: 'border-stone bg-oat', rule: 'border-l-2 border-ink', label: 'Medium risk' },
  LOW: { box: 'border-stone bg-paper', rule: 'border-l-2 border-cloud', label: 'Low risk: no known red flags' },
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
    <p className="whitespace-pre-wrap break-words text-sm leading-relaxed text-ink">
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
          <h2 className="font-sans text-[24px] font-semibold leading-[1.3]">{t('checkTitle')}</h2>
          <p className="mt-2 text-[18px] text-slatem">{t('checkHint')}</p>
        </div>
        <textarea
          className="field min-h-[140px] resize-y !font-serif !text-[18px]"
          placeholder={t('placeholder')}
          value={text}
          maxLength={6000}
          onChange={(e) => setText(e.target.value)}
          aria-label={t('checkTitle')}
        />
        {preview && (
          <div className="flex items-center gap-3 rounded-xl border border-stone p-2">
            <img src={preview} alt="screenshot preview" className="h-16 w-16 rounded-lg object-cover" />
            <span className="flex-1 truncate text-xs text-slatem">{image.name}</span>
            <button type="button" className="btn-ghost !px-3 !py-1.5 !text-xs" onClick={() => { setImage(null); if (fileRef.current) fileRef.current.value = ''; }}>
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
        {error && <p role="alert" className="text-sm text-ink">{error}</p>}
      </form>

      {v && (
        <section ref={resultRef} aria-live="polite" className={`rounded-[24px] border p-6 sm:p-8 ${style.box}`}>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 font-sans text-xs font-semibold uppercase tracking-[0.06em]">
            <span>{style.label}</span>
            {v.scamType !== 'NONE' && <span>· {v.scamLabel}</span>}
            <span className="ml-auto font-mono font-normal normal-case text-slatem">{v.score}/100</span>
          </div>
          <h3 className="mt-3 font-serif text-[24px] font-normal leading-[1.3] tracking-[-0.002em]">{v.headline}</h3>
          <p className="mt-3 text-[18px] text-slatem">{v.advice}</p>

          {v.seenBefore.length > 0 && (
            <div className="note mt-6">
              <p className="eyebrow !text-ink">Warning — {t('seenBefore')}</p>
              <ul className="mt-1 space-y-1 text-sm">
                {v.seenBefore.map((h) => (
                  <li key={h.kind + h.value}>
                    <span className="font-mono text-ink">{h.value}</span>{' '}
                    <span className="text-slatem">· {h.reports} {t('reportsWord')}{h.confirmed ? ` · ${t('confirmed')}` : ''}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {v.signals.length > 0 && (
            <div className="mt-4">
              <p className="label">{t('why')}</p>
              {checkedText && <div className="mb-3 rounded-xl border border-stone bg-paper p-3"><Highlighted text={checkedText} phrases={phrases} /></div>}
              <ul className="space-y-2">
                {v.signals.map((s, i) => (
                  <li key={s.id + i} className="flex gap-2 text-sm">
                    <mark className="hl h-fit shrink-0 break-all font-mono text-xs">{s.phrase}</mark>
                    <span className="text-slatem">{s.reason}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="mt-6 border-t border-stone pt-4 text-[16px] text-slatem">
            {v.aiUsed ? (
              <p><span className="font-sans text-xs font-semibold uppercase tracking-[0.06em] text-ink">{t('aiNote')}</span> — {v.aiExplanation || '—'}{v.aiConfidence != null && ` (${Math.round(v.aiConfidence * 100)}%)`}</p>
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
