package com.hajikoma.mayugame.screen;

import java.util.List;
import java.util.Map;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.Character;
import com.hajikoma.mayugame.CharacterHandler;
import com.hajikoma.mayugame.HairRoot;
import com.hajikoma.mayugame.Mayu;
import com.hajikoma.mayugame.Mission;
import com.hajikoma.mayugame.Scores;
import com.hajikoma.mayugame.framework.Audio;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Sound;
import com.hajikoma.mayugame.framework.Text;
import com.hajikoma.mayugame.framework.Vibrate;
import com.hajikoma.mayugame.item.Enhancer;
import com.hajikoma.mayugame.item.Item;
import com.hajikoma.mayugame.item.Item.ItemCategory;
import com.hajikoma.mayugame.item.Item.ItemType;
import com.hajikoma.mayugame.item.Picker;
import com.hajikoma.mayugame.item.SpecialItem;

/**
 * ゲームのメインスクリーン。ミッション内容、使用アイテムに応じてお手入れを行う。
 * アイテム個別の処理は、メンテナンス性を考慮し、原則各タイミングで呼び出されるアイテム処理用メソッドに記述する。
 * TODO:変数のスコープをできるだけ小さくする
 */
public class TrimScreen extends Screen {

	/** 現在のシーンを表す列挙型 */
	private static enum Scene{
		/** お手入れ開始準備、セットアップに使用 */
		READY,
		/** お手入れ開始直後、カウントダウンに使用 */
		START,
		/** お手入れ中 */
		PLAYING,
		/** 一時停止 */
		PAUSE,
		/** ゲームオーバー */
		GAMEOVER;
	}

	/** アイテムの使用状況を表す列挙型 */
	private static enum ItemState{
		/** 未使用 */
		STANDBY,
		/** 使用中 */
		USING,
		/** 使用済 */
		USED,
		/** アイテム未選択 */
		NONE;
	}

	/** 使用する毛根の場所変数 */
	private Rect[] hrArea;

	/** キャラクターの描画に使用する矩形。画像内で描画すべき矩形の座標 */
	private Rect charaSrcArea;
	/** キャラクターの描画先 */
	private Rect charaDstArea = new Rect(430, 40, 430 + 280, 40 + 400);
	/** アイテム画像の描画先 */
	private Rect itemGraDstArea = new Rect(40, 1280 - 40 - 180, 40 + 180, 1280 - 40);
	/** アイテム使用ボタンの描画先 */
	private Rect itemUseDstArea = new Rect(40, 1280 - 40 - 180, 720 - 40, 1280 - 40);

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;
	/** Vibrateインスタンス */
	private final Vibrate vib;

	/** 汎用的に使用するタイマー */
	private float timer;
	/** ランダムな数を得るのに使用 */
	private Random random = new Random();

	/** チュートリアル表示フラグ */
	private boolean[] isTrimTutorialShow = new boolean[5];
	/** チュートリアルのデモ用の毛の描画座標y */
	int tutoMayuY = 825;

	/** 現在のシーン */
	private Scene scene;
	/** 挑戦中のミッション */
	private Mission mis;
	/** 挑戦中のミッションのインデックス */
	private int misIndex;
	/** 使用するまゆ毛MAPから読み込んだキー一覧 */
	private String[] mayuMapKeys;

	/** キャラクターハンドラー */
	private CharacterHandler ch;
	/** 現在のキャラクターのアクション */
	private int charaAction = CharacterHandler.ACTION_STAND;

	/** 各スコアを格納 */
	private Scores sc = new Scores();
	/** コンボ数を格納 */
	private int combo = 0;
	/** コンボ数の表示座標 */
	private Point comboXY = new Point();
	/** コンボ数の表示座標 */
	private float comboTime = 0.0f;
	/** 毛根の残り成長力合計 */
	private int totalPower;
	/** 毛根 */
	private HairRoot[] hr;
	/** 毛根、毛の各処理に汎用的に使用するカウンタ */
	private float[] counter;
	/** ポイントの加算、効果音再生を各毛に一度だけ行うためのフラグ */
	private boolean[] isDoneOnceCommand;
	/** 抜けた時に加算されるポイント */
	private int[] addPoint;

	/** 抜き方(抜くジェスチャー) */
	private Picker picker = Assets.default_picker;
	/** 抜かれたジェスチャーを格納 */
	private int[] pickedBy;
	/** フリック距離x,yを格納 */
	private int[] velocityX,velocityY;
	/** スワイプ距離x,yを格納 */
	private int[] distanceX,distanceY;

	/** アイテムの使用状況 */
	private ItemState itemState;
	/** アイテムを使い始めてからの経過時間 */
	private float itemTime = 0.0f;
	/** 選択中のアイテム */
	private Item item;
	/** 選択中のアイテムのインデックス */
	private int useItemIndex;
	/** 選択中のアイテムのカテゴリー */
	private ItemCategory iCate;
	/** 選択中のアイテムのタイプ */
	private ItemType iType;
	/** 選択中のアイテムの効果音 */
	private Sound itemUseSound;
	/** 強化型アイテムの効果(int型) */
	private int cPickerExpansion, cPickerTorning, cHRPower;
	/** 強化型アイテムの効果（float型） */
	private float cPickerBonusPar, cEnergy;
	/** アイテムエフェクトに使用する変化するalpha値 */
	private int[] argb = new int[]{0, 0, 0, 0};


	/**
	 * TrimScreenを生成する
	 * @param game 共通のgameアクティビティ
	 * @param misIndex 挑戦するミッションのインデックス
	 * @param useItemIndex 選択したアイテムのインデックス
	 * @param isItemUse アイテムが使用できるかどうか（選択したアイテムの所持数が0の場合、false）
	 */
	public TrimScreen(Game game, int misIndex, int useItemIndex, boolean isItemUse) {
		super(game);
		this.game = game;
		gra = game.getGraphics();
		txt = game.getText();
		vib = game.getVibrate();
		this.misIndex = misIndex;
		this.mis = Assets.mission_list.get(misIndex);
		this.useItemIndex = useItemIndex;

		//チュートリアル表示の必要性をチェック
		for(int i = 0; i < isTrimTutorialShow.length; i++){
			isTrimTutorialShow[i]	= checkPreferenceState("TrimTutorial" + i)?	false : true;
		}

		if(isTrimTutorialShow[0]){
			Assets.icon_hand = gra.newPixmap("others/icon_hand.png", PixmapFormat.ARGB4444);
		}

		//使用アイテムが選択されているかどうかを判定
		if(isItemUse){
			this.item = Assets.item_list.get(useItemIndex);
			iCate = item.CATEGORY;
			iType = item.TYPE;
			itemState = ItemState.STANDBY;
		}else{
			this.item = null;
			iType = ItemType.NONE;
			itemState = ItemState.NONE;
		}

		// キャラクターのセットアップ
		ch = new CharacterHandler(new Character(gra.newPixmap("chara/chara.png", PixmapFormat.ARGB4444)));

		//固有グラフィックと、各毛根エリアの読み込み
		switch(misIndex / 6){
		case 0:
			//かみ
			Assets.trim_bg = gra.newPixmap("others/trim_bg_head.jpg", PixmapFormat.RGB565);
			hrArea =new Rect[]{
					new Rect(280,	440, 280 + 360, 440 + 100),
					new Rect(140,	540, 140 + 250, 540 + 100),
					new Rect(390,	540, 390 + 250, 540 + 100),
					new Rect(90,	640, 90 + 270,	640 + 100),
					new Rect(360,	640, 360 + 270, 640 + 100)
				};
			break;
		case 1:
			//左まゆ
			Assets.trim_bg = gra.newPixmap("others/trim_bg_mayuL.jpg", PixmapFormat.RGB565);
			hrArea = new Rect[]{
				new Rect(40,	650,	40 + 180,	650 + 70),
				new Rect(220,	650,	220 + 180,	650 + 70),
				new Rect(340,	720,	340 + 170,	720 + 80),
				new Rect(440,	800,	440 + 170,	800 + 80),
				new Rect(530,	880,	530 + 130,	880 + 110)
			};
			break;
		case 2:
			//右まゆ
			Assets.trim_bg = gra.newPixmap("others/trim_bg_mayuR.jpg", PixmapFormat.RGB565);
			hrArea = new Rect[]{
				new Rect(250,	630,	250 + 120,	630 + 40),
				new Rect(370,	630,	370 + 120,	630 + 40),
				new Rect(235,	670,	235 + 100,	670 + 40),
				new Rect(220,	710,	220 + 80,	710 + 50)
			};
			break;
		case 3:
			//わき
			Assets.trim_bg = gra.newPixmap("others/trim_bg_waki.jpg", PixmapFormat.RGB565);
			hrArea = new Rect[]{
				new Rect(180,	460,	180 + 340,	460 + 130),
				new Rect(130,	590,	130 + 340,	590 + 130),
				new Rect(80,	720,	80 + 340,	720 + 130),
				new Rect(80,	850,	80 + 270,	850 + 130)
			};
			break;
		case 4:
			//あし
			Assets.trim_bg = gra.newPixmap("others/trim_bg_ashi.jpg", PixmapFormat.RGB565);
			hrArea =new Rect[]{
				new Rect(290, 440, 290 + 80, 440 + 85),
				new Rect(410, 440, 410 + 80, 440 + 85),
				new Rect(250, 525, 250 + 80, 525 + 100),
				new Rect(370, 525, 370 + 90, 525 + 100),
				new Rect(210, 625, 210 + 60, 625 + 120),
				new Rect(320, 625, 320 + 80, 625 + 120),
				new Rect(155, 745, 155 + 60, 745 + 100),
				new Rect(260, 745, 260 + 70, 745 + 100),
				new Rect(110, 845, 110 + 40, 845 + 100),
				new Rect(200, 845, 200 + 50, 845 + 100)
			};
			break;
		case 5:
			//全身(未実装)
			break;
		}

		Assets.onomatopee = gra.newPixmap("others/onomatopee.png", PixmapFormat.ARGB4444);

		scene = Scene.READY;
	}


	/** メインループ内で呼ばれる。ループ内のためインスタンスの生成には慎重を期すこと。*/
	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		//共通部分の描画
		gra.drawPixmap(Assets.trim_bg, 0, 0);

		charaSrcArea = ch.action(charaAction, deltaTime);
		gra.drawPixmap(ch.chara.getVisual(), charaDstArea, charaSrcArea);

		drawGraphicalNumber(gra, sc.mp,		100, 60, 100, -1);
		drawGraphicalNumber(gra, totalPower,100, 60, 300, -1);

		//アイテム使用ボタンの描画とアイテムの使用時間計測、時間によるitemStateの変化
		switch (itemState){
		case STANDBY:
			gra.drawRoundRect(itemUseDstArea, 10.0f, Color.argb(245, 200, 255, 255));
			txt.drawText(item.NAME,	240, 1130, 440, Assets.map_style.get("title"));
			txt.drawText("を使う",	240, 1210, 300, Assets.map_style.get("title"));
			break;

		case USING:
			if(itemTime < item.USE_TIME){
				//使用中のエフェクトと使用時テキストの描画
				argb[3] = gra.argbGenerator(argb[3], 15 ,200);
				for(int i = 0; i < 180; i++){
					gra.drawLine(itemUseDstArea.left, itemUseDstArea.top + i,
					itemUseDstArea.left + itemUseDstArea.width(), itemUseDstArea.top + i,
					Color.argb(200, 255, 222, argb[3] + i));
				}
				txt.drawText(item.INFO, 240, 1170, 500, Assets.map_style.get("title"));
				itemTime += deltaTime;
			}else{
				itemState = ItemState.USED;
				picker = Assets.default_picker;
				cPickerExpansion = 0;
				cPickerTorning = 0;
				cPickerBonusPar = 0.0f;
				cHRPower = 0;
				cEnergy = 0.0f;
			}
			//アイテムごとのエフェクトの描画
			itemOnEffect(deltaTime);
			break;

		case USED:
			gra.drawRoundRect(itemUseDstArea, 10.0f, Color.argb(245, 150, 150, 150));
			txt.drawText("アイテム使用済", 265, 1165, 500, Assets.map_style.get("title"));
			break;

		case NONE:
			gra.drawRoundRect(itemUseDstArea, 10.0f, Color.argb(245, 150, 150, 150));
			txt.drawText("アイテムは使えません", 115, 1165, 500, Assets.map_style.get("title"));
			break;
		}

		//選択したアイテムの描画
		if(itemState != ItemState.NONE){
			gra.drawPixmap(item.VISUAL, itemGraDstArea, item.DRAW_AREA);
		}

		//シーンごとの処理
		switch(scene){
		case READY://-----------------------------------------------------------------------------------
			//変数の初期化
			totalPower = mis.ROOT_COUNT * mis.ROOT_POWER;
			sc.initializeFields();

			int rc = mis.ROOT_COUNT;
			pickedBy	= new int[rc];
			velocityX	= new int[rc];
			velocityY	= new int[rc];
			distanceX	= new int[rc];
			distanceY	= new int[rc];
			counter		= new float[rc];
			addPoint	= new int[rc];
			isDoneOnceCommand = new boolean[rc];
			hr = new HairRoot[rc];

			//毛根の配置
			hairRootSetUp();
			mayuMapKeys = mis.USE_MAYUMAP.keySet().toArray(new String[mis.USE_MAYUMAP.size()]);

			//アイテムごとの初期化
			if(itemState != ItemState.NONE){
				itemOnReady();
			}

			scene = Scene.START;
			break;
		//-------------------------------------------------------------------------------------------------

		case START://--------------------------------------------------------------------------------------
			//チュートリアル表示
			if(isTrimTutorialShow[isTrimTutorialShow.length - 1]){
				showTrimTutorial(deltaTime);
			}else{
				//ミッション開始カウントダウン
				timer += deltaTime;
				if (timer <= 3.0f){
					txt.drawText(String.valueOf((int)(4.0f - timer)), 330, 700, 500, Assets.map_style.get("big"));
				}else if(timer <= 3.5f){
					txt.drawText("お手入れ開始", 5, 700, 620, Assets.map_style.get("big"));
				}else{
					timer = 0.0f;
					Assets.icon_hand = null;
					scene = Scene.PLAYING;
				}
			}

			//チュートリアル表示中のタッチイベントの処理
			for(int gi = 0; gi < gestureEvents.size(); gi++){
				GestureEvent ges = gestureEvents.get(gi);
				if(isTrimTutorialShow[isTrimTutorialShow.length - 1]){
					if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
						if(isTrimTutorialShow[0]){
							isTrimTutorialShow[0] = false;
						}else if(isTrimTutorialShow[1]){
							isTrimTutorialShow[1] = false;
						}else if(isTrimTutorialShow[2]){
							isTrimTutorialShow[2] = false;
						}else if(isTrimTutorialShow[3]){
							isTrimTutorialShow[3] = false;
						}else{
							isTrimTutorialShow[4] = false;

							for(int i = 0; i < isTrimTutorialShow.length; i++){
								switchPreferenceFlag("TrimTutorial" + i, true);
							}
						}

						timer = 0.0f;
					}
				}
			}

			break;
		//-------------------------------------------------------------------------------------------------

		case PLAYING://-----------------------------------------------------------------------------------
			//毛の描画
			for(int i = 0; i < mis.ROOT_COUNT; i++){
				//毛根の成長力が残っている場合、毛を生やす
				if(hr[i].getPower() >= 1){
					//生える毛が登録されていない場合、登録する。
					if(!hr[i].isObjectSet()){
						hr[i].setObject(selectObj(mis.USE_MAYUMAP));
						velocityX[i] = 0;
						velocityY[i] = 0;
						distanceX[i] = 0;
						distanceY[i] = 0;
					}
					//経過時間分成長させる
					if (!hr[i].obj.isPicked() && !hr[i].obj.isTorn()) {
						//アイテム「ワールド」使用時は加算しない
						if(iType != ItemType.WORLD){
							hr[i].grow(deltaTime + random.nextFloat() / 20 + cEnergy);
						}else{
							if(itemState == ItemState.STANDBY){
								hr[i].grow(deltaTime + random.nextFloat() / 20 + cEnergy);
							}else if(itemState == ItemState.USING){
								//成長させない
							}else if(itemState == ItemState.USED){
								//止まっていた時間分、一気に成長させる
								hr[i].grow(deltaTime + itemTime + cEnergy);
							}
						}
					}
					//現在の高さに応じて描画。アイテム「ワールド」使用中は抜けていても描画し続ける
					if(!hr[i].obj.isPicked() || (iType == ItemType.WORLD && itemState == ItemState.USING)){
						gra.drawPixmap(
							hr[i].obj.getVisual(), hr[i].obj.getX(), hr[i].obj.getY(),
							(int)hr[i].obj.getEnergy()%5 * 50,	0, 50, hr[i].obj.getHeight()
						);
					}
				}

				//毛が寿命を迎えた場合、毛は自然に抜け落ちる。（ポイントは手に入らない。）
				if(hr[i].isGrown() && hr[i].obj.getLifeSpan() <= 0){
					hr[i].pickedUp();
					hr[i].changePower(-1);
					playSound(Assets.pick_up2, 0.3f);

					//アイテム「酸性雨」使用中のみポイントが入る
					if(iType == ItemType.ACIDRAIN && itemState == ItemState.USING){
						addPoint[i] = (int)(hr[i].obj.getPoint() * (picker.getPointPar() + cPickerBonusPar) + combo * 0.1f );
					}

				}

				//毛が生えている場合の描画
				if(hr[i].isObjectSet()){
					//アイテム「ワールド」使用中は抜ける描画をしない
					if(iType == ItemType.WORLD && itemState == ItemState.USING){
						//何もしない
					}else{
						int x = hr[i].obj.getX();
						int y = hr[i].obj.getY();

						//毛がちぎれる描画
						if(hr[i].obj.isTorn()){
							if(counter[i] < 0.8f){
								//毛がちぎれて浮いている状態
								int flyDist = (int)(counter[i] * 20);
								gra.drawPixmap(hr[i].obj.getVisual(), x, y - flyDist - hr[i].obj.getHeight(), 0, 0, 50, hr[i].obj.getHeight()/2);
								//オノマトペ描画
								gra.drawPixmap(Assets.onomatopee, x - 45, y - hr[i].obj.getRootLength(), 360, 0, 180, 90);
								counter[i] += deltaTime;
							}else{
								//ちぎれる描画の終了
								hr[i].obj.endTorn();
								counter[i] = 0.0f;
							}
						}

						//毛が抜ける描画
						if ((!hr[i].obj.isTorn() && !hr[i].isGrown())) {
							if(hr[i].obj.getLifeSpan() > 0.0f){
								//抜き方毎に、抜かれた描画
								switch (pickedBy[i]){

								case GestureEvent.GESTURE_FLING:
									if(counter[i] < 0.3f){
										//毛が引っ張られている状態
										int extend = (int)(counter[i] * 90);
										gra.drawPixmap(hr[i].obj.getVisual(), x, y - extend, 0, 0, 50, hr[i].obj.getHeight() + extend);
										counter[i] += deltaTime;
									}else if(y < 1280){
										//毛が抜けて飛んでいる状態
										if(velocityX[i] > 0.0f){
											hr[i].obj.setX(x + random.nextInt(8) - random.nextInt(2));
										}else{
											hr[i].obj.setX(x - random.nextInt(8) + random.nextInt(2));
										}
										hr[i].obj.setY(y + (int)(-40 + velocityY[i] / 300 + counter[i] * 40));
										gra.drawPixmap(hr[i].obj.getVisual(), x, y - hr[i].obj.getRootLength(), 0, 0, 50, hr[i].obj.getHeight() + hr[i].obj.getRootLength());

										//キャラクターアクションの変更
										charaAction = CharacterHandler.ACTION_CRY;

										//スコアの加算と効果音の再生
										if(!isDoneOnceCommand[i]){
											addScore(i);
											playSound(Assets.pick_up1, 0.5f);
											isDoneOnceCommand[i] = true;
										}
										txt.drawText("+" + String.valueOf(addPoint[i]), 300, (int)(180 - counter[i] * 30), 500, Assets.map_style.get("title"));

										counter[i] += deltaTime;
									}else{
										pickedCompletely(i);
										isDoneOnceCommand[i] = false;
									}
									break;

								case GestureEvent.GESTURE_SCALE_IN:
								case GestureEvent.GESTURE_LONG_PRESS:
								case GestureEvent.GESTURE_SHOW_PRESS:
								case GestureEvent.GESTURE_SINGLE_TAP_UP:
									if(counter[i] < 1.0f){
										//毛が抜けて飛んでいる状態
										hr[i].obj.setY(y - (int)(20 - counter[i] * 10));
										gra.drawPixmap(hr[i].obj.getVisual(), x, y - hr[i].obj.getRootLength(), 0, 0, 50, hr[i].obj.getHeight() + hr[i].obj.getRootLength());

										//オノマトペ描画
										gra.drawPixmap(Assets.onomatopee, hr[i].getX() - 180, hr[i].getY() - hr[i].obj.getRootLength(), 0, 0, 180, 90);

										//キャラクターアクションの変更
										if(iType == ItemType.TWEEZERS){
											charaAction = CharacterHandler.ACTION_CRY;
										}else{
											charaAction = CharacterHandler.ACTION_HAPPY;
										}

										//スコアの加算、振動、効果音の再生
										if(!isDoneOnceCommand[i]){
											addScore(i);
											if(pickedBy[i] == GestureEvent.GESTURE_SCALE_IN){
												doVibrate(vib, Assets.vibShortOnce);
												playSound(itemUseSound, 0.5f);
											}else{
												doVibrate(vib, Assets.vibLongOnce);
												playSound(itemUseSound, 0.5f);

											}
											isDoneOnceCommand[i] = true;
										}
										txt.drawText("+" + String.valueOf(addPoint[i]), 300, (int)(180 - counter[i] * 30), 500, Assets.map_style.get("title"));

										counter[i] += deltaTime;
									}else{
										pickedCompletely(i);
										isDoneOnceCommand[i] = false;
									}
									break;

								case GestureEvent.GESTURE_SCROLL:
									if(iType == ItemType.TAPE){
										if(counter[i] < 1.0f){
											//毛が抜けて飛んでいる状態
											hr[i].obj.setX(x - distanceX[i] / 2);
											hr[i].obj.setY(y - distanceY[i] / 2);
											gra.drawPixmap(hr[i].obj.getVisual(), x, y - hr[i].obj.getRootLength(), 0, 0, 50, hr[i].obj.getHeight() + hr[i].obj.getRootLength());

											//オノマトペ描画
											gra.drawPixmap(Assets.onomatopee, hr[i].getX() - 65, hr[i].getY() - 90, 540, 0, 180, 90);

											//キャラクターアクションの変更
											charaAction = CharacterHandler.ACTION_CRY;

											//スコアの加算、振動、効果音の再生
											if(!isDoneOnceCommand[i]){
												addScore(i);
												doVibrate(vib, Assets.vibShortOnce);
												playSound(itemUseSound, 0.5f);
												isDoneOnceCommand[i] = true;
											}
											txt.drawText("+" + String.valueOf(addPoint[i]), 300, (int)(180 - counter[i] * 30), 500, Assets.map_style.get("title"));

											counter[i] += deltaTime;
										}else{
											pickedCompletely(i);
											isDoneOnceCommand[i] = false;
										}
										break;
									}else if(iType == ItemType.RAZOR){
										if(counter[i] < 1.0f){
											//まゆ毛が切れている状態
											hr[i].obj.setX(x - distanceX[i] / 4);
											hr[i].obj.setY(y + (int)(20 - counter[i] * 5));
											gra.drawPixmap(hr[i].obj.getVisual(), x, y, 0, 0, 50, hr[i].obj.getHeight());

											//オノマトペ描画
											gra.drawPixmap(Assets.onomatopee, hr[i].getX() - 90, hr[i].getY() - hr[i].obj.getRootLength(), 180, 0, 180, 90);

											//キャラクターアクションの変更
											charaAction = CharacterHandler.ACTION_HAPPY;

											//スコアの加算、振動、効果音の再生
											if(!isDoneOnceCommand[i]){
												addScore(i);
												doVibrate(vib, Assets.vibShortOnce);
												playSound(itemUseSound, 0.5f);
												isDoneOnceCommand[i] = true;
											}
											txt.drawText("+" + String.valueOf(addPoint[i]), 300, (int)(180 - counter[i] * 30), 500, Assets.map_style.get("title"));

											counter[i] += deltaTime;
										}else{
											pickedCompletely(i);
											isDoneOnceCommand[i] = false;
										}
										break;
									}
								}
							}else{
								//自然に抜け落ちた場合の描画
								if(y < 1280){
									//まゆ毛が落ちていく状態
									hr[i].obj.setX(x + random.nextInt(2) - random.nextInt(2));
									hr[i].obj.setY(y + (int)(deltaTime * 300));
									gra.drawPixmap(hr[i].obj.getVisual(), x, y - hr[i].obj.getRootLength(), 250, 0, 50, hr[i].obj.getHeight() + hr[i].obj.getRootLength());
									if(!isDoneOnceCommand[i]){
										//アイテム「酸性雨」使用中のみポイントが入る
										if(iType == ItemType.ACIDRAIN && itemState == ItemState.USING){
											addScore(i);
										}else{
											playSound(Assets.fall, 0.2f);
										}

										isDoneOnceCommand[i] = true;
									}

								}else{
									//まゆ毛がフレームアウトし、完全になくなった状態
									pickedCompletely(i);
									isDoneOnceCommand[i] = false;
									sc.dropCount++;
								}
							}
						}
					}
				}
			}

			//タッチイベントの処理
			for(int gi = 0; gi < gestureEvents.size(); gi++){
				GestureEvent ges = gestureEvents.get(gi);

				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP && itemState == ItemState.STANDBY && isBounds(ges, itemUseDstArea)){
					//アイテム使用ボタンが押された場合
					itemOnTouch();
					itemState = ItemState.USING;
					Assets.ud.setItemNumber(useItemIndex, -1);
				}else if(ges.type == picker.GESTURE){
					//それ以外の領域でタッチイベントが発生した場合
					for(int ri = 0; ri < mis.ROOT_COUNT; ri++){
						//毛を抜く判定。毛が生えており、タッチがその毛の域内で、かつちぎれ・ひっぱられの各モーション中以外の場合にのみ抜ける
						if(hr[ri].isGrown() && !hr[ri].obj.isTorn() && isBounds(ges, hr[ri].obj.getX(), hr[ri].obj.getY(), 100, hr[ri].obj.getHeight(), picker.getExpansion() + cPickerExpansion)){
							boolean pickedFlag = true;	//毛が抜けたかどうか

							//アイテム「ワールド」使用中はちぎれない
							if(iType == ItemType.WORLD && itemState == ItemState.USING){
								pickedFlag = true;
								playSound(Assets.pick_up1, 0.5f);
							}else if(hr[ri].obj.getWeakness() > picker.getTorning() + cPickerTorning + random.nextInt(20)){
								//ちぎれた場合
								pickedFlag = false;
								hr[ri].obj.torn();
								sc.tornCount++;
								combo = 0;
								playSound(Assets.pick_up2, 0.5f);
								int tornLength = hr[ri].obj.getHeight() / 2;
								hr[ri].obj.setY(hr[ri].obj.getY() + tornLength);
								hr[ri].obj.setHeight(tornLength);
								hr[ri].obj.setMaxLength(hr[ri].obj.getMaxLength() - tornLength);
								//短くなることで、少しちぎれにくくなる
								hr[ri].obj.setWeakness(hr[ri].obj.getWeakness() - 3);
							}else{
								pickedFlag = true;
							}

							//毛が抜けた場合の共通処理
							if(pickedFlag){
								//抜き方別の特殊な処理
								switch (picker.GESTURE){
								case GestureEvent.GESTURE_FLING:
									velocityX[ri] = (int)ges.velocityX;
									velocityY[ri] = (int)ges.velocityY;
									break;
								case GestureEvent.GESTURE_SCROLL:
									distanceX[ri] = (int)ges.distanceX;
									distanceY[ri] = (int)ges.distanceY;
									break;
								}

								hr[ri].pickedUp();
								hr[ri].changePower(-1);
								addPoint[ri] = (int)(hr[ri].obj.getPoint() * (picker.getPointPar()
										+ cPickerBonusPar) + combo * 0.1f );
								sc.pickCount++;
								sc.pickLength +=hr[ri].obj.getHeight();
								pickedBy[ri] = picker.GESTURE;

								//コンボの処理
								combo++;
								comboTime = 0.0f;
								comboXY.x = 50 + random.nextInt(400);
								comboXY.y = 450 + random.nextInt(550);
							}
						}
					}
				}
			}

			//アイテム「ワールド」使用後は、時を止めていた時間をリセットする
			if(iType == ItemType.WORLD && itemState == ItemState.USED){
				itemTime = 0.0f;
			}

			//コンボ数を表示
			if(comboTime >= 1.0f){
				combo = 0;
			}
			if(combo > 1){
				if(iType == ItemType.WORLD && itemState == ItemState.USING){
					//何もしない
				}else{
					txt.drawText(String.valueOf(combo) + "combo!", comboXY.x, comboXY.y, 500, Assets.map_style.get("score"));
				}
			}

			//ゲームオーバーの判定
			if(totalPower == 0){
				scene = Scene.GAMEOVER;
			}

			sc.gameTime += deltaTime;
			comboTime += deltaTime;
			break;
		//-------------------------------------------------------------------------------------------------

		case PAUSE://-----------------------------------------------------------------------------------
			//再開待機画面
			gra.drawRoundRect(40, 40, 640, 1200, 15.0f, Color.argb(230, 255, 204, 204));
			txt.drawText("画面タッチで再開！", 140, 650, 500, Assets.map_style.get("title"));

			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					scene = Scene.PLAYING;
				}
			}
			break;
		//-------------------------------------------------------------------------------------------------

		case GAMEOVER://-----------------------------------------------------------------------------------
			//お手入れ終了表示
			if (timer <= 3.0f){
				txt.drawText("お手入れ終了", 5, 700, 620, Assets.map_style.get("big"));
				timer += deltaTime;
			}else{
				game.setScreen(new ResultScreen(game, misIndex, useItemIndex, sc));
			}
			break;
		//-------------------------------------------------------------------------------------------------

		}
	}

	/** アイテムごとの処理（シーン：READYで呼ばれる。開始時に自動的に実行する内容。） */
	private boolean itemOnReady(){
		switch(iCate){
		case PICKER:
			item = (Picker) item;
			break;
		case ENHANCER:
			item = (Enhancer) item;
			break;
		case SPECIAL:
			item = (SpecialItem) item;
			break;
		default:
			break;
		}

		Audio aud = game.getAudio();
		itemUseSound = aud.newSound("sound/strange_wave.mp3");

		switch(iType){
		case INHERITANCE:
			itemState = ItemState.USING;
			Assets.ud.setItemNumber(useItemIndex, -1);

			cHRPower = ((Enhancer) item).C_HR_POWER;
			cPickerBonusPar = ((Enhancer) item).C_PICKER_BONUS_PAR;
			for(int i = 0; i < hr.length; i++){
				hr[i].changePower(cHRPower);
				totalPower += cHRPower;
			}
			doVibrate(vib, Assets.vibShortOnce);
			break;
		case ACIDRAIN:
			itemUseSound = aud.newSound("sound/shower.mp3");
			break;
		case LASER:
			itemUseSound = aud.newSound("sound/laser3.mp3");
			break;
		case RAZOR:
			itemUseSound = aud.newSound("sound/camera1.mp3");
			break;
		case TAPE:
			itemUseSound = aud.newSound("sound/magic_tape.mp3");
			break;
		case TWEEZERS:
			itemUseSound = aud.newSound("sound/pushing_a_key.mp3");
			break;
		case WORLD:
			itemUseSound = aud.newSound("sound/snapping3.mp3");
			break;
		default:
			break;
		}

		return true;
	}

	/** アイテムごとの処理（インフォボックス描画時） */
	private boolean itemOnEffect(float deltaTime){
		switch(iCate){
		case PICKER:
			break;
		case ENHANCER:
			switch(iType){
			case RIUP:
				txt.drawText(
					"+" + String.valueOf(((Enhancer) item).C_HR_POWER * hr.length),
					300, (int)(280 - itemTime * 30), 500, Assets.map_style.get("title")
				);
				break;
			case INHERITANCE:
				txt.drawText(
					String.valueOf(((Enhancer) item).C_HR_POWER * hr.length),
					300, (int)(390 - itemTime * 30), 500, Assets.map_style.get("title")
				);
				break;
			case WORLD:
				//時間経過を止める（加算した分を減算）
				sc.gameTime -= deltaTime;
				if(itemTime < 1.5f){
					txt.drawText("時よ止まれッ!", 5 + (100 - (int)(itemTime * 150)), 700, 700, Assets.map_style.get("big"));
				}else if(itemTime < 3.0f){
					//何もしない
				}else if(itemTime < 5.0f){
					txt.drawText("時は動き出す", -600 + (int)(itemTime * 150), 700, 700, Assets.map_style.get("big"));
				}
				break;
			default:
			}
			break;
		case SPECIAL:
			break;
		default:
			break;
		}
		return true;
	}

	/** アイテムごとの処理（タッチ判定時、一度しか呼ばれない） */
	private boolean itemOnTouch(){
		switch(iCate){
		case PICKER:
			picker = (Picker)item;
			break;

		case ENHANCER:
			cPickerExpansion = ((Enhancer) item).C_PICKER_EXPANSION;
			cPickerTorning = ((Enhancer) item).C_PICKER_TORNING;
			cPickerBonusPar = ((Enhancer) item).C_PICKER_BONUS_PAR;
			cHRPower = ((Enhancer) item).C_HR_POWER;
			cEnergy = ((Enhancer) item).C_ENERGY;
			break;

		case SPECIAL:
			playSound(itemUseSound, 0.5f);
			break;

		default:
			break;
		}

		switch(iType){
		case RIUP:
			for(int i = 0; i < hr.length; i++){
				hr[i].changePower(cHRPower);
				totalPower += cHRPower;
			}
			doVibrate(vib, Assets.vibShortRythem, 3);
			playSound(itemUseSound, 0.5f);
			break;
		case WORLD:
			doVibrate(vib, Assets.vibLongRythem, 5);
			playSound(itemUseSound, 0.5f);
			break;
		default:
			doVibrate(vib, Assets.vibShortOnce);
			playSound(itemUseSound, 0.5f);
			break;
		}

		return true;
	}

	/** 毛根を指定座標矩形内のランダムな位置に配置するヘルパー */
	public void hairRootSetUp(){
		int index = 0;
		for(int i = 0; i < mis.ROOT_COUNT; i++){
			Rect area = hrArea[index];
			hr[i] = new HairRoot(area.left + random.nextInt(area.width()), area.top + random.nextInt(area.height()), mis.ROOT_POWER);
			counter[i] = 0;
			index++;
			if(index >= hrArea.length){
				index = 0;
			}
		}
	}

	/**
	 *  生やす毛を決めるヘルパー。 毛のインスタンスにランダムにアクセスし、レア度に応じてインスタンスのクローンを返す。
	 *  @param useMap 生やす候補となる毛のMAP
	 *  @return 生やす毛のインスタンスのクローン
	 */
	public Mayu selectObj(Map<String, Mayu> useMap){
		Mayu testMayu;
		while(true){
			testMayu = useMap.get(mayuMapKeys[random.nextInt(mayuMapKeys.length)]);
			if(testMayu.getRare() >= random.nextInt(100)){
				return testMayu.clone();
			}
		}
	}

	/**
	 * スコアを加算するヘルパー
	 * @param index 加算対象のインデックス
	 */
	private void addScore(int index) {
		sc.mp += addPoint[index];
	}

	/**
	 * 毛が抜かれた後の描画等処理が完全に終わったときに呼ばれる
	 * @param index 処理が完全に終わった対象のインデックス
	 */
	private void pickedCompletely(int index) {
		totalPower += -1;
		hr[index].deleteObject();
		counter[index] = 0.0f;
		charaAction = CharacterHandler.ACTION_STAND;
	}

	/** このスクリーンでの処理はない */
	@Override
	public void present(float deltaTime) {

	}

	/** 一時停止状態をセットする */
	@Override
	public void pause() {
		scene = Scene.PAUSE;
	}

	/** このスクリーンでの処理はない */
	@Override
	public void resume() {}

	/** 固有の参照を明示的に切る */
	@Override
	public void dispose() {
		Assets.trim_bg = null;
		Assets.onomatopee = null;
		ch = null;
		//アイテムサウンドのインスタンスを破棄する
		if(itemState != ItemState.NONE){
			itemUseSound.dispose();
		}
	}

	/** チュートリアルを表示する */
	private void showTrimTutorial(float deltaTime){
		drawTutorialBG(gra, txt);

		timer += deltaTime;

		if(isTrimTutorialShow[0]){
			txt.drawText("つぎつぎと毛が生えてくるので、フリックして抜こう！", 80, 450, 550, Assets.map_style.get("title"));

			int tutoMayuX = 330;
			int handY = 800;
			int handSrcX = 0;

			if(timer < 1.0f){
				//何もしない
			}else if(timer < 3.5f){
				handY = 800 - (int)((timer - 1.0f) * 200);

				if(handY > 725){
					handSrcX = 150;
				}else if(handY > 650){
					handSrcX = 300;
				}else if(handY > 575){
					handSrcX = 450;
				}else if(handY > 500){
					handSrcX = 600;
				}else{
					handSrcX = 600;
					handY = 500;
				}

				if(timer > 1.5f){
					tutoMayuX = 330 + (int)((timer - 1.5f) * 30);
					tutoMayuY -= (int)(20 - timer * 10);
				}

			}else if(timer < 6.0f){
				tutoMayuY = 825;
				timer = 0.0f;
			}

			gra.drawPixmap(Assets.mayu_short, tutoMayuX, tutoMayuY, 0, 0, 50, 125);
			gra.drawPixmap(Assets.icon_hand, 330, handY, handSrcX, 0, 150, 150);

		}else if(isTrimTutorialShow[1]){
			txt.drawText("アイテムを使うには、下部のアイテム欄をタッチ！", 80, 450, 550, Assets.map_style.get("title"));
			txt.drawText("毛を抜くアイテムは、使用している間だけ特定のジェスチャーで毛を抜ける。", 80, 650, 550, Assets.map_style.get("title"));

			int handX = 400;
			int handY = 1050;

			if(timer < 1.5f){
				handX = 400 - (int)((timer * 30));
				handY = 1050 + (int)((timer * 30));
			}else{
				timer = 0.0f;
			}
			gra.drawPixmap(Assets.icon_hand, handX, handY, 300, 0, 150, 150);

		}else if(isTrimTutorialShow[2]){
			txt.drawText("一定時間で毛は自然に抜け落ちる。この場合はポイントが入らない。", 80, 450, 550, Assets.map_style.get("title"));

			tutoMayuY = 650;
			int mayuSrcX = 0;
			int mayuHeight = 200;

			if(timer < 1.0f){
				//何もしない
			}else if(timer < 2.5f){
				tutoMayuY = 650 + (int)((timer - 1.0f) * 200);
				mayuSrcX = 250;
				mayuHeight = 250;
			}else{
				timer = 0.0f;
			}

			for(int i = 0; i < 3; i++){
				gra.drawPixmap(Assets.mayu_normal, 280 + i * 50, tutoMayuY, mayuSrcX, 0, 50, mayuHeight);
			}

		}else if(isTrimTutorialShow[3]){
			txt.drawText("毛はちぎれることがある。", 80, 450, 550, Assets.map_style.get("title"));
			txt.drawText("ちぎれるとポイントが減ってしまう上、コンボが途切れてしまう。", 80, 550, 550, Assets.map_style.get("title"));

			int mayuSrcY = 0;
			int mayuHeight = 100;

			if(timer < 1.0f){
				//何もしない
			}else if(timer < 2.0f){
				mayuSrcY = 50;
				int flyDist = (int)(timer * 100);
				gra.drawPixmap(Assets.mayu_short, 330, 800 + mayuSrcY - flyDist, 0, 0, 50, mayuHeight / 2);
				gra.drawPixmap(Assets.onomatopee, 330 - 55, 760, 360, 0, 180, 90);
			}else{
				timer = 0.0f;
			}

			gra.drawPixmap(Assets.mayu_short, 330, 800 + mayuSrcY, 0, mayuSrcY, 50, mayuHeight - mayuSrcY);

		}else{
			txt.drawText("アイテムを使ったり、抜く能力を強化して効率よくポイントを貯めよう！", 80, 450, 550, Assets.map_style.get("title"));
			txt.drawText("全てのコースのクリアを目指せ！", 80, 650, 550, Assets.map_style.get("title"));
		}
	}

	@Override
	public String toString() {
		return "TrimScreen";
	}

}
