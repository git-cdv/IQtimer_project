package com.chkan.iqtimer.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.chkan.iqtimer.CurrentSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.chkan.iqtimer.database.PrefHelper.KEY_PREF_COUNT;
import static com.chkan.iqtimer.database.PrefHelper.KEY_PREF_INTERVAL;
import static com.chkan.iqtimer.database.PrefHelper.KEY_PREF_PLAN;
import static com.chkan.iqtimer.tools.Constants.DEFAULT_COUNT_VALUE;
import static com.chkan.iqtimer.tools.Constants.DEFAULT_PLAN;
import static com.chkan.iqtimer.tools.Constants.DEFAULT_WORK_TIME;

public class App extends Application {

    public static App instance;

    private ExecutorService executor;
    private Context context;
    private static SharedPreferences mPref,mPrefSettings;
    private CurrentSession mCurrentSession;

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

        mCurrentSession = new CurrentSession(DefaultMinutes,DefaultPlan,Count);
    }

    public static App getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    public Context getContext(){return context;}
    public SharedPreferences getPref(){return mPref;}
    public SharedPreferences getPrefSettings(){return mPrefSettings;}
    public CurrentSession getSession(){return mCurrentSession;}
}
