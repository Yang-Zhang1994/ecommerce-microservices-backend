'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { syncMemberSessionFromServer } from '@/lib/gulimallSession';

/**
 * Runs on every Next.js route: if the browser has an auth {@code SESSION} cookie but localStorage
 * was cleared or never filled, copies the member profile from {@code GET /api/auth/oauth/member/session}.
 */
export default function AuthSessionSync() {
  const pathname = usePathname();

  useEffect(() => {
    if (pathname === '/login' || pathname === '/register') {
      return;
    }
    void syncMemberSessionFromServer();
  }, [pathname]);
  return null;
}
