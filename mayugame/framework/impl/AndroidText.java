package com.hajikoma.mayugame.framework.impl;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.hajikoma.mayugame.framework.Text;

/**
 * Textインターフェースを実装する。
 * 文字の描画処理を行う。本ゲームではXMLレイアウトを使用しないため、
 * 文字表示のプロパティ（alignなど）を自己実装する必要があり、このクラスを作成している。
 */
public class AndroidText implements Text {

	/** スタイルを格納するリスト */
	List<Paint> textStyles;
	/** 出力に使うCanvas */
	Canvas canvas;

	/**
	 * AndroidTextインスタンスを生成する。
	 * @param frameBuffer 仮想フレームバッファ
	 */
	public AndroidText(Bitmap frameBuffer){
		textStyles = new ArrayList<Paint>();
		this.canvas = new Canvas(frameBuffer);
	}

	/**
	 * 文字を描画する
	 * @param text 描画テキスト
	 * @param x 描画座標x
	 * @param y 描画座標y。y座標位置に文字のベースラインがくるため注意する。（文字サイズ分、下にオフセット要）
	 * @param wordWrapWidth 改行幅。x + wordWrapWidthの位置座標で自動改行する。
	 * @param style プロパティ
	 */
	@Override
	public void drawText(String text, int x, int y, int wordWrapWidth, Paint style) {
		float width = 0.0f;
		int startPos = 0;
		int endPos = 0;
		int line = 0;
		int length = text.length();
		float[] widths = new float[length];
		style.getTextWidths(text, widths);
		for(int i = 0; i < length; i++){
			if(width > wordWrapWidth || i == length - 1){
				if(width > wordWrapWidth){
					endPos = i;
				}else if(i == length - 1){
					endPos = i + 1;
				}
				canvas.drawText(text.substring(startPos, endPos), x, y + line * style.getTextSize(), style);
				width = 0.0f;
				startPos = i;
				line++;
			}
			width += widths[i];
		}
	}

	/**
	 * alignを指定して文字を描画する
	 * @deprecated 未実装
	 */
	@Override
	public void drawText(String text, int x, int y, int wordWrap,
			TextAlign align, Paint style) {
	}

	/**
	 * 使いまわしスタイルを取得する
	 * @param styleIndex 取得するスタイルのインデックス
	 */
	@Override
	public Paint getTextStyle(int styleIndex) {
		return textStyles.get(styleIndex);
	}


	/**
	 * TextStyleをスタイルリストに加える。
	 * @param Paint リストに加えるTextStyle
	 * @return 加えた場所（リストのインデックス）
	 */
	@Override
	public int setTextStyle(Paint style) {
		textStyles.add(style);
		return textStyles.size()-1;
	}

}
