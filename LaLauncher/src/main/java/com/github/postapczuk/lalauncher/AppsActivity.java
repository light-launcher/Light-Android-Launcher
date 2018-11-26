package com.github.postapczuk.lalauncher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
        fetchAppList(listView);
    }

    ListView prepareListView() {
        ListView newListView = new ListView(this);
        newListView.setVerticalScrollBarEnabled(false);
        newListView.setId(android.R.id.list);
        newListView.setDivider(null);
        return setActions(newListView);
    }

    List<ResolveInfo> getActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        return activities;
    }

    private ListView setActions(ListView listView) {
        fetchAppList(listView);
        onClickHandler(listView);
        onLongPressHandler(listView);
        onSwipeHandler(listView);
        return listView;
    }

    public void fetchAppList(ListView listView) {
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

    public void onClickHandler(ListView listView) {
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
    public void onLongPressHandler(ListView listView) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNames.get(position)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                fetchAppList(listView);
            }
            return true;
        });
    }
}
