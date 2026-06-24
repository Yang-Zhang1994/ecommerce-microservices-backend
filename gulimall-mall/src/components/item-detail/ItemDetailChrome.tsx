import Link from 'next/link';

/** 对应模板 #max 内 header + nav（精简文案，结构 class 与 header.css 一致） */
export function ItemDetailChrome() {
  return (
    <div id="max">
      <header>
        <div className="min">
          <ul className="header_ul_left">
            <li>
              <Link href="/" className="aa">
                商城首页
              </Link>
            </li>
            <li>
              <Link href="#">北京</Link>
            </li>
          </ul>
          <ul className="header_ul_right">
            <li style={{ border: 0 }}>
              <Link href="#" className="aa">
                你好，请登录
              </Link>
            </li>
            <li>
              <Link href="#" style={{ color: 'red' }}>
                免费注册
              </Link>{' '}
              |
            </li>
            <li>
              <Link href="#" className="aa">
                我的订单
              </Link>{' '}
              |
            </li>
          </ul>
        </div>
      </header>
      <nav>
        <div className="nav_min">
          <div className="nav_top">
            <div className="nav_top_one">
              <Link href="/">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src="/static/item-detail/image/logo1.jpg" alt="GrainMart" width={120} height={40} />
              </Link>
            </div>
            <form className="nav_top_two" action="/search" method="get">
              <input type="text" name="keyword" placeholder="搜索商品" aria-label="搜索关键词" />
              <button type="submit">搜索</button>
            </form>
            <div className="nav_top_three">
              <Link href="/cart/list">My Cart</Link>
              <span style={{ marginLeft: 6 }}>🛒</span>
            </div>
          </div>
          <div className="nav_down">
            <ul className="nav_down_ul">
              <li className="nav_down_ul_1" style={{ width: '24%', float: 'left' }}>
                <Link href="#">全部商品分类</Link>
              </li>
              <li className="ul_li">
                <Link href="#">服装城</Link>
              </li>
              <li className="ul_li">
                <Link href="#">美妆馆</Link>
              </li>
              <li className="ul_li">
                <Link href="#">超市</Link>
              </li>
              <li className="ul_li" style={{ borderRight: '1px solid lavender' }}>
                <Link href="#">生鲜</Link>
              </li>
              <li className="ul_li">
                <Link href="#">全球购</Link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </div>
  );
}
