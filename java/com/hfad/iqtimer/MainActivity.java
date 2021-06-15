package com.hfad.iqtimer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.hfad.iqtimer.databinding.ActivityMainBinding;
import com.hfad.iqtimer.dialogs.DialogFragmentBreakEnded;
import com.hfad.iqtimer.dialogs.DialogFragmentSesEnd;
import com.hfad.iqtimer.progress.ProgressActivity;
import com.hfad.iqtimer.settings.AboutActivity;
import com.hfad.iqtimer.settings.SettingsActivity;
import com.hfad.iqtimer.statistic.StatisticActivity;
import com.hfad.iqtimer.tools.StateEvent;
import com.hfad.iqtimer.tools.TickEvent;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MYLOGS";
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_BREAK_ENDED = 300;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final String KEY_STATE = "iqtimer.state";
    private static final int STATE_STOP = 706;
    private static final int STATE_RUN = 705;
    private static final int CHANGE_INTERVAL_STICKY = 710;
    private static final int STATE_PAUSE = 707;
    private static final int RUN_FROM_DIALOG = 7055;
    private static final int BREAK_ENDED = 178;
    private static final int TIMER_FINISHED = 177;


    ImageButton mButtonMenu, mStopButton;
    boolean mBound = false;
    boolean mActive = false;
    ServiceConnection mConn;
    Intent mIntent;
    SharedPreferences sPrefSettings;
    DialogFragment dlg1, dlg2;
    DialogPlus dialogMenu;
    MainViewModel model;
    ActivityMainBinding binding;
    TextView mTimerView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        model.checkState();
        binding.setModel(model);


        mTimerView = (TextView) findViewById(R.id.timer_view);
        mStopButton = (ImageButton) findViewById(R.id.imageButtonStop);
        mButtonMenu = (ImageButton) findViewById(R.id.imageButtonMenu);

        mIntent = new Intent(MainActivity.this, TimerService.class);

        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //данные для меню
        dataForMenu();

        mConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "MainActivity: onServiceConnected");
                mBound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "MainActivity: onServiceDisconnected");
                mBound = false;
            }
        };

        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()){

                    case R.id.timer_view:
                        if(model.mState==STATE_RUN|model.mState==STATE_BREAK_STARTED){//это пауза
                            Log.d(TAG, "MainActivity: Pause");
                            EventBus.getDefault().post(new StateEvent(STATE_PAUSE));

                        }else {
                            Log.d(TAG, "MainActivity: Start");
                            mIntent.putExtra(KEY_STATE, STATE_RUN);//старт таймера
                            startTimeService(mIntent);
                            EventBus.getDefault().post(new StateEvent(STATE_RUN));
                        }

                        break;

                    case R.id.imageButtonStop:
                        Log.d(TAG, "MainActivity: btn_Stop");
                        EventBus.getDefault().post(new StateEvent(STATE_STOP));
                        break;
                    case R.id.imageButtonMenu:
                        Log.d(TAG, "MainActivity: btn_Menu");
                        dialogMenu.show();
                        break;

                }
            }
        };
        //регистрируем слушателей кнопок и настроек
        mTimerView.setOnClickListener(clickListener);
        mStopButton.setOnClickListener(clickListener);
        mButtonMenu.setOnClickListener(clickListener);
        sPrefSettings.registerOnSharedPreferenceChangeListener(this);

        if(savedInstanceState == null) {//проверяем что это не после переворота, а следующий вход
            if (model.mState == TIMER_FINISHED) {
                showMyDialog(STATE_TIMER_FINISHED);
            }
            if (model.mState == BREAK_ENDED) {
                showMyDialog(STATE_BREAK_ENDED);
            }
        }

    }

    private void showMyDialog(int State) {
        Log.d(TAG, "MainActivity: showMyDialog()");

        switch (State){
            case STATE_TIMER_FINISHED:
                if (dlg1==null){
                dlg1 = new DialogFragmentSesEnd();
                dlg1.setCancelable(false);
                dlg1.show(getSupportFragmentManager(), "IsBreak");}
                break;
            case STATE_BREAK_ENDED:
                if (dlg2==null){
                dlg2 = new DialogFragmentBreakEnded();
                dlg2.setCancelable(false);
                dlg2.show(getSupportFragmentManager(), "BreakEnded");}
                break;
        }

    }

    private void startTimeService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageState(StateEvent event) {
        Log.d(TAG, "MainActivity: StateEvent - " + event.state);
        switch (event.state){
            case STATE_TIMER_FINISHED:
                //создаем диалог если Активити активно
                if(mActive) {showMyDialog(STATE_TIMER_FINISHED);}
                break;
            case STATE_BREAK_STARTED:
                Intent mIntentBreak = new Intent(MainActivity.this, TimerService.class);
                mIntentBreak.putExtra(KEY_STATE,STATE_BREAK_STARTED);
                startTimeService(mIntentBreak);
                break;
           case STATE_BREAK_ENDED:
                //создаем диалог если Активити активно
                if(mActive) {showMyDialog(STATE_BREAK_ENDED);}
                break;
            case ST_BREAK_STARTED_IN_NOTIF:
                if(mActive) {dlg1.dismiss();}
                break;
            case RUN_FROM_DIALOG:
                mIntent.putExtra(KEY_STATE, STATE_RUN);
                startTimeService(mIntent);
                EventBus.getDefault().post(new StateEvent(STATE_RUN));
                break;
        }

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity: onStart + bindService + Registered receiver");
        super.onStart();
        EventBus.getDefault().register(this);
        if(!mBound){bindService(mIntent, mConn, 0);}
        mActive = true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity: onStop + unbindService");
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (!mBound) return;
        unbindService(mConn);
        mBound = false;
        mActive = false;
         }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume - mState - "+ model.mState);
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
        sPrefSettings.unregisterOnSharedPreferenceChangeListener(this);
    }

    void dataForMenu(){

        // массивы данных
        String[] texts = { "Достижения","Статистика", "Настройки", "О программе"};
        int [] img = {R.drawable.ic_outline_cup_24, R.drawable.ic_baseline_leaderboard_24,R.drawable.ic_outline_settings_24,R.drawable.ic_outline_info_24};

        // упаковываем данные в понятную для адаптера структуру
        ArrayList<Map<String, Object>> data = new ArrayList<>(
                texts.length);
        Map<String, Object> m;
        for (int i = 0; i < texts.length; i++) {
            m = new HashMap<>();
            m.put("text", texts[i]);
            m.put("image", img[i]);
            data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {"text","image"};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvText, R.id.ivImg};

        // создаем адаптер
        SimpleAdapter sMenuAdapter = new SimpleAdapter(this, data, R.layout.item_list_menu,
                from, to);

        // ДИАЛОГОВОЕ МЕНЮ
        dialogMenu = DialogPlus.newDialog(this)
                .setAdapter(sMenuAdapter)
                .setCancelable(true)
                .setPadding(8,24,8,24)
                .setContentBackgroundResource(R.drawable.for_menu)
                .setOutAnimation(R.anim.slide_out_bottom)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        switch (position) {
                            case (0):
                                //открываем активити со Достижениями
                                Intent openProgress = new Intent(getApplication(), ProgressActivity.class);
                                startActivity(openProgress);
                                dialogMenu.dismiss();
                                break;
                            case (1):
                                //открываем активити со статистикой
                                Intent openStat = new Intent(getApplication(), StatisticActivity.class);
                                startActivity(openStat);
                                dialogMenu.dismiss();
                                break;
                            //открываем активити с настройками
                            case (2):
                                Intent openSettings = new Intent(getApplication(), SettingsActivity.class);
                                startActivity(openSettings);
                                dialogMenu.dismiss();
                                break;
                            case (3):
                                //открываем активити с инфой
                                Intent openAbout = new Intent(getApplication(), AboutActivity.class);
                                startActivity(openAbout);
                                dialogMenu.dismiss();
                                break;
                        }
                    }
                })
                .create();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("key_mActive",mActive);
    }

    //слушает изменение настройки и выполняет код при событии
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "MainActivity: onSharedPreferenceChanged()");
        switch (key) {
            case ("default_interval"):
              if(model.mState!=STATE_RUN){
                  binding.timerView.setText(model.repo.getDefaultTime());}
                  EventBus.getDefault().postSticky(new StateEvent(CHANGE_INTERVAL_STICKY));
                break;
            case ("set_plan_day"):
                //установка количества точек из плана в настройках
                binding.stepProgressBar.setNumDots(model.repo.getPlan());
                break;
        }
    }


}
