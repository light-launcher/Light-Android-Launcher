package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class OnSwipeTouchListenerAllApps implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final ListView listView;

    OnSwipeTouchListenerAllApps(Context ctx, ListView listView) {
        gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
        this.listView = listView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeBottom() {
    }

    private boolean canScrollUp() {
        return !(listView.getFirstVisiblePosition() > 0 || listView.getChildAt(0).getTop() < listView.getPaddingTop());
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
                if (canScrollUp()) {
                    int height = ScreenUtils.getDisplay(ctx).getHeight();

                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffY) > height / 5) {
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