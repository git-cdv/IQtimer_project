package com.chkan.iqtimer.progress;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chkan.iqtimer.R;

import java.util.Objects;

import static com.chkan.iqtimer.R.id.btnCancel;

public class DialogFragmentGoal extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "MYLOGS";

    View v;
    TextInputLayout mTextInputSession,mTextInputPeriod;
    TextInputEditText mEditTextSession,mEditTextPeriod,mEditTextNameGoal;
    String selectedNameGoal;
    ProgressViewModel model;

    @SuppressLint("InflateParams")
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
        mTextInputPeriod= v.findViewById(R.id.textInputDays);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedNameGoal = "sessions";
        model = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
    }

    //слушает кнопки
    @Override
    public void onClick(View v) {

    if(v.getId()==btnCancel){
        model.isPutAdd.set(false);
        this.dismiss();
    } else {
        if(validateInput()){
            String mSelectedName;
            String mName = Objects.requireNonNull(mEditTextNameGoal.getText()).toString();
            String mQnum= Objects.requireNonNull(mEditTextSession.getText()).toString();
            String mPeriodNum= Objects.requireNonNull(mEditTextPeriod.getText()).toString();
            boolean isGoalSessions = selectedNameGoal.equals("sessions");
            if (isGoalSessions){
                mSelectedName = getResources().getString(R.string.sessiy);
            }else {mSelectedName = getResources().getString(R.string.powerdays);}

            String mTextDisc = getResources().getString(R.string.text_my_goal)+ " " +mQnum + " " + mSelectedName + " " + getResources().getString(R.string.text_in) + " " + mPeriodNum + " " + getResources().getString(R.string.days) +".";

            if (isGoalSessions){
                model.createNewGoalSes(mName,mTextDisc,mQnum,mPeriodNum);
            }else {model.createNewGoalPower(mName,mTextDisc,mQnum,mPeriodNum);}
            this.dismiss();
        }
    }

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        model.isPutAdd.set(false);
    }

    private boolean validateInput() {

        if (Objects.requireNonNull(mEditTextSession.getText()).toString().length() == 0) {
            mTextInputSession.setError(getResources().getString(R.string.empty));
            setupTextListener(mTextInputSession,mEditTextSession);
            return false;
        } else if (Objects.requireNonNull(mEditTextPeriod.getText()).toString().length() == 0){
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
        //устанавливается здесь чтобы не пропадало после переворота
        AutoCompleteTextView completeTextViewSes = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewSes);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.goals, android.R.layout.simple_spinner_dropdown_item);
        completeTextViewSes.setAdapter(adapter);
        completeTextViewSes.setOnItemClickListener(((parent, view, position, l) -> {
            if (position==0){
                selectedNameGoal = "sessions";
            } else {
                selectedNameGoal = "powerdays";
            }

        }));

    }

}