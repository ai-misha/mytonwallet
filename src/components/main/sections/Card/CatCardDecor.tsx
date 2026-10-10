import React, { memo, useState } from '../../../../lib/teact/teact';

import buildClassName from '../../../../util/buildClassName';
import { playMeow, playPurr } from '../../../../util/cat/meow';

import useLastCallback from '../../../../hooks/useLastCallback';

import styles from './CatCardDecor.module.scss';

type OwnProps = {
  layer: 'back' | 'front';
};

const TWITCH_DURATION = 600;
const PURR_DURATION = 2000;

/**
 * Turns the wallet card into a cat: the `back` layer has ears and whiskers sticking out from behind the card,
 * the `front` layer has two paws holding the card from below. Ears meow when tapped, paws purr.
 */
function CatCardDecor({ layer }: OwnProps) {
  const [twitchingEar, setTwitchingEar] = useState<'left' | 'right'>();
  const [isPurring, setIsPurring] = useState(false);

  const handleEarClick = useLastCallback((side: 'left' | 'right') => {
    playMeow(side === 'left' ? 1 : 1.15);
    setTwitchingEar(side);
    setTimeout(() => setTwitchingEar(undefined), TWITCH_DURATION);
  });

  const handleLeftEarClick = useLastCallback(() => handleEarClick('left'));
  const handleRightEarClick = useLastCallback(() => handleEarClick('right'));

  const handlePawClick = useLastCallback(() => {
    if (isPurring) return;

    playPurr(PURR_DURATION / 1000);
    setIsPurring(true);
    setTimeout(() => setIsPurring(false), PURR_DURATION);
  });

  if (layer === 'back') {
    return (
      <div className={styles.back} aria-hidden>
        {renderEar('left', twitchingEar === 'left', handleLeftEarClick)}
        {renderEar('right', twitchingEar === 'right', handleRightEarClick)}
        {renderWhiskers('left')}
        {renderWhiskers('right')}
      </div>
    );
  }

  return (
    <div className={buildClassName(styles.front, isPurring && styles.purring)} aria-hidden>
      {renderPaw('left', handlePawClick)}
      {renderPaw('right', handlePawClick)}
    </div>
  );
}

function renderEar(side: 'left' | 'right', isTwitching: boolean, onClick: NoneToVoidFunction) {
  return (
    <svg
      className={buildClassName(styles.ear, styles[side], isTwitching && styles.twitching)}
      viewBox="0 0 56 46"
      xmlns="http://www.w3.org/2000/svg"
      onClick={onClick}
    >
      <path className={styles.earFur} d="M2 46 L20 5 Q26 -3 32 4 L54 46 Z" />
      <path className={styles.earSkin} d="M13 46 L24 16 Q26.5 11 29.5 16 L43 46 Z" />
      <path className={styles.earTuft} d="M24 22 L22 14 M27 20 L27 11 M30 22 L32 14" />
    </svg>
  );
}

function renderWhiskers(side: 'left' | 'right') {
  return (
    <svg
      className={buildClassName(styles.whiskers, styles[side])}
      viewBox="0 0 40 30"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path d="M40 9 Q20 4 1 1 M40 15 Q20 14 0 15 M40 21 Q20 25 2 29" />
    </svg>
  );
}

function renderPaw(side: 'left' | 'right', onClick: NoneToVoidFunction) {
  return (
    <svg
      className={buildClassName(styles.paw, styles[side])}
      viewBox="0 0 48 30"
      xmlns="http://www.w3.org/2000/svg"
      onClick={onClick}
    >
      <path className={styles.pawFur} d="M2 30 Q0 4 24 3 Q48 4 46 30 Z" />
      <path className={styles.pawLine} d="M16 6 L16 14 M24 4.5 L24 13 M32 6 L32 14" />
      <ellipse className={styles.pawBean} cx="24" cy="22" rx="7" ry="5" />
      <ellipse className={styles.pawBean} cx="12.5" cy="17" rx="3" ry="3.4" />
      <ellipse className={styles.pawBean} cx="35.5" cy="17" rx="3" ry="3.4" />
    </svg>
  );
}

export default memo(CatCardDecor);
