package com.bilalcagdanlioglu.yemekkapinda.Helper;

import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

public class NotificationHelper  extends ContextWrapper {

    private static final String APP_ID ="com.bilalcagdanlioglu.yemekkapinda";
    private static final String APP_NAME="Yemek Kapında";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super( base );
    }
}
