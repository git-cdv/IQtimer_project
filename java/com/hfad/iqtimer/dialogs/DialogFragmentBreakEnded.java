package com.hfad.iqtimer.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hfad.iqtimer.MainActivity;
import com.hfad.iqtimer.R;

import java.util.Objects;

public class DialogFragmentBreakEnded extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = "MYLOGS";
    private static final int STATE_TIMER_WORKING = 500;
    private static final int STATE_TIMER_WAIT = 101;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_break_end).setPositiveButton(R.string.dialog_work_start, this)
                .setNegativeButton(R.string.cancel, this);

        return adb.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                Log.d(TAG, "Dialog: BUTTON_POSITIVE");
                ((MainActivity) requireActivity()).onBreakTime(STATE_TIMER_WORKING);
                break;
            case Dialog.BUTTON_NEGATIVE:
                ((MainActivity)getActivity()).onBreakTime(STATE_TIMER_WAIT);
                break;
        }
           }

}