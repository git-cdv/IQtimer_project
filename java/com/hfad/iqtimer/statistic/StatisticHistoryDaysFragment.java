package com.hfad.iqtimer.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticHistoryDaysFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    int mPlanDefault;
    BarChart mBarChart;
    ArrayList<BarEntry> arrayForChart;
    String[] datesForChart;
    static Cursor mCursor;

    private static final String TAG = "MYLOGS";
    private static final String KEY_PREF_PLAN = "set_plan_day";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stat_history_days, null);

        //получаем ссылку на БД
        DatabaseHelper = new SessionDatabaseHelper(getActivity());
        db = DatabaseHelper.getReadableDatabase();//разрешаем чтение

        LoaderManager.getInstance(this).initLoader(0, null, this);

        //получаем доступ к файлу с настройками приложения
        SharedPreferences sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //вытаскиваем дефолтную значение плана из настроек и присваем переменной
        mPlanDefault = Integer.valueOf(sPrefSettings.getString(KEY_PREF_PLAN, "5"));
        mBarChart = (BarChart) v.findViewById(R.id.history_chart_days);

        return v;
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
        LimitLine ll = new LimitLine(mPlanDefault, stringPlanDay);
        ll.setLineColor(Color.RED);
        //как пунктир
        ll.enableDashedLine(16f,4f,2f);
        ll.setLineWidth(1f);
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

    @NonNull
    @Override
    //создаем Loader и даем ему на вход объект для работы с БД
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "StatisticHistoryDaysFragment: onCreateLoader");
        return new MyCursorLoader(getActivity(), db);
    }

    @Override
    //мы получаем результат работы лоадера – новый курсор с данными. Этот курсор мы отдаем адаптеру методом swapCursor.
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "StatisticHistoryDaysFragment: onLoadFinished");
        mCursor = data;
        //запускаем загрузку графика, как готов курсор
        setupHistoryChart();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "StatisticHistoryDaysFragment: onLoadReset");

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
            Log.d(TAG, "StatisticHistoryDaysFragment: onLoadReset");
            //Курсор возвращает значения "_id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
            mCursor = db.query("SESSIONS",
                    new String[]{"_id", "DATE", "SESSION_COUNT"},
                    null, null, null, null, null);
            return mCursor;
        }
    }

    class MyBarDataSet extends BarDataSet {

        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() < mPlanDefault){
                return mColors.get(0);}
            else {
                return mColors.get(1);}

        }

    }

    void getDataForChart() {
        Log.d(TAG, "StatisticHistoryDaysFragment: getDataForChart");
        // создаем лоадер для чтения данных (работает только с фрагментами)

        int mCountArray =0;

        //создаем массив для графика
        arrayForChart = new ArrayList<BarEntry>();
        //создаем массив для значений замен на XAxis
        datesForChart = new String [mCursor.getCount()];
        //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
        SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatterOut = new SimpleDateFormat("MMM-dd", Locale.ENGLISH);

        //перебор всех записей в курсоре
        while (mCursor.moveToNext()) {
            String strDate = mCursor.getString(1);
            int strCountSession = mCursor.getInt(2);
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
        mCursor.close();
        db.close();
    }

}
