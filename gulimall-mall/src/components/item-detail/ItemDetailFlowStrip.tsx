/** 对应模板 .qianggoulioucheng；图片由 /static/item-detail/img/ Nginx 直出 */
const IMG = '/static/item-detail/img';

const STEPS = [
  { src: `${IMG}/shop_03.png`, title: '1.等待预约', sub: '预约即将开始' },
  { src: `${IMG}/shop_04.png`, title: '2.预约中', sub: '请关注活动时段' },
  { src: `${IMG}/shop_05.png`, title: '3.等待抢购', sub: '抢购即将开始' },
  { src: `${IMG}/shop_06.png`, title: '4.抢购中', sub: '立即下单' },
];

export function ItemDetailFlowStrip() {
  return (
    <div className="qianggoulioucheng">
      <div className="lioucheng">
        <h3>预约抢购流程</h3>
      </div>
      <ul className="qianggoubuzhao">
        {STEPS.map((s) => (
          <li key={s.title}>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={s.src} alt="" />
            <dl className="buzhou">
              <dt>{s.title}</dt>
              <dd style={{ margin: 0, padding: 0 }}>{s.sub}</dd>
            </dl>
          </li>
        ))}
      </ul>
    </div>
  );
}
