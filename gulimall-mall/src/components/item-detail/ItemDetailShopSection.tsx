import type { SkuItemDetail } from '@/types/api';
import { buildProductGalleryUrls, buildSkuThumbUrls, uniqueUrls } from '@/lib/productGallery';
import { ItemSaleAttrPicker } from './ItemSaleAttrPicker';
import { ItemDetailBuyBar } from './ItemDetailBuyBar';
import { ItemDetailFlowStrip } from './ItemDetailFlowStrip';
import { ItemGalleryClient } from './ItemGalleryClient';
import { ItemDetailSidebar } from './ItemDetailSidebar';
import { ItemDetailTabsClient } from './ItemDetailTabsClient';

type Props = {
  item: SkuItemDetail;
};

function formatPrice(p: number | undefined) {
  if (p == null || Number.isNaN(p)) return '—';
  return p.toFixed(2);
}

/**
 * 对应模板 .Shop 整块：主信息 + 购买流程条 + .ShopXiangqing（侧栏 + 详情 Tab）
 */
export function ItemDetailShopSection({ item }: Props) {
  const info = item.info!;
  const title = info.skuTitle || info.skuName || `SKU ${info.skuId}`;
  const hint = info.skuSubtitle || '';
  const skuImages = uniqueUrls((item.images || []).map((i) => i.imgUrl));
  const spuProductImages = uniqueUrls((item.spuImages || []).map((i) => i.imgUrl));
  const images = buildSkuThumbUrls(skuImages, info.skuDefaultImg);
  const productGallery = buildProductGalleryUrls(spuProductImages, item.desc?.decript);
  const saleAttrs = item.saleAttr || [];

  return (
    <div className="Shop">
      <div className="box">
        <div className="box-one">
          <ItemGalleryClient images={images} alt={title} />
          <div className="box-two">
            <div className="box-name">{title}</div>
            {hint ? <div className="box-hide">{hint}</div> : null}
            <div className="box-summary clear">
              <ul>
                <li>商城价</li>
                <li>
                  <span>￥</span>
                  <span>{formatPrice(info.price)}</span>
                </li>
                <li>正品保障</li>
                <li>
                  <a href="#">价格说明</a>
                </li>
              </ul>
            </div>
            <div className="box-stock">
              <ul className="box-ul">
                <li>配送至</li>
                <li className="box-stock-li">
                  <div className="box-stock-one">北京朝阳区</div>
                </li>
                <li>
                  <span>现货</span>，由仓库发货
                </li>
              </ul>
            </div>
            <div className="box-supply">
              <ul className="box-ul">
                <li />
                <li>
                  由 <span>GrainMart</span> 发货，并提供售后服务
                </li>
              </ul>
            </div>
            <div className="box-attr-3">
              {saleAttrs.length ? (
                <ItemSaleAttrPicker skuId={info.skuId} saleAttrs={saleAttrs} variant="classic" />
              ) : (
                <div className="box-attr clear">
                  <dl>
                    <dt>规格</dt>
                    <dd>
                      <a href="#">默认规格</a>
                    </dd>
                  </dl>
                </div>
              )}
            </div>
            <ItemDetailBuyBar
              skuId={info.skuId}
              title={title}
              img={images.length > 0 ? images[0] : null}
            />
            <div className="box-footer-zo">
              <div className="box-footer clear">
                <dl>
                  <dt>温馨提示</dt>
                  <dd>· 图片仅供参考，请以实物为准。</dd>
                  <dd>· 促销活动以结算页为准。</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <ItemDetailFlowStrip />

      <div className="ShopXiangqing">
        <ItemDetailSidebar />
        <ItemDetailTabsClient
          skuId={info.skuId}
          groupAttrs={item.groupAttrs || []}
          productGallery={productGallery}
        />
      </div>
    </div>
  );
}
