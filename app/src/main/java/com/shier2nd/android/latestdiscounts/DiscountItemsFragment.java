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

    public static DiscountItemsFragment newInstance() {
        return new DiscountItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dicount_items, container, false);

        mDiscountRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_discount_items_recycler_view);
        mDiscountRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mDiscountRecyclerView.setAdapter(new DiscountAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<DiscountItem>> {
        @Override
        protected List<DiscountItem> doInBackground(Void... params) {
            return new SmzdmFetchr().fetchItems(1);
        }

        @Override
        protected void onPostExecute(List<DiscountItem> discountItems) {
            mItems = discountItems;
            setupAdapter();
        }
    }
}
