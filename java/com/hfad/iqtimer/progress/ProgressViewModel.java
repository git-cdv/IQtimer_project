package com.hfad.iqtimer.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class ProgressViewModel extends AndroidViewModel {

    private static final int STATE_GOAL_ACTIVE = 1;
    private static final int STATE_GOAL_DONE = 2;
    private static final int STATE_DAYS_ENDED = -1;
    private static final int STATE_GOAL_OFF = 0;

    public ProgressViewModel(@NonNull @org.jetbrains.annotations.NotNull Application application) {
        super(application);
    }


    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> desc = new MutableLiveData<>();
    public MutableLiveData<String> q_current = new MutableLiveData<>();
    public MutableLiveData<String> q_plan = new MutableLiveData<>();
    public MutableLiveData<String> days_current = new MutableLiveData<>();
    public MutableLiveData<String> days_plan = new MutableLiveData<>();
    public MutableLiveData<Boolean> isPutAdd = new MutableLiveData<>();
    public MutableLiveData<Boolean> isViewDone = new MutableLiveData<>();
    public MutableLiveData<String> textGoalDone = new MutableLiveData<>();

    public MutableLiveData<Integer> entuziast_progress = new MutableLiveData<>();
    public MutableLiveData<Integer> entuziast_max = new MutableLiveData<>();
    public MutableLiveData<Integer> voin_progress = new MutableLiveData<>();
    public MutableLiveData<Integer> voin_max = new MutableLiveData<>();
    public MutableLiveData<Integer> boss_progress = new MutableLiveData<>();
    public MutableLiveData<Integer> boss_max = new MutableLiveData<>();

    ProgressRepository repository = new ProgressRepository(getApplication());

    public void setName(String value) {
        this.name.setValue(value);
    }
    public void setDesc(String value) {
        this.desc.setValue(value);
    }
    public void setQ_current(String value) {
        this.q_current.setValue(value);
    }
    public void setQ_plan(String value) {
        this.q_plan.setValue(value);
    }
    public void setDays_current(String value) {
        this.days_current.setValue(value);
    }
    public void setDays_plan(String value) {
        this.days_plan.setValue(value);
    }
    public void isPutAdd(Boolean value) {
        this.isPutAdd.setValue(value);
    }
    public void isViewDone(Boolean value) {
        this.isViewDone.setValue(value);
    }



    void getState(){
        switch (repository.getStateGoal()) {
            case STATE_GOAL_OFF:
                setStateEmpty();
                break;
            case STATE_GOAL_ACTIVE:
                setGoal(repository.readGoal());
                isPutAdd(true);
                ///ЗАГЛУШКА
                entuziast_progress.setValue(2);
                entuziast_max.setValue(5);
                voin_progress.setValue(0);
                voin_max.setValue(1);
                boss_progress.setValue(4);
                boss_max.setValue(5);
                ///ЗАГЛУШКА
                break;
            case STATE_GOAL_DONE:
                setGoal(repository.readGoal());
                isPutAdd(false);
                isViewDone(true);
                textGoalDone.setValue(repository.getTextGoalDone(STATE_GOAL_DONE));
                break;
            case STATE_DAYS_ENDED:
                setGoal(repository.readGoal());
                isPutAdd(false);
                isViewDone(true);
                textGoalDone.setValue(repository.getTextGoalDone(STATE_DAYS_ENDED));
                break;
        }
    }

    public void setGoal(String [] dataGoal){
        setName(dataGoal[0]);
        setDesc(dataGoal[1]);
        setQ_current(dataGoal[2]);
        setQ_plan(dataGoal[3]);
        setDays_current(dataGoal[4]);
        setDays_plan(dataGoal[5]);
    }

    public void createNewGoalSes(String name,String desc,String q_plan,String days_plan){
        setName(name);
        setDesc(desc);
        setDays_plan(days_plan);
        setQ_current(String.valueOf(repository.getCurrentCount()));
        setQ_plan(q_plan);
        setDays_current("1");
        isViewDone(false);
        repository.createNewGoalSes(name,desc,q_plan,days_plan);

//ДЕРНУТЬ БАИНДИНГ ДЛЯ ПЕРЕРИСОВКИ ПРОГОРЕСС БАРА

    }
    public void createNewGoalPower(String name,String desc,String q_plan,String days_plan){
        setName(name);
        setDesc(desc);
        setDays_plan(days_plan);
        String mCurrentPower = repository.createNewGoalPower(name,desc,q_plan,days_plan);
        setQ_plan(q_plan);
        setQ_current(mCurrentPower);
        setDays_current(mCurrentPower);
        isViewDone(false);
    }

    public void deleteGoal(){
        setStateEmpty();
        repository.deleteGoal();
    }

    private void setStateEmpty(){
        setGoal(repository.emptyGoal());
        isPutAdd(false);
    }
}
