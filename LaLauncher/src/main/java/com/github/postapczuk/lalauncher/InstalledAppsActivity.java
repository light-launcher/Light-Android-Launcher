package com.github.postapczuk.lalauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class InstalledAppsActivity extends Activity {

    private List<Pair<String, String>> appsPosition = new ArrayList<Pair<String, String>>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private EditText editTextFilter;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    protected void onResume() {
        super.onResume();
        if (getActivities(getPackageManager()).size() - 1 != appsPosition.size()) {
            editTextFilter.getText().clear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_LAYOUT_IN_SCREEN, FLAG_LAYOUT_IN_SCREEN);
        setContentView(R.layout.activity_installed);

        editTextFilter = (EditText) findViewById(R.id.searchFilter);
        int horizontalPadding = ScreenUtils.getDisplay(getApplicationContext()).getWidth() / 12;
        int topPadding = ScreenUtils.getDisplay(getApplicationContext()).getHeight() / 10;
        int bottomPadding = topPadding / 4;
        editTextFilter.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);


        adapter = createNewAdapter();
        listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        fetchAppList();

        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fetchAppList();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String lowered = charSequence.toString().toLowerCase();
                lock.readLock().lock();
                List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
                for (Pair<String, String> entry : appsPosition) {
                    if (lowered.length() == 0) {
                        list.add(entry);
                    } else {
                        for (String word : entry.first.toLowerCase().split("\\s+")) {
                            if (word.startsWith(lowered)) {
                                list.add(entry);
                                break;
                            }
                        }
                    }
                }
                appsPosition = list;
                (InstalledAppsActivity.this).adapter.getFilter().filter(charSequence);
                lock.readLock().unlock();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private ArrayAdapter<String> createNewAdapter() {
        return new ArrayAdapter<String>(
                this,
                R.layout.activity_listview
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
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = getActivities(packageManager);
        lock.writeLock().lock();
        appsPosition.clear();
        adapter.clear();
        for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            appsPosition.add(Pair.create(appName, resolver.activityInfo.packageName));
        }
        listView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPrimary));
        setActions();
        lock.writeLock().unlock();
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
    }

    private void onClickHandler() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String packageName = appsPosition.get(position).second;
                try {
                    InstalledAppsActivity.this.startActivity(InstalledAppsActivity.this.getPackageManager().getLaunchIntentForPackage(packageName));
                } catch (Exception e) {
                    Toast.makeText(
                            InstalledAppsActivity.this,
                            String.format("Couldn't launch %s", packageName),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }
}
