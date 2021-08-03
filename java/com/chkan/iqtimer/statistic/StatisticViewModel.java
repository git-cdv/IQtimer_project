package com.chkan.iqtimer.statistic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class StatisticViewModel extends AndroidViewModel {

    final StatisticRepository repo;

    public StatisticViewModel(@NonNull Application application) {
        super(application);
        repo = new StatisticRepository(getApplication());
        setDataObzor();
        setDataDays();
        setDataMonth();
    }

    public MutableLiveData<Integer> getCountToday() {
        return countToday;
    }
    public MutableLiveData<Integer> getCountWeek() {
        return countWeek;
    }
    public MutableLiveData<Integer> getCountMonth() {
        return countMonth;
    }
    public MutableLiveData<Integer> getCountTotal() {
        return countTotal;
    }
    public MutableLiveData<String[]> getDaysDates() { return daysDates; }
    public MutableLiveData<ArrayList<BarEntry>> getBarDaysEntries() {return barDaysEntries;}
    public MutableLiveData<ArrayList<String>> getMonthDates() {return monthDates;}
    public MutableLiveData<ArrayList<BarEntry>> getBarMonthEntries() {return barMonthEntries;}
    public MutableLiveData<Integer> getPlanDefault() {return mPlanDefault;}

    private final MutableLiveData<Integer> countToday = new MutableLiveData<>();
    private final MutableLiveData<Integer> countWeek = new MutableLiveData<>();
    private final MutableLiveData<Integer> countMonth = new MutableLiveData<>();
    private final MutableLiveData<Integer> countTotal = new MutableLiveData<>();
    private final MutableLiveData<Integer> mPlanDefault = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BarEntry>> barDaysEntries = new MutableLiveData<>();
    private final MutableLiveData<String []> daysDates = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BarEntry>> barMonthEntries = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> monthDates = new MutableLiveData<>();
    public final ObservableBoolean isDataObzorDone = new ObservableBoolean();
    public final ObservableBoolean isDataDaysDone = new ObservableBoolean();
    public final ObservableBoolean isDataMonthDone = new ObservableBoolean();


    //результат приходит в рабочем фоне, поэтому используем LiveData и .postValue() для передачи в Главный поток
    public void setDataObzor() {
        isDataObzorDone.set(false);
        repo.getDataObzor(result -> {
            countToday.postValue(result[0]);
            countWeek.postValue(result[1]);
            countMonth.postValue(result[2]);
            countTotal.postValue(result[3]);
            isDataObzorDone.set(true);
        });
    }

    public void setDataDays() {
        isDataDaysDone.set(false);
        repo.getDataDays((barEntries, dates, planDefault) -> {
            barDaysEntries.postValue(barEntries);
            mPlanDefault.postValue(planDefault);
            daysDates.postValue(dates);
        });
    }

    public void setDataMonth() {
        isDataMonthDone.set(false);
        repo.getDataMonth((barEntries, dates) -> {
            barMonthEntries.postValue(barEntries);
            monthDates.postValue(dates);
        });
    }

}
