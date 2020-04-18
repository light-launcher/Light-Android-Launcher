package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Window.FEATURE_ACTIVITY_TRANSITIONS;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class InstalledAppsActivity extends Activity {

    private List<String> packageNames = new ArrayList<>();
    private List<String> appNamesPosition = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    @Override
    protected void onResume() {
        super.onResume();
        if (appNamesPosition.size() != packageNames.size()) {
            fetchAppList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().requestFeature(
                    FEATURE_ACTIVITY_TRANSITIONS);
        }
        this.getWindow().setFlags(FLAG_LAYOUT_IN_SCREEN, FLAG_LAYOUT_IN_SCREEN);
        setContentView(R.layout.activity_installed);

        EditText editTextFilter = (EditText) findViewById(R.id.searchFilter);
        int horizontalPadding = ScreenUtils.getDisplay(getApplicationContext()).getWidth() / 12;
        int topPadding = ScreenUtils.getDisplay(getApplicationContext()).getHeight() / 10;
        int bottomPadding = topPadding / 4;
        editTextFilter.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);


        adapter = createNewAdapter();
        listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        fetchAppList();

        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (InstalledAppsActivity.this).adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
                return view;
            }
        };
    }

    private void fetchAppList() {
        packageNames.clear();
        adapter.clear();
        PackageManager packageManager = getPackageManager();
        for (ResolveInfo resolver : getActivities(packageManager)) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            appNamesPosition.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }
        listView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPrimary));
        setActions();
    }

    private List<ResolveInfo> getActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        return activities;
    }

    private void setTextColoring(TextView text) {
        text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        text.setHighlightColor(getResources().getColor(R.color.colorTextPrimary));
    }

    private void setActions() {
        onClickHandler();
        onLongPressHandler();
        onSwipeHandler();
    }

    private void onClickHandler() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 100L);
            String packageName = packageNames.get(position);
            try {
                startActivity(getPackageManager().getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(
                        InstalledAppsActivity.this,
                        String.format("Couldn't launch %s", packageName),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void onLongPressHandler() {
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

    @SuppressLint("ClickableViewAccessibility")
    private void onSwipeHandler() {
        listView.setOnTouchListener(new OnSwipeTouchListenerAllApps(this, listView) {
            public void onSwipeBottom() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }

    private void toggleTextviewBackground(View selectedItem, Long millis) {
        selectedItem.setBackgroundColor(getResources().getColor(R.color.colorBackgroundFavorite));
        new Handler().postDelayed(() -> selectedItem.setBackgroundColor(getResources().getColor(R.color.colorTransparent)), millis);
    }
}
