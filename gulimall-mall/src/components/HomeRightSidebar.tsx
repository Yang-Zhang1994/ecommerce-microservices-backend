import styles from './HomeRightSidebar.module.css';

const STATIC = '/static/index/img';

const SERVICE_ITEMS = [
  { img: 'huafei.png', label: 'Recharge' },
  { img: 'jipiao.png', label: 'Flights' },
  { img: 'jiudian.png', label: 'Hotel' },
  { img: 'youxi.png', label: 'Games' },
  { img: 'qiyegou.png', label: 'Business' },
  { img: 'jiayouka.png', label: 'Fuel card' },
  { img: 'dianyingpiao.png', label: 'Movies' },
  { img: 'huochepiao.png', label: 'Trains' },
  { img: 'zhongchou.png', label: 'Crowdfunding' },
  { img: 'licai.png', label: 'Finance' },
  { img: 'lipinka.png', label: 'Gift card' },
  { img: 'baitiao.png', label: 'Credit' },
];

export default function HomeRightSidebar() {
  return (
    <div className={styles.homeRightSidebar}>
      <div className={styles.userBox}>
        <div className={styles.userInfo}>
          <div className={styles.userAvatar}>
            <a href="/login"><img src={`${STATIC}/touxiang.png`} alt="" /></a>
          </div>
          <div className={styles.userText}>
            <p>Hi, welcome!</p>
            <p>
              <a href="/login">Sign in</a>
              <a href="/register">Register</a>
            </p>
          </div>
        </div>
        <div className={styles.userLinks}>
          <a href="/promo/new">New user deals</a>
          <a href="/member">PLUS member</a>
        </div>
      </div>
      <div className={styles.promoBox}>
        <div className={styles.promoTabs}>
          <p className={styles.active}><a href="/promo">Promo</a></p>
          <p><a href="/notice">Notice</a></p>
          <a href="/promo/more" className={styles.more}>More</a>
        </div>
        <div className={styles.promoList}>
          <ul>
            <li><a href="/search?kw=tissue">Tissue deals</a></li>
            <li><a href="/search?kw=furniture">Furniture 999 off 300</a></li>
            <li><a href="/search?kw=fridge">Tech fridge, save 1000</a></li>
            <li><a href="/coupon">Grab 102 off 101 coupon</a></li>
          </ul>
        </div>
      </div>
      <div className={styles.serviceIcons}>
        <ul>
          {SERVICE_ITEMS.map((item, i) => (
            <li key={i}>
              <a href={`/service/${i}`}>
                <img src={`${STATIC}/${item.img}`} alt="" />
                <span>{item.label}</span>
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
