package com.hfad.iqtimer.progress;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hfad.iqtimer.R;

import static com.hfad.iqtimer.R.id.btnCancel;

public class DialogFragmentGoal extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "MYLOGS";

    View v;
    TextInputLayout mTextInputSession,mTextInputPeriod;
    TextInputEditText mEditTextSession,mEditTextPeriod,mEditTextNameGoal;
    String selectedNameGoal;
    ProgressViewModel model;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "DialogFragmentGoal: onCreateView");
        v = inflater.inflate(R.layout.fragment_dialog_goal, null);
        v.findViewById(R.id.btnOk).setOnClickListener(this);
        v.findViewById(btnCancel).setOnClickListener(this);
        mEditTextSession = v.findViewById(R.id.dlgEditTextSession);
        mEditTextPeriod= v.findViewById(R.id.dlgEditTextPeriod);
        mEditTextNameGoal = v.findViewById(R.id.dlgEditTextNameGoal);
        mTextInputSession = v.findViewById(R.id.textInputSession);
        mTextInputPeriod= v.findViewById(R.id.textInputDays);;

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedNameGoal = "sessions";
        model = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
    }

    //?????????????? ????????????
    @Override
    public void onClick(View v) {

    switch (v.getId()){
    case btnCancel:
        model.isPutAdd.set(false);
        this.dismiss();
        break;
    case R.id.btnOk:
        if(validateInput()){
            String mSelectedName;
            String mName = mEditTextNameGoal.getText().toString();
            String mQnum= mEditTextSession.getText().toString();
            String mPeriodNum= mEditTextPeriod.getText().toString();
            boolean isGoalSessions = selectedNameGoal.equals("sessions");
            if (isGoalSessions){
                mSelectedName = getResources().getString(R.string.sessiy);
            }else {mSelectedName = getResources().getString(R.string.powerdays);}

            String mTextDisc = "?????? ????????: "+ mQnum + " " + mSelectedName + " ???? " + mPeriodNum + " " + getResources().getString(R.string.days) +".";

            if (isGoalSessions){
                model.createNewGoalSes(mName,mTextDisc,mQnum,mPeriodNum);
            }else {model.createNewGoalPower(mName,mTextDisc,mQnum,mPeriodNum);}


        this.dismiss();
        }
        break;
}
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        model.isPutAdd.set(false);
    }

    private boolean validateInput() {

        if (mEditTextSession.getText().toString().length() == 0) {
            mTextInputSession.setError(getResources().getString(R.string.empty));
            setupTextListener(mTextInputSession,mEditTextSession);
            return false;
        } else if (mEditTextPeriod.getText().toString().length() == 0){
            mTextInputPeriod.setError(getResources().getString(R.string.empty));
            setupTextListener(mTextInputPeriod,mEditTextPeriod);
            return false;
        }
        return true;
    }

    private void setupTextListener(TextInputLayout layout,TextInputEditText textEdit) {
        TextWatcher mTextWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
        };
        textEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onResume() {
        super.onResume();
        //?????????????????????????????? ?????????? ?????????? ???? ?????????????????? ?????????? ????????????????????
        AutoCompleteTextView completeTextViewSes = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewSes);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.goals, android.R.layout.simple_spinner_dropdown_item);
        completeTextViewSes.setAdapter(adapter);
        completeTextViewSes.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                if (position==0){
                    selectedNameGoal = "sessions";
                } else {
                    selectedNameGoal = "powerdays";
                }

            }
        }));

    }

}