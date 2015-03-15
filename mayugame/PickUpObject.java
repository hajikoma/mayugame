package com.hajikoma.mayugame;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * 「抜かれる対象物」を表す抽象クラス。将来の拡張性（毛以外のものが生える）を想定して作成。
 *  全ての「抜かれるもの」はこのクラスを継承すること。
 */
public abstract class PickUpObject {

	/** 状態を表す（生えていない段階） */
	public static final int STATE_NONE = 0;
	/** 状態を表す（生えている段階） */
	public static final int STATE_ADULT = 1;
	/** 姿形を持つ */
	private Pixmap visual;
	/** 幅 */
	private int width;
	/** 高さ */
	private int height;
	/** 座標x */
	private int x;
	/** 座標y */
	private int y;


	public PickUpObject(){}

	public PickUpObject(Pixmap visual){
		setVisual(visual);
		width = visual.getWidth();
		height = visual.getHeight();
	}

	public Pixmap getVisual() {
		return visual;
	}

	public void setVisual(Pixmap visual) {
		this.visual = visual;
	}

	/** 大きさを持つ */
	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}