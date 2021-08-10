package com.chkan.iqtimer.progress;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.chkan.iqtimer.R;
import com.chkan.iqtimer.databinding.FragmentProgressBinding;

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
        mViewmodel.getStateP();
        }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress,container,false);
        View v = binding.getRoot();

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }

}