package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
        }
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, simple_list_item_1, new ArrayList<String>());
        createNewListView();
        listView.setBackgroundColor(Color.BLACK);
        setTaskBarTransparent();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }

    @Override
    public void onSwipeHandler() {
        listView.setOnTouchListener(new OnSwipeTouchListenerAllApps(this) {
            public void onSwipeBottom() {
                listView.cancelLongPress();
                onBackPressed();
            }
        });
    }
}