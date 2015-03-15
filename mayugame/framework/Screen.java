package com.hajikoma.mayugame.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.impl.AndroidGame;

public abstract class Screen {
    protected final Game game;

    public Screen(Game game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);

    public abstract void present(float deltaTime);

    public abstract void pause();

    public abstract void resume();

    public abstract void dispose();

    /** 現在のスクリーンに応じた処理を行う場合に備え、toString()のオーバーライドを強制 */
    public abstract String toString();

	/**
	 * boolean型のSharedPreferencesの値を調べる。
	 * @param preferenceName 調べるPreferenceの名前
	 * @return 指定のPreferenceのboolean値。指定のPreferenceが存在しない場合は常にfalse
	 */
    public boolean checkPreferenceState(String preferenceName){
    	SharedPreferences preference = ((AndroidGame)game).getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    	try{
    		if (preference.getBoolean(preferenceName, false) == false) {
    			return false;
    		} else {
    			return true;
    		}
    	}catch (ExceptionInInitializerError e){
    		e.getCause().printStackTrace();
    	}
    	return false;
    }

	/**
	 * boolean型のSharedPreferencesの値を変更する。
	 * @param preferenceName 対象のPreferenceの名前
	 * @param switchTo 対象のPreferenceに格納するboolean値
	 */
    public void switchPreferenceFlag(String preferenceName, boolean switchTo){
    	SharedPreferences preference = ((AndroidGame)game).getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    	Editor editor= preference.edit();
		editor.putBoolean(preferenceName, switchTo);
		editor.commit();
	}

	/**
	 * イベントの座標が矩形領域内にあるかどうかを判定するヘルパー。
	 * スワイプ動作（onScrollイベント）の場合、イベントの開始地点と現在位置の両方を判定する。
	 *  @param e 判定するイベント
	 *  @param x 矩形領域左上座標x
	 *  @param y 矩形領域左上座標y
	 *  @param width 矩形領域の幅
	 *  @param height 矩形領域の高さ
	 *  @return イベントが矩形領域内で発生している場合true、領域外の場合false
	 */
	public static boolean isBounds(GestureEvent e, int x, int y, int width, int height) {
		if (e.x1 > x && e.x1 < x + width && e.y1 > y && e.y1 < y + height) {
			return true;
		} else if(e.type == GestureEvent.GESTURE_SCROLL) {
			if(e.x2 > x && e.x2 < x + width && e.y2 > y && e.y2 < y + height){
				return true;
			}else{
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * イベントの座標が矩形領域内にあるかどうかを判定するヘルパー。
	 * スワイプ動作（onScrollイベント）の場合、イベントの開始地点と現在位置の両方を判定する。
	 * expansionの値分、判定する対象領域が拡張される。
	 *  @param e 判定するイベント
	 *  @param x 矩形領域左上座標x
	 *  @param y 矩形領域左上座標y
	 *  @param width 矩形領域の幅
	 *  @param height 矩形領域の高さ
	 *  @param expansion pickerの持つ当たり判定の拡大値
	 *  @return イベントが矩形領域内で発生している場合true、領域外の場合false
	 */
	public static boolean isBounds(GestureEvent e, int x, int y, int width, int height, int expansion) {
		return isBounds(e, x - expansion, y - expansion, width + expansion * 2, height + expansion * 2 + 15);
	}

	/**
	 * イベントの座標が矩形領域内にあるかどうかを判定するヘルパー。
	 *  @param e 判定するイベント
	 *  @param rect 判定する領域
	 *  @return イベントが矩形領域内で発生している場合true、領域外の場合false
	 */
	public static boolean isBounds(GestureEvent e, Rect rect) {
		if (rect.contains(e.x1, e.y1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 効果音を再生する。設定でミュートになっている場合は再生しない。
	 * @param sound 再生する効果音
	 * @param volume 音量
	 * @return 再生した場合true、ミュートで再生しなかった場合false
	 */
	public static boolean playSound(Sound sound, float volume) {
		if (Assets.ud.isMute()) {
			return false;
		} else {
			sound.play(volume);
			return true;
		}
	}

	/**
	 * 振動（バイブレーション）させる。設定でバイブOFFになっている場合は再生しない。
	 * @param vib Vibrateインスタンス
	 * @param milliseconds 振動させる時間（ミリ秒）
	 * @return 振動させた場合true、バイブOFFにより振動させなかった場合false
	 */
	public static boolean doVibrate(Vibrate vib, int milliseconds) {
		if (Assets.ud.isVibeOn()) {
			vib.vibrate(milliseconds);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 繰り返し振動（バイブレーション）させる。設定でバイブOFFになっている場合は再生しない。
	 * @param vib Vibrateインスタンス
	 * @param pattern 振動パターン
	 * @param repeat 繰り返し振動させる回数
	 * @return 振動させた場合true、バイブOFFにより振動させなかった場合false
	 */
	public static boolean doVibrate(Vibrate vib, long[] pattern, int repeat) {
		if (!Assets.ud.isVibeOn()) {
			return false;
		} else {
			vib.vibrate(pattern, repeat);
			return true;
		}
	}

	/**
	 * 画像を使って数字を描画する
	 * FIXME:drawNumberに負の数字が渡された場合NumberFormatExceptionが発生する
	 * HACK:drawNumberに負の数字が渡された場合、0を描画する
	 * @param gra Graphicインスタンス
	 * @param drawNumber 描画する数字
	 * @param atHeightPx 描画する数値一つ一つの高さ（ピクセル）
	 * @param x 描画座標x
	 * @param y 描画座標y
	 * @param digit 表示桁数（指定しない場合は-1）
	 */
	public static void drawGraphicalNumber(Graphics gra, int drawNumber, int atHeightPx, int x, int y, int digit){
		if(drawNumber < 0){
			drawNumber = 0;
		}

		String numStr = String.valueOf(drawNumber);
		int srcX, offsetX = 0;
		int atWidthPx = atHeightPx * 3 / 4;	//数字一つの解像度は30px＊40px。

		int roopLength = (digit == -1) ? numStr.length() : digit;

		for(int i = 0; i < roopLength; i++){
			//3桁の区切りカンマを描画
			if(i != 0 && (roopLength - i) % 3 == 0){
				srcX = 300;
				gra.drawPixmap(Assets.number, x + offsetX, y, atWidthPx / 3, atHeightPx, srcX, 0, 10, 40);
				offsetX += atHeightPx / 3;	//カンマの解像度は10px*40pxで、数字の横幅/3
			}

			//表示指定桁数に数値の長さが満たない場合、足りない分だけ”0”を描画
			if(digit != -1 && digit > numStr.length() + i){
				gra.drawPixmap(Assets.number, x + offsetX, y, atWidthPx, atHeightPx, 0, 0, 30, 40);
				offsetX += atWidthPx;
				continue;
			}

			//数値を一つずつ取り出し
			int charIndex =  (digit == -1) ? i : i - (digit - numStr.length());
			int num = Integer.valueOf(String.valueOf(numStr.charAt(charIndex)));

			//数値を描画
			srcX = num * 30;
			gra.drawPixmap(Assets.number, x + offsetX, y, atWidthPx, atHeightPx, srcX, 0, 30, 40);
			offsetX += atWidthPx;

		}
	}

	/**
	 * チュートリアル画面の背景を描画する
	 * @param gra Graphicインスタンス
	 * @param txt Textインスタンス
	 */
	public static void drawTutorialBG(Graphics gra, Text txt){
		gra.drawRoundRect(30, 240, 660, 800, 15.0f, Color.argb(250, 255, 255, 255));
		txt.drawText("～チュートリアル～", 140, 320, 600, Assets.map_style.get("title"));
		txt.drawText("→画面タッチで進む", 400, 1010, 400, Assets.map_style.get("general"));
	}

}
