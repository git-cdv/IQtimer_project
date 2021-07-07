package com.hfad.iqtimer.progress;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.dialogs.DialogOnLock;
import com.hfad.iqtimer.dialogs.DialogProgressDeleteGoal;


public class ProgressActivity extends AppCompatActivity {

    DialogFragmentGoal dlgSetGoal;
    DialogProgressDeleteGoal dlgDelGoal;
    DialogOnLock dlgOnLock;
    NavController navController;
    ProgressViewModel mViewmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mViewmodel = new ViewModelProvider(this).get(ProgressViewModel.class);
}


    public void toClickProgress(View v) {
        switch(v.getId()) {
            case R.id.img_btn_add_goal:
            case R.id.btnNewGoal:
                dlgSetGoal = new DialogFragmentGoal();
                dlgSetGoal.show(getSupportFragmentManager(), "dlgSetGoal");
                mViewmodel.isPutAdd.set(true);
                break;
            case R.id.img_btn_cancel_goal:
                dlgDelGoal= new DialogProgressDeleteGoal();
                dlgDelGoal.show(getSupportFragmentManager(), "dlgDelGoal");
                mViewmodel.isPutAdd.set(false);
                break;
            case R.id.BtnMore:
                toProgressList();
                break;
            case R.id.img_btn_lock:
                dlgOnLock = new DialogOnLock();
                dlgOnLock.show(getSupportFragmentManager(), "dlgOnLock");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }

    }

public void toProgressList(){
    navController.navigate(R.id.action_progressFragment_to_listProgressFragment);
}
}