package com.shier2nd.android.latestdiscounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by Woodinner on 6/6/16.
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            boolean isOn = QueryPreferences.isAlarmOn(context);
            PollService.setServiceAlarm(context, isOn);
        }
    }
}
