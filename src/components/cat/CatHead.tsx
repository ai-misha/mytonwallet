import React, { memo } from '../../lib/teact/teact';

import buildClassName from '../../util/buildClassName';

import styles from './CatHead.module.scss';

type OwnProps = {
  className?: string;
  isSleeping?: boolean;
  isHappy?: boolean;
};

/**
 * A ginger cat head. Pupils follow `--look-x` / `--look-y` (in SVG units, ±3) set on any ancestor,
 * and the eyes blink by themselves every few seconds.
 */
function CatHead({ className, isSleeping, isHappy }: OwnProps) {
  return (
    <svg
      className={buildClassName(styles.root, isSleeping && styles.sleeping, isHappy && styles.happy, className)}
      viewBox="0 0 100 90"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden
    >
      <g className={styles.earLeft}>
        <path className={styles.fur} d="M13 44 L19 5 Q21 -1 26 3 L47 25 Z" />
        <path className={styles.skin} d="M20 35 L23 13 L38 26 Z" />
      </g>
      <g className={styles.earRight}>
        <path className={styles.fur} d="M87 44 L81 5 Q79 -1 74 3 L53 25 Z" />
        <path className={styles.skin} d="M80 35 L77 13 L62 26 Z" />
      </g>

      <ellipse className={styles.fur} cx="50" cy="55" rx="40" ry="33" />
      <ellipse className={styles.muzzle} cx="50" cy="70" rx="17" ry="12" />

      <path className={styles.stripe} d="M50 24 L50 34 M41 25 L43.5 33 M59 25 L56.5 33" />
      <path className={styles.stripe} d="M11 52 L20 54 M11 60 L19 60 M89 52 L80 54 M89 60 L81 60" />

      <ellipse className={styles.blush} cx="24" cy="67" rx="6" ry="3.5" />
      <ellipse className={styles.blush} cx="76" cy="67" rx="6" ry="3.5" />

      <g className={styles.eyes}>
        <g className={styles.eye}>
          <ellipse className={styles.iris} cx="34" cy="52" rx="7.5" ry="8.5" />
          <ellipse className={styles.pupil} cx="34" cy="52" rx="2.6" ry="6.4" />
          <circle className={styles.shine} cx="36.5" cy="48.5" r="1.8" />
        </g>
        <g className={styles.eye}>
          <ellipse className={styles.iris} cx="66" cy="52" rx="7.5" ry="8.5" />
          <ellipse className={styles.pupil} cx="66" cy="52" rx="2.6" ry="6.4" />
          <circle className={styles.shine} cx="68.5" cy="48.5" r="1.8" />
        </g>
      </g>
      <path className={styles.closedEyes} d="M27 53 Q34 59 41 53 M59 53 Q66 59 73 53" />

      <path className={styles.nose} d="M45.5 63.5 Q50 61.5 54.5 63.5 L50 68.5 Z" />
      <path className={styles.mouth} d="M50 68.5 Q50 74 44 73.5 M50 68.5 Q50 74 56 73.5" />
      <path className={styles.tongue} d="M47.5 73 Q50 79 52.5 73 Z" />

      <path
        className={styles.whiskers}
        d="M32 67 L4 61 M32 70.5 L3 72 M33 74 L7 82 M68 67 L96 61 M68 70.5 L97 72 M67 74 L93 82"
      />
    </svg>
  );
}

export default memo(CatHead);
