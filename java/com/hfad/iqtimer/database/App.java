package com.hfad.iqtimer.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.CurrentSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hfad.iqtimer.database.PrefHelper.KEY_COUNTER_CURRENT;
import static com.hfad.iqtimer.database.PrefHelper.KEY_PREF_COUNT;
import static com.hfad.iqtimer.database.PrefHelper.KEY_PREF_INTERVAL;
import static com.hfad.iqtimer.database.PrefHelper.KEY_PREF_PLAN;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_COUNTER_VALUE;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_COUNT_VALUE;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_PLAN;
import static com.hfad.iqtimer.tools.Constants.DEFAULT_WORK_TIME;

public class App extends Application {

    public static App instance;

    private ExecutorService executor;
    private static Context context;
    private static SharedPreferences mPref,mPrefSettings;
    private static CurrentSession mCurrentSession;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        executor = Executors.newFixedThreadPool(3);
        context = getApplicationContext();
        mPref = getSharedPreferences("data_preferences", MODE_PRIVATE);
        mPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);

        String DefaultMinutes =
                mPrefSettings.getString(KEY_PREF_INTERVAL, DEFAULT_WORK_TIME);
        int DefaultPlan = Integer.parseInt(mPrefSettings.getString(KEY_PREF_PLAN, DEFAULT_PLAN));
        int Count = mPref.getInt(KEY_PREF_COUNT, DEFAULT_COUNT_VALUE);
        int Counter = mPref.getInt(KEY_COUNTER_CURRENT, DEFAULT_COUNTER_VALUE);
        boolean IsNeedCount = mPrefSettings.getBoolean("switch_count", true);

        mCurrentSession = new CurrentSession(DefaultMinutes,DefaultPlan,Count,Counter,IsNeedCount);
    }

    public static App getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    public static Context getContext(){return context;}
    public static SharedPreferences getPref(){return mPref;}
    public static SharedPreferences getPrefSettings(){return mPrefSettings;}
    public static CurrentSession getSession(){return mCurrentSession;}
}
