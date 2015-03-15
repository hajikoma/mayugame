package com.hajikoma.mayugame.item;

import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * 強化型アイテムを表すクラス。特定の値を変化させることで、お手入れを有利に進めることができる。
 * フィールドのメンバはすべてfinalで定義されており、LoadingScreenで各アイテムが生成された後に値が変わることはない。
 */
public class Enhancer extends Item {

	/** Pickerのexpansionを変化させる値 */
	public final int C_PICKER_EXPANSION;
	/** Pickerのtorningを変化させる値 */
	public final int C_PICKER_TORNING;
	/** PickerのbonusParを変化させる値 */
	public final float C_PICKER_BONUS_PAR;
	/** HairRootのpowerを変化させる値 */
	public final int C_HR_POWER;
	/** HairRootに与えるenergyを変化させる値 */
	public final float C_ENERGY;

	/** EnhanceItemインスタンスを生成する。 */
	public Enhancer(ItemCategory category, ItemType type, String name,
			String explain, String info, int price, Pixmap visual, int cPickExpansion,
			int cPickTorning, float cPickBonusPar, int cHRPower, float cEnergy, float useTime,
			Rect drawArea, String unlockInfo, int unlockNeed) {

		super(category, type, name, explain, info, price, visual, useTime, drawArea, unlockInfo, unlockNeed);
		this.C_PICKER_EXPANSION = cPickExpansion;
		this.C_PICKER_TORNING = cPickTorning;
		this.C_PICKER_BONUS_PAR = cPickBonusPar;
		this.C_HR_POWER = cHRPower;
		this.C_ENERGY = cEnergy;
	}

}
