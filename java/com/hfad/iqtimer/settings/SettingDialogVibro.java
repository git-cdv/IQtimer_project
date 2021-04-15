package com.hfad.iqtimer.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.ListSounds;

public class SettingDialogVibro extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_PREF_VIBRO_NUM = "prefvibrochoice";

    SharedPreferences sPrefSettings;
    Vibrator mVibro;
    ListSounds mListSounds;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getContext());
        int mChoiceSound = sPrefSettings.getInt(KEY_PREF_VIBRO_NUM,0);
        mListSounds = new ListSounds ();
        String ListTitle[] = mListSounds.getListTitleVibro(getContext());

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_vibrator)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .setSingleChoiceItems(ListTitle,mChoiceSound,this);//ждет адаптер(массив) + выбраную ячейку + слушатель
        return adb.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        mVibro = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        long [][] ListVibro = mListSounds.getListVibro();

        if (which == Dialog.BUTTON_POSITIVE){
            if (mVibro!=null) {
                mVibro.cancel();
            }
            ListView lv = ((AlertDialog) dialog).getListView();
            SharedPreferences.Editor ed = sPrefSettings.edit();
            //записываем выбранный вариант вибро
            ed.putInt(KEY_PREF_VIBRO_NUM,lv.getCheckedItemPosition());
            ed.commit();

            }
        else if (which == Dialog.BUTTON_NEGATIVE){
            if (mVibro!=null) {
                mVibro.cancel();
            }
        } else {
            if (mVibro!=null) {
                mVibro.cancel();
            }

            if (mVibro.hasVibrator()||which!=0) {
                mVibro.vibrate(ListVibro[which], -1);
            }

        }
    }
}