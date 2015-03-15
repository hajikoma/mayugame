package com.hajikoma.mayugame.screen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.Mayu;
import com.hajikoma.mayugame.Mission;
import com.hajikoma.mayugame.Settings;
import com.hajikoma.mayugame.UserData;
import com.hajikoma.mayugame.framework.Audio;
import com.hajikoma.mayugame.framework.FileIO;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Pixmap;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;
import com.hajikoma.mayugame.item.DefaultPicker;
import com.hajikoma.mayugame.item.Enhancer;
import com.hajikoma.mayugame.item.Item;
import com.hajikoma.mayugame.item.Item.ItemCategory;
import com.hajikoma.mayugame.item.Item.ItemType;
import com.hajikoma.mayugame.item.Picker;

/**
 * アプリ起動後、最初に使用するスクリーン。 ゲーム内で使用する画像、音楽、デフォルト設定、ユーザーデータなどを読み込む。
 * データ読み込みが完了次第、次のスクリーンに処理を移す。
 * ユーザーデータ読み込み失敗時以外、描画は行わない。
 */
public class LoadingScreen extends Screen {

	protected final Game game;
	private Graphics gra;
	private Text txt;

	/** ユーザーデータの読み込みが成功したかどうかのフラグ */
	private boolean isLoadOK;
	/** ユーザーデータの読み込み失敗時のエラーインスタンス */
	private Exception error;


	/** LoadingScreenを生成し、データをセットアップする */
	public LoadingScreen(Game game) {
		super(game);
		this.game = game;
		gra = game.getGraphics();
		txt = game.getText();
		Audio aud = game.getAudio();

		//初回起動時と二回目以降でユーザーデータのロード処理の分岐
		if(checkPreferenceState("Launched")){
			//二回目以降の処理
			try {
				Assets.ud = Settings.load(game.getFileIO());
				isLoadOK = true;
			} catch (Exception e) {
				// 読み込み失敗時は初期値を使用
				error = e;
				Assets.ud = UserData.getUserData();
				isLoadOK = false;
			}
		}else{
			//初回起動時の処理
			Assets.ud = UserData.getUserData();
			Settings.save(game.getFileIO(), Assets.ud);
			isLoadOK = true;
			switchPreferenceFlag("Launched", true);
		}


		//テスト用のUserData初期化-------------------------------
/*		int testItemState = 10;
		int[] test = Assets.ud.getAllItemState();
		for(int i = 0; i < test.length; i++){
			Assets.ud.itemUnlock(i);
			Log.d(""+i, "" + Assets.ud.setItemNumber(i, testItemState));
		}*/
		//Assets.ud.setTotalMP(111111);
		//-------------------------------テスト用のUserData初期化

		// デフォルトの抜き方（Fling）のセットアップ
		Assets.default_picker = new DefaultPicker(ItemCategory.DEFAULT,
				ItemType.DEFAULT, "", "", "", 0, Assets.item_picker1,
				GestureEvent.GESTURE_FLING, Assets.ud.getExpansion(),
				Assets.ud.getTorning(), Assets.ud.getPointPar(), 0.0f,
				new Rect(720, 0, 900, 180), "-", 0);

		//共有グラフィックの読み込み
		Assets.icons		= gra.newPixmap("others/icons.png",			PixmapFormat.ARGB4444);
		Assets.icon_button	= gra.newPixmap("others/icon_button.png",	PixmapFormat.ARGB4444);
		Assets.number		= gra.newPixmap("others/number.png",		PixmapFormat.ARGB4444);

		// サウンドの読み込み(アイテム使用時のサウンドは、trimScreenにて読み込む)
		Assets.click		= aud.newSound("sound/poka01.mp3");
		Assets.powerup		= aud.newSound("sound/powerup05.mp3");
		Assets.result_score	= aud.newSound("sound/tissue.mp3");
		Assets.pay_point	= aud.newSound("sound/coin04.mp3");

		Assets.pick_up1	= aud.newSound("sound/clap01.mp3");
		Assets.pick_up2	= aud.newSound("sound/clap02.mp3");
		Assets.fall		= aud.newSound("sound/fall.mp3");
		Assets.weak		= aud.newSound("sound/weak.mp3");

		//バイブパターンのセットアップ
		Assets.vibShortOnce	= 50;
		Assets.vibLongOnce	= 150;
		Assets.vibShortRythem	= new long[]{50,50};
		Assets.vibLongRythem	= new long[]{150,150};

		// まゆ毛インスタンスのセットアップ
		Assets.mayu_normal	= gra.newPixmap("mayu/mayu_normal.png",	PixmapFormat.ARGB4444);
		Assets.mayu_white	= gra.newPixmap("mayu/mayu_white.png",	PixmapFormat.ARGB4444);
		Assets.mayu_short	= gra.newPixmap("mayu/mayu_short.png",	PixmapFormat.ARGB4444);

		HashMap<String, HashMap<String, Mayu>> mayuMaps = new HashMap<String, HashMap<String, Mayu>>();
		mayuMaps.put("normal_easy",		createMayuMap("mayu/mayu_normal_easy.txt"));
		mayuMaps.put("normal_normal",	createMayuMap("mayu/mayu_normal_normal.txt"));
		mayuMaps.put("normal_hard",		createMayuMap("mayu/mayu_normal_hard.txt"));
		mayuMaps.put("short_easy",		createMayuMap("mayu/mayu_short_easy.txt"));
		mayuMaps.put("short_normal",	createMayuMap("mayu/mayu_short_normal.txt"));
		mayuMaps.put("short_hard",		createMayuMap("mayu/mayu_short_hard.txt"));
		mayuMaps.put("white_easy",		createMayuMap("mayu/mayu_white_easy.txt"));
		mayuMaps.put("white_normal",	createMayuMap("mayu/mayu_white_normal.txt"));
		mayuMaps.put("white_hard",		createMayuMap("mayu/mayu_white_hard.txt"));

		Assets.maps_mayu = mayuMaps;

		// ミッションインスタンスのセットアップ
		Assets.mission_list = createMissionList("mission/mission.txt");

		// アイテムインスタンスのセットアップ
		Assets.item_picker1		= gra.newPixmap("item/item_picking_tools.jpg",	PixmapFormat.RGB565);
		Assets.item_enhancer1	= gra.newPixmap("item/item_enhancer.jpg",		PixmapFormat.RGB565);

		ArrayList<Item> itemList = new ArrayList<Item>();
		itemList = createItemList("item/picker.txt", itemList);
		itemList = createItemList("item/enhancer.txt", itemList);
		Assets.item_list = itemList;

		// テキストスタイルのセットアップ
		HashMap<String, Paint> styleMap = new HashMap<String, Paint>();
		Paint general = new Paint();
		general.setTextSize(30);
		general.setAntiAlias(true);
		styleMap.put("general", general);
		Paint title = new Paint();
		title.setTextSize(50);
		title.setAntiAlias(true);
		styleMap.put("title", title);
		Paint score = new Paint();
		score.setTextSize(100);
		score.setAntiAlias(true);
		styleMap.put("score", score);
		Paint big = new Paint();
		big.setTextSize(120);
		big.setAntiAlias(true);
		styleMap.put("big", big);
		Assets.map_style = styleMap;

	}

	/** 次のスクリーンに処理を移す。 */
	@Override
	public void update(float deltaTime) {
		if(!isLoadOK){
			List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
			game.getInput().getKeyEvents();

			for(int i = 0; i < gestureEvents.size(); i++){
				GestureEvent ges = gestureEvents.get(i);
				if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
					game.setScreen(new OpeningScreen(game));
				}
			}

		}else{
			game.setScreen(new OpeningScreen(game));
		}
	}

	/** セーブデータ読み込み失敗時のみ、エラーをユーザーに通知 */
	@Override
	public void present(float deltaTime) {
		if(!isLoadOK){
			gra.drawRoundRect(new Rect(60, 300, 60 + 600, 300 + 680), 15.0f, Color.LTGRAY);
			txt.drawText("セーブデータの読み込みに失敗しました。",	80, 380, 540,	Assets.map_style.get("title"));
			txt.drawText("error:" + error.getCause(),			80, 500, 540,	Assets.map_style.get("general"));
			txt.drawText("主な原因：",							80, 680, 540,	Assets.map_style.get("general"));
			txt.drawText("・SDカードが挿さっていない、または認識されていない",	100, 730, 520,	Assets.map_style.get("general"));
			txt.drawText("・何らかの操作によりセーブデータが消去された",			100, 810, 520,	Assets.map_style.get("general"));
			txt.drawText("初期データでゲームを開始します...",					80, 920, 540,	Assets.map_style.get("general"));
		}
	}


	/**
	 * まゆ毛の設定ファイルからデータを読み込み、Mapを返すヘルパー。ファイルの記述方法は以下の通り。
	 * 一行目：ファイルの説明（どんなまゆ毛データセットかなど） 二行目：各パラメーターの項目名とデータ型
	 * 三行目以降：まゆ毛データ、パラメータの区切り記号は","(カンマ) なお一行目と二行目は読み飛ばされる。
	 * @param fileName まゆ毛データの設定ファイル
	 * @return まゆ毛のnameをキー、Mayuインスタンスを値に持つmap
	 */
	public HashMap<String, Mayu> createMayuMap(String fileName) {
		FileIO fIO = game.getFileIO();
		BufferedReader reader = null;
		HashMap<String, Mayu> map = new HashMap<String, Mayu>();
		try {
			reader = new BufferedReader(new InputStreamReader(fIO.readAsset(fileName)));
			String line;
			// 一行目、二行目は読み飛ばす
			reader.readLine();
			reader.readLine();
			//画像の登録にリフレクションを利用
			Class<Assets> clazz = Assets.class;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split(",");
				Mayu mayu = new Mayu(
					data[0], data[1], (Pixmap)(clazz.getField(data[2]).get(null)),
					Integer.parseInt(data[3]), Integer.parseInt(data[4]),
					Integer.parseInt(data[5]), Integer.parseInt(data[6]),
					Integer.parseInt(data[7]), Integer.parseInt(data[8]),
					Float.parseFloat(data[9]), Integer.parseInt(data[10])
				);
				map.put(mayu.NAME, mayu);
			}
		} catch (NullPointerException e) {
			System.out.println("ファイル内に不要な空白行が入っている可能性があります。" + e);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("パラメーター数が不足しています。" + e);
		} catch (IOException e) {
			System.out.println("ファイルの取得に失敗しました。" + e);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			System.out.println("指定のファイルが存在しません。" + e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("ファイルのクローズに失敗しました。" + e);
			}
		}
		return map;
	}

	/**
	 * ミッションの設定ファイルからデータを読み込み、Listを返すヘルパー。 ファイルの記述方法は以下の通り。
	 * 一行目：ファイルの説明（どんなミッションセットかなど） 二行目：各パラメーターの項目名とデータ型
	 * 三行目以降：ミッションデータ、パラメータの区切り記号は","(カンマ) なお一行目と二行目は読み飛ばされる。
	 * @param fileName ミッションデータの設定ファイル
	 * @return ミッションのtitleをキー、Missionインスタンスを値に持つmap
	 */
	public ArrayList<Mission> createMissionList(String fileName) {
		FileIO fIO = game.getFileIO();
		BufferedReader reader = null;
		ArrayList<Mission> list = new ArrayList<Mission>();
		try {
			reader = new BufferedReader(new InputStreamReader(fIO.readAsset(fileName)));
			String line;
			// 一行目、二行目は読み飛ばす
			reader.readLine();
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] data = line.split(",");
				Mission mission = new Mission(
					data[1], data[2], Integer.parseInt(data[3]),
					Assets.maps_mayu.get(data[4]),
					Integer.parseInt(data[5]), Integer.parseInt(data[6]),
					Integer.parseInt(data[7]), Float.parseFloat(data[8])
				);
				list.add(mission);
			}
		} catch (NullPointerException e) {
			System.out.println("ファイル内に不要な空白行が入っている可能性があります。" + e);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("パラメーター数が不足しています。" + e);
		} catch (IOException e) {
			System.out.println("ファイルの取得に失敗しました。" + e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("ファイルのクローズに失敗しました。" + e);
			}
		}
		return list;
	}

	/**
	 * アイテムの設定ファイルからデータを読み込み、Listを返すヘルパー。 ファイルの記述方法は以下の通り。
	 * 一行目：アイテムのカテゴリー 二行目：各パラメーターの項目名とデータ型
	 * 三行目以降：アイテムのデータ、パラメータの区切り記号は","(カンマ)
	 * なお一行目と二行目は読み飛ばされる。
	 * @param fileName ミッションデータの設定ファイル
	 * @param itemList データを格納するアイテムリスト
	 * @return アイテムリスト
	 */
	public ArrayList<Item> createItemList(String fileName, ArrayList<Item> itemList) {
		FileIO fIO = game.getFileIO();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fIO.readAsset(fileName)));
			String line;
			// 一行目、二行目は読み飛ばす
			reader.readLine();
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] data = line.split(",");
				Item item = null;
				//画像の登録にリフレクションを用いる
				Class<Assets> clazz = Assets.class;
				switch (ItemCategory.valueOf(data[0])){
				case PICKER:
					item = new Picker(ItemCategory.PICKER,
							ItemType.valueOf(data[1]), data[2], data[3],
							data[4], Integer.parseInt(data[5]),
							(Pixmap) (clazz.getField(data[6]).get(null)),
							Integer.parseInt(data[7]),
							Integer.parseInt(data[8]),
							Integer.parseInt(data[9]),
							Float.parseFloat(data[10]),
							Float.parseFloat(data[11]),
							new Rect(
								Integer.parseInt(data[12]),
								Integer.parseInt(data[13]),
								Integer.parseInt(data[14]),
								Integer.parseInt(data[15])),
							data[16],Integer.parseInt(data[17]));
					break;
				case ENHANCER:
					item = new Enhancer(ItemCategory.ENHANCER,
							ItemType.valueOf(data[1]), data[2], data[3],
							data[4], Integer.parseInt(data[5]),
							(Pixmap) (clazz.getField(data[6]).get(null)),
							Integer.parseInt(data[7]),
							Integer.parseInt(data[8]),
							Float.parseFloat(data[9]),
							Integer.parseInt(data[10]),
							Float.parseFloat(data[11]),
							Float.parseFloat(data[12]),
							new Rect(
								Integer.parseInt(data[13]),
								Integer.parseInt(data[14]),
								Integer.parseInt(data[15]),
								Integer.parseInt(data[16])),
							data[17],Integer.parseInt(data[18]));
					break;
				case SPECIAL:
					break;
				default:
					break;
				}
				itemList.add(item);
			}
		} catch (NullPointerException e) {
			System.out.println("ファイル内に不要な空白行が入っている可能性があります。" + e);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("パラメーター数が不足しています。" + e);
		} catch (IOException e) {
			System.out.println("ファイルの取得に失敗しました。" + e);
		} catch (NoSuchFieldException e) {
			System.out.println("NoSuchFieldException" + e);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("NumberFormatException" + e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("IllegalArgumentException" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException" + e);
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("ファイルのクローズに失敗しました。" + e);
			}
		}
		return itemList;
	}

	/** このスクリーンでの処理はない */
	@Override
	public void pause() {}

	/** このスクリーンでの処理はない */
	@Override
	public void resume() {}

	/** このスクリーンでの処理はない */
	@Override
	public void dispose() {}

	@Override
	public String toString() {
		return "LoadingScreen";
	}
}
