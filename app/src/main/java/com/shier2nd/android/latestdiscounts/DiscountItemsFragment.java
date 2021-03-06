package com.shier2nd.android.latestdiscounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Woodinner on 5/27/16.
 */
public class DiscountItemsFragment extends VisibleFragment {

    private static final String TAG = "DiscountItemsFragment";

    private RecyclerView mDiscountRecyclerView;
    private List<DiscountItem> mItems = new ArrayList<>();
    private boolean mBackgroundIsLoading;
    private int mLastFetchedPage;
    private String mLastItemTimeSort;
    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private boolean mShouldShowProgressBar;
    private boolean mIsInHomePage;
    private LinearLayout mNoItemsView;
    private TextView mNoResultTextView;

    public static DiscountItemsFragment newInstance() {
        return new DiscountItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mIsInHomePage = (QueryPreferences.getStoredQuery(getActivity()) == null);
        mShouldShowProgressBar = true;

        initialUrlParam();
        updateItems();
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
                        // Update the items of new page
                        updateItems();

                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isScrollingUp = (dy > 0);
            }
        });

        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_progress_bar);
        showProgressBar(mShouldShowProgressBar);

        setupAdapter();

        mNoItemsView = (LinearLayout) v.findViewById(R.id.no_items_view);
        mNoResultTextView = (TextView) v.findViewById(R.id.no_result_text_view);
        showItemsView(QueryPreferences.getStoredQuery(getActivity()));

        updateSubtitle(QueryPreferences.getStoredQuery(getActivity()));

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Since calling setRetainInstance(true), when the screen rotation,
        // onCreate() will not called, so should not display ProgressBar
        mShouldShowProgressBar = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_discount_items, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();

        // Avoid having fullscreen keyboard editing on landscape
        int options = mSearchView.getImeOptions();
        mSearchView.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateUI();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Pre-populate the search text box with the saved query when
        // you presses on the search icon to expand the SearchView
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                mSearchView.setQuery(query, false);
            }
        });

        MenuItem clearSearch = menu.findItem(R.id.menu_item_clear);
        clearSearch.setVisible(!mIsInHomePage);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        setToggleItemTitleByVersion(toggleItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                updateUI();
                return true;
            case R.id.menu_item_clear:
                // Clear the query in the Shared Preferences before updating the UI
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateUI();
                return true;
            case R.id.menu_item_toggle_polling:
                startPollingByVersion();
                // Tell DiscountItemActivity to update its toolbar options menu
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.menu_item_settings:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToggleItemTitleByVersion(MenuItem toggleItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (PollService.isServiceAlarmOn(getActivity())) {
                toggleItem.setTitle(R.string.stop_polling);
            } else {
                toggleItem.setTitle(R.string.start_polling);
            }
        } else {
            final int JOB_ID = 1;
            if (PollJobService.isBeenScheduled(JOB_ID, getActivity())) {
                toggleItem.setTitle(R.string.stop_polling);
            } else {
                toggleItem.setTitle(R.string.start_polling);
            }
        }
    }

    private void startPollingByVersion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PollService.startPolling(getActivity());
        } else {
            PollJobService.startPolling(getActivity());
        }
    }

    private void updateUI() {
        // If SearchView is not iconified, collapse the SearchView
        if (!mSearchView.isIconified()) {
            collapseSearchView(getActivity());
        }
        // initialize the url parameters, and
        // then fire the request in the background
        initialUrlParam();
        updateItems();

        updateSubtitle(QueryPreferences.getStoredQuery(getActivity()));
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query, this).execute(mLastFetchedPage + 1);
    }

    private void updateSubtitle(String query) {
        String subtitle = getString(R.string.app_name);
        if (query != null) {
            subtitle = getString(R.string.search_subtitle_format, query);
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null){
            activity.getSupportActionBar().setTitle(subtitle);
        }
    }

    private void initialUrlParam() {
        mLastFetchedPage = 0;
        mLastItemTimeSort = "";
    }

    private void collapseSearchView(Context context) {
        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        // collapse the SearchView
        mSearchView.setIconified(true);
        mSearchView.setIconified(true);
    }

    private void showProgressBar(boolean isShow) {
        if (isShow) {
            mProgressBar.setVisibility(View.VISIBLE);
            mDiscountRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mDiscountRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapter() {
        if (isAdded()) {
            mDiscountRecyclerView.setAdapter(new DiscountAdapter(mItems));
        }
    }

    private void showItemsView(String query) {
        // If there is no matched search result, display no_items_view
        // after completing the background task
        if (!mBackgroundIsLoading && mItems.size() < 1) {
            String noResultText = getString(R.string.no_result, query);
            mNoResultTextView.setText(noResultText);
            mNoItemsView.setVisibility(View.VISIBLE);
            mDiscountRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mNoItemsView.setVisibility(View.INVISIBLE);
            mDiscountRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<DiscountItem>> {
        private String mQuery;
        private DiscountItemsFragment mGalleryFragment;

        public FetchItemsTask(String query, DiscountItemsFragment fragment) {
            mQuery = query;
            mGalleryFragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            mBackgroundIsLoading = true;

            // Show the progress bar when you first launch the app or refresh the items
            if (mGalleryFragment.isResumed() && mLastFetchedPage == 0) {
                mGalleryFragment.showProgressBar(true);
            }

            if (mLastFetchedPage > 0) {
                // Add null, so the adapter will check view_type and show progress bar at bottom
                mItems.add(null);
                mDiscountRecyclerView.getAdapter().notifyItemInserted(mItems.size() - 1);
            }
        }

        @Override
        protected List<DiscountItem> doInBackground(Integer... params) {
            if (mQuery == null) {
                return new SmzdmFetchr().fetchHomeDiscounts(params[0], mLastItemTimeSort);
            } else {
                return new SmzdmFetchr().searchDiscounts((params[0] - 1) * 20, mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<DiscountItem> discountItems) {
            mBackgroundIsLoading = false;

            // Hide the progress bar when you first launch the app or refresh the items
            if (mGalleryFragment.isResumed() && mLastFetchedPage == 0) {
                mGalleryFragment.showProgressBar(false);
            }

            // Hook the RecyclerView up to the data of discounts
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

                if (discountItems.size() != 0) {
                    QueryPreferences.setLastResultId(getActivity(),
                            new SmzdmFetchr().getLatestResultId(mQuery, discountItems));
                }
            }

            // Get time_sort of the last item as the param for the request url of next page
            if (mQuery == null && discountItems.size() != 0) {
                mLastItemTimeSort = discountItems.get(discountItems.size() - 1).getTimeSort();
            }

            // Set the visibility of Clear Search menu option
            mIsInHomePage = (mQuery == null);
            getActivity().invalidateOptionsMenu();

            showItemsView(mQuery);
        }
    }
}
