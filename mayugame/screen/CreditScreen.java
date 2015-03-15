package com.hajikoma.mayugame.screen;

import java.util.List;

import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.MayuGame;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;

/** 開発者情報等を表示するスクリーン。 */
public class CreditScreen extends Screen {

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;

	/** URIリンクの表示領域 */
	private Rect URIDstArea = new Rect(80, 370, 80 + 640, 370 + 70);
	/** まゆげーのPlayストアURI */
	public static final String mayugameURI = "https://play.google.com/store/apps/details?id=com.hajikoma.mayugame";

	public CreditScreen(Game game){
		super(game);
		this.game = game;
		gra = game.getGraphics();

		//固有グラフィックの読み込み
		Assets.credit_bg = gra.newPixmap("others/credit_bg.jpg", PixmapFormat.RGB565);

	}

	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		for(int i = 0; i < gestureEvents.size(); i++){
			GestureEvent ges = gestureEvents.get(i);
			if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
				if(isBounds(ges, URIDstArea)){
					playSound(Assets.click, 0.5f);
					((MayuGame)game).startBrowserApp(mayugameURI);
					break;
				}else{
					playSound(Assets.click, 0.5f);
					game.setScreen(new TitleScreen(game));
					break;
				}
			}
		}

	}

	@Override
	public void present(float deltaTime) {
		gra.drawPixmap(Assets.credit_bg, 0, 0);
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {
		Assets.credit_bg = null;
	}

	@Override
	public String toString() {
		return "CreditScreen";
	}

}
