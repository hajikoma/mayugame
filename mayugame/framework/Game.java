package com.hajikoma.mayugame.framework;

import com.hajikoma.mayugame.framework.impl.AndroidFastRenderView;

public interface Game {
    public AndroidFastRenderView getRenderView();

    public Input getInput();

    public FileIO getFileIO();

    public Graphics getGraphics();

    public Audio getAudio();

    public Text getText();

    public Vibrate getVibrate();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getStartScreen();
}
