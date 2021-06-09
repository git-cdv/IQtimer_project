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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hfad.iqtimer.MainActivity;
import com.hfad.iqtimer.R;
import com.hfad.iqtimer.databinding.FragmentProgressBinding;
import com.hfad.iqtimer.dialogs.DialogProgressDeleteGoal;

import java.util.Objects;


public class ProgressFragment extends Fragment implements View.OnClickListener {

    DialogFragmentGoal dlgSetGoal;
    DialogProgressDeleteGoal dlgDelGoal;
    FragmentProgressBinding binding;
    ProgressViewModel mViewmodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mViewmodel.getStateGoal();//заполняем вью ЦЕЛИ в зависимости от активности цели
        mViewmodel.getCounter();//заполняем вью Счетчика эф дней подряд

        if (mViewmodel.isPremium()) {
            mViewmodel.getStateP();//заполняем вью Достижений
        }

        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress,container,false);
        View v = binding.getRoot();

        (v.findViewById(R.id.viewBtnMore)).setOnClickListener(this);
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
                mViewmodel.isPutAdd.set(true);
                break;
            case R.id.img_btn_cancel_goal:
                dlgDelGoal= new DialogProgressDeleteGoal();
                dlgDelGoal.show(getParentFragmentManager(), "dlgDelGoal");
                mViewmodel.isPutAdd.set(false);
                break;
            case R.id.viewBtnMore:
                ((ProgressActivity) requireActivity()).toProgressList();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }

    }

}