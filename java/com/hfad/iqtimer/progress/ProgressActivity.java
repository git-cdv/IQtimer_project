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

        if(v.getId()==R.id.img_btn_add_goal||v.getId()==R.id.btnNewGoal){
            dlgSetGoal = new DialogFragmentGoal();
            dlgSetGoal.show(getSupportFragmentManager(), "dlgSetGoal");
            mViewmodel.isPutAdd.set(true);
        } else if (v.getId()==R.id.img_btn_cancel_goal){
            dlgDelGoal= new DialogProgressDeleteGoal();
            dlgDelGoal.show(getSupportFragmentManager(), "dlgDelGoal");
            mViewmodel.isPutAdd.set(false);
        } else if (v.getId()==R.id.BtnMore){
            toProgressList();
        } else if (v.getId()==R.id.img_btn_lock){
            dlgOnLock = new DialogOnLock();
            dlgOnLock.show(getSupportFragmentManager(), "dlgOnLock");
        }
    }

public void toProgressList(){
    navController.navigate(R.id.action_progressFragment_to_listProgressFragment);
}
}