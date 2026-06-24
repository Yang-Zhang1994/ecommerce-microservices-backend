'use client';

import CategorySidebar from '@/components/CategorySidebar';
import type { CategoryEntity } from '@/types/api';
import styles from '@/app/page.module.css';

type Props = {
  categories: CategoryEntity[];
  categoryError?: string;
  children: React.ReactNode;
  /** Full-width block rendered below the category + featured row (e.g. flash sale). */
  bottom?: React.ReactNode;
};

export default function HomeMainLayout({ categories, categoryError, children, bottom }: Props) {
  return (
    <>
      <div className={styles.mainRow}>
        <aside className={styles.categoryAside} aria-label="Product categories">
          <CategorySidebar categories={categories} error={categoryError} />
        </aside>
        <div className={styles.featuredMain}>{children}</div>
      </div>
      {bottom ? <div className={styles.bottomSection}>{bottom}</div> : null}
    </>
  );
}
