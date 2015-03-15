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
import com.hajikoma.mayugame.item.Item;
import com.hajikoma.mayugame.item.Item.ItemCategory;
import com.hajikoma.mayugame.item.Picker;

/**
 * アイテム一覧、アイテム詳細画面のクラス。
 * このクラスが呼び出されるルートは、メニュー画面の「アイテム」タブクリックと、
 * コース詳細のアイテム「変更」ボタンクリックの２種類があり、それぞれでシーン定数がわかれている。
 * 呼び出され方により処理内容が一部異なるため、注意する。
 */
public class ItemScreen extends Screen {

	/** 現在のシーンを表す列挙型 */
	static enum ItemScene{
		/** シーン定数（コース詳細から使用アイテム詳細を開く） */
		TRIM_ITEM_DETAIL,
		/** シーン定数（コース詳細から使用アイテムを変更） */
		TRIM_ITEM_LIST,
		/** シーン定数（アイテム一覧） */
		ITEM_LIST,
		/** シーン定数（アイテム詳細） */
		ITEM_DETAIL,
	}

	/** 「アイテム」画面のページ定数 */
	private static final int ITEM_PAGE_COUNT = 2;
	/** １ページに表示するアイテム定数 */
	private static final int ITEM_AT_PAGE = 12;

	/** ナビゲーションボタンの描画先。0:コース 1:アイテム 2:データ */
	private Rect[] navDstArea = new Rect[]{
		new Rect(40,	1040,	40 + 200,	1040 + 200),
		new Rect(260,	1040,	260 + 200,	1040 + 200),
		new Rect(480,	1040,	480 + 200,	1040 + 200)
	};
	/** ナビゲーションボタンを覆うエリア。お手入れ画面から開かれたときに、ナビゲーションボタンを消すために使用する */
	private Rect navCoverArea = new Rect(navDstArea[0].left - 2, navDstArea[0].top - 2, navDstArea[2].right + 2, navDstArea[2].bottom + 2);
	/** 矢印描画先。0:前ページ 1:次ページ */
	private Rect[] scrollDstArea = new Rect[]{
		 new Rect(40, 900, 40 + 180, 900 + 120),
		 new Rect(720 - 40 - 180, 900, 720 - 40, 900 + 120)
	};
	/** 実行ボタン描画先 */
	private Rect doDstArea = new Rect(50, 1020, 50 + 400, 1020 + 200);
	/** 戻るボタン描画先 */
	private Rect backDstArea = new Rect(50 + 400 + 10, 1020, 50 + 400 + 10 + 200, 1020 + 200);
	/** アイテム一覧の画像描画先 */
	private Rect itemListGraDstArea = new Rect(100, 140, 100 + 160, 140 + 160);
	/** アイテム一覧でのチェックマーク描画先 */
	private Rect listCheckDstArea = new Rect(90, 120, 90 + 60, 120 + 60);
	/** アイテム詳細の画像描画先 */
	private Rect itemDetailGraDstArea = new Rect(85, 380, 85 + 180, 380 + 180);
	/** アイテム詳細でのチェックマーク描画先 */
	private Rect detailCheckDstArea = new Rect(65, 360, 65 + 80, 360 + 80);
	/** アイテム詳細の「買う」ボタン描画先 */
	private Rect buyDstArea = new Rect(300, 490, 300 + 200, 490 + 120);

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;

	/** 現在のシーン */
	private ItemScene scene;

	/** チュートリアル表示フラグ */
	private boolean isItemListTutorialShow;
	/** チュートリアル表示フラグ */
	private boolean isItemDetailTutorialShow;

	/** アイテムの開放状況 */
	private int[] itemStates;
	/** 閲覧中のアイテム */
	private Item item;
	/** 閲覧中のアイテムのindex */
	private int showItemIndex = 0;
	/** 選択中のアイテムのindex */
	private int useItemIndex;

	/** アイテムのデータの読み込み開始位置 */
	private int startPos;
	/** アイテムのデータの読み込み終了位置 */
	private int endPos;
	/** ページ番号 */
	private int pageNo = 1;

	/** コース詳細画面からアイテム詳細画面を開いたかどうかのフラグ */
	private boolean isFromChangeScene = false;
	/** 選択中のミッションのindex */
	private int misIndex;


	/** ItemScreenインスタンスを生成する */
	public ItemScreen(Game game, int useItemIndex){
		super(game);
		this.game = game;
		this.useItemIndex = useItemIndex;

		gra = game.getGraphics();
		txt = game.getText();

		itemStates = Assets.ud.getAllItemState();

		//固有グラフィックの読み込み
		Assets.item_bg = gra.newPixmap("others/item_bg1.jpg", PixmapFormat.RGB565);

		//チュートリアル表示の必要性をチェック
		isItemListTutorialShow		= checkPreferenceState("ItemListTutorial")?		false : true;
		isItemDetailTutorialShow	= checkPreferenceState("ItemDetailTutorial")?	false : true;

		scene = ItemScene.ITEM_LIST;
	}

	/** コース詳細画面から、ItemScreenインスタンスを生成する */
	public ItemScreen(Game game, int misIndex, int useItemIndex, ItemScene scene){
		this(game, useItemIndex);
		this.misIndex = misIndex;
		this.scene = scene;

		//固有グラフィックの読み込み
		if(scene == ItemScene.TRIM_ITEM_DETAIL){
			Assets.item_bg = null;
			Assets.item_bg = gra.newPixmap("others/item_bg2.jpg", PixmapFormat.RGB565);
		}
	}

	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		//シーンごとのタッチ判定処理
		switch(scene){
		case TRIM_ITEM_DETAIL:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isItemDetailTutorialShow){
						isItemDetailTutorialShow = false;
						switchPreferenceFlag("ItemDetailTutorial", true);
						break;
					}else{
						if(isFromChangeScene){
							if(isBounds(ges, buyDstArea) && hasEnoughMP(item.PRICE)){
								useItemIndex = showItemIndex;
								buy(useItemIndex);
								break;
							}

							if(isBounds(ges, doDstArea) && Assets.ud.getItemState(showItemIndex) >= 1){
								playSound(Assets.click, 0.5f);
								isFromChangeScene = false;
								useItemIndex = showItemIndex;
								game.setScreen(new TrimScreen(game, misIndex, useItemIndex, true));
								break;
							}

							if(isBounds(ges, backDstArea)){
								playSound(Assets.click, 0.5f);
								isFromChangeScene = false;
								scene = ItemScene.TRIM_ITEM_LIST;
								Assets.item_bg = null;
								Assets.item_bg = gra.newPixmap("others/item_bg1.jpg", PixmapFormat.RGB565);
								break;
							}

						}else{
							if(isBounds(ges, buyDstArea) && hasEnoughMP(item.PRICE)){
								buy(useItemIndex);
								break;
							}

							if(isBounds(ges, backDstArea)){
								playSound(Assets.click, 0.5f);
								game.setScreen(new CourseScreen(game, misIndex, useItemIndex));
								break;

							}

						}
					}
				}
			}
			break;

		case TRIM_ITEM_LIST:
		case ITEM_LIST:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isItemListTutorialShow){
						isItemListTutorialShow = false;
						switchPreferenceFlag("ItemListTutorial", true);
						break;
					}else{
						for(int j = startPos; j < endPos; j++){
							int offsetX = (j - (pageNo -1) * ITEM_AT_PAGE) % 3 * 180;
							int offsetY = (j - (pageNo -1) * ITEM_AT_PAGE) / 3 * 180;
							if(isBounds(ges, itemListGraDstArea.left + offsetX, itemListGraDstArea.top + offsetY, itemListGraDstArea.width(), itemListGraDstArea.height())){
								showItemIndex = startPos - (pageNo -1) * ITEM_AT_PAGE + j;
								playSound(Assets.click, 0.5f);
								Assets.item_bg = null;
								Assets.item_bg = gra.newPixmap("others/item_bg2.jpg", PixmapFormat.RGB565);

								if(scene == ItemScene.TRIM_ITEM_LIST){
									scene = ItemScene.TRIM_ITEM_DETAIL;
									isFromChangeScene = true;
								}else{
									scene = ItemScene.ITEM_DETAIL;
								}

								break;
							}
						}

						if(scene == ItemScene.TRIM_ITEM_LIST){
							if(isBounds(ges, navDstArea[2])){
								playSound(Assets.click, 0.5f);
								game.setScreen(new CourseScreen(game, misIndex, useItemIndex));
								break;
							}
						}else{
							if(navCheckGesture(ges)){
								break;
							}
						}

						if(scrollCheckGesture(ges)){
							break;
						}
					}
				}
			}
			break;

		case ITEM_DETAIL:
			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					if(isItemDetailTutorialShow){
						isItemDetailTutorialShow = false;
						switchPreferenceFlag("ItemDetailTutorial", true);
						break;
					}else{
						if(isBounds(ges, buyDstArea) && hasEnoughMP(item.PRICE) && Assets.ud.getItemState(showItemIndex) != -1){
							useItemIndex = showItemIndex;
							buy(useItemIndex);
							break;
						}
						if(isBounds(ges, backDstArea)){
							showItemIndex = -1;
							scene = ItemScene.ITEM_LIST;
							playSound(Assets.click, 0.5f);
							Assets.item_bg = null;
							Assets.item_bg = gra.newPixmap("others/item_bg1.jpg", PixmapFormat.RGB565);
							break;
						}
					}
				}
			}
			break;

		}

		//ページ遷移の制御
		if(pageNo <= 0){
			pageNo = ITEM_PAGE_COUNT;
		}else if(pageNo > ITEM_PAGE_COUNT){
			pageNo = 1;
		}

		//アイテムの読み込み範囲を格納
		startPos = (pageNo - 1) * ITEM_AT_PAGE;
		if(Assets.item_list.size() >= pageNo * ITEM_AT_PAGE){
			endPos = pageNo * ITEM_AT_PAGE;
		}else{
			endPos = Assets.item_list.size();
		}
	}

	@Override
	public void present(float deltaTime) {
		//共通部分の描画
		gra.drawPixmap(Assets.item_bg, 0, 0);

		//シーンごとの描画処理
		switch(scene){
		case TRIM_ITEM_DETAIL:
			//アイテム詳細の描画
			if(isFromChangeScene){
				drawItemDetail(showItemIndex);
			}else{
				drawItemDetail(useItemIndex);
			}

			//デフォルトのチェックマークとボタンの描画
			if(isFromChangeScene && Assets.ud.getItemState(showItemIndex) != -1){
				if(useItemIndex == showItemIndex){
					gra.drawPixmap(Assets.icons, detailCheckDstArea, 240, 0, 80, 80);
				}
				if(Assets.ud.getItemState(showItemIndex) >= 1){
					drawDoButton(0);
				}else{
					gra.drawRect(itemDetailGraDstArea, Color.argb(150, 150, 150, 150));
					drawDoButton(1);
				}
			}

			if(isItemDetailTutorialShow){
				showItemTutorial();
			}

			break;

		case TRIM_ITEM_LIST:
			//タブ部分を塗りつぶす
			gra.drawRect(navCoverArea, Color.argb(255,230,245,255));

			//アイテム一覧の描画
			drawItemList();
			gra.drawPixmap(Assets.icon_button, navDstArea[2], 400, 0, 200, 200);

			if(isItemListTutorialShow){
				showItemTutorial();
			}
			break;

		case ITEM_LIST:
			drawPageNo();
			drawItemList();

			if(isItemListTutorialShow){
				showItemTutorial();
			}
			break;

		case ITEM_DETAIL:
			drawItemDetail(showItemIndex);

			//デフォルトのチェックマーク描画
			if(Assets.ud.getItemState(showItemIndex) != -1 && useItemIndex == showItemIndex){
				gra.drawPixmap(Assets.icons, detailCheckDstArea, 240, 0, 80, 80);
			}

			if(isItemDetailTutorialShow){
				showItemTutorial();
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
		Assets.item_bg = null;
	}

	/** 共通のタッチイベント判定（ページ遷移制御）を行うヘルパー */
	private boolean navCheckGesture(GestureEvent ges){
		if(isBounds(ges, navDstArea[0])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new CourseScreen(game, useItemIndex));
			return true;
		}else if(isBounds(ges, navDstArea[1])){
			playSound(Assets.click, 0.5f);
			pageNo = 1;
			return true;
		}else if(isBounds(ges, navDstArea[2])){
			playSound(Assets.click, 0.5f);
			game.setScreen(new DataScreen(game, useItemIndex));
			return true;
		}
		return false;
	}

	/** 共通のタッチイベント判定（ページ遷移制御）を行うヘルパー */
	private boolean scrollCheckGesture(GestureEvent ges){
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
	 * ミッション開放、アイテム購入、抜き方強化に必要なポイントを持っているかどうかを判定する。
	 * @param cost 必要なポイント
	 * @return 持っている場合true,持っていない場合false
	 */
	public static boolean hasEnoughMP(int cost){
		if(Assets.ud.getTotalMP() >= cost){
			return true;
		}else{
			return false;
		}
	}

	/** アイテム一覧を描画するヘルパー  */
	private void drawItemList(){
		for(int i = startPos; i < endPos; i++){
			Item item = Assets.item_list.get(i);
			int offsetX = (i - (pageNo -1) * ITEM_AT_PAGE) % 3 * 180;
			int offsetY = (i - (pageNo -1) * ITEM_AT_PAGE) / 3 * 180;

			Rect iLGDA = itemListGraDstArea;
			gra.drawRoundRect(iLGDA.left + offsetX, iLGDA.top + offsetY, iLGDA.width(), iLGDA.height() + 3, 3.0f, Color.rgb(50, 50, 50));
			gra.drawPixmap(item.VISUAL, iLGDA.left + offsetX, iLGDA.top + offsetY, iLGDA.width(), iLGDA.height(), item.DRAW_AREA);

			if(itemStates[i] == -1){
				//アイテムが未開放の場合
				gra.drawRect(iLGDA.left + offsetX, iLGDA.top + offsetY, iLGDA.width(), iLGDA.height(), Color.argb(200,200,200,200));
				gra.drawPixmap(Assets.icons, iLGDA.left + offsetX + 35, iLGDA.top + offsetY + 35, 320, 0, 80, 80);
			}else{
				//アイテムが開放済みの場合
				txt.drawText("× " + String.valueOf(itemStates[i]), iLGDA.left + 100 + offsetX, iLGDA.top + 150 + offsetY, 100, Assets.map_style.get("general"));
			}

			//選択中のチェックマークの描画
			if(useItemIndex >= startPos && useItemIndex < endPos){
				offsetX = (useItemIndex - (pageNo -1) * ITEM_AT_PAGE) % 3 * 180;
				offsetY = (useItemIndex - (pageNo -1) * ITEM_AT_PAGE) / 3 * 180;
				gra.drawPixmap(Assets.icons, listCheckDstArea.left + offsetX, listCheckDstArea.top + offsetY, listCheckDstArea.width(), listCheckDstArea.height(), 240, 0, 80, 80);
			}
		}
	}

	/**
	 * アイテム詳細を描画するヘルパー
	 * @param itemIndex 描画するアイテムのインデックス
	 */
	private void drawItemDetail(int itemIndex){
		item = Assets.item_list.get(itemIndex);
		txt.drawText(item.NAME, 80, 295, 600, Assets.map_style.get("title"));
		gra.drawPixmap(item.VISUAL, itemDetailGraDstArea, item.DRAW_AREA);

		if(itemStates[itemIndex] == -1){
			gra.drawRoundRect(50, 320, 620, 690, 15.0f, Color.argb(240, 255, 204, 204));

			String unlockNeedMisName = Assets.mission_list.get(item.UNLOCK_NEED).TITLE;
			txt.drawText("アイテムは未開放です",				70, 550, 560, Assets.map_style.get("title"));
			txt.drawText("「" + unlockNeedMisName + "」",	50, 670, 560, Assets.map_style.get("title"));
			txt.drawText("をクリアしよう！",					70, 790, 550, Assets.map_style.get("title"));
		}else{
			txt.drawText("× " + String.valueOf(itemStates[itemIndex]), itemDetailGraDstArea.left + 120, itemDetailGraDstArea.top + 170, 100, Assets.map_style.get("title"));
			txt.drawText(item.PRICE + "P", 320, 425, 400, Assets.map_style.get("title"));
			txt.drawText(Assets.ud.getTotalMP() + "P)", 480, 460, 240, Assets.map_style.get("general"));

			if(!hasEnoughMP(item.PRICE)){
				gra.drawRect(buyDstArea, Color.argb(120,120,120,120));
			}

			txt.drawText(item.EXPLAIN, 80, 730, 540, Assets.map_style.get("general"));

			//アイテムの性能を表示
			if(item.CATEGORY == ItemCategory.PICKER){
				txt.drawText("" + ((Picker)item).getExpansion(),	360, 900, 300, Assets.map_style.get("general"));
				txt.drawText("" + ((Picker)item).getTorning(),		360, 945, 300, Assets.map_style.get("general"));
				txt.drawText("" + ((Picker)item).getPointPar(),		360, 990, 300, Assets.map_style.get("general"));
			}else{
				txt.drawText("-", 360, 900, 300, Assets.map_style.get("general"));
				txt.drawText("-", 360, 945, 300, Assets.map_style.get("general"));
				txt.drawText("-", 360, 990, 300, Assets.map_style.get("general"));
			}
		}
	}

	/**
	 * アイテムの購入処理を行うヘルパー
	 * @param itemIndex 買うアイテムのインデックス
	 */
	private void buy(int itemIndex){
		playSound(Assets.pay_point, 0.5f);
		Assets.ud.setItemNumber(itemIndex, 1);
		Assets.ud.setTotalMP(-item.PRICE);
		Settings.save(game.getFileIO(), Assets.ud);
	}

	/**
	 * 実行ボタンを描画するヘルパー
	 * @param srcY 描画元画像内の上からの順番
	 */
	private void drawDoButton(int srcY){
		gra.drawPixmap(Assets.icon_button, doDstArea, 0, srcY * 200, 400, 200);
	}

	/** ページ番号を描画するヘルパー  */
	private void drawPageNo(){
		txt.drawText(pageNo + "／" + ITEM_PAGE_COUNT, 300, 990, 400, Assets.map_style.get("title"));
	}

	/** シーンに応じたlist画面のチュートリアルを表示する */
	private void showItemTutorial(){
		drawTutorialBG(gra, txt);

		if(scene == ItemScene.TRIM_ITEM_LIST || scene == ItemScene.ITEM_LIST){
			txt.drawText("アイテムアイコンをタッチすると、詳細が見られるよ",								60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("ロックされているアイテムも、特定のお手入れをクリアすることで使えるようになる！",	60, 650, 580, Assets.map_style.get("title"));
		}else{
			txt.drawText("アイテムはポイントで買うことができる！",		60, 450, 580, Assets.map_style.get("title"));
			txt.drawText("難しいお手入れもアイテムを使って攻略しよう",	60, 650, 580, Assets.map_style.get("title"));
		}
	}

	@Override
	public String toString() {
		return "ItemScreen";
	}

}
