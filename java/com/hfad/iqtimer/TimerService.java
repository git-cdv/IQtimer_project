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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.database.ListSounds;
import com.hfad.iqtimer.database.WriteCountDataIntentService;
import com.hfad.iqtimer.progress.ProgressCountDataIntentService;

import java.util.Locale;

public class TimerService extends Service {
    private static final String TAG = "MYLOGS";
    private static final String KEY_TIME = "timedown";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String KEY_PREF_BREAKTIME = "break_time";
    private static final String KEY_PREF_SOUND_RES = "prefsoundres";
    private static final String KEY_PREF_SOUND_BREAK_RES = "prefsoundbreakres";
    private static final String KEY_PREF_CHANGE = "iqtimer.timerchange";
    private static final String KEY_PREF_VIBRO_NUM = "prefvibrochoice";
    private static final String BR_FOR_SIGNALS = "iqtimer.brforsignals";
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_TIMER_WORKING = 500;
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int STATE_TIMER_WAIT = 101;
    private static final int ST_TIMER_STOPED = 200;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_BREAK_ENDED = 300;
    private static final int ST_NOTIF_PAUSED = 600;
    private static final int ST_NOTIF_STOPED = 700;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final int ST_NOTIF_BREAK_STOPED = 900;
    private static final String KEY_TASK = "taskforintentservice";

    static private long mTimeLeftInMillis;
    static private long mBreakTimeInMillis;
    static private long mDefaultTimeInMillis;
    private static CountDownTimer mTimer;
    MyBinder mBinder = new MyBinder();
    static SharedPreferences sPref;
    static SharedPreferences sPrefSettings;
    String mNotifChannel;
    long mSeconds;
    static boolean isBreak = false;
    Intent iTimeUpdateOnUI;
    static int mSTATE=0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "TimerService: onCreate");
        super.onCreate();
        mNotifChannel = createNotificationChannel();
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
        mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
        iTimeUpdateOnUI = new Intent("TIMER_UPDATED");
        mTimeLeftInMillis = mDefaultTimeInMillis;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TimerService: onStartCommand startId -"+startId+", mSTATE - "+mSTATE);
        super.onStartCommand(intent, flags, startId);

        if (sPref.getBoolean(KEY_PREF_CHANGE,false)){
            mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
            mTimeLeftInMillis = mDefaultTimeInMillis;
            SharedPreferences.Editor ed = sPref.edit();
            ed.putBoolean(KEY_PREF_CHANGE, false);
            ed.apply();
        }

        //извлекаем и проверяпм состояние
        int mState = intent.getIntExtra(KEY_STATE,0);

        switch (mState){
            case STATE_TIMER_WORKING:
                Log.d(TAG, "TimerService: onStartCommand - ST_TIMER_STARTED");
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                mSTATE = STATE_TIMER_WORKING;
                break;
            case ST_NOTIF_PAUSED: //обработка интента от кнопки Пауза из Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_PAUSED");
                TimerPause();
                NotificationOnPause();
                break;
            case ST_NOTIF_STOPED: //обработка интента от кнопки Стоп из Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_STOPED");
                TimerStop();
                stopForeground( true ); //отключаем нотификацию
                //отправляем для отображения в mTextField MainActivity
                Intent i = new Intent(BR_FOR_SIGNALS);
                i.putExtra(KEY_STATE,ST_TIMER_STOPED);
                sendBroadcast(i);
                break;
            case STATE_BREAK_STARTED: //обработка интента для перерыва
                Log.d(TAG, "TimerService: onStartCommand - ST_BREAK_STARTED");
                mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
                mTimeLeftInMillis=mBreakTimeInMillis;
                isBreak = true;
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                mSTATE = STATE_BREAK_STARTED;
                //закрываем диалог
                Intent i2 = new Intent(BR_FOR_SIGNALS);
                i2.putExtra(KEY_STATE, ST_BREAK_STARTED_IN_NOTIF);
                sendBroadcast(i2);
                break;
            case ST_NOTIF_BREAK_STOPED: //обработка интента от кнопки Стоп из Break Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_BREAK_STOPED");
                TimerStop();
                stopForeground( true ); //отключаем нотификацию
                isBreak=false;
                mTimeLeftInMillis = mDefaultTimeInMillis;
                //отправляем для отображения в mTextField MainActivity
                Intent i3 = new Intent(BR_FOR_SIGNALS);
                i3.putExtra(KEY_STATE,ST_TIMER_STOPED);
                sendBroadcast(i3);
                mSTATE = STATE_TIMER_WAIT;
                break;
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
        mTimeLeftInMillis = mDefaultTimeInMillis;
        //отключаем нотификацию
        stopForeground( true );
        Log.d(TAG, "TimerService: TimerStop()");
    }

    public void TimerPause() {
        mTimer.cancel();
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
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0);
        //интент для кнопки СТОП
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, 0);
        //интент для кнопки ПАУЗА
        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.putExtra(KEY_STATE,ST_NOTIF_PAUSED);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 3, pauseIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.dowork))
                .setContentText(mTime)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.stop), stopPendingIntent)
                .addAction(0, getString(R.string.pause), pausePendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnPause() {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 4, notificationIntent, 0);
        //интент для кнопки СТОП
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 5, stopIntent, 0);
        //интент для кнопки ПРОДОЛЖИТЬ
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_TIMER_WORKING);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 6, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(false)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.on_pause))
                .setContentText(getString(R.string.qest_continue))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.stop), stopPendingIntent)
                .addAction(0, getString(R.string.dialog_continue), continuePendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnSessionEnd() {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, notificationIntent, 0);
        //интент для кнопки Начать перерыв
        Intent startBreakIntent = new Intent(this, TimerService.class);
        startBreakIntent.putExtra(KEY_STATE,STATE_BREAK_STARTED);
        PendingIntent startBreakPendingIntent = PendingIntent.getService(this, 8, startBreakIntent, 0);
        //интент для кнопки Пропустить перерыв
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_TIMER_WORKING);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 9, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(false)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.dialog_session_end))
                .setContentText(getString(R.string.qest_break))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.dialog_rest_start), startBreakPendingIntent)
                .addAction(0, getString(R.string.dialog_rest_reset), continuePendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnBreak(String mTime) {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 13, notificationIntent, 0);
        //интент для кнопки СТОП
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_BREAK_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 14, stopIntent, 0);


        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.break_time))
                .setContentText(mTime)
                .setSmallIcon(R.drawable.ic_baseline_breaktime_8)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.stop), stopPendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnBreakEnd() {

        //интент для перехода к MainActivity после нажатия на Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, notificationIntent, 0);
        //интент для кнопки Продолжить работу
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_TIMER_WORKING);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 12, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(false)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.dialog_break_end))
                .setContentText(getString(R.string.qest_continue))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.dialog_rest_end), continuePendingIntent)
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
    //возвращает текущее состояние сервиса
    int getSTATEinService (){
        return mSTATE;
    }

    //устанавливает состояние сервиса
    int setSTATEinService (int state){
        mSTATE = state;
        return mSTATE;
    }

    class Timer extends CountDownTimer{

        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onTick(long millisUntilFinished) {
            mTimeLeftInMillis = millisUntilFinished;
            mSeconds = mTimeLeftInMillis/1000;
            String mTime;

            if (mTimeLeftInMillis >= 3600000) {//если время отчета равно или больше 1 часа, то формат с часами
                mTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mSeconds  / 3600,
                        (mSeconds  % 3600) / 60, (mSeconds % 60));
            } else {//формат с минутами и секундами
                mTime = String.format(Locale.getDefault(), "%02d:%02d", mSeconds  / 60, mSeconds % 60);
            }
            //отправляем для отображения в mTextField MainActivity
            iTimeUpdateOnUI.putExtra(KEY_TIME, mTime);
            sendBroadcast(iTimeUpdateOnUI);

            if (!isBreak){
            NotificationUpdate(mTime);
            } else {
                NotificationOnBreak(mTime);
            }

        }

        public void onFinish() {
            if (!isBreak){
                Intent mIntentService = new Intent(getApplicationContext(), ProgressCountDataIntentService.class);
                mIntentService.putExtra(KEY_TASK,STATE_TIMER_FINISHED);
                startService(mIntentService);
            mSTATE = STATE_TIMER_FINISHED;
            NotificationOnSessionEnd();
            mTimeLeftInMillis = mDefaultTimeInMillis;
            startSoundForNotif(STATE_TIMER_FINISHED);
            startVibrator();
            } else {
                //для окончания перерыва
                Intent i = new Intent(BR_FOR_SIGNALS);
                i.putExtra(KEY_STATE, STATE_BREAK_ENDED);
                sendBroadcast(i);
                mTimeLeftInMillis = mDefaultTimeInMillis;
                isBreak = false;
                NotificationOnBreakEnd();
                mSTATE = STATE_BREAK_ENDED;
                startSoundForNotif(STATE_BREAK_ENDED);
                startVibrator();
            }

        }
    }



    private void startSoundForNotif(int State) {
        if (sPrefSettings.getBoolean("switch_notif",true)) {
            int mSoundRes;
            if (State == STATE_TIMER_FINISHED) {
                mSoundRes = sPrefSettings.getInt(KEY_PREF_SOUND_RES, 0);
            } else {
                mSoundRes = sPrefSettings.getInt(KEY_PREF_SOUND_BREAK_RES, 0);
            }

            if (mSoundRes == 0) {
                //получаем дефолтную мелодию
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mPlayer = MediaPlayer.create(getApplication(), defaultSoundUri);
                mPlayer.start();
            } else {
                MediaPlayer mPlayer = MediaPlayer.create(getApplication(), mSoundRes);
                mPlayer.start();
            }
        }
    }

    private void startVibrator() {
        int mVibroNum = sPrefSettings.getInt(KEY_PREF_VIBRO_NUM,0);
        if (mVibroNum!=0){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ListSounds mListSounds = new ListSounds();
        long [][] ListVibro = mListSounds.getListVibro();
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(ListVibro[mVibroNum], -1);}
        }
    }
        }

