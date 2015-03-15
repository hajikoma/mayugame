package com.hajikoma.mayugame.screen;

import java.util.List;

import android.graphics.Color;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.Mission;
import com.hajikoma.mayugame.Settings;
import com.hajikoma.mayugame.UserData;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;
import com.hajikoma.mayugame.item.Item;
import com.hajikoma.mayugame.screen.ItemScreen.ItemScene;

/**
 * お手入れする内容の選択画面。お手入れ部位選択→コース選択→コース詳細・お手入れ開始の
 * 一連の画面がこのクラスに含まれる。
 */
public class CourseScreen extends Screen {

	/** 現在のシーンを表す列挙型 */
	private static enum MenuScene{
		/** シーン定数（お手入れ部位一覧） */
		TRIM_PARTS,
		/** シーン定数（コース選択） */
		TRIM_COURSE,
		/** シーン定数（コース詳細） */
		TRIM_COURSE_DETAIL;
	}

	/** ナビゲーションボタンの描画先。0:コース 1:アイテム 2:データ */
	private Rect[] navDstArea = new Rect[]{
		new Rect(40,	1040,	40 + 200,	1040 + 200),
		new Rect(260,	1040,	260 + 200,	1040 + 200),
		new Rect(480,	1040,	480 + 200,	1040 + 200)
	};
	/** 各ミッション一覧の描画先 */
	private Rect partsDstArea = new Rect(40, 200, 40 + 240, 200 + 100);
	/** 各ミッション一覧の描画先 */
	private Rect courseDstArea = new Rect(60, 200, 60 + 600, 200 + 100);
	/** ミッション詳細の使用アイテム関係の描画先。0:アイテム画像 1:アイテム変更ボタン */
	private Rect[] pickByDstArea = new Rect[]{
		new Rect(85, 760, 85 + 180, 760 + 180),
		new Rect(300, 820, 300 + 200, 820 + 120)
	};
	/** 詳細画面の実行ボタン描画先 */
	private Rect doDstArea = new Rect(50, 1020, 50 + 400, 1020 + 200);
	/** 詳細画面の戻るボタン描画先 */
	private Rect backDstArea = new Rect(470, 1020, 470 + 200, 1020 + 200);
	/** ミッションロック時のポップアップ描画先 */
	private Rect lockDstArea = new Rect(45, 690, 675, 1235);

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;

	/** チュートリアル表示フラグ */
	private boolean isPartsTutorialShow;
	/** チュートリアル表示フラグ */
	private boolean isCourseTutorialShow;
	/** チュートリアル表示フラグ */
	private boolean isDetailTutorialShow;

	/** 現在のシーン */
	private MenuScene scene;
	/** ミッションの達成状況 */
	private int[] misStates;
	/** 選択中の部位のindex。-1の時はなにもクリックされていない状態を示す */
	private int partsIndex = -1;
	/** 選択中のコースのindex。-1の時はなにもクリックされていない状態を示す */
	private int courseIndex = -1;
	/** 選択中のミッションのindex。-1の時はなにもクリックされていない状態を示す */
	private int misIndex = -1;
	/** 選択中のミッション */
	private Mission mis;
	/** 選択中の部位を表示するための文字列 */
	String[] partsStr = {"かみ","左まゆ","右まゆ","わき","あし"};

	/** 閲覧中のアイテム */
	private Item item;
	/** 選択中のアイテムのindex */
	private int useItemIndex;


	/** MenuScreenインスタンスを生成する */
	public CourseScreen(Game game, int useItemIndex){
		super(game);
		this.game = game;
		this.useItemIndex = useItemIndex;

		gra = game.getGraphics();
		txt = game.getText();

		misStates = Assets.ud.getAllMisState();

		//固有グラフィックの読み込み
		if(misIndex == -1){
			Assets.course_bg1 = gra.newPixmap("others/course_bg1.jpg", PixmapFormat.RGB565);
		}

		//チュートリアル表示の必要性をチェック
		isPartsTutorialShow		= checkPreferenceState("PartsTutorial")?	false : true;
		isCourseTutorialShow	= checkPreferenceState("CourseTutorial")?	false : true;
		isDetailTutorialShow	= checkPreferenceState("DetailTutorial")?	false : true;

		scene = MenuScene.TRIM_PARTS;
	}

	/** MenuScreenインスタンスを生成し、ミッション詳細画面を直接開く */
	public CourseScreen(Game game, int misIndex, int useItemIndex){
		this(game, useItemIndex);
		this.misIndex = misIndex;
		partsIndex = misIndex / 6;
		courseIndex = misIndex % 6;

		//固有グラフィックの読み込み
		Assets.course_bg3 = gra.newPixmap("others/course_bg3.jpg", PixmapFormat.RGB565);

		scene = MenuScene.TRIM_COURSE_DETAIL;
	}


	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		//シーンごとのタッチ判定処理
		switch(scene){
		case TRIM_PARTS:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isPartsTutorialShow){
						isPartsTutorialShow = false;
						switchPreferenceFlag("PartsTutorial", true);
						break;
					}else{
						for(int j = 0; j < 5; j++){
							if(isBounds(ges, partsDstArea.left, partsDstArea.top + j * 140, partsDstArea.width(), partsDstArea.height())){
								partsIndex = j;
								scene = MenuScene.TRIM_COURSE;
								playSound(Assets.click, 0.5f);
								Assets.course_bg1 = null;
								Assets.course_bg2 = gra.newPixmap("others/course_bg2.jpg", PixmapFormat.RGB565);
								break;
							}
						}
						if(commonCheckGesture(ges)){
							break;
						}
					}
				}
			}
			break;

		case TRIM_COURSE:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isCourseTutorialShow){
						isCourseTutorialShow = false;
						switchPreferenceFlag("CourseTutorial", true);
						break;
					}else{
						for(int j = 0; j < 6; j++){
							if(isBounds(ges, courseDstArea.left, courseDstArea.top + j * 140, courseDstArea.width(), courseDstArea.height())){
								courseIndex = j;
								misIndex = partsIndex * 6 + courseIndex;
								scene = MenuScene.TRIM_COURSE_DETAIL;
								playSound(Assets.click, 0.5f);
								Assets.course_bg2 = null;
								Assets.course_bg3 = gra.newPixmap("others/course_bg3.jpg", PixmapFormat.RGB565);
								break;
							}
						}
						if(isBounds(ges, backDstArea)){
							partsIndex = -1;
							scene = MenuScene.TRIM_PARTS;
							playSound(Assets.click, 0.5f);
							Assets.course_bg2 = null;
							Assets.course_bg1 = gra.newPixmap("others/course_bg1.jpg", PixmapFormat.RGB565);
							break;
						}
					}
				}
			}
			break;

		case TRIM_COURSE_DETAIL:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isDetailTutorialShow){
						isDetailTutorialShow = false;
						switchPreferenceFlag("DetailTutorial", true);
						break;
					}else{
						if(misStates[misIndex] == UserData.MIS_LOCKED){
							//開放ボタンのタッチを取得
							if(isBounds(ges, doDstArea) && ItemScreen.hasEnoughMP(mis.UNLOCK_NEED)){
								Assets.ud.misUnLock(misIndex);
								Assets.ud.setTotalMP(-mis.UNLOCK_NEED);
								Settings.save(game.getFileIO(), Assets.ud);

								misStates[misIndex] = Assets.ud.getMisState(misIndex);
								playSound(Assets.pay_point, 0.5f);
								break;
							}
						}else{
							//アイテム欄のタッチを取得
							if(isBounds(ges, pickByDstArea[0])){
								playSound(Assets.click, 0.5f);
								game.setScreen(new ItemScreen(game, misIndex, useItemIndex, ItemScene.TRIM_ITEM_DETAIL));
								break;
							}else if(isBounds(ges, pickByDstArea[1])){
								playSound(Assets.click, 0.5f);
								game.setScreen(new ItemScreen(game, misIndex, useItemIndex, ItemScene.TRIM_ITEM_LIST));
								break;
							}
							//お手入れ開始ボタンのタッチを取得
							if(isBounds(ges, doDstArea)){
								if(Assets.ud.getItemState(useItemIndex) == 0){
									playSound(Assets.click, 0.5f);
									game.setScreen(new TrimScreen(game, misIndex, useItemIndex, false));
									break;
								}else{
									playSound(Assets.click, 0.5f);
									game.setScreen(new TrimScreen(game, misIndex, useItemIndex, true));
									break;
								}
							}
						}
						//戻るボタンのタッチを取得
						if(isBounds(ges, backDstArea)){
							courseIndex = -1;
							misIndex = -1;
							scene = MenuScene.TRIM_COURSE;
							playSound(Assets.click, 0.5f);
							Assets.course_bg3 = null;
							Assets.course_bg2 = gra.newPixmap("others/course_bg2.jpg", PixmapFormat.RGB565);
							break;
						}
					}
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void present(float deltaTime) {


		//シーンごとの描画処理
		switch(scene){
		case TRIM_PARTS:
			gra.drawPixmap(Assets.course_bg1, 0, 0);

			if(isPartsTutorialShow){
				showCourseTutorial();
			}
			break;

		case TRIM_COURSE:
			gra.drawPixmap(Assets.course_bg2, 0, 0);

			//選択中の部位を表示
			txt.drawText(partsStr[partsIndex], 100, 1200, 420, Assets.map_style.get("score"));

			//ミッション達成状況を表示
			for(int i = partsIndex * 6; i < partsIndex * 6 + 6; i++){
				int offsetY = (i - partsIndex * 6) * 140;
				switch (misStates[i]){
				case UserData.MIS_LOCKED:
					gra.drawPixmap(Assets.icons, courseDstArea.left - 20, courseDstArea.top - 20 + offsetY, 320, 0, 80 ,80);
					break;
				case UserData.MIS_UNLOCKED:
					//処理なし
					break;
				case UserData.MIS_BAD:
					gra.drawPixmap(Assets.icons, courseDstArea.left - 20, courseDstArea.top - 20 + offsetY, 160, 0, 80 ,80);
					break;
				case UserData.MIS_GOOD:
					gra.drawPixmap(Assets.icons, courseDstArea.left - 20, courseDstArea.top - 20 + offsetY, 80, 0, 80 ,80);
					break;
				case UserData.MIS_EXCELLENT:
					gra.drawPixmap(Assets.icons, courseDstArea.left - 20, courseDstArea.top - 20 + offsetY, 0, 0, 80 ,80);
					break;
				}
			}

			if(isCourseTutorialShow){
				showCourseTutorial();
			}
			break;

		case TRIM_COURSE_DETAIL:
			gra.drawPixmap(Assets.course_bg3, 0, 0);

			//ミッション内容の描画
			mis = (Mission)Assets.mission_list.get(misIndex);
			txt.drawText(mis.TITLE, 100, 290, 560, Assets.map_style.get("title"));

			//開放状況に応じた出力
			if(misStates[misIndex] == UserData.MIS_LOCKED){
				//ミッション未開放時、開放条件を表示
				gra.drawRoundRect(lockDstArea, 15.0f, Color.argb(240, 255, 204, 204));
				txt.drawText("このコースは未開放です",	80,		770, 500, Assets.map_style.get("title"));
				txt.drawText("開放に必要なポイント:",	80,		860, 500, Assets.map_style.get("general"));
				txt.drawText(mis.UNLOCK_NEED + "P",	400,	865, 280, Assets.map_style.get("title"));
				txt.drawText("(所有ポイント:" + Assets.ud.getTotalMP() + "P)", 80, 920, 500, Assets.map_style.get("general"));
				gra.drawPixmap(Assets.icon_button, doDstArea, 400, 320, 200, 120);
				gra.drawPixmap(Assets.icon_button, backDstArea, 400, 0, 200, 200);

				if(!ItemScreen.hasEnoughMP(mis.UNLOCK_NEED)){
					gra.drawRect(doDstArea, Color.argb(150, 150, 150, 150));
				}

			}else{
				//ミッション開放時、詳細、クリア条件、使用アイテム、お手入れ開始ボタンを描画
				txt.drawText(mis.DETAIL,								100, 440, 560, Assets.map_style.get("title"));
				txt.drawText(mis.NEED_SCORE + "P",						300, 575, 560, Assets.map_style.get("title"));
				txt.drawText((int)(mis.NEED_SCORE * mis.BORDER) + "P",	300, 635, 560, Assets.map_style.get("title"));

				item = Assets.item_list.get(useItemIndex);
				gra.drawPixmap(item.VISUAL, pickByDstArea[0], item.DRAW_AREA);
				txt.drawText(item.NAME, 80 + 180 + 40, 785, 400, Assets.map_style.get("general"));
				txt.drawText("×" + Assets.ud.getItemState(useItemIndex), 220, 930, 400, Assets.map_style.get("general"));

				if(Assets.ud.getItemState(useItemIndex) == 0){
					gra.drawRect(pickByDstArea[0], Color.argb(150, 150, 150, 150));
					gra.drawPixmap(Assets.icon_button, doDstArea, 0, 400, 400, 200);
				}

			}

			if(isDetailTutorialShow){
				showCourseTutorial();
			}
			break;

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
		Assets.course_bg1 = null;
		Assets.course_bg2 = null;
		Assets.course_bg3 = null;
	}

	/** 共通のタッチイベント判定（ページ遷移制御）を行うヘルパー */
	private boolean commonCheckGesture(GestureEvent ges){
		if(isBounds(ges, navDstArea[0])){
			return true;
		}else if(isBounds(ges, navDstArea[1])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new ItemScreen(game, useItemIndex));
			return true;
		}else if(isBounds(ges, navDstArea[2])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new DataScreen(game, useItemIndex));
			return true;
		}
		return false;
	}

	/** シーンに応じたチュートリアルを表示する */
	private void showCourseTutorial(){
		drawTutorialBG(gra, txt);

		if(scene == MenuScene.TRIM_PARTS){
			txt.drawText("まずはお手入れする部位を選ぼう！", 60, 450, 580, Assets.map_style.get("title"));
		}else if(scene == MenuScene.TRIM_COURSE){
			txt.drawText("次は脱毛コースを選ぼう！", 60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("選べるコースは、各コースの詳細からポイントを使って開放することで増やせるよ！", 60, 550, 580, Assets.map_style.get("title"));
		}else{
			txt.drawText("アイテムはお手入れ一回につき一つだけ使える！", 60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("使うアイテムを決めたら、お手入れ開始！", 60, 650, 580, Assets.map_style.get("title"));
		}
	}

	@Override
	public String toString() {
		return "CourseScreen";
	}

}
