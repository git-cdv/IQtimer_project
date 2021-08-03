package com.chkan.iqtimer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.chkan.iqtimer.database.App;
import com.chkan.iqtimer.database.PrefHelper;
import com.chkan.iqtimer.databinding.ActivityMainBinding;
import com.chkan.iqtimer.dialogs.DialogSession;
import com.chkan.iqtimer.dialogs.DialogStop;
import com.chkan.iqtimer.progress.ProgressActivity;
import com.chkan.iqtimer.settings.AboutActivity;
import com.chkan.iqtimer.settings.SettingsActivity;
import com.chkan.iqtimer.statistic.StatisticActivity;
import com.chkan.iqtimer.tools.StateEvent;
import com.chkan.iqtimer.tools.TimerState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        DialogSession.DialogSessionListener, DialogStop.DialogStopListener {

    private static final String TAG = "MYLOGS";
    private static final int STATE_BREAK_STARTED = 400;
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_RUN = 705;

    ImageButton mButtonMenu;
    boolean mBound = false;
    boolean mActive = false;
    ServiceConnection mConn;
    Intent mIntent;
    DialogFragment dlg,dlgStop;
    ActivityMainBinding binding;
    private ImageView mTutorialDot, mTutorialDot2;
    private static long back_pressed;
    private final CurrentSession mCurrentSession = App.getInstance().getSession();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setSession(mCurrentSession);

        mButtonMenu = binding.imageButtonMenu;
        mTutorialDot = binding.tutorialDot;
        mTutorialDot2 = binding.tutorialDot2;

        mIntent = new Intent(MainActivity.this, TimerService.class);

        mConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                mBound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        };

        //регистрируем слушателя настроек
        App.getPrefSettings().registerOnSharedPreferenceChangeListener(this);

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StateEvent e) {
        Log.d(TAG, "MainActivity: StateEvent - "+e.state.name());
        switch (e.state) {
            case TIMER_FINISHED:
                showMyDialog();
                mCurrentSession.mCount.set(PrefHelper.getCount());
                //удаляем липкое оповещение чтобы больше не получать
                clearStickyEvent();
                break;
            case BREAK_FINISHED:
                //создаем диалог если Активити активно
                showMyDialog();
                clearStickyEvent();
                break;
            case BREAK:
            case ACTIVE:
                //если запущенно из Нотиф - убираем диалог
                if(dlg != null){dlg.dismiss();}
                break;
        }
    }


    private void showMyDialog() {
        if (dlg == null) {
            dlg = new DialogSession();
            dlg.setCancelable(false);
            dlg.show(getSupportFragmentManager(), "UniversalDialogSession");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showTutorialSnackbars();
        if(mCurrentSession.getState().get() == TimerState.TIMER_FINISHED||mCurrentSession.getState().get() == TimerState.BREAK_FINISHED){
            Log.d(TAG, "MainActivity: onResume - showMyDialog()");
            showMyDialog();
        }
    }

    public void toClick(View v) {

        if (v.getId() == R.id.timer_view) {

            if (mCurrentSession.getState().get() == TimerState.ACTIVE) {//это пауза
                Log.d(TAG, "MainActivity: Pause");
                mCurrentSession.setState(TimerState.PAUSED);
                EventBus.getDefault().post(new StateEvent(TimerState.PAUSED));//для TimerService
            } else {
                Log.d(TAG, "MainActivity: Start");
                mIntent.putExtra(KEY_STATE, STATE_RUN);
                startTimeService(mIntent);
                mCurrentSession.setState(TimerState.ACTIVE);
            }
        } else if (v.getId() == R.id.imageButtonStop) {
            Log.d(TAG, "MainActivity: btn_Stop");
            if (dlgStop == null) {
                dlgStop = new DialogStop();
                dlgStop.setCancelable(false);
                dlgStop.show(getSupportFragmentManager(), "StopDialog");
            }
        } else if (v.getId() == R.id.imageButtonMenu) {
            Log.d(TAG, "MainActivity: btn_Menu");
            showMenu(v);
        }
    }


    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.popup_menu);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case (R.id.option_progress):
                    //открываем активити со Достижениями
                    Intent openProgress = new Intent(getApplication(), ProgressActivity.class);
                    startActivity(openProgress);
                    break;
                case (R.id.option_statistic):
                    //открываем активити со статистикой
                    Intent openStat = new Intent(getApplication(), StatisticActivity.class);
                    startActivity(openStat);
                    break;
                //открываем активити с настройками
                case (R.id.option_setting):
                    Intent openSettings = new Intent(getApplication(), SettingsActivity.class);
                    startActivity(openSettings);
                    break;
                case (R.id.option_about):
                    //открываем активити с инфой
                    Intent openAbout = new Intent(getApplication(), AboutActivity.class);
                    startActivity(openAbout);
                    break;
            }
            return true;

        });

        popup.show();

    }

    private void startTimeService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void showTutorialSnackbars() {

        final int MESSAGE_SIZE = 3;
        int i = PrefHelper.getLastIntroStep();

        if (i < MESSAGE_SIZE) {

            final List<String> messages = Arrays.asList(
                    getString(R.string.tutorial_mess1),
                    getString(R.string.tutorial_mess2),
                    getString(R.string.tutorial_mess3));

            final Animation animTap = AnimationUtils.loadAnimation(this, R.anim.tutorial_tap);

            if (i == 0) {
                mTutorialDot.setVisibility(View.VISIBLE);
                mTutorialDot.setAnimation(animTap);
            }

            if (i == 1) {
                mTutorialDot.setVisibility(View.VISIBLE);
                mTutorialDot.animate().translationY(180f);
                mTutorialDot.clearAnimation();
                mTutorialDot.setAnimation(animTap);
            }

            if (i == 2) {
                mTutorialDot.clearAnimation();
                mTutorialDot.setVisibility(View.GONE);
                mTutorialDot2.setVisibility(View.VISIBLE);
                mTutorialDot2.setAnimation(animTap);
            }

            @SuppressLint("ShowToast")
            Snackbar s = Snackbar.make(mButtonMenu, messages.get(PrefHelper.getLastIntroStep()), Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", view -> {
                        int nextStep = i + 1;
                        PrefHelper.setLastIntroStep(nextStep);
                        showTutorialSnackbars();
                    })
                    .setAnchorView(mButtonMenu)
                    .setActionTextColor(Color.WHITE)
                    .setBackgroundTint(getResources().getColor(R.color.brand_blue_900,getTheme()));

            s.setBehavior(new BaseTransientBottomBar.Behavior() {
                @Override
                public boolean canSwipeDismissView(View child) {
                    return false;
                }
            });
            s.show();
        } else {
            mTutorialDot2.clearAnimation();
            mTutorialDot2.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity: onStart + bindService + Registered receiver");
        super.onStart();
        if (!mBound) {
            bindService(mIntent, mConn, 0);
        }
        mActive = true;
        if(!EventBus.getDefault().isRegistered(this)){
        EventBus.getDefault().register(this);}
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity: onStop + unbindService");
        super.onStop();
        if (!mBound) return;
        unbindService(mConn);
        mBound = false;
        mActive = false;
        EventBus.getDefault().unregister(this);
    }



    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainActivity: onDestroy + Unregistered receiver");
        super.onDestroy();
        App.getPrefSettings().unregisterOnSharedPreferenceChangeListener(this);
    }

    //слушает изменение настройки и выполняет код при событии
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "MainActivity: onSharedPreferenceChanged()");
        switch (key) {
            case ("default_interval"):
                    String min = PrefHelper.getDefaultTime();
                    mCurrentSession.setDefaultMinutes(min);
                    EventBus.getDefault().post(new StateEvent(TimerState.CHANGE_INTERVAL));//для TimerService
                break;
            case ("set_plan_day"):
                //установка количества точек из плана в настройка
                int i = Integer.parseInt(PrefHelper.getDefaultPlan());
                binding.stepProgressBar.setNumDots(i);
                mCurrentSession.setPlan(i);
                binding.invalidateAll();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "MainActivity: onBackPressed");

        if (mCurrentSession.getState().get() == TimerState.ACTIVE||mCurrentSession.getState().get() == TimerState.BREAK) {
            moveTaskToBack(true);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                try {
                    Toast.makeText(getBaseContext(), R.string.onback, Toast.LENGTH_SHORT)
                            .show();
                } catch (Throwable th) {
                    // ignoring this exception
                }
            }
            back_pressed = System.currentTimeMillis();
        }

    }

    @Override
    public void onDialogPositiveClick(TimerState state) {
        if (state==TimerState.TIMER_FINISHED){
            Intent mIntentBreak = new Intent(MainActivity.this, TimerService.class);
            mIntentBreak.putExtra(KEY_STATE, STATE_BREAK_STARTED);
            startTimeService(mIntentBreak);
            mCurrentSession.setState(TimerState.BREAK);
        } else {
            mIntent.putExtra(KEY_STATE, STATE_RUN);
            startTimeService(mIntent);
            mCurrentSession.setState(TimerState.ACTIVE);
        }
        dlg = null;
    }

    @Override
    public void onDialogNegativeClick() {
            mCurrentSession.setState(TimerState.STOPED);
            dlg = null;
    }

    private void clearStickyEvent() {
        StateEvent stickyEvent = EventBus.getDefault().getStickyEvent(StateEvent.class);
        if(stickyEvent != null) { EventBus.getDefault().removeStickyEvent(stickyEvent);}
    }

    @Override
    public void onDialogStopPositiveClick() {
        mCurrentSession.setState(TimerState.STOPED);
        EventBus.getDefault().post(new StateEvent(TimerState.STOPED));//для TimerService
        dlgStop=null;
    }

    @Override
    public void onDialogStopNegativeClick() {

        dlgStop.dismiss();
        dlgStop=null;
    }
}
