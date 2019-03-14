package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        adapter = new ArrayAdapter<String>(this, simple_list_item_1, new ArrayList<String>()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                applyItemPadding(text);

                // Prevents the color of the text changing on click
                text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                text.setHighlightColor(getResources().getColor(R.color.colorTextPrimary));

                return view;
            }
        };

        createNewListView();
        listView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPrimary));
        setTaskBarTransparent();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }

    @Override
    public void onSwipeHandler() {
        listView.setOnTouchListener(new OnSwipeTouchListenerAllApps(this, listView) {
            public void onSwipeBottom() {
                listView.cancelLongPress();
                onBackPressed();
            }
        });
    }

    // Set the left padding at the item level vs the listview level
    private void applyItemPadding(TextView item){
        Display display = ScreenUtils.getDisplay(getApplicationContext());
        int widthViewBasedLeftPadding = (display.getWidth() / 6);
        item.setPadding(widthViewBasedLeftPadding, 0, 0, 0);
    }
}