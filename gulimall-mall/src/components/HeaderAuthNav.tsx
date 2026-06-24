'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  clearGulimallSession,
  displayName,
  GULIMALL_SESSION_KEY,
  loadGulimallSession,
  logoutGulimallSession,
  syncMemberSessionFromServer,
  type GulimallSessionPayload,
} from '@/lib/gulimallSession';
import shellStyles from '@/components/layout/MallShell.module.css';

type Props = {
  variant?: 'top';
};

function isAuthEntryPath(pathname: string | null): boolean {
  return pathname === '/login' || pathname === '/register';
}

export default function HeaderAuthNav({ variant }: Props) {
  const pathname = usePathname();
  const onAuthEntry = isAuthEntryPath(pathname);
  const [session, setSession] = useState<GulimallSessionPayload | null>(null);
  /** Wait for server SESSION check before showing a cached nickname. */
  const [sessionReady, setSessionReady] = useState(false);

  const refresh = useCallback(() => {
    setSession(loadGulimallSession());
  }, []);

  useEffect(() => {
    setSessionReady(false);
    if (onAuthEntry) {
      clearGulimallSession();
      setSession(null);
      void syncMemberSessionFromServer({ authSessionOnly: true }).then((ok) => {
        if (ok) {
          refresh();
        }
        setSessionReady(true);
      });
      return;
    }
    void syncMemberSessionFromServer().then(() => {
      refresh();
      setSessionReady(true);
    });
  }, [onAuthEntry, pathname, refresh]);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === null || e.key === GULIMALL_SESSION_KEY) {
        refresh();
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, [refresh]);

  const signOut = async () => {
    await logoutGulimallSession();
    setSession(null);
    const current = `${window.location.pathname}${window.location.search}`;
    window.location.assign(`/login?redirect=${encodeURIComponent(current || '/')}`);
  };

  if (variant === 'top') {
    if (!sessionReady) {
      return (
        <>
          <Link href="/login">Sign in</Link>
          <Link href="/register">Register</Link>
        </>
      );
    }
    if (session?.member) {
      const name = displayName(session.member);
      return (
        <>
          <span title={session.loggedInAt ? `Signed in at ${new Date(session.loggedInAt).toLocaleString()}` : undefined}>
            Hi, {name}
          </span>
          <button type="button" className={shellStyles.signOutBtn} onClick={signOut}>
            Sign out
          </button>
        </>
      );
    }
    return (
      <>
        <Link href="/login">Sign in</Link>
        <Link href="/register">Register</Link>
      </>
    );
  }

  return null;
}
