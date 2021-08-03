package com.chkan.iqtimer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.chkan.iqtimer.R;

public class DialogStop extends DialogFragment {

    public interface DialogStopListener {
        void onDialogStopPositiveClick();
        void onDialogStopNegativeClick();
    }

    DialogStop.DialogStopListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DialogStop.DialogStopListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Must implement DialogStopListener");
        }
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder adb = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.reset_timer).setPositiveButton(R.string.yes, (dialog, which) -> listener.onDialogStopPositiveClick())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> listener.onDialogStopNegativeClick());
            return adb.create();

    }
}
