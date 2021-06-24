package com.hfad.iqtimer.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.tools.StateEvent;

import org.greenrobot.eventbus.EventBus;

public class DialogFragmentBreakEnded extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = "MYLOGS";
    private static final int RUN_FROM_DIALOG = 7055;
    private static final int STATE_DIALOG_CANCEL = 726;

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
                EventBus.getDefault().post(new StateEvent(RUN_FROM_DIALOG));
                break;
            case Dialog.BUTTON_NEGATIVE:
                EventBus.getDefault().post(new StateEvent(STATE_DIALOG_CANCEL));
                dismiss();
                break;
        }
           }


}