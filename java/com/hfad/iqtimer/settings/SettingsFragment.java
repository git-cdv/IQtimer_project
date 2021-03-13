package com.hfad.iqtimer.settings;


import android.os.Bundle;
import android.widget.Toast;


import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hfad.iqtimer.R;


public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //создаем из ресурса схемы настроек фрагмент
        addPreferencesFromResource(R.xml.timer_preferences);

        Preference preference = findPreference("default_interval");
        preference.setOnPreferenceChangeListener(this);
    }

    private void setPreferenceLabel(Preference preference, String value) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        Toast toast = Toast.makeText(getContext(), "Please enter an integer number", Toast.LENGTH_LONG);

        if (preference.getKey().equals("default_interval")) {
            String defaultIntervalString =(String) o;

            try {
                int defaultInterval = Integer.parseInt(defaultIntervalString);
            } catch (NumberFormatException nef) {
                toast.show();
                return false;
            }
        }

        return true;
    }
}
