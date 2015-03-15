package com.hajikoma.mayugame.framework.impl;

import android.media.SoundPool;

import com.hajikoma.mayugame.framework.Sound;

/**
 * Soundインタフェースを実装する。
 * 効果音の再生を制御する。
 */
public class AndroidSound implements Sound {

	/** SoundPool内での効果音のID */
	int soundId;
	/** 効果音をメモリ上にプールしておくインスタンス */
	SoundPool soundPool;

	/**
	 * フィールド変数soundIdとsoundPoolを初期化してインスタンスを生成する。
	 * @param soundPool 効果音がプールされるSoundPool
	 * @param soundId SoundPool内での効果音のID
	 */
	public AndroidSound(SoundPool soundPool, int soundId) {
		this.soundId = soundId;
		this.soundPool = soundPool;
	}

	/**
	 * 再生を開始する
	 * @param volume 再生音量
	 */
	@Override
	public void play(float volume) {
		soundPool.play(soundId, volume, volume, 0, 0, 1);
	}

	/** インスタンスを破棄する */
	@Override
	public void dispose() {
		soundPool.unload(soundId);
	}
}
