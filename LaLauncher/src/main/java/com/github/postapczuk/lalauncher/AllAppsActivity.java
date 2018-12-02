package com.github.postapczuk.lalauncher;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class AllAppsActivity extends AppsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView = prepareListView();
        setContentView(listView);
      
        // Set padding for all apps list
        listView.setPadding(MainActivity.GlobalVars.getListPaddingLeft(), MainActivity.GlobalVars.getListPadding(), 0, MainActivity.GlobalVars.getListPadding());
        listView.setClipToPadding(false);
      
        // Dim the system bars (API level 14)
        // https://developer.android.com/training/system-ui/dim#java
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onSwipeHandler(ListView listView) {
        listView.setOnTouchListener(new OnSwipeTouchListener(AllAppsActivity.this) {
            public void onSwipeRight() {
                onBackPressed();
            }
        });
    }
}