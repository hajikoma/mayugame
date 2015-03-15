package com.hajikoma.mayugame.framework.impl;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Pixmap;

/**
 * Graphicsインターフェースを実装する。
 * Bitmapをフレームバッファに見立てて描画を行う
 * 描画座標の指定に際しては、座標数とフレームバッファの幅と高さの最大値が1ずれることに注意する。
 * (座標は原点0,0から始まるのに対し、幅と高さは1から始まるため)
 */
public class AndroidGraphics implements Graphics {

	/** アセットマネージャ */
	AssetManager assets;
	/** 仮想フレームバッファ */
	Bitmap frameBuffer;
	/** 仮想フレームバッファの描画を行う */
	Canvas canvas;
	/** 描画スタイル */
	Paint paint;
	Rect srcRect = new Rect();
	Rect dstRect = new Rect();
	RectF rectF = new RectF();

	/**
	 * AndroidGraphicsインスタンスを生成する。
	 * @param assets アセットマネージャ
	 * @param frameBuffer 仮想フレームバッファ
	 */
	public AndroidGraphics(AssetManager assets, Bitmap frameBuffer) {
		this.assets = assets;
		this.frameBuffer = frameBuffer;
		this.canvas = new Canvas(frameBuffer);
		this.paint = new Paint();
	}

	/**
	 * 画像ファイルをメモリに読み込む
	 * FIXME:大きな画像を読み込んだ際、OutOfMemoryが発生しやすい
	 * @param fileName 読み込む画像
	 * @param format Pixmapを生成する際のフォーマット
	 * @return フォーマット画像を登録したPixmap
	 */
	@Override
	public Pixmap newPixmap(String fileName, PixmapFormat format) {

		//System.gc();

		//カラーフォーマットの設定
		Config config = null;
		if (format == PixmapFormat.RGB565)
			config = Config.RGB_565;
		else if (format == PixmapFormat.ARGB4444)
			config = Config.ARGB_4444;
		else
			config = Config.ARGB_8888;

		Options options = new Options();
		options.inPreferredConfig = config;

		//フォーマットに従って画像を読み込み
		InputStream in = null;
		Bitmap bitmap = null;
		try {
			in = assets.open(fileName);
			bitmap = BitmapFactory.decodeStream(in);
			if (bitmap == null)
				throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		} catch (OutOfMemoryError e) {
			System.out.println("Out of Memory!" + e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

		//読み込んだ画像のフォーマットを取得（BitmapFactoryは設定を無視することがあるため）
		if (bitmap.getConfig() == Config.RGB_565)
			format = PixmapFormat.RGB565;
		else if (bitmap.getConfig() == Config.ARGB_4444)
			format = PixmapFormat.ARGB4444;
		else
			format = PixmapFormat.ARGB8888;

		return new AndroidPixmap(bitmap, format);
	}

	/**
	 * 仮想フレームバッファを塗りつぶし、画面をクリアする
	 * @param color 描画色
	 */
	@Override
	public void clear(int color) {
		canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8, (color & 0xff));
	}

	/**
	 * ピクセルを描画する
	 * @param x ピクセル座標x
	 * @param y ピクセル座標y
	 * @param color 描画色
	 */
	@Override
	public void drawPixel(int x, int y, int color) {
		paint.setColor(color);
		canvas.drawPoint(x, y, paint);
	}

	/**
	 * 線を引く
	 * @param x 始点座標x
	 * @param y 始点座標y
	 * @param x2 終点座標x
	 * @param y2 終点座標y
	 * @param color 描画色
	 */
	@Override
	public void drawLine(int x, int y, int x2, int y2, int color) {
		paint.setColor(color);
		canvas.drawLine(x, y, x2, y2, paint);
	}

	/**
	 * 四角形を描画する
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 * @param width 幅
	 * @param height 高さ
	 * @param color 描画色
	 */
	@Override
	public void drawRect(int x, int y, int width, int height, int color) {
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
	}

	/**
	 * 四角形を描画する
	 * @param rect 描画領域
	 * @param color 描画色
	 */
	@Override
	public void drawRect(Rect rect, int color) {
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		canvas.drawRect(rect, paint);
	}

	/**
	 * 角丸四角形を描画する
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 * @param width 幅
	 * @param height 高さ
	 * @param radius 角丸半径
	 * @param color 描画色
	 */
	@Override
    public void drawRoundRect(int x, int y, int width, int height, float radius, int color){
		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + width;
		dstRect.bottom = y + height;

		drawRoundRect(dstRect, radius, color);
	}


	/**
	 * 角丸四角形を描画する
	 * @param rect 描画領域
	 * @param radius 角丸半径
	 * @param color 描画色
	 */
	@Override
    public void drawRoundRect(Rect rect, float radius, int color) {
		paint.setColor(color);
		paint.setStyle(Style.FILL);

		rectF.left = (float)rect.left;
		rectF.top = (float)rect.top;
		rectF.right = (float)rect.right;
		rectF.bottom = (float)rect.bottom;

		canvas.drawRoundRect(rectF, radius, radius, paint);
	}

	/**
	 * 画像の矩形領域を描画する
	 * @param pixmap 描画画像インスタンス
	 * @param dstRect 描画先の矩形領域
	 * @param srcRect 元画像から描画する矩形領域
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, Rect dstRect, Rect srcRect) {
		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * 画像の矩形領域を描画する
	 * @param pixmap 描画画像インスタンス
	 * @param dstRect 描画先の矩形領域
	 * @param srcX 元画像から描画する矩形領域左上角座標x
	 * @param srcY 元画像から描画する矩形領域左上角座標y
	 * @param srcWidth 元画像から描画する矩形領域の高さ
	 * @param srcHeight 元画像から描画する矩形領域の幅
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, Rect dstRect, int srcX, int srcY, int srcWidth, int srcHeight) {
		srcRect.left = srcX;
		srcRect.top = srcY;
		//srcRect.right = srcX + srcWidth - 1;
		//srcRect.bottom = srcY + srcHeight - 1;
		srcRect.right = srcX + srcWidth;
		srcRect.bottom = srcY + srcHeight;

		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * 画像の矩形領域を描画する
	 * @param pixmap 描画画像インスタンス
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 * @param dstWidth 描画する矩形領域の高さ
	 * @param dstHeight 描画する矩形領域の幅
	 * @param srcRect 元画像から描画する矩形領域
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, int dstWidth, int dstHeight, Rect srcRect) {
		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + dstWidth;
		dstRect.bottom = y + dstHeight;

		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * 画像の矩形領域を拡大・縮小して描画する
	 * 拡大・縮小はコストが高いため、元画像を描画サイズに合わせて作成する方が望ましい。
	 * @param pixmap 描画画像インスタンス
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 * @param dstWidth 描画する矩形領域の高さ
	 * @param dstHeight 描画する矩形領域の幅
	 * @param srcX 元画像から描画する矩形領域左上角座標x
	 * @param srcY 元画像から描画する矩形領域左上角座標y
	 * @param srcWidth 元画像から描画する矩形領域の高さ
	 * @param srcHeight 元画像から描画する矩形領域の幅
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, int dstWidth,
			int dstHeight, int srcX, int srcY, int srcWidth, int srcHeight) {
		srcRect.left = srcX;
		srcRect.top = srcY;
		srcRect.right = srcX + srcWidth;
		srcRect.bottom = srcY + srcHeight;

		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + dstWidth;
		dstRect.bottom = y + dstHeight;

		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * 画像の矩形領域を描画する
	 * @param pixmap 描画画像インスタンス
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 * @param srcX 元画像から描画する矩形領域左上角座標x
	 * @param srcY 元画像から描画する矩形領域左上角座標y
	 * @param srcWidth 元画像から描画する矩形領域の高さ
	 * @param srcHeight 元画像から描画する矩形領域の幅
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight) {
		srcRect.left = srcX;
		srcRect.top = srcY;
		//srcRect.right = srcX + srcWidth - 1;
		//srcRect.bottom = srcY + srcHeight - 1;
		srcRect.right = srcX + srcWidth;
		srcRect.bottom = srcY + srcHeight;

		dstRect.left = x;
		dstRect.top = y;
		//dstRect.right = x + srcWidth - 1;
		//dstRect.bottom = y + srcHeight - 1;
		dstRect.right = x + srcWidth;
		dstRect.bottom = y + srcHeight;

		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * 画像を描画する
	 * @param pixmap 描画画像インスタンス
	 * @param x 左上角座標x
	 * @param y 左上各座標y
	 */
	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y) {
		canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, x, y, null);
	}

	/**
	 * 画像の透明度を変える。非常にコストの高い処理なので、レンダリングスレッド内で呼ばないこと。
	 * @param pixmap 透明度を変えるpixmap
	 * @param addAlpha 加える透明度。（0-255）
	 * @return 透明度を変えたpixmap
	 */
	public Pixmap changeAlpha(Pixmap pixmap, int addAlpha){
		// PixmapからBitmapを取得
		Bitmap bmp = pixmap.getBitmap();

		if (!bmp.isMutable()) {
		    bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
		}

		int width = bmp.getWidth();
		int height = bmp.getHeight();

		// Bitmapのピクセルデータを取得
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int y=0; y<height; y++) {
		    for (int x=0; x<width; x++) {
		        // 透過率をintとして取得
		        int a = pixels[x + y * width];
		        a = a >>> 24;

		        // 透過率の変更
		        if (a != 0) {
		            a -= addAlpha;
		            if (a < 0) {
		                a = 0;
		            }
		        }
		        a = a << 24;
		        // ピクセルの色情報から透過率の削除
		        int b = pixels[x + y * width];
		        b = b << 8; //・・・４
		        b = b >>> 8; //・・・５
		        // 透過情報と色情報の合成
		        pixels[x + y * width] = a ^ b;
		    }
		}
		// 透過度が変わったBitmapデータの作成
		bmp.setPixels(pixels, 0, width, 0, 0, width, height);
		pixmap.setBitmap(bmp);
		return pixmap;
	}

	/**
	 * 0から指定値の間をループする整数配列を取得する。
	 * 時間経過で色合いが変化するargb値として使用できる。
	 * @param step 変化する量。負の値を指定することもできる。
	 * @param limit 最大値。0-255の間で指定する。
	 * @return 0-255の整数
	 */
	@Override
	public int argbGenerator(int argb, int step, int limit){
		argb += step;
		if(argb < 0){
			argb = 255;
		}else if(argb > limit){
			argb = 0;
		}
		return argb;
	}

	/**
	 * 仮想フレームバッファの幅を取得する
	 * @return 仮想フレームバッファの幅
	 */
	@Override
	public int getWidth() {
		return frameBuffer.getWidth();
	}

	/**
	 * 仮想フレームバッファの高さを取得する
	 * @return 仮想フレームバッファの高さ
	 */
	@Override
	public int getHeight() {
		return frameBuffer.getHeight();
	}

}
