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
        listView.setId(android.R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setSelector(R.color.colorTransparent);
        setActions();
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
            TextView selectedItem = view.findViewById(view.getId());

            // Setting the background resource changes the padding values
            // so we have to reset them after changing the background resource
            final int paddingBottom = selectedItem.getTotalPaddingBottom();
            final int paddingLeft = selectedItem.getTotalPaddingLeft();
            final int paddingRight = selectedItem.getTotalPaddingRight();
            final int paddingTop = selectedItem.getTotalPaddingTop();

            selectedItem.setBackgroundResource(R.drawable.underline);
            selectedItem.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            // Remove underline from selected item
            new Handler().postDelayed(() -> selectedItem.setBackgroundResource(0), 350);

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
            TextView selectedItem = view.findViewById(view.getId());
            selectedItem.setBackgroundResource(R.drawable.underline);

            // Remove underline from selected item after a delay
            new Handler().postDelayed(() -> selectedItem.setBackgroundResource(0), 350);

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

    private void applyPadding() {
        listView.setClipToPadding(false);
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            final int displayHeight = display.getHeight();
            int heightViewBasedTopPadding = displayHeight / 6;
            if (getTotalHeightOfListView() < displayHeight - heightViewBasedTopPadding) {
                heightViewBasedTopPadding = (displayHeight / 2) - (getTotalHeightOfListView() / 2);
            }
            int widthViewBasedSidePadding = (display.getWidth() / 6);

            listView.setPadding(widthViewBasedSidePadding, heightViewBasedTopPadding, widthViewBasedSidePadding, 0);
        }
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
