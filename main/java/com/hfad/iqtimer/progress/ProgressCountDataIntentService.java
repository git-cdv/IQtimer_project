package com.hfad.iqtimer.progress;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.TimerService;
import com.hfad.iqtimer.tools.StateEvent;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

public class ProgressCountDataIntentService extends IntentService {
    private static final String TAG = "MYLOGS";
    private static final String KEY_PREMIUM = "isPremium";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_PLAN = "set_plan_day" ;
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int CHECK_COUNTER = 501;
    private static final String KEY_PREF_PERIOD_TYPE = "progress.period_type";
    private static final String KEY_TASK = "taskforintentservice";
    private static final String KEY_PREF_CURRENT_Q = "progress.q_current";
    private static final String KEY_PREF_QPLAN = "progress.q_plan";
    private static final int STATE_GOAL_ACTIVE = 1;
    private static final int STATE_GOAL_DONE = 2;
    private static final String KEY_PREF_GOAL_STATE = "progress.state";
    private static final String KEY_LAST_WORKDAY = "last.workday";

    private static final String KEY_ENTUZIAST_LEVEL = "ENTUZIAST.level";
    private static final String KEY_ENTUZIAST_CURRENT = "ENTUZIAST.current";
    private static final String KEY_VOIN_LEVEL = "VOIN.level";
    private static final String KEY_VOIN_CURRENT = "VOIN.current";
    private static final String KEY_VOIN_LASTDAY = "VOIN.lastday";
    private static final String KEY_BOSS_LEVEL = "BOSS.level";
    private static final String KEY_BOSS_CURRENT = "BOSS.current";
    private static final String KEY_LASTDAY_POKOR = "pokoritel.lastday";
    private static final String KEY_POKORITEL_LEVEL = "POKORITEL.level";
    private static final String KEY_POKORITEL_CURRENT = "POKORITEL.current";
    private static final String KEY_HERO_LEVEL = "HERO.level";
    private static final String KEY_HERO_CURRENT = "HERO.current";
    private static final String KEY_HERO_LASTDAY = "HERO.lastday";
    private static final String KEY_LEGENDA_LEVEL = "LEGENDA.level";
    private static final String KEY_LEGENDA_CURRENT = "LEGENDA.current";
    private static final String KEY_COUNT_WINNER = "winner.count";
    private static final String KEY_COUNTER_CURRENT = "COUNTER.current";
    private static final int STATE_COUNTER_UP = 777;


    SharedPreferences mPref,sPrefSettings,mPrefProgress;
    SharedPreferences.Editor ed,edProgress;
    int mPrefCount;
    boolean isGoalSessions,isPremium;
    LocalDate mToDay;

    public ProgressCountDataIntentService() {
        super("ProgressCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "ProgressCountDataIntentService(): onHandleIntent");
        //получаем доступ к файлу с данными по дате и сессиям
        mPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefProgress = getSharedPreferences("progress_pref", MODE_PRIVATE);
        ed = mPref.edit();
        edProgress= mPrefProgress.edit();
        mToDay = LocalDate.now();

        isGoalSessions = mPref.getString(KEY_PREF_PERIOD_TYPE,"").equals("sessions");
        isPremium = mPref.getBoolean(KEY_PREMIUM, true);
        //извлекаем и проверяпм состояние
        int mTask = intent.getIntExtra(KEY_TASK,0);

        switch (mTask) {
            case STATE_TIMER_FINISHED:
                mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);
                mPrefCount++;
                ed.putInt(KEY_PREF_COUNT, mPrefCount);
                ed.apply();

                EventBus.getDefault().post(new StateEvent(STATE_TIMER_FINISHED));

                if(isPremium){countPokoritel();countVoin();}

                int mPlan = Integer.parseInt(sPrefSettings.getString(KEY_PREF_PLAN,"6"));

                if(mPref.getInt(KEY_PREF_GOAL_STATE,0)==STATE_GOAL_ACTIVE){
                    if(isGoalSessions){
                    int mCurrentQ = mPref.getInt(KEY_PREF_CURRENT_Q,0);
                    mCurrentQ++;
                    ed.putInt(KEY_PREF_CURRENT_Q, mCurrentQ);
                    ed.apply();
                    isGoalFinished(mCurrentQ);
                    } else {
                        if(mPrefCount==mPlan) {
                            int mCurrentQ = mPref.getInt(KEY_PREF_CURRENT_Q, 0);
                            mCurrentQ++;
                            ed.putInt(KEY_PREF_CURRENT_Q, mCurrentQ);
                            ed.apply();
                            isGoalFinished(mCurrentQ);
                        }
                    }
                }

                if(mPrefCount==mPlan) {
                    countMainCounterAndEntuziast();
                    if (isPremium) {countHero();}
                }

                break;
            case CHECK_COUNTER:


                break;

        }

    }

    private void countHero() {
        if(mToDay.getDayOfWeek()==6|mToDay.getDayOfWeek()==7) {
            LocalDate mLastDay = LocalDate.parse(mPrefProgress.getString(KEY_HERO_LASTDAY, "2020-01-01"));
            if(mToDay.getDayOfYear()!=mLastDay.getDayOfYear()) {
                int mCurrentDays = mPrefProgress.getInt(KEY_HERO_CURRENT, 0);
                mCurrentDays++;
                edProgress.putInt(KEY_HERO_CURRENT, mCurrentDays);
                edProgress.putString(KEY_HERO_LASTDAY, mToDay.toString());
                edProgress.apply();

                checkLevelWith100(KEY_HERO_LEVEL, mCurrentDays);
            }
        }
    }

    private void countPokoritel() {
        LocalDate mLastDay = LocalDate.parse(mPrefProgress.getString(KEY_LASTDAY_POKOR, "2020-01-01"));
              if (mLastDay.getDayOfYear()!=mToDay.getDayOfYear()) {
                int mCurrentDays = mPrefProgress.getInt(KEY_POKORITEL_CURRENT, 0);
                mCurrentDays++;
                edProgress.putInt(KEY_POKORITEL_CURRENT, mCurrentDays);
                edProgress.putString(KEY_LASTDAY_POKOR, mToDay.toString());
                edProgress.apply();

                checkLevelWith365(KEY_POKORITEL_LEVEL,mCurrentDays);
            }
    }

    private void countVoin() {
        if(mToDay.getDayOfWeek()==6|mToDay.getDayOfWeek()==7) {
            LocalDate mLastDay = LocalDate.parse(mPrefProgress.getString(KEY_VOIN_LASTDAY, "2020-01-01"));
            if(mToDay.getDayOfYear()!=mLastDay.getDayOfYear()) {
                int mCurrentDays = mPrefProgress.getInt(KEY_VOIN_CURRENT, 0);
                mCurrentDays++;
                edProgress.putInt(KEY_VOIN_CURRENT, mCurrentDays);
                edProgress.putString(KEY_VOIN_LASTDAY,mToDay.toString());
                edProgress.apply();

                checkLevelWith100(KEY_VOIN_LEVEL, mCurrentDays);
            }
        }
    }

    private void checkLevelWith100(String keyLevel, int currentValue) {

        int mCurrentLevel = mPrefProgress.getInt(keyLevel, 0);
        //int [] mPlan = {2,6,10,14,20,30,40,50,60,80,100};
        int [] mPlan = {2,3,4,5,6,7,8,9,10,11,12};
        //проверяем на повышение уровня и на его конец
        if (currentValue!=100){
            if (currentValue==mPlan[mCurrentLevel]){
                edProgress.putInt(keyLevel, ++mCurrentLevel);
                edProgress.apply();
            }
            if(keyLevel.equals(KEY_HERO_LEVEL)) {checkLegenda(KEY_HERO_LEVEL);}
        } else {
            //ЗДЕСЬ ОБРАБОТКА ЗОЛОТОГО ЗНАЧКА И ОСТАНОВКИ ПОДСЧЕТА И СТАТУС ВЫПОЛНЕНИЯ ПОБЕДИТЕЛЯ
            switch (keyLevel) {
                case KEY_VOIN_LEVEL:
                    countWinner();
                    break;
                case KEY_BOSS_LEVEL:
                    countWinner();
                    break;
                case KEY_HERO_LEVEL:
                    countWinner();
                    break;
            }
        }
    }

    private void checkLegenda(String keyLevel) {

        int mLevelLegenda = mPrefProgress.getInt(KEY_LEGENDA_LEVEL, 0);
        int mLevelEntuziast = mPrefProgress.getInt(KEY_ENTUZIAST_LEVEL, 0);
        int mLevelHero = mPrefProgress.getInt(KEY_HERO_LEVEL, 0);
        int mLevelPokoritel = mPrefProgress.getInt(KEY_POKORITEL_LEVEL, 0);

        if (mLevelLegenda<10) {

            switch (keyLevel){
                case KEY_ENTUZIAST_LEVEL:
                    if (mLevelEntuziast == mLevelLegenda + 1){legendaCurrentUp(mLevelLegenda);}
                    break;
                case KEY_HERO_LEVEL:
                    if (mLevelHero == mLevelLegenda + 1){legendaCurrentUp(mLevelLegenda);}
                    break;
                case KEY_POKORITEL_LEVEL:
                    if (mLevelPokoritel == mLevelLegenda + 1){legendaCurrentUp(mLevelLegenda);}
                    break;
            }
        }
    }

    private void legendaCurrentUp(int legendaLevel) {
        int mCurrent = mPrefProgress.getInt(KEY_LEGENDA_CURRENT, 0);
        mCurrent++;
        edProgress.putInt(KEY_LEGENDA_CURRENT, mCurrent);
        edProgress.apply();

        if(mCurrent==3){
            int mLevel = legendaLevel+1;
            edProgress.putInt(KEY_LEGENDA_LEVEL, mLevel);
            edProgress.apply();

            if(mLevel==10){
                //ЗДЕСЬ ОБРАБОТКА ЗОЛОТОГО ЗНАЧКА
                countWinner();
            }
        }
    }

    private void countMainCounterAndEntuziast() {
        LocalDate mYesterday = mToDay.minusDays(1);
        String mLastWorkDay = mPrefProgress.getString(KEY_LAST_WORKDAY, mYesterday.toString());
        LocalDate mLastWorkDayDate = LocalDate.parse(mLastWorkDay);

        //проверяем что сегодня записи еще не было
        if (mLastWorkDayDate.getDayOfYear()!=mToDay.getDayOfYear()) {

            //если вчера было выполнение плана или выходной - то мы добавляем сегоднешнее выполнение
            if (mYesterday.getDayOfYear() == mLastWorkDayDate.getDayOfYear()|mYesterday.getDayOfWeek() == 6 | mYesterday.getDayOfWeek() == 7) {
                int mCurrentCounter = mPrefProgress.getInt(KEY_COUNTER_CURRENT, 0);
                mCurrentCounter++;
                edProgress.putInt(KEY_COUNTER_CURRENT, mCurrentCounter);
                EventBus.getDefault().post(new StateEvent(STATE_COUNTER_UP));
                if (isPremium) {
                    int mCurrentDays = mPrefProgress.getInt(KEY_ENTUZIAST_CURRENT, 0);
                    mCurrentDays++;
                    edProgress.putInt(KEY_ENTUZIAST_CURRENT, mCurrentDays);

                    checkLevelWith365(KEY_ENTUZIAST_LEVEL, mCurrentDays);
                }

            } else {
                 //если не было вчера выполнения - обновляем на 1
                    edProgress.putInt(KEY_COUNTER_CURRENT, 1);
                    if (isPremium) {
                        edProgress.putInt(KEY_ENTUZIAST_CURRENT, 1);
                    }
            }
            edProgress.putString(KEY_LAST_WORKDAY, mToDay.toString());
            edProgress.apply();
        }
    }

    private void checkLevelWith365(String keyLevel, int currentValue) {

        int mCurrentLevel = mPrefProgress.getInt(keyLevel, 0);
        //int [] mPlan = {5,10,30,50,75,100,150,200,250,300,365};
        int [] mPlan = {2,3,4,5,6,7,8,9,10,11,12};

        //проверяем на повышение уровня и на его конец
        if (currentValue!=365){
            if (currentValue==mPlan[mCurrentLevel]){
                edProgress.putInt(keyLevel, ++mCurrentLevel);
                edProgress.apply();
            }
            if(keyLevel.equals(KEY_ENTUZIAST_LEVEL) | keyLevel.equals(KEY_POKORITEL_LEVEL))
            {checkLegenda(keyLevel);}
        } else {
            //ЗДЕСЬ ОБРАБОТКА ЗОЛОТОГО ЗНАЧКА И ОСТАНОВКИ ПОДСЧЕТА И СТАТУС ВЫПОЛНЕНИЯ ПОБЕДИТЕЛЯ
            switch (keyLevel){
                case KEY_ENTUZIAST_LEVEL:
                     countWinner();
                     break;
                case KEY_POKORITEL_LEVEL:
                    countWinner();
                    break;

            }
        }
    }

    private void countWinner() {
        int mCurrent = mPrefProgress.getInt(KEY_COUNT_WINNER, 0);
        edProgress.putInt(KEY_COUNT_WINNER, ++mCurrent);
        edProgress.apply();
    }

    private void isGoalFinished(int mCurrentQ) {
            //проверяем выполнение
            int mPlan = Integer.parseInt(mPref.getString(KEY_PREF_QPLAN,"0"));
            if (mCurrentQ>=mPlan){
                ed.putInt(KEY_PREF_GOAL_STATE,STATE_GOAL_DONE);
                ed.apply();
                if(isPremium){countBoss();}
                ///МОЖНО ОТПРАВИТЬ БАСОМ СОБЫТИЕ О ОКОНЧАНИИ Цели

            }

    }

    private void countBoss() {
        int mCurrent = mPrefProgress.getInt(KEY_BOSS_CURRENT, 0);
        mCurrent++;
        edProgress.putInt(KEY_BOSS_CURRENT, mCurrent);
        edProgress.apply();

        checkLevelWith100(KEY_BOSS_LEVEL,mCurrent);

    }

}