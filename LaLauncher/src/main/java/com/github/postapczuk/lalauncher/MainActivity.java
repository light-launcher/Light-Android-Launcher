package com.github.postapczuk.lalauncher;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java8.util.Comparators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.R.layout.simple_list_item_1;
import static android.view.ViewGroup.LayoutParams.FILL_PARENT;

public class MainActivity extends AppsActivity {

    private static final String FAVS = "favourites";
    private static final int FAVS_MAX_SIZE = 20;
    private static final String SEPARATOR = ",,,";
    private static final String ADD_APPLICATION = "+ add application";

    private SharedPreferences preferences;
    private List<String> favourites = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        preferences = getSharedPreferences("light-phone-launcher", 0);
        for (int i = 0; i < FAVS_MAX_SIZE; i++) {
            favourites = Arrays.asList(preferences.getString(FAVS, "").split(SEPARATOR));
        }
        listView = prepareListView();
        setContentView(listView);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
        layoutParams.height = FILL_PARENT;
        listView.setClipToPadding(false);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            int heightViewBasedTopPadding = (display.getHeight() / 2) - (getTotalHeightofListView() / 2);
            int widthViewBasedLeftPadding = (display.getWidth() / 6);
            listView.setPadding(widthViewBasedLeftPadding, heightViewBasedTopPadding, 0, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    @Override
    public void fetchAppList(ListView listView) {
        adapter.clear();
        packageNames.clear();

        List<ComponentName> componentNames = new ArrayList<>();

        for (String option : favourites) {
            componentNames.add(new ComponentName(option, option));
        }

        List<String> apps = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            String labelName = getLabelName(componentName);
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
    public void onClickHandler(ListView listView) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == packageNames.size() || packageNames.get(position).equals("")) {
                addFavourite();
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
    public void onLongPressHandler(ListView listView) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Boolean aBoolean = MainActivity.this.removeFavourite(position);
            MainActivity.this.fetchAppList(listView);
            return aBoolean;
        });
    }

    @Override
    public void onSwipeHandler(ListView listView) {
        listView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                startActivity(new Intent(getBaseContext(), AllAppsActivity.class));
            }
        });
    }

    private boolean addFavourite() {
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
            fetchAppList(listView);
        });
        builder.show();
        return true;
    }

    private void setFavourite(List<String> smallPackageNames, int which) {
        String favouritePackage = smallPackageNames.get(which);
        packageNames.add(favouritePackage);
        preferences.edit().putString(FAVS, TextUtils.join(SEPARATOR, packageNames)).commit();
    }

    private Boolean removeFavourite(int position) {
        packageNames.remove(position);
        preferences.edit().putString(FAVS, TextUtils.join(SEPARATOR, packageNames)).commit();
        return true;
    }

    private String getLabelName(ComponentName componentName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(componentName.getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            return null;
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
}
