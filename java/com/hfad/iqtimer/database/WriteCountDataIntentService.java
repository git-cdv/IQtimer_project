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

public class WriteCountDataIntentService extends IntentService {
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String TAG = "MYLOGS";


    public WriteCountDataIntentService() {
        super("WriteCountDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "IntentService: onHandleIntent");
        String mLocalDate = (LocalDate.now()).toString();
        //получаем доступ к файлу с данными по дате и сессиям
        SharedPreferences sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        //берем текущие значения за крайний день
        Integer mPrefCount = sPref.getInt(KEY_PREF_COUNT,500);
        String mPrefDate = sPref.getString(KEY_PREF_DATE,"default");
        //получаем текущую дату из sPref
        LocalDate mDateFromsPref = LocalDate.parse(mPrefDate);
        //подготавливааем формат
        DateTimeFormatter fmtOut = DateTimeFormat.forPattern("E, MMM. d, yyyy");
        String strOutput = fmtOut.print(mDateFromsPref);

        //получаем ссылку на БД
        SessionDatabaseHelper DatabaseHelper = new SessionDatabaseHelper(getApplication());
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase();//разрешаем чтение и запись
        DatabaseHelper.insertSession(db, mPrefDate, mPrefCount,strOutput);//записываем последние данные

        //обновляем дату и обнуляем счетчик в sPref
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(KEY_PREF_DATE, mLocalDate);
        ed.putInt(KEY_PREF_COUNT,0);
        ed.commit();

        Cursor mCursor = db.query("SESSIONS",
                new String[]{"DATE", "SESSION_COUNT"},
                null, null, null, null, null);

        //получаем и парсим последнюю дату в курсоре
        mCursor.moveToLast();
        String strDate = mCursor.getString(0);
        LocalDate mDateFromCursor = LocalDate.parse(strDate);

        // текущая дата
        LocalDate mToday = LocalDate.now();
        // получаем следующую дату
        LocalDate mNextDateFromCursor = mDateFromCursor.plusDays(1);

        //пока дата не равна текущему дню - проставляем 0
        while(mNextDateFromCursor.getDayOfYear() != mToday.getDayOfYear()){

            String mNextDate = mNextDateFromCursor.toString("yyyy-MM-dd");
            String mNextDateFull = mNextDateFromCursor.toString("E, MMM. d, yyyy");

            DatabaseHelper.insertSession(db, mNextDate, 0,mNextDateFull);
            // получаем следующую дату
            mNextDateFromCursor = mNextDateFromCursor.plusDays(1);
        }

        db.close();//закрывает БД
        mCursor.close();
        }

}