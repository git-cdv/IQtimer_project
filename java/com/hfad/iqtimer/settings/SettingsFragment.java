package com.hfad.iqtimer.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

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
        //получаем количество настроек в preferenceScreen
        mListSounds = new ListSounds();
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(requireContext());

        Preference preferenceInterval = findPreference("default_interval");//получаем настройку интервала
        assert preferenceInterval != null;
        setDefaultSummaryLite(preferenceInterval);
        Preference preferenceBreakTime = findPreference("break_time");//получаем настройку перерыва
        assert preferenceBreakTime != null;
        setDefaultSummaryLite(preferenceBreakTime);
        Preference preferencePlan = findPreference("set_plan_day"); //получаем настройку плана
        assert preferencePlan != null;
        setDefaultSummaryLite(preferencePlan);
        Preference preferenceSwitch = findPreference("switch_notif"); //получаем настройку switch
        //получаем настройку Диалога выбора мелодии и вибро
        preferenceDialogSounds = findPreference("set_dialog_sounds");
        setDefaultSummary(preferenceDialogSounds, KEY_PREF_SOUND_NUM);
        preferenceDialogSoundsBreak = findPreference("set_dialog_sounds_break");
        setDefaultSummary(preferenceDialogSoundsBreak, KEY_PREF_SOUND_BREAK_NUM);
        Preference preferenceDialogVibro = findPreference("set_dialog_vibro");
        setDefaultSummary(preferenceDialogVibro, KEY_PREF_VIBRO_NUM);

        //вытаскиваем EditTextPreference и назначаем его InputType.TYPE_CLASS_NUMBER
        EditTextPreference editTextPreferenceInterval = getPreferenceManager().findPreference("default_interval");
        EditTextPreference editTextPreferenceBreak = getPreferenceManager().findPreference("break_time");
        EditTextPreference editTextPreferencePlan = getPreferenceManager().findPreference("set_plan_day");

        EditTextPreference.OnBindEditTextListener EditTextListener = editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.getText().clear();//очищаем фокус для удобства ввода

        };

        assert editTextPreferenceInterval != null;
        editTextPreferenceInterval.setOnBindEditTextListener(EditTextListener);
        assert editTextPreferenceBreak != null;
        editTextPreferenceBreak.setOnBindEditTextListener(EditTextListener);
        assert editTextPreferencePlan != null;
        editTextPreferencePlan.setOnBindEditTextListener(EditTextListener);


        if (!sPrefSettings.getBoolean("switch_notif",true)){
            preferenceDialogSounds.setVisible(false);
            preferenceDialogSoundsBreak.setVisible(false);
        }
        //назначаем на нее слушателя на нажатие
        assert preferenceSwitch != null;
        preferenceSwitch.setOnPreferenceClickListener(this);
        preferenceDialogSounds.setOnPreferenceClickListener(this);
        preferenceDialogSoundsBreak.setOnPreferenceClickListener(this);
        assert preferenceDialogVibro != null;
        preferenceDialogVibro.setOnPreferenceClickListener(this);
        //назначаем на нее слушателя для проверки на валидность
        preferenceInterval.setOnPreferenceChangeListener(this);
        preferencePlan.setOnPreferenceChangeListener(this);
        preferenceBreakTime.setOnPreferenceChangeListener(this);

    }

    private void setDefaultSummary(Preference pref, String prefKey) {
        setPreferenceLabel(pref, getDefaultSound(prefKey));
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
                String[] ListTitle =mListSounds.getListTitle(requireContext());
                mDefValue = ListTitle[mChoiceSound];
                break;
            case KEY_PREF_VIBRO_NUM:
                String[] ListTitleVibro =mListSounds.getListTitleVibro(requireContext());
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
               //получаем настройку которую передали по ключу
        Preference preference = findPreference(key);
        switch (key) {
            case KEY_PREF_SOUND_RES:
            case KEY_PREF_SOUND_NUM:
                getDefaultSound(KEY_PREF_SOUND_NUM);
                preference = findPreference("set_dialog_sounds");
                assert preference != null;
                setPreferenceLabel(preference, mDefValue);
                break;
            case KEY_PREF_SOUND_BREAK_RES:
            case KEY_PREF_SOUND_BREAK_NUM:
                getDefaultSound(KEY_PREF_SOUND_BREAK_NUM);
                preference = findPreference("set_dialog_sounds_break");
                assert preference != null;
                setPreferenceLabel(preference, mDefValue);
                break;
            case KEY_PREF_VIBRO_NUM:
                getDefaultSound(KEY_PREF_VIBRO_NUM);
                preference = findPreference("set_dialog_vibro");
                assert preference != null;
                setPreferenceLabel(preference, mDefValue);
                break;
            case "switch_notif":
                assert preference != null;
                if (sharedPreferences.getBoolean(preference.getKey(),true)){
                    preferenceDialogSounds.setVisible(true);
                    preferenceDialogSoundsBreak.setVisible(true);
                } else{
                    preferenceDialogSounds.setVisible(false);
                    preferenceDialogSoundsBreak.setVisible(false);
                }
                break;

            default:
                assert preference != null;
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

        if (preference.getKey().equals("set_plan_day")) {
            String defaultPlanString = (String) o;

            if (Integer.parseInt(defaultPlanString) > 30 || Integer.parseInt(defaultPlanString) < 1) {
                assert toast != null;
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

