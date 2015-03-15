package com.hajikoma.mayugame;

import android.graphics.Point;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * キャラクターを表すクラス。
 * ゲームの状態によってさまざまなアクションをする。
 * このクラスの制御用に、haractorHandlerクラスを用意している。
 * haractorHandlerクラスを通じて制御することを推奨する。
 * NOTE:動作を表すクラスは、一部しか実装していない。これは、全動作のグラフィックを用意すると
 * androidのメモリ容量では処理しきれないため。動作を増やすなら、GPUレンダリングに切り替える必要がある。
 */
public class Character {
	/** まゆ毛の状態を表す定数（ふつう） */
	public static final int MAYU_NORMAL = 0;
	/** まゆ毛の状態を表す定数（伸びてきた） */
	public static final int MAYU_GROWING = 1;
	/** まゆ毛の状態を表す定数（ぼさぼさ） */
	public static final int MAYU_HEAVY = 2;
	/** まゆ毛の状態を表す定数（ボーボー） */
	public static final int MAYU_SHAGGY = 3;
	/** まゆ毛の状態 */
	private int mayuState;
	/** 見た目 */
	private Pixmap visual;
	/** 幅 */
	private static final int CHARA_WIDTH = 280;
	/** 高さ（身長） */
	private static final int CHARA_HEIGHT = 400;
	/** アクションの経過時間 */
	private float actionTime = 0.0f;
	/** アクション中かどうかのフラグ */
	private boolean isAction = false;
	/** 戻り値に使用する座標系。画像内で描画すべき矩形の左上座標を示す */
	Point drawPoint;

	/** Charactorを生成する */
	public Character(Pixmap visual){
		this.setVisual(visual);
		drawPoint = new Point(1, 1);
		setMayuState(MAYU_NORMAL);
	}

	/** 立っている通常状態。他のアクションが呼ばれていない間は常にこのメソッドが呼ばれる想定 */
	public Point standing(){
		if(actionTime > 2.7f){
			endCurrentAction();
		}else if (actionTime > 2.5f){
			drawPoint.x = CHARA_WIDTH;
		}else{
			drawPoint.x = 0;
			drawPoint.y = 0;
		}
		return drawPoint;
	}

	/** 喜ぶ */
	public Point happy(){
		drawPoint.y = CHARA_HEIGHT * 1;
		if(actionTime > 2.0f){
			endCurrentAction();
		}else if (actionTime > 1.0f){
			drawPoint.x = CHARA_WIDTH;
		}else{
			drawPoint.x = 0;
		}
		return drawPoint;
	}

	/** 気にする */
	public Point mind(){
		return drawPoint;
	}

	/** 驚く */
	public Point surprized(){
		return drawPoint;
	}

	/** 泣く */
	public Point cry(){
		drawPoint.y = CHARA_HEIGHT * 2;
		if(actionTime > 2.0f){
			endCurrentAction();
		}else if (actionTime > 1.0f){
			drawPoint.x = CHARA_WIDTH;
		}else{
			drawPoint.x = 0;
		}
		return drawPoint;
	}

	/** 経過時間を足す */
	public void addDelta(float deltaTime){
		actionTime += deltaTime;
	}

	/** 新規アクションを開始する */
	public void startNewAction(){
		isAction = true;
	}

	/** 実行中のアクションを終了する */
	public void endCurrentAction(){
		isAction = false;
		actionTime = 0.0f;
	}

	/** アクション中かどうかを取得する */
	public boolean isAction() {
		return isAction;
	}

	public int getMayuState() {
		return mayuState;
	}

	public void setMayuState(int mayuState) {
		this.mayuState = mayuState;
	}

	public Pixmap getVisual() {
		return visual;
	}

	public void setVisual(Pixmap visual) {
		this.visual = visual;
	}

	public int getWidth() {
		return CHARA_WIDTH;
	}

	public int getHeight() {
		return CHARA_HEIGHT;
	}

}
