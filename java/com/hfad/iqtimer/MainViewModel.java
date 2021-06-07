package com.hfad.iqtimer;

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
import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MYLOGS";

    private static final int STATE_NEXT_ENTRY = 701;
    private static final int STATE_NEW_ENTRY = 700;
    private static final int STATE_RUN = 705;
    private static final int STATE_STOP = 706;
    private static final int STATE_PAUSE = 707;

    int mState=0;

    // переменная которая обновляет значение в Представлении через set
    public ObservableField<String> timer = new ObservableField<>();
    public ObservableField<Integer> count = new ObservableField<>();
    public ObservableInt plan = new ObservableInt();
    public ObservableBoolean isNeedStop = new ObservableBoolean();
    public ObservableBoolean isPause = new ObservableBoolean();

    public MainViewModel(@NonNull @NotNull Application application) {
        super(application);
        EventBus.getDefault().register(this);
        }



    MainRepository repo = new MainRepository(getApplication());

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageTick(TickEvent event) {
        timer.set(event.message);
        Log.d(TAG, "MainViewModel: TickEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageState(StateEvent event) {
       setState(event.state);
        Log.d(TAG, "MainViewModel: StateEvent - " + event.state);
    }

    public void checkState() {
        Log.d(TAG, "MainViewModel: checkState");
        setState(repo.getState());
    }


    public void setState(int state) {
        switch (state){
            case STATE_STOP:
                setEmptyState();
                mState=STATE_STOP;
                Log.d(TAG, "MainViewModel: setState - STATE_STOP");
                break;
            case STATE_RUN:
                isPause.set(false);
                isNeedStop.set(true);
                if (mState==0){//если закрывалась Активити
                    count.set(repo.getCurrentCount());
                }
                mState=STATE_RUN;
                Log.d(TAG, "MainViewModel: setState - STATE_RUN");
                break;
            case STATE_PAUSE:
                isPause.set(true);
                if (mState==0){//если закрывалась Активити
                    timer.set(repo.getPauseTime());
                    isNeedStop.set(true);
                count.set(repo.getCurrentCount());
                }
                mState=STATE_PAUSE;
                Log.d(TAG, "MainViewModel: setState - STATE_PAUSE");
                break;
            case STATE_NEW_ENTRY:
                timer.set(repo.getDefaultTime());
                count.set(0);
                plan.set(repo.getPlan());
                isNeedStop.set(false);
                isPause.set(false);
                Log.d(TAG, "MainViewModel: setState - STATE_NEW_ENTRY");
                break;

            case STATE_NEXT_ENTRY:
                setEmptyState();
                Log.d(TAG, "MainViewModel: setState - STATE_NEXT_ENTRY");
                break;

        }
    }

    public void setEmptyState() {
                timer.set(repo.getDefaultTime());
                count.set(repo.getCurrentCount());
                plan.set(repo.getPlan());
                isNeedStop.set(false);
                isPause.set(false);
                mState=0;
    }

    public void updateDefaultTime() {
        timer.set(repo.getDefaultTime());
    }

    @Override
    protected void onCleared() {
        EventBus.getDefault().unregister(this);
        super.onCleared();
    }

    public void checkKilledState() {
        if (mState==STATE_RUN&timer.get()==null){
            setEmptyState();
            //repo.setStateStop();
            Log.d(TAG, "MainViewModel: checkKilledState()");
        }
    }
}
