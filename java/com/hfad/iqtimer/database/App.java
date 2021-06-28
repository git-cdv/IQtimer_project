package com.hfad.iqtimer.database;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    public static App instance;

    private AppDatabase database;
    private ExecutorService executor;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .build();
        executor = Executors.newFixedThreadPool(3);
        context = getApplicationContext();
    }

    public static App getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
    public ExecutorService getExecutor() {
        return executor;
    }
    public Context getContext(){return context;}
}
