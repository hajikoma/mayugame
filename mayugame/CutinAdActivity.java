package com.hajikoma.mayugame;

import net.app_c.cloud.sdk.AppCCloud;
import net.app_c.cloud.sdk.AppCCutinView.OnAppCCutinListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * appCCloudによるカットイン広告を表示するアクティビティ。
 * 一時的に最前面アクティビティとして生成され、広告の×ボタンを押すことでアクティビティごと削除される。
 */
public class CutinAdActivity extends Activity implements OnAppCCutinListener {

	private AppCCloud appCCloud;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//全画面表示に設定
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		RelativeLayout adView = new RelativeLayout(this);
		setContentView(adView);

		// appC cloud生成
		appCCloud = new AppCCloud(this).start();
	}

	/** カットインの初期化と表示 */
	@Override
	protected void onResume() {
		super.onResume();
		appCCloud.Ad.initCutin();
		appCCloud.Ad.callCutin();
	}

	/** appC cloud終了処理 */
	@Override
	public void finish() {
		super.finish();
		appCCloud.finish();
	}

	/** カットインオープン（OnAppCCutinListenerのコールバック） */
	@Override
	public void onCutinOpen() {
		//Toast.makeText(this, "カットインオープン", Toast.LENGTH_SHORT).show();
	}

	/**
	 * カットインクローズ（OnAppCCutinListenerのコールバック）
	 * CutinActivityをfinishし、結果元のアクティビティに処理が戻る
	 */
	@Override
	public void onCutinClose() {
		//Toast.makeText(this, "カットインクローズ", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	/**
	 * カットイン終了（OnAppCCutinListenerのコールバック）
	 * 何もしない
	 */
	@Override
	public void onCutinFinish() { /*何もしない*/ }

}