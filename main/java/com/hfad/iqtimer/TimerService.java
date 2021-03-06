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
import com.hfad.iqtimer.progress.ProgressCountDataIntentService;
import com.hfad.iqtimer.tools.StateEvent;
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
    private static final String KEY_SERVICE_STATE = "TimerService.state";
    private static final String KEY_PAUSE_TIME = "pausetime.state";
    private static final int TIMER_FINISHED = 177;
    private static final int BREAK_ENDED = 178;


    static private long mTimeLeftInMillis;
    static private long mBreakTimeInMillis;
    static private long mDefaultTimeInMillis;
    private static CountDownTimer mTimer;
    MyBinder mBinder = new MyBinder();
    SharedPreferences mPref;
    static SharedPreferences sPrefSettings;
    SharedPreferences.Editor ed;
    String mNotifChannel;
    long mSeconds;
    static boolean isBreak = false;
    static int mState;
    static String mTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "TimerService: onCreate");
        super.onCreate();
        EventBus.getDefault().register(this);

        mNotifChannel = createNotificationChannel();
        mPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        ed = mPref.edit();
        //???????????????? ???????????? ?? ?????????? ?? ?????????????????????? ????????????????????
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDefaultTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_INTERVAL, "45")))*60000;
        mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
        mTimeLeftInMillis = mDefaultTimeInMillis;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TimerService: onStartCommand startId -"+startId+", mSTATE - "+mState);
        super.onStartCommand(intent, flags, startId);

        //?????????????????? ?? ?????????????????? ??????????????????
        int mState = intent.getIntExtra(KEY_STATE,0);

        switch (mState){
            case STATE_RUN:
                Log.d(TAG, "TimerService: onStartCommand - STATE_RUN");
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                EventBus.getDefault().post(new StateEvent(STATE_RUN));
                if (!isBreak){
                ed.putInt(KEY_SERVICE_STATE, STATE_RUN);
                ed.apply();} else {
                    ed.putInt(KEY_SERVICE_STATE, STATE_BREAK_STARTED);
                    ed.apply();
                }
                break;
            case ST_NOTIF_PAUSED: //?????????????????? ?????????????? ???? ???????????? ?????????? ???? Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_PAUSED");
                TimerPause();
                EventBus.getDefault().post(new StateEvent(STATE_PAUSE));
                break;
            case ST_NOTIF_STOPED: //?????????????????? ?????????????? ???? ???????????? ???????? ???? Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_STOPED");
                TimerStop();
                EventBus.getDefault().post(new StateEvent(STATE_STOP));
                break;
            case STATE_BREAK_STARTED: //?????????????????? ?????????????? ?????? ????????????????
                Log.d(TAG, "TimerService: onStartCommand - ST_BREAK_STARTED");
                mBreakTimeInMillis = (Integer.parseInt(sPrefSettings.getString(KEY_PREF_BREAKTIME, "15")))*60000;
                mTimeLeftInMillis=mBreakTimeInMillis;
                isBreak = true;
                mTimer = new Timer(mTimeLeftInMillis, 1000);
                mTimer.start();
                //?????????????????? ????????????
                EventBus.getDefault().post(new StateEvent(ST_BREAK_STARTED_IN_NOTIF));
                ed.putInt(KEY_SERVICE_STATE, STATE_BREAK_STARTED);
                ed.apply();
                break;
            case ST_NOTIF_BREAK_STOPED: //?????????????????? ?????????????? ???? ???????????? ???????? ???? Break Notification
                Log.d(TAG, "TimerService: onStartCommand - ST_NOTIF_BREAK_STOPED");
                TimerStop();
                isBreak=false;
                mTimeLeftInMillis = mDefaultTimeInMillis;
                break;
        }

        return START_STICKY;
    }

    @Subscribe(priority = 1, sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StateEvent event) {

        switch (event.state){
        case STATE_STOP:
            Log.d(TAG, "TimerService: STATE_STOP");
            TimerStop();
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
        ed.putInt(KEY_SERVICE_STATE, STATE_STOP);
        ed.apply();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void TimerStop() {
        mTimer.cancel();
        mTimeLeftInMillis = mDefaultTimeInMillis;
        ed.putInt(KEY_SERVICE_STATE, STATE_STOP);
        ed.apply();
        isBreak=false;
        //?????????????????? ??????????????????????
        stopForeground( true );
        Log.d(TAG, "TimerService: TimerStop()");
    }

    public void TimerPause() {
        mTimer.cancel();
        NotificationOnPause();
        ed.putInt(KEY_SERVICE_STATE, STATE_PAUSE);
        ed.putString(KEY_PAUSE_TIME, mTime);
        ed.apply();
        Log.d(TAG, "TimerService: TimerPause()");
    }

    class MyBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    public void NotificationUpdate(String mTime) {

        //???????????? ?????? ???????????????? ?? MainActivity ?????????? ?????????????? ???? Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0);
        //???????????? ?????? ???????????? ????????
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, 0);
        //???????????? ?????? ???????????? ??????????
        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.putExtra(KEY_STATE,ST_NOTIF_PAUSED);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 3, pauseIntent, 0);

        //???????? ???????????? ?????????? ?? ???? ?????????????? ?? ???????????????????????????? ????????????
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//?????????????????? ???????????????????? ?? ?????????? ?????????????? ???????????? ??????????????????
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

        //???????????? ?????? ???????????????? ?? MainActivity ?????????? ?????????????? ???? Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 4, notificationIntent, 0);
        //???????????? ?????? ???????????? ????????
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 5, stopIntent, 0);
        //???????????? ?????? ???????????? ????????????????????
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 6, continueIntent, 0);

        //???????? ???????????? ?????????? ?? ???? ?????????????? ?? ???????????????????????????? ????????????
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(false)//?????????????????? ???????????????????? ?? ?????????? ?????????????? ???????????? ??????????????????
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

        //???????????? ?????? ???????????????? ?? MainActivity ?????????? ?????????????? ???? Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, notificationIntent, 0);
        //???????????? ?????? ???????????? ???????????? ??????????????
        Intent startBreakIntent = new Intent(this, TimerService.class);
        startBreakIntent.putExtra(KEY_STATE,STATE_BREAK_STARTED);
        PendingIntent startBreakPendingIntent = PendingIntent.getService(this, 8, startBreakIntent, 0);
        //???????????? ?????? ???????????? ???????????????????? ??????????????
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 9, continueIntent, 0);

        //???????? ???????????? ?????????? ?? ???? ?????????????? ?? ???????????????????????????? ????????????
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

        //???????????? ?????? ???????????????? ?? MainActivity ?????????? ?????????????? ???? Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 13, notificationIntent, 0);
        //???????????? ?????? ???????????? ????????
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.putExtra(KEY_STATE,ST_NOTIF_BREAK_STOPED);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 14, stopIntent, 0);


        //???????? ???????????? ?????????? ?? ???? ?????????????? ?? ???????????????????????????? ????????????
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? mNotifChannel : "";
        final Notification[] notification = {new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)//?????????????????? ???????????????????? ?? ?????????? ?????????????? ???????????? ??????????????????
                .setContentTitle(getString(R.string.break_time))
                .setContentText(mTime)
                .setSmallIcon(R.drawable.ic_baseline_breaktime_8)
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.stop), stopPendingIntent)
                .build()};
        startForeground(1, notification[0]);
    }

    public void NotificationOnBreakEnd() {

        //???????????? ?????? ???????????????? ?? MainActivity ?????????? ?????????????? ???? Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, notificationIntent, 0);
        //???????????? ?????? ???????????? ???????????????????? ????????????
        Intent continueIntent = new Intent(this, TimerService.class);
        continueIntent.putExtra(KEY_STATE,STATE_RUN);
        PendingIntent continuePendingIntent = PendingIntent.getService(this, 12, continueIntent, 0);

        //???????? ???????????? ?????????? ?? ???? ?????????????? ?? ???????????????????????????? ????????????
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
        //???????????????? ?????????????? ?????????? ???????????????????????? ?? ????????????????????
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

            if (mTimeLeftInMillis >= 3600000) {//???????? ?????????? ???????????? ?????????? ?????? ???????????? 1 ????????, ???? ???????????? ?? ????????????
                mTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mSeconds  / 3600,
                        (mSeconds  % 3600) / 60, (mSeconds % 60));
            } else {//???????????? ?? ???????????????? ?? ??????????????????
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
            //EventBus.getDefault().post(new StateEvent(STATE_TIMER_FINISHED));
            ed.putInt(KEY_SERVICE_STATE,TIMER_FINISHED);
            ed.apply();

            } else {
                //?????? ?????????????????? ????????????????
                mTimeLeftInMillis = mDefaultTimeInMillis;
                isBreak = false;
                NotificationOnBreakEnd();
                startSoundForNotif(STATE_BREAK_ENDED);
                startVibrator();
                EventBus.getDefault().post(new StateEvent(STATE_BREAK_ENDED));
                ed.putInt(KEY_SERVICE_STATE,BREAK_ENDED);
                ed.apply();
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
                //???????????????? ?????????????????? ??????????????
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

