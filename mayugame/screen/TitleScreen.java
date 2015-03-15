package com.hajikoma.mayugame.screen;

import java.util.List;

import android.graphics.Rect;

import com.hajikoma.mayugame.Assets;
import com.hajikoma.mayugame.Settings;
import com.hajikoma.mayugame.framework.Game;
import com.hajikoma.mayugame.framework.Graphics;
import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;
import com.hajikoma.mayugame.framework.Input.GestureEvent;
import com.hajikoma.mayugame.framework.Screen;
import com.hajikoma.mayugame.framework.Text;
import com.hajikoma.mayugame.framework.Vibrate;

/** タイトルスクリーン。音、バイブ、チュートリアル表示が行える。 */
public class TitleScreen extends Screen {

	/** 共通して使用するゲームクラス */
	private final Game game;
	/** Graphicsインスタンス */
	private final Graphics gra;
	/** Textインスタンス */
	private final Text txt;
	/** Vibrateインスタンス */
	protected Vibrate vib;

	/** ミュート画像の表示領域 */
	private Rect muteDstArea = new Rect(100, 860, 300, 1060);
	/** バイブレーション画像の表示領域 */
	private Rect vibDstArea = new Rect(420, 860, 620, 1060);
	/** チュートリアルをもう一度の表示領域 */
	private Rect tutoAgainDstArea = new Rect(300, 1180, 480, 1250);
	/** アプリ情報の表示領域 */
	private Rect infoDstArea = new Rect(500, 1180, 700, 1250);


	public TitleScreen(Game game){
		super(game);
		this.game = game;
		gra = game.getGraphics();
		txt = game.getText();
		vib = game.getVibrate();

		//固有グラフィックの読み込み
		Assets.title_bg = gra.newPixmap("others/title_bg.jpg", PixmapFormat.RGB565);
		Assets.settings = gra.newPixmap("others/settings.png", PixmapFormat.ARGB4444);

	}

	@Override
	public void update(float deltaTime) {
		List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
		game.getInput().getKeyEvents();

		for(int i = 0; i < gestureEvents.size(); i++){
			GestureEvent ges = gestureEvents.get(i);
			if(ges.type == GestureEvent.GESTURE_SINGLE_TAP_UP){
				if(isBounds(ges, muteDstArea)){
					if(Assets.ud.isMute()){
						Assets.ud.unMute();
						playSound(Assets.fall, 0.3f);
						Settings.save(game.getFileIO(), Assets.ud);
						break;
					}else{
						Assets.ud.mute();
						Settings.save(game.getFileIO(), Assets.ud);
						break;
					}
				}else if(isBounds(ges, vibDstArea)){
					if(Assets.ud.isVibeOn()){
						Assets.ud.vibeOff();
						Settings.save(game.getFileIO(), Assets.ud);
						break;
					}else{
						Assets.ud.vibeOn();
						doVibrate(vib, Assets.vibShortOnce);
						Settings.save(game.getFileIO(), Assets.ud);
						break;
					}
				}else if(isBounds(ges, tutoAgainDstArea)){
					playSound(Assets.click, 0.5f);
					String[] tutorialPreferences = {
							"PartsTutorial",
							"CourseTutorial",
							"DetailTutorial",
							"ItemListTutorial",
							"ItemDetailTutorial",
							"DataPickerTutorial",
							"DataScoreTutorial",
							"TrimTutorial0",
							"TrimTutorial1",
							"TrimTutorial2",
							"TrimTutorial3",
							"TrimTutorial4"
					};
					for(int ti = 0; ti < tutorialPreferences.length; ti++){
						switchPreferenceFlag(tutorialPreferences[ti], false);
					}

				}else if(isBounds(ges, infoDstArea)){
					playSound(Assets.click, 0.5f);
					game.setScreen(new CreditScreen(game));
				}else{
					playSound(Assets.click, 0.5f);
					game.setScreen(new CourseScreen(game,Assets.ud.getUseItemIndex()));
					break;
				}
			}
		}

	}

	/** 背景と、音・バイブ各設定に応じてアイコンを描画する */
	@Override
	public void present(float deltaTime) {
		gra.drawPixmap(Assets.title_bg, 0, 0);

		if(Assets.ud.isMute()){
			gra.drawPixmap(Assets.settings, muteDstArea, 200, 0, 200, 200);
		}else{
			gra.drawPixmap(Assets.settings, muteDstArea, 0, 0, 200, 200);
		}

		if(Assets.ud.isVibeOn()){
			gra.drawPixmap(Assets.settings, vibDstArea, 0, 200, 200, 200);
		}else{
			gra.drawPixmap(Assets.settings, vibDstArea, 200, 200, 200, 200);
		}

		// ストレージのパスを表示（デバイスごとに異なるので）。リリース時削除
/*		txt.drawText((Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator) + "", 100, 100, 600,
				Assets.map_style.get("title"));*/

	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {
		Assets.title_bg = null;
		Assets.settings = null;
	}

	@Override
	public String toString() {
		return "TitleScreen";
	}

}
