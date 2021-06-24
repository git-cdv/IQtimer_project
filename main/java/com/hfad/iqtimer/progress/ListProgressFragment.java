package com.hfad.iqtimer.progress;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.NavigationUI;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.databinding.FragmentProgressListBinding;
import com.hfad.iqtimer.statistic.StatisticFragment;
import com.hfad.iqtimer.statistic.StatisticListDaysFragment;

import java.util.Objects;


public class ListProgressFragment extends Fragment {

    ProgressViewModel mViewmodel;
    FragmentProgressListBinding binding;
    private static final String TAG = "MYLOGS";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mViewmodel.getStateP();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress_list,container,false);
        View v = binding.getRoot();

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }
}
