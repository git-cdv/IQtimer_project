package com.hfad.iqtimer;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;

import com.hfad.iqtimer.tools.StateEvent;
import com.hfad.iqtimer.tools.TickEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MYLOGS";

    private static final int STATE_TIMER_FINISHED = 100;
    private static final int STATE_NEXT_ENTRY = 701;
    private static final int STATE_NEW_ENTRY = 700;
    private static final int STATE_RUN = 705;
    private static final int STATE_STOP = 706;
    private static final int STATE_PAUSE = 707;
    private static final int TIMER_FINISHED = 177;
    private static final int BREAK_ENDED = 178;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final int STATE_DIALOG_CANCEL = 726;
    private static final int STATE_COUNTER_UP = 777;

    int mState=0;

    // переменная которая обновляет значение в Представлении через set
    public ObservableField<String> timer = new ObservableField<>();
    public ObservableField<Integer> count = new ObservableField<>();
    public ObservableField<Integer> counter = new ObservableField<>();
    public ObservableInt plan = new ObservableInt();
    public ObservableBoolean isNeedStop = new ObservableBoolean();
    public ObservableBoolean isPause = new ObservableBoolean();
    public ObservableBoolean isNeedCount = new ObservableBoolean();

    public MainViewModel(@NonNull Application application) {
        super(application);
        EventBus.getDefault().register(this);
        }

    MainRepository repo = new MainRepository(getApplication());

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageTick(TickEvent event) {
        timer.set(event.message);
        //Log.d(TAG, "MainViewModel: TickEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageState(StateEvent event) {
       setState(event.state);
        Log.d(TAG, "MainViewModel: StateEvent - " + event.state);
    }

    public void checkState() {
        Log.d(TAG, "MainViewModel: checkState");
        int state = repo.getState();
        setState(state);
        if (state!=STATE_NEW_ENTRY){
        count.set(repo.getCurrentCount());
        counter.set(repo.getCounter());
        }
        isNeedCount.set(repo.getIsNeedCount());
    }

    public void setState(int state) {
        Log.d(TAG, "MainViewModel: setState - "+state);
        switch (state){
            case STATE_RUN:
                if (isMyServiceRunning(TimerService.class)){
                isPause.set(false);
                isNeedStop.set(true);
                if (mState==0){//если закрывалась Активити
                    count.set(repo.getCurrentCount());
                    plan.set(repo.getPlan());
                    counter.set(repo.getCounter());
                }
                mState=STATE_RUN;
                } else {
                    setEmptyState();
                    mState=STATE_STOP;
                }
                break;

            case STATE_PAUSE:
                isPause.set(true);
                if (mState==0){//если закрывалась Активити
                    timer.set(repo.getPauseTime());
                    isNeedStop.set(true);
                    count.set(repo.getCurrentCount());
                    plan.set(repo.getPlan());
                    counter.set(repo.getCounter());
                }
                mState=STATE_PAUSE;
                break;

            case STATE_NEW_ENTRY:
                timer.set(repo.getDefaultTime());
                count.set(0);
                plan.set(repo.getPlan());
                counter.set(repo.getCounter());
                isNeedStop.set(false);
                isPause.set(false);
                break;

            case ST_BREAK_STARTED_IN_NOTIF:
            case STATE_BREAK_STARTED:
                isPause.set(false);
                isNeedStop.set(true);
                mState=STATE_BREAK_STARTED;
                break;

            case STATE_DIALOG_CANCEL:
                mState=STATE_STOP;
                repo.setStateStop();
                break;

            case STATE_TIMER_FINISHED:
                setEmptyState();
                count.set(repo.getCurrentCount());
                mState=STATE_TIMER_FINISHED;
                break;

            case TIMER_FINISHED:setEmptyState();mState=TIMER_FINISHED;break;
            case STATE_STOP:setEmptyState();mState=STATE_STOP;break;
            case BREAK_ENDED:setEmptyState();mState=BREAK_ENDED;break;
            case STATE_NEXT_ENTRY: setEmptyState();break;
            case STATE_COUNTER_UP: counter.set(repo.getCounter());break;
        }

    }

    public void setEmptyState() {
                timer.set(repo.getDefaultTime());
                plan.set(repo.getPlan());
                isNeedStop.set(false);
                isPause.set(false);
                counter.set(repo.getCounter());
    }

    @Override
    protected void onCleared() {
        EventBus.getDefault().unregister(this);
        super.onCleared();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "MainActivity: isMyServiceRunning - true");
                return true;
            }
        }
        Log.d(TAG, "MainActivity: isMyServiceRunning - false");
        return false;
    }

}
