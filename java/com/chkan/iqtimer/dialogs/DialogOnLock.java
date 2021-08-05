package com.chkan.iqtimer.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.chkan.iqtimer.R;
import com.chkan.iqtimer.progress.ProgressActivity;

import java.util.Objects;

public class DialogOnLock extends DialogFragment{

    private static final String TAG = "MYLOGS";

    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dialog_onlock, null);
        Button btn = v.findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "DialogOnLock: onClick");
                ProgressActivity activity = (ProgressActivity) getActivity();
                assert activity != null;
                activity.launchBilling("sku_access_achivements");
                dismiss();
            }
        });

        return v;
    }
}
