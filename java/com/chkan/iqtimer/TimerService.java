package com.chkan.iqtimer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.chkan.iqtimer.database.App;
import com.chkan.iqtimer.progress.ProgressCountDataIntentService;
import com.chkan.iqtimer.tools.RingtoneAndVibro;
import com.chkan.iqtimer.tools.StateEvent;
import com.chkan.iqtimer.tools.TimerState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class TimerService extends Service {
    private static final String TAG = "MYLOGS";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String KEY_PREF_BREAKTIME = "break_time";
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_BREAK_ENDED = 300;
    private static final int ST_NOTIF_PAUSED = 600;
    private static final int ST_NOTIF_STOPED = 700;
    private static final int ST_NOTIF_BREAK_STOPED = 900;
    private static final String KEY_TASK = "taskforintentservice";
    private static final int STATE_RUN = 705;


    static private long mTimeLeftInMillis;
    static private long mBreakTimeInMillis;
    static private long mDefaultTimeInMillis;
    private static CountDownTimer mTimer;
    final MyBinder mBinder = new MyBinder();
    SharedPreferences mPref;
    static SharedPreferences sPrefSettings;
    String mNotifChannel;
    RingtoneAndVibro mRingtoneAndVibro;
    long mSeconds;
    static boolean isBreak = false;
    static String mTime;
    private final CurrentSession mCurrentSession = App.instance.getSession();

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "TimerService: onCreate");
        super.onCreate();

        EventBus.getDefault().register(this);

        mRingtoneAndVibro = new RingtoneAndVibro(getBaseContext());

        mNotifChannel = createNotificationChannel();
        mPref = App.getPref();
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = App.getPrefSettings();
        mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
        mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
        mTimeLeftInMillis = mDefaultTimeInMillis;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StateEvent e) {
        switch (e.state) {
            case STOPED:
                Log.d(TAG, "TimerService: STOP");
                TimerStop();
                break;
            case PAUSED:
                Log.d(TAG, "TimerService: PAUSE");
                TimerPause();
                break;
            case CHANGE_INTERVAL:
                mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
                mTimeLeftInMillis = mDefaultTimeInMillis;
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TimerService: onStartCommand startId -"+startId);
        super.onStartCommand(intent, flags, startId);

        //извлекаем и проверяпм состояние
        int mState = intent.getIntExtra(KEY_STATE,0);

        switch (mState){
            case STATE_RUN:
                Log.d(TAG, "TimerService: onStartCommand - STATE_RUN");
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                EventBus.getDefault().postSticky(new StateEvent(TimerState.ACTIVE));
                mCurrentSession.setState(TimerState.ACTIVE);
                break;
            case ST_NOTIF_PAUSED: //обработка интента от кнопки Пауза из Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_PAUSED");
                TimerPause();
                mCurrentSession.setState(TimerState.PAUSED);
                break;
            case ST_NOTIF_STOPED: //обработка интента от кнопки Стоп из Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_STOPED");
                TimerStop();
                mCurrentSession.setState(TimerState.STOPED);
                break;
            case STATE_BREAK_STARTED: //обработка интента для перерыва
                Log.d(TAG, "TimerService: onStartCommand - ST_BREAK_STARTED");
                mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
                mTimeLeftInMillis=mBreakTimeInMillis;
                isBreak = true;
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                mCurrentSession.setState(TimerState.BREAK);
                EventBus.getDefault().postSticky(new StateEvent(TimerState.BREAK));
                break;
            case ST_NOTIF_BREAK_STOPED: //обработка интента от кнопки Стоп из Break Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_BREAK_STOPED");
                TimerStop();
                isBreak=false;
                mTimeLeftInMillis = mDefaultTimeInMillis;
                mCurrentSession.setState(TimerState.STOPED);
                break;
        }

        return START_STICKY;
    }

    public IBinder onBind(@NonNull Intent intent) {
        return mBinder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TimerService: onDestroy");
        if (mTimer!=null){mTimer.cancel();}
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void TimerStop() {
        mTimer.cancel();
        mTimeLeftInMillis = mDefaultTimeInMillis;
        isBreak=false;
        //отключаем нотификацию
        stopForeground( true );
        Log.d(TAG, "TimerService: TimerStop()");
    }

    public void TimerPause() {
        mTimer.cancel();
        NotificationOnPause();
        Log.d(TAG, "TimerService: TimerPause()");
    }

    static class MyBinder extends Binder {
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
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.dowork))
                .setContentText(mTime)
                .setSmallIcon(R.drawable.ic_notif_timer)
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
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 6, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(false)//прилипает оповещение и можно удалить только програмно
                .setContentTitle(getString(R.string.on_pause))
                .setContentText(getString(R.string.qest_continue))
                .setSmallIcon(R.drawable.ic_notif_paused)
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
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 9, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(false)
                .setShowWhen(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.dialog_session_end))
                .setContentText(getString(R.string.qest_break))
                .setSmallIcon(R.drawable.ic_notif_timer)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.dialog_rest_start), startBreakPendingIntent)
                .addAction(0, getString(R.string.break_skip), continuePendingIntent)
                .build()};

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(false);}

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification[0]);
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
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 12, continueIntent, 0);

        //если версия после О то создаем с использованием канала
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(false)
                .setShowWhen(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.dialog_break_end))
                .setContentText(getString(R.string.qest_continue))
                .setSmallIcon(R.drawable.ic_baseline_breaktime_8)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.work_start), continuePendingIntent)
                .build()};

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(false);}

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification[0]);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_service_channelid";
        //название которое видит пользователь в настройках
        String channelName = "IQtimer Notification";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }


    class Timer extends CountDownTimer{

        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onTick(long millisUntilFinished) {
            mTimeLeftInMillis = millisUntilFinished;
            mSeconds = mTimeLeftInMillis/1000;

            if (mTimeLeftInMillis >= 3600000) {//если время отчета равно или больше 1 часа, то формат с часами
                mTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mSeconds  / 3600,
                        (mSeconds  % 3600) / 60, (mSeconds % 60));
            } else {//формат с минутами и секундами
                mTime = String.format(Locale.getDefault(), "%02d:%02d", mSeconds  / 60, mSeconds % 60);
            }
            mCurrentSession.mTime.set(mTime);

            if (!isBreak){
            NotificationUpdate(mTime);
            } else {
                NotificationOnBreak(mTime);
            }

        }

        public void onFinish() {
            Log.d(TAG, "TimerService: onFinish");
            if (!isBreak){
                Intent mIntentService = new Intent(getApplicationContext(), ProgressCountDataIntentService.class);
                mIntentService.putExtra(KEY_TASK,STATE_TIMER_FINISHED);
                startService(mIntentService);

            mTimeLeftInMillis = mDefaultTimeInMillis;
            startSoundNotif(STATE_TIMER_FINISHED);
            NotificationOnSessionEnd();
            mCurrentSession.setState(TimerState.TIMER_FINISHED);

            } else {
                //для окончания перерыва
                mTimeLeftInMillis = mDefaultTimeInMillis;
                isBreak = false;
                NotificationOnBreakEnd();
                startSoundNotif(STATE_BREAK_ENDED);
                mCurrentSession.setState(TimerState.BREAK_FINISHED);
                EventBus.getDefault().postSticky(new StateEvent(TimerState.BREAK_FINISHED));
            }

        }
    }

    private void startSoundNotif(int state) {
        acquireScreenLock();
        bringActivityToFront();
        mRingtoneAndVibro.play(state);
    }

    private void bringActivityToFront() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("42", 42);
        getApplication().startActivity(activityIntent);
    }

    private void acquireScreenLock() {

        //FULL_WAKE_LOCK - CPU - on, Экран - Яркие сохраняют яркость, Клавиатура - Яркие сохраняют яркость
        //ACQUIRE_CAUSES_WAKEUP - Как только будет получена блокировка wake lock, экран и клавиатура будут немедленно открыты
        //ON_AFTER_RELEASE - При снятии блокировки включения таймер активности сбрасывается, и экран становится ярче, чем обычно.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        //проверяем активность устройства
        boolean isScreenOn = Build.VERSION.SDK_INT >= 23 ? powerManager.isInteractive() : powerManager.isScreenOn(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wakeLock.acquire(5000);
        }
    }

}

