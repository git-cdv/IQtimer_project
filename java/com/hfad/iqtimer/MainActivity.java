package com.hfad.iqtimer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hfad.iqtimer.database.SessionDatabaseHelper;
import com.hfad.iqtimer.dialogs.DialogFragmentBreakEnded;
import com.hfad.iqtimer.dialogs.DialogFragmentSesEnd;
import com.hfad.iqtimer.settings.AboutActivity;
import com.hfad.iqtimer.settings.SettingsActivity;
import com.hfad.iqtimer.statistic.StatisticActivity;

import java.time.LocalDate;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MYLOGS";
    private static final String KEY_TIME = "timedown";
    private static final String KEY_COUNT = "countup";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String KEY_PREF_INTERVAL = "default_interval";
    private static final String BR_FOR_SIGNALS = "iqtimer.brforsignals";
    private static final int ST_TIMER_STARTED = 500;
    private static final int ST_TIMER_FINISH = 100;
    private static final int ST_TIMER_STOPED = 200;
    private static final int ST_BREAK_STARTED = 400;
    private static final int ST_BREAK_ENDED = 300;
    private static final int ST_BREAK_STARTED_IN_NOTIF = 800;
    private static final String KEY_STATE = "iqtimer.state";

    TextView mTextField,mTextFieldCount;
    Button mStartButton, mStopButton, mPauseButton;
    String mLocalDate;
    Integer mCurrentCount;
    private SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    boolean mBound = false;
    ServiceConnection mConn;
    TimerService mTimerService;
    Intent mIntent;
    BroadcastReceiver uiUpdated, brForSignals;
    SharedPreferences sPref, sPrefSettings;
    String mDefaultTime;
    DialogFragment dlg1,dlg2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextField = (TextView) findViewById(R.id.timer_view);
        mTextFieldCount = (TextView) findViewById(R.id.count_ses);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mPauseButton = (Button) findViewById(R.id.btn_pause);
        mIntent = new Intent(MainActivity.this, TimerService.class);
        mIntent.putExtra(KEY_STATE,ST_TIMER_STARTED);
        //получаем доступ к файлу с данными по дате и сессиям
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        //вытаскиваем дефолтную значение интервала из настроек и присваиваем mDefaultTime
        setDefaultTimeFromPref(sPrefSettings);
        mTextField.setText(mDefaultTime);
        //создаем экземпляр диалога
        dlg1 = new DialogFragmentSesEnd();
        dlg2 = new DialogFragmentBreakEnded();

        if(savedInstanceState == null) {//проверяем что это не после переворота, а следующий вход

            mLocalDate = (LocalDate.now()).toString();

            if (!sPref.contains(KEY_PREF_DATE)){//проверка на 0-вой заход
                NewPreferences(); //заполняем Preferences файл как новый
            } else {
                //если уже была запись в текущий день (т.е день НЕ НОВЫЙ) - берем ее в mTextFieldCount
                if (sPref.getString(KEY_PREF_DATE,"").equals(mLocalDate)){
                    mCurrentCount = sPref.getInt(KEY_PREF_COUNT,500);
                    mTextFieldCount.setText(mCurrentCount.toString());
                } else {//если первый заход сегодня

                    //берем текущие значени за прошлый день
                    Integer mPrefCount = sPref.getInt(KEY_PREF_COUNT,500);
                    String mPrefDate = sPref.getString(KEY_PREF_DATE,"default");

                    //получаем ссылку на БД
                    DatabaseHelper = new SessionDatabaseHelper(getApplication());
                    db = DatabaseHelper.getWritableDatabase();//разрешаем чтение и запись
                    //добавляет запись в БД с данными за прошлый день на момент первого входа на текущий день
                    DatabaseHelper.insertSession(db, mPrefDate, mPrefCount);
                    db.close();//закрывает БД

                    //обновляем дату и обнуляем счетчик в sPref, обновляем mTextFieldCount
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(KEY_PREF_DATE, mLocalDate);
                    ed.putInt(KEY_PREF_COUNT,0);
                    ed.commit();
                    mCurrentCount = 0;
                    mTextFieldCount.setText("0");
                }
            }

        }

        if(savedInstanceState != null){//проверяем что это после переворота
            mBound= savedInstanceState.getBoolean("mBound");
            mCurrentCount = savedInstanceState.getInt("mCurrentCount");
            mTextFieldCount.setText(mCurrentCount.toString());
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
                    case ST_TIMER_FINISH:
                        mCurrentCount=intent.getExtras().getInt(KEY_COUNT);
                        mTextFieldCount.setText(mCurrentCount.toString());
                        mTextField.setText(mDefaultTime);
                        dlg1.show(getSupportFragmentManager(), "IsBreak");
                        break;
                    case ST_TIMER_STOPED:
                        mTextField.setText(mDefaultTime);
                        if(dlg1!=null){dlg1.dismiss();}
                        break;
                    case ST_BREAK_ENDED:
                        mTextField.setText(mDefaultTime);
                        dlg2.show(getSupportFragmentManager(), "BreakEnded");
                        break;
                    case ST_BREAK_STARTED_IN_NOTIF:
                        dlg1.dismiss();
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
                    case R.id.btn_start:
                        startTimeService(mIntent);
                        Log.d(TAG, "MainActivity: btn_Start");
                        break;
                    case R.id.btn_stop:
                        Log.d(TAG, "MainActivity: btn_Stop");
                        if(mBound){mTimerService.TimerStop();}
                        mTextField.setText(mDefaultTime);
                        break;
                    case R.id.btn_pause:
                        Log.d(TAG, "MainActivity: btn_Pause");
                        if(mBound){mTimerService.TimerPause();}
                        break;
                }
            }
        };
        //регистрируем слушателей кнопок и настроек
        mStartButton.setOnClickListener(clickListener);
        mStopButton.setOnClickListener(clickListener);
        mPauseButton.setOnClickListener(clickListener);
        sPrefSettings.registerOnSharedPreferenceChangeListener(this);

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
    }

    public void onBreakTime(boolean isStart) {
        if(isStart){
        Log.d(TAG, "MainActivity: onBreakTime()");
        Intent mIntentBreak = new Intent(MainActivity.this, TimerService.class);
        mIntentBreak.putExtra(KEY_STATE,ST_BREAK_STARTED);
        startTimeService(mIntentBreak);
        } else {
            startTimeService(mIntent);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity: onStart + bindService");
        super.onStart();
        bindService(mIntent, mConn, 0);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity: onStop + unbindService");
        super.onStop();
        if (!mBound) return;
        unbindService(mConn);
        mBound = false;
        try {
            unregisterReceiver(uiUpdated);
            unregisterReceiver(brForSignals);
        } catch (Exception e) {
            Log.d(TAG, "MainActivity: onStop - Unregistered receiver ERROR");
            // Receiver was probably already stopped in onPause()
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(uiUpdated, new IntentFilter("TIMER_UPDATED"));
        registerReceiver(brForSignals, new IntentFilter(BR_FOR_SIGNALS));
        Log.d(TAG, "MainActivity: onResume + Registered receiver");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(uiUpdated);
        unregisterReceiver(brForSignals);
        Log.d(TAG, "MainActivity: onPause + Unregistered receiver");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainActivity: onDestroy");
        super.onDestroy();
        sPrefSettings.unregisterOnSharedPreferenceChangeListener(this);
    }

    void NewPreferences(){
        Log.d(TAG, "MainActivity: NewPreferences");
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(KEY_PREF_DATE, mLocalDate);
        ed.putInt(KEY_PREF_COUNT,0);
        ed.commit();
        mCurrentCount = 0;
        mTextFieldCount.setText("0");
    }

    //устанавливаем МЕНЮ
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //создаем "заполнитель" меню
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //реагируем на нажатие элементов меню
        int id = item.getItemId();
        switch (id) {
            //открываем активити с настройками
            case (R.id.settings):
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                return true;
            case (R.id.about):
                //открываем активити с инфой
                Intent openAbout = new Intent(this, AboutActivity.class);
                startActivity(openAbout);
                return true;
            case (R.id.statistic):
                //открываем активити со статистикой
                Intent openStat = new Intent(this, StatisticActivity.class);
                startActivity(openStat);
                return true;
            default:
                return false;
        }
    }
    private void setDefaultTimeFromPref(SharedPreferences sPref) {
        //проверяем настройку с дефолтным интервалом, если ее нет то устанавливается - defValue
        int mDefaultMinutes = Integer.valueOf(sPref.getString(KEY_PREF_INTERVAL, "45"));

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
        if (key.equals("default_interval")) {
            setDefaultTimeFromPref(sharedPreferences);
        }
    }


}


// Добавить в нотификейшн - Стоп и Пауза
// Добавить после паузы в нотификейшн - Продолжить? как у гудтайм