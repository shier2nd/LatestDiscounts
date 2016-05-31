package com.shier2nd.android.latestdiscounts;

import android.support.v4.app.Fragment;

public class DiscountItemsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return DiscountItemsFragment.newInstance();
    }
}
