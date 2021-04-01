package com.hfad.iqtimer.statistic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.SessionDatabaseHelper;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView mTextObzorDay,mTextObzorWeek,mTextObzorMonth,mTextObzorTotal;
    SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    static Cursor sCursor;
    SimpleCursorAdapter listAdapter;
    SharedPreferences sPref;
    Integer mPrefCount;
    Integer mCountWeek;
    Integer mCountMonth;
    Integer mCountTotal;
    BarChart mBarChart;
    ArrayList<BarEntry> arrayForChart;
    String[] datesForChart;

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
        mBarChart = (BarChart) findViewById(R.id.history_chart);
        //берем текущие значение
        mPrefCount = sPref.getInt(KEY_PREF_COUNT,0);
        mTextObzorDay.setText(mPrefCount.toString());

        // создаем лоадер для чтения данных (работает только с фрагментами)
        LoaderManager.getInstance(this).initLoader(0, null, this);

             // формируем столбцы сопоставления
            String[] from = new String[]{"DATE", "SESSION_COUNT"};
            int[] to = new int[]{R.id.stat_date, R.id.stat_count};

            listAdapter = new SimpleCursorAdapter(this,
                    R.layout.stat_view_item,//Как должны выводиться данные.
                    null,//в варианте с лоадером - курсор передает лоадре
                    from,//Вывести содержимое столбца в надписях внутри компонента ListView
                    to,//куда сопоставить и вывести
                    0);
            //получаем ссылку на списковое представление
            ListView listStat = (ListView) findViewById(R.id.list_stat);

            listStat.setAdapter(listAdapter);

        mCountWeek =0;
        mCountMonth = 0;
        mCountTotal = 0;

        mCountWeek = mCountWeek+mPrefCount;
        mCountMonth = mCountMonth+mPrefCount;
        mCountTotal = mCountTotal+mPrefCount;
        mTextObzorWeek.setText(mCountWeek.toString());
        mTextObzorMonth.setText(mCountMonth.toString());
        mTextObzorTotal.setText(mCountTotal.toString());

        setupHistoryChart();

       //////БЛОК ОБЗОРА
        ///СПИНЕР ОБЗОРА
        // Создаем адаптер, используем simple_spinner_item в качестве layout для отображения Spinner на экран
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        //методом setDropDownViewResource указываем какой layout использовать для прорисовки пунктов выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.obzor_spinner);
        spinner.setAdapter(adapter);
        // выделяем элемент
        spinner.setSelection(1);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //обработка нажатия

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });



    }

    void setupHistoryChart() {

        getDataForChart();

        String stringDescription = getResources().getString(R.string.stat_chart_description);
        String stringPlanDay = getResources().getString(R.string.stat_chart_planday);

        //создаем через свой класс, где переопределен метод вывода цвета для бара
        MyBarDataSet barDataSet1 = new MyBarDataSet(arrayForChart,stringDescription);
        //назначаем цвета для баров
        barDataSet1.setColors(new int[]{Color.RED, Color.GREEN });
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return datesForChart[(int) value];
            }
        };
        //настройка оси Х (шаг и формат подписей)
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        //добавление "линии тренда" (план)
        YAxis leftAxis = mBarChart.getAxisLeft();
        LimitLine ll = new LimitLine(4f, stringPlanDay);
        ll.setLineColor(Color.RED);
        //как пунктир
        ll.enableDashedLine(5f,5f,2f);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.RED);
        ll.setTextSize(10f);
        leftAxis.addLimitLine(ll);

        mBarChart.setData(barData);
        //устанавливает количество Баров для отображение, если больше - скролится
        mBarChart.setVisibleXRangeMaximum(14f);
        //убираем description
        Description description = mBarChart.getDescription();
        description.setEnabled(false);
        mBarChart.invalidate();
        }

    void getDataForChart() {
        Log.d(TAG, "StatisticActivity: getDataForChart");
        int mCountArray =0;

        //Курсор возвращает значения "_id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
        Cursor cursor = db.query("SESSIONS",
                new String[]{"_id", "DATE", "SESSION_COUNT"},
                null, null, null, null, null);

        //создаем массив для графика
        arrayForChart = new ArrayList<BarEntry>();
        //создаем массив для значений замен на XAxis
        datesForChart = new String [cursor.getCount()];
        //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
        SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatterOut = new SimpleDateFormat("MMM-dd", Locale.ENGLISH);

        //перебор всех записей в курсоре
        while (cursor.moveToNext()) {
            String strDate = cursor.getString(1);
            int strCountSession = cursor.getInt(2);
            Date date=null;

            try {
                //Создаём дату с помощью форматтера, который в свою очередь парсит её из входной строки
                date = formatterIn.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Добавляем индекс и счетчик в массив
            arrayForChart.add(new BarEntry(mCountArray,strCountSession));

            //Добавляем дату в формате formatterOut в массив для замен на XAxis
            datesForChart[mCountArray] = formatterOut.format(date);
            mCountArray++;
        }

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
        //передаем адаптеру для отображения в листе
        listAdapter.swapCursor(data);

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
            sCursor = db.query("SESSIONS",
                    new String[]{"_id", "DATE", "SESSION_COUNT"},
                    null, null, null, null, null);
            return sCursor;
        }
    }

    class MyBarDataSet extends BarDataSet {

        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() < 4){
                return mColors.get(0);}
            else {
                return mColors.get(1);}

        }

    }
}

/*    //Курсор возвращает значения "_id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
    Cursor sCursor2 = db.query("SESSIONS",
            new String[]{"_id", "DATE", "SESSION_COUNT"},
            null, null, null, null, null);

    DataPoint[] array = new DataPoint[sCursor2.getCount()];
    int mCountArray=0;
        mCountWeek =0;
                mCountMonth = 0;
                mCountTotal = 0;

                //перебор всех записей в курсоре
                while (sCursor2.moveToNext()) {
                Date date=null;
                String strDate = sCursor2.getString(1);
                int strCountSession = sCursor2.getInt(2);
                //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                try {
                //Создаём дату с помощью форматтера, который в свою очередь парсит её из входной строки
                date = formatter.parse(strDate);
                } catch (ParseException e) {
                e.printStackTrace();
                }
                //Добавляем дату и счетчик в массив
                array[mCountArray] = new DataPoint(date, strCountSession);
                mCountArray++;
                //считаем сумму сессий за последние 7 дней
                if (mCountArray<7){mCountWeek = mCountWeek+strCountSession;}
        //считаем сумму сессий за последние 30 дней
        if (mCountArray<30){mCountMonth = mCountMonth+strCountSession;}
        //считаем сумму сессий Total
        mCountTotal = mCountTotal+strCountSession;
        }
        //текущая дата
        Calendar calendar = Calendar.getInstance();
        calendar.add(calendar.DAY_OF_MONTH, -1);
        Date dayYest = calendar.getTime();
        //Прибавление и вычитание значений в классе Calendar осуществляется с помощью метода add().
        // В него необходимо передать то поле, которое ты хочешь изменить, и число - сколько именно ты хочешь прибавить/убавить от текущего значения.
        calendar.add(calendar.DAY_OF_MONTH, -7);
        //дата 7 дней назад
        Date d2 = calendar.getTime();*/

