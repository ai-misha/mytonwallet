import React, { memo } from '../../lib/teact/teact';

import { IS_GRAM_WALLET } from '../../config';

import useLang from '../../hooks/useLang';

import CatHead from '../cat/CatHead';
import Image from '../ui/Image';

import styles from './AppLocked.module.scss';

import gramWalletLogoPath from '../../assets/logoGramWallet.svg';

function Logo() {
  const lang = useLang();

  if (IS_GRAM_WALLET) {
    return (
      <div className={styles.logo}>
        <Image className={styles.logo} imageClassName={styles.logo} url={gramWalletLogoPath} alt={lang('Logo')} />
      </div>
    );
  }

  // The wallet is locked, so the cat is asleep 💤
  return (
    <div className={styles.logo} aria-label={lang('Logo')}>
      <CatHead className={styles.catLogo} isSleeping />
      <span className={styles.catZzz} aria-hidden>
        <span>z</span>
        <span>z</span>
        <span>Z</span>
      </span>
    </div>
  );
}

export default memo(Logo);
