package com.hajikoma.mayugame.framework.impl;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.hajikoma.mayugame.framework.Input;

/**
 * Inputインタフェースを実装する。Inputインタフェースの内部クラスKeyEventとTouchEventはオーバーライドしない。
 * タッチスクリーン、キーボード、加速度センサーをポーリングし、イベントに基づいてタッチスクリーンとキーボードにアクセスする。
 * 内部にクラスKeyEventとクラスTouchEventを持つ。これらクラスはパフォーマンス上の理由でアクセス修飾子をpublicにしている。
 */
public class AndroidInput implements Input {

	//AccelerometerHandler accelHandler;
	KeyboardHandler keyHandler;
	GestureHandler gestureHandler;

	/**
	 * AndroidInputを生成する。
	 * @param context コンテクスト
	 * @param view リスナーを登録するビュー
	 * @param scaleX
	 * @param scaleY
	 */
	public AndroidInput(Context context, View view, float scaleX, float scaleY) {
		//accelHandler = new AccelerometerHandler(context);
		keyHandler = new KeyboardHandler(view);
		gestureHandler = new GestureHandler(context, view, scaleX, scaleY);
	}

	/**
	 * キーの状態を取得する
	 * @param keyCode 状態を照会するキーコード
	 * @return キーが押されている場合true、押されていないまたは定義されていないキーコードの場合false
	 */
	@Override
	public boolean isKeyPressed(int keyCode) {
		return keyHandler.isKeyPressed(keyCode);
	}

	/**
	 * タッチ状態を取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチされている場合true、タッチされていないまたはIDが不正な場合false
	 */
	/*@Override
	public boolean isTouchDown(int pointer) {
		return touchHandler.isTouchDown(pointer);
	}*/

	/**
	 * タッチ座標Xを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標X
	 */
	/*
	@Override
	public int getTouchX(int pointer) {
		return touchHandler.getTouchX(pointer);
	}*/

	/**
	 * タッチ座標Yを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標Y
	 */
	/*
	@Override
	public int getTouchY(int pointer) {
		return touchHandler.getTouchY(pointer);
	}*/

	/**
	 * X軸の加速度を取得する
	 * @return X軸の加速度
	 */
/*	@Override
	public float getAccelX() {
		return accelHandler.getAccelX();
	}*/

	/**
	 * Y軸の加速度を取得する
	 * @return Y軸の加速度
	 */
/*	@Override
	public float getAccelY() {
		return accelHandler.getAccelY();
	}*/

	/**
	 * Z軸の加速度を取得する
	 * @return Z軸の加速度
	 */
/*	@Override
	public float getAccelZ() {
		return accelHandler.getAccelZ();
	}*/

	/**
	 * 最後のこのメソッドを呼び出した時以降に記録されたキーイベントを取得する
	 * イベントは発生順に記録されている
	 */
	/*
	@Override
	public List<TouchEvent> getTouchEvents() {
		return touchHandler.getTouchEvents();
	}*/

	/**
	 * 最後のこのメソッドを呼び出した時以降に記録されたタッチイベントを取得する
	 * イベントは発生順に記録されている
	 */
	@Override
	public List<KeyEvent> getKeyEvents() {
		return keyHandler.getKeyEvents();
	}

	/**
	 * 最後のこのメソッドを呼び出した時以降に記録されたタッチイベントを取得する
	 * イベントは発生順に記録されている
	 */
	@Override
	public List<GestureEvent> getGestureEvents() {
		return gestureHandler.getGestureEvents();
	}
}
