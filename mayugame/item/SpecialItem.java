package com.hajikoma.mayugame.item;

import android.graphics.Rect;

import com.hajikoma.mayugame.framework.Pixmap;

/**
 * メインプログラムに手を加える必要がある特別な効果を持つアイテムを表すクラス。
 * 内部的にはItemクラスのコンストラクタを呼び出すだけで、Itemクラスと全く同一である。
 * メインプログラムを変更するアイテムであり、実装が複雑かつバグを生みやすいため
 * 注意せよというマーカーの意味でこのクラスは存在している。
 */
public class SpecialItem extends Item {

	public SpecialItem(ItemCategory category, ItemType type, String name,
			String explain, String info, int price, Pixmap visual,
			float useTime, Rect drawArea, String unlockInfo, int unlockNeed) {
		super(category, type, name, explain, info, price, visual, useTime,
				drawArea, unlockInfo, unlockNeed);
	}

}
