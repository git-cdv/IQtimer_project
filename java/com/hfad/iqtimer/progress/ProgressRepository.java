package com.hfad.iqtimer.progress;

import android.content.Context;
import android.content.SharedPreferences;

import com.hfad.iqtimer.database.App;

import org.joda.time.LocalDate;

import java.util.LinkedHashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;


public class ProgressRepository {

    private static final String KEY_ENTUZIAST_LEVEL = "ENTUZIAST.level";
    private static final String KEY_ENTUZIAST_CURRENT = "ENTUZIAST.current";
    private static final String KEY_VOIN_LEVEL = "VOIN.level";
    private static final String KEY_VOIN_CURRENT = "VOIN.current";
    private static final String KEY_BOSS_LEVEL = "BOSS.level";
    private static final String KEY_BOSS_CURRENT = "BOSS.current";
    private static final String KEY_HERO_LEVEL = "HERO.level";
    private static final String KEY_HERO_CURRENT = "HERO.current";
    private static final String KEY_POKORITEL_LEVEL = "POKORITEL.level";
    private static final String KEY_POKORITEL_CURRENT = "POKORITEL.current";
    private static final String KEY_LEGENDA_LEVEL = "LEGENDA.level";
    private static final String KEY_LEGENDA_CURRENT = "LEGENDA.current";
    private static final String KEY_WINNER_LEVEL = "WINNER_.level";
    private static final String KEY_WINNER_CURRENT = "WINNER_.current";
    private static final String KEY_COUNTER_CURRENT = "COUNTER_value";
    private static final String KEY_LAST_WORKDAY = "last.workday";

    SharedPreferences mPref;


    public ProgressRepository() {
        mPref = App.getPref();
    }

    public int [][] getStateP (){
        int[] bossArray;
        /*int [] mPlan1 = {2,6,10,14,20,30,40,50,60,80,100};
        int [] mPlan2 = {5,10,30,50,75,100,150,200,250,300,365};*/

        int [] mPlan1 = {2,3,4,5,6,7,8,9,10,11,12};
        int [] mPlan2 = {2,3,4,5,6,7,8,9,10,11,12};

        int mEntLevel = mPref.getInt(KEY_ENTUZIAST_LEVEL,0);
        int [] entuziastArray = {mEntLevel, mPref.getInt(KEY_ENTUZIAST_CURRENT,0), mPlan2[mEntLevel]};

        int mVoinLevel = mPref.getInt(KEY_VOIN_LEVEL,0);
        int [] voinArray = {mVoinLevel, mPref.getInt(KEY_VOIN_CURRENT,0), mPlan1[mVoinLevel]};

        int mBossLevel = mPref.getInt(KEY_BOSS_LEVEL,0);
        if (mBossLevel<11) {
            bossArray = new int[]{mBossLevel, mPref.getInt(KEY_BOSS_CURRENT, 0), mPlan1[mBossLevel]};
        } else {bossArray = new int[]{11, mPref.getInt(KEY_BOSS_CURRENT, 0), mPlan1[10]};}

        int mHeroLevel = mPref.getInt(KEY_HERO_LEVEL,0);
        int [] heroArray = {mHeroLevel, mPref.getInt(KEY_HERO_CURRENT,0), mPlan1[mHeroLevel]};

        int mPokLevel = mPref.getInt(KEY_POKORITEL_LEVEL,0);
        int [] pokoritelArray = {mPokLevel, mPref.getInt(KEY_POKORITEL_CURRENT,0), mPlan2[mPokLevel]};

        int mLegLevel = mPref.getInt(KEY_LEGENDA_LEVEL,0);
        int [] legendaArray = {mLegLevel, mPref.getInt(KEY_LEGENDA_CURRENT,0), 3};

        int mWinnerLevel = mPref.getInt(KEY_WINNER_LEVEL,0);
        int [] winnerArray = {mWinnerLevel, mPref.getInt(KEY_WINNER_CURRENT,0), 6};

        return new int[][]{entuziastArray,voinArray,bossArray,heroArray,pokoritelArray,legendaArray,winnerArray};
    }

    public Integer getCurrentCounter() {
        LocalDate mToday = LocalDate.now();
        LocalDate mYesterday = mToday.minusDays(1);
        String mLastWorkDay = mPref.getString(KEY_LAST_WORKDAY, mYesterday.toString());
        LocalDate mLastWorkDayDate = LocalDate.parse(mLastWorkDay);

        //если сегодня уже было выполнение плана ИЛИ вчера было выполнение плана ИЛИ выходной - то возвращаем текущее значение
        if (mLastWorkDayDate.getDayOfYear()==mToday.getDayOfYear()|mYesterday.getDayOfYear() == mLastWorkDayDate.getDayOfYear()|mYesterday.getDayOfWeek() == 6 | mYesterday.getDayOfWeek() == 7){
            return mPref.getInt(KEY_COUNTER_CURRENT, 0);
        } else {
            SharedPreferences.Editor ed = mPref.edit();
            ed.putInt(KEY_ENTUZIAST_CURRENT, 0);
            ed.apply();
            return 0;
        }
    }

}
