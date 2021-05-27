package com.hfad.iqtimer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hfad.iqtimer.progress.GoalRepository;
import com.hfad.iqtimer.tools.StateEvent;
import com.hfad.iqtimer.tools.StateServiceEvent;
import com.hfad.iqtimer.tools.TickEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel {

    private static final int STATE_NEXT_ENTRY = 701;
    private static final int STATE_NEW_ENTRY = 700;
    private static final int STATE_RUN = 705;
    private static final int STATE_STOP = 706;
    private static final int STATE_PAUSE = 707;
    private static final int STATE_A_DESTROY = 712;
    private static final int STATE_PAUSE_SERVICE = 713;

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
        if (mState!=STATE_RUN){
            mState = STATE_RUN;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageState(StateEvent event) {
        setState(event.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageServiceState(StateServiceEvent event) {
        setState(event.state);
        timer.set(event.text);
    }


    public void checkState() {
        if (mState==0){
            checkEntryState();
        }
    }


    public void setState(int state) {
        switch (state){
            case STATE_STOP:
                setEmptyState();
                isPause.set(false);
                mState=STATE_STOP;
                break;
            case STATE_RUN:
                isPause.set(false);
                isNeedStop.set(true);
                mState=STATE_RUN;
                break;
            case STATE_PAUSE:
                mState=STATE_PAUSE;
                isPause.set(true);
                break;
            case STATE_PAUSE_SERVICE:
                mState=STATE_PAUSE;
                isPause.set(true);
                isNeedStop.set(true);
                count.set(repo.getCurrentCount());
                plan.set(repo.getPlan());
                break;

        }
    }

    public void checkEntryState() {
        switch (repo.getState()){
            case STATE_NEW_ENTRY:
                timer.set(repo.getDefaultTime());
                count.set(0);
                plan.set(repo.getPlan());
                isNeedStop.set(false);
                isPause.set(false);
                break;

            case STATE_NEXT_ENTRY:
                setEmptyState();
                break;


        }
    }
    public void setEmptyState() {
                timer.set(repo.getDefaultTime());
                count.set(repo.getCurrentCount());
                plan.set(repo.getPlan());
                isNeedStop.set(false);
                isPause.set(false);
    }

    public void updateDefaultTime() {
        timer.set(repo.getDefaultTime());
    }

    @Override
    protected void onCleared() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().post(new StateEvent(STATE_A_DESTROY));
        super.onCleared();
    }

}
