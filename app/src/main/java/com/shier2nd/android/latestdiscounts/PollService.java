package com.shier2nd.android.latestdiscounts;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Woodinner on 6/3/16.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000 * 60;
//    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.shier2nd.android.latestdiscounts.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE =
            "com.shier2nd.android.latestdiscounts.PRIVATE";
    public static final String REQUEST_CODE = "REQUSET_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    // Any component that wants to start this service should use this method
    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreferences.setAlarmOn(context, isOn);
    }

    // Check whether that PendingIntent exists or not to see whether the alarm is active or not
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        // Remember the result ID before polling
        String resultIdBeforePolling = QueryPreferences.getLastResultId(this);

        String query = QueryPreferences.getStoredQuery(this);
        List<DiscountItem> items;

        if (query == null) {
            items = new SmzdmFetchr().fetchHomeDiscounts(1, "");
        } else {
            items = new SmzdmFetchr().searchDiscounts(0, query);
        }

        if (items.size() == 0) {
            return;
        }

        String newResultId = new SmzdmFetchr().getLatestResultId(query, items);

        if (!newResultId.equals(resultIdBeforePolling)) {

            Resources resources = getResources();
            Intent i = DiscountItemsActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_products_title))
                    .setSmallIcon(R.drawable.ic_sale_discounts)
                    .setContentTitle(resources.getString(R.string.new_products_title))
                    .setContentText(resources.getString(R.string.new_products_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            showBackgroundNotification(0, notification);

            QueryPreferences.setLastResultId(this, newResultId);

            Log.i(TAG, "Got a new result: " + newResultId);
        } else {
            Log.i(TAG, "Got an old result: " + newResultId);
        }
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;

        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}
