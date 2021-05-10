package com.hfad.iqtimer.progress;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.iqtimer.R;

import static android.content.Context.MODE_PRIVATE;

public class ProgressFragment extends Fragment implements View.OnClickListener {

    private static final int KEY_NAME_INT = 3;
    private static final int KEY_DESC_INT = 4;
    private static final int KEY_QPLAN_INT = 5;
    private static final int KEY_PERIOD_PLAN_INT = 6;

    private PrefThread mPrefThread;
    ImageButton btnSetGoal;
    DialogFragmentGoal dlgSetGoal;
    TextView mTvGoalName, mTvGoalDisc,mTvGoalCountPlan;
    ProgressViewModel mProgressViewModel;
    ProgressBar mProgressBarGoal;


    //обработчик Handler для UI-потока, используется фон потоком для взаимодействия с UI-потоком
    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY_NAME_INT:
                    mTvGoalName.setText((String)msg.obj);
                    break;
                case KEY_DESC_INT:
                    mTvGoalDisc.setText((String)msg.obj);
                    break;
                case KEY_PERIOD_PLAN_INT:
                    mTvGoalCountPlan.setText((String)msg.obj);
                    break;
                case KEY_QPLAN_INT:
                    mProgressBarGoal.setMax(Integer.parseInt((String) msg.obj));
                    mProgressBarGoal.setProgress(5);
                    break;

            }
        }
    };

    //фоновый поток который читает и записывает значения в SharedPreferences
    private class PrefThread extends HandlerThread {
        String mKey;
        private final SharedPreferences mPrefs;
        private static final int READ = 1;
        private static final int WRITE = 2;
        private static final String KEY_PREF_NAME = "iqtimer.progresspref.name";
        private static final String KEY_PREF_DESC = "iqtimer.progresspref.description";
        private static final String KEY_PREF_QPLAN = "iqtimer.progresspref.q_in_plan";
        private static final String KEY_PREF_PERIOD_PLAN = "iqtimer.progresspref.period_in_plan";


        private Handler mHandler;

        public PrefThread() {
            super("PrefThread", Process.THREAD_PRIORITY_BACKGROUND);
            mPrefs = requireContext().getSharedPreferences("ProgressPrefs", MODE_PRIVATE);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.arg1) {
                        case KEY_NAME_INT:
                            mKey = KEY_PREF_NAME;
                            break;
                        case KEY_DESC_INT:
                            mKey = KEY_PREF_DESC;
                            break;
                        case KEY_QPLAN_INT:
                            mKey = KEY_PREF_QPLAN;
                            break;
                        case KEY_PERIOD_PLAN_INT:
                            mKey = KEY_PREF_PERIOD_PLAN;
                            break;
                    }
                    switch (msg.what) {
                        case READ:
                            mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.arg1, mPrefs.getString(mKey, "0")));
                            break;
                        case WRITE:
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString(mKey, (String) msg.obj);
                            editor.apply();
                            break;
                    }
                }
            };
        }

        public void read(int keyPrefInt) {
            Message msg = mHandler.obtainMessage(READ, keyPrefInt, 0);
            mHandler.sendMessage(msg);
        }

        public void write(int keyPrefInt, String value) {
            mHandler.sendMessage(mHandler.obtainMessage(WRITE, keyPrefInt, 0, value));
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_progress, null);
        btnSetGoal = (ImageButton) v.findViewById(R.id.img_btn_add_goal);
        btnSetGoal.setOnClickListener(this);
        mTvGoalName = (TextView) v.findViewById(R.id.nameMainGoal);
        mTvGoalDisc = (TextView) v.findViewById(R.id.goalDisc);
        mTvGoalCountPlan = (TextView) v.findViewById(R.id.countGoal_plan_value);
        mProgressBarGoal= (ProgressBar) v.findViewById(R.id.progressBarGoal);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressViewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mProgressViewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String value) {
                mTvGoalName.setText(value);
                writeToPrefs(KEY_NAME_INT,value);
            }
        });
        mProgressViewModel.getDisc().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String value) {
                mTvGoalDisc.setText(value);
                writeToPrefs(KEY_DESC_INT,value);
            }
        });
        //изменяем и сохраняем КОЛИЧЕСТВО СЕССИЙ/УДАРНЫХ ДНЕЙ
        mProgressViewModel.getQNum().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String value) {
                mProgressBarGoal.setMax(Integer.parseInt(value));
                mProgressBarGoal.setProgress(5);
                writeToPrefs(KEY_QPLAN_INT,value);
            }
        });

        //изменяем и сохраняем КОЛИЧЕСТВО ДНЕЙ
        mProgressViewModel.getPeriodNum().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String value) {
                mTvGoalCountPlan.setText(value);
                writeToPrefs(KEY_PERIOD_PLAN_INT,value);
            }
        });

        //запуск фонового потока при создании Активити
        mPrefThread = new PrefThread();
        mPrefThread.start();
    }

    //запись пустого значения из UI-потока
    public void writeToPrefs(int keyPrefInt, String value) {
        mPrefThread.write(keyPrefInt, value);
    }

    //иницилизация чтения из UI-потока
    public void readToPrefs(int keyPrefInt) {
        mPrefThread.read(keyPrefInt);
    }

    @Override
    public void onClick(View v) {
        dlgSetGoal = new DialogFragmentGoal();
        dlgSetGoal.show(getParentFragmentManager(),"dlgSetGoal");
    }

    @Override
    public void onStart() {
        super.onStart();
        //заполняем имя и описание из ProgressPrefs
        readToPrefs(KEY_NAME_INT);
        readToPrefs(KEY_DESC_INT);
        readToPrefs(KEY_QPLAN_INT);
        readToPrefs(KEY_PERIOD_PLAN_INT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //завершаем фоновый поток
        mPrefThread.quit();
    }
}