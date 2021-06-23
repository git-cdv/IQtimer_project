package com.hfad.iqtimer.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;
import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.database.AppDatabase;
import com.hfad.iqtimer.database.Session;
import com.hfad.iqtimer.database.SessionDao;
import com.hfad.iqtimer.database.SessionDatabaseHelper;
import com.hfad.iqtimer.databinding.FragmentProgressBinding;
import com.hfad.iqtimer.databinding.FragmentStatisticBinding;
import com.hfad.iqtimer.progress.ProgressViewModel;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class StatisticFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    SQLiteDatabase db;
    SessionDatabaseHelper DatabaseHelper;
    StatisticViewModel mViewmodel;
    FragmentStatisticBinding binding;
    static Cursor sCursorForHistory;
    int mPrefCount;
    int mPlanDefault;
    BarChart mBarChartDay,mBarChartMonth;
    ArrayList<BarEntry> arrayForChartDay;
    String[] datesForChartDay;
    ArrayList<BarEntry> arrayForChartMonth;
    ArrayList<String> datesForChartMonth;
    boolean isNeedLoad = true;
    int mColorOnPrimary;

    private static final String TAG = "MYLOGS";
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_PLAN = "set_plan_day";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mPref = getContext().getSharedPreferences("prefcount", MODE_PRIVATE);
        mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);

        mViewmodel = new ViewModelProvider(requireActivity()).get(StatisticViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "StatisticFragment: onCreateView");
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_statistic,container,false);
        View v = binding.getRoot();

        //получаем ссылку на БД
        DatabaseHelper = new SessionDatabaseHelper(getActivity());
        db = DatabaseHelper.getReadableDatabase();//разрешаем чтение
        mColorOnPrimary = MaterialColors.getColor(requireContext(),R.attr.colorOnPrimary,Color.GRAY);

        if(savedInstanceState == null){
            Log.d(TAG, "savedInstanceState == null");

            mViewmodel.setDataObzor();

        // создаем лоадер для Истории
        LoaderManager.getInstance(this).initLoader(1, null, this);
        }

        //получаем доступ к файлу с настройками приложения
        SharedPreferences sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //вытаскиваем дефолтную значение плана из настроек и присваем переменной
        mPlanDefault = Integer.valueOf(sPrefSettings.getString(KEY_PREF_PLAN, "5"));
        mBarChartDay = (BarChart) v.findViewById(R.id.history_chart_days);
        mBarChartMonth = (BarChart) v.findViewById(R.id.history_chart_month);

        if(savedInstanceState != null){//проверяем что это после переворота
            Log.d(TAG, "StatisticFragment: savedInstanceState != null");
            arrayForChartDay=savedInstanceState.getParcelableArrayList("arrayForChartDay");
            datesForChartDay=savedInstanceState.getStringArray("datesForChartDay");
            arrayForChartMonth=savedInstanceState.getParcelableArrayList("arrayForChartMonth");
            datesForChartMonth=savedInstanceState.getStringArrayList("datesForChartMonth");
            isNeedLoad = false;
            setupHistoryChart();

        }

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "StatisticFragment: onSaveInstanceState");
        savedInstanceState.putParcelableArrayList("arrayForChartDay",arrayForChartDay);
        savedInstanceState.putStringArray("datesForChartDay",datesForChartDay);
        savedInstanceState.putParcelableArrayList("arrayForChartMonth",arrayForChartMonth);
        savedInstanceState.putStringArrayList("datesForChartMonth",datesForChartMonth);
    }

    void setupHistoryChart() {
        Log.d(TAG, "StatisticFragment: setupHistoryChart()");

        if(isNeedLoad){getDataForHistoryDay();}

        String stringDescription = getResources().getString(R.string.stat_chart_description);

        //создаем через свой класс, где переопределен метод вывода цвета для бара
        MyBarDataSet barDataSet1 = new MyBarDataSet(arrayForChartDay,stringDescription);
        //назначаем цвета для баров
        barDataSet1.setColors(new int[]{ContextCompat.getColor(requireContext(), R.color.brand_orange), ContextCompat.getColor(requireContext(), R.color.brand_blue_600) });
        barDataSet1.setValueTextColor(mColorOnPrimary);
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return datesForChartDay[(int) value];
            }
        };
        //настройка оси Х (шаг и формат подписей)
        XAxis xAxis = mBarChartDay.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(mColorOnPrimary);


        //добавление "линии тренда" (план) и начала с 0
        YAxis leftAxis = mBarChartDay.getAxisLeft();
        YAxis rightAxis = mBarChartDay.getAxisRight();

        LimitLine ll = new LimitLine(mPlanDefault);
        ll.setLineColor(ContextCompat.getColor(requireContext(), R.color.brand_orange));
        //как пунктир
        ll.enableDashedLine(16f,4f,2f);
        ll.setLineWidth(1f);
        leftAxis.addLimitLine(ll);
        //чтобы начиналось с 0
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(mColorOnPrimary);
        rightAxis.setTextColor(mColorOnPrimary);

        mBarChartDay.setData(barData);
        //устанавливает количество Баров для отображение, если больше - скролится
        mBarChartDay.setVisibleXRangeMaximum(14f);
        //переводит начальный вид графиков в конец
        mBarChartDay.moveViewToX(arrayForChartDay.size());
        //убираем description
        Description description = mBarChartDay.getDescription();
        description.setEnabled(false);
        mBarChartDay.setAutoScaleMinMaxEnabled(true);
        mBarChartDay.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.brand_orange));
        Legend legend = mBarChartDay.getLegend();
        legend.setTextColor(mColorOnPrimary);
        mBarChartDay.invalidate();

        if(isNeedLoad){getDataForHistoryMonth();}

        setupHistoryChartMonth();

    }

    void setupHistoryChartMonth(){
        Log.d(TAG, "StatisticFragment: setupHistoryChartMonth()");

        String stringDescription = getResources().getString(R.string.stat_chart_description_month);

        //создаем через свой класс, где переопределен метод вывода цвета для бара
        BarDataSet barDataSet1 = new BarDataSet(arrayForChartMonth,stringDescription);
        //назначаем цвета для баров
        barDataSet1.setColors(ContextCompat.getColor(requireContext(), R.color.brand_blue_600));
        barDataSet1.setValueTextColor(mColorOnPrimary);
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return datesForChartMonth.get((int) value);
            }
        };

        //настройка оси Х (шаг и формат подписей)
        XAxis xAxis = mBarChartMonth.getXAxis();

        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(mColorOnPrimary);

        //чтобы начиналось с 0
        YAxis leftAxisM = mBarChartMonth.getAxisLeft();
        YAxis rightAxisM = mBarChartMonth.getAxisRight();
        leftAxisM.setAxisMinimum(0f);
        rightAxisM.setAxisMinimum(0f);
        leftAxisM.setTextColor(mColorOnPrimary);
        rightAxisM.setTextColor(mColorOnPrimary);

        mBarChartMonth.setData(barData);
        //устанавливает количество Баров для отображение, если больше - скролится
        mBarChartMonth.setVisibleXRangeMaximum(12f);
        //переводит начальный вид графиков в конец
        mBarChartMonth.moveViewToX(arrayForChartMonth.size());
        //убираем description
        Description description = mBarChartMonth.getDescription();
        description.setEnabled(false);
        Legend legend = mBarChartMonth.getLegend();
        legend.setTextColor(mColorOnPrimary);
        mBarChartMonth.invalidate();
    }

    void getDataForHistoryDay(){

        Log.d(TAG, "StatisticFragment: getDataForHistoryDay");

        int mCountArray =0;

        //создаем массив для графика
        arrayForChartDay = new ArrayList<>();
        //создаем массив для значений замен на XAxis
        datesForChartDay = new String [sCursorForHistory.getCount()+1];
        //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
        SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatterOut = new SimpleDateFormat("MMMdd", Locale.getDefault());

        //перебор всех записей в курсоре
        while (sCursorForHistory.moveToNext()) {
            String strDate = sCursorForHistory.getString(1);
            int strCountSession = sCursorForHistory.getInt(2);
            Date date=null;

            try {
                //Создаём дату с помощью форматтера, который в свою очередь парсит её из входной строки
                date = formatterIn.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Добавляем индекс и счетчик в массив
            arrayForChartDay.add(new BarEntry(mCountArray,strCountSession));

            //Добавляем дату в формате formatterOut в массив для замен на XAxis
            datesForChartDay[mCountArray] = formatterOut.format(date);
            mCountArray++;

        }
        if (sCursorForHistory.getCount()==0) {//если 0-вой вход
            //Добавляем сегоднешнюю дату
            datesForChartDay[0] = formatterOut.format(new Date());
            //Добавляем индекс и счетчик в массив с сегоднешним значением
            arrayForChartDay.add(new BarEntry(0, mPrefCount));
        }else {
             //Добавляем сегоднешнюю дату
            datesForChartDay[mCountArray] = formatterOut.format(new Date());
            //Добавляем индекс и счетчик в массив с сегоднешним значением
            arrayForChartDay.add(new BarEntry(mCountArray, mPrefCount));
        }

    }

    void getDataForHistoryMonth(){

        Log.d(TAG, "StatisticFragment: getDataForHistoryMonth");
        // создаем лоадер для чтения данных (работает только с фрагментами)

        int mCountArray =0;
        int mCheck=0;
        int mCountMonth=0;
        int mCurrentMonth=0;
        int mMonth=0;


        //создаем массив для графика
        arrayForChartMonth = new ArrayList<BarEntry>();
        //создаем массив для значений замен на XAxis
        datesForChartMonth = new ArrayList<String>();;
        //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
        DateTimeFormatter fmtOut = DateTimeFormat.forPattern("MMM");

        //перемещение на 0 позицию
        sCursorForHistory.moveToPosition(-1);

        //перебор всех записей в курсоре
        while (sCursorForHistory.moveToNext()) {
            String strDate = sCursorForHistory.getString(1);
            int strCountSession = sCursorForHistory.getInt(2);

            //получаем текущую дату с курсора
            LocalDate mDateFromCursor = LocalDate.parse(strDate);

            if(mCheck==0){//проверяем что это первый проход
                mMonth=mDateFromCursor.getMonthOfYear();
                mCheck=1;
            }
            mCurrentMonth =mDateFromCursor.getMonthOfYear();

            if(mMonth==mCurrentMonth){

                mCountMonth = strCountSession+mCountMonth;
            } else {
                //Добавляем индекс и счетчик в массив
                arrayForChartMonth.add(new BarEntry(mCountArray,mCountMonth));

                String strOutputDateTime = fmtOut.print(mDateFromCursor.minusMonths(1));
                //Добавляем месяц в формате fmtOut в массив для замен на XAxis
                datesForChartMonth.add(strOutputDateTime);
                mCountArray++;
                mCheck=0;
                mCountMonth = strCountSession;
            }
            if(sCursorForHistory.isLast()){
                //Добавляем индекс и счетчик в массив
                arrayForChartMonth.add(new BarEntry(mCountArray,mCountMonth+mPrefCount));

                String strOutputDateTime = fmtOut.print(mDateFromCursor);
                //Добавляем месяц в формате fmtOut в массив для замен на XAxis
                datesForChartMonth.add(strOutputDateTime);
            }
        }
        if (sCursorForHistory.getCount()==0) {//если 0-вой вход
            //Добавляем сегоднешнюю дату
            datesForChartMonth.add(fmtOut.print(LocalDate.now()));
            //Добавляем индекс и счетчик в массив
            arrayForChartMonth.add(new BarEntry(0,mPrefCount));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "StatisticFragment: onStop + destroyLoader");
        //убиваю лоадер, потому что он перезапускается после onStop()
        LoaderManager.getInstance(this).destroyLoader(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "StatisticFragment: onDestroy");
        sCursorForHistory.close();
        db.close();
    }

    @NonNull
    @Override
    //создаем Loader и даем ему на вход объект для работы с БД
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "StatisticFragment: onCreateLoader");
         return new MyLoaderForHistory(getActivity(), db);
    }

    @Override
    //мы получаем результат работы лоадера – новый курсор с данными. Этот курсор мы отдаем адаптеру методом swapCursor.
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "StatisticFragment: onLoadFinished");
            sCursorForHistory = data;
            setupHistoryChart();//запускаем загрузку графика, как готов курсор
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "StatisticFragment: onLoadReset");

    }

    //наш лоадер, наследник класса CursorLoader. У него мы переопределяем метод loadInBackground, в котором просто получаем курсор с данными БД
    static class MyLoaderForHistory extends CursorLoader {
        SQLiteDatabase db;

        public MyLoaderForHistory(Context context, SQLiteDatabase db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "StatisticFragment: MyLoaderForHistory loadInBackground");
            //Курсор возвращает значения "_id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
            //с сортировкой по убыванию ИД
            sCursorForHistory = db.query("SESSIONS",
                    new String[]{"_id","DATE", "SESSION_COUNT"},
                    null, null, null, null, null);
            return sCursorForHistory;
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
}
