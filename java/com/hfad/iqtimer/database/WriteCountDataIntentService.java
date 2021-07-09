package com.hfad.iqtimer.database;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.room.Room;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WriteCountDataIntentService extends IntentService {
    private static final String TAG = "MYLOGS";
    LocalDate mToday;

    public WriteCountDataIntentService() {
        super("WriteCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "IntentService: onHandleIntent");
        mToday = LocalDate.now();

        //проверяем что это не первый вход
        if (PrefHelper.isFirstRun()) {
            PrefHelper.setDateAndCount(mToday.toString(),0);
            PrefHelper.setFirstRun(false);
        } else {
            //берем текущие значения за крайний день
            int mPrefCount = PrefHelper.getCount();
            String mPrefDate = PrefHelper.getWorkDate();
            //получаем текущую дату из sPref
            LocalDate mDateFromPref = LocalDate.parse(mPrefDate);
            //подготавливааем формат для date_full
            DateTimeFormatter fmtOut = DateTimeFormat.forPattern("E, MMM d, yyyy");
            String strOutput = fmtOut.print(mDateFromPref);

            //получаем ссылку на БД
            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database")
                    .build();
            //получаем Dao для операций с БД
            SessionDao sessionDao = db.sessionDao();
            //записываем последние данные
            sessionDao.insert(new Session(mPrefDate,mPrefCount,strOutput));

            //обновляем дату и обнуляем счетчик в sPref
            PrefHelper.setDateAndCount(mToday.toString(),0);

            // получаем следующую дату от крайней
            LocalDate mNextDateFromPref = mDateFromPref.plusDays(1);

            //пока дата не равна текущему дню - проставляем 0
            while (mNextDateFromPref.getDayOfYear() != mToday.getDayOfYear()) {

                String mNextDate = mNextDateFromPref.toString("yyyy-MM-dd");
                String mNextDateFull = mNextDateFromPref.toString("E, MMM d, yyyy");

                sessionDao.insert(new Session(mNextDate,0,mNextDateFull));
                // получаем следующую дату
                mNextDateFromPref = mNextDateFromPref.plusDays(1);
            }

            db.close();//закрывает БД
        }
    }

}