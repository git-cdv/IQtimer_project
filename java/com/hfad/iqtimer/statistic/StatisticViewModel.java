package com.hfad.iqtimer.statistic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class StatisticViewModel extends AndroidViewModel {

    public StatisticViewModel(@NonNull Application application) {super(application);}

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

    private final MutableLiveData<Integer> countToday = new MutableLiveData<>();
    private final MutableLiveData<Integer> countWeek = new MutableLiveData<>();
    private final MutableLiveData<Integer> countMonth = new MutableLiveData<>();
    private final MutableLiveData<Integer> countTotal = new MutableLiveData<>();

    StatisticRepository repo = new StatisticRepository(getApplication());

    //результат приходит в рабочем фоне, поэтому используем LiveData и .postValue() для передачи в Главный поток
    public void setDataObzor() {
        repo.getDataObzor(new RepositoryCallback() {
            @Override
            public void onComplete(int[] result) {
                countToday.postValue(result[0]);
                countWeek.postValue(result[1]);
                countMonth.postValue(result[2]);
                countTotal.postValue(result[3]);
            }
        });
    }
}
