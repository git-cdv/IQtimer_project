package com.hfad.iqtimer.progress;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.TimerService;

import org.joda.time.LocalDate;

public class ProgressCountDataIntentService extends IntentService {
    private static final String TAG = "MYLOGS";
    private static final String KEY_COUNT = "countup";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_PLAN = "set_plan_day" ;
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_TIMER_FINISHED = 100;
    private static final String BR_FOR_SIGNALS = "iqtimer.brforsignals";
    private static final String KEY_PREF_SWITCH = "progress.switch";
    private static final String KEY_PREF_PERIOD_TYPE = "progress.period_type";
    private static final int NEW_GOAL = 402;
    private static final String KEY_TASK = "taskforintentservice";
    private static final String KEY_PREF_CURRENT_Q = "progress.q_current";
    private static final String KEY_PREF_QPLAN = "progress.q_plan";
    private static final String KEY_PREF_FINISHED_DONE = "progress.finished_done";
    private static final String KEY_PREF_STARTDATE = "progress.startdate";
    private static final String KEY_PREF_DAYS_PLAN = "progress.period_plan";
    private static final String KEY_PREF_DAYS_ENDED = "progress.days_ended";

    SharedPreferences mPref,sPrefSettings;
    SharedPreferences.Editor ed;
    int mPrefCount;
    boolean isGoalSessions;

    public ProgressCountDataIntentService() {
        super("ProgressCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "ProgressCountDataIntentService(): onHandleIntent");
        //получаем доступ к файлу с данными по дате и сессиям
        mPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        ed = mPref.edit();
        isGoalSessions = mPref.getString(KEY_PREF_PERIOD_TYPE,"").equals("sessions");

        //извлекаем и проверяпм состояние
        int mTask = intent.getIntExtra(KEY_TASK,0);

        switch (mTask) {
            case STATE_TIMER_FINISHED:
                mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);
                mPrefCount++;
                ed.putInt(KEY_PREF_COUNT, mPrefCount);
                ed.apply();

                Intent i = new Intent(BR_FOR_SIGNALS);
                i.putExtra(KEY_COUNT, mPrefCount);
                i.putExtra(KEY_STATE, STATE_TIMER_FINISHED);
                sendBroadcast(i);

                boolean isGoalActive = mPref.getBoolean(KEY_PREF_SWITCH,false);

                if(isGoalActive){
                    if(isGoalSessions){
                    int mCurrentQ = mPref.getInt(KEY_PREF_CURRENT_Q,0);
                    mCurrentQ++;
                    ed.putInt(KEY_PREF_CURRENT_Q, mCurrentQ);
                    ed.apply();
                    isGoalFinished(mCurrentQ);
                    } else {
                        int mPlan = Integer.parseInt(sPrefSettings.getString(KEY_PREF_PLAN,"6"));
                        if(mPrefCount==mPlan) {
                            int mCurrentQ = mPref.getInt(KEY_PREF_CURRENT_Q, 0);
                            mCurrentQ++;
                            ed.putInt(KEY_PREF_CURRENT_Q, mCurrentQ);
                            ed.apply();
                            isGoalFinished(mCurrentQ);
                        }
                    }
                }

                break;
            case NEW_GOAL:

                break;

        }

    }

    private void isGoalFinished(int mCurrentQ) {
            //проверяем выполнение
            int mPlan = Integer.parseInt(mPref.getString(KEY_PREF_QPLAN,"0"));
            if (mCurrentQ>=mPlan){
                ed.putBoolean(KEY_PREF_FINISHED_DONE,true);
                ed.putBoolean(KEY_PREF_SWITCH,false);
                ed.apply();

                ///МОЖНО ОТПРАВИТЬ БАСОМ СОБЫТИЕ О ОКОНЧАНИИ Цели

            } else {
                //проверяем выполнение по времени
                LocalDate mToDay = LocalDate.now();
                String mDate = mPref.getString(KEY_PREF_STARTDATE, mToDay.toString());
                int mPlanDays = Integer.parseInt(mPref.getString(KEY_PREF_DAYS_PLAN, "0"));
                LocalDate mPlanDate = (LocalDate.parse(mDate)).plusDays(mPlanDays);

                if (mToDay.getDayOfYear()>mPlanDate.getDayOfYear()){
                    ed.putBoolean(KEY_PREF_DAYS_ENDED,true);
                    ed.putBoolean(KEY_PREF_SWITCH,false);
                    ed.apply();

                    ///МОЖНО ОТПРАВИТЬ БАСОМ СОБЫТИЕ О ОКОНЧАНИИ Цели

                }
            }

    }

}