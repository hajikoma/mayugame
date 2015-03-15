package com.hajikoma.mayugame.framework.impl;

import android.graphics.Bitmap;

import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Pixmap;

/**
 * Pixmapインターフェースを実装する。
 * 描画画像そのものあたる。
 */
public class AndroidPixmap implements Pixmap {

	/** ラッピングする画像インスタンス */
	Bitmap bitmap;
	/** Graphicsインターフェース内で定義されている画像フォーマット */
	PixmapFormat format;

	/**
	 * 指定のフォーマットでAndroidPixmapインスタンスを生成する。
	 * @param bitmap 画像ファイル
	 * @param format 画像フォーマット
	 */
	public AndroidPixmap(Bitmap bitmap, PixmapFormat format) {
		this.bitmap = bitmap;
		this.format = format;
	}

    public Bitmap getBitmap(){
    	return bitmap;
    }

	/**
	 * 画像の幅を取得する
	 * @return 画像の幅
	 */
	@Override
	public int getWidth() {
		return bitmap.getWidth();
	}

	/**
	 * 画像の高さを取得する
	 * @return 画像の高さ
	 */
	@Override
	public int getHeight() {
		return bitmap.getHeight();
	}

	/**
	 * 画像読み込み時のフォーマットを取得する
	 * @return フォーマット
	 */
	@Override
	public PixmapFormat getFormat() {
		return format;
	}

	/** インスタンスを破棄する */
	@Override
	public void dispose() {
		bitmap.recycle();
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
