package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListenerAllApps implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    OnSwipeTouchListenerAllApps(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeBottom() {
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final Context ctx;

        public GestureListener(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                int height = ScreenUtils.getDisplay(ctx).getHeight();
                int width = ScreenUtils.getDisplay(ctx).getWidth();

                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > width / 5 && Math.abs(velocityY) > width / 5 && diffY > 0 && e2.getY() < height / 2) {

                        onSwipeBottom();
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}