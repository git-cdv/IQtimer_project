package com.chkan.iqtimer.progress;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class ProgressViewModel extends AndroidViewModel {

    private static final String TAG = "MYLOGS";

    private static final int STATE_GOAL_ACTIVE = 1;
    private static final int STATE_GOAL_DONE = 2;
    private static final int STATE_DAYS_ENDED = -1;
    private static final int STATE_GOAL_OFF = 0;

    public ProgressViewModel(@NonNull Application application) {
        super(application);
    }

    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> desc = new ObservableField<>();
    public final ObservableField<String> q_current = new ObservableField<>();
    public final ObservableField<String> q_plan = new ObservableField<>();
    public final ObservableField<String> days_current = new ObservableField<>();
    public final ObservableField<String> days_plan = new ObservableField<>();
    public final ObservableBoolean isPutAdd = new ObservableBoolean();
    public final ObservableBoolean isViewDone = new ObservableBoolean();
    public final ObservableBoolean isPremium = new ObservableBoolean();
    public final ObservableField<String> textGoalDone = new ObservableField<>();
    public final ObservableField<Integer> counter = new ObservableField<>();

    public final MutableLiveData<Integer> entuziast_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> entuziast_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> entuziast_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> voin_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> voin_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> voin_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> boss_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> boss_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> boss_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> hero_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> hero_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> hero_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> pokoritel_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> pokoritel_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> pokoritel_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> legenda_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> legenda_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> legenda_l = new MutableLiveData<>();
    public final MutableLiveData<Integer> winner_c = new MutableLiveData<>();
    public final MutableLiveData<Integer> winner_p = new MutableLiveData<>();
    public final MutableLiveData<Integer> winner_l = new MutableLiveData<>();

    final GoalRepository repo = new GoalRepository(getApplication());
    final ProgressRepository repoProgress = new ProgressRepository();

    void getStateGoal(){
        Log.d(TAG, "ProgressViewModel: getState");
        switch (repo.getStateGoal()) {
            case STATE_GOAL_OFF:
                setStateEmpty();
                break;
            case STATE_GOAL_ACTIVE:
                setGoal(repo.readGoal());
                isPutAdd.set(true);
                break;
            case STATE_GOAL_DONE:
                setGoal(repo.readGoal());
                isPutAdd.set(false);
                isViewDone.set(true);
                textGoalDone.set(repo.getTextGoalDone(STATE_GOAL_DONE));
                break;
            case STATE_DAYS_ENDED:
                setGoal(repo.readGoal());
                isPutAdd.set(false);
                isViewDone.set(true);
                textGoalDone.set(repo.getTextGoalDone(STATE_DAYS_ENDED));
                break;
        }
    }

    public void setGoal(String [] dataGoal){
        name.set(dataGoal[0]);
        desc.set(dataGoal[1]);
        q_current.set(dataGoal[2]);
        q_plan.set(dataGoal[3]);
        days_current.set(dataGoal[4]);
        days_plan.set(dataGoal[5]);
    }

    public void createNewGoalSes(String Name,String Desc,String Q_plan,String Days_plan){
        name.set(Name);
        desc.set(Desc);
        days_plan.set(Days_plan);
        q_current.set(String.valueOf(repo.getCurrentCount()));
        q_plan.set(Q_plan);
        days_current.set("1");
        isViewDone.set(false);
        repo.createNewGoalSes(Name,Desc,Q_plan,Days_plan);

//ДЕРНУТЬ БАИНДИНГ ДЛЯ ПЕРЕРИСОВКИ ПРОГОРЕСС БАРА

    }
    public void createNewGoalPower(String Name,String Desc,String Q_plan,String Days_plan){
        name.set(Name);
        desc.set(Desc);
        days_plan.set(Days_plan);
        String mCurrentPower = repo.createNewGoalPower(Name,Desc,Q_plan,Days_plan);
        q_plan.set(Q_plan);
        q_current.set(mCurrentPower);
        days_current.set(mCurrentPower);
        isViewDone.set(false);
    }

    public void deleteGoal(){
        setStateEmpty();
        repo.deleteGoal();
    }

    private void setStateEmpty(){
        setGoal(repo.emptyGoal());
        isPutAdd.set(false);
    }

    public void getStateP() {
        //проверяем отображать замок или нет
        isPremium.set(repo.isPremium());

        MutableLiveData[][] liveDataArray = {{entuziast_l, entuziast_c, entuziast_p}, {voin_l, voin_c, voin_p}, {boss_l, boss_c, boss_p}, {hero_l, hero_c, hero_p}, {pokoritel_l, pokoritel_c, pokoritel_p}, {legenda_l, legenda_c, legenda_p}, {winner_l, winner_c, winner_p}};
        int[][] stateArray;

        if (isPremium.get()) {
            //получаем данные по Достижениям и назначаем их LiveData
            stateArray = repoProgress.getStateP(); //{entuziastArray,voinArray,bossArray,heroArray,pokoritelArray,legendaArray,winnerArray};
        } else {
            stateArray = new int[][]{{0,0,5},{0,0,2},{0,0,2},{0,0,2},{0,0,5},{0,0,3},{0,0,6}};
        }

        for (int i = 0; i < 7; i++) {  //идём по строкам - количество Достижений
        for (int j = 0; j < 3; j++) {//идём по столбцам - количество параметров состояния Достижений
            //назначение соответствующим LiveData параметров состояния
            liveDataArray[i][j].setValue(stateArray[i][j]);
        }
        }
    }

    public void getCounter() {
        counter.set(repoProgress.getCurrentCounter());
    }
}
