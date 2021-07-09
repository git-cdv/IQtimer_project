package com.hfad.iqtimer.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.databinding.FragmentProgressListBinding;


public class ListProgressFragment extends Fragment {

    ProgressViewModel mViewmodel;
    FragmentProgressListBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        mViewmodel.getStateP();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_progress_list,container,false);
        View v = binding.getRoot();
        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }
}
