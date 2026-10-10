import React, {
  memo, useEffect, useRef, useState,
} from '../../lib/teact/teact';

import { requestMutation } from '../../lib/fasterdom/fasterdom';
import buildClassName from '../../util/buildClassName';
import { playMeow, playPurr } from '../../util/cat/meow';

import useLastCallback from '../../hooks/useLastCallback';

import CatHead from './CatHead';

import styles from './CatOverlay.module.scss';

const FIRST_PEEK_DELAY = [8000, 15000] as const;
const NEXT_PEEK_DELAY = [35000, 80000] as const;
const PEEK_DURATION = 5000;
const HAPPY_DURATION = 1400;
const MAX_PAW_PRINTS = 10;
const PAW_PRINT_LIFETIME = 1000;
const MAX_LOOK_OFFSET = 3;
const TAPS_FOR_RAIN = 3;
const TAP_SERIES_TIMEOUT = 900;

const RAIN_EMOJIS = ['🐱', '😺', '😸', '😻', '🐾', '🐈', '😽', '🐟', '🧶', '🐈‍⬛'];
const RAIN_DROP_COUNT = 32;
const RAIN_LIFETIME = 4500;
const SECRET_WORDS = ['meow', 'мяу', 'kitty', 'кис'];
const SECRET_BUFFER_LENGTH = 8;

function getRandomBetween([min, max]: readonly [number, number]) {
  return min + Math.random() * (max - min);
}

function isTypingTarget(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) return false;

  return target.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName);
}

function isAnimationDisabled() {
  return document.documentElement.classList.contains('animation-level-0');
}

let pawPrintCount = 0;
let isLeftPaw = false;

function spawnPawPrint(x: number, y: number) {
  if (pawPrintCount >= MAX_PAW_PRINTS || isAnimationDisabled()) return;

  pawPrintCount++;
  isLeftPaw = !isLeftPaw;

  const element = document.createElement('div');
  element.className = 'cat-paw-print';
  element.style.setProperty('--paw-x', `${x + (isLeftPaw ? -5 : 5)}px`);
  element.style.setProperty('--paw-y', `${y}px`);
  element.style.setProperty('--paw-rotation', `${Math.round((Math.random() - 0.5) * 50)}deg`);

  requestMutation(() => {
    document.body.appendChild(element);
  });

  setTimeout(() => {
    requestMutation(() => {
      element.remove();
      pawPrintCount--;
    });
  }, PAW_PRINT_LIFETIME);
}

export function startCatRain() {
  if (isAnimationDisabled()) return;

  const fragment = document.createDocumentFragment();
  const width = window.innerWidth;

  for (let i = 0; i < RAIN_DROP_COUNT; i++) {
    const drop = document.createElement('div');
    drop.className = 'cat-rain-drop';
    drop.textContent = RAIN_EMOJIS[Math.floor(Math.random() * RAIN_EMOJIS.length)];
    drop.style.setProperty('--rain-x', `${Math.round(Math.random() * width)}px`);
    drop.style.setProperty('--rain-drift', `${Math.round((Math.random() - 0.5) * 160)}px`);
    drop.style.setProperty('--rain-spin', `${Math.round((Math.random() - 0.5) * 720)}deg`);
    drop.style.setProperty('--rain-duration', `${Math.round(2000 + Math.random() * 1600)}ms`);
    drop.style.animationDelay = `${Math.round(Math.random() * 900)}ms`;
    drop.style.fontSize = `${(1.25 + Math.random() * 1.5).toFixed(2)}rem`;
    fragment.appendChild(drop);
  }

  const drops = Array.from(fragment.children);

  requestMutation(() => {
    document.body.appendChild(fragment);
  });

  setTimeout(() => {
    requestMutation(() => {
      drops.forEach((drop) => drop.remove());
    });
  }, RAIN_LIFETIME);
}

/**
 * Global cat layer: a ginger cat that peeks from the screen edge every now and then (its eyes follow the pointer,
 * a tap makes it meow, three taps make it rain cats), paw prints on every tap, and a "meow" keyboard easter egg.
 */
function CatOverlay() {
  const rootRef = useRef<HTMLDivElement>();
  const [isPeeking, setIsPeeking] = useState(false);
  const [isHappy, setIsHappy] = useState(false);
  const isHoveredRef = useRef(false);
  const tapCountRef = useRef(0);
  const tapTimeoutRef = useRef<number>();

  // Peeking schedule
  useEffect(() => {
    let timeout: number;

    const scheduleNextPeek = (delay: readonly [number, number]) => {
      timeout = window.setTimeout(() => {
        if (document.hidden) {
          scheduleNextPeek(NEXT_PEEK_DELAY);
          return;
        }

        setIsPeeking(true);

        const hideWhenIdle = () => {
          timeout = window.setTimeout(() => {
            if (isHoveredRef.current) {
              hideWhenIdle();
              return;
            }

            setIsPeeking(false);
            scheduleNextPeek(NEXT_PEEK_DELAY);
          }, PEEK_DURATION);
        };

        hideWhenIdle();
      }, getRandomBetween(delay));
    };

    scheduleNextPeek(FIRST_PEEK_DELAY);

    return () => window.clearTimeout(timeout);
  }, []);

  // Paw prints on every tap and click
  useEffect(() => {
    const handlePointerDown = (e: PointerEvent) => {
      if (e.button !== 0) return;

      spawnPawPrint(e.clientX, e.clientY);
    };

    document.addEventListener('pointerdown', handlePointerDown, { capture: true, passive: true });

    return () => document.removeEventListener('pointerdown', handlePointerDown, { capture: true });
  }, []);

  // Typing a secret word anywhere outside of inputs summons the cats
  useEffect(() => {
    let buffer = '';

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key.length !== 1 || e.metaKey || e.ctrlKey || e.altKey || isTypingTarget(e.target)) return;

      buffer = (buffer + e.key.toLowerCase()).slice(-SECRET_BUFFER_LENGTH);

      if (SECRET_WORDS.some((word) => buffer.endsWith(word))) {
        buffer = '';
        playMeow();
        startCatRain();
        setIsPeeking(true);
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Eyes follow the pointer while the cat is visible
  useEffect(() => {
    if (!isPeeking) return undefined;

    let frame: number | undefined;
    let lastEvent: PointerEvent | undefined;

    const updateLook = () => {
      frame = undefined;
      const root = rootRef.current;
      if (!root || !lastEvent) return;

      const rect = root.getBoundingClientRect();
      const dx = lastEvent.clientX - (rect.left + rect.width / 2);
      const dy = lastEvent.clientY - (rect.top + rect.height / 3);
      const distance = Math.hypot(dx, dy) || 1;
      const factor = Math.min(1, distance / 120) * MAX_LOOK_OFFSET;

      requestMutation(() => {
        root.style.setProperty('--look-x', ((dx / distance) * factor).toFixed(2));
        root.style.setProperty('--look-y', ((dy / distance) * factor).toFixed(2));
      });
    };

    const handlePointerMove = (e: PointerEvent) => {
      lastEvent = e;
      frame ??= requestAnimationFrame(updateLook);
    };

    document.addEventListener('pointermove', handlePointerMove, { passive: true });

    return () => {
      document.removeEventListener('pointermove', handlePointerMove);
      if (frame) cancelAnimationFrame(frame);
    };
  }, [isPeeking]);

  const handleMouseEnter = useLastCallback(() => {
    isHoveredRef.current = true;
  });

  const handleMouseLeave = useLastCallback(() => {
    isHoveredRef.current = false;
  });

  const handleClick = useLastCallback(() => {
    tapCountRef.current++;
    window.clearTimeout(tapTimeoutRef.current);
    tapTimeoutRef.current = window.setTimeout(() => {
      tapCountRef.current = 0;
    }, TAP_SERIES_TIMEOUT);

    if (tapCountRef.current >= TAPS_FOR_RAIN) {
      tapCountRef.current = 0;
      startCatRain();
      playPurr();
    } else {
      playMeow(0.9 + Math.random() * 0.3);
    }

    setIsHappy(true);
    setTimeout(() => setIsHappy(false), HAPPY_DURATION);
  });

  return (
    <div
      ref={rootRef}
      className={buildClassName(styles.root, isPeeking && styles.peeking, isHappy && styles.happy)}
      aria-hidden
    >
      <div
        className={styles.cat}
        onClick={handleClick}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        {isHappy && (
          <div className={styles.hearts}>
            <span>💕</span>
            <span>✨</span>
            <span>💗</span>
          </div>
        )}
        <CatHead className={styles.head} isHappy={isHappy} />
        <div className={buildClassName(styles.paw, styles.pawLeft)} />
        <div className={buildClassName(styles.paw, styles.pawRight)} />
      </div>
    </div>
  );
}

export default memo(CatOverlay);
