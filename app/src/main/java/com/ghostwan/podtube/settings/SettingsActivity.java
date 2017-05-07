package com.ghostwan.podtube.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import com.ghostwan.podtube.R;
import com.nononsenseapps.filepicker.Utils;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "Preferences";
    private static final int REQUEST_AUDIO_PATH = 0;
    private static final int REQUEST_VIDEO_PATH = 1;
    private MyPreferenceFragment preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            initPreference(PrefManager.PREFERENCE_THREADS, PrefManager.PREFERENCE_THREADS_DEFAULT, normalCheckListener);
            initPreference(PrefManager.PREFERENCE_DOWNLOAD_AUDIO_FOLDER, PrefManager.getDefaultPath(), normalCheckListener);
            initPreference(PrefManager.PREFERENCE_DOWNLOAD_VIDEO_FOLDER, PrefManager.getDefaultPath(), normalCheckListener);

            Preference button = findPreference("preference_clear");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    PrefManager.clearPref(getContext());
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Are you sure?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                    return true;
                }
            });
        }

        private void initPreference(String preferenceName, String preferenceDefaultValue, Preference.OnPreferenceChangeListener listener) {
            Preference preference = getPreferenceScreen().findPreference(preferenceName);
            preference.setOnPreferenceChangeListener(listener);
            String value = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preferenceName, preferenceDefaultValue);
            listener.onPreferenceChange(preference, value);
        }

        private Preference.OnPreferenceChangeListener normalCheckListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                preference.setSummary(stringValue);
                return true;
            }
        };

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            Log.d("TAG", "onPreferenceTreeClick() called with: preferenceScreen = [" + preferenceScreen + "], preference = [" + preference + "]");
            if (preference.getKey().equals(PrefManager.PREFERENCE_DOWNLOAD_AUDIO_FOLDER) ) {
                PrefManager.showFilePicker(getActivity(), REQUEST_AUDIO_PATH);
            }
            else if (preference.getKey().equals(PrefManager.PREFERENCE_DOWNLOAD_VIDEO_FOLDER) ) {
                PrefManager.showFilePicker(getActivity(), REQUEST_VIDEO_PATH);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void setPreference(String key, String value) {
            SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            defaultPreferences.edit().putString(key, value).apply();
            Preference preference = getPreferenceScreen().findPreference(key);
            preference.setSummary(value);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Request code: "+ requestCode + " result code :"+resultCode );
        if(resultCode == Activity.RESULT_OK) {
                String path = Utils.getFileForUri(data.getData()).getAbsolutePath();
            switch (requestCode) {
                case REQUEST_AUDIO_PATH:
                    preferenceFragment.setPreference(PrefManager.PREFERENCE_DOWNLOAD_AUDIO_FOLDER, path);
                    break;
                case REQUEST_VIDEO_PATH:
                    preferenceFragment.setPreference(PrefManager.PREFERENCE_DOWNLOAD_VIDEO_FOLDER, path);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
