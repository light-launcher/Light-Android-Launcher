package com.github.postapczuk.lalauncher;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java8.util.Comparators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends AppsActivity {

    private static final String FAVS = "favourites";
    private static final String SEPARATOR = ",,,";
    private static final String ADD_APPLICATION = "+ Add favorite app";

    private SharedPreferences preferences;
    private List<String> favourites = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        loadListView();
    }

    @Override
    public void fetchAppList() {
        adapter.clear();
        packageNames.clear();

        List<ComponentName> componentNames = new ArrayList<>();

        for (String option : favourites) {
            componentNames.add(new ComponentName(option, option));
        }

        List<String> apps = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            String labelName = getApplicationLabel(componentName);
            if (labelName != null) apps.add(labelName);
        }
        apps.add(ADD_APPLICATION);

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
    public void onClickHandler() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == packageNames.size() || packageNames.get(position).equals("")) {
                showFavouriteModal();
                return;
            }
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
    }

    @Override
    public void onLongPressHandler() {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            this.removeFavourite(position);
            return true;
        });
    }

    @Override
    public void onSwipeHandler() {
        listView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                startActivity(new Intent(getBaseContext(), AllAppsActivity.class));
            }
        });
    }

    private void loadFavouritesFromPreferences() {
        preferences = getSharedPreferences("light-phone-launcher", 0);
        favourites = Arrays.asList(preferences.getString(FAVS, "").split(SEPARATOR));
        if (favourites.size() == 1 && favourites.get(0) == "") {
            favourites = new ArrayList<>();
        }
    }


    private void showFavouriteModal() {
        AlertDialog.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        }
        builder.setTitle("Pick an app");

        List<String> smallAdapter = new ArrayList<>();
        List<String> smallPackageNames = new ArrayList<>();

        List<ResolveInfo> activities = getActivities(packageManager);
        Collections.sort(activities, Comparators.comparing(pm -> pm.loadLabel(packageManager).toString().toLowerCase()));

        for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Light Android Launcher"))
                continue;
            smallAdapter.add(appName);
            smallPackageNames.add(resolver.activityInfo.packageName);
        }

        builder.setItems(smallAdapter.toArray(new CharSequence[smallAdapter.size()]), (dialog, which) -> {
            setFavourite(smallPackageNames, which);
            fetchAppList();
        });
        builder.show();
    }

    private void setFavourite(List<String> smallPackageNames, int which) {
        packageNames.add(smallPackageNames.get(which));
        updateFavouritesInPreferences();
        loadListView();
    }

    private void removeFavourite(int position) {
        packageNames.remove(position);
        updateFavouritesInPreferences();
        loadListView();
    }

    private void updateFavouritesInPreferences() {
        if (packageNames.isEmpty()) {
            preferences.edit().putString(FAVS, "").commit();
        } else {
            preferences.edit().putString(FAVS, TextUtils.join(SEPARATOR, packageNames)).commit();
        }
    }

    private void loadListView() {
        loadFavouritesFromPreferences();
        createNewListView();
    }

    private String getApplicationLabel(ComponentName componentName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(componentName.getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            return null;
        }
    }
}
