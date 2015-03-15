package com.hajikoma.mayugame.framework;

import java.util.List;

public interface Input {

    /** キーイベントを格納する。 */
    public static class KeyEvent {
        public static final int KEY_DOWN = 0;
        public static final int KEY_UP = 1;

        public int type;
        public int keyCode;
        public char keyChar;
    }

    /** タッチイベントを格納する。 */
    public static class TouchEvent {
        public static final int TOUCH_DOWN = 0;
        public static final int TOUCH_UP = 1;
        public static final int TOUCH_DRAGGED = 2;

        public int type;
        public int x, y;
        public int pointer;
    }

    /** ジェスチャーイベントを格納する。 */
    public static class GestureEvent {
        public static final int GESTURE_DOWN = 0;
        public static final int GESTURE_SINGLE_TAP_UP = 1;
        public static final int GESTURE_SINGLE_TAP_CONFIRMED = 2;
        public static final int GESTURE_DOUBLE_TAP = 3;
        public static final int GESTURE_DOUBLE_TAP_EVENT = 4;
        public static final int GESTURE_FLING = 5;
        public static final int GESTURE_SCROLL = 6;
        public static final int GESTURE_SHOW_PRESS = 7;
        public static final int GESTURE_LONG_PRESS = 8;
        public static final int GESTURE_SCALE_BEGIN = 9;
        public static final int GESTURE_SCALE_IN = 10;
        public static final int GESTURE_SCALE_OUT = 11;
        public static final int GESTURE_SCALE_END = 12;

        /** イベントの種類 */
        public int type;
        /** イベント座標。onFling、onScrollイベントではイベントが2つあるため、1と2を用意。 */
        public int x1, y1, x2, y2;
        /** onFlingイベントにおけるフリック速度 */
        public float velocityX, velocityY;
		/** onScrollイベントにおけるスクロール距離 */
        public float distanceX, distanceY;
		/** scaleイベントにおける2点間の距離 */
        public float span;
    }


    public boolean isKeyPressed(int keyCode);

    //public boolean isTouchDown(int pointer);

    //public int getTouchX1();

    //public int getTouchY1();

    //public int getTouchX2();

    //public int getTouchY2();

    //public float getVelocityX();

	//public float getVelocityY();

	//public float getDistanceX();

	//public float getDistanceY();

    //public float getAccelX();

    //public float getAccelY();

    //public float getAccelZ();

    public List<KeyEvent> getKeyEvents();

    //public List<TouchEvent> getTouchEvents();

    public List<GestureEvent> getGestureEvents();
}
