package com.hfad.iqtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;



public class TimerService extends Service {
    private static final String TAG = "MYLOGS";
    private static final String KEY_TIME = "timedown";
    private static final String KEY_COUNT = "countup";
    private static final String KEY_STOP = "onstop";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String NOTIF_STOP = "com.iqtimer.notif.stop";
    private static final String NOTIF_PAUSE = "com.iqtimer.notif.pause";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String NOTIF_CONTINUE = "com.iqtimer.notif.continue";

    static long sDefaultMillis;
    private static CountDownTimer mTimer;
    MyBinder mBinder = new MyBinder();
    static SharedPreferences sPref;
    static SharedPreferences sPrefSettings;
    String mNotifChannel;
    long mSeconds;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "TimerService: onCreate");
        super.onCreate();
        mNotifChannel = createNotificationChannel();
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        sDefaultMillis = (Integer.valueOf(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TimerService: onStartCommand");
        super.onStartCommand(intent, flags, startId);

        //обработка интента от кнопки Пауза из Notification
        if (NOTIF_PAUSE.equals(intent.getAction())) {
            Log.d(TAG, "TimerService: onStartCommand - NOTIF_PAUSE");
            TimerPause();
            NotificationOnPause();
                    } else {
            //обработка интента от кнопки Стоп из Notification
            if (NOTIF_STOP.equals(intent.getAction())) {
                Log.d(TAG, "TimerService: onStartCommand - NOTIF_STOP");
                TimerStop();
                //отключаем нотификацию
                stopForeground( true );
                //отправляем для отображения в mTextField MainActivity
                Intent i = new Intent("TIMER_UPDATED");
                i.putExtra(KEY_STOP, true);
                sendBroadcast(i);
            } else {
                mTimer = new CountDownTimer(sDefaultMillis, 1000) {
                    public void onTick(long millisUntilFinished) {
                        mSeconds = millisUntilFinished / 1000;
                        String mTime;

                        if (sDefaultMillis >= 3600000) {//если время отчета равно или больше 1 часа, то формат с часами
                            mTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mSeconds / 3600,
                                    (mSeconds % 3600) / 60, (mSeconds % 60));
                        } else {//формат с минутами и секундами
                            mTime = String.format(Locale.getDefault(), "%02d:%02d", mSeconds / 60, mSeconds % 60);
                        }
                        //отправляем для отображения в mTextField MainActivity
                        Intent i = new Intent("TIMER_UPDATED");
                        i.putExtra(KEY_TIME, mTime);
                        sendBroadcast(i);
                        NotificationUpdate(mTime);

                    }

                    public void onFinish() {
                        int mPrefCount = sPref.getInt(KEY_PREF_COUNT, 500);
                        mPrefCount++;
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putInt(KEY_PREF_COUNT, mPrefCount);
                        ed.commit();

                        Intent i = new Intent("TIMER_UPDATED");
                        i.putExtra(KEY_COUNT, mPrefCount);
                        sendBroadcast(i);
                        sDefaultMillis = (Integer.valueOf(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;

                        MediaPlayer mPlayer = MediaPlayer.create(getApplication(),R.raw.bell_sound);
                        mPlayer.start();

                    }
                }.start();
            }
        }

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "TimerService: onBind");
        return mBinder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "TimerService: onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "TimerService: onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TimerService: onDestroy");
        mTimer.cancel();
        super.onDestroy();
    }

    public void TimerStop() {
        mTimer.cancel();
        sDefaultMillis = (Integer.valueOf(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
        //отключаем нотификацию
        stopForeground( true );
        Log.d(TAG, "TimerService: TimerStop()");
    }

    public void TimerPause() {
        mTimer.cancel();
        sDefaultMillis = mSeconds * 1000;
        Log.d(TAG, "TimerService: TimerPause()");
    }

    class MyBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    public void NotificationUpdate(String mTime) {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //интент для кнопки СТОП
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(NOTIF_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        //интент для кнопки ПАУЗА
        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.setAction(NOTIF_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//прилипает оповещение и можно удалить только програмно
                .setContentTitle("Работаем")
                .setContentText(mTime)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, "Stop", stopPendingIntent)
                .addAction(0, "Pause", pausePendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnPause() {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //интент для кнопки СТОП
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(NOTIF_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        //интент для кнопки ПАУЗА
        Intent continueIntent = new Intent(this, TimerService.class);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 0, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//прилипает оповещение и можно удалить только програмно
                .setContentTitle("На паузе")
                .setContentText("Продолжить?")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, "Stop", stopPendingIntent)
                .addAction(0, "Продолжить", continuePendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_service_channelid";
        //название которое видит пользователь в настройках
        String channelName = "IQtimer Notification";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}
