'use client';

import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import styles from './MallShell.module.css';

export default function HeaderSearchForm() {
  const searchParams = useSearchParams();
  const urlKeyword = searchParams.get('keyword') ?? '';
  const [keyword, setKeyword] = useState(urlKeyword);

  useEffect(() => {
    setKeyword(urlKeyword);
  }, [urlKeyword]);

  return (
    <form className={styles.searchForm} action="/search" method="get">
      <input
        className={styles.searchInput}
        type="search"
        name="keyword"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder="Search products…"
        aria-label="Search"
      />
      <button type="submit" className={styles.searchBtn}>
        Search
      </button>
    </form>
  );
}
