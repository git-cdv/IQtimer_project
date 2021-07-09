package com.hfad.iqtimer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.tools.TimerState;

public class DialogSession extends DialogFragment {

    public interface DialogSessionListener {
        void onDialogPositiveClick(TimerState state);
        void onDialogNegativeClick();
    }

    DialogSessionListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DialogSessionListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Must implement NoticeDialogListener");
        }
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(App.instance.getSession().getState().get()== TimerState.TIMER_FINISHED){
        AlertDialog.Builder adb = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_session_end).setPositiveButton(R.string.dialog_rest_start, (dialog, which) -> listener.onDialogPositiveClick(TimerState.TIMER_FINISHED))
                .setNegativeButton(R.string.back, (dialog, which) -> listener.onDialogNegativeClick());
        return adb.create();
        } else {

            AlertDialog.Builder adb = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_break_end).setPositiveButton(R.string.work_start, (dialog, which) -> listener.onDialogPositiveClick(TimerState.BREAK_FINISHED))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> listener.onDialogNegativeClick());

            return adb.create();
        }
    }
}
