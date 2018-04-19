package launcher.minimalist.com;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(packageNames.get(position)));
                } catch (Exception e) {
                    fetchAppList();
                }
            }
        });

        // Long press on an item in the list to open the app settings
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // Attempt to launch the app with the package name
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + packageNames.get(position)));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    fetchAppList();
                }
                return false;
            }
        });
        fetchAppList();
    }

    private void fetchAppList() {
        // Start from a clean adapter when refreshing the list
        adapter.clear();
        packageNames.clear();

        // Query the package manager for all apps
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        // Sort the applications by alphabetical order and add them to the list
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        for (ResolveInfo resolver : activities) {

            // Exclude the settings app and this launcher from the list of apps shown
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.equals("Settings") || appName.equals("Minimalist Launcher"))
                continue;

            adapter.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Prevent the back button from closing the activity.
        fetchAppList();
    }
}
