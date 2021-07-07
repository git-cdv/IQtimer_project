package com.hfad.iqtimer.progress;

import android.content.res.Resources;
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


public class ProgressFragment extends Fragment {

    FragmentProgressBinding binding;
    ProgressViewModel mViewmodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mViewmodel.getCounter();//заполняем вью Счетчика эф дней подряд
        mViewmodel.getStateGoal();//заполняем вью ЦЕЛИ в зависимости от активности цели
        //заполняем вью Достижений
        if (mViewmodel.isPremium()) {mViewmodel.getStateP(); }
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress,container,false);
        View v = binding.getRoot();

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }

}