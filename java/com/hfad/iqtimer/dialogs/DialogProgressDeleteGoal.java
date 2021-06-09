package com.hfad.iqtimer.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.progress.ProgressViewModel;

import org.jetbrains.annotations.NotNull;

public class DialogProgressDeleteGoal extends DialogFragment implements DialogInterface.OnClickListener {

    ProgressViewModel mViewmodel;

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_delete_goal_title).setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.cancel, this);

            return adb.create();
        }

        public void onClick(DialogInterface dialog, int which) {
            mViewmodel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);

            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    mViewmodel.deleteGoal();
                    dismiss();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    mViewmodel.isPutAdd.set(true);
                    dismiss();
                    break;
            }
        }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        super.onCancel(dialog);
        mViewmodel.isPutAdd.set(true);
        dismiss();
    }
}

