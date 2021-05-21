package com.hfad.iqtimer.progress;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Set;


public class ProgressViewModel extends AndroidViewModel {

    private static final String TAG = "MYLOGS";

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

    public MutableLiveData<Integer> entuziast_c = new MutableLiveData<>();
    public MutableLiveData<Integer> entuziast_p = new MutableLiveData<>();
    public MutableLiveData<Integer> entuziast_l = new MutableLiveData<>();
    public MutableLiveData<Integer> voin_c = new MutableLiveData<>();
    public MutableLiveData<Integer> voin_p = new MutableLiveData<>();
    public MutableLiveData<Integer> voin_l = new MutableLiveData<>();
    public MutableLiveData<Integer> boss_c = new MutableLiveData<>();
    public MutableLiveData<Integer> boss_p = new MutableLiveData<>();
    public MutableLiveData<Integer> boss_l = new MutableLiveData<>();
    public MutableLiveData<Integer> hero_c = new MutableLiveData<>();
    public MutableLiveData<Integer> hero_p = new MutableLiveData<>();
    public MutableLiveData<Integer> hero_l = new MutableLiveData<>();
    public MutableLiveData<Integer> pokoritel_c = new MutableLiveData<>();
    public MutableLiveData<Integer> pokoritel_p = new MutableLiveData<>();
    public MutableLiveData<Integer> pokoritel_l = new MutableLiveData<>();
    public MutableLiveData<Integer> legenda_c = new MutableLiveData<>();
    public MutableLiveData<Integer> legenda_p = new MutableLiveData<>();
    public MutableLiveData<Integer> legenda_l = new MutableLiveData<>();
    public MutableLiveData<Integer> winner_c = new MutableLiveData<>();
    public MutableLiveData<Integer> winner_p = new MutableLiveData<>();
    public MutableLiveData<Integer> winner_l = new MutableLiveData<>();

    GoalRepository repo = new GoalRepository(getApplication());
    ProgressRepository repoProgress = new ProgressRepository(getApplication());

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



    void getStateGoal(){
        Log.d(TAG, "ProgressViewModel: getState");
        switch (repo.getStateGoal()) {
            case STATE_GOAL_OFF:
                setStateEmpty();
                break;
            case STATE_GOAL_ACTIVE:
                setGoal(repo.readGoal());
                isPutAdd(true);
                break;
            case STATE_GOAL_DONE:
                setGoal(repo.readGoal());
                isPutAdd(false);
                isViewDone(true);
                textGoalDone.setValue(repo.getTextGoalDone(STATE_GOAL_DONE));
                break;
            case STATE_DAYS_ENDED:
                setGoal(repo.readGoal());
                isPutAdd(false);
                isViewDone(true);
                textGoalDone.setValue(repo.getTextGoalDone(STATE_DAYS_ENDED));
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
        setQ_current(String.valueOf(repo.getCurrentCount()));
        setQ_plan(q_plan);
        setDays_current("1");
        isViewDone(false);
        repo.createNewGoalSes(name,desc,q_plan,days_plan);

//ДЕРНУТЬ БАИНДИНГ ДЛЯ ПЕРЕРИСОВКИ ПРОГОРЕСС БАРА

    }
    public void createNewGoalPower(String name,String desc,String q_plan,String days_plan){
        setName(name);
        setDesc(desc);
        setDays_plan(days_plan);
        String mCurrentPower = repo.createNewGoalPower(name,desc,q_plan,days_plan);
        setQ_plan(q_plan);
        setQ_current(mCurrentPower);
        setDays_current(mCurrentPower);
        isViewDone(false);
    }

    public void deleteGoal(){
        setStateEmpty();
        repo.deleteGoal();
    }

    private void setStateEmpty(){
        setGoal(repo.emptyGoal());
        isPutAdd(false);
    }

    public boolean isPremium() {
        return repo.isPremium();
    }

    public void getStateP() {
     //получаем данные по Достижениям и назначаем их LiveData
        MutableLiveData[][] liveDataArray = {{entuziast_l,entuziast_c,entuziast_p}, {voin_l,voin_c,voin_p}, {boss_l,boss_c,boss_p}, {hero_l,hero_c,hero_p}, {pokoritel_l,pokoritel_c,pokoritel_p}, {legenda_l,legenda_c,legenda_p}, {winner_l,winner_c,winner_p}};

        int[][] stateArray = repoProgress.getStateP(); //{entuziastArray,voinArray,bossArray,heroArray,pokoritelArray,legendaArray,winnerArray};

        for (int i = 0; i < 7; i++) {  //идём по строкам - количество Достижений
            for (int j = 0; j < 3; j++) {//идём по столбцам - количество параметров состояния Достижений
                //назначение соответствующим LiveData параметров состояния
                liveDataArray[i][j].setValue(stateArray[i][j]);
            }
        }
    }
}
