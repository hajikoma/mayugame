package com.hajikoma.mayugame.framework.impl;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.hajikoma.mayugame.framework.Music;

/**
 * Musicインタフェースを実装する。
 * BGMのストリーミング再生を制御する
 * OnCompletionListenerの実装により、再生終了のイベントを受け取る。
 */
public class AndroidMusic implements Music, OnCompletionListener {

	/** BGMの再生を行うインスタンス */
	MediaPlayer mediaPlayer;
	/** MediaPlayerのpreparedメソッドが呼ばれた状態かどうかのフラグ */
	boolean isPrepared = false;

	public AndroidMusic(AssetFileDescriptor assetDescriptor) {
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(),
					assetDescriptor.getStartOffset(),
					assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't load music");
		}
	}

	/** インスタンスを破棄する */
	@Override
	public void dispose() {
		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();
		mediaPlayer.release();
	}

	/** 再生を一時停止する */
    @Override
    public void pause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

	/**
	 * ループ再生が有効かどうかを判定する
	 * @return ループ再生が有効ならtrue、無効ならfalse
	 */
	@Override
	public boolean isLooping() {
		return mediaPlayer.isLooping();
	}

	/**
	 * 再生中かどうかを判定する
	 * @return 再生中ならtrue、停止または一時停止中ならfalse
	 */
    @Override
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	/**
	 * 停止中かどうかを判定する
	 * MediaPlayerクラスのisPlayingメソッドは、停止中でも一時停止中でも
	 * tureを返すため、フィールド変数の状態を返す。
	 * @return ループ再生が有効ならtrue、無効ならfalse
	 */
	@Override
	public boolean isStopped() {
		return !isPrepared;
	}

	/** 再生を開始する */
	@Override
	public void play() {
		//すでに再生中の場合、何もせず処理を戻す
		if (mediaPlayer.isPlaying())
			return;

		try {
			//isPreparedフラグはOnCompletionListenerの実装により、
			//別スレッドで実行される可能性があるため、シンクロさせる。
			synchronized (this) {
				if (!isPrepared)
					mediaPlayer.prepare();
				mediaPlayer.start();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ループ再生の有効/無効を切り替える
	 * @param isLooping ループ再生の有効/無効
	 */
	@Override
	public void setLooping(boolean isLooping) {
		mediaPlayer.setLooping(isLooping);
	}

	/**
	 * 音量を変更する
	 * @param volume 再生音量
	 */
	@Override
	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	/** 再生を停止する */
	@Override
	public void stop() {
		mediaPlayer.stop();
		synchronized (this) {
			isPrepared = false;
		}
	}

	/**
	 * 再生が完了した時に呼ばれる
	 * isPreparedフラグをfalseにすることで、他のメソッドで例外が発生しないようにする。
	 * @param player 再生に使用しているMediaPlayer
	 */
	@Override
	public void onCompletion(MediaPlayer player) {
		synchronized (this) {
			isPrepared = false;
		}
	}

}
