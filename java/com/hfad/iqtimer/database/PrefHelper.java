package com.hfad.iqtimer.database;

import androidx.preference.PreferenceManager;

import static com.hfad.iqtimer.tools.Constants.DEFAULT_NEED_COUNT;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_PLAN;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_WORK_TIME;

public class PrefHelper {

    static final String KEY_PREF_INTERVAL = "default_interval";
    static final String KEY_PREF_PLAN = "set_plan_day";
    static final String KEY_PREF_COUNT = "COUNT_value";
    static final String KEY_PREF_COUNTER = "COUNTER_value";
    private static final String INTRO_SNACKBAR_STEP = "INTRO_step";
    private static final String KEY_PREF_DATE = "CURRENT_date";
    private static final String KEY_FIRST_RUN = "IS_first_run";

    public static int getLastIntroStep() {
        return App.getPref().getInt(INTRO_SNACKBAR_STEP, 0);
    }

    public static void setLastIntroStep(int step) {
        App.getPref().edit().putInt(INTRO_SNACKBAR_STEP, step).apply();
    }
    public static String getDefaultTime() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getContext()).getString(KEY_PREF_INTERVAL, DEFAULT_WORK_TIME);
    }
    public static String getDefaultPlan() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getContext()).getString(KEY_PREF_PLAN, DEFAULT_PLAN);
    }

    public static boolean getNeedCount() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getContext()).getBoolean("switch_count", DEFAULT_NEED_COUNT);
    }

    public static String getWorkDate() {
        return App.getPref().getString(KEY_PREF_DATE, "empty");
    }

    public static boolean isFirstRun() {
        return App.getPref().getBoolean(KEY_FIRST_RUN, true);
    }

    public static void setDateAndCount(String date, int count) {
        App.getPref().edit().putString(KEY_PREF_DATE, date).putInt(KEY_PREF_COUNT,count).apply();
    }

    public static void setFirstRun(boolean b) {
        App.getPref().edit().putBoolean(KEY_FIRST_RUN, b).apply();
    }

    public static int getCount() {
        return App.getPref().getInt(KEY_PREF_COUNT, 0);
    }
}
