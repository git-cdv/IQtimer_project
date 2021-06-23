package com.hfad.iqtimer.statistic;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

public class StatisticViewModel extends AndroidViewModel {
    public StatisticViewModel(@NonNull Application application) {
        super(application);
    }

    public ObservableField<Integer> countToday = new ObservableField<>();
    public ObservableField<Integer> countWeek = new ObservableField<>();
    public ObservableField<Integer> countMonth = new ObservableField<>();
    public ObservableField<Integer> countTotal = new ObservableField<>();

    StatisticRepository repo = new StatisticRepository(getApplication());

    public void setDataObzor() {
        repo.getDataObzor();
    }
}
