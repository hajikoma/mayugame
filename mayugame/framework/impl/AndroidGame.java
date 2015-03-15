package com.hajikoma.mayugame.framework.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.hajikoma.mayugame.framework.Audio;
import com.hajikoma.mayugame.framework.FileIO;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Input;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;
import com.hajikoma.mayugame.framework.Vibrate;

/**
 * Gameインターフェースを実装する。
 * ゲームプログラムに必要な各種モジュール（ループ、グラフィック、ミュージック、ファイル）のインスタンスを格納する。
 * 各スクリーンではスクリーン固有の描画やイベントを定義し、モジュールの操作はこのクラスを通じて一手に行う。
 * 実際には、唯一のアクティビティであるこのクラスからアクティブスクリーンを次々と切り替えてゲームを表現する。
 */
public abstract class AndroidGame extends Activity implements Game {

	/** X軸方向のターゲット画面解像度 */
	final int TARGET_WIDTH = 720;
	/** Y軸方向のターゲット画面解像度 */
	final int TARGET_HEIGHT = 1280;
	/** メインループスレッドを処理するSurfaceView */
	private AndroidFastRenderView renderView;
	/** 画像処理モジュール */
	Graphics graphics;
	/** BGM,効果音モジュール */
	Audio audio;
	/** ユーザー入力処理モジュール */
	Input input;
	/** データ入出力モジュール */
	FileIO fileIO;
	/** データ入出力モジュール */
	Text text;
	/** バイブレーション処理モジュール */
	Vibrate vibrate;
	/** アクティブスクリーン */
	Screen screen;


	/** アクティビティ生成時に呼ばれる */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//全画面表示に設定
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//画面解像度の違いを補正する
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		int frameBufferWidth =	isLandscape ? TARGET_HEIGHT : TARGET_WIDTH;
		int frameBufferHeight =	isLandscape ? TARGET_WIDTH : TARGET_HEIGHT;
		Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Config.RGB_565);
		float scaleX;
		float scaleY;

		//getWidth(),getHeightがAPI level13以降非推奨なので、処理を分岐させる
		if(Build.VERSION.SDK_INT < 13){
			scaleX = (float) frameBufferWidth / getWindowManager().getDefaultDisplay().getWidth();
			scaleY = (float) frameBufferHeight / getWindowManager().getDefaultDisplay().getHeight();
		}else{
			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);
			scaleX = (float) frameBufferWidth / size.x;
			scaleY = (float) frameBufferHeight / size.y;
		}

		//各モジュールのインスタンス化
		renderView =	new AndroidFastRenderView(this, frameBuffer);
		graphics =		new AndroidGraphics(getAssets(), frameBuffer);
		fileIO =		new AndroidFileIO(getAssets());
		audio =			new AndroidAudio(this);
		input =			new AndroidInput(this, getRenderView(), scaleX, scaleY);
		text =			new AndroidText(frameBuffer);
		vibrate =		new AndroidVibrate(this);
		screen =		getStartScreen();

		setContentView(getRenderView());

		//スクリーンロックをオフに設定
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	/** 再開時に呼ばれる */
	@Override
	public void onResume() {
		super.onResume();
		screen.resume();
		getRenderView().resume();
	}

	/** 一時停止時に呼ばれる */
	@Override
	public void onPause() {
		super.onPause();
		getRenderView().pause();
		screen.pause();

		if (isFinishing())
			screen.dispose();
	}

	/** AndroidFastRenderViewインスタンスを取得する */
	@Override
	public AndroidFastRenderView getRenderView() {
		return renderView;
	}

	/** Inputインスタンスを取得する */
	@Override
	public Input getInput() {
		return input;
	}

	/** FileIOインスタンスを取得する */
	@Override
	public FileIO getFileIO() {
		return fileIO;
	}

	/** Graphicsインスタンスを取得する */
	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	/** Audioインスタンスを取得する */
	@Override
	public Audio getAudio() {
		return audio;
	}

	/** Textインスタンスを取得する */
	@Override
	public Text getText() {
		return text;
	}

	/** Vibrateインスタンスを取得する */
	@Override
	public Vibrate getVibrate() {
		return vibrate;
	}

	/**
	 * 表示するスクリーンを設定する。既存のスクリーンは一時停止後、破棄される。
	 * @param screen アクティブに設定するスクリーン
	 */
	@Override
	public void setScreen(Screen screen) {
		if (screen == null){
			throw new IllegalArgumentException("Screen must not be null");
		}

		this.screen.pause();
		this.screen.dispose();
		System.gc();
		screen.resume();
		screen.update(0);
		this.screen = screen;
	}

	/**
	 * 現在のスクリーンを取得する
	 * @return アクティブな現在のスクリーン
	 */
	public Screen getCurrentScreen() {
		return screen;
	}

}
