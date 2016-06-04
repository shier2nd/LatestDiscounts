package com.shier2nd.android.latestdiscounts;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import java.util.List;

/**
 * Created by Woodinner on 6/3/16.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000 * 60;
//    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

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

        /*QueryPreferences.setAlarmOn(context, isOn);*/
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

        String query = QueryPreferences.getStoredQuery(this);
        // Remember the latest result ID before polling
        String latestResultId = QueryPreferences.getLastResultId(this);
        List<DiscountItem> items;

        if (query == null) {
            items = new SmzdmFetchr().fetchHomeDiscounts(1, "");
        } else {
            items = new SmzdmFetchr().searchDiscounts(0, query);
        }

        if (items.size() == 0) {
            return;
        }

        String newLatestResultId = new SmzdmFetchr().getLatestResultId(query, items);

        // The new latest result ID may not equal to the old one when you Clear Search
        if (!newLatestResultId.equals(latestResultId)
                && newLatestResultId.compareTo(latestResultId) > 0) {

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

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);
            /*showBackgroundNotification(0, notification);*/

            QueryPreferences.setLastResultId(this, newLatestResultId);
        }
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;

        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}
