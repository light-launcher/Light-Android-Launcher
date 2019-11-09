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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.view.Window.FEATURE_ACTIVITY_TRANSITIONS;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

public class InstalledAppsActivity extends Activity {

    private List<String> packageNames = new ArrayList<>();
    private List<String> appNamesPosition = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> packageLookupAdapter;
    private ListView listView;
    public EditText editTextFilter;

    @Override
    protected void onResume() {
        super.onResume();
        if (getActivities(getPackageManager()).size() - 1 != packageNames.size()) {
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
        this.getWindow().setFlags(
                FLAG_LAYOUT_NO_LIMITS,
                FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_installed);

        EditText editTextFilter = (EditText) findViewById(R.id.searchFilter);
        TextView textViewSpacer1 = (TextView) findViewById(R.id.textViewSpacer1);
        TextView textViewSpacer2 = (TextView) findViewById(R.id.textViewSpacer2);

        adapter = createNewAdapter();
        packageLookupAdapter = createNewAdapter();
        listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        fetchAppList();
        AttitudeHelper.applyPadding(listView, ScreenUtils.getDisplay(getApplicationContext()));
        AttitudeHelper.applySpacerPadding(textViewSpacer1, ScreenUtils.getDisplay(getApplicationContext()), 7);
        AttitudeHelper.applySearchPadding(editTextFilter,ScreenUtils.getDisplay(getApplicationContext()));
        AttitudeHelper.applySpacerPadding(textViewSpacer2, ScreenUtils.getDisplay(getApplicationContext()), 25);

        initializeSearchFilter(editTextFilter);
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

    private void initializeSearchFilter(EditText editTextFilter) {
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (InstalledAppsActivity.this).adapter.getFilter().filter(charSequence);
                (InstalledAppsActivity.this).packageLookupAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fetchAppList() {
        packageNames.clear();
        adapter.clear();
        for (ResolveInfo resolver : getActivities(getPackageManager())) {
            String appName = (String) resolver.loadLabel(getPackageManager());
            if (appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            String reference = appName+"||"+resolver.activityInfo.packageName;
            packageLookupAdapter.add(reference);
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

    private String returnPackageName(int position) {
        String[] packageReference = packageLookupAdapter.getItem(position).toString().split("[||]");
        String packageName = packageReference[packageReference.length-1];
        return packageName;
    }

    private void onClickHandler() {

        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 100L);
            String packageName = returnPackageName(position);
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
            String packageName = returnPackageName(position);
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
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
