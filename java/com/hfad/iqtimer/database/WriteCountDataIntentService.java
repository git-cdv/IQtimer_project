package com.hfad.iqtimer.database;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


public class WriteCountDataIntentService extends IntentService {
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_DATE = "prefdate";
    private static final String TAG = "MYLOGS";
    Calendar cLastDate;

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

        //получаем ссылку на БД
        SessionDatabaseHelper DatabaseHelper = new SessionDatabaseHelper(getApplication());
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase();//разрешаем чтение и запись
        DatabaseHelper.insertSession(db, mPrefDate, mPrefCount);//записываем последние данные

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
        SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        //Создаём дату с помощью форматтера, который в свою очередь парсит её из входной строки
        try {
            //получаем последнюю дату в БД из строки
            cLastDate = Calendar.getInstance();
            cLastDate.setTime(formatterIn.parse(strDate));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        // текущая дата
        Calendar cToday = new GregorianCalendar();
        // получаем следующую дату
        cLastDate.add(Calendar.DAY_OF_YEAR, 1);

        //пока дата не равна текущему дню - проставляем 0
        while(cLastDate.get(Calendar.YEAR) != cToday.get(Calendar.YEAR) &&
                cLastDate.get(Calendar.DAY_OF_YEAR) != cToday.get(Calendar.DAY_OF_YEAR)){
            String mDate = formatterIn.format(cLastDate.getTime());
            DatabaseHelper.insertSession(db, mDate, 0);
            // получаем следующую дату
            cLastDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        db.close();//закрывает БД
        mCursor.close();
        }

}