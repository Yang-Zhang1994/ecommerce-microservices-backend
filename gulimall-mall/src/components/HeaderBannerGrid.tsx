'use client';

import { useState } from 'react';
import type { CategoryEntity } from '@/types/api';
import CategorySidebar from '@/components/CategorySidebar';

interface HeaderBannerGridProps {
  categories: CategoryEntity[];
  error?: string;
  centerContent: React.ReactNode;
  rightContent: React.ReactNode;
  classNames: {
    headerMainLeft: string;
    headerMainLeftPanelOpen: string;
    headerMainCenter: string;
    headerMainRight: string;
  };
}

export default function HeaderBannerGrid({
  categories,
  error,
  centerContent,
  rightContent,
  classNames: cn,
}: HeaderBannerGridProps) {
  const [panelOpen, setPanelOpen] = useState(false);

  return (
    <>
      <div
        className={`${cn.headerMainLeft} ${panelOpen ? cn.headerMainLeftPanelOpen : ''}`}
      >
        <CategorySidebar
          categories={categories}
          error={error}
          onPanelOpenChange={setPanelOpen}
        />
      </div>
      <div className={cn.headerMainCenter}>{centerContent}</div>
      <div className={cn.headerMainRight}>{rightContent}</div>
    </>
  );
}
