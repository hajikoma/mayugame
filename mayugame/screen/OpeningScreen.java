package com.hajikoma.mayugame.screen;

import java.util.List;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;

/** オープニングスクリーン。プロローグを流すのみ。 */
public class OpeningScreen extends Screen {

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;

	/** 経過時間 */
	private float pastTime = 0.0f;
	/** プロローグ文章 */
	private String[] prologueStrs = new String[]{
			"その子は みめうるわしく",
			"だれからも好かれる存在です",
			"でもその子には ひとつだけ",
			"だれにも言えない",
			"悩みがありました",
			"それは・・・",
			"毛の生命力が",
			"はんぱないことだったのです"
	};

	public OpeningScreen(Game game){
		super(game);
		this.game = game;
		gra = game.getGraphics();
		txt = game.getText();

		//固有グラフィックの読み込み
		Assets.opening_bg = gra.newPixmap("others/opening_bg.jpg", PixmapFormat.RGB565);

	}

	/** プロローグ終了か、タッチでタイトルへ移動する */
	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		for(int gi = 0; gi < gestureEvents.size(); gi++){
			GestureEvent ges = gestureEvents.get(gi);
			if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
				playSound(Assets.click, 0.5f);
				game.setScreen(new TitleScreen(game));
				break;
			}
		}

		if(pastTime >= 35.0f){
			game.setScreen(new TitleScreen(game));
		}
	}

	/** プロローグを流す */
	@Override
	public void present(float deltaTime) {
		gra.drawPixmap(Assets.opening_bg, 0, 0);

		for(int i = 0; i < prologueStrs.length; i++){
			int pointY = 1100 + i * 120 - (int)(pastTime * 50);
			if(pointY > 260 && pointY < 1110){
				txt.drawText(prologueStrs[i], 40, pointY, 600, Assets.map_style.get("title"));
			}
		}

		pastTime += deltaTime;
	}

	/** このスクリーンでの処理はない */
	@Override
	public void pause() {}

	/** このスクリーンでの処理はない */
	@Override
	public void resume() {}

	/** スクリーン変更時に呼ばれる */
	@Override
	public void dispose() {
		Assets.opening_bg = null;
	}

	@Override
	public String toString() {
		return "OpeningScreen";
	}

}
