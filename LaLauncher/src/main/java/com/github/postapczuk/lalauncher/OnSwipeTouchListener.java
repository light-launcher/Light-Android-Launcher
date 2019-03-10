package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
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
                int height = getDisplayHeight();
                int width = getDisplayWidth();

                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > width / 5 && Math.abs(velocityX) > height / 5) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > width / 5 && Math.abs(velocityY) > width / 5) {
                        if (diffY > 0 && e1.getY() < height / 5) {
                            onSwipeBottom();
                        } else if (diffY <= 0 && e1.getY() > height - (height / 5)) {
                            onSwipeTop();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        private int getDisplayHeight() {
            WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                return display.getHeight();
            }
            return 0;
        }

        private int getDisplayWidth() {
            WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                return display.getWidth();
            }
            return 0;
        }
    }
}