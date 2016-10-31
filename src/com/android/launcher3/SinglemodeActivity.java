package com.android.launcher3;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;


/**
 * Created by chencong.huang on 2016/10/9.
 */
public class SinglemodeActivity extends Activity {
    public static boolean ALL_APP_SINGLEMODE = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LauncherSingleModeFragment())
                .commit();
    }

    public static class LauncherSingleModeFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.all_apps_check);

            final SwitchPreference pref = (SwitchPreference) findPreference(
                    Utilities.ALLOW_SINGLEMODE_PREFERENCE_KEY);
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (pref.isChecked()) {
                        ALL_APP_SINGLEMODE = true;
                    } else {
                        ALL_APP_SINGLEMODE = false;
                    }
                    LauncherAppState.getInstance().getModel().forceReload();
                    return true;
                }
            });
        }
    }
}
