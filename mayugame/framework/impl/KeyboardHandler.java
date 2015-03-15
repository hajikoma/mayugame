package com.hajikoma.mayugame.framework.impl;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnKeyListener;

import com.hajikoma.mayugame.framework.Input.KeyEvent;
import com.hajikoma.mayugame.framework.Pool;
import com.hajikoma.mayugame.framework.Pool.PoolObjectFactory;

/**
 * キーイベントを処理する。特殊な「戻る」キーが押された時の処理は
 * activity（MayuGameクラス）で行っている。
 */
public class KeyboardHandler implements OnKeyListener {

	/** 各キーの状態を格納する */
	boolean[] pressedKeys = new boolean[128];
	/** KeyEventのプール */
	Pool<KeyEvent> keyEventPool;
	/** 前回getKeyEventsメソッドを実行した時以降に捕捉したKeyEventを格納する */
	List<KeyEvent> keyEventsBuffer = new ArrayList<KeyEvent>();
	/** getKeyEventsメソッドで返されるKeyEventのリスト */
	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();

	/**
	 * KeyboardHandolerを生成する。
	 * キーイベントを取得できるようビューを設定すると共に、KeyEventを格納するためのプールを生成する
	 * @param view キーイベントを登録するビュー
	 */
	public KeyboardHandler(View view) {
		PoolObjectFactory<KeyEvent> factory = new PoolObjectFactory<KeyEvent>() {
			@Override
			public KeyEvent createObject() {
				return new KeyEvent();
			}
		};
		keyEventPool = new Pool<KeyEvent>(factory, 100);
		view.setOnKeyListener(this);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
	}

	/**
	 * キーが押された時に呼ばれる
	 * イベントがACTION_DOWNまたはUPの場合は、KeyEventのメンバに適切な値をセットする。
	 * またpressedKeyにキーの状態を登録し、インスタンスをkeyEventsBufferに登録する。
	 * @param v リスナーが登録されているビュー
	 * @param keyCode イベントの発生したキーコード
	 * @param event キーイベントの種類
	 * @return 常にfalse(イベント処理をビューに渡す)
	 */
	@Override
	public boolean onKey(View v, int keyCode, android.view.KeyEvent event) {
		if (event.getAction() == android.view.KeyEvent.ACTION_MULTIPLE)
			return false;
		//キーイベントはUIスレッドで捕捉され、メインループスレッドで処理されるため、シンクロさせる。
		synchronized (this) {
			KeyEvent keyEvent = keyEventPool.newObject();
			keyEvent.keyCode = keyCode;
			keyEvent.keyChar = (char) event.getUnicodeChar();
			if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
				keyEvent.type = KeyEvent.KEY_DOWN;
				if(keyCode > 0 && keyCode < 127)
					pressedKeys[keyCode] = true;
			}
			if (event.getAction() == android.view.KeyEvent.ACTION_UP) {
				keyEvent.type = KeyEvent.KEY_UP;
				if(keyCode > 0 && keyCode < 127)
					pressedKeys[keyCode] = false;
			}
			keyEventsBuffer.add(keyEvent);
		}
		return false;
	}

	/**
	 * キーの状態を取得する
	 * @param keyCode 状態を照会するキーコード
	 * @return キーが押されている場合true、押されていないまたは定義されていないキーコードの場合false
	 */
	public boolean isKeyPressed(int keyCode) {
		if (keyCode < 0 || keyCode > 127)
			return false;
		return pressedKeys[keyCode];
	}

	/**
	 * キーイベントのリストを取得する
	 * 適当な期間でこのメソッドを呼ばないと、keyEventsが長くなりすぎ、Poolに移せなくなるので注意する。
	 * @return 前回getKeyEventsメソッドを実行した時以降のキーイベントのリスト
	 */
	public List<KeyEvent> getKeyEvents() {
		synchronized (this) {
			int len = keyEvents.size();
			for (int i = 0; i < len; i++)
				keyEventPool.free(keyEvents.get(i));
			keyEvents.clear();
			keyEvents.addAll(keyEventsBuffer);
			keyEventsBuffer.clear();
			return keyEvents;
		}
	}
}
