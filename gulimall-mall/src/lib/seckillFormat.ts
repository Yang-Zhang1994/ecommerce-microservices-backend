export function pad(n: number): string {
  return String(n).padStart(2, '0');
}

export function formatPrice(v: number | undefined): string {
  if (v == null || Number.isNaN(v)) return '—';
  return `$${Number(v).toFixed(2)}`;
}

export function discountPercent(seckill?: number, original?: number): number | null {
  if (seckill == null || original == null || original <= 0 || seckill >= original) {
    return null;
  }
  return Math.round((1 - seckill / original) * 100);
}

export function formatSeckillStart(ms: number): string {
  return new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(ms));
}

export function splitCountdown(ms: number): { hours: number; minutes: number; seconds: number } {
  const diff = Math.max(0, ms);
  return {
    hours: Math.floor(diff / 3600000),
    minutes: Math.floor((diff % 3600000) / 60000),
    seconds: Math.floor((diff % 60000) / 1000),
  };
}
