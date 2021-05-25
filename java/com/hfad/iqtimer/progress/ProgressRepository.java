package com.hfad.iqtimer.progress;

import android.content.Context;
import android.content.SharedPreferences;

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
    Context context;
    SharedPreferences mPref;
    SharedPreferences.Editor ed;


    public ProgressRepository(Context context) {
        this.context = context;
        mPref = context.getSharedPreferences("progress_pref", MODE_PRIVATE);
    }

    public int [][] getStateP (){
        /*int [] mPlan1 = {2,6,10,14,20,30,40,50,60,80,100};
        int [] mPlan2 = {5,10,30,50,75,100,150,200,250,300,365};*/

        int [] mPlan1 = {2,3,4,5,6,7,8,9,10,11,12};
        int [] mPlan2 = {2,3,4,5,6,7,8,9,10,11,12};

        int mEntLevel = mPref.getInt(KEY_ENTUZIAST_LEVEL,0);
        int [] entuziastArray = {mEntLevel, mPref.getInt(KEY_ENTUZIAST_CURRENT,0), mPlan2[mEntLevel]};

        int mVoinLevel = mPref.getInt(KEY_VOIN_LEVEL,0);
        int [] voinArray = {mVoinLevel, mPref.getInt(KEY_VOIN_CURRENT,0), mPlan1[mVoinLevel]};

        int mBossLevel = mPref.getInt(KEY_BOSS_LEVEL,0);
        int [] bossArray = {mBossLevel, mPref.getInt(KEY_BOSS_CURRENT,0), mPlan1[mBossLevel]};

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

}
