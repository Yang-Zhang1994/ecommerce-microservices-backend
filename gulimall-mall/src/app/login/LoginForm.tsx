'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { FormEvent, useEffect, useState } from 'react';
import {
  clearGulimallSession,
  clearLoginAutoRedirectGuard,
  markLoginAutoRedirect,
  saveGulimallSession,
  shouldBlockLoginAutoRedirect,
  syncMemberSessionFromServer,
} from '@/lib/gulimallSession';
import styles from '../auth/auth.module.css';

function safeRedirect(raw: string | null): string {
  try {
    const target = decodeURIComponent(raw || '/');
    if (!target || target[0] !== '/') return '/';
    if (target.startsWith('/login')) return '/';
    return target;
  } catch {
    return '/';
  }
}

function redirectFromParams(searchParams: URLSearchParams): string {
  return safeRedirect(searchParams.get('redirect') ?? searchParams.get('originUrl'));
}

export default function LoginForm() {
  const searchParams = useSearchParams();
  const redirectTo = redirectFromParams(searchParams);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const oauthError = searchParams.get('error');
    if (oauthError) {
      setError('Sign-in failed. Try again or use email/password below.');
    }
  }, [searchParams]);

  useEffect(() => {
    if (shouldBlockLoginAutoRedirect(redirectTo)) {
      clearGulimallSession();
      setError('Session expired or checkout requires sign-in. Please sign in again.');
      return;
    }
    void (async () => {
      const ok = await syncMemberSessionFromServer({ authSessionOnly: true });
      if (!ok) {
        clearGulimallSession();
        return;
      }
      markLoginAutoRedirect(redirectTo);
      window.location.replace(redirectTo);
    })();
  }, [redirectTo]);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (!username.trim() || !password) {
      setError('Please enter username and password');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username.trim(), password }),
      });
      const data = (await res.json()) as { code?: number; msg?: string; member?: unknown };
      if (res.ok && data.code === 0) {
        if (data.member && typeof data.member === 'object') {
          saveGulimallSession(data.member as Parameters<typeof saveGulimallSession>[0]);
        } else {
          await syncMemberSessionFromServer({ authSessionOnly: true });
        }
        clearLoginAutoRedirectGuard();
        markLoginAutoRedirect(redirectTo);
        window.location.assign(redirectTo);
        return;
      }
      setError(data.msg || 'Login failed');
    } catch {
      setError('Network error, please try again');
    } finally {
      setLoading(false);
    }
  };

  const googleHref = '/api/auth/oauth2/authorization/google';

  return (
    <div className={styles.card}>
      <h1 className={styles.title}>Sign in</h1>
      <p className={styles.subtitle}>Use your GrainMart account to checkout and view orders.</p>

      <form className={styles.form} onSubmit={onSubmit}>
        {error && <p className={styles.error}>{error}</p>}
        <label className={styles.label}>
          Email or username
          <input
            type="text"
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={loading}
          />
        </label>
        <label className={styles.label}>
          Password
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
          />
        </label>
        <button type="submit" className={styles.primaryBtn} disabled={loading}>
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <div className={styles.divider}>or</div>

      <a href={googleHref} className={styles.oauthBtn}>
        Continue with Google
      </a>

      <p className={styles.footer}>
        No account? <Link href="/register">Register</Link>
      </p>
    </div>
  );
}
