package com.hfad.iqtimer.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;


import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.hfad.iqtimer.R;


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //создаем из ресурса схемы настроек фрагмент
        addPreferencesFromResource(R.xml.timer_preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen()
                .getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        //получаем количество настроек в preferenceScreen
        int count = preferenceScreen.getPreferenceCount();
        //получаем конкретную настройку
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            String value = sharedPreferences.getString(preference.getKey(),
                    "");
            setPreferenceLabel(preference, value);

        }
        //получаем настройку интервала
        Preference preferenceInterval = findPreference("default_interval");
        //назначаем на нее слушателя для проверки на валидность
        preferenceInterval.setOnPreferenceChangeListener(this);
    }

    //устанавливает название настройки, получает настройку и значение
    private void setPreferenceLabel(Preference preference, String value) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    //слушает изменение в настройках ПОСЛЕ записи
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //получаем настройку которую передали по ключу
        Preference preference = findPreference(key);
        //получаем значение настройки
            String value = sharedPreferences.getString(preference.getKey(), "");
            //устанавливаем значение в описание Summary
            setPreferenceLabel(preference, value);
    }

    //слушает изменение одной конкретной настройки ДО ее записи (для проверки на валидность)
    //если метод возвращает true - значение будет записано, false - нет
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        Toast toast = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            toast = Toast.makeText(getContext(), "Please enter an integer number", Toast.LENGTH_LONG);
        }

        if (preference.getKey().equals("default_interval")) {
            String defaultIntervalString =(String) o;

            try {
                //пробуем распознать как целое число
                int defaultInterval = Integer.parseInt(defaultIntervalString);
            } catch (NumberFormatException nef) {
                toast.show();
                return false;
            }
        }

        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // регистрируем слушателя изменения в настройках
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //удаляем слушателя изменения в настройках
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}

