package com.hfad.iqtimer.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.util.Log;

import com.hfad.iqtimer.MainActivity;
import com.hfad.iqtimer.R;

public class DialogFragmentSesEnd extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = "MYLOGS";
    private static final int STATE_BREAK_STARTED = 400;
    private static final int STATE_TIMER_WAIT = 101;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_session_end).setPositiveButton(R.string.dialog_rest_start, this)
                .setNegativeButton(R.string.back, this);

        return adb.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                Log.d(TAG, "Dialog: BUTTON_POSITIVE");
                ((MainActivity)getActivity()).onBreakTime(STATE_BREAK_STARTED);
                break;
            case Dialog.BUTTON_NEGATIVE:
                ((MainActivity)getActivity()).onBreakTime(STATE_TIMER_WAIT);
                break;
        }
           }

}