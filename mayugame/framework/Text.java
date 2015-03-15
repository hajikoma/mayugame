package com.hajikoma.mayugame.framework;

import android.graphics.Paint;

/**
 * インターフェース Text テキストの出力を行うフレームワーク。
 * スタイルを定義したTextStyle(Paintクラスを継承)をList形式で予め格納しておき、
 * 出力時に使用するTextStyleを指定する使い方を想定している。
 */
public interface Text {

	/** drawTextでalignを指定する時の値 */
	public static enum TextAlign{
		/** 左寄せ（デフォルト） */
		left,
		/** 中央ぞろえ */
		center,
		/** 右寄せ */
		right;
	}

	/**
	 * テキストを出力する。テキストが長い場合やフォントサイズが大きい場合は
	 * 文字が画面外にはみ出る可能性があるため、適切なwordWrapを指定すること。
	 * @param text 出力テキスト
	 * @param x 出力座標x
	 * @param y 出力座標y
	 * @param wordWrap 自動改行する一行の幅
	 * @param style 使用するスタイル
	 */
	public void drawText(String text, int x, int y, int wordWrap, Paint style);

	/**
	 * alignを指定してテキストを出力する。中央ぞろえ、右寄せの場合の基準は、左端がx、右端が x + wordWrapになる。
	 * 左寄せの場合は、drawText(String,int,int,int,Paint)メソッドを使用した場合と同じ結果になる。
	 * @param text 出力テキスト
	 * @param x 出力座標x
	 * @param y 出力座標y
	 * @param wordWrap 自動改行する一行の幅
	 * @param TextAlign 文字よせの指定
	 * @param style 使用するスタイル
	 */
	public void drawText(String text, int x, int y, int wordWrap, TextAlign align, Paint style);

	/** 格納されているスタイルを取得する */
	public Paint getTextStyle(int styleIndex);

	/** 新規スタイルを格納する */
	public int setTextStyle(Paint style);

}
