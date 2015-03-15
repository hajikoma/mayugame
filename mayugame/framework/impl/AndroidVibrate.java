package com.hajikoma.mayugame.framework.impl;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import com.hajikoma.mayugame.framework.Vibrate;

/**
 * Vibrateインターフェースを実装する。
 * 内部にVibratorインスタンスを保持し、バイブレーション機能を制御する。
 */
public class AndroidVibrate implements Vibrate {

	/** 保持するVibratorインスタンス */
	Vibrator vibrator;
	/** 繰り返し振動のフラグ */
	boolean isRepeat = false;

	/**
	 * AndroidVibrateを生成する。
	 * @param activity アクティビティ。Gameインスタンスが渡される。
	 */
	public AndroidVibrate(Activity activity) {
		vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
	}

	/**
	 * 一度だけ振動させる
	 * @param milliseconds 振動させる時間（ミリ秒）
	 */
	@Override
	public void vibrate(int milliseconds){
		vibrator.vibrate(milliseconds);
	}

	/**
	 * 与えられたパターンで振動させる。
	 * androidが提供しているクラスVibratorのメソッドVibrate()の引数intは、
	 * 0：繰り返し、-1：繰り返さない、なので間違いに注意する。
	 * @param pattern 振動パターン。OFF/ON/OFF/ON…の順に、ミリ秒で指定する。
	 * @param repeat 繰り返し回数
	 */
	@Override
	public void vibrate(long[] pattern, int repeat) {
		for(int i = 0; i < repeat; i++){
			vibrator.vibrate(pattern, -1);
			isRepeat = true;
		}
		isRepeat = false;
	}

	/** 振動を止める */
	@Override
	public void cancel(){
		if(isRepeat){
			vibrator.cancel();
		}
	}
}
