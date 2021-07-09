package com.hfad.iqtimer.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.ListSounds;


public class SettingDialogSoundsBreak extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_PREF_SOUND_BREAK_RES = "prefsoundbreakres";
    private static final String KEY_PREF_SOUND_BREAK_NUM = "prefsoundbreaknumber";


    String[] ListTitle;
    int[] ListSounds;
    ListSounds mListSounds;
    SharedPreferences sPrefSettings;
    MediaPlayer mPlayer;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListSounds = new ListSounds();
        ListSounds = mListSounds.getList();
        ListTitle=mListSounds.getListTitle(requireContext());
        //получаем доступ к файлу с настройками приложения
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int mChoiceSound = sPrefSettings.getInt(KEY_PREF_SOUND_BREAK_NUM,0);
        AlertDialog.Builder adb = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.pref_sounds_break)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .setSingleChoiceItems(ListTitle,mChoiceSound,this);//ждет адаптер(массив) + выбраную ячейку + слушатель
        return adb.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mPlayer==null){mPlayer=new MediaPlayer();}

        if (which == Dialog.BUTTON_POSITIVE){
            if ( mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            ListView lv = ((AlertDialog) dialog).getListView();
            SharedPreferences.Editor ed = sPrefSettings.edit();
            //записываем выбранный звукe
            ed.putInt(KEY_PREF_SOUND_BREAK_NUM,lv.getCheckedItemPosition());
            ed.putInt(KEY_PREF_SOUND_BREAK_RES,ListSounds[lv.getCheckedItemPosition()]);
            ed.apply();

            }
        else if (which == Dialog.BUTTON_NEGATIVE){
            if ( mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        } else if (which==0){
            if ( mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            //получаем дефолтную мелодию
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mPlayer = MediaPlayer.create(getContext(),defaultSoundUri);
            mPlayer.start();
        }
        else {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }

            mPlayer = MediaPlayer.create(getContext(),ListSounds[which]);
            mPlayer.start();

        }
    }
}