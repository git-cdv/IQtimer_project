package com.chkan.iqtimer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.chkan.iqtimer.database.App;
import com.chkan.iqtimer.database.PrefHelper;
import com.chkan.iqtimer.database.WriteCountDataIntentService;
import com.chkan.iqtimer.tools.TimerState;

import java.time.LocalDate;
import java.util.Locale;

public class CurrentSession {
    private static final String TAG = "MYLOGS";

    final String mToday;
    final Context context;
    String DefaultMinutes;

    public final ObservableField<String> mTime = new ObservableField<>();
    public final ObservableField<TimerState> mState = new ObservableField<>();
    public final ObservableField<Integer> mCount = new ObservableField<>();
    private final MutableLiveData<Integer> mPlan = new MutableLiveData<>();

    public CurrentSession(String DefaultMinutes, int DefaultPlan, int Count) {
        this.DefaultMinutes=DefaultMinutes;
        this.mTime.set(getDefaultTime(DefaultMinutes));
        this.mState.set(TimerState.STOPED);
        this.mCount.set(Count);
        this.mPlan.setValue(DefaultPlan);
        mToday = (LocalDate.now()).toString();
        context = App.instance.getContext();
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

    public MutableLiveData<Integer> getPlan() {
        return mPlan;
    }
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
            mCount.set(0);
        }
    }
    public void setDefaultMinutes(String min) {
        DefaultMinutes=min;
        mTime.set(getDefaultTime(DefaultMinutes));
    }

}