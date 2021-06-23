package com.hfad.iqtimer.statistic;

import android.content.Context;
import android.content.SharedPreferences;

import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.database.AppDatabase;
import com.hfad.iqtimer.database.Session;
import com.hfad.iqtimer.database.SessionDao;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static android.content.Context.MODE_PRIVATE;

interface RepositoryCallback {
    void onComplete(int[] result);
}

public class StatisticRepository {
    private static final String KEY_PREF_COUNT = "prefcount";

    Context context;
    ExecutorService executor;

    public StatisticRepository(Context context) {
        this.context = context;
    }

    public void getDataObzor(final RepositoryCallback callback) {
        executor = App.getInstance().getExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isCountWeek =true;
                boolean isCountMonth =true;
                int mCountWeek =0;
                int mCountMonth = 0;
                int mCountTotal = 0;


                AppDatabase db = App.getInstance().getDatabase();
                SessionDao sesDao = db.sessionDao();
                List<Session> sessionList = sesDao.getAll();

                SharedPreferences mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
                int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);

                //перебираем все строки в sessionList
                for (Session item : sessionList) {
                    int strCountSession = item.count;
                    String strCountDate = item.date;

                    LocalDate mDate = LocalDate.parse(strCountDate);

                    if (isCountWeek){
                        mCountWeek = mCountWeek+strCountSession;
                        if (mDate.getDayOfWeek() == DateTimeConstants.MONDAY){
                            isCountWeek =false;
                        }
                    }

                    if (isCountMonth){
                        mCountMonth = mCountMonth+strCountSession;
                        if (mDate.getDayOfMonth() == 1){
                            isCountMonth =false;
                        }
                    }

                    //считаем сумму сессий Total
                    mCountTotal = mCountTotal+strCountSession;

                    mCountWeek = mCountWeek+mPrefCount;
                    mCountMonth = mCountMonth+mPrefCount;
                    mCountTotal = mCountTotal+mPrefCount;
                }
                int [] array = {mPrefCount,mCountWeek,mCountMonth,mCountTotal};

                callback.onComplete(array);
            }
        });
    }

}
