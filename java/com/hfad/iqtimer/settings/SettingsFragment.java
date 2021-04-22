package com.hfad.iqtimer.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;


import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.ListSounds;


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String KEY_PREF_SOUND_NUM = "prefsoundnumber";
    private static final String KEY_PREF_SOUND_RES = "prefsoundres";
    private static final String KEY_PREF_SOUND_BREAK_NUM = "prefsoundbreaknumber";
    private static final String KEY_PREF_SOUND_BREAK_RES = "prefsoundbreakres";
    private static final String KEY_PREF_VIBRO_NUM = "prefvibrochoice";
    SettingDialogSounds mDlgSounds;
    SettingDialogSoundsBreak mDlgSoundsBreak;
    SettingDialogVibro mDlgVibro;
    SharedPreferences sPrefSettings;
    SharedPreferences sharedPreferences;
    String mDefValue;
    ListSounds mListSounds;
    Preference preferenceDialogSounds;
    Preference preferenceDialogSoundsBreak;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //создаем из ресурса схемы настроек фрагмент
        addPreferencesFromResource(R.xml.timer_preferences);


        sharedPreferences = getPreferenceScreen()
                .getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        //получаем количество настроек в preferenceScreen
        int count = preferenceScreen.getPreferenceCount();
        mListSounds = new ListSounds();
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        Preference preferenceInterval = findPreference("default_interval");//получаем настройку интервала
        setDefaultSummaryLite(preferenceInterval);
        Preference preferenceBreakTime = findPreference("break_time");//получаем настройку перерыва
        setDefaultSummaryLite(preferenceBreakTime);
        Preference preferencePlan = findPreference("set_plan_day"); //получаем настройку плана
        setDefaultSummaryLite(preferencePlan);
        Preference preferenceSwitch = findPreference("switch_notif"); //получаем настройку switch
        //получаем настройку Диалога выбора мелодии и вибро
        preferenceDialogSounds = findPreference("set_dialog_sounds");
        setDefaultSummary(preferenceDialogSounds, KEY_PREF_SOUND_NUM);
        preferenceDialogSoundsBreak = findPreference("set_dialog_sounds_break");
        setDefaultSummary(preferenceDialogSoundsBreak, KEY_PREF_SOUND_BREAK_NUM);
        Preference preferenceDialogVibro = findPreference("set_dialog_vibro");
        setDefaultSummary(preferenceDialogVibro, KEY_PREF_VIBRO_NUM);

        if (!sPrefSettings.getBoolean("switch_notif",true)){
            preferenceDialogSounds.setVisible(false);
            preferenceDialogSoundsBreak.setVisible(false);
        }
        //назначаем на нее слушателя на нажатие
        preferenceSwitch.setOnPreferenceClickListener(this);
        preferenceDialogSounds.setOnPreferenceClickListener(this);
        preferenceDialogSoundsBreak.setOnPreferenceClickListener(this);
        preferenceDialogVibro.setOnPreferenceClickListener(this);
        //назначаем на нее слушателя для проверки на валидность
        preferenceInterval.setOnPreferenceChangeListener(this);
        preferencePlan.setOnPreferenceChangeListener(this);
        preferenceBreakTime.setOnPreferenceChangeListener(this);

    }

    private void setDefaultSummary(Preference pref, String prefKey) {
        getDefaultSound(prefKey);
        setPreferenceLabel(pref, mDefValue);
    }

    private void setDefaultSummaryLite(Preference pref) {
        mDefValue = sharedPreferences.getString(pref.getKey(),"");
        //устанавливаем значения по умолчанию в Summary
        setPreferenceLabel(pref, mDefValue);
    }


    private String getDefaultSound(String key) {
        int mChoiceSound = sPrefSettings.getInt(key,0);
        switch (key){
            case KEY_PREF_SOUND_BREAK_NUM:
            case KEY_PREF_SOUND_NUM:
                String ListTitle[]=mListSounds.getListTitle(getContext());
                mDefValue = ListTitle[mChoiceSound];
                break;
            case KEY_PREF_VIBRO_NUM:
                String ListTitleVibro[]=mListSounds.getListTitleVibro(getContext());
                mDefValue = ListTitleVibro[mChoiceSound];
            break;
                    }
        return mDefValue;
    }

    //устанавливает название настройки, получает настройку и значение
    private void setPreferenceLabel(Preference preference, String value) {
        switch (preference.getKey()){
            case "default_interval":
            case "break_time":
                preference.setSummary(value + " " + getString(R.string.minut));
                break;
            case "set_plan_day":
                preference.setSummary(value + " " + getString(R.string.sessiy));
                break;
            default:
                preference.setSummary(value);
            }
    }

    //слушает изменение в настройках ПОСЛЕ записи
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value;
        //получаем настройку которую передали по ключу
        Preference preference = findPreference(key);
        switch (key) {
            case KEY_PREF_SOUND_RES:
            case KEY_PREF_SOUND_NUM:
                getDefaultSound(KEY_PREF_SOUND_NUM);
                preference = findPreference("set_dialog_sounds");
                setPreferenceLabel(preference, mDefValue);
                break;
            case KEY_PREF_SOUND_BREAK_RES:
            case KEY_PREF_SOUND_BREAK_NUM:
                getDefaultSound(KEY_PREF_SOUND_BREAK_NUM);
                preference = findPreference("set_dialog_sounds_break");
                setPreferenceLabel(preference, mDefValue);
                break;
            case KEY_PREF_VIBRO_NUM:
                getDefaultSound(KEY_PREF_VIBRO_NUM);
                preference = findPreference("set_dialog_vibro");
                setPreferenceLabel(preference, mDefValue);
                break;
            case "switch_notif":
                if (sharedPreferences.getBoolean(preference.getKey(),true)){
                    preferenceDialogSounds.setVisible(true);
                    preferenceDialogSoundsBreak.setVisible(true);
                } else{
                    preferenceDialogSounds.setVisible(false);
                    preferenceDialogSoundsBreak.setVisible(false);
                }
                break;

            default:
                mDefValue = sharedPreferences.getString(preference.getKey(), "");
                //устанавливаем значение в описание Summary
                setPreferenceLabel(preference, mDefValue);
        }
    }

    //слушает изменение одной конкретной настройки ДО ее записи (для проверки на валидность)
    //если метод возвращает true - значение будет записано, false - нет
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        Toast toast = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            toast = Toast.makeText(getContext(), R.string.sett_e_tost, Toast.LENGTH_LONG);
        }

        if (preference.getKey().equals("default_interval")||preference.getKey().equals("break_time")) {
            String defaultIntervalString =(String) o;

            try {
                //пробуем распознать как целое число
                int defaultInterval = Integer.parseInt(defaultIntervalString);
            } catch (NumberFormatException nef) {
                toast.show();
                return false;
            }
        }
        if (preference.getKey().equals("set_plan_day")) {
            String defaultPlanString =(String) o;

            try {
                //пробуем распознать как целое число
                int defaultInterval = Integer.parseInt(defaultPlanString);
                if (Integer.parseInt(defaultPlanString)>30||Integer.parseInt(defaultPlanString)<1){
                    toast.show();
                    return false;
                }
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "set_dialog_sounds":
                mDlgSounds = new SettingDialogSounds();
                mDlgSounds.show(getParentFragmentManager(), "dialogSoundsChoice");
                break;
            case "set_dialog_sounds_break":
                mDlgSoundsBreak = new SettingDialogSoundsBreak();
                mDlgSoundsBreak.show(getParentFragmentManager(), "dialogSoundsBreakChoice");
                break;
            case "set_dialog_vibro":
                mDlgVibro = new SettingDialogVibro();
                mDlgVibro.show(getParentFragmentManager(), "dialogVibroChoice");
                break;
        }


        return true;
    }
}

