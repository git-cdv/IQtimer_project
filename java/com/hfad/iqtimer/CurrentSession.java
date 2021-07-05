package com.hfad.iqtimer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.database.PrefHelper;
import com.hfad.iqtimer.database.WriteCountDataIntentService;
import com.hfad.iqtimer.tools.TimerState;

import java.time.LocalDate;
import java.util.Locale;

public class CurrentSession {
    private static final String TAG = "MYLOGS";

    String mToday;
    Context context;
    String DefaultMinutes;

    public ObservableField<String> mTime = new ObservableField<>();
    public ObservableField<TimerState> mState = new ObservableField<>();
    private final MutableLiveData<Integer> mCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> mCounter = new MutableLiveData<>();
    private final MutableLiveData<Integer> mPlan = new MutableLiveData<>();
    private final MutableLiveData<Boolean> IsNeedCount = new MutableLiveData<>();


    public CurrentSession(String DefaultMinutes, int DefaultPlan, int Count, int Counter, boolean IsNeedCount) {
        this.DefaultMinutes=DefaultMinutes;
        this.mTime.set(getDefaultTime(DefaultMinutes));
        this.mState.set(TimerState.STOPED);
        this.mCount.setValue(Count);
        this.mPlan.setValue(DefaultPlan);
        this.mCounter.setValue(Counter);
        this.IsNeedCount.setValue(IsNeedCount);
        mToday = (LocalDate.now()).toString();
        context = App.getInstance().getContext();
        checkNewDay();
    }
    public ObservableField<TimerState> getState() {
        return mState;
    }

    public void setState(TimerState state) {
        Log.d(TAG, "CurrentSession: setState - "+state.name());
        mState.set(state);
        if(state==TimerState.STOPED||state==TimerState.TIMER_FINISHED||state==TimerState.BREAK_FINISHED){
            mTime.set(getDefaultTime(DefaultMinutes));
        }
    }

    public MutableLiveData<Integer> getCount() {
        return mCount;
    }
    public MutableLiveData<Integer> getCounter() {
        return mCounter;
    }
    public MutableLiveData<Integer> getPlan() {
        return mPlan;
    }
    public MutableLiveData<Boolean> getIsNeedCount() {
        return IsNeedCount;
    }
    public void setIsNeedCount(boolean b) {IsNeedCount.setValue(b);}
    public void setPlan(int i) { mPlan.setValue(i);}

    public String getDefaultTime(String DefaultMinutes) {
        String mDefaultTime;
        //проверяем настройку с дефолтным интервалом, если ее нет то устанавливается - defValue
        int mDefaultMinutes = Integer.parseInt(DefaultMinutes);

        if (mDefaultMinutes >= 60) {//если время отчета равно или больше 1 часа, то формат с часами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mDefaultMinutes / 60,
                    mDefaultMinutes % 60, 0);
        } else {//формат с минутами и секундами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d", mDefaultMinutes, 0);
        }
        return mDefaultTime;
    }
    //если первый заход сегодня - записываем данные с прошлого дня
    private void checkNewDay() {
        if (!PrefHelper.getWorkDate().equals(mToday)) {
            Intent mIntentService = new Intent(context, WriteCountDataIntentService.class);
            context.startService(mIntentService);
            mCount.setValue(0);
        }
    }
    public void setDefaultMinutes(String min) {
        DefaultMinutes=min;
        mTime.set(getDefaultTime(DefaultMinutes));
    }

}
