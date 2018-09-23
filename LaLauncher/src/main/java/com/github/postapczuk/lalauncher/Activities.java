package com.github.postapczuk.lalauncher;

import android.widget.ListView;

interface Activities {
    void fetchAppList(ListView listView);

    void onClickHandler(ListView listView);

    void onLongPressHandler(ListView listView);

    void onSwipeHandler(ListView listView);
}
