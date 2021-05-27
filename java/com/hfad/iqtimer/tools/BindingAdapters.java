package com.hfad.iqtimer.tools;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

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

    @BindingAdapter("app:animationOnPause")
    public static void animationOnPause(TextView v, boolean value) {

        Animation animTimerView = new AlphaAnimation(0.2f, 1.0f);//анимация альфа канала (прозрачности от 0 до 1)
            animTimerView.setDuration(800); //длительность анимации
            animTimerView.setStartOffset(50);//сдвижка начала анимации (с середины)
            animTimerView.setRepeatMode(Animation.REVERSE);//режим повтора - сначала или в обратном порядке
            animTimerView.setRepeatCount(Animation.INFINITE);//режим повтора (бесконечно)

        if(value){
            v.startAnimation(animTimerView);
        } else {
            v.clearAnimation();
        }
    }



}
