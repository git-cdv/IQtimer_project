package com.hfad.iqtimer.progress;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hfad.iqtimer.R;

public class ProgressFragment extends Fragment implements View.OnClickListener {
    Button btnSetGoal;
    DialogFragmentGoal dlgSetGoal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_progress, null);
        btnSetGoal = (Button)v.findViewById(R.id.btn_set_goal);
        btnSetGoal.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        dlgSetGoal = new DialogFragmentGoal();
        dlgSetGoal.show(getParentFragmentManager(),"dlgSetGoal");
    }
}