package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java8.util.Comparators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class FavouriteAppsActivity extends Activity {

    private static final String FAVS = "favourites";
    private static final String SEPARATOR = ",,,";

    private List<String> packageNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private SharedPreferences preferences;
    private List<String> favourites = new ArrayList<String>();

    private ImageView swipeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(
                    SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        getWindow().setFlags(FLAG_LAYOUT_IN_SCREEN, FLAG_LAYOUT_IN_SCREEN);
        setContentView(R.layout.activity_favourites);
        loadFavouritesFromPreferences();
        adapter = createNewAdapter();
        listView = findViewById(R.id.mobile_list);
        swipeIcon = findViewById(R.id.swipe_icon);
        listView.setAdapter(adapter);
        fetchAppList();
    }

    private void loadFavouritesFromPreferences() {
        preferences = getSharedPreferences("light-phone-launcher", 0);
        favourites = Arrays.asList(preferences.getString(FAVS, "").split(SEPARATOR));
        if (favourites.size() == 1 && favourites.get(0) == "") {
            favourites = new ArrayList<>();
        }
    }

    private ArrayAdapter<String> createNewAdapter() {
        return new ArrayAdapter<String>(
                this,
                R.layout.activity_listview,
                new ArrayList<>()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                setTextColoring(view);
                if (position == getCount() - 1 && position != 0) {
                    view.setTextColor(getResources().getColor(R.color.colorTextDimmed));
                }
                return view;
            }
        };
    }

    private void fetchAppList() {
        adapter.clear();

        List<ComponentName> componentNames = new ArrayList<>();

        for (String option : favourites) {
            componentNames.add(new ComponentName(option, option));
        }

        List<String> apps = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            apps.add(getApplicationLabel(componentName));
        }

        apps.add(apps.size() > 0 ? "+" : "+ Add favourites");

        packageNames = new ArrayList<>();
        for (ComponentName componentName : componentNames) {
            String packageName = componentName.getPackageName();
            packageNames.add(packageName);
        }
        for (String app : apps) {
            adapter.add(app);
        }
        setActions();
        AttitudeHelper.applyPadding(listView, ScreenUtils.getDisplay(getApplicationContext()));
    }

    private String getApplicationLabel(ComponentName componentName) {
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
            return getPackageManager().getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "Uninstalled app";
        }
    }

    private void setTextColoring(TextView text) {
        text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        text.setHighlightColor(getResources().getColor(R.color.colorTextPrimary));
    }

    private void setActions() {
        onClickHandler(listView);
        onLongPressHandler(listView);
        onSwipeHandler(listView);
        onClickHandlerForSwipeIcon(swipeIcon);
    }

    private void onClickHandler(ListView listView) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextViewBackground(view, 100L);

            if (position == packageNames.size() || packageNames.get(position).equals("")) {
                showFavouriteModal(listView);
                return;
            }
            String packageName = packageNames.get(position);
            try {
                startActivity(getPackageManager().getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        String.format("Error: Couldn't launch app: %s", packageName),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void onLongPressHandler(ListView listView) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position == packageNames.size() || packageNames.get(position).equals("")) {
                return true;
            }

            toggleTextViewBackground(view, 350L);
            FavouriteAppsActivity favouriteAppsActivity = this;

            runOnUiThread(() -> new Handler().postDelayed(() -> {
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Favourite app removal");
                        alertDialog.setMessage("Do you want to remove this application from your favourites?");
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                favouriteAppsActivity.removeFavourite(position);
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.show();
                    }
                    , 350L));
            return true;
        });
    }

    private void onClickHandlerForSwipeIcon(ImageView imageView) {
        imageView.setOnClickListener((listener) -> onBackPressed());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onSwipeHandler(ListView listView) {
        listView.setOnTouchListener(new OnSwipeTouchListenerMain(this) {
            public void onSwipeTop() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivityIfNeeded(new Intent(getBaseContext(), InstalledAppsActivity.class), 0);
        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
    }

    private void showFavouriteModal(ListView listView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        }
        builder.setTitle("Add favorite app");

        List<String> smallAdapter = new ArrayList<>();
        List<String> smallPackageNames = new ArrayList<>();

        List<ResolveInfo> activities = getActivities();
        Collections.sort(activities, Comparators.comparing(pm -> pm.loadLabel(getPackageManager()).toString().toLowerCase()));


        for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(getPackageManager());
            if (appName.equals("Light Android Launcher") || favourites.contains(resolver.activityInfo.packageName))
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

    private List<ResolveInfo> getActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(getPackageManager()));
        return activities;
    }

    private void updateFavouritesInPreferences() {
        if (packageNames.isEmpty()) {
            preferences.edit().putString(FAVS, "").commit();
        } else {
            preferences.edit().putString(FAVS, TextUtils.join(SEPARATOR, packageNames)).commit();
        }
    }

    private void setFavourite(List<String> smallPackageNames, int which) {
        packageNames.add(smallPackageNames.get(which));
        updateFavouritesInPreferences();
        loadListView();
    }

    private void removeFavourite(int position) {
        if (position < packageNames.size()) {
            packageNames.remove(position);
            updateFavouritesInPreferences();
            loadListView();
        }
    }

    private void loadListView() {
        loadFavouritesFromPreferences();
        fetchAppList();
    }

    private void toggleTextViewBackground(View selectedItem, Long millis) {
        selectedItem.setBackgroundColor(getResources().getColor(R.color.colorBackgroundFavorite));
        new Handler().postDelayed(() -> selectedItem.setBackgroundColor(getResources().getColor(R.color.colorTransparent)), millis);
    }
}
