package com.hajikoma.mayugame.framework.impl;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import com.hajikoma.mayugame.framework.Audio;
import com.hajikoma.mayugame.framework.Music;
import com.hajikoma.mayugame.framework.Sound;

/**
 * Audioインタフェースを実装する。
 * アセットファイルに基づいて、MusicとSoundの各インスタンスを生成する。
 */
public class AndroidAudio implements Audio {

	/** コンストラクタで渡されるアセットマネージャ */
	AssetManager assets;
	/** 効果音をメモリ上にプールしておくインスタンス */
	SoundPool soundPool;

	/**
	 * フィールド変数assetsとsoundPoolを初期化する。
	 * Activityを受け取る事で、音量調節をミュージックストリームにセットできるようにしている。
	 * @param activity アクティビティ
	 */
	public AndroidAudio(Activity activity) {
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		this.assets = activity.getAssets();
		this.soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
	}

	/**
	 * ストリーミング再生を行うMusicインスタンスを取得する
	 * @param fileName 再生するBGMファイル名
	 * @return BGMファイルと紐づけられたMusicインスタンス
	 */
	@Override
	public Music newMusic(String filename) {
		try {
			AssetFileDescriptor assetDescriptor = assets.openFd(filename);
			return new AndroidMusic(assetDescriptor);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load music '" + filename + "'");
		}
	}

	/**
	 * メモリからの再生を行うSoundインスタンスを取得する
	 * @param fileName 再生する効果音ファイル名
	 * @return 効果音ファイルと紐づけられたSoundインスタンス
	 */
	@Override
	public Sound newSound(String filename) {
		try {
			AssetFileDescriptor assetDescriptor = assets.openFd(filename);
			int soundId = soundPool.load(assetDescriptor, 0);
			return new AndroidSound(soundPool, soundId);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load sound '" + filename + "'");
		}
	}
}