package com.hfad.iqtimer.progress;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.databinding.FragmentProgressBinding;
import com.hfad.iqtimer.dialogs.DialogProgressDeleteGoal;


public class ProgressFragment extends Fragment implements View.OnClickListener {

    DialogFragmentGoal dlgSetGoal;
    DialogProgressDeleteGoal dlgDelGoal;
    FragmentProgressBinding binding;
    ProgressViewModel mViewmodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mViewmodel.getState();//заполняем вью в зависимости от активности цели
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress,container,false);
        View v = binding.getRoot();

        (v.findViewById(R.id.btnNewGoal)).setOnClickListener(this);
        (v.findViewById(R.id.img_btn_add_goal)).setOnClickListener(this);
        (v.findViewById(R.id.img_btn_cancel_goal)).setOnClickListener(this);

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.img_btn_add_goal:
            case R.id.btnNewGoal:
                dlgSetGoal = new DialogFragmentGoal();
                dlgSetGoal.show(getParentFragmentManager(), "dlgSetGoal");
                mViewmodel.isPutAdd(true);
                break;
            case R.id.img_btn_cancel_goal:
                dlgDelGoal= new DialogProgressDeleteGoal();
                dlgDelGoal.show(getParentFragmentManager(), "dlgDelGoal");
                mViewmodel.isPutAdd(false);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }

    }

}