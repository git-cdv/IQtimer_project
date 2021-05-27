package com.hfad.iqtimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hfad.iqtimer.database.WriteCountDataIntentService;
import com.hfad.iqtimer.databinding.ActivityMainBinding;
import com.hfad.iqtimer.dialogs.DialogFragmentBreakEnded;
import com.hfad.iqtimer.dialogs.DialogFragmentSesEnd;
import com.hfad.iqtimer.progress.ProgressActivity;
import com.hfad.iqtimer.progress.ProgressViewModel;
import com.hfad.iqtimer.settings.AboutActivity;
import com.hfad.iqtimer.settings.SettingsActivity;
import com.hfad.iqtimer.statistic.StatisticActivity;
import com.hfad.iqtimer.tools.StateEvent;
import com.hfad.iqtimer.tools.TickEvent;
import com.marcok.stepprogressbar.StepProgressBar;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MYLOGS";
    private static final String KEY_COUNT = "countup";
    private static final String BR_FOR_SIGNALS = "iqtimer.brforsignals";
    private static final int STATE_TIMER_WORKING = 500;
    private static final int STATE_TIMER_FINISHED = 100;
    private static final int STATE_TIMER_WAIT = 101;
    private static final int STATE_TIMER_ONPAUSE = 102;
    private static final int ST_TIMER_STOPED = 200;
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_BREAK_ENDED = 300;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final String KEY_STATE = "iqtimer.state";
    private static final String KEY_PREF_PLAN = "set_plan_day";
    private static final int STATE_STOP = 706;
    private static final int STATE_RUN = 705;
    private static final int CHANGE_INTERVAL_STICKY = 710;


    ImageButton mButtonMenu, mStopButton;
    Integer mCurrentCount;
    boolean mBound = false;
    boolean mActive = false;
    ServiceConnection mConn;
    TimerService mTimerService;
    Intent mIntent;
    BroadcastReceiver brForSignals;
    SharedPreferences sPref, sPrefSettings;
    DialogFragment dlg1, dlg2;
    DialogPlus dialogMenu;
    Animation animTimerView;
    MainViewModel model;
    ActivityMainBinding binding;

    Button mStartButton;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        //проверяем что это не после переворота, а следующий вход и что таймер не запущен
        if (savedInstanceState == null&model.mState!=STATE_RUN) {model.checkEntryState();}
        binding.setModel(model);

        mStartButton = (Button) findViewById(R.id.button);
        mStopButton = (ImageButton) findViewById(R.id.imageButtonStop);
        mButtonMenu = (ImageButton) findViewById(R.id.imageButtonMenu);

        mIntent = new Intent(MainActivity.this, TimerService.class);
        //получаем доступ к файлу с данными по дате и сессиям
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);

        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //данные для меню
        dataForMenu();

        //create BroadcastReceiver для сигналов
        brForSignals = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //извлекаем и проверяпм состояние
                int mState = intent.getExtras().getInt(KEY_STATE);

                switch (mState){
                    case STATE_TIMER_FINISHED:
                        mCurrentCount=intent.getExtras().getInt(KEY_COUNT);
                        /*mTextFieldCount.setText(mCurrentCount.toString());*/
                        /*mTextField.setText(mDefaultTime);*/
                        /*progressBarSetup();*/
                        //создаем диалог если Активити активно
                        if(mActive) {
                            showMyDialog(STATE_TIMER_FINISHED);
                        }
                        break;
                    case ST_TIMER_STOPED:
                       /* mTextField.setText(mDefaultTime);*/
                        if(dlg1!=null){dlg1.dismiss();}
                        break;
                    case STATE_BREAK_ENDED:
                        /*mTextField.setText(mDefaultTime);*/
                        //создаем диалог если Активити активно
                        if(mActive) {
                            showMyDialog(STATE_BREAK_ENDED);
                        }
                        break;
                    case ST_BREAK_STARTED_IN_NOTIF:
                        if(mActive) {dlg1.dismiss();}
                        break;
                }
            }
        };

        mConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "MainActivity: onServiceConnected");
                //получаем ссылку на сервис
                mTimerService = ((TimerService.MyBinder) binder).getService();
                mBound = true;
                /*mSTATE=mTimerService.getSTATEinService();//получаем текущий статус в сервисе
                stateViewPrepare(mSTATE);//подготавливаем UI в зависимости от статуса*/
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

                    case R.id.button:
                        mIntent.putExtra(KEY_STATE,STATE_TIMER_WORKING);
                        startTimeService(mIntent);
                        EventBus.getDefault().post(new StateEvent(STATE_RUN));
                        /*if(animTimerView!=null){
                          mTextField.clearAnimation();
                            animTimerView=null;
                        }*/
                        Log.d(TAG, "MainActivity: Start");
                        break;

                    case R.id.imageButtonStop:
                        Log.d(TAG, "MainActivity: btn_Stop");
                        EventBus.getDefault().post(new StateEvent(STATE_STOP));


                      /*  if(animTimerView!=null){
                            mTextField.clearAnimation();
                            animTimerView=null;
                        }*/
                        break;
                    case R.id.imageButtonMenu:
                        Log.d(TAG, "MainActivity: btn_Menu");
                        dialogMenu.show();
                        break;

                }
            }
        };
        //регистрируем слушателей кнопок и настроек
        mStopButton.setOnClickListener(clickListener);
        mButtonMenu.setOnClickListener(clickListener);
        mStartButton.setOnClickListener(clickListener);
        sPrefSettings.registerOnSharedPreferenceChangeListener(this);

    }

    private void stateViewPrepare(int state) {
        Log.d(TAG, "MainActivity: stateViewPrepare()");
        switch (state){
            case STATE_TIMER_FINISHED:
                showMyDialog(STATE_TIMER_FINISHED);
                break;
            case STATE_BREAK_ENDED:
                showMyDialog(STATE_BREAK_ENDED);
                break;
        }
    }

    private void showMyDialog(int State) {
        Log.d(TAG, "MainActivity: showMyDialog()");

        switch (State){
            case STATE_TIMER_FINISHED:
                dlg1 = new DialogFragmentSesEnd();
                dlg1.show(getSupportFragmentManager(), "IsBreak");
                break;
            case STATE_BREAK_ENDED:
                dlg2 = new DialogFragmentBreakEnded();
                dlg2.show(getSupportFragmentManager(), "BreakEnded");
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

    public void onBreakTime(int value) {
        Log.d(TAG, "MainActivity: onBreakTime()");
        switch (value){
            case STATE_BREAK_STARTED:
                Intent mIntentBreak = new Intent(MainActivity.this, TimerService.class);
                mIntentBreak.putExtra(KEY_STATE,STATE_BREAK_STARTED);
                startTimeService(mIntentBreak);
                break;
            case STATE_TIMER_WORKING:
                startTimeService(mIntent);
                break;
            case STATE_TIMER_WAIT:
                /*mTimerService.setSTATEinService(STATE_TIMER_WAIT);*/
                break;
        }
        }

   /* @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TickEvent event) {
       binding.timerView.setText(event.message);
    }*/

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity: onStart + bindService + Registered receiver");
        super.onStart();
        /*EventBus.getDefault().register(this);*/
        registerReceiver(brForSignals, new IntentFilter(BR_FOR_SIGNALS));
        if(!mBound){bindService(mIntent, mConn, 0);}
        mActive = true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity: onStop + unbindService");
        super.onStop();
        /*EventBus.getDefault().unregister(this);*/
        if (!mBound) return;
        unbindService(mConn);
        mBound = false;
        mActive = false;
         }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume");
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
        unregisterReceiver(brForSignals);
    }

    void dataForMenu(){

        // массивы данных
        String[] texts = { "IQTimer","Обновить","Достижения","Статистика", "Настройки", "О программе"};
        int [] img = {R.drawable.ic_baseline_timer_24,R.drawable.ic_baseline_trending_up_24, R.drawable.ic_outline_cup_24, R.drawable.ic_baseline_leaderboard_24,R.drawable.ic_baseline_settings_24,R.drawable.ic_baseline_info_24};

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
                .setExpanded(false)
                .setCancelable(true)
                .setPadding(8,24,8,24)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        switch (position) {
                            case (2):
                                //открываем активити со Достижениями
                                Intent openProgress = new Intent(getApplication(), ProgressActivity.class);
                                startActivity(openProgress);
                                dialogMenu.dismiss();
                                break;
                            case (3):
                                //открываем активити со статистикой
                                Intent openStat = new Intent(getApplication(), StatisticActivity.class);
                                startActivity(openStat);
                                dialogMenu.dismiss();
                                break;
                            //открываем активити с настройками
                            case (4):
                                Intent openSettings = new Intent(getApplication(), SettingsActivity.class);
                                startActivity(openSettings);
                                dialogMenu.dismiss();
                                break;
                            case (5):
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


    Animation createBlink (){
        animTimerView = new AlphaAnimation(0.2f, 1.0f);//анимация альфа канала (прозрачности от 0 до 1)
        animTimerView.setDuration(800); //длительность анимации
        animTimerView.setStartOffset(50);//сдвижка начала анимации (с середины)
        animTimerView.setRepeatMode(Animation.REVERSE);//режим повтора - сначала или в обратном порядке
        animTimerView.setRepeatCount(Animation.INFINITE);//режим повтора (бесконечно)
        return animTimerView;
    }

}
