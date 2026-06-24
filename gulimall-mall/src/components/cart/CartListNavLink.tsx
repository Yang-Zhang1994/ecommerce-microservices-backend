'use client';

import type { AnchorHTMLAttributes, ReactNode } from 'react';

type Props = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'href'> & {
  href?: string;
  children: ReactNode;
};

/**
 * Plain anchor to /cart/list (full document navigation). Avoids Next {@link Link} client cache
 * that can show a stale empty cart. Cart data is loaded client-side in {@link CartListContent}.
 */
export default function CartListNavLink({ href = '/cart/list', children, ...rest }: Props) {
  return (
    <a href={href} {...rest}>
      {children}
    </a>
  );
}
