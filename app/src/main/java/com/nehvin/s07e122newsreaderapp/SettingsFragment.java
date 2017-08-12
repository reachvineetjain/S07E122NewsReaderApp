package com.nehvin.s07e122newsreaderapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Created by Vineet K Jain on 08-Aug-17.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener{


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++)
        {
            Preference p = preferenceScreen.getPreference(i);
            String value = sharedPreferences.getString("no_of_results_to_show", "");
            setPreferenceSummary(p,value);
        }

        Preference no_of_results_preference = findPreference("no_of_results_to_show");
        no_of_results_preference.setOnPreferenceChangeListener(this);
    }

    private void setPreferenceSummary(Preference preference, String value)
    {
        if(preference instanceof EditTextPreference)
        {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(null != preference){
            String value = sharedPreferences.getString("no_of_results_to_show", "");
            setPreferenceSummary(preference,value);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Toast error = Toast.makeText(getContext(),
                "Please select a number between 1 and 100", Toast.LENGTH_LONG);
        if(preference.getKey().equals("no_of_results_to_show")){
            String stringSize = ((String) (newValue)).trim();
            if(stringSize.equals(""))
                stringSize = "0";
            try {
                int size = Integer.parseInt(stringSize);
                if (size > 100 || size <= 0) {
                    error.show();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "onPreferenceChange: "+ nfe.toString() );
                error.show();
                return false;
            }
        }
        return true;
    }
}