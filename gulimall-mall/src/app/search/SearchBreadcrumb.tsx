import SearchLink from './SearchLink';
import type { SearchNavVo } from '@/types/api';
import { buildSearchPath, normalizeNavLink, type SearchQueryState } from '@/lib/searchQuery';
import styles from './page.module.css';

type Props = {
  query: SearchQueryState;
  navs: SearchNavVo[];
};

export default function SearchBreadcrumb({ query, navs }: Props) {
  const keyword = query.keyword.trim();
  if (!keyword && navs.length === 0) return null;

  return (
    <nav className={styles.breadcrumb} aria-label="Search filters">
      {keyword && (
        <>
          <SearchLink
            href={buildSearchPath({
              ...query,
              catalog3Ids: [],
              brandIds: [],
              attrs: [],
              pageNum: 1,
            })}
            className={styles.breadcrumbRoot}
          >
            {keyword}
          </SearchLink>
          {navs.length > 0 && <span className={styles.breadcrumbSep}>›</span>}
        </>
      )}
      {navs.map((nav, index) => (
        <span key={`${nav.navName}-${nav.navValue}-${index}`} className={styles.breadcrumbItem}>
          {nav.link ? (
            <SearchLink href={normalizeNavLink(nav.link)} className={styles.breadcrumbChip}>
              <span>
                {nav.navName}: {nav.navValue}
              </span>
              <span className={styles.breadcrumbClose} aria-hidden>
                ×
              </span>
            </SearchLink>
          ) : (
            <span className={styles.breadcrumbCurrent}>
              {nav.navName}: {nav.navValue}
            </span>
          )}
          {index < navs.length - 1 && <span className={styles.breadcrumbSep}>›</span>}
        </span>
      ))}
    </nav>
  );
}
