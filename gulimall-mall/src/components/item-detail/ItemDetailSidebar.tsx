import Link from 'next/link';

/** 对应模板 .allLeft「看了又看」占位（数据可后续接推荐接口） */
export function ItemDetailSidebar() {
  return (
    <div className="allLeft">
      <div className="huoreyuyue">
        <h3>看了又看</h3>
      </div>
      <div className="dangeshopxingqing">
        <ul className="shopdange">
          <li>
            <Link href="/search?keyword=手机">更多商品请前往搜索</Link>
            <p style={{ marginTop: 8 }}>
              <strong style={{ color: 'red' }}>推荐位</strong>
            </p>
          </li>
        </ul>
      </div>
    </div>
  );
}
