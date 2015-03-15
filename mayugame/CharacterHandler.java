package com.hajikoma.mayugame;

import android.graphics.Point;
import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * Characterクラスを便利に利用するためのサポートクラス。
 * Characterを内部に保持し、適宜メソッドを呼び出す。
 * このクラスを利用することで、Viewのクラスからは
 * 行いたいアクション定数を引数にしてdoActionメソッドを呼び出すだけで良くなる。
 */
public class CharacterHandler {

	/** アクション定数（立ち。通常状態） */
	public static final int ACTION_STAND = 0;
	/** アクション定数（泣く） */
	public static final int ACTION_CRY = 1;
	/** アクション定数（喜ぶ） */
	public static final int ACTION_HAPPY = 2;

	/** 保持するCharacter */
	public com.hajikoma.mayugame.Character chara;
	/** 戻り値に使用する矩形座標。画像内で描画すべき矩形の座標を示す */
	private Rect drawArea = new Rect();
	/** Charaのアクションメソッドの戻り値 */
	private Point drawPoint;

	/** CharacterHandlerを生成する。 */
	public CharacterHandler(Character chara){
		this.chara = chara;
	}

	/** CharacterHandlerを生成する。同時にCharacterも生成し、内部に保持する */
	public CharacterHandler(Pixmap visual){
		chara = new Character(visual);
	}

	/**
	 * アクション定数に応じてアクションを行う
	 * @param actionType アクションの種類（アクション定数を指定）
	 * @param deltaTime デルタ時間
	 */
	public Rect action(int actionType, float deltaTime){
		if(!chara.isAction()){
			chara.startNewAction();
		}
		chara.addDelta(deltaTime);

		switch(actionType){
		case ACTION_STAND:
			drawPoint = chara.standing();
			break;
		case ACTION_CRY:
			drawPoint = chara.cry();
			break;
		case ACTION_HAPPY:
			drawPoint = chara.happy();
			break;
		}

		drawArea.top = drawPoint.y;
		drawArea.left = drawPoint.x;
		drawArea.right = drawPoint.x + chara.getWidth();
		drawArea.bottom = drawPoint.y + chara.getHeight();

		return drawArea;
	}
}
