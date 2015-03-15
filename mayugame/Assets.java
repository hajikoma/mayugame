package com.hajikoma.mayugame;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Paint;

import com.hajikoma.mayugame.framework.Pixmap;
import com.hajikoma.mayugame.framework.Sound;
import com.hajikoma.mayugame.item.DefaultPicker;
import com.hajikoma.mayugame.item.Item;

/**
 * Assetsフォルダのリソースを格納する変数のみで構成され、リソースの利用方法を一元化している。
 * 設計上は、各Screen共通で使用するリソースはLoadingScreenで読み込み、
 * 特定のScreenでのみ使用するリソースは各Screenのコンストラクタで読み込む。
 * NOTE:リソース一つにつき一つの変数を用意すること。Screen変更時のnullPointerExceptionと、
 * 大きな画像読み込み時のOutOfMemoryの抑制になる。
 */
public class Assets {

	public static Pixmap opening_bg;
	public static Pixmap title_bg;
	public static Pixmap credit_bg;
	public static Pixmap course_bg1;
	public static Pixmap course_bg2;
	public static Pixmap course_bg3;
	public static Pixmap item_bg;
	public static Pixmap data_bg;
	public static Pixmap trim_bg;
	public static Pixmap result_bg;

	public static Pixmap settings;
	public static Pixmap icons;
	public static Pixmap icon_button;
	public static Pixmap icon_button_result;
	public static Pixmap icon_hand;
	public static Pixmap number;
	;
	public static Pixmap chara_visual;

	public static Sound click;
	public static Sound powerup;
	public static Sound result_score;
	public static Sound pay_point;

	public static Sound pick_up1;
	public static Sound pick_up2;
	public static Sound fall;
	public static Sound weak;

	public static int vibShortOnce;
	public static int vibLongOnce;
	public static long[] vibShortRythem;
	public static long[] vibLongRythem;

	public static Pixmap mayu_normal;
	public static Pixmap mayu_white;
	public static Pixmap mayu_short;
	public static HashMap<String, HashMap<String, Mayu>> maps_mayu;

	public static ArrayList<Mission> mission_list;

	public static Pixmap item_picker1;
	public static Pixmap item_enhancer1;

	public static DefaultPicker default_picker;
	public static ArrayList<Item> item_list = new ArrayList<Item>();

	public static Pixmap onomatopee;

	public static HashMap<String, Paint> map_style;


//各Screenで共有する変数。Screenが遷移しても値を保持しておきたい変数や、
//メモリの使用量が大きく、都度生成するのが望ましくない変数のみここに定義する。
	/** ユーザーデータ */
	public static UserData ud;

}
