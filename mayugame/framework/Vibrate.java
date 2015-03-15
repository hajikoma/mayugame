package com.hajikoma.mayugame.framework;

public interface Vibrate {
	public void vibrate(int milliseconds);

	public void vibrate(long[] pattern, int repeat);

	public void cancel();

}
