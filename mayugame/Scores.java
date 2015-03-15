package com.hajikoma.mayugame;

/**
 *  お手入れに関する各スコア変数と、それらを初期化するメソッドだけを持つクラス。
 *  trimScreenとResultScreen間で受け渡しされる。
 */
public class Scores {
	/** ポイント */
	public int mp = 0;
	/** 抜いた本数 */
	public int pickCount= 0;
	/** 抜け落ちた本数 */
	public int dropCount= 0;
	/** ちぎれた本数 */
	public int tornCount= 0;
	/** 抜いた長さ */
	public int pickLength= 0;
	/** お手入れ経過時間 */
	public float gameTime= 0.0f;

	/** フィールド変数をすべて初期化する */
	public void initializeFields(){
		mp = 0;
		pickCount= 0;
		dropCount= 0;
		tornCount= 0;
		pickLength= 0;
		gameTime= 0.0f;
	}
}
