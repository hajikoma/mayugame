package com.hajikoma.mayugame.screen;

import java.util.List;

import android.graphics.Color;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.Settings;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;

/** ユーザーデータのスクリーン。スコア関係の表示画面と、デフォルトPickerの強化画面の２画面 */
public class DataScreen extends Screen {

	/** ナビゲーションボタンの描画先。0:コース 1:アイテム 2:データ */
	private Rect[] navDstArea = new Rect[]{
		new Rect(40,	1040,	40 + 200,	1040 + 200),
		new Rect(260,	1040,	260 + 200,	1040 + 200),
		new Rect(480,	1040,	480 + 200,	1040 + 200)
	};
	/** 矢印描画先。0:前ページ 1:次ページ */
	private Rect[] scrollDstArea = new Rect[]{
		 new Rect(40, 900, 40 + 180, 900 + 120),
		 new Rect(720 - 40 - 180, 900, 720 - 40, 900 + 120)
	};
	/** デフォルトの抜き方の各強化ボタンの描画先 */
	private Rect[] enPickerDstArea = new Rect[]{
		new Rect(480, 390, 480 + 200, 390 + 120),
		new Rect(480, 580, 480 + 200, 580 + 120),
		new Rect(480, 770, 480 + 200, 770 + 120)
	};

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;

	/** チュートリアル表示フラグ */
	private boolean isDataPickerTutorialShow = true;
	/** チュートリアル表示フラグ */
	private boolean isDataScoreTutorialShow = true;

	/** デフォルトの抜き方の各パラメータが強化できるかどうか */
	private boolean[] isAbles = new boolean[3];
	/** デフォルトの抜き方の各パラメータの強化に必要なまゆポイント */
	private int[] enCosts = new int[3];

	/** 選択中のアイテムのindex */
	private int useItemIndex;

	/** 「データ」画面のページ定数 */
	private static final int DATA_PAGE_COUNT = 2;
	/** ページ番号 */
	private int pageNo = 1;

	/** MenuScreenインスタンスを生成する */
	public DataScreen(Game game, int useItemIndex){
		super(game);
		this.game = game;
		this.useItemIndex = useItemIndex;

		gra = game.getGraphics();
		txt = game.getText();

		isAbles = Assets.default_picker.isAbleEnParam();
		enCosts = Assets.default_picker.getEnCost();

		//固有グラフィックの読み込み
		Assets.data_bg = gra.newPixmap("others/data_bg.jpg", PixmapFormat.RGB565);

		//チュートリアル表示の必要性をチェック
		isDataPickerTutorialShow	= checkPreferenceState("DataPickerTutorial")?	false : true;
		isDataScoreTutorialShow		= checkPreferenceState("DataScoreTutorial")?	false : true;

	}

	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		if(pageNo == 1){
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isDataScoreTutorialShow){
						isDataScoreTutorialShow = false;
						switchPreferenceFlag("DataScoreTutorial", true);
						break;
					}else{
						if(commonCheckGesture(ges)){
							break;
						}
					}
				}
			}

		}else{
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isDataPickerTutorialShow){
						isDataPickerTutorialShow = false;
						switchPreferenceFlag("DataPickerTutorial", true);
						break;
					}else{
						boolean enFlag = false;
						int enCost = 0;
						for(int j = 0; j < 3; j++){
							if(isBounds(ges, enPickerDstArea[j])){
								if(isAbles[j] && hasEnoughMP(enCosts[j])){
									if(j == 0){
										Assets.default_picker.enExpansion(2);
									}else if(j == 1){
										Assets.default_picker.enTorning(2);
									}else{
										Assets.default_picker.enPointPar(0.1f);
									}
									enCost = enCosts[j];
									playSound(Assets.powerup, 0.5f);
									enFlag = true;
									break;
								}
							}
						}
						if(enFlag){
							Assets.ud.setTotalMP(-enCost);
							Assets.ud.syncUserDataWithDefaultPicker(Assets.default_picker);
							Settings.save(game.getFileIO(), Assets.ud);
							enFlag = false;
							isAbles = Assets.default_picker.isAbleEnParam();
							enCosts = Assets.default_picker.getEnCost();
						}
						if(commonCheckGesture(ges)){
							break;
						}
					}
				}
			}
		}

		//ページ遷移の制御
		if(pageNo <= 0){
			pageNo = DATA_PAGE_COUNT;
		}else if(pageNo > DATA_PAGE_COUNT){
			pageNo = 1;
		}
	}

	@Override
	public void present(float deltaTime) {
		//共通部分の描画
		gra.drawPixmap(Assets.data_bg, 0, 0);
		drawPageNo();
		drawGraphicalNumber(gra, Assets.ud.getTotalMP(), 80, 90, 215, 8);

		//シーンごとの描画処理
		if(pageNo == 1){
			//スコア画面描画
			txt.drawText("抜 い た 毛：",	60, 380, 600, Assets.map_style.get("title"));
			txt.drawText("抜け落ちた毛：",	60, 520, 600, Assets.map_style.get("title"));
			txt.drawText("ちぎれた毛：",		60, 660, 600, Assets.map_style.get("title"));
			txt.drawText("抜いた毛の長さ：",	60, 800, 600, Assets.map_style.get("title"));

			txt.drawText(Assets.ud.getPickCount() + "本",	300, 440, 300, Assets.map_style.get("title"));
			txt.drawText(Assets.ud.getDropCount() + "本",	300, 580, 300, Assets.map_style.get("title"));
			txt.drawText(Assets.ud.getTornCount() + "本",	300, 720, 300, Assets.map_style.get("title"));
			txt.drawText(Assets.ud.getTotalLength() + "mm",	300, 860, 300, Assets.map_style.get("title"));

			if(isDataScoreTutorialShow){
				showDataTutorial();
			}

		}else{
			//強化画面描画
			txt.drawText("抜	 く 広 さ ：" +	Assets.default_picker.getExpansion(),	60, 380, 600, Assets.map_style.get("title"));
			txt.drawText("ちぎれにくさ：" +	Assets.default_picker.getTorning(),		60, 570, 600, Assets.map_style.get("title"));
			txt.drawText("ポイント倍率：" +	Assets.default_picker.getPointPar(),	60, 760, 600, Assets.map_style.get("title"));

			for(int i = 0; i < 3; i++){
				gra.drawPixmap(Assets.icon_button, enPickerDstArea[i], 400, 200, 200, 120);
				if(isAbles[i]){
					txt.drawText("強化に必要なポイント", 70, 440 + i * 190, 600, Assets.map_style.get("general"));
					txt.drawText(String.valueOf(enCosts[i]) + " P", 70, 480 + i * 190, 600, Assets.map_style.get("general"));
					//強化に必要なポイントが足りない場合、強化ボタンをグレーハッチング
					if(!hasEnoughMP(enCosts[i])){
						gra.drawRect(enPickerDstArea[i], Color.argb(200, 150, 150, 150));
					}
				}else{
					//強化上限に達している場合、強化ボタンをグレーハッチング
					txt.drawText("強化上限に達しています", 70, 440 + i * 190, 600, Assets.map_style.get("general"));
					gra.drawRect(enPickerDstArea[i], Color.argb(200, 150, 150, 150));
				}
			}

			if(isDataPickerTutorialShow){
				showDataTutorial();
			}

		}
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
		Assets.data_bg = null;
	}

	/** 共通のタッチイベント判定（ページ遷移制御）を行うヘルパー */
	private boolean commonCheckGesture(GestureEvent ges){
		if(isBounds(ges, navDstArea[0])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new CourseScreen(game, useItemIndex));
			return true;
		}else if(isBounds(ges, navDstArea[1])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new ItemScreen(game, useItemIndex));
			return true;
		}else if(isBounds(ges, navDstArea[2])){
			playSound(Assets.click, 0.5f);
			pageNo = 1;
			return true;
		}

		if(isBounds(ges, scrollDstArea[0])){
			playSound(Assets.click, 0.5f);
			pageNo--;
			return true;
		}else if(isBounds(ges, scrollDstArea[1])){
			playSound(Assets.click, 0.5f);
			pageNo++;
			return true;
		}
		return false;
	}

	/**
	 * アイテム購入・デフォルトの抜き方強化に必要な分のポイントを持っているか判定する。
	 * @param cost 必要なポイント
	 * @return 持っている場合true,持っていない場合false
	 */
	private boolean hasEnoughMP(int cost){
		if(Assets.ud.getTotalMP() >= cost){
			return true;
		}else{
			return false;
		}
	}

	/** ページ番号を描画するヘルパー  */
	private void drawPageNo(){
		txt.drawText(pageNo + "／" + DATA_PAGE_COUNT, 300, 990, 400, Assets.map_style.get("title"));
	}

	/** シーンに応じたチュートリアルを表示する */
	private void showDataTutorial(){
		drawTutorialBG(gra, txt);

		if(pageNo == 1){
			txt.drawText("これまでの脱毛データが表示される！",			60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("どんどんお手入れしてポイントを貯めよう！",	60, 650, 580, Assets.map_style.get("title"));
		}else{
			txt.drawText("ポイントを使って脱毛能力を強化できる！",		60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("強化して難しいお手入れに挑戦しよう！",		60, 650, 580, Assets.map_style.get("title"));
		}
	}

	@Override
	public String toString() {
		return "DataScreen";
	}

}
