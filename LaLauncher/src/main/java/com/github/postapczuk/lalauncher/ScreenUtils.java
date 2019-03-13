package com.github.postapczuk.lalauncher;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

class ScreenUtils {

    static Display getDisplay(Context ctx) {
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            return windowManager.getDefaultDisplay();
        }
        return null;
    }
}
