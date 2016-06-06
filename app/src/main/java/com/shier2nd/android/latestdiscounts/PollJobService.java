package com.shier2nd.android.latestdiscounts;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
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

        public PollTask() {
            mQuery = QueryPreferences.getStoredQuery(PollJobService.this);
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

            String latestResultId = QueryPreferences.getLastResultId(PollJobService.this);
            String newLatestResultId = new SmzdmFetchr().getLatestResultId(mQuery, items);

            // The new latest result ID may not equal to the old one when you Clear Search
            if (!newLatestResultId.equals(latestResultId)
                    && newLatestResultId.compareTo(latestResultId) > 0) {

                Resources resources = getResources();
                Intent i = DiscountItemsActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0);

                Notification notification = new NotificationCompat.Builder(PollJobService.this)
                        .setTicker(resources.getString(R.string.new_products_title))
                        .setSmallIcon(R.drawable.ic_sale_discounts)
                        .setContentTitle(resources.getString(R.string.new_products_title))
                        .setContentText(resources.getString(R.string.new_products_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(PollJobService.this);
                notificationManager.notify(0, notification);
            /*showBackgroundNotification(0, notification);*/

                QueryPreferences.setLastResultId(PollJobService.this, newLatestResultId);

                Log.i(TAG, "Got a new result: " + newLatestResultId);
            } else {
                Log.i(TAG, "Got an old result:" + newLatestResultId);
            }
        }
    }

}
