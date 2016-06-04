package com.shier2nd.android.latestdiscounts;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class DiscountItemsActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, DiscountItemsActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return DiscountItemsFragment.newInstance();
    }
}
