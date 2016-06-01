package com.shier2nd.android.latestdiscounts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Woodinner on 5/27/16.
 */
public class DiscountItemsFragment extends Fragment {

    private static final String TAG = "DiscountItemsFragment";

    private RecyclerView mDiscountRecyclerView;
    private List<DiscountItem> mItems = new ArrayList<>();
    private boolean mBackgroundIsLoading;
    private int mLastFetchedPage;
    private String mLastItemTimeSort;

    public static DiscountItemsFragment newInstance() {
        return new DiscountItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mLastFetchedPage = 0;
        mLastItemTimeSort = "";
        new FetchItemsTask().execute(mLastFetchedPage + 1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dicount_items, container, false);

        mDiscountRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_discount_items_recycler_view);
        // Use a linear layout manager
        mDiscountRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDiscountRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isScrollingUp = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemsNumber = layoutManager.getItemCount();

                    if (lastItemPosition == (totalItemsNumber - 1)
                            && isScrollingUp && !mBackgroundIsLoading) {
                        /*Log.i(TAG, "is loading page" + (mLastFetchedPage + 1) +
                                ", the current total items number is " + totalItemsNumber);*/
                        /*Toast.makeText(getActivity(),
                                R.string.toast_loading_new_page, Toast.LENGTH_SHORT).show();*/
                        new FetchItemsTask().execute(mLastFetchedPage + 1);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isScrollingUp = (dy > 0);
            }
        });

        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mDiscountRecyclerView.setAdapter(new DiscountAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<DiscountItem>> {
        @Override
        protected void onPreExecute() {
            mBackgroundIsLoading = true;

            if (mLastFetchedPage > 0) {
                // Add null, so the adapter will check view_type and show progress bar at bottom
                mItems.add(null);
                mDiscountRecyclerView.getAdapter().notifyItemInserted(mItems.size() - 1);
            }
        }

        @Override
        protected List<DiscountItem> doInBackground(Integer... params) {
            return new SmzdmFetchr().fetchItems(params[0], mLastItemTimeSort);
        }

        @Override
        protected void onPostExecute(List<DiscountItem> discountItems) {
            mBackgroundIsLoading = false;

            if (mLastFetchedPage++ > 0) {
                // Remove progress item
                mItems.remove(mItems.size() - 1);
                mDiscountRecyclerView.getAdapter().notifyItemRemoved(mItems.size());
                // Add the new page items to mItems
                mItems.addAll(discountItems);
                mDiscountRecyclerView.getAdapter().notifyDataSetChanged();
            } else {
                mItems = discountItems;
                setupAdapter();
            }

            // Get time_sort of the last item as the param for the request url of next page
            mLastItemTimeSort = discountItems.get(discountItems.size() - 1).getTimeSort();
        }
    }
}
