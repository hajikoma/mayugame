package com.hajikoma.mayugame;

/*import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MayuGame extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mayu_game);
	}


}*/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.impl.AndroidGame;
import com.hajikoma.mayugame.screen.LoadingScreen;


/**
 * AndroidGameクラスを継承する、本ゲーム唯一のアクティビティ。（広告を除く）
 * プログラムの起動窓口（よってデフォルトインテントはこのクラスを設定）
 * 各低レベル処理を行うクラスインスタンスを格納し、スクリーンを切り替え時にはこのクラスが必ず渡される。
 * ライフサイクルに沿ってメインループを管理しデータを保存する。
 */
public class MayuGame extends AndroidGame{

	/** 広告表示アクティビティ */
	private Intent adIntent;

	/** 初期スクリーンを取得する(このクラスのonCreateで呼ばれる) */
	@Override
    public Screen getStartScreen(){
		return new LoadingScreen(this);
	}

	/**
	 * 広告表示アクティビティを生成（準備）する。
	 * 広告データの受信に時間がかかるため、準備と表示のメソッドを分離している
	 */
	public void adActivityPrepare(){
		if(adIntent == null){
			adIntent = new Intent(getApplicationContext(),CutinAdActivity.class);
		}
	}

	/**
	 * 広告表示アクティビティを開始する
	 * 広告データの受信に時間がかかるため、準備と表示のメソッドを分離している
	 * @exception NullPointerException 事前にadActivityPrepareメソッドを呼んでいない場合スロー
	 */
	public void adActivityForward(){
		if(adIntent != null){
			startActivity(adIntent);

			adIntent = null;
		}else{
			throw new NullPointerException("adActivityPrepareメソッドを事前に呼び出してください");
		}
	}

	/**
	 * ブラウザインテントにURIを渡して起動する
	 * @param passURI ブラウザに渡すURI
	 */
	public void startBrowserApp(String passURI){
		Uri uri = Uri.parse(passURI);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
	}

	/**
	 * 「戻る」ハードウェアキーが押されたときに呼ばれ、現在のScreenに応じた処理を行う。
	 * 描画内容がリアルタイムで変わるScreen(ex.TrimScreen)では、ゲーム終了確認のアラートを表示
	 * それ以外のScreenでは、一画面前へ戻る
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				showAlertDialog();
				return false;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/** アプリ終了確認のアラートを表示する */
	private void showAlertDialog(){
		//描画を止める
		getRenderView().pause();
		getCurrentScreen().pause();

		AlertDialog.Builder aDB = new AlertDialog.Builder(this);
		aDB.setMessage("お手入れを終わる？");

		aDB.setNegativeButton("いいえ",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which){
						//描画を再開
						getCurrentScreen().resume();
						getRenderView().resume();
					}
				});
		aDB.setPositiveButton("はい",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which){
						finish();
					}
				});
		//aDB.setCancelable(true);
		AlertDialog aD = aDB.create();
		aD.show();
	}

}

