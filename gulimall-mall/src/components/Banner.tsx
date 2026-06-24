'use client';

import { useCallback, useEffect, useState } from 'react';
import styles from './Banner.module.css';

const MAIN_SLIDES = [
  { src: '/static/index/img/lunbo.png', alt: 'Banner 1' },
  { src: '/static/index/img/lunbo3.png', alt: 'Banner 2' },
  { src: '/static/index/img/lunbo6.png', alt: 'Banner 3' },
  { src: '/static/index/img/lunbo7.png', alt: 'Banner 4' },
];

export default function Banner() {
  const [mainIndex, setMainIndex] = useState(0);

  useEffect(() => {
    const t = setInterval(() => {
      setMainIndex((i) => (i + 1) % MAIN_SLIDES.length);
    }, 4000);
    return () => clearInterval(t);
  }, []);

  const goMain = useCallback((i: number) => {
    setMainIndex(i);
  }, []);

  const goPrev = useCallback(() => {
    setMainIndex((i) => (i - 1 + MAIN_SLIDES.length) % MAIN_SLIDES.length);
  }, []);

  const goNext = useCallback(() => {
    setMainIndex((i) => (i + 1) % MAIN_SLIDES.length);
  }, []);

  return (
    <div className={styles.wrapper}>
      {/* Main: 4 slides stacked, shown by index */}
      <div className={styles.swiper}>
        {MAIN_SLIDES.map((s, i) => (
          <a
            href="/search"
            key={i}
            className={styles.slide}
            style={{
              opacity: i === mainIndex ? 1 : 0,
              zIndex: i === mainIndex ? 1 : 0,
              pointerEvents: i === mainIndex ? 'auto' : 'none',
            }}
            aria-hidden={i !== mainIndex}
          >
            <img src={s.src} alt={s.alt} draggable={false} />
          </a>
        ))}
        {/* Control layer: does not capture clicks except on buttons */}
        <div className={styles.controls} aria-hidden>
          <div className={styles.pagination} role="tablist">
            {MAIN_SLIDES.map((_, i) => (
              <button
                key={i}
                type="button"
                role="tab"
                aria-label={`Slide ${i + 1}`}
                aria-selected={i === mainIndex}
                className={i === mainIndex ? styles.bulletActive : styles.bullet}
                onClick={() => goMain(i)}
              />
            ))}
          </div>
          <button type="button" className={styles.arrowPrev} aria-label="Previous" onClick={goPrev}>
            ‹
          </button>
          <button type="button" className={styles.arrowNext} aria-label="Next" onClick={goNext}>
            ›
          </button>
        </div>
      </div>

      {/* Bottom: two fixed promo images, equal size, not part of the carousel */}
      <div className={styles.sideImages}>
        <a href="/search" className={styles.sideImagesLeft}>
          <img src="/static/index/img/5a13bf0bNe1606e58.jpg" alt="Digital life" />
        </a>
        <a href="/search" className={styles.sideImagesRight}>
          <img src="/static/index/img/5a154759N5385d5d6.jpg" alt="Winter special" />
        </a>
      </div>
    </div>
  );
}
