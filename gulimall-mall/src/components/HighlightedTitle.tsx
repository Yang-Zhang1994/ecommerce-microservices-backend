import {
  hasSearchHighlight,
  plainTitle,
  sanitizeSearchHighlight,
} from '@/lib/searchHighlight';
import styles from './HighlightedTitle.module.css';

type Props = {
  html?: string | null;
  className?: string;
  fallback?: string;
};

export default function HighlightedTitle({ html, className, fallback = 'Product' }: Props) {
  const raw = html?.trim() || '';
  if (!raw) {
    return <span className={className}>{fallback}</span>;
  }
  if (!hasSearchHighlight(raw)) {
    return <span className={className}>{raw}</span>;
  }
  return (
    <span
      className={`${styles.highlight} ${className ?? ''}`.trim()}
      dangerouslySetInnerHTML={{ __html: sanitizeSearchHighlight(raw) }}
    />
  );
}

export { plainTitle };
