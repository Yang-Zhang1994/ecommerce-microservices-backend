'use client';

import { useCallback, useMemo, useState } from 'react';
import type { SkuItemSaleAttr } from '@/types/api';
import {
  initialSaleAttrSelection,
  itemDetailUrl,
  normalizeSaleAttrValues,
  resolveSkuIdFromSelection,
} from '@/lib/saleAttrNavigation';
import styles from './ItemSaleAttrPicker.module.css';

type Props = {
  skuId: number;
  saleAttrs: SkuItemSaleAttr[];
  /** classic = JD template (.box-attr-2 / .redborder); mall = compact Next layout */
  variant?: 'mall' | 'classic';
};

export function ItemSaleAttrPicker({ skuId, saleAttrs, variant = 'mall' }: Props) {
  const [selected, setSelected] = useState<Record<number, string>>(() =>
    initialSaleAttrSelection(saleAttrs, skuId),
  );

  const resolvedSkuId = useMemo(
    () => resolveSkuIdFromSelection(saleAttrs, selected),
    [saleAttrs, selected],
  );

  const incomplete = resolvedSkuId == null;

  const onPick = useCallback(
    (attrId: number, attrValue: string) => {
      const next = { ...selected, [attrId]: attrValue };
      setSelected(next);
      const target = resolveSkuIdFromSelection(saleAttrs, next);
      if (target != null && target !== skuId) {
        window.location.href = itemDetailUrl(target);
      }
    },
    [saleAttrs, selected, skuId],
  );

  if (!saleAttrs.length) return null;

  if (variant === 'classic') {
    return (
      <div data-testid="item-sale-attr-picker" data-dynamic-sale-attr="1">
        {saleAttrs.map((row) => {
          const attrId = row.attrId;
          const values = normalizeSaleAttrValues(row.attrValues, row.attrName);
          if (attrId == null || !values.length) return null;
          const current = selected[attrId];
          return (
            <div className="box-attr-2 clear" key={attrId}>
              <dl>
                <dt>{row.attrName || `属性${attrId}`}</dt>
                {values.map((v) => {
                  const isSelected = current === v.attrValue;
                  return (
                    <dd
                      key={`${attrId}-${v.attrValue}`}
                      className={isSelected ? 'redborder' : undefined}
                      data-sku-ids={v.skuIds.join(',')}
                    >
                      <a
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          onPick(attrId, v.attrValue);
                        }}
                      >
                        {v.attrValue}
                      </a>
                    </dd>
                  );
                })}
              </dl>
            </div>
          );
        })}
      </div>
    );
  }

  return (
    <div className={styles.wrap} data-testid="item-sale-attr-picker">
      {saleAttrs.map((row) => {
        const attrId = row.attrId;
        const values = normalizeSaleAttrValues(row.attrValues, row.attrName);
        if (attrId == null || !values.length) return null;
        const current = selected[attrId];
        return (
          <div className={styles.row} key={attrId}>
            <span className={styles.label}>{row.attrName || `Option ${attrId}`}</span>
            <ul className={styles.options}>
              {values.map((v) => {
                const isSelected = current === v.attrValue;
                return (
                  <li className={styles.option} key={`${attrId}-${v.attrValue}`}>
                    <button
                      type="button"
                      className={`${styles.optionBtn} ${isSelected ? styles.optionBtnSelected : ''}`}
                      onClick={() => onPick(attrId, v.attrValue)}
                      aria-pressed={isSelected}
                    >
                      {v.attrValue}
                    </button>
                  </li>
                );
              })}
            </ul>
          </div>
        );
      })}
      {incomplete && (
        <p className={styles.hint}>Select all options to switch to another SKU.</p>
      )}
    </div>
  );
}
