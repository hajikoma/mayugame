package com.hajikoma.mayugame.framework.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 加速度センサーのX,Y,Z各軸の加速度を取得するクラス。
 * 加速度センサーが存在しない場合、各値は常に0となる。
 */
public class AccelerometerHandler implements SensorEventListener {

	/** X軸の加速度 */
	float accelX;
	/** Y軸の加速度 */
	float accelY;
	/** Z軸の加速度 */
	float accelZ;

	/**
	 * 加速度センサーのインスタンスを取得する。
	 * @param context コンテクスト
	 */
	public AccelerometerHandler(Context context) {
		SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			Sensor accelerometer = manager.getSensorList(
					Sensor.TYPE_ACCELEROMETER).get(0);
			manager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/** センサーの精度変更時に呼ばれる（未使用） */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ここでは何もしない
	}

	/**
	 * 傾きが変化した時に呼ばれる
	 * 実質的に、ゲームプレイ中は常に呼ばれ続ける
	 * @param event 傾きを検知したイベント
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		accelX = event.values[0];
		accelY = event.values[1];
		accelZ = event.values[2];
	}

	/**
	 * X軸の加速度を取得する
	 * @return X軸の加速度
	 */
	public float getAccelX() {
		return accelX;
	}

	/**
	 * Y軸の加速度を取得する
	 * @return Y軸の加速度
	 */
	public float getAccelY() {
		return accelY;
	}

	/**
	 * Z軸の加速度を取得する
	 * @return Z軸の加速度
	 */
	public float getAccelZ() {
		return accelZ;
	}
}
