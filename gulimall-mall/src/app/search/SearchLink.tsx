import Link from 'next/link';
import type { ComponentProps } from 'react';

type Props = ComponentProps<typeof Link>;

/** In-page search navigation: keep scroll position (sort, filters, pager, etc.). */
export default function SearchLink({ scroll = false, ...props }: Props) {
  return <Link scroll={scroll} {...props} />;
}
