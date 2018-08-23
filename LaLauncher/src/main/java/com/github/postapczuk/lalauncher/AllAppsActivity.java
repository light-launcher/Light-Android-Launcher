package com.github.postapczuk.lalauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class AllAppsActivity extends Activity {

    private PackageManager packageManager;
    private ArrayList<String> packageNames;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup UI elements
        listView = new ListView(this);
        listView.setVerticalScrollBarEnabled(false);
        listView.setId(android.R.id.list);
        listView.setDivider(null);
        setContentView(listView);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
        p.setMargins(100, 0, 0, 0);

        // Get a list of all the apps installed
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        packageNames = new ArrayList<>();

        // Tap on an item in the list to launch the app
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                startActivity(packageManager.getLaunchIntentForPackage(packageNames.get(position)));
            } catch (Exception e) {
                fetchAppList();
            }
        });

        // Long press on an item in the list to open the app settings
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            try {
                // Attempt to launch the app with the package name
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNames.get(position)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                fetchAppList();
            }
            return true;
        });
        fetchAppList();
    }

    private void fetchAppList() {
        // Start from a clean adapter when refreshing the list
        adapter.clear();
        packageNames.clear();

        // Exclude the settings app and this launcher from the list of apps shown
        for (ResolveInfo resolver : AllApps.getActivities(packageManager)) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Settings") || appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }
        listView.setAdapter(adapter);

        // On Swipe back
        listView.setOnTouchListener(new OnSwipeTouchListener(AllAppsActivity.this) {
            public void onSwipeRight() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        fetchAppList();
        this.finish();
        Toast.makeText(AllAppsActivity.this, "Home", Toast.LENGTH_SHORT).show();
    }
}