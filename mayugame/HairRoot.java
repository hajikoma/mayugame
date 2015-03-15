package com.hajikoma.mayugame;

/**
 * 毛根を表すクラス。毛根クラスには、生える毛のクラスがセットされる。
 * 生える毛のクラスは、毛根クラスを通じて栄養（deltaTime）をもらい、成長する。
 * 毛根の数×毛根の生命力（何本の毛を生やせるか）が、そのコースでのトータルの毛の数になる。
 */
public class HairRoot {

	/** 毛根の生命力 */
	private int power;
	/** 登録されている毛(コードの簡略化のためpublicにて定義) */
	public Mayu obj;
	/** 毛が登録されているかどうかのフラグ */
	private boolean isObjectSet = false;
	/** 毛が生えているかどうかのフラグ */
	private boolean isGrown = false;
	/** 座標x */
	private int x;
	/** 座標y */
	private int y;

	/**
	 * HairRootオブジェクトを生成する 毛根は動かないので、座標x,yは生成時に設定し以後不変とする
	 */
	public HairRoot(int x, int y, int power) {
		this.x = x;
		this.y = y;
		changePower(power);
	}

	/**
	 * 生える毛を登録する。毛の座標も登録する
	 * @param obj 登録するMayu
	 */
	public void setObject(Mayu obj){
		this.obj = obj;
		this.obj.setX(x);
		this.obj.setY(y);
		isObjectSet = true;
	}

	/** 毛を削除する（完全に抜けてなくなった状態にする） */
	public void deleteObject(){
		obj = null;
		isObjectSet = false;
	}

	/** 毛が登録されているかを取得する */
	public boolean isObjectSet(){
		return isObjectSet;
	}

	/**
	 * まゆ毛を成長させる。まゆ毛の高さが伸びる。
	 * @param deltaTime 成長させる値
	 */
	public void grow(float deltaTime) {
		obj.growUp(deltaTime);
		obj.setY(y - obj.getHeight());
		isGrown = true;
	}

	/** 毛を抜く */
	public void pickedUp() {
		isGrown = false;
		obj.picked();
	}

	/**
	 * 毛根の成長力を変化させる 成長力は0以下にはできない。0以下になるような値を渡した場合、0になる。
	 * @param gain 変化させる成長力
	 * @return 変化後の成長力
	 */
	public int changePower(int gain) {
		if (power + gain < 0) {
			power = 0;
		} else {
			power += gain;
		}
		return power;
	}

	/** 現在の成長力を取得する */
	public int getPower() {
		return power;
	}

	/** 毛が生えているかどうかを取得する */
	public boolean isGrown() {
		return isGrown;
	}

	/** 毛根の座標xを取得する */
	public int getX() {
		return x;
	}

	/** 毛根の座標yを取得する */
	public int getY() {
		return y;
	}

}
