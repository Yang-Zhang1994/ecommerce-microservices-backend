/** Mirrors public/auth/js/session.js — same localStorage key and shape. */

import type { OrderListOrder, R } from '@/types/api';

export const GULIMALL_SESSION_KEY = 'gulimall_session';

/**
 * Stripe appends {@code session_id} to the success URL; order service verifies it server-side and
 * returns member fields without requiring {@code /order/list} (which returns 401 until auth sync works).
 */
async function syncMemberFromPostPayStripe(): Promise<boolean> {
  if (typeof window === 'undefined') {
    return false;
  }
  if (!window.location.pathname.endsWith('/order/success')) {
    return false;
  }
  const q = new URLSearchParams(window.location.search);
  const orderSn = q.get('orderSn')?.trim();
  const sessionId = q.get('session_id')?.trim();
  if (!orderSn || !sessionId) {
    return false;
  }
  try {
    const u = new URL('/api/order/post-pay/member', window.location.origin);
    u.searchParams.set('orderSn', orderSn);
    u.searchParams.set('session_id', sessionId);
    const res = await fetch(u.toString(), {
      credentials: 'include',
      mode: 'cors',
      cache: 'no-store',
    });
    const body = (await res.json()) as {
      code?: number;
      member?: GulimallMember | null;
    };
    if (!res.ok || Number(body.code) !== 0 || !body.member) {
      return false;
    }
    saveGulimallSession(body.member);
    return true;
  } catch {
    return false;
  }
}

/**
 * When auth session JSON has no {@code member} but order APIs still see the same SESSION (e.g. after
 * Stripe return), derive minimal profile from the first order row so the header matches checkout state.
 */
async function syncMemberFromOrderListFallback(): Promise<boolean> {
  try {
    const res = await fetch('/api/order/list?page=1&limit=1', {
      credentials: 'include',
      mode: 'cors',
      cache: 'no-store',
    });
    const body = (await res.json()) as R<OrderListOrder[]>;
    if (!res.ok || Number(body.code) !== 0) {
      return false;
    }
    const rows = Array.isArray(body.data) ? body.data : [];
    const o = rows[0];
    if (!o) {
      return false;
    }
    const id = o.memberId;
    const username =
      typeof o.memberUsername === 'string' ? o.memberUsername.trim() : '';
    if ((id == null || Number.isNaN(Number(id))) && !username) {
      return false;
    }
    saveGulimallSession({
      id: id != null ? Number(id) : undefined,
      username: username || undefined,
      nickname: username || undefined,
    });
    return true;
  } catch {
    return false;
  }
}

/**
 * URL for {@code GET /api/auth/oauth/member/session} after Google OAuth.
 * {@code JSESSIONID} must match the host where OAuth ran.
 * Dev: gateway on {@code localhost|127.0.0.1:88}.
 * Prod ({@code www.example.com}): prefer same-origin {@code /api/auth/...}; ignore baked-in
 * {@code NEXT_PUBLIC_API_BASE=http://localhost:88} when the page is on a real host.
 */
export function oauthMemberSessionFetchUrl(): string {
  if (typeof window !== 'undefined') {
    const { hostname } = window.location;
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      // Same-origin via next.config rewrites → gateway; avoids cross-origin SESSION quirks after
      // payment redirects and keeps cookies aligned with the page host (localhost vs 127.0.0.1).
      return '/api/auth/oauth/member/session';
    }
    if (hostname.endsWith('ecommerce.com')) {
      return '/api/auth/oauth/member/session';
    }
    const base = process.env.NEXT_PUBLIC_API_BASE?.trim();
    if (
      base &&
      !base.includes('localhost') &&
      !base.includes('127.0.0.1')
    ) {
      return `${base.replace(/\/$/, '')}/api/auth/oauth/member/session`;
    }
    return '/api/auth/oauth/member/session';
  }
  const base = process.env.NEXT_PUBLIC_API_BASE?.trim();
  if (base) {
    return `${base.replace(/\/$/, '')}/api/auth/oauth/member/session`;
  }
  return '/api/auth/oauth/member/session';
}

export function authLogoutFetchUrl(): string {
  if (typeof window !== 'undefined') {
    const { hostname } = window.location;
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return '/api/auth/logout';
    }
    const base = process.env.NEXT_PUBLIC_API_BASE?.trim();
    if (
      base &&
      !base.includes('localhost') &&
      !base.includes('127.0.0.1')
    ) {
      return `${base.replace(/\/$/, '')}/api/auth/logout`;
    }
    return '/api/auth/logout';
  }
  const base = process.env.NEXT_PUBLIC_API_BASE?.trim();
  if (base) {
    return `${base.replace(/\/$/, '')}/api/auth/logout`;
  }
  return '/api/auth/logout';
}

export type GulimallMember = {
  id?: number;
  username?: string;
  nickname?: string;
  mobile?: string;
  email?: string;
  [key: string]: unknown;
};

export type GulimallSessionPayload = {
  member: GulimallMember | null;
  loggedInAt: number;
};

export function saveGulimallSession(member: GulimallMember | null): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    const payload: GulimallSessionPayload = { member, loggedInAt: Date.now() };
    window.localStorage.setItem(GULIMALL_SESSION_KEY, JSON.stringify(payload));
  } catch {
    /* private mode / quota */
  }
}

export function loadGulimallSession(): GulimallSessionPayload | null {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    const raw = window.localStorage.getItem(GULIMALL_SESSION_KEY);
    if (!raw) {
      return null;
    }
    return JSON.parse(raw) as GulimallSessionPayload;
  } catch {
    return null;
  }
}

export function clearGulimallSession(): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    window.localStorage.removeItem(GULIMALL_SESSION_KEY);
    window.sessionStorage.removeItem(GULIMALL_SESSION_KEY);
  } catch {
    /* ignore */
  }
}

let syncMemberSessionPromise: Promise<boolean> | null = null;

export type SyncMemberSessionOptions = {
  /**
   * When true (login auto-redirect), only trust {@code GET /api/auth/oauth/member/session}
   * with a non-empty {@code member}. Skips order-list fallback so stale localStorage + weak
   * cookies do not bounce login ↔ checkout forever.
   */
  authSessionOnly?: boolean;
};

/**
 * Copies server-side auth session member into {@link GULIMALL_SESSION_KEY} when the browser has a
 * valid {@code SESSION} cookie (password login / OAuth). Idempotent for concurrent callers.
 */
export function syncMemberSessionFromServer(
  options?: SyncMemberSessionOptions,
): Promise<boolean> {
  if (typeof window === 'undefined') {
    return Promise.resolve(false);
  }
  if (syncMemberSessionPromise) {
    return syncMemberSessionPromise;
  }
  syncMemberSessionPromise = (async (): Promise<boolean> => {
    const attempt = async (): Promise<boolean> => {
      const res = await fetch(oauthMemberSessionFetchUrl(), {
        credentials: 'include',
        mode: 'cors',
        cache: 'no-store',
      });
      let data: { code?: number; member?: GulimallMember | null } = {};
      try {
        data = (await res.json()) as typeof data;
      } catch {
        return false;
      }
      if (!res.ok) {
        if (res.status === 401 || res.status === 403) {
          clearGulimallSession();
        }
        return false;
      }
      const code = data.code;
      const ok = code === undefined || code === 0;
      if (!ok) {
        clearGulimallSession();
        return false;
      }
      if (data.member) {
        saveGulimallSession(data.member);
        return true;
      }
      return false;
    };

    try {
      if (!options?.authSessionOnly && (await syncMemberFromPostPayStripe())) {
        return true;
      }
      if (await attempt()) {
        return true;
      }
      // One retry after external redirects (Stripe): cookie/session attachment can lag one tick.
      await new Promise((r) => setTimeout(r, 150));
      if (await attempt()) {
        return true;
      }
      await new Promise((r) => setTimeout(r, 400));
      if (await attempt()) {
        return true;
      }
      if (!options?.authSessionOnly) {
        if (await syncMemberFromPostPayStripe()) {
          return true;
        }
        if (await syncMemberFromOrderListFallback()) {
          return true;
        }
      }
      clearGulimallSession();
      return false;
    } catch {
      clearGulimallSession();
      return false;
    } finally {
      syncMemberSessionPromise = null;
    }
  })();
  return syncMemberSessionPromise;
}

const LOGIN_REDIRECT_GUARD_KEY = 'gulimall_login_redirect_guard';

/** Break login ↔ protected-page loops when local mirror exists but SSR still rejects the user. */
export function shouldBlockLoginAutoRedirect(redirectTo: string): boolean {
  if (typeof window === 'undefined') {
    return false;
  }
  try {
    const raw = sessionStorage.getItem(LOGIN_REDIRECT_GUARD_KEY);
    if (!raw) {
      return false;
    }
    const parsed = JSON.parse(raw) as { target?: string; at?: number };
    return (
      parsed.target === redirectTo &&
      typeof parsed.at === 'number' &&
      Date.now() - parsed.at < 8000
    );
  } catch {
    return false;
  }
}

export function markLoginAutoRedirect(redirectTo: string): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    sessionStorage.setItem(
      LOGIN_REDIRECT_GUARD_KEY,
      JSON.stringify({ target: redirectTo, at: Date.now() }),
    );
  } catch {
    /* ignore */
  }
}

export function clearLoginAutoRedirectGuard(): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    sessionStorage.removeItem(LOGIN_REDIRECT_GUARD_KEY);
  } catch {
    /* ignore */
  }
}

export function displayName(member: GulimallMember | null | undefined): string {
  if (!member) {
    return '';
  }
  const n = member.nickname;
  if (typeof n === 'string' && n.trim()) {
    return n.trim();
  }
  const u = member.username;
  if (typeof u === 'string' && u.trim()) {
    return u.trim();
  }
  return 'User';
}

/**
 * Logs out server-side auth session (HttpSession) and clears local session mirror.
 */
export async function logoutGulimallSession(): Promise<void> {
  if (typeof window !== 'undefined') {
    try {
      await fetch(authLogoutFetchUrl(), {
        method: 'POST',
        credentials: 'include',
        mode: 'cors',
        cache: 'no-store',
      });
    } catch {
      // still clear local session even if network fails
    }
  }
  clearGulimallSession();
}
