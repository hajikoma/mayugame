package com.hajikoma.mayugame.framework.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 本ゲーム唯一のView。（広告を除く）
 * 仮想フレームバッファを描画し、メインループスレッド処理を行う。
 */
public class AndroidFastRenderView extends SurfaceView implements Runnable {

	/** gemeアクティビティ */
	AndroidGame game;
	/** 仮想フレームバッファ */
	Bitmap framebuffer;
	/** メインループスレッド */
	Thread renderThread = null;
	/** メインループスレッドホルダー */
	SurfaceHolder holder;
	/** スレッド処理中かどうかのフラグ */
	volatile boolean running = false;

	/**
	 * AndroidFastRenderViewインスタンスを生成する。
	 * @param game gemeアクティビティ
	 * @param frameBuffer 仮想フレームバッファ
	 */
	public AndroidFastRenderView(AndroidGame game, Bitmap framebuffer) {
		super(game);
		this.game = game;
		this.framebuffer = framebuffer;
		this.holder = getHolder();
	}

	/**
	 * スレッドをスタートする。
	 * アクティビティのonResumeに合わせて呼び出す。
	 */
	public void resume() {
		running = true;
		renderThread = new Thread(this);
		renderThread.start();
	}

	/** スレッド処理の内容。仮想フレームバッファを描画する。 */
	public void run() {
		Rect dstRect = new Rect();
		long startTime = System.nanoTime();
		while(running) {
			if(!holder.getSurface().isValid())
				continue;

			float deltaTime = (System.nanoTime()-startTime) / 1000000000.0f;
			startTime = System.nanoTime();
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().present(deltaTime);

			Canvas canvas = holder.lockCanvas();
			canvas.getClipBounds(dstRect);	//SurfaceViewの大きさ(画面全体の大きさ)を引数のRectに設定する
			canvas.drawBitmap(framebuffer, null, dstRect, null);	//画面全体を塗りつぶす
			holder.unlockCanvasAndPost(canvas);
		}
	}

	/**
	 * スレッドを終了する。
	 * アクティビティのonPauseに合わせて呼び出す。
	 */
	public void pause() {
		running = false;
		while(true) {
			try {
				renderThread.join();
				break;
			} catch (InterruptedException e) {
				//joinメソッドが実行されるまで無限ループ
			}
		}
	}

}


