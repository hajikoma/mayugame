package com.hajikoma.mayugame;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * PickUpObjectを継承する。標準的な毛を表すクラス。
 * 毛根クラスにセットされ、毛根を通じて成長する。
 * TODO:クラス名はMayuではなく、「毛」にすべき。
 */
public class Mayu extends PickUpObject implements Cloneable {

	/** 名前 */
	public final String NAME;
	/** 特徴 */
	public final String EXPLAIN;
	/** レア度（生えやすさ）。0 -100の間で設定し、数値が小さいほど生えにくい */
	private int rare;
	/**
	 *  生えている時の最大の長さ。画像の高さとは別物なので注意。
	 *  最長で、画像の高さ - 生えている時に見えない部分（付け根の部分）になる。
	 *  基本的には不変だが、ちぎれた際には短くなる。
	 */
	private int maxLength;
	/** 生えている時には見えない付け根部分の長さ。抜かれた時、この長さを描画領域の高さに加える */
	private int rootLength;
	/** 成長力（成長力が高いと、早く伸びる） */
	private int vitality;
	/** ちぎれやすさ(値が高いと、抜くときにちぎれやすい) */
	private int weakness;
	/** 寿命(寿命がつきると、自然に抜け落ちる) */
	private float lifeSpan;
	/** 与えられた栄養の合計 */
	private float energy = 0;
	/** 抜いた時に得られるポイント */
	private int point;
	/** 抜かれているかいないかのフラグ */
	private boolean isPicked = false;
	/** ちぎれているかいないかのフラグ */
	private boolean isTorn = false;

	/**
	 * アニメーションするMayuインスタンスを生成する。
	 * 画像を登録すると同時に、全フィールド変数を設定する。
	 * @param visual 画像
	 * @param rare レア度
	 * @param width 幅
	 * @param maxLength 生えている時の最大の長さ
	 * @param rootLength 付け根部分の長さ
	 * @param vitality 成長力
	 * @param weakness ひ弱さ
	 * @param point 抜いた時に得られるまゆポイント
	 */
	public Mayu(String name, String explain, Pixmap visual, int rare,
			int width, int maxLength, int rootLength, int vitality,
			int weakness, float lifeSpan, int point) {
		this.NAME = name;
		this.EXPLAIN = explain;
		setVisual(visual);
		this.rare = rare;
		setWidth(width);
		this.maxLength = maxLength;
		this.rootLength = rootLength;
		this.vitality = vitality;
		this.weakness = weakness;
		this.lifeSpan = lifeSpan;
		this.point = point;
	}

	/**
	 * 経過時間を加える。時間経過とともにheightが変化する（伸びる）。
	 * 長くなるにつれてちぎれやすくなるため、伸びる分だけstrengthが下がる。
	 * HairRootクラスのgrowメソッドを通じて呼ばれる。
	 * @param deltaTime デルタタイム
	 */
	public void growUp(float deltaTime) {
		energy += deltaTime;
		this.setHeight((int)(energy * vitality));
		lifeSpan += -deltaTime;
	}

	/** 抜かれた状態にする */
	public void picked() {
		isPicked = true;
	}

	/** ちぎれた状態にする。短くなる分保有する栄養は少なくなる。
	 */
	public void torn() {
		isTorn = true;
		energy = energy /2;
	}

	/** 初期値状態のインスタンスのコピーを取得する（clone()の実装） */
	public Mayu clone(){
		Mayu copy = new Mayu(NAME, EXPLAIN,getVisual(), rare, getWidth(), maxLength, rootLength, vitality, weakness, lifeSpan, point);
		return copy;
	}

	/** 与えられた栄養を0にリセットする */
	public void resetEnergy(){
		energy = 0;
	}

	/**
	 * 抜いたときの得点を取得する。
	 * ちぎれていると得点は半分になる。
	 */
	public int getPoint() {
		if(isTorn){
			return (int)(point * 0.5);
		}else{
			return point;
		}
	}

	/**
	 * 高さを格納する。heightの値はmaxLengthを超えることはない。
	 * maxLengthを超える値を格納しようとした場合、heightはmaxLengthと等しい値が格納される。
	 * @param height 格納する高さ
	 */
	@Override
	public void setHeight(int height){
		if(height > maxLength){
			super.setHeight(maxLength);
		}else if(height < 0){
			super.setHeight(0);
		}else{
			super.setHeight(height);
		}
	}

	public int getRare() {
		return rare;
	}

	public void setRare(int rare) {
		this.rare = rare;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength){
		this.maxLength = maxLength;
	}

	public int getRootLength() {
		return rootLength;
	}

	public int getWeakness() {
		return weakness;
	}

	public void setWeakness(int weakness) {
		this.weakness = weakness;
	}

	public float getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(float lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public float getEnergy() {
		return energy;
	}

	public int getVitality() {
		return vitality;
	}

	public void setVitality(int vitality) {
		this.vitality = vitality;
	}

	public float getPointPar() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public boolean isPicked() {
		return isPicked;
	}

	public boolean isTorn() {
		return isTorn;
	}

	public void endTorn(){
		isTorn = false;
	}


}