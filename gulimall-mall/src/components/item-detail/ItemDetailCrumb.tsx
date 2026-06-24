import Link from 'next/link';

type Props = {
  title: string;
  /** 中间级面包屑，无分类数据时可省略 */
  segments?: string[];
};

/** 对应模板 .crumb-wrap / .crumb（简化无下拉） */
export function ItemDetailCrumb({ title, segments = [] }: Props) {
  return (
    <div className="crumb-wrap">
      <div className="w">
        <div className="crumb">
          <div className="crumb-item">
            <Link href="/">首页</Link>
          </div>
          {segments.map((s) => (
            <span key={s} style={{ display: 'contents' }}>
              <div className="crumb-item sep">&gt;</div>
              <div className="crumb-item">
                <span>{s}</span>
              </div>
            </span>
          ))}
          <div className="crumb-item sep">&gt;</div>
          <div className="crumb-item">{title}</div>
        </div>
        <div className="contact">
          <ul className="contact-ul">
            <li>
              <Link href="#">GrainMart 自营旗舰店</Link>
              <span className="contact-sp">
                <span className="contact-sp1">自营</span>
                <span className="contact-sp2">正品保障</span>
              </span>
            </li>
            <li>
              <Link href="#">联系客服</Link>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
