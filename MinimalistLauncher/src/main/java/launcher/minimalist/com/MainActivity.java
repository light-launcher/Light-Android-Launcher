package launcher.minimalist.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends Activity {

    private PackageManager packageManager;
    private SharedPreferences preferences;
    private List<String> packageNames = new ArrayList<String>();
    private List<String> adapter = new ArrayList<String>();
    private ListView listView;

    private String option1 = "";
    private String option2 = "";
    private String option3 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        preferences = getSharedPreferences("light-phone-launcher", 0);

        option1 = preferences.getString("opt1", "");
        option2 = preferences.getString("opt2", "");
        option3 = preferences.getString("opt3", "");

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

        List<ComponentName> componentNames = Arrays.asList(
                getComponentName(Intent.ACTION_DIAL, null),
                getComponentName(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
                new ComponentName(option1, option1),
                new ComponentName(option2, option2),
                new ComponentName(option3, option3));

        adapter = componentNames.stream()
                .map(this::getLabelName)
                .collect(Collectors.toList());

        packageNames = componentNames.stream()
                .map(ComponentName::getPackageName)
                .collect(Collectors.toList());

        listView.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        simple_list_item_1,
                        adapter));
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

        List<ResolveInfo> activities = AllApps.getActivities(packageManager);
        activities.sort(Comparator.comparing(pm -> pm.loadLabel(packageManager).toString()));
        activities.forEach(resolver -> {
                    String appName = (String) resolver.loadLabel(packageManager);
                    if (appName.equals("Minimalist Launcher"))
                        return;
                    smallAdapter.add(appName);
                    smallPackageNames.add(resolver.activityInfo.packageName);
                });

        builder.setItems(smallAdapter.toArray(new CharSequence[smallAdapter.size()]), (dialog, which) -> {
            setOption(position, smallPackageNames, which);
            fetchAppList();
        });
        builder.show();
        return true;
    }

    private void setOption(int position, List<String> smallPackageNames, int which) {
        String optionPackage = smallPackageNames.get(which);
        switch (position) {
            case 2:
                option1 = optionPackage;
                preferences.edit().putString("opt1", optionPackage).apply();
                break;
            case 3:
                option2 = optionPackage;
                preferences.edit().putString("opt2", optionPackage).apply();
                break;
            case 4:
                option3 = optionPackage;
                preferences.edit().putString("opt3", optionPackage).apply();
                break;
            default:
                throw new UnknownError();
        }
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
