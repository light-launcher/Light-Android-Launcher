package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import static android.R.layout.simple_list_item_1;

public class AllAppsActivity extends AppsActivity {

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            this.getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            this.getWindow().setExitTransition(new Slide(Gravity.LEFT));
        }
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, simple_list_item_1, new ArrayList<String>());
        createNewListView();
        setTaskBarTransparent();
    }

    @Override
    public void onBackPressed() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    @Override
    public void onSwipeHandler() {
        listView.setOnTouchListener(new OnSwipeTouchListener(AllAppsActivity.this) {
            public void onSwipeRight() {
                listView.cancelLongPress();
                onBackPressed();
            }
        });
    }
}