package com.hajikoma.mayugame.item;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * 特殊なアイテムであるFling(デフォルトの抜き方で、育成要素がある)のクラス。
 * Pickerクラスに、育成をするためのメソッドを追加している。
 */
public class DefaultPicker extends Picker {

	/** expansionの最大値 */
	private static int maxExpansion = 40;
	/** torningの最大値 */
	private static int maxTorning = 60;
	/** pointParの最大値 */
	private static float maxPointPar = 2.0f;


	/** DefaultPickerを生成する。内部的にはPickerクラスのコンストラクタを呼び出すのみの処理。*/
	public DefaultPicker(ItemCategory category, ItemType type, String name,
			String explain, String info, int price, Pixmap visual, int gesture,
			int expansion, int torning, float pointPar, float useTime,
			Rect drawArea, String unlockInfo, int unlockNeed) {

		super(category, type, name, explain, info, price, visual, gesture, expansion,
				torning, pointPar, useTime, drawArea, unlockInfo, unlockNeed);
	}

	/**
	 * expansionを強化する。強化後の値が上限を超える場合は強化されない。
	 * @param expansion 強化する値
	 */
	public void enExpansion(int expansion){
		if(getExpansion() + expansion <= maxExpansion){
			setExpansion(getExpansion() + expansion);
		}else{
			setExpansion(maxExpansion);
		}
	}

	/**
	 * torningを強化する。強化後の値が上限を超える場合は強化されない。
	 * @param torning 強化する値
	 */
	public void enTorning(int torning){
		if(getTorning() + torning <= maxTorning){
			setTorning(getTorning() + torning);
		}else{
			setTorning(maxTorning);
		}
	}

	/**
	 * pointParを強化する。強化後の値が上限を超える場合は強化されない。
	 * pointParはfloat型なので、演算誤差防止の為に内部ではBigDecimalクラスで計算している。
	 * @param pointPar 強化する値
	 */
	public void enPointPar(float pointPar){
		if(getPointPar() + pointPar <= maxPointPar){
			int precision = 2;
			RoundingMode rm = RoundingMode.UP;
			MathContext mc = new MathContext(precision, rm);
			BigDecimal bd1 = new BigDecimal(String.valueOf(getPointPar()));
			BigDecimal bd2 = new BigDecimal(String.valueOf(pointPar), mc);
			setPointPar(bd1.add(bd2).floatValue());
		}else{
			setPointPar(maxPointPar);
		}
	}

	/**
	 * 各パラメータが強化できるか（上限に達していないか）どうかを返す。
	 * @return 強化できる場合true,できない場合false。
	 *         配列のインデックスは0:expansion、1:torning、2:pointPar
	 */
	public boolean[] isAbleEnParam() {
		boolean[] isAbles = new boolean[3];
		if(getExpansion() < maxExpansion){
			isAbles[0] = true;
		}else{
			isAbles[0] = false;
		}
		if(getTorning() < maxTorning){
			isAbles[1] = true;
		}else{
			isAbles[1] = false;
		}
		if(getPointPar() < maxPointPar){
			isAbles[2] = true;
		}else{
			isAbles[2] = false;
		}
		return isAbles;
	}

	/**
	 * 各パラメータの強化に必要なまゆポイントを返す。
	 * @return 強化できる場合true,できない場合false。
	 *         配列のインデックスは0:expansion、1:torning、2:pointPar
	 */
	public int[] getEnCost() {
		int[] enCosts = new int[3];
		enCosts[0] = 1000 + (getExpansion() - 20) * 2000;
		enCosts[1] = 1000 + (getTorning() - 30) * 1000;
		enCosts[2] = 1000 + (int)((getPointPar() - 1.0f) * 35000);
		return enCosts;
	}

	public static int getMaxExpansion() {
		return maxExpansion;
	}

	public static void setMaxExpansion(int maxExpansion) {
		DefaultPicker.maxExpansion = maxExpansion;
	}

	public static int getMaxTorning() {
		return maxTorning;
	}

	public static void setMaxTorning(int maxTorning) {
		DefaultPicker.maxTorning = maxTorning;
	}

	public static float getMaxPointPar() {
		return maxPointPar;
	}

	public static void setMaxPointPar(float maxPointPar) {
		DefaultPicker.maxPointPar = maxPointPar;
	}

}
