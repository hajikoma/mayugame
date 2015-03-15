package com.hajikoma.mayugame.framework.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.os.Environment;

import com.hajikoma.mayugame.framework.FileIO;

/**
 * FileIOインタフェースを実装する。
 * アセット、外部ストレージにあるファイルのストリームを取得する。
 * アセット内のファイルは読み取り専用なので、writeAssetメソッドは存在しない。
 * 外部ストレージが利用可能かどうかは確認しないため、このクラスを利用する前に適切に確認するべきである。
 */
public class AndroidFileIO implements FileIO {

	/** コンストラクタで渡されるアセットマネージャ */
	public AssetManager assets;
	/** 外部ストレージの絶対パス */
	public String externalStoragePath;

	/**
	 * フィールド変数assetsとexternalStoragePathを初期化してインスタンスを生成する。
	 * @param assets アセットマネージャ
	 */
	public AndroidFileIO(AssetManager assets) {
		this.assets = assets;
		this.externalStoragePath = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator;
	}

	/**
	 * アセット内のファイルのインプットストリームを取得する。
	 * @param fileName ストリームを取得するファイル名
	 * @return ファイルのインプットストリーム
	 * @exception java.io.IOException ファイルの取得に失敗した時にスローされる
	 */
	@Override
	public InputStream readAsset(String fileName) throws IOException {
		return assets.open(fileName);
	}

	/**
	 * 外部ストレージのファイルのインプットストリームを取得する。
	 * @param fileName ストリームを取得するファイル名
	 * @return ファイルのインプットストリーム
	 * @exception java.io.IOException ファイルの取得に失敗した時にスローされる
	 */
	@Override
	public InputStream readFile(String fileName) throws IOException {
		return new FileInputStream(externalStoragePath + fileName);
	}

	/**
	 * 外部ストレージのファイルのアウトプットストリームを取得する。
	 * @param fileName ストリームを取得するファイル名
	 * @return ファイルのアウトプットストリーム
	 * @exception java.io.IOException ファイルの取得に失敗した時にスローされる
	 */
	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		return new FileOutputStream(externalStoragePath + fileName);
	}

}
