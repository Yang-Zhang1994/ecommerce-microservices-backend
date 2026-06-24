'use client';

import { useMemo, useState } from 'react';

/** 对应模板 .box-btns 数量与主按钮 */
export function ItemDetailBuyBar({
  skuId,
  title,
  img,
}: {
  skuId: number;
  title?: string | null;
  img?: string | null;
}) {
  const [qty, setQty] = useState(1);
  const addToCartHref = useMemo(() => {
    const params = new URLSearchParams({
      skuId: String(skuId),
      num: String(qty),
    });
    if (title) params.set('title', title);
    if (img) params.set('img', img);
    return `/api/cart/add?${params.toString()}`;
  }, [img, qty, skuId, title]);

  return (
    <div className="box-btns clear">
      <div className="box-btns-one">
        <input
          type="text"
          readOnly
          value={qty}
          aria-label="购买数量"
        />
        <div className="box-btns-one1">
          <div>
            <button type="button" id="jia" onClick={() => setQty((q) => Math.min(99, q + 1))}>
              +
            </button>
          </div>
          <div>
            <button type="button" id="jian" onClick={() => setQty((q) => Math.max(1, q - 1))}>
              -
            </button>
          </div>
        </div>
      </div>
      <div className="box-btns-two">
        <a href={addToCartHref}>
          Add to Cart
        </a>
      </div>
      <div className="box-btns-three">分享</div>
    </div>
  );
}
