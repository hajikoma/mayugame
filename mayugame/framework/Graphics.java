package com.hajikoma.mayugame.framework;

import android.graphics.Rect;

public interface Graphics {
    public static enum PixmapFormat {
        ARGB8888, ARGB4444, RGB565
    }

    public Pixmap newPixmap(String fileName, PixmapFormat format);

    public void clear(int color);

    public void drawPixel(int x, int y, int color);

    public void drawLine(int x, int y, int x2, int y2, int color);

    public void drawRect(int x, int y, int width, int height, int color);

    public void drawRect(Rect rect, int color);

    public void drawRoundRect(int x, int y, int width, int height, float radius, int color);

    public void drawRoundRect(Rect rect, float radius, int color);

    public void drawPixmap(Pixmap pixmap, Rect dstRect, Rect srcRect);

    public void drawPixmap(Pixmap pixmap, Rect dstRect, int srcX, int srcY, int srcWidth, int srcHeight);

    public void drawPixmap(Pixmap visual, int x, int y, int dstWidth, int dstHeight, Rect srcRect);

    public void drawPixmap(Pixmap pixmap, int x, int y, int dstWidth, int dstHeight, int srcX, int srcY, int srcWidth, int srcHeight);

    public void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight);

    public void drawPixmap(Pixmap pixmap, int x, int y);

    public int getWidth();

    public int getHeight();

	public int argbGenerator(int argb, int step, int limit);


}
