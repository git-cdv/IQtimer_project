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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.database.ListSounds;
import com.hfad.iqtimer.database.WriteCountDataIntentService;
import com.hfad.iqtimer.progress.ProgressCountDataIntentService;
import com.hfad.iqtimer.tools.StateEvent;
import com.hfad.iqtimer.tools.StateServiceEvent;
import com.hfad.iqtimer.tools.TickEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class TimerService extends Service {
    private static final String TAG = "MYLOGS";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String KEY_PREF_BREAKTIME = "break_time";
    private static final String KEY_PREF_SOUND_RES = "prefsoundres";
    private static final String KEY_PREF_SOUND_BREAK_RES = "prefsoundbreakres";
    private static final String KEY_PREF_VIBRO_NUM = "prefvibrochoice";
    private static final String BR_FOR_SIGNALS = "iqtimer.brforsignals";
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int ST_TIMER_STOPED = 200;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_BREAK_ENDED = 300;
    private static final int ST_NOTIF_PAUSED = 600;
    private static final int ST_NOTIF_STOPED = 700;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final int ST_NOTIF_BREAK_STOPED = 900;
    private static final String KEY_TASK = "taskforintentservice";
    private static final int STATE_STOP = 706;
    private static final int STATE_RUN = 705;
    private static final int STATE_PAUSE = 707;
    private static final int CHANGE_INTERVAL_STICKY = 710;
    private static final int STATE_A_DESTROY = 712;
    private static final int STATE_PAUSE_SERVICE = 713;

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
    static int mState;
    static String mTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "TimerService: onCreate");
        super.onCreate();
        EventBus.getDefault().register(this);

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
        Log.d(TAG, "TimerService: onStartCommand startId -"+startId+", mSTATE - "+mState);
        super.onStartCommand(intent, flags, startId);

        //извлекаем и проверяпм состояние
        int mState = intent.getIntExtra(KEY_STATE,0);

        switch (mState){
            case STATE_RUN:
                Log.d(TAG, "TimerService: onStartCommand - STATE_RUN");
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                EventBus.getDefault().post(new StateEvent(STATE_RUN));
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

                break;
        }

        return START_STICKY;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StateEvent event) {
    switch (event.state){
        case STATE_STOP:
            Log.d(TAG, "TimerService: STATE_STOP");
            TimerStop();
            mState=STATE_STOP;
            break;
        case STATE_RUN:
            Log.d(TAG, "TimerService: STATE_RUN");
            mState=STATE_RUN;
            break;
        case CHANGE_INTERVAL_STICKY:
            mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
            mTimeLeftInMillis = mDefaultTimeInMillis;
        break;
        case STATE_PAUSE:
            Log.d(TAG, "TimerService: STATE_PAUSE");
            TimerPause();
            NotificationOnPause();
            mState=STATE_PAUSE;
            break;
        case STATE_A_DESTROY:
            Log.d(TAG, "TimerService: STATE_A_DESTROY");
            if (mState==STATE_PAUSE){
                //возвращает состояние паузы для ViewModel, если ее убили
            EventBus.getDefault().postSticky(new StateServiceEvent(STATE_PAUSE_SERVICE,mTime));}
            break;
}
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
        EventBus.getDefault().unregister(this);
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
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.dialog_rest_start), startBreakPendingIntent)
                .addAction(0, getString(R.string.dialog_rest_reset), continuePendingIntent)
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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.dialog_rest_end), continuePendingIntent)
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
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
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
            EventBus.getDefault().post(new TickEvent(mTime));


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

            mTimeLeftInMillis = mDefaultTimeInMillis;
            startSoundForNotif(STATE_TIMER_FINISHED);
            startVibrator();
            NotificationOnSessionEnd();
            } else {
                //для окончания перерыва
                Intent i = new Intent(BR_FOR_SIGNALS);
                i.putExtra(KEY_STATE, STATE_BREAK_ENDED);
                sendBroadcast(i);
                mTimeLeftInMillis = mDefaultTimeInMillis;
                isBreak = false;
                NotificationOnBreakEnd();
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

