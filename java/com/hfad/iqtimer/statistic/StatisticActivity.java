 package com.hfad.iqtimer.statistic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.SessionDatabaseHelper;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StatisticActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    TextView mTextObzorDay,mTextObzorWeek,mTextObzorMonth,mTextObzorTotal;
    SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    static Cursor sCursor;
    SharedPreferences sPref;
    Integer mPrefCount;
    Integer mCountWeek;
    Integer mCountMonth;
    Integer mCountTotal;

    private static final String TAG = "MYLOGS";
    private static final String KEY_PREF_COUNT = "prefcount";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        //получаем ссылку на БД
        DatabaseHelper = new SessionDatabaseHelper(getApplication());
        db = DatabaseHelper.getReadableDatabase();//разрешаем чтение

        //получаем доступ к файлу с данными по дате и сессиям
        sPref = getSharedPreferences("prefcount", MODE_PRIVATE);
        mTextObzorDay = (TextView) findViewById(R.id.obzor_stat_day);
        mTextObzorWeek = (TextView) findViewById(R.id.obzor_stat_week);
        mTextObzorMonth = (TextView) findViewById(R.id.obzor_stat_month);
        mTextObzorTotal = (TextView) findViewById(R.id.obzor_stat_total);

        if(savedInstanceState == null) {//проверяем что это не после переворота, а следующий вход
            //берем текущие значение
            mPrefCount = sPref.getInt(KEY_PREF_COUNT,0);
            mTextObzorDay.setText(mPrefCount.toString());

            // создаем лоадер для чтения данных (работает только с фрагментами)
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }

        if(savedInstanceState != null){//проверяем что это после переворота
            mCountWeek = savedInstanceState.getInt("mCountWeek");
            mCountMonth = savedInstanceState.getInt("mCountMonth");
            mCountTotal = savedInstanceState.getInt("mCountTotal");
            mPrefCount = savedInstanceState.getInt("mPrefCount");
            mTextObzorDay.setText(mPrefCount.toString());
            mTextObzorWeek.setText(mCountWeek.toString());
            mTextObzorMonth.setText(mCountMonth.toString());
            mTextObzorTotal.setText(mCountTotal.toString());

        }


        //добавляем фрагмент для отображения в контейнере
        Fragment fragHistoryDays = new StatisticHistoryDaysFragment();
        Fragment fragHistoryMonth = new StatisticHistoryMonthFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.frgmContainer, fragHistoryDays);//1 - в каком контейнере, 2 - какой фрагмент добавить
        ft.commit();

        // Создаем адаптер, используем simple_spinner_item в качестве layout для отображения Spinner на экран
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.forSpinnerHistory, R.layout.spinner_item);
        Spinner spinner = (Spinner) findViewById(R.id.history_spinner);
        spinner.setAdapter(adapter);
        // выделяем элемент
        spinner.setSelection(0);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if(position==1){
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.remove(fragHistoryDays);
                    ft.replace(R.id.frgmContainer, fragHistoryMonth);//1 - в каком контейнере, 2 - какой фрагмент добавить
                    ft.commit();

                }else {
                    //добавляем фрагмент для отображения в контейнере
                    Fragment fragHistoryDays = new StatisticHistoryDaysFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.remove(fragHistoryMonth);
                    ft.replace(R.id.frgmContainer, fragHistoryDays);//1 - в каком контейнере, 2 - какой фрагмент добавить
                    ft.commit();
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

         //создаем стрелку НАЗАД
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.stat_actionbar_title);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stat_menu, menu);
        return true;

    }

    @Override
    //обрабатываем стрелку НАЗАД
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //обработка кнопки назад
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        //обработка кнопки Списка
        if (id == R.id.stat_list) {
        Intent i = new Intent(this,StatisticListActivity.class);
        startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


    void setupHistoryChart() {

        getDataForChart();

        mCountWeek = mCountWeek+mPrefCount;
        mCountMonth = mCountMonth+mPrefCount;
        mCountTotal = mCountTotal+mPrefCount;

        mTextObzorWeek.setText(mCountWeek.toString());
        mTextObzorMonth.setText(mCountMonth.toString());
        mTextObzorTotal.setText(mCountTotal.toString());

         }

    void getDataForChart() {
        Log.d(TAG, "StatisticActivity: getDataForChart");
        boolean isCountWeek =true;
        boolean isCountMonth =true;
        mCountWeek =0;
        mCountMonth = 0;
        mCountTotal = 0;


        //перебор всех записей в курсоре
        while (sCursor.moveToNext()) {
            int strCountSession = sCursor.getInt(2);
            String strCountDate = sCursor.getString(1);

            LocalDate mDateFromCursor = LocalDate.parse(strCountDate);

            if (isCountWeek){
                mCountWeek = mCountWeek+strCountSession;
                if (mDateFromCursor.getDayOfWeek() == DateTimeConstants.MONDAY){
                    isCountWeek =false;
                }
            }

            if (isCountMonth){
                mCountMonth = mCountMonth+strCountSession;
                if (mDateFromCursor.getDayOfMonth() == 1){
                    isCountMonth =false;
                }
            }

            //считаем сумму сессий Total
            mCountTotal = mCountTotal+strCountSession;
        }

        }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("mPrefCount",mPrefCount);
        savedInstanceState.putInt("mCountWeek",mCountWeek);
        savedInstanceState.putInt("mCountMonth",mCountMonth);
        savedInstanceState.putInt("mCountTotal",mCountTotal);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sCursor.close();
        db.close();
    }

    @NonNull
    @Override
    //создаем Loader и даем ему на вход объект для работы с БД
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "StatisticActivity: onCreateLoader");
        return new MyCursorLoader(this, db);
    }

    @Override
    //мы получаем результат работы лоадера – новый курсор с данными. Этот курсор мы отдаем адаптеру методом swapCursor.
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "StatisticActivity: onLoadFinished");
        sCursor = data;
        //запускаем загрузку графика, как готов курсор
        setupHistoryChart();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "StatisticActivity: onLoadReset");

    }

    //наш лоадер, наследник класса CursorLoader. У него мы переопределяем метод loadInBackground, в котором просто получаем курсор с данными БД
    static class MyCursorLoader extends CursorLoader {
        SQLiteDatabase db;

        public MyCursorLoader(Context context, SQLiteDatabase db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "StatisticActivity: onLoadReset");
            //Курсор возвращает значения "_id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
            //с сортировкой по убыванию ИД
            sCursor = db.query("SESSIONS",
                    new String[]{"_id","DATE", "SESSION_COUNT"},
                    null, null, null, null, "_id DESC");
            return sCursor;
        }
    }


}

