package com.hajikoma.mayugame.item;

import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * アイテムを表す汎用的クラス。 ここで言うアイテムとは、ユーザーが選択、使用できるものというニュアンスである。
 * よって、単なる消耗品（例：育毛剤）だけでなく、毛を抜く道具（例：毛抜き）や 毛を抜く方法（例：ダブルタップで抜く） もアイテムに含まれる。
 */
public class Item {

	/** カテゴリー */
	public final ItemCategory CATEGORY;
	/** タイプ */
	public final ItemType TYPE;
	/** 名前を持つ */
	public final String NAME;
	/** 特徴を持つ */
	public final String EXPLAIN;
	/** 使用中の説明文を持つ */
	public final String INFO;
	/** 買うのに必要なまゆポイント */
	public final int PRICE;
	/** 姿形を持つ */
	public final Pixmap VISUAL;
	/** 使用時の持続時間 */
	public final float USE_TIME;
	/** 描画領域 */
	public final Rect DRAW_AREA;
	/** アイテムの開放条件テキスト */
	public final String UNLOCK_INFO;
	/** アイテムの開放条件（いくつのミッションクリアで開放されるか） */
	public final int UNLOCK_NEED;


	/** アイテムのカテゴリーを定義する内部列挙型クラス */
	public enum ItemCategory {
		/** デフォルトの抜き方（フリック） */
		DEFAULT,
		/** 抜く系アイテム */
		PICKER,
		/** 強化系アイテム */
		ENHANCER,
		/** 特殊アイテム */
		SPECIAL;
	}

	/** アイテムのタイプを定義する内部列挙型クラス */
	public enum ItemType {
		/** デフォルトの抜き方（フリック） */
		DEFAULT,
		/** アイテム選択なし */
		NONE,
		/** レーザー脱毛器 */
		LASER,
		/** 毛抜き */
		TWEEZERS,
		/** ガムテープ */
		TAPE,
		/** カミソリ */
		RAZOR,
		/** リアップ */
		RIUP,
		/** わかめ */
		WAKAME,
		/** ザ･ワールド */
		WORLD,
		/** 酸性雨 */
		ACIDRAIN,
		/** 隔世遺伝 */
		INHERITANCE,
		/** 神の手 */
		GODHAND;
	}

	public Item(ItemCategory category, ItemType type, String name,
			String explain, String info, int price, Pixmap visual,
			float useTime, Rect drawArea, String unlockInfo, int unlockNeed) {
		this.CATEGORY = category;
		this.TYPE = type;
		this.NAME = name;
		this.EXPLAIN = explain;
		this.INFO = info;
		this.PRICE = price;
		this.VISUAL = visual;
		this.USE_TIME = useTime;
		this.DRAW_AREA = drawArea;
		this.UNLOCK_INFO = unlockInfo;
		this.UNLOCK_NEED = unlockNeed;
	}

}
