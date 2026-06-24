'use client';

import Link from 'next/link';
import { useState } from 'react';
import { categorySearchHref } from '@/lib/categorySearch';
import type { CategoryEntity } from '@/types/api';
import styles from './CategorySidebar.module.css';

interface CategorySidebarProps {
  categories: CategoryEntity[];
  error?: string;
  onPanelOpenChange?: (open: boolean) => void;
}

export default function CategorySidebar({ categories, error, onPanelOpenChange }: CategorySidebarProps) {
  const [hoveredCat, setHoveredCat] = useState<CategoryEntity | null>(null);

  const setHovered = (cat: CategoryEntity | null) => {
    setHoveredCat(cat);
    onPanelOpenChange?.(!!cat);
  };

  return (
    <div className={styles.wrapper} onMouseLeave={() => setHovered(null)}>
      <ul className={styles.catalogLeft}>
        {error && <li className={styles.errorItem}>{error}</li>}
        {categories.map((cat) => (
          <li
            key={cat.catId}
            onMouseEnter={() => setHovered(cat)}
            className={hoveredCat?.catId === cat.catId ? styles.catalogLeftLiActive : undefined}
          >
            <Link href={categorySearchHref(cat)} className={styles.catalogLeftA}>
              <b>{cat.name}</b>
            </Link>
          </li>
        ))}
      </ul>

      {hoveredCat?.children && hoveredCat.children.length > 0 && (
        <div className={styles.panel}>
          {hoveredCat.children.map((level2) => (
            <div className={styles.panelRow} key={level2.catId}>
              <div className={styles.panelLevel2Col}>
                <Link href={categorySearchHref(level2)} className={styles.panelLevel2}>
                  {level2.name} &gt;
                </Link>
              </div>
              <div className={styles.panelLevel3Col}>
                {level2.children && level2.children.length > 0 ? (
                  <div className={styles.panelLevel3Wrap}>
                    {level2.children.map((level3) => (
                      <Link
                        key={level3.catId}
                        href={categorySearchHref(level3)}
                        className={styles.panelLevel3}
                      >
                        {level3.name}
                      </Link>
                    ))}
                  </div>
                ) : null}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
