package com.shier2nd.android.latestdiscounts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Woodinner on 6/5/16.
 */
@TargetApi(21)
public class PollJobService extends JobService {
    private static final String TAG = "PollJobService";

    private PollTask mCurrentTask;

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.shier2nd.android.latestdiscounts.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE =
            "com.shier2nd.android.latestdiscounts.PRIVATE";
    public static final String REQUEST_CODE = "REQUSET_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    @TargetApi(21)
    public static void startPolling(Context context) {
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        final int JOB_ID = 1;

        if (isBeenScheduled(JOB_ID, context)){
            Log.i(TAG, "scheduler.cancel(JOB_ID)");
            scheduler.cancel(JOB_ID);
        } else{
            Log.i(TAG, "scheduler.schedule(jobInfo)");
            int pollInterval = QueryPreferences.getPollInterval(context);
            Log.i(TAG, "the poll interval is: " + pollInterval + " ms");
            JobInfo jobInfo = new JobInfo.Builder(
                    JOB_ID, new ComponentName(context, PollJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(pollInterval)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        }
    }

    @TargetApi(21)
    public static boolean isBeenScheduled(int JOB_ID, Context context){
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()){
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return false;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, List<DiscountItem>> {
        private String mQuery;
        private String mResultIdBeforePolling;

        public PollTask() {
            mQuery = QueryPreferences.getStoredQuery(PollJobService.this);
            mResultIdBeforePolling = QueryPreferences.getLastResultId(PollJobService.this);
        }

        @Override
        protected List<DiscountItem> doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];

            Log.i(TAG, "Poll Smzdm for new product");

            List<DiscountItem> items;

            if (mQuery == null) {
                items = new SmzdmFetchr().fetchHomeDiscounts(1, "");
            } else {
                items = new SmzdmFetchr().searchDiscounts(0, mQuery);
            }

            jobFinished(jobParams, false);
            return items;
        }

        @Override
        protected void onPostExecute(List<DiscountItem> items) {

            if (items.size() == 0) {
                return;
            }

            String newResultId = new SmzdmFetchr().getLatestResultId(mQuery, items);

            if (!newResultId.equals(mResultIdBeforePolling)) {

                Resources resources = getResources();
                Intent i = DiscountItemsActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0);

                Notification notification = new NotificationCompat.Builder(PollJobService.this)
                        .setTicker(resources.getString(R.string.new_products_title))
                        .setSmallIcon(R.drawable.ic_notification_discounts)
                        .setContentTitle(resources.getString(R.string.new_products_title))
                        .setContentText(resources.getString(R.string.new_products_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                showBackgroundNotification(0, notification);

                QueryPreferences.setLastResultId(PollJobService.this, newResultId);

                Log.i(TAG, "Got a new result: " + newResultId);
            } else {
                Log.i(TAG, "Got an old result: " + newResultId);
            }
        }
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

}
