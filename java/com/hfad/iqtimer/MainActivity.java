package com.hfad.iqtimer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
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
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hfad.iqtimer.database.WriteCountDataIntentService;
import com.hfad.iqtimer.dialogs.DialogFragmentBreakEnded;
import com.hfad.iqtimer.dialogs.DialogFragmentSesEnd;
import com.hfad.iqtimer.progress.ProgressActivity;
import com.hfad.iqtimer.settings.AboutActivity;
import com.hfad.iqtimer.settings.SettingsActivity;
import com.hfad.iqtimer.statistic.StatisticActivity;
import com.marcok.stepprogressbar.StepProgressBar;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MYLOGS";
    private static final String KEY_TIME = "timedown";
    private static final String KEY_COUNT = "countup";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String KEY_PREF_CHANGE = "iqtimer.timerchange";
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
    private static final String KEY_PREF_PLAN = "set_plan_day" ;


    TextView mTextField,mTextFieldCount;
    ImageButton mButtonMenu,mStopButton;
    StepProgressBar mStepProgressBar;
    String mLocalDate;
    Integer mCurrentCount;
    boolean mBound = false;
    boolean mActive = false;
    ServiceConnection mConn;
    TimerService mTimerService;
    Intent mIntent;
    BroadcastReceiver uiUpdated, brForSignals;
    SharedPreferences sPref, sPrefSettings;
    String mDefaultTime;
    Integer mDefaultPlan;
    DialogFragment dlg1,dlg2;
    int mSTATE=STATE_TIMER_WAIT;
    DialogPlus dialogMenu;
    Animation animTimerView;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate");
        setContentView(R.layout.activity_main);

        mTextField = (TextView) findViewById(R.id.timer_view);
        mTextFieldCount = (TextView) findViewById(R.id.count_ses);
        mStopButton = (ImageButton) findViewById(R.id.imageButtonStop);
        mButtonMenu = (ImageButton) findViewById(R.id.imageButtonMenu);
        mStepProgressBar =(StepProgressBar)findViewById(R.id.stepProgressBar);

        mIntent = new Intent(MainActivity.this, TimerService.class);
        //получаем доступ к файлу с данными по дате и сессиям
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);

        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        //вытаскиваем дефолтную значение интервала из настроек и присваиваем mDefaultTime
        setDefaultTimeFromPref(sPrefSettings);
        if (mSTATE==STATE_TIMER_WAIT){mTextField.setText(mDefaultTime);}//если первый вход и сервис еще не запущен
        mDefaultPlan = Integer.valueOf(sPrefSettings.getString(KEY_PREF_PLAN, "8"));
        //установка количества точек из плана в настройках
        mStepProgressBar.setNumDots(mDefaultPlan);
         //данные для меню
        dataForMenu();

        if(savedInstanceState == null) {//проверяем что это не после переворота, а следующий вход
            mLocalDate = (LocalDate.now()).toString();

            checkFirstRun();//проверяем на 0 вход

            //если уже была запись в текущий день (т.е день НЕ НОВЫЙ) - берем ее в mTextFieldCount
            if (sPref.getString(KEY_PREF_DATE, "").equals(mLocalDate)) {
                mCurrentCount = sPref.getInt(KEY_PREF_COUNT, 500);
                mTextFieldCount.setText(mCurrentCount.toString());

            } else {//если первый заход сегодня
                mCurrentCount = 0;
                mTextFieldCount.setText("0");
                Intent mIntentService = new Intent(this, WriteCountDataIntentService.class);
                startService(mIntentService);

            }
            progressBarSetup();
        }

        if(savedInstanceState != null){//проверяем что это после переворота
            mBound= savedInstanceState.getBoolean("mBound");
            mCurrentCount = savedInstanceState.getInt("mCurrentCount");
            mDefaultPlan = savedInstanceState.getInt("mDefaultPlan");
            mTextFieldCount.setText(mCurrentCount.toString());
            mSTATE = savedInstanceState.getInt(KEY_STATE);
            progressBarSetup();
            Log.d(TAG, "MainActivity: savedInstanceState mSTATE - "+mSTATE);

        }

        //create BroadcastReceiver для обновления таймера
        uiUpdated = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTextField.setText(intent.getExtras().getString(KEY_TIME));
            }
        };

        //create BroadcastReceiver для сигналов
        brForSignals = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //извлекаем и проверяпм состояние
                int mState = intent.getExtras().getInt(KEY_STATE);

                switch (mState){
                    case STATE_TIMER_FINISHED:
                        mCurrentCount=intent.getExtras().getInt(KEY_COUNT);
                        mTextFieldCount.setText(mCurrentCount.toString());
                        mTextField.setText(mDefaultTime);
                        progressBarSetup();
                        //создаем диалог если Активити активно
                        if(mActive) {
                            showMyDialog(STATE_TIMER_FINISHED);
                        }
                        break;
                    case ST_TIMER_STOPED:
                        mTextField.setText(mDefaultTime);
                        if(dlg1!=null){dlg1.dismiss();}
                        break;
                    case STATE_BREAK_ENDED:
                        mTextField.setText(mDefaultTime);
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
                mSTATE=mTimerService.getSTATEinService();//получаем текущий статус в сервисе
                stateViewPrepare(mSTATE);//подготавливаем UI в зависимости от статуса
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
                        if (mSTATE == STATE_TIMER_WORKING){
                            Log.d(TAG, "MainActivity: Pause");
                            if(mBound){mTimerService.TimerPause();}
                            mSTATE = STATE_TIMER_ONPAUSE;
                            mTextField.startAnimation(createBlink());
                            break;
                        } else {
                        mIntent.putExtra(KEY_STATE,STATE_TIMER_WORKING);
                        startTimeService(mIntent);
                        mSTATE = STATE_TIMER_WORKING;
                        mStopButton.setVisibility(View.VISIBLE);
                        if(animTimerView!=null){
                            mTextField.clearAnimation();
                            animTimerView=null;
                        }
                        Log.d(TAG, "MainActivity: Start");
                        break;}

                    case R.id.imageButtonStop:
                        Log.d(TAG, "MainActivity: btn_Stop");
                        if(mBound){mTimerService.TimerStop();}
                        mTextField.setText(mDefaultTime);
                        mStopButton.setVisibility(View.INVISIBLE);
                        mSTATE = STATE_TIMER_FINISHED;
                        if(animTimerView!=null){
                            mTextField.clearAnimation();
                            animTimerView=null;
                        }
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
        mTextField.setOnClickListener(clickListener);
        sPrefSettings.registerOnSharedPreferenceChangeListener(this);

    }

    private void progressBarSetup() {
        //убирает полностью активные точки если значение -1 (меньше или больше - выбросит ошибку)
        if (mCurrentCount < mDefaultPlan) {
            mStepProgressBar.setCurrentProgressDot(mCurrentCount - 1);
        } else {
            mStepProgressBar.setCurrentProgressDot(mDefaultPlan - 1);
        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("mBound",mBound);
        savedInstanceState.putInt("mCurrentCount",mCurrentCount);
        savedInstanceState.putInt("mDefaultPlan",mDefaultPlan);
        savedInstanceState.putInt(KEY_STATE,mSTATE);
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
                mSTATE = STATE_TIMER_WORKING;
                startTimeService(mIntent);
                break;
            case STATE_TIMER_WAIT:
                mSTATE = STATE_TIMER_WAIT;
                mTimerService.setSTATEinService(STATE_TIMER_WAIT);
                break;
        }
        }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity: onStart + bindService + Registered receiver");
        super.onStart();
        registerReceiver(uiUpdated, new IntentFilter("TIMER_UPDATED"));
        registerReceiver(brForSignals, new IntentFilter(BR_FOR_SIGNALS));
        bindService(mIntent, mConn, 0);
        mActive = true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity: onStop + unbindService");
        super.onStop();
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
        unregisterReceiver(uiUpdated);
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

    private void setDefaultTimeFromPref(SharedPreferences sPref) {
        //проверяем настройку с дефолтным интервалом, если ее нет то устанавливается - defValue
        int mDefaultMinutes = Integer.parseInt(sPref.getString(KEY_PREF_INTERVAL, "45"));

        if (mDefaultMinutes >= 60) {//если время отчета равно или больше 1 часа, то формат с часами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", mDefaultMinutes / 60,
                    mDefaultMinutes % 60, 0);
        } else {//формат с минутами и секундами
            mDefaultTime = String.format(Locale.getDefault(), "%02d:%02d", mDefaultMinutes, 0);
        }
    }
    //слушает изменение настройки и выполняет код при событии
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "MainActivity: onSharedPreferenceChanged()");
        switch (key) {
            case ("default_interval"):
                setDefaultTimeFromPref(sharedPreferences);
                mTextField.setText(mDefaultTime);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putBoolean(KEY_PREF_CHANGE, true);
                ed.apply();
                break;
            case ("set_plan_day"):
                mDefaultPlan = Integer.valueOf(sharedPreferences.getString(KEY_PREF_PLAN, "8"));
                //установка количества точек из плана в настройках
                mStepProgressBar.setNumDots(mDefaultPlan);
                break;
        }

    }

    private void checkFirstRun() {
        if (sPref.getBoolean("firstrun", true)) {
            Log.d(TAG, "MainActivity: checkFirstRun()");
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(KEY_PREF_DATE, mLocalDate);
            ed.putInt(KEY_PREF_COUNT,0);
            ed.apply();
            mCurrentCount = 0;
            mTextFieldCount.setText("0");

            sPref.edit().putBoolean("firstrun", false).apply();
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
