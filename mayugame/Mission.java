package com.hajikoma.mayugame;

import java.util.HashMap;

/**
 * お手入れのゲーム内容を定義するクラス。このクラスが格納する各変数の内容に応じて、
 * お手入れ中に生える毛や結果判定の条件が決まる。
 * 内容が途中で変わることはないため、変数はすべて定数として宣言している。
 */
public class Mission {

	/** ミッションのタイトル */
	public final String TITLE;
	/** ミッションの内容 */
	public final String DETAIL;
	/** ミッションの開放に必要なポイント */
	public final int UNLOCK_NEED;
	/** 使用するmayuMap（生える毛） */
	public final HashMap<String, Mayu> USE_MAYUMAP;
	/** 毛根の数。最もパフォーマンスに影響する値であるため、安易に増やさないこと。*/
	public final int ROOT_COUNT;
	/** 毛根一つ一つの生命力*/
	public final int ROOT_POWER;
	/** クリア条件（ポイント） */
	public final int NEED_SCORE;
	/** 結果判定の大成功と成功のボーダーライン（しきい値） */
	public final float BORDER;

	/** Missonインスタンスを生成する */
	public Mission(
			String title, String detail, int unlockNeed,
			HashMap<String, Mayu> useMayuMap,
			int rootCount, int rootPower,
			int score, float border) {
		TITLE = title;
		DETAIL = detail;
		UNLOCK_NEED = unlockNeed;
		USE_MAYUMAP = useMayuMap;
		ROOT_COUNT = rootCount;
		ROOT_POWER = rootPower;
		NEED_SCORE = score;
		BORDER = border;
	}

}
