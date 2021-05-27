package com.hfad.iqtimer.tools;

import android.view.View;

import androidx.databinding.BindingAdapter;

import com.marcok.stepprogressbar.StepProgressBar;

public class BindingAdapters {

    @BindingAdapter("app:numberDots")
    public static void setNumDots(StepProgressBar mStepProgressBar, int value) {
        mStepProgressBar.setNumDots(value);

    }

    @BindingAdapter("app:activeDotIndex")
    public static void setCurrentDots(StepProgressBar mStepProgressBar, int value) {
        mStepProgressBar.setCurrentProgressDot(value - 1);
    }

}
