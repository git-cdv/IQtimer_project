 package com.hfad.iqtimer;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hfad.iqtimer.database.SessionDatabaseHelper;

import java.time.LocalDate;
import java.util.Locale;

public class MainViewModel extends AndroidViewModel {

    private SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    CountDownTimer mTimer;
    int mCountSessionDay;
    String mLocalDate;
    int mCurrentLastCount;//переменная с последним значение кол-ва сессий сегодня в БД
    //т.к. ViewModel никак не связана с Активити, то для обновления mTextField из таймера мы создаем LiveData
    MutableLiveData<String> mLiveDataTime = new MutableLiveData<>();
    //MutableLiveData используется для того чтобы можно было изменять ViewModel из других классов (и еще с ней можно работать с разных потоков)
    MutableLiveData<Integer> mLiveDataSession = new MutableLiveData<>();
    long millisInFuture = 5000;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    void startTimer() {
        mTimer = new CountDownTimer(millisInFuture, 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;

                if (millisInFuture >= 3600000) {//если время отчета равно или больше 1 часа, то формат с часами
                    mLiveDataTime.setValue(String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600,
                            (seconds % 3600) / 60, (seconds % 60)));
                } else {//формат с минутами и секундами
                    mLiveDataTime.setValue(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
                }
            }

            public void onFinish() {
                //увеличивает количество пройденных сессиий
                mCountSessionDay++;
                //обновляет количество в mLiveDataSession
                mLiveDataSession.setValue(mCountSessionDay);
            }
        }.start();
    }

    void stopTimer() {
        mTimer.cancel();
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCleared() {
        super.onCleared();
        if (isHaveNoteToday()) {//если запись на сегодня уже была - то обновляем
            //обновляем текущее значение счетчика в БД
            String strSQL = "UPDATE SESSIONS SET SESSION_COUNT =" + mCountSessionDay + " WHERE DATE = '" + mLocalDate + "';";
            db.execSQL(strSQL);
            db.close();
        } else {
            //добавляет запись в БД с текущей датой и значение счетчика на момент "убивания" приложения
            DatabaseHelper.insertSession(db, mLocalDate, mCountSessionDay);
            db.close();//закрывает БД
        }

    }

    //узнаем если уже запись сегодня с количеством сессий
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isHaveNoteToday() {
        //передаем переменной текущую дату как строку
        mLocalDate = (LocalDate.now()).toString();
        //получаем ссылку на БД
        DatabaseHelper = new SessionDatabaseHelper(getApplication());
        db = DatabaseHelper.getWritableDatabase();//разрешаем чтение и запись

        String sql = "SELECT EXISTS (SELECT * FROM SESSIONS WHERE DATE='" + mLocalDate + "' LIMIT 1)";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        // cursor.getInt(0) is 1 if column with value exists
        if (cursor.getInt(0) == 1) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public int CurrentLastCount() {
        //создаем курсор для поиска строки с записью сегодняшнего дня
        Cursor cursor = db.query("SESSIONS",
                new String[]{"DATE", "SESSION_COUNT"},
                "DATE = ?",
                new String[]{mLocalDate},
                null, null, null);
        //переходим курсором и получем текущее значение счетчика в БД
        if (cursor.moveToFirst()) {
            mCountSessionDay = cursor.getInt(1);
            //обновляет количество в mLiveDataSession
            mLiveDataSession.setValue(mCountSessionDay);
        }
        cursor.close();
        return mCurrentLastCount;
    }
}
