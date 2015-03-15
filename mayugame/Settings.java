package com.hajikoma.mayugame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Log;

import com.hajikoma.mayugame.framework.FileIO;
import com.hajikoma.mayugame.framework.impl.AndroidFileIO;

/** 各種設定を行うクラス。現在はデータのセーブとロード機能のみ。*/
public class Settings{

    /**
     * 直列化し外部ファイルにデータを保存する。
     * 対象のフォルダ・ファイルが存在しない場合は作成する。
     */
	public static void save(FileIO fIO, UserData ud){
		//フォルダが存在しない場合、作成する
		File file = new File(((AndroidFileIO)fIO).externalStoragePath + File.separator + "mayugame");
		if(!file.exists()){
			file.mkdir();
		}

		//データ書き込み
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = (FileOutputStream)fIO.writeFile("mayugame/savedata.dat");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ud);
			oos.flush();
		} catch (FileNotFoundException e) {
			Log.e("save",""+e);
		} catch (IOException e) {
			Log.e("save",""+e);
		}finally{
			try{
				if(oos != null){
					oos.close();
				}
			}catch(IOException e){
				Log.e("save-close",""+e);
			}
		}
	}

    /** 直列化したデータを外部ファイルから読み込む */
	public static UserData load(FileIO fIO) throws FileNotFoundException,IOException,ClassNotFoundException{
		ObjectInputStream ois = null;
		UserData ud =null;
		try {
			FileInputStream fis = (FileInputStream)fIO.readFile("mayugame/savedata.dat");
			ois = new ObjectInputStream(fis);
			ud = (UserData)ois.readObject();
			ois.close();
		}finally{
			try{
				if(ois != null){
					ois.close();
				}
			}catch(IOException e) {
				Log.e("load-close",""+e);
			}
		}
		return ud;
	}

}