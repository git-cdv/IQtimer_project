package com.hfad.iqtimer.progress;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.R;

import org.joda.time.LocalDate;

import static android.content.Context.MODE_PRIVATE;

public class GoalRepository {

    private static final String KEY_PREF_NAME = "progress.name";
    private static final String KEY_PREF_DESC = "progress.description";
    private static final String KEY_PREF_QPLAN = "progress.q_plan";
    private static final String KEY_PREF_DAYS_PLAN = "progress.period_plan";
    private static final String KEY_PREF_CURRENTDATE = "progress.currentdate";
    private static final String KEY_PREF_CURRENT_Q = "progress.q_current";
    private static final String KEY_PREF_STARTDATE = "progress.startdate";
    private static final String KEY_PREF_PERIOD_TYPE = "progress.period_type";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_PLAN = "set_plan_day";
    private static final int STATE_GOAL_ACTIVE = 1;
    private static final int STATE_GOAL_DONE = 2;
    private static final int STATE_DAYS_ENDED = -1;
    private static final int STATE_GOAL_OFF = 0;
    private static final String KEY_PREF_GOAL_STATE = "progress.state";
    private static final String KEY_PREMIUM = "isPremium";


    Context context;
    SharedPreferences mPref, mPrefSettings;
    SharedPreferences.Editor ed;


    public GoalRepository(Context context) {
        this.context = context;
        mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
        mPrefSettings = PreferenceManager.getDefaultSharedPreferences(context);
        ed = mPref.edit();
    }

    public String[] readGoal() {
        int currentQ = mPref.getInt(KEY_PREF_CURRENT_Q, 0);

        String name = mPref.getString(KEY_PREF_NAME, context.getResources().getString(R.string.goal_name_empty));
        String desc = mPref.getString(KEY_PREF_DESC, context.getResources().getString(R.string.goal_desc_empty));
        String q_plan = mPref.getString(KEY_PREF_QPLAN, "0");
        String q_current = String.valueOf(currentQ);
        String days_plan = mPref.getString(KEY_PREF_DAYS_PLAN, "0");
        String days_current = getDaysCurrent();

        return new String[]{name, desc, q_current, q_plan, days_current, days_plan};

    }

    public String[] emptyGoal() {
        String name = context.getResources().getString(R.string.goal_name_empty);
        String desc = context.getResources().getString(R.string.goal_desc_empty);
        String q_plan = "0";
        String q_current = "0";
        String days_plan = "0";
        String days_current = "0";

        return new String[]{name, desc, q_plan, q_current, days_plan, days_current};

    }

    private String getDaysCurrent() {
        LocalDate mToDay = LocalDate.now();
        String mDate = mPref.getString(KEY_PREF_STARTDATE, mToDay.toString());
        LocalDate mStartDate = LocalDate.parse(mDate);

        if (mToDay.getDayOfYear() == mStartDate.getDayOfYear()) {
            return "1";
        } else {
            return String.valueOf((mToDay.getDayOfYear() - mStartDate.getDayOfYear())+1);
        }
    }

    public void createNewGoalSes(String name, String desc, String q_plan, String days_plan) {
        String mStartDate = (LocalDate.now()).toString();
        int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);
        ed.putString(KEY_PREF_STARTDATE, mStartDate);
        ed.putString(KEY_PREF_CURRENTDATE, "1");
        ed.putInt(KEY_PREF_GOAL_STATE,STATE_GOAL_ACTIVE);
        ed.putString(KEY_PREF_NAME, name);
        ed.putString(KEY_PREF_DESC, desc);
        ed.putString(KEY_PREF_QPLAN, q_plan);
        ed.putInt(KEY_PREF_CURRENT_Q,mPrefCount);
        ed.putString(KEY_PREF_DAYS_PLAN, days_plan);
        ed.putString(KEY_PREF_PERIOD_TYPE, "sessions");
        ed.apply();
    }

    public String createNewGoalPower(String name, String desc, String q_plan, String days_plan) {
        String mStartDate = (LocalDate.now()).toString();
        ed.putString(KEY_PREF_STARTDATE, mStartDate);
        ed.putInt(KEY_PREF_GOAL_STATE,STATE_GOAL_ACTIVE);
        ed.putString(KEY_PREF_NAME, name);
        ed.putString(KEY_PREF_DESC, desc);
        ed.putString(KEY_PREF_QPLAN, q_plan);
        ed.putString(KEY_PREF_DAYS_PLAN, days_plan);
        ed.putString(KEY_PREF_PERIOD_TYPE, "powers");

        int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);
        int mPlan = Integer.parseInt(mPrefSettings.getString(KEY_PREF_PLAN, "6"));

        if (mPrefCount >= mPlan) {
            ed.putString(KEY_PREF_CURRENTDATE, "1");
            ed.putInt(KEY_PREF_CURRENT_Q, 1);
            ed.apply();
            return "1";
        } else {
            ed.putString(KEY_PREF_CURRENTDATE, "0");
            ed.apply();
            return "0";
        }
    }

    void deleteGoal() {
        ed.putInt(KEY_PREF_GOAL_STATE,STATE_GOAL_OFF);
        ed.putInt(KEY_PREF_CURRENT_Q, 0);
        ed.putString(KEY_PREF_CURRENTDATE, "0");
        ed.apply();
    }

    public int getStateGoal() {
        checkDaysEnded();
        return mPref.getInt(KEY_PREF_GOAL_STATE, STATE_GOAL_OFF);
    }

    private void checkDaysEnded() {
        //проверяем выполнение по времени
        LocalDate mToDay = LocalDate.now();
        String mStartDate = mPref.getString(KEY_PREF_STARTDATE, "1985-12-31");
        int mPlanDays = Integer.parseInt(mPref.getString(KEY_PREF_DAYS_PLAN, "0"));
        LocalDate mPlanDate = (LocalDate.parse(mStartDate)).plusDays(mPlanDays);

        if (mToDay.getDayOfYear()>=mPlanDate.getDayOfYear()){
            ed.putInt(KEY_PREF_GOAL_STATE,STATE_DAYS_ENDED);
            ed.apply();
        }
    }

    public int getCurrentCount() {
        return mPref.getInt(KEY_PREF_COUNT, 0);
    }

    public String getTextGoalDone(int stateGoalDone) {
        if (stateGoalDone==STATE_GOAL_DONE){
            return context.getResources().getString(R.string.textGoalDone);
        } else {
            String startText = context.getResources().getString(R.string.textDaysEnded);
            int currentQ = mPref.getInt(KEY_PREF_CURRENT_Q, 0);
            int planQ = Integer.parseInt(mPref.getString(KEY_PREF_QPLAN, "0"));
            double Percent = ((double) currentQ/(double) planQ)*100;
            int result = (int) Math.round(Percent);
            return startText + result + "%.";
        }
    }

    public boolean isPremium() {
        return mPref.getBoolean(KEY_PREMIUM, true);
    }

}
