import { useCallback, useEffect, useMemo, useState } from 'react';
import { api, hasToken, setToken } from './api.js';
import { LANGS, makeT } from './i18n.js';
import AuthPanel from './components/AuthPanel.jsx';
import CheckTab from './components/CheckTab.jsx';
import GoldenTab from './components/GoldenTab.jsx';
import FamilyTab from './components/FamilyTab.jsx';

const TABS = ['check', 'golden', 'family'];

function loadLang() {
  try {
    return localStorage.getItem('chaukas.lang') || 'en';
  } catch {
    return 'en';
  }
}

export default function App() {
  const [lang, setLang] = useState(loadLang);
  const [tab, setTab] = useState('check');
  const [user, setUser] = useState(null);
  const [booting, setBooting] = useState(hasToken());
  const [ai, setAi] = useState(null);
  const [unread, setUnread] = useState(0);
  const t = useMemo(() => makeT(lang), [lang]);

  useEffect(() => {
    document.documentElement.lang = lang;
    try {
      localStorage.setItem('chaukas.lang', lang);
    } catch {
      /* ignore */
    }
  }, [lang]);

  useEffect(() => {
    api.health().then((h) => setAi(h.ai)).catch(() => setAi(null));
    if (!hasToken()) return;
    api.me().then(setUser).catch(() => setToken(null)).finally(() => setBooting(false));
  }, []);

  const refreshUnread = useCallback(() => {
    if (!hasToken()) return setUnread(0);
    api.unread().then((r) => setUnread(r.unread)).catch(() => {});
  }, []);

  useEffect(() => {
    refreshUnread();
    if (!user) return undefined;
    const id = setInterval(refreshUnread, 30000);
    return () => clearInterval(id);
  }, [user, refreshUnread]);

  function onAuth(res) {
    setToken(res.token);
    setUser(res.user);
    if (res.user.lang) setLang(res.user.lang);
  }

  function signOut() {
    setToken(null);
    setUser(null);
    setUnread(0);
  }

  function changeLang(code) {
    setLang(code);
    if (user) api.setLang(code).catch(() => {});
  }

  return (
    <div className="mx-auto max-w-3xl px-4 pb-24 pt-6 sm:pt-10">
      <header className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <img src="/favicon.svg" alt="" className="h-10 w-10" />
          <div>
            <h1 className="text-xl font-bold tracking-tight">Chaukas <span className="text-saffron">चौकस</span></h1>
            <p className="text-xs text-slate-400">{t('tagline')}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div role="group" aria-label="Language" className="flex overflow-hidden rounded-xl border border-edge text-xs">
            {LANGS.map((l) => (
              <button
                key={l.code}
                onClick={() => changeLang(l.code)}
                aria-pressed={lang === l.code}
                className={`px-3 py-2 transition ${lang === l.code ? 'bg-saffron font-semibold text-ink' : 'text-slate-300 hover:bg-white/5'}`}
              >
                {l.label}
              </button>
            ))}
          </div>
          {user && (
            <button className="btn-ghost !px-3 !py-2 text-xs" onClick={signOut} title={user.email}>
              {t('signOut')}
            </button>
          )}
        </div>
      </header>

      <nav className="mb-5 grid grid-cols-3 gap-2" role="tablist">
        {TABS.map((id) => (
          <button
            key={id}
            role="tab"
            aria-selected={tab === id}
            onClick={() => setTab(id)}
            className={`relative rounded-xl border px-3 py-2.5 text-sm font-semibold transition ${
              tab === id ? 'border-saffron bg-saffron/10 text-saffron' : 'border-edge text-slate-300 hover:bg-white/5'
            }`}
          >
            {t(id === 'check' ? 'tabCheck' : id === 'golden' ? 'tabGolden' : 'tabFamily')}
            {id === 'family' && unread > 0 && (
              <span className="absolute -right-1 -top-1 rounded-full bg-danger px-1.5 text-[10px] font-bold text-white">{unread}</span>
            )}
          </button>
        ))}
      </nav>

      <main>
        {tab === 'check' && <CheckTab t={t} lang={lang} aiMode={ai} />}
        {tab !== 'check' && booting && <p className="text-slate-400">{t('loading')}</p>}
        {tab !== 'check' && !booting && !user && <AuthPanel t={t} lang={lang} onAuth={onAuth} note={tab === 'golden' ? t('goldenNeedLogin') : null} />}
        {tab === 'golden' && user && <GoldenTab t={t} lang={lang} />}
        {tab === 'family' && user && <FamilyTab t={t} onRead={refreshUnread} />}
      </main>

      <footer className="mt-10 text-center text-xs text-slate-500">
        {t('noSafe')} · cybercrime helpline <span className="font-mono text-slate-300">1930</span>
      </footer>
    </div>
  );
}
