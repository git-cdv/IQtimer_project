package com.hfad.iqtimer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SessionDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sessions"; // the name of our database
    private static final int DB_VERSION = 2; // the version of the database

    //Вызываем конструктор суперкласса SQLiteOpenHelper и передаем ему имя и версию базы данных
    //Если имя не задано, то база данных будет существовать только в памяти, и при закрытии пропадет
    public SessionDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    //вызывается при первом создании базы данных на устройстве - ждет пустую БД
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    //метод для заполнения БД
    public void insertSession(SQLiteDatabase db, String date, int count, String datefull) {
        //Объект ContentValues описывает набор данных.
        // Обычно создается новый объект ContentValues для каждой строки данных, которую потребуется создать.
        ContentValues sessionValues = new ContentValues();
        //добавляет данные в виде пар "имя/значение": NAME — столбец, в который добавляются данные, value — сами данные
        sessionValues.put("DATE", date);
        sessionValues.put("SESSION_COUNT", count);
        sessionValues.put("DATEFULL", datefull);
        //В таблицу вставляется одна строка
        //второй параметр нужен если надо вставить пустую строку
        db.insert("SESSIONS", null, sessionValues);
    }

    /*  Поскольку изменения должны распространяться как на новых, так и на существующих пользователей,
      соответствующий код должен быть включен как в метод onCreate(), так и в метод onUpgrade().
      Метод onCreate() гарантирует, что новый столбец будет присутствовать у всех новых пользователей, а методonUpgrade()
      позаботится о том, чтобы он был и у всех существующих пользователей.
      Вместо того, чтобы повторять похожий код в методах onCreate() и onUpgrade(), мы создадим отдельный метод updateMyDatabase(),
      который будет вызываться из onCreate() и onUpgrade().*/
    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            //создание таблицы с указанием названий столбцов и типов данных в них
            db.execSQL("CREATE TABLE SESSIONS (_id INTEGER PRIMARY KEY AUTOINCREMENT, "//первичный ключ с именем _id - обязателен
                    + "DATE TEXT, "
                    + "SESSION_COUNT INTEGER);");
        }
        if (oldVersion < 2) { //код для обновления структуры БД
            //добавляем столбец DATEFULL
            db.execSQL("ALTER TABLE SESSIONS ADD COLUMN DATEFULL TEXT;");
        }
    }
}
