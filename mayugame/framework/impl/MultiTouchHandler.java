package com.hajikoma.mayugame.framework.impl;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;
import android.view.View;

import com.hajikoma.mayugame.framework.Input.TouchEvent;
import com.hajikoma.mayugame.framework.Pool;
import com.hajikoma.mayugame.framework.Pool.PoolObjectFactory;

/**
 * マルチタッチイベントを処理する。
 */
public class MultiTouchHandler implements TouchHandler {

	/** タッチ状態のフラグ */
	boolean[] isTouched = new boolean[20];
	/** タッチ座標x */
	int[] touchX = new int[20];
	/** タッチ座標y */
	int[] touchY = new int[20];
	/** TouchEventのプール */
	Pool<TouchEvent> touchEventPool;
	/** getTouchEventsメソッドで返されるTouchEventのリスト */
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	/** 前回getTouchEventsメソッドを実行した時以降に捕捉したTouchEventを格納する */
	List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();
	float scaleX;
	float scaleY;

	/**
	 * MultiTouchHandlerを生成する。
	 * マルチタッチイベントを取得できるようビューを設定すると共に、TouchEventを格納するためのプールを生成する
	 * @param view リスナーを登録するビュー
	 * @param scaleX
	 * @param scaleY
	 */
	public MultiTouchHandler(View view, float scaleX, float scaleY) {
		PoolObjectFactory<TouchEvent> factory = new PoolObjectFactory<TouchEvent>() {
			@Override
			public TouchEvent createObject() {
				return new TouchEvent();
			}
		};
		touchEventPool = new Pool<TouchEvent>(factory, 100);
		view.setOnTouchListener(this);	//OnTouchListenerを実装したインスタンスを通知先に指定する（ここでは自分自身）

		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * タッチされた時に呼ばれる
	 * イベントに応じてTouchEventのメンバに適切な値をセットする。
	 * またisTouchedにポインターID毎の状態を登録し、インスタンスをtouchEventsBufferに登録する。
	 * @param v リスナーが登録されているビュー
	 * @param event イベントの内容
	 * @return 常にtrue(このメソッドでイベントの処理は完結)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		synchronized (this) {
			int action = event.getActionMasked();		//getAction()では(Maskしないと)ACTION_POINTER_DOWN,ACTION_POINTER_UPが取得できない
			int pointerIndex = event.getActionIndex();	//pointerIndexの正体はMotionEventクラスの内部配列のインデックス。よって同じ指でも、event毎に異なってくる。
			int pointerId = event.getPointerId(pointerIndex);	//pointerIdは、指がタッチしている間は不変。空いている番号がIDとして割り当てられる。
			TouchEvent touchEvent;

			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				touchEvent = touchEventPool.newObject();
				touchEvent.type = TouchEvent.TOUCH_DOWN;
				touchEvent.pointer = pointerId;
				touchEvent.x = touchX[pointerId] = (int) (event.getX(pointerIndex) * scaleX);
				touchEvent.y = touchY[pointerId] = (int) (event.getY(pointerIndex) * scaleY);
				isTouched[pointerId] = true;
				touchEventsBuffer.add(touchEvent);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				touchEvent = touchEventPool.newObject();
				touchEvent.type = TouchEvent.TOUCH_UP;
				touchEvent.pointer = pointerId;
				touchEvent.x = touchX[pointerId] = (int) (event.getX(pointerIndex) * scaleX);
				touchEvent.y = touchY[pointerId] = (int) (event.getY(pointerIndex) * scaleY);
				isTouched[pointerId] = false;
				touchEventsBuffer.add(touchEvent);
				break;
			case MotionEvent.ACTION_MOVE:
				int pointerCount = event.getPointerCount();
				for (int i = 0; i < pointerCount; i++) {
					pointerIndex = i;
					pointerId = event.getPointerId(pointerIndex);
					touchEvent = touchEventPool.newObject();
					touchEvent.type = TouchEvent.TOUCH_DRAGGED;
					touchEvent.pointer = pointerId;
					touchEvent.x = touchX[pointerId] = (int) (event.getX(pointerIndex) * scaleX);
					touchEvent.y = touchY[pointerId] = (int) (event.getY(pointerIndex) * scaleY);
					touchEventsBuffer.add(touchEvent);
				}
				break;
			}
			return true;
		}
	}

	/**
	 * タッチ状態を取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチされている場合true、タッチされていないまたはIDが不正な場合false
	 */
	@Override
	public boolean isTouchDown(int pointer) {
		synchronized (this) {
			if (pointer < 0 || pointer >= 20)
				return false;
			else
				return isTouched[pointer];
		}
	}

	/**
	 * タッチ座標Xを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標X
	 */
	@Override
	public int getTouchX(int pointer) {
		synchronized (this) {
			if (pointer < 0 || pointer >= 20)
				return 0;
			else
				return touchX[pointer];
		}
	}

	/**
	 * タッチ座標Yを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標Y
	 */
	@Override
	public int getTouchY(int pointer) {
		synchronized (this) {
			if (pointer < 0 || pointer >= 20)
				return 0;
			else
				return touchY[pointer];
		}
	}

	/**
	 * タッチイベントのリストを取得する
	 * 適当な期間でこのメソッドを呼ばないと、touchEventsが長くなりすぎ、Poolに移せなくなるので注意する。
	 * @return 前回getTouchEventsメソッドを実行した時以降のタッチイベントのリスト
	 */
	@Override
	public List<TouchEvent> getTouchEvents() {
		synchronized (this) {
			int len = touchEvents.size();
			for (int i = 0; i < len; i++)
				touchEventPool.free(touchEvents.get(i));
			touchEvents.clear();
			touchEvents.addAll(touchEventsBuffer);
			touchEventsBuffer.clear();
			return touchEvents;
		}
	}
}
