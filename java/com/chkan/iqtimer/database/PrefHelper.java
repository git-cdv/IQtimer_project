package com.chkan.iqtimer.database;

import androidx.preference.PreferenceManager;

import com.chkan.iqtimer.R;

import static com.chkan.iqtimer.tools.Constants.DEFAULT_NEED_COUNT;
import static com.chkan.iqtimer.tools.Constants.DEFAULT_PLAN;
import static com.chkan.iqtimer.tools.Constants.DEFAULT_WORK_TIME;

public class PrefHelper {

    static final String KEY_PREF_INTERVAL = "default_interval";
    static final String KEY_PREF_PLAN = "set_plan_day";
    static final String KEY_PREF_COUNT = "COUNT_value";
    private static final String INTRO_SNACKBAR_STEP = "INTRO_step";
    private static final String KEY_PREF_DATE = "CURRENT_date";
    private static final String KEY_FIRST_RUN = "IS_first_run";
    private static final String KEY_PREF_CURRENT_Q = "GOAL_Q_current";
    private static final String KEY_PREF_NAME = "GOAL_name";
    private static final String KEY_PREF_DESC = "GOAL_desc";
    private static final String KEY_PREF_QPLAN = "GOAL_Q_plan";
    private static final String KEY_PREMIUM = "isPremium";

    public static int getLastIntroStep() {
        return App.instance.getPref().getInt(INTRO_SNACKBAR_STEP, 0);
    }

    public static void setLastIntroStep(int step) {
        App.instance.getPref().edit().putInt(INTRO_SNACKBAR_STEP, step).apply();
    }
    public static String getDefaultTime() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getContext()).getString(KEY_PREF_INTERVAL, DEFAULT_WORK_TIME);
    }
    public static String getDefaultPlan() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getContext()).getString(KEY_PREF_PLAN, DEFAULT_PLAN);
    }

    public static String getWorkDate() {
        return App.instance.getPref().getString(KEY_PREF_DATE, "empty");
    }

    public static boolean isFirstRun() {
        return App.instance.getPref().getBoolean(KEY_FIRST_RUN, true);
    }

    public static void setDateAndCount(String date, int count) {
        App.instance.getPref().edit().putString(KEY_PREF_DATE, date).putInt(KEY_PREF_COUNT,count).apply();
    }

    public static void setFirstRun(boolean b) {
        App.instance.getPref().edit().putBoolean(KEY_FIRST_RUN, b).apply();
    }

    public static int getCount() {
        return App.instance.getPref().getInt(KEY_PREF_COUNT, 0);
    }

    public static int getCurrentQ() {
        return  App.instance.getPref().getInt(KEY_PREF_CURRENT_Q, 0);
    }

    public static String getGoalName() {
        return  App.instance.getPref().getString(KEY_PREF_NAME, App.getInstance().getContext().getResources().getString(R.string.goal_name_empty));
    }

    public static String getGoalDesc() {
        return  App.instance.getPref().getString(KEY_PREF_DESC, App.getInstance().getContext().getString(R.string.goal_desc_empty));
    }

    public static String getPlanQ() {
        return App.instance.getPref().getString(KEY_PREF_QPLAN, "0");
    }

    public static void setPremium() {
        App.instance.getPref().edit().putBoolean(KEY_PREMIUM, true).apply();
    }
}
