'use client';

import { useCallback, useEffect, useState } from 'react';
import styles from './ItemGalleryClient.module.css';

type Props = {
  images: string[];
  alt: string;
};

export function ItemGalleryClient({ images, alt }: Props) {
  const list =
    images.length > 0
      ? images
      : [
          'data:image/svg+xml,' +
            encodeURIComponent(
              '<svg xmlns="http://www.w3.org/2000/svg" width="440" height="440"><rect fill="#f0f0f0" width="100%" height="100%"/></svg>',
            ),
        ];
  const [active, setActive] = useState(0);

  useEffect(() => {
    setActive(0);
  }, [images.join('|')]);

  const main = list[Math.min(active, list.length - 1)];

  const scrollThumbs = useCallback((dir: -1 | 1) => {
    const el = document.getElementById('item-thumb-scroll');
    if (!el) return;
    el.scrollBy({ left: dir * 52, behavior: 'smooth' });
  }, []);

  return (
    <div className={`boxx ${styles.root}`}>
      <div className="imgbox">
        <div className="probox">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img className="img1" alt={alt} src={main} />
          <div className="hoverbox" aria-hidden />
        </div>
        <div className="showbox" aria-hidden>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img className="img1" alt="" src={main} />
        </div>
      </div>
      <div className="box-lh">
        <div className="box-lh-one">
          <ul id="item-thumb-scroll" style={{ display: 'flex', overflowX: 'auto', gap: 4, margin: 0, padding: 0 }}>
            {list.map((src, i) => (
              <li key={`${src}-${i}`} style={{ listStyle: 'none', flexShrink: 0 }}>
                <button
                  type="button"
                  className={styles.thumbBtn}
                  onClick={() => setActive(i)}
                  style={{
                    padding: 0,
                    border: active === i ? '2px solid #e4393c' : '1px solid #ddd',
                    cursor: 'pointer',
                    background: '#fafafa',
                  }}
                  aria-label={`Thumbnail ${i + 1}`}
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img className={styles.thumbImg} src={src} alt="" />
                </button>
              </li>
            ))}
          </ul>
        </div>
        <div id="left" role="button" tabIndex={0} onClick={() => scrollThumbs(-1)} onKeyDown={(e) => e.key === 'Enter' && scrollThumbs(-1)}>
          &lt;
        </div>
        <div id="right" role="button" tabIndex={0} onClick={() => scrollThumbs(1)} onKeyDown={(e) => e.key === 'Enter' && scrollThumbs(1)}>
          &gt;
        </div>
      </div>
    </div>
  );
}
