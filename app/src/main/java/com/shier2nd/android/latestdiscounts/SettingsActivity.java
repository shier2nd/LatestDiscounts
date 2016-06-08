package com.shier2nd.android.latestdiscounts;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Woodinner on 6/7/16.
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
        }
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                findPreference("pref_key_poll_interval").setEnabled(false);
            } else {
                setDefaultValue("pref_key_poll_interval");
                updatePrefSummary("pref_key_poll_interval", R.string.pref_summary_poll_interval);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_key_poll_interval")) {
                updatePrefSummary(key, R.string.pref_summary_poll_interval);

                String value = getPreferenceManager().getSharedPreferences().getString(key, "");
                int interval = Integer.parseInt(value);
                Log.i(TAG, "update the poll interval: " + interval);
                QueryPreferences.setPollInterval(getActivity(), interval);

                PollJobService.startPolling(getActivity());
                PollJobService.startPolling(getActivity());
            }
        }

        private void setDefaultValue(String key) {
            ListPreference pref = (ListPreference) findPreference(key);
            if(pref.getValue() == null){
                pref.setValueIndex(2); //set to index of your default value
            }
        }

        private void updatePrefSummary(String key, int resId) {
            ListPreference pref = (ListPreference) findPreference(key);
            String summary = getString(resId, pref.getEntry());
            pref.setSummary(summary);
        }
    }
}
