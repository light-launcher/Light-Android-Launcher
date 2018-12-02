package com.github.postapczuk.lalauncher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
        listView.setId(android.R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        setActions();
        setTaskBarTransparent();
        applyPadding();
        setContentView(listView);
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

    void applyPadding() {
        listView.setClipToPadding(false);
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            int heightViewBasedTopPadding = (display.getHeight() / 2) - (getTotalHeightOfListView() / 2);
            int widthViewBasedLeftPadding = (display.getWidth() / 6);
            listView.setPadding(widthViewBasedLeftPadding, heightViewBasedTopPadding, 0, 0);
        }
    }

    private int getTotalHeightOfListView() {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, listView);
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += view.getMeasuredHeight();
        }
        return totalHeight + (listView.getDividerHeight() * (adapter.getCount()));
    }

    private void setTaskBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
}
