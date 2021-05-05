package com.hfad.iqtimer.progress;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.fragment.app.DialogFragment;

import com.hfad.iqtimer.R;

public class DialogFragmentGoal extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    View v;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_dialog_goal, null);
        v.findViewById(R.id.btnOk).setOnClickListener(this);
        v.findViewById(R.id.btnCancel).setOnClickListener(this);

        return v;
    }
    //слушает выбор спиннеров
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //слушает кнопки
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResume() {
        super.onResume();
        //устанавливается здесь чтобы не пропадало после переворота
        AutoCompleteTextView completeTextViewSes = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewSes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.goals, android.R.layout.simple_spinner_dropdown_item);
        completeTextViewSes.setAdapter(adapter);
        AutoCompleteTextView completeTextViewDays = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewDays);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.date_goals, android.R.layout.simple_spinner_dropdown_item);
        completeTextViewDays.setAdapter(adapter2);
    }
}