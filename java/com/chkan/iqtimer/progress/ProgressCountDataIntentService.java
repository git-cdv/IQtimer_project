package com.chkan.iqtimer.progress;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.chkan.iqtimer.CurrentSession;
import com.chkan.iqtimer.database.App;
import com.chkan.iqtimer.tools.StateEvent;
import com.chkan.iqtimer.tools.TimerState;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

public class ProgressCountDataIntentService extends IntentService {


    private static final String TAG = "MYLOGS";
    private static final String KEY_PREMIUM = "isPremium";
    private static final String KEY_PREF_COUNT = "COUNT_value";
    private static final String KEY_PREF_PLAN = "set_plan_day" ;
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int CHECK_COUNTER = 501;
    private static final String KEY_PREF_PERIOD_TYPE = "GOAL_type";
    private static final String KEY_TASK = "taskforintentservice";
    private static final String KEY_PREF_CURRENT_Q = "GOAL_Q_current";
    private static final String KEY_PREF_QPLAN = "GOAL_Q_plan";
    private static final int STATE_GOAL_ACTIVE = 1;
    private static final int STATE_GOAL_DONE = 2;
    private static final String KEY_PREF_GOAL_STATE = "GOAL_state";
    private static final String KEY_LAST_WORKDAY = "last.workday";
    private static final String KEY_ENTUZIAST_LEVEL = "ENTUZIAST.level";
    private static final String KEY_ENTUZIAST_CURRENT = "ENTUZIAST.current";
    private static final String KEY_VOIN_LEVEL = "VOIN.level";
    private static final String KEY_VOIN_CURRENT = "VOIN.current";
    private static final String KEY_VOIN_LASTDAY = "VOIN.lastday";
    private static final String KEY_BOSS_LEVEL = "BOSS.level";
    private static final String KEY_BOSS_CURRENT = "BOSS.current";
    private static final String KEY_LASTDAY_POKOR = "POKORITEL.lastday";
    private static final String KEY_POKORITEL_LEVEL = "POKORITEL.level";
    private static final String KEY_POKORITEL_CURRENT = "POKORITEL.current";
    private static final String KEY_HERO_LEVEL = "HERO.level";
    private static final String KEY_HERO_CURRENT = "HERO.current";
    private static final String KEY_HERO_LASTDAY = "HERO.lastday";
    private static final String KEY_LEGENDA_LEVEL = "LEGENDA.level";
    private static final String KEY_LEGENDA_CURRENT = "LEGENDA.current";
    private static final String KEY_WINNER_CURRENT = "WINNER_.current";
    private static final String KEY_COUNTER_CURRENT = "COUNTER_value";
    private static final String KEY_WINNER_LEVEL = "WINNER_.level";


    SharedPreferences mPref,sPrefSettings;
    SharedPreferences.Editor ed;
    int mPrefCount;
    boolean isGoalSessions,isPremium;
    LocalDate mToDay;
    private final CurrentSession mCurrentSession = App.instance.getSession();

    public ProgressCountDataIntentService() {
        super("ProgressCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "ProgressCountDataIntentService(): onHandleIntent");
        //получаем доступ к файлу с данными по дате и сессиям
        mPref = App.getInstance().getPref();
        sPrefSettings = App.getInstance().getPrefSettings();
        ed = mPref.edit();
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

                mCurrentSession.mCount.set(mPrefCount);
                //отправляем в Мэйн для запуска диалога
                EventBus.getDefault().postSticky(new StateEvent(TimerState.TIMER_FINISHED));

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
            LocalDate mLastDay = LocalDate.parse(mPref.getString(KEY_HERO_LASTDAY, "2020-01-01"));
            if(mToDay.getDayOfYear()!=mLastDay.getDayOfYear()) {
                int mCurrentDays = mPref.getInt(KEY_HERO_CURRENT, 0);
                mCurrentDays++;
                ed.putInt(KEY_HERO_CURRENT, mCurrentDays);
                ed.putString(KEY_HERO_LASTDAY, mToDay.toString());
                ed.apply();

                checkLevelWith100(KEY_HERO_LEVEL, mCurrentDays);
            }
        }
    }

    private void countPokoritel() {
        LocalDate mLastDay = LocalDate.parse(mPref.getString(KEY_LASTDAY_POKOR, "2020-01-01"));
              if (mLastDay.getDayOfYear()!=mToDay.getDayOfYear()) {
                int mCurrentDays = mPref.getInt(KEY_POKORITEL_CURRENT, 0);
                mCurrentDays++;
                ed.putInt(KEY_POKORITEL_CURRENT, mCurrentDays);
                ed.putString(KEY_LASTDAY_POKOR, mToDay.toString());
                ed.apply();

                checkLevelWith365(KEY_POKORITEL_LEVEL,mCurrentDays);
            }
    }

    private void countVoin() {

        if(mToDay.getDayOfWeek()==6|mToDay.getDayOfWeek()==7) {
            LocalDate mLastDay = LocalDate.parse(mPref.getString(KEY_VOIN_LASTDAY, "2020-01-01"));
            if(mToDay.getDayOfYear()!=mLastDay.getDayOfYear()) {
                int mCurrentDays = mPref.getInt(KEY_VOIN_CURRENT, 0);
                mCurrentDays++;
                ed.putInt(KEY_VOIN_CURRENT, mCurrentDays);
                ed.putString(KEY_VOIN_LASTDAY,mToDay.toString());
                ed.apply();

                checkLevelWith100(KEY_VOIN_LEVEL, mCurrentDays);
            }
        }
    }

    private void checkLevelWith100(String keyLevel, int currentValue) {

        int mCurrentLevel = mPref.getInt(keyLevel, 0);
        int [] mPlan = {2,6,10,14,20,30,40,50,60,80,100};

        if (currentValue<100){
            if (currentValue==mPlan[mCurrentLevel]){
                ed.putInt(keyLevel, ++mCurrentLevel);
                ed.apply();
            }
            if(keyLevel.equals(KEY_HERO_LEVEL)) {checkLegenda(KEY_HERO_LEVEL);}

            //когда дошло до последнего значения - назначаем лвл 11 для золотого значка и убрать надпись с Уровнем
        } else if (currentValue==100){
            countWinner();
            ed.putInt(keyLevel, 11);
            ed.apply();
        }
    }

    private void checkLegenda(String keyLevel) {

        int mLevelLegenda = mPref.getInt(KEY_LEGENDA_LEVEL, 0);
        int mLevel = mPref.getInt(keyLevel, 0);

        if (mLevelLegenda<10) {
            if (mLevel == mLevelLegenda + 1){legendaCurrentUp(mLevelLegenda);}
        }
    }

    private void legendaCurrentUp(int legendaLevel) {
        int mCurrent = mPref.getInt(KEY_LEGENDA_CURRENT, 0);
        mCurrent++;
        ed.putInt(KEY_LEGENDA_CURRENT, mCurrent);
        ed.apply();

        if(mCurrent==3){
            //увеличиваем левел Легенды
            int mLevel = legendaLevel+1;

            //проверяем текущие уровни других Достижений
            int mCount = 0;
            int mLevelEntuz = mPref.getInt(KEY_ENTUZIAST_LEVEL, 0);
            if(mLevelEntuz>mLevel){mCount++;}
            int mLevelHero = mPref.getInt(KEY_HERO_LEVEL, 0);
            if(mLevelHero>mLevel){mCount++;}
            int mLevelPokor = mPref.getInt(KEY_POKORITEL_LEVEL, 0);
            if(mLevelPokor>mLevel){mCount++;}

            ed.putInt(KEY_LEGENDA_LEVEL, mLevel);
            ed.putInt(KEY_LEGENDA_CURRENT, mCount);
            ed.apply();

            if(mLevel==10){
                countWinner();
                ed.putInt(KEY_LEGENDA_LEVEL, 11);
                ed.putInt(KEY_LEGENDA_CURRENT, 3);
                ed.apply();
            }
        }
    }

    private void countMainCounterAndEntuziast() {
        LocalDate mYesterday = mToDay.minusDays(1);
        String mLastWorkDay = mPref.getString(KEY_LAST_WORKDAY, mYesterday.toString());
        LocalDate mLastWorkDayDate = LocalDate.parse(mLastWorkDay);

        //проверяем что сегодня записи еще не было
        if (mLastWorkDayDate.getDayOfYear()!=mToDay.getDayOfYear()) {

            //если вчера было выполнение плана или выходной - то мы добавляем сегоднешнее выполнение
            if (mYesterday.getDayOfYear() == mLastWorkDayDate.getDayOfYear()|mYesterday.getDayOfWeek() == 6 | mYesterday.getDayOfWeek() == 7) {
                int mCurrentCounter = mPref.getInt(KEY_COUNTER_CURRENT, 0);
                mCurrentCounter++;
                ed.putInt(KEY_COUNTER_CURRENT, mCurrentCounter);

                if (isPremium) {
                    int mCurrentDays = mPref.getInt(KEY_ENTUZIAST_CURRENT, 0);
                    mCurrentDays++;
                    ed.putInt(KEY_ENTUZIAST_CURRENT, mCurrentDays);

                    checkLevelWith365(KEY_ENTUZIAST_LEVEL, mCurrentDays);
                }

            } else {
                 //если не было вчера выполнения - обновляем на 1
                    ed.putInt(KEY_COUNTER_CURRENT, 1);
                    if (isPremium) {
                        ed.putInt(KEY_ENTUZIAST_CURRENT, 1);
                    }
            }
            ed.putString(KEY_LAST_WORKDAY, mToDay.toString());
            ed.apply();
        }
    }

    private void checkLevelWith365(String keyLevel, int currentValue) {

        int mCurrentLevel = mPref.getInt(keyLevel, 0);

        int [] mPlan = {5,10,30,50,75,100,150,200,250,300,365};

        if (currentValue<365){
            if (currentValue==mPlan[mCurrentLevel]){
                ed.putInt(keyLevel, ++mCurrentLevel);
                ed.apply();
            }
            if(keyLevel.equals(KEY_ENTUZIAST_LEVEL) | keyLevel.equals(KEY_POKORITEL_LEVEL))
            {checkLegenda(keyLevel);}
        }
        if (currentValue==365){
            ed.putInt(keyLevel, 11);
            ed.apply();
            countWinner();
        }
    }

    private void countWinner() {
        int mCurrent = mPref.getInt(KEY_WINNER_CURRENT, 0);
        mCurrent++;
        ed.putInt(KEY_WINNER_CURRENT, mCurrent);
        ed.apply();

        if(mCurrent==6){
            ed.putInt(KEY_WINNER_LEVEL,11);
            ed.apply();
        }
    }

    private void isGoalFinished(int mCurrentQ) {
            //проверяем выполнение
            int mPlan = Integer.parseInt(mPref.getString(KEY_PREF_QPLAN,"0"));
            if (mCurrentQ>=mPlan){
                ed.putInt(KEY_PREF_GOAL_STATE,STATE_GOAL_DONE);
                ed.apply();
                if(isPremium){countBoss();}

            }

    }

    private void countBoss() {
        int mCurrent = mPref.getInt(KEY_BOSS_CURRENT, 0);
        mCurrent++;
        ed.putInt(KEY_BOSS_CURRENT, mCurrent);
        ed.apply();

        checkLevelWith100(KEY_BOSS_LEVEL,mCurrent);

    }

}