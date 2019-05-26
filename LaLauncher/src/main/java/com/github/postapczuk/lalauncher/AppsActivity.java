package com.github.postapczuk.lalauncher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AppsActivity extends Activity implements Activities {

    PackageManager packageManager;
    ArrayList<String> packageNames = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView listView;

    @Override
    public void onBackPressed() {
        fetchAppList();
    }

    void createNewListView() {
        listView = new ListView(this);
        setContentView(listView);
        listView.setId(android.R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setSelector(android.R.color.transparent);
        setActions();
        applyPadding();
    }

    List<ResolveInfo> getActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        return activities;
    }

    private void setActions() {
        fetchAppList();
        onClickHandler();
        onLongPressHandler();
        onSwipeHandler();
    }

    public void fetchAppList() {
        adapter.clear();
        packageNames.clear();

        for (ResolveInfo resolver : getActivities(packageManager)) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }

        listView.setAdapter(adapter);
    }

    public void onClickHandler() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 100L);

            String packageName = packageNames.get(position);
            try {
                startActivity(packageManager.getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(
                        AppsActivity.this,
                        String.format("Couldn't launch %s", packageName),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void onLongPressHandler() {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 350L);

            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNames.get(position)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                fetchAppList();
            }
            return true;
        });
    }

    void toggleTextviewBackground(View selectedItem, Long millis) {
        selectedItem.setBackgroundColor(getResources().getColor(R.color.colorBackgroundFavorite));
        new Handler().postDelayed(() ->
                selectedItem.setBackgroundColor(getResources().getColor(R.color.colorTransparent)),
                millis);
    }

    void setTextColoring(TextView text) {
        text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        text.setHighlightColor(getResources().getColor(R.color.colorTextPrimary));
    }

    private void applyPadding() {
        listView.setClipToPadding(false);
        Display display = ScreenUtils.getDisplay(getApplicationContext());
        final int displayHeight = display.getHeight();
        int heightViewBasedTopPadding = displayHeight / 6;
        if (getTotalHeightOfListView() < displayHeight - heightViewBasedTopPadding) {
            heightViewBasedTopPadding = (displayHeight / 2) - (getTotalHeightOfListView() / 2);
        }

        listView.setPadding(0, heightViewBasedTopPadding, 0, 0);
    }

    // Set the left padding at the item level vs the listview level
    void applyItemPadding(TextView item){
        Display display = ScreenUtils.getDisplay(getApplicationContext());
        int widthViewBasedLeftPadding = (display.getWidth() / 6);
        item.setPadding(widthViewBasedLeftPadding, 0, 0, 0);
    }

    void setTaskBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private int getTotalHeightOfListView() {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount() - 1; i++) {
            View view = adapter.getView(i, null, listView);
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += view.getMeasuredHeight();
        }
        return totalHeight + (listView.getDividerHeight() * (adapter.getCount()));
    }
}
