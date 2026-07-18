'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import type { PromoBannerSlide } from '@/components/home/promoBanners';
import styles from './ProductPromoCarousel.module.css';

type Props = {
  slides: PromoBannerSlide[];
};

export default function ProductPromoCarousel({ slides }: Props) {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    if (slides.length <= 1) return undefined;
    const t = setInterval(() => {
      setIndex((i) => (i + 1) % slides.length);
    }, 4500);
    return () => clearInterval(t);
  }, [slides.length]);

  const go = useCallback((i: number) => setIndex(i), []);
  const goPrev = useCallback(() => {
    setIndex((i) => (i - 1 + slides.length) % slides.length);
  }, [slides.length]);
  const goNext = useCallback(() => {
    setIndex((i) => (i + 1) % slides.length);
  }, [slides.length]);

  if (slides.length === 0) return null;

  return (
    <section className={styles.wrapper} aria-label="Featured product promotions">
      <div className={styles.swiper}>
        {slides.map((slide, i) => (
          <Link
            key={slide.id}
            href={`/item/${slide.skuId}`}
            className={styles.slide}
            style={{
              opacity: i === index ? 1 : 0,
              zIndex: i === index ? 1 : 0,
              pointerEvents: i === index ? 'auto' : 'none',
            }}
            aria-hidden={i !== index}
            tabIndex={i === index ? 0 : -1}
            aria-label={`View ${slide.title}`}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={slide.bannerSrc} alt={slide.title} draggable={false} />
          </Link>
        ))}

        {slides.length > 1 ? (
          <div className={styles.controls} aria-hidden>
            <div className={styles.pagination} role="tablist">
              {slides.map((slide, i) => (
                <button
                  key={slide.id}
                  type="button"
                  role="tab"
                  aria-label={`Slide ${i + 1}: ${slide.title}`}
                  aria-selected={i === index}
                  className={i === index ? styles.bulletActive : styles.bullet}
                  onClick={() => go(i)}
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
        ) : null}
      </div>
    </section>
  );
}
