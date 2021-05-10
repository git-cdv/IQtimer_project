package com.hfad.iqtimer.progress;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProgressViewModel extends ViewModel {
    private final MutableLiveData<String> goalName = new MutableLiveData<String>();
    private final MutableLiveData<String> goalDisc = new MutableLiveData<String>();
    private final MutableLiveData<String> goalQNum = new MutableLiveData<String>();
    private final MutableLiveData<Integer> goalQName = new MutableLiveData<Integer>();
    private final MutableLiveData<String> goalPeriodNum = new MutableLiveData<String>();

    public void setGoalData (String name,String disc,String QNum,Integer QName,String PeriodNum){
        goalDisc.setValue(disc);
        goalName.setValue(name);
        goalQNum.setValue(QNum);
        goalQName.setValue(QName);
        goalPeriodNum.setValue(PeriodNum);
    }

    public LiveData<String> getName() {
        return goalName;
    }

    public LiveData<String> getDisc() {
        return goalDisc;
    }

    public LiveData<String> getQNum() {
        return goalQNum;
    }

    public LiveData<Integer> getQName() {
        return goalQName;
    }

    public LiveData<String> getPeriodNum() {
        return goalPeriodNum;
    }

}
