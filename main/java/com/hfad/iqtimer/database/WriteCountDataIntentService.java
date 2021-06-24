package com.hfad.iqtimer.database;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class WriteCountDataIntentService extends IntentService {
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String TAG = "MYLOGS";
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    LocalDate mToday;

    public WriteCountDataIntentService() {
        super("WriteCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "IntentService: onHandleIntent");
        //получаем доступ к файлу с данными по дате и сессиям
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        ed = sPref.edit();
        // текущая дата
        mToday = LocalDate.now();

        //проверяем что это не первый вход
        if (sPref.getBoolean("firstrun", true)) {
            Log.d(TAG, "WriteCountDataIntentService: checkFirstRun()");
            ed.putString(KEY_PREF_DATE, mToday.toString());
            ed.putInt(KEY_PREF_COUNT, 0);
            ed.putBoolean("firstrun", false);
            ed.apply();
        } else {
            //берем текущие значения за крайний день
            int mPrefCount = sPref.getInt(KEY_PREF_COUNT, 500);
            String mPrefDate = sPref.getString(KEY_PREF_DATE, "default");
            //получаем текущую дату из sPref
            LocalDate mDateFromPref = LocalDate.parse(mPrefDate);
            //подготавливааем формат для date_full
            DateTimeFormatter fmtOut = DateTimeFormat.forPattern("E, MMM d, yyyy");
            String strOutput = fmtOut.print(mDateFromPref);

            //получаем ссылку на БД
            AppDatabase db = App.getInstance().getDatabase();
            //получаем Dao для операций с БД
            SessionDao sessionDao = db.sessionDao();
            //записываем последние данные
            sessionDao.insert(new Session(mPrefDate,mPrefCount,strOutput));

            //обновляем дату и обнуляем счетчик в sPref
            ed.putString(KEY_PREF_DATE, mToday.toString());
            ed.putInt(KEY_PREF_COUNT, 0);
            ed.apply();

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