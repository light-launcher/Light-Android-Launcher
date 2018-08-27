package com.github.postapczuk.lalauncher;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java8.util.Comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends AppsActivity {

    private static final String OPT = "opt%s";

    private SharedPreferences preferences;
    private List<String> options = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();

        // Get a list of all the apps installed
        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        preferences = getSharedPreferences("light-phone-launcher", 0);

        for (int i = 0; i < 3; i++) {
            options.add(preferences.getString(String.format(OPT, Integer.toString(i + 1)), ""));
        }

        prepareListView();
        fetchAppList();
        setActions();
    }

    @Override
    public void onBackPressed() {
        fetchAppList();
    }

    private void prepareListView() {
        listView = new ListView(this);
        listView.setId(android.R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setLongClickable(true);
        setContentView(listView);
        ((ViewGroup.MarginLayoutParams) listView.getLayoutParams())
                .setMargins(60, 100, 0, 0);
    }


    private void fetchAppList() {
        adapter.clear();
        packageNames.clear();

        List<ComponentName> componentNames = new ArrayList<>();
        componentNames.add(getComponentName(Intent.ACTION_DIAL, null));
        componentNames.add(getComponentName(Intent.ACTION_SENDTO, Uri.parse("smsto:")));

        for (int i = 0; i < 3; i++) {
            componentNames.add(new ComponentName(options.get(i), options.get(i)));
        }

        List<String> apps = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            String labelName = getLabelName(componentName);
            apps.add(labelName);
        }
        packageNames = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            String packageName = componentName.getPackageName();
            packageNames.add(packageName);
        }
        adapter = new ArrayAdapter<>(
                this,
                simple_list_item_1,
                apps);
        listView.setAdapter(adapter);
    }

    private void setActions() {
        // On click item
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String packageName = packageNames.get(position);
            try {
                startActivity(packageManager.getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        String.format("Error: Couldn't launch app: %s", packageName),
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Long press on an item in the list to open the app settings
        listView.setOnItemLongClickListener((parent, view, position, id) ->
                MainActivity.this.showDialogWithApps(position));

        // On swipe
        listView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                startActivity(new Intent(getBaseContext(), AllAppsActivity.class));
                Toast.makeText(
                        MainActivity.this,
                        "Applications",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private boolean showDialogWithApps(int position) {
        if (position < 2) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick an app");

        List<String> smallAdapter = new ArrayList<>();
        List<String> smallPackageNames = new ArrayList<>();

        List<ResolveInfo> activities = getActivities(packageManager);
        Collections.sort(activities, Comparators.comparing(pm -> pm.loadLabel(packageManager).toString()));

        for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Light Android Launcher"))
                continue;
            smallAdapter.add(appName);
            smallPackageNames.add(resolver.activityInfo.packageName);
        }

        builder.setItems(smallAdapter.toArray(new CharSequence[smallAdapter.size()]), (dialog, which) -> {
            setOption(position, smallPackageNames, which);
            fetchAppList();
        });
        builder.show();
        return true;
    }

    private void setOption(int position, List<String> smallPackageNames, int which) {
        String optionPackage = smallPackageNames.get(which);
        options.set(position - 2, optionPackage);
        preferences.edit().putString(String.format(OPT, Integer.toString(position - 1)), optionPackage).commit();
    }

    private ComponentName getComponentName(String action, Uri data) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(action);
        if (data != null) {
            sendIntent.setData(data);
        }
        return sendIntent.resolveActivity(packageManager);
    }

    private String getLabelName(ComponentName componentName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    componentName.getPackageName(),
                    0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            return "(EMPTY)";
        }
    }
}
