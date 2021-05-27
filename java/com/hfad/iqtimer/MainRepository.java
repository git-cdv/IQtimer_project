package com.hfad.iqtimer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.database.WriteCountDataIntentService;

import java.time.LocalDate;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class MainRepository {

    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String KEY_PREF_PLAN = "set_plan_day" ;
    private static final int STATE_NEXT_ENTRY = 701;
    private static final int STATE_NEW_ENTRY = 700;

    Context context;
    SharedPreferences mPref,mPrefSettings;
    SharedPreferences.Editor ed;
    String mToday;


    public MainRepository(Context context) {
        this.context = context;
        mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
        mPrefSettings = PreferenceManager.getDefaultSharedPreferences(context);
        mToday = (LocalDate.now()).toString();
    }

    public int getState() {
        //если уже была запись в текущий день (т.е день НЕ НОВЫЙ)
        if (mPref.getString(KEY_PREF_DATE, "empty").equals(mToday)) {
            return STATE_NEXT_ENTRY;

        } else {//если первый заход сегодня - записываем данные с прошлого дня
            Intent mIntentService = new Intent(context, WriteCountDataIntentService.class);
            context.startService(mIntentService);
            return STATE_NEW_ENTRY;
        }
    }

    public String getDefaultTime() {
        String mDefaultTime;
        //проверяем настройку с дефолтным интервалом, если ее нет то устанавливается - defValue
        int mDefaultMinutes = Integer.parseInt(mPrefSettings.getString(KEY_PREF_INTERVAL, "45"));

        if (mDefaultMinutes >= 60) {//если время отчета равно или больше 1 часа, то формат с часами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mDefaultMinutes / 60,
                    mDefaultMinutes % 60, 0);
        } else {//формат с минутами и секундами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d", mDefaultMinutes, 0);
        }
        return mDefaultTime;
    }

    public Integer getPlan() {
        return Integer.valueOf(mPrefSettings.getString(KEY_PREF_PLAN, "6"));
    }

    public Integer getCurrentCount() {
        return mPref.getInt(KEY_PREF_COUNT, 0);
    }
}
