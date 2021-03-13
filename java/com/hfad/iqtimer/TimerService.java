package com.hfad.iqtimer;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import java.util.Locale;

public class TimerService extends Service {
    static long TIME_LIMIT = 10000;
    CountDownTimer mTimer;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mTimer = new CountDownTimer(TIME_LIMIT, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                String mTime;

                if (TIME_LIMIT >= 3600000) {//если время отчета равно или больше 1 часа, то формат с часами
                    mTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600,
                            (seconds % 3600) / 60, (seconds % 60));
                } else {//формат с минутами и секундами
                    mTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
                }

                Intent i = new Intent("TIMER_UPDATED");
                i.putExtra("countdown",mTime);

                sendBroadcast(i);


            }

            public void onFinish() {

                Intent i = new Intent("TIMER_UPDATED");
                i.putExtra("countdown","Sent!");

                sendBroadcast(i);

                stopSelf();

            }
        };

        mTimer.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
    @Override
    public void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }
}