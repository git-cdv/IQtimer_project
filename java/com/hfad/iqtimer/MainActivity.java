package com.hfad.iqtimer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String COUNT_S = "cs" ;
    TextView mTextField,mTextFieldCount;
    Button mStartButton, mStopButton, mResetButton;
    Integer mCountSession;
    MainViewModel mViewModel;//создаем экземпляр ViewModel
    Integer mCurrentLastCount;//переменная с последним значение кол-ва сессий сегодня в БД

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextField = (TextView) findViewById(R.id.timer_view);
        mTextFieldCount = (TextView) findViewById(R.id.count_ses);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mResetButton = (Button) findViewById(R.id.btn_reset);

        if(savedInstanceState != null){
            mCountSession=savedInstanceState.getInt(COUNT_S);
            mTextFieldCount.setText(mCountSession.toString());
        }

        //получаем ViewModel
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_start: mViewModel.startTimer(); break;
                    case R.id.btn_stop:  mViewModel.stopTimer(); break;
                    case R.id.btn_reset: mViewModel.startTimer(); break;
                }
            }
        };

        mStartButton.setOnClickListener(clickListener);
        mStopButton.setOnClickListener(clickListener);
        mResetButton.setOnClickListener(clickListener);


        if(savedInstanceState == null) {//проверяем что это не после переворота, а следующий вход
            //определяем и вывод текущее значение счетчика сессий
            if (mViewModel.isHaveNoteToday()) {
                //получаем и выводим значение последней записи
                mViewModel.CurrentLastCount();
            } else {
                mTextFieldCount.setText("0");
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        //подкючаем слушателя изменений LiveData Таймера
        mViewModel.mLiveDataTime.observe(this, new Observer<String>(){
            @Override
            public void onChanged(String s) {
                mTextField.setText(s);
            }
        });

        //подкючаем слушателя изменений LiveData Счетчика Сессий
        mViewModel.mLiveDataSession.observe(this, new Observer<Integer>(){
            @Override
            public void onChanged(Integer s) {
                mCountSession = s;
                mTextFieldCount.setText(s.toString());
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(COUNT_S,mCountSession);
    }
}