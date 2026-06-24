'use client';

import { useState } from 'react';
import type { SpuItemAttrGroup } from '@/types/api';

const TABS = [
  { id: 'spec', label: 'Specs & Gallery' },
  { id: 'service', label: '售后保障' },
] as const;

type Props = {
  skuId: number;
  groupAttrs: SpuItemAttrGroup[];
  productGallery: string[];
};

/** 对应模板 .shopjieshao + .huawei / .xuanxiangka 标签切换 */
export function ItemDetailTabsClient({ skuId, groupAttrs, productGallery }: Props) {
  const [active, setActive] = useState<(typeof TABS)[number]['id']>('spec');

  return (
    <div className="allquanbushop">
      <ul className="shopjieshao">
        {TABS.map((t) => (
          <li
            key={t.id}
            className={t.id === 'spec' ? 'baozhuang' : 'baozhang'}
            style={active === t.id ? { background: '#e4393c' } : undefined}
          >
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                setActive(t.id);
              }}
              style={{ color: active === t.id ? '#fff' : '#666' }}
            >
              {t.label}
            </a>
          </li>
        ))}
      </ul>
      <button type="button" className="Lijiyuyuessss">
        加入购物车
      </button>

      <div className="huawei">
        <ul className="xuanxiangka">
          <li className="jieshoa" id="li2" style={{ display: active === 'spec' ? 'block' : 'none' }}>
            <div className="guiGebox guiGebox1" style={{ padding: 16 }}>
              <div className="guiGe">
                <h3>基本信息</h3>
                <dl>
                  <dt>商品编号</dt>
                  <dd>{skuId}</dd>
                </dl>
              </div>
              {groupAttrs.length ? (
                groupAttrs.map((g) => (
                  <div className="guiGe" key={g.groupId}>
                    <h3>{g.groupName || `参数组${g.groupId}`}</h3>
                    {(g.attrs || []).length ? (
                      (g.attrs || []).map((a) => (
                        <dl key={`${g.groupId}-${a.attrId}`}>
                          <dt>{a.attrName || `属性${a.attrId}`}</dt>
                          <dd>{a.attrValue || '—'}</dd>
                        </dl>
                      ))
                    ) : (
                      <p style={{ color: '#666', fontSize: 12 }}>暂无参数</p>
                    )}
                  </div>
                ))
              ) : (
                <p style={{ color: '#666', fontSize: 12 }}>暂无规格参数，后续可在后台维护后展示。</p>
              )}
            </div>
            {productGallery.length > 0 ? (
              <div className="item-spu-gallery item-detail-tab-gallery">
                <p className="item-spu-gallery-title">Product gallery</p>
                <ul className="item-spu-gallery-list">
                  {productGallery.map((url) => (
                    <li key={url}>
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img src={url} alt="" loading="lazy" />
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </li>
          <li className="jieshoa" id="li3" style={{ display: active === 'service' ? 'block' : 'none' }}>
            <div style={{ padding: 16, lineHeight: 1.8, color: '#666' }}>
              <p>· 7 天无理由退货（依平台规则）。</p>
              <p>· 由 GrainMart 提供售后与物流跟踪。</p>
              <p>· 具体保修政策以商品类目与品牌为准。</p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  );
}
