package com.github.postapczuk.lalauncher;

import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

abstract class AttitudeHelper {
    static ListView applyPadding(ListView listView, Display display) {
        listView.setClipToPadding(false);
        final int displayHeight = display.getHeight();
        final int displayWidth = display.getWidth();
        int heightViewBasedTopPadding = displayHeight / 100;
        if (getTotalHeightOfListView(listView) < displayHeight - heightViewBasedTopPadding) {
            heightViewBasedTopPadding = (displayHeight / 2) - (getTotalHeightOfListView(listView) / 2);
        }
        listView.setPadding(0, heightViewBasedTopPadding, 0, 0);
        return listView;
    }

    static TextView applySpacerPadding(TextView textView, Display display, int width) {
        final int displayWidth = display.getWidth();
        textView.setPadding(0,displayWidth/width,0,0);
        return textView;
    }

    private static int getTotalHeightOfListView(ListView listView) {
        int totalHeight = 0;
        ListAdapter adapter = listView.getAdapter();
        for (int i = 0; i < adapter.getCount() - 1; i++) {
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
