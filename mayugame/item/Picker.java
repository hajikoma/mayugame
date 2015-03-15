package com.hajikoma.mayugame.item;

import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * 抜き方を変化させるアイテムのクラス。特定のジェスチャーを持ち、使用中はそのジェスチャーでまゆ毛を抜ける。
 * 各フィールドはfinalまたはreadonlyになっており、コンストラクタで値を格納して以降変更不可。
 * 特殊なアイテムであるFling(デフォルトの抜き方）は育成要素があるため、別途DefaultPickerクラスで生成する。
 */
public class Picker extends Item {

	/** まゆ毛を抜くジェスチャータイプ。コンストラクタで格納し以後不変 */
	public final int GESTURE;
	/** まゆ毛を抜く範囲の増加値（当たり判定の広さ） */
	private int expansion;
	/** まゆ毛のちぎれやすさ */
	private int torning;
	/** まゆ毛を抜いた時のポイント倍率 */
	private float pointPar;

	/** Pickerインスタンスを生成する。*/
	public Picker(ItemCategory category, ItemType type, String name,
			String explain, String info, int price, Pixmap visual, int gesture,
			int expansion, int torning, float pointPar, float useTime,
			Rect drawArea, String unlockInfo, int unlockNeed) {

		super(category, type, name, explain, info, price, visual, useTime,
				drawArea, unlockInfo, unlockNeed);
		this.GESTURE = gesture;
		this.expansion = expansion;
		this.torning = torning;
		this.pointPar = pointPar;
	}

	public int getExpansion() {
		return expansion;
	}

	protected void setExpansion(int expansion){
		this.expansion = expansion;
	}

	public int getTorning() {
		return torning;
	}

	protected void setTorning(int torning){
		this.torning = torning;
	}

	public float getPointPar() {
		return pointPar;
	}

	protected void setPointPar(float pointPar){
		this.pointPar = pointPar;
	}

}
