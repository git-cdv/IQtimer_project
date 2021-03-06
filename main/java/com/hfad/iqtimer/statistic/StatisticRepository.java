package com.hfad.iqtimer.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.data.BarEntry;
import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.database.AppDatabase;
import com.hfad.iqtimer.database.Session;
import com.hfad.iqtimer.database.SessionDao;

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
import java.util.concurrent.ExecutorService;

import static android.content.Context.MODE_PRIVATE;

interface ObzorCallback {
    void onComplete(int[] result);
}

interface DaysCallback {
    void onComplete(ArrayList<BarEntry> barEntries ,String [] dates,int planDefault);
}

interface MonthCallback {
    void onComplete(ArrayList<BarEntry> barEntries ,ArrayList<String> dates);
}

public class StatisticRepository {
    private static final String KEY_PREF_COUNT = "prefcount";
    private static final String KEY_PREF_PLAN = "set_plan_day";

    Context context;
    ExecutorService executor;
    AppDatabase db;
    SessionDao sesDao;

    public StatisticRepository(Context context) {
        this.context = context;
        this.executor = App.getInstance().getExecutor();
        this.db = App.getInstance().getDatabase();
        this.sesDao = db.sessionDao();

    }

    public void getDataObzor(final ObzorCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isCountWeek =true;
                boolean isCountMonth =true;
                int mCountWeek =0;
                int mCountMonth = 0;
                int mCountTotal = 0;

                List<Session> sessionList = sesDao.getAll();

                SharedPreferences mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
                int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);

                //перебираем все строки в sessionList
                for (Session item : sessionList) {
                    int strCountSession = item.count;
                    String strCountDate = item.date;

                    LocalDate mDate = LocalDate.parse(strCountDate);

                    if (isCountWeek){
                        mCountWeek = mCountWeek+strCountSession;
                        if (mDate.getDayOfWeek() == DateTimeConstants.MONDAY){
                            isCountWeek =false;
                        }
                    }

                    if (isCountMonth){
                        mCountMonth = mCountMonth+strCountSession;
                        if (mDate.getDayOfMonth() == 1){
                            isCountMonth =false;
                        }
                    }

                    //считаем сумму сессий Total
                    mCountTotal = mCountTotal+strCountSession;

                    mCountWeek = mCountWeek+mPrefCount;
                    mCountMonth = mCountMonth+mPrefCount;
                    mCountTotal = mCountTotal+mPrefCount;
                }
                int [] array = {mPrefCount,mCountWeek,mCountMonth,mCountTotal};

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                callback.onComplete(array);
            }
        });
    }

    public void getDataDays(DaysCallback daysCallback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                int mCountArray =0;

                Cursor mCursorForHistory = sesDao.getHistoryCursor();

                SharedPreferences mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
                int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);

                //получаем доступ к файлу с настройками приложения
                SharedPreferences sPrefSettings = PreferenceManager.getDefaultSharedPreferences(context);
                //вытаскиваем дефолтную значение плана из настроек и присваем переменной
                int mPlanDefault = Integer.valueOf(sPrefSettings.getString(KEY_PREF_PLAN, "5"));

                //создаем массив для графика
                ArrayList<BarEntry> arrayForChartDay = new ArrayList<>();
                //создаем массив для значений замен на XAxis
                String[] datesForChartDay = new String [mCursorForHistory.getCount()+1];
                //Создаем новый объект SimpleDateFormat с шаблоном, который совпадает с тем, что у нас в строке (иначе распарсить не получится)
                SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat formatterOut = new SimpleDateFormat("MMMdd", Locale.getDefault());

                //перебор всех записей в курсоре
                while (mCursorForHistory.moveToNext()) {
                    String strDate = mCursorForHistory.getString(1);
                    int strCountSession = mCursorForHistory.getInt(2);
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
                if (mCursorForHistory.getCount()==0) {//если 0-вой вход
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

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                daysCallback.onComplete(arrayForChartDay,datesForChartDay,mPlanDefault);
            }
        });

    }
    public void getDataMonth(MonthCallback monthCallback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                int mCountArray =0;
                int mCheck=0;
                int mCountMonth=0;
                int mCurrentMonth=0;
                int mMonth=0;

                Cursor sCursorForHistory = sesDao.getHistoryCursor();

                SharedPreferences mPref = context.getSharedPreferences("prefcount", MODE_PRIVATE);
                int mPrefCount = mPref.getInt(KEY_PREF_COUNT, 0);

                //создаем массив для графика
                ArrayList<BarEntry> arrayForChartMonth = new ArrayList<BarEntry>();
                //создаем массив для значений замен на XAxis
                ArrayList<String> datesForChartMonth = new ArrayList<String>();;
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

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                monthCallback.onComplete(arrayForChartMonth,datesForChartMonth);
            }
        });
    }
}

