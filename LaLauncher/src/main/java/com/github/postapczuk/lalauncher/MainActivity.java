package com.github.postapczuk.lalauncher;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java8.util.Comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.layout.simple_list_item_1;
import static android.view.ViewGroup.LayoutParams.FILL_PARENT;

public class MainActivity extends AppsActivity {

    private static final String CONTACTS = "contacts";
    private static final String MESSENGER = "messenger";
    private static final String OPT = "opt%s";
    private static final int OPT_COUNT = 3;

    private SharedPreferences preferences;
    private String contacts = "";
    private String messenger = "";
    private List<String> options = new ArrayList<String>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        preferences = getSharedPreferences("light-phone-launcher", 0);
        contacts = preferences.getString(CONTACTS, "");
        messenger = preferences.getString(MESSENGER, "");
        for (int i = 0; i < OPT_COUNT; i++) {
            options.add(preferences.getString(String.format(OPT, Integer.toString(i + 1)), ""));
        }
        listView = prepareListView();
        setContentView(listView);

        // Set padding for favorite apps list
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
        layoutParams.height = FILL_PARENT;
        listView.setClipToPadding(false);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            GlobalVars.setListPadding((display.getHeight() / 2) - (getTotalHeightofListView() / 2));
            listView.setPadding(GlobalVars.getListPaddingLeft(), GlobalVars.getListPadding(), 0, 0);
        }
    }

    @Override
    public void fetchAppList(ListView listView) {
        adapter.clear();
        packageNames.clear();

        List<ComponentName> componentNames = new ArrayList<>();

        componentNames.add(new ComponentName(contacts, contacts));
        componentNames.add(new ComponentName(messenger, messenger));
        for (int i = 0; i < OPT_COUNT; i++) {
            String option = options.get(i);
            componentNames.add(new ComponentName(option, option));
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

    @Override
    public void onLongPressHandler(ListView listView) {
        listView.setOnItemLongClickListener((parent, view, position, id) ->
                MainActivity.this.showDialogWithApps(position));
    }

    @Override
    public void onSwipeHandler(ListView listView) {
        listView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                startActivity(new Intent(getBaseContext(), AllAppsActivity.class));
            }
        });
    }

    private boolean showDialogWithApps(int position) {
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
            fetchAppList(listView);
        });
        builder.show();
        return true;
    }

    private void setOption(int position, List<String> smallPackageNames, int which) {
        String optionPackage = smallPackageNames.get(which);
        if (position == 0) {
            contacts = optionPackage;
            preferences.edit().putString(CONTACTS, optionPackage).commit();
        } else if (position == 1) {
            messenger = optionPackage;
            preferences.edit().putString(MESSENGER, optionPackage).commit();
        } else {
            options.set(position - 2, optionPackage);
            preferences.edit().putString(String.format(OPT, Integer.toString(position - 1)), optionPackage).commit();
        }
    }

    private String getLabelName(ComponentName componentName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(componentName.getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            return "Long-press to pick app";
        }
    }

    private int getTotalHeightofListView() {
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

    public static class GlobalVars {
        private static int listPadding = 100;
        private static int listPaddingLeft = 100;

        public static int getListPadding() {
            return listPadding;
        }

        public static void setListPadding(int padding) {
            listPadding = padding;
        }

        public static int getListPaddingLeft() {
            return listPaddingLeft;
        }
    }
}
