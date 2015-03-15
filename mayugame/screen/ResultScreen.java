package com.hajikoma.mayugame.screen;

import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.MayuGame;
import com.hajikoma.mayugame.Mission;
import com.hajikoma.mayugame.Scores;
import com.hajikoma.mayugame.Settings;
import com.hajikoma.mayugame.UserData;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;

/**
 * お手入れの結果を表示するスクリーン
 * 結果表示後にカットイン広告を一度だけ表示する
 */
public class ResultScreen extends Screen {

	/** 抜いた数のポイント倍率 */
	public static final int POINT_AT_PICK = 10;
	/** 抜け落ちた数のポイント倍率 */
	public static final int POINT_AT_DROP = -25;
	/** ちぎれた数のポイント倍率 */
	public static final int POINT_AT_TORN = -25;

	/** もう一度お手入れボタン描画先 */
	Rect doDstArea = new Rect(40, 1040, 40 + 400, 1040 + 200);
	/** メニューへボタン描画先 */
	private Rect backDstArea = new Rect(40 + 400 + 30, 1040, 40 + 400 + 10 + 200, 1020 + 200);

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;

	/** 挑戦したミッション */
	private Mission mis;
	/** 挑戦したミッションのインデックス */
	private int misIndex;

	/** 使用したアイテムのインデックス */
	private int useItemIndex;
	/** 新たなアイテムが開放されたかどうかのフラグ */
	private boolean itemUnlockFlag = false;

	/** 各スコアを格納 */
	private Scores sc;
	/** お手入れの総合判定結果 */
	private int result;
	/** 最終的な獲得ポイント */
	private int finallyPoint = 0;

	/** リザルト画面を表示し始めてからの経過時間 */
	private float pastTime;
	/** 個別結果を表示する際の効果音を鳴らしたかどうかのフラグ */
	private boolean[] isSoundPlayed = new boolean[]{false, false, false, false};

	/** 広告表示をしたかのフラグ */
	private boolean isAdShown;

	/** ResultScreenを生成する */
	public ResultScreen(Game game, int misIndex, int useItemIndex, Scores sc) {
		super(game);
		this.game = game;
		this.misIndex = misIndex;
		this.useItemIndex = useItemIndex;
		this.sc = sc;

		gra = game.getGraphics();
		txt = game.getText();
		mis = (Mission)Assets.mission_list.get(misIndex);

		//広告表示判定。表示確率はユーザーにストレスを与えない程度に抑える
		//広告データ受信に時間がかかるので、メソッドの先頭で処理している
		Random rand = new Random();
		if(rand.nextInt(100) < 40){
			isAdShown = true;
			((MayuGame)game).adActivityPrepare();
		}else{
			isAdShown = false;
		}

		//固有グラフィックの読み込み
		Assets.result_bg = gra.newPixmap("others/result_bg.jpg", PixmapFormat.RGB565);

		//総合結果を判定
		finallyPoint = sc.mp + sc.pickCount * POINT_AT_PICK + sc.dropCount * POINT_AT_DROP + sc.tornCount * POINT_AT_TORN;
		if (finallyPoint >= mis.NEED_SCORE) {
			result = UserData.MIS_EXCELLENT;
		}else if (finallyPoint >= mis.NEED_SCORE * mis.BORDER) {
			result = UserData.MIS_GOOD;
		}else{
			result = UserData.MIS_BAD;
		}

		//ミッションクリア（結果が失敗以外）の場合、開放されるアイテムがあるか判定
		if(result != UserData.MIS_BAD){
			for(int i = 0; i < Assets.item_list.size(); i++){
				if(misIndex == Assets.item_list.get(i).UNLOCK_NEED && Assets.ud.getItemState(i) == -1){
					Assets.ud.itemUnlock(i);
					itemUnlockFlag = true;
				}
			}
		}

		//データを保存
		if(result > Assets.ud.getMisState(misIndex)){
			Assets.ud.setMisResult(misIndex, result);
		}
		if(useItemIndex != -1){
			Assets.ud.setUseItemIndex(useItemIndex);
		}
		if(finallyPoint > 0){
			Assets.ud.setTotalMP(finallyPoint);
		}
		Assets.ud.addPickCount(sc.pickCount);
		Assets.ud.addDropCount(sc.dropCount);
		Assets.ud.addTornCount(sc.tornCount);
		Assets.ud.setTotalLength(sc.pickLength);
		Settings.save(game.getFileIO(), Assets.ud);

	}

	@Override
	public void update(float deltaTime) {

	}

	/** お手入れ結果と、必要に応じて広告を表示 */
	@Override
	public void present(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		gra.drawPixmap(Assets.result_bg, 0, 0);

		if(pastTime < 1.0f){
			drawGraphicalNumber(gra, 0, 80, 220, 250, 6);
		}else{
			soundPlayOnce(0);
			drawGraphicalNumber(gra, sc.mp, 80, 220, 250, 6);
		}

		if(pastTime >= 2.0f){
			soundPlayOnce(1);
			txt.drawText("" + sc.pickCount * POINT_AT_PICK, 470, 415, 500, Assets.map_style.get("title"));
		}
		if(pastTime >= 2.5f){
			soundPlayOnce(2);
			txt.drawText("" + sc.dropCount * POINT_AT_DROP, 470, 485, 500, Assets.map_style.get("title"));
		}
		if(pastTime >= 3.0f){
			soundPlayOnce(3);
			txt.drawText("" + sc.tornCount * POINT_AT_TORN, 470, 555, 500, Assets.map_style.get("title"));
		}

		if(pastTime < 1.0f){
			drawGraphicalNumber(gra, 0, 100, 120, 680, 6);
		}else if(pastTime < 2.0f){
			drawGraphicalNumber(gra, sc.mp, 100, 120, 680, 6);
		}else if(pastTime < 2.5f){
			drawGraphicalNumber(gra, sc.mp + sc.pickCount * POINT_AT_PICK, 100, 120, 680, 6);
		}else if(pastTime < 3.0f){
			drawGraphicalNumber(gra, sc.mp + sc.pickCount * POINT_AT_PICK + sc.dropCount * POINT_AT_DROP, 100, 120, 680, 6);
		}else{
			drawGraphicalNumber(gra, finallyPoint, 100, 120, 680, 6);
		}

		if(pastTime >= 4.5f){
			if(result == UserData.MIS_EXCELLENT){
				txt.drawText("大 成 功", 150, 950, 500, Assets.map_style.get("big"));
			}else if(result == UserData.MIS_GOOD){
				txt.drawText("成 功", 220, 950, 500, Assets.map_style.get("big"));
			}else if(result ==  UserData.MIS_BAD){
				txt.drawText("失 敗", 220, 950, 500, Assets.map_style.get("big"));
			}
		}

		if(pastTime > 6.0f && itemUnlockFlag){
			//新たなアイテムが開放された場合、通知
			gra.drawRoundRect(40, 40, 640, 1200, 15.0f, Color.argb(245,255,255,255));
			if(itemUnlockFlag){
				txt.drawText("新たなアイテムが使用できるようになりました！", 80, 550, 540, Assets.map_style.get("title"));
			}
			if(pastTime > 8.0f){
				itemUnlockFlag = false;
			}
		}else if(pastTime > 6.0f && isAdShown){
			//広告を表示
			((MayuGame)game).adActivityForward();
			isAdShown = false;
		}else if(pastTime > 7.5f){
			//タッチ判定
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(isBounds(ges,doDstArea)){
					playSound(Assets.click, 0.5f);
					game.setScreen(new CourseScreen(game, misIndex, useItemIndex));
					break;

				}else if(isBounds(ges, backDstArea)){
					playSound(Assets.click, 0.5f);
					game.setScreen(new CourseScreen(game, useItemIndex));
					break;
				}
			}
		}else{
			//ボタンをグレーアウト
			gra.drawRect(30, 1030, 660, 220, Color.rgb(204, 255, 204));
		}

		pastTime += deltaTime;
	}

	/** このスクリーンでの処理はない */
	@Override
	public void pause() {}

	/** このスクリーンでの処理はない */
	@Override
	public void resume() {}

	/** 固有の参照を明示的に切る */
	@Override
	public void dispose() {
		Assets.result_bg = null;
		Assets.icon_button_result = null;
	}

	/** 効果音を一度だけ再生するヘルパー */
	private void soundPlayOnce(int flagIndex){
		if(!isSoundPlayed[flagIndex]){
			playSound(Assets.result_score, 0.5f);
			isSoundPlayed[flagIndex] = true;
		}
	}

	@Override
	public String toString() {
		return "ResultScreen";
	}

}
