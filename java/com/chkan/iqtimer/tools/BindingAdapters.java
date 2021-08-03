package com.chkan.iqtimer.tools;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.BindingAdapter;

import com.chkan.iqtimer.R;
import com.chkan.iqtimer.database.App;
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
    @BindingAdapter("app:backgroundProgress")
    public static void setBackgroundProgress(ImageView v, int value) {
        Context context = App.getInstance().getContext();
        if (value<11){
            v.setBackground(AppCompatResources.getDrawable(context,R.drawable.rounded_border_900));
        } else {
            v.setBackground(AppCompatResources.getDrawable(context,R.drawable.rounded_border_gold));
        }
    }


}
