package com.hajikoma.mayugame.framework.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;

import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Pool;
import com.hajikoma.mayugame.framework.Pool.PoolObjectFactory;


/**
 * ジェスチャーイベント、スケールジェスチャーイベントを処理する。
 * 両者のリスナーを並列関係で処理することはできないので、スケールジェスチャー→ジェスチャーの順で処理する。
 * 詳しくはonTouchメソッドを参照
 */
public class GestureHandler implements OnTouchListener {
	//boolean[] isTouched = new boolean[20];
	//int[] touchX = new int[20];
	//int[] touchY = new int[20];
	Pool<GestureEvent> gestureEventPool;
	List<GestureEvent> gestureEvents = new ArrayList<GestureEvent>();
	List<GestureEvent> gestureEventsBuffer = new ArrayList<GestureEvent>();
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	float scaleX;
	float scaleY;

	/**
	 * GestureHandlerを生成する。
	 * @param context コンテクスト
	 * @param view リスナーを登録するビュー
	 * @param scaleX X軸の解像度の違いを補正する係数
	 * @param scaleY Y軸の解像度の違いを補正する係数
	 */
	public GestureHandler(Context context, View view, float scaleX, float scaleY) {
		PoolObjectFactory<GestureEvent> factory = new PoolObjectFactory<GestureEvent>() {
			@Override
			public GestureEvent createObject() {
				return new GestureEvent();
			}
		};
		gestureEventPool = new Pool<GestureEvent>(factory, 100);
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		gestureDetector = new GestureDetector(context, new GesutureListener());
		view.setOnTouchListener(this);

		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * タッチされた時に呼ばれる。
	 * ピンチイン・ピンチアウト中はscaleGestureDetectorにイベントを渡し、
	 * それ以外の時はgestureDetectorにイベントを渡すことで、イベントの競合を避ける。
	 * @param v イベントが渡されるビュー
	 * @param event イベント
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//v.performClick();	//警告回避のため入れている
		if(scaleGestureDetector != null) {
			final boolean isScaling = scaleGestureDetector.isInProgress();
				scaleGestureDetector.onTouchEvent(event);
				if(isScaling || scaleGestureDetector.isInProgress()){
					return true;
			}
		}
		if(gestureDetector != null){
			return gestureDetector.onTouchEvent(event);
		}
		return false;
	}

	/**
	 * 内部クラス ScaleGestureListener
	 * SimpleOnScaleGestureListenerを継承し、スケールジェスチャーイベントの処理内容を実装する。
	 * ピンチイン・ピンチアウトの判別は、タッチしている２点の距離が広がっているか狭まっているかによって行っている。
	 */
	private class ScaleGestureListener extends SimpleOnScaleGestureListener {

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SCALE_BEGIN;
			gestureEvent.x1 = (int)(detector.getFocusX() * scaleX);
			gestureEvent.y1 = (int)(detector.getFocusY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onScaleBegin");
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			if (detector.getCurrentSpan()
					- detector.getPreviousSpan() > 0) {
				gestureEvent.type = GestureEvent.GESTURE_SCALE_OUT;
			} else {
				gestureEvent.type = GestureEvent.GESTURE_SCALE_IN;
			}
			gestureEvent.span = detector.getCurrentSpan();
			gestureEvent.x1 = (int) (detector.getFocusX() * scaleX);
			gestureEvent.y1 = (int) (detector.getFocusY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "" + gestureEvent.span);
			//Log.d("", "" + gestureEvent.type);
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SCALE_END;
			gestureEvent.x1 = (int)(detector.getFocusX() * scaleX);
			gestureEvent.y1 = (int)(detector.getFocusY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onScaleEnd");
		}
	}

	/**
	 * 内部クラス GesutureListener
	 * SimpleOnGestureListenerを継承し、ジェスチャーイベントの処理内容を実装する。
	 */
	private class GesutureListener extends SimpleOnGestureListener{
		@Override
		public boolean onDown(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_DOWN;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onDown");
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SINGLE_TAP_UP;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onSingleTapUp");
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SINGLE_TAP_CONFIRMED;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onSingleTapConfirmed");
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_DOUBLE_TAP;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onDoubleTap");
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_DOUBLE_TAP_EVENT;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onDoubleTapEvent");
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_FLING;
			gestureEvent.x1 = (int)(e1.getX() * scaleX);
			gestureEvent.y1 = (int)(e1.getY() * scaleY);
			gestureEvent.x2 = (int)(e2.getX() * scaleX);
			gestureEvent.y2 = (int)(e2.getY() * scaleY);
			gestureEvent.velocityX = velocityX;
			gestureEvent.velocityY = velocityY;
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onFling");
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SCROLL;
			gestureEvent.x1 = (int)(e1.getX() * scaleX);
			gestureEvent.y1 = (int)(e1.getY() * scaleY);
			gestureEvent.x2 = (int)(e2.getX() * scaleX);
			gestureEvent.y2 = (int)(e2.getY() * scaleY);
			gestureEvent.distanceX = distanceX;
			gestureEvent.distanceY = distanceY;
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onScroll");
			return false;
		}

		@Override
		public void onShowPress(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_SHOW_PRESS;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onShowPress");
		}

		@Override
		public void onLongPress(MotionEvent event) {
			GestureEvent gestureEvent = gestureEventPool.newObject();
			gestureEvent.type = GestureEvent.GESTURE_LONG_PRESS;
			gestureEvent.x1 = (int)(event.getX() * scaleX);
			gestureEvent.y1 = (int)(event.getY() * scaleY);
			gestureEventsBuffer.add(gestureEvent);
			//Log.d("", "onLongPress");
		}
	}


	/**
	 * タッチ座標Xを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標X
	 */
	/*
	public int getTouchX(int pointer) {
		synchronized (this) {
			if (pointer < 0 || pointer >= 20)
				return 0;
			else
				return touchX[pointer];
		}
	}*/

	/**
	 * タッチ座標Yを取得する
	 * @param pointer 照会するタッチのポインターID
	 * @return タッチ座標Y
	 */
	/*
	public int getTouchY(int pointer) {
		synchronized (this) {
			if (pointer < 0 || pointer >= 20)
				return 0;
			else
				return touchY[pointer];
		}
	}*/

	/**
	 * ジェスチャーイベントのリストを取得する
	 * 適当な期間でこのメソッドを呼ばないと、touchEventsが長くなりすぎ、Poolに移せなくなるので注意する。
	 * @return 前回getTouchEventsメソッドを実行した時以降のタッチイベントのリスト
	 */
	public List<GestureEvent> getGestureEvents() {
		synchronized (this) {
			int len = gestureEvents.size();
			for (int i = 0; i < len; i++){
				gestureEventPool.free(gestureEvents.get(i));
			}
			gestureEvents.clear();
			gestureEvents.addAll(gestureEventsBuffer);
			gestureEventsBuffer.clear();
			return gestureEvents;
		}
	}
}
