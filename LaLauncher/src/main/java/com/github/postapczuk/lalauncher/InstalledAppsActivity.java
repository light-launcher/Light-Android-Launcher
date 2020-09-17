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
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static android.view.Window.FEATURE_ACTIVITY_TRANSITIONS;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class InstalledAppsActivity extends Activity {

    private List<Pair<String, String>> appsPosition = new ArrayList<>();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().requestFeature(
                    FEATURE_ACTIVITY_TRANSITIONS);
        }
        this.getWindow().setFlags(FLAG_LAYOUT_IN_SCREEN, FLAG_LAYOUT_IN_SCREEN);
        setContentView(R.layout.activity_installed);

        editTextFilter = (EditText) findViewById(R.id.searchFilter);
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
                fetchAppList();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String lowered = charSequence.toString().toLowerCase();
                lock.readLock().lock();
                List<Pair<String, String>> list = new ArrayList<>();
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
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = getActivities(packageManager);

        lock.writeLock().lock();
        appsPosition.clear();
        adapter.clear();

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Callable<Pair<String, String>>> callables = new ArrayList<>();
        for (ResolveInfo resolver : activities) {
            callables.add(() -> {
                String appName = (String) resolver.loadLabel(packageManager);
                if (appName.equals("Light Android Launcher"))
                    return null;
                return Pair.create(appName, resolver.activityInfo.packageName);
            });
        }
        try {
            List<Future<Pair<String, String>>> futures = executorService.invokeAll(callables);
            for (Future<Pair<String, String>> future : futures) {
                Pair<String, String> appNameAndPackage = future.get();
                if (appNameAndPackage != null) {
                    adapter.add(appNameAndPackage.first);
                    appsPosition.add(Pair.create(appNameAndPackage.first, appNameAndPackage.second));
                }
            }
            executorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        } catch (ExecutionException e) {
            throw new RuntimeException("Task execution exception during getting app name");
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
        onLongPressHandler();
        onSwipeHandler();
    }

    private void onClickHandler() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 100L);
            String packageName = appsPosition.get(position).second;
            try {
                Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntentForPackage);
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
                intent.setData(Uri.parse("package:" + appsPosition.get(position).second));
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
