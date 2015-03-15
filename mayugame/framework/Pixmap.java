package com.hajikoma.mayugame.framework;

import android.graphics.Bitmap;

import com.hajikoma.mayugame.framework.Graphics.PixmapFormat;

public interface Pixmap {

    public Bitmap getBitmap();

    public void setBitmap(Bitmap bitmap);

    public int getWidth();

    public int getHeight();

    public PixmapFormat getFormat();

    public void dispose();
}
