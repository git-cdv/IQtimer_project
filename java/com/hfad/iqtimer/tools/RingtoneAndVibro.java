package com.hfad.iqtimer.tools;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.preference.PreferenceManager;

import com.hfad.iqtimer.database.ListSounds;

import java.io.IOException;

public class RingtoneAndVibro extends ContextWrapper {

    private static final int STATE_TIMER_FINISHED = 100;
    private static final String KEY_PREF_SOUND_RES = "prefsoundres";
    private static final String KEY_PREF_SOUND_BREAK_RES = "prefsoundbreakres";
    private static final String KEY_PREF_VIBRO_NUM = "prefvibrochoice";

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private AudioManager mAudioManager;
    SharedPreferences sPrefSettings;

    public RingtoneAndVibro(Context base) {
        super(base);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        sPrefSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void play(int State) {
        try {
            mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            //AudioManager.RINGER_MODE_SILENT - ВКЛ режим "без звука" и ВЫКЛ вибрация
            //AudioManager.RINGER_MODE_VIBRATE - ВКЛ режим "без звука" и ВКЛ вибрация
            //AudioManager.RINGER_MODE_NORMAL - ВЫКЛ режим "без звука"

            boolean isRingerModeNormal = mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL;

            //если включен звук в настройках приложения и телефона
            if (sPrefSettings.getBoolean("switch_notif",true)&&isRingerModeNormal) {

                int mSoundRes;
                Uri SoundUri;
                if (State==STATE_TIMER_FINISHED) {
                    mSoundRes = sPrefSettings.getInt(KEY_PREF_SOUND_RES, 0);
                } else {
                    mSoundRes = sPrefSettings.getInt(KEY_PREF_SOUND_BREAK_RES, 0);
                }

                if (mSoundRes == 0) {
                    //получаем дефолтную мелодию
                    SoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                } else {
                    SoundUri = Uri.parse("android.resource://"+getPackageName()+"/"+mSoundRes);

                }

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(this, SoundUri);
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(mp -> {
                    // TODO: check duration of custom ringtones which may be much longer than notification sounds.
                    // If it's n seconds long and we're in continuous mode,
                    // schedule a stop after x seconds.
                    mMediaPlayer.start();
                });
            }

            final int mVibroNum = sPrefSettings.getInt(KEY_PREF_VIBRO_NUM,0);
            if (mVibroNum > 0) {
                ListSounds mListSounds = new ListSounds();
                long [][] ListVibro = mListSounds.getListVibro();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mVibrator.vibrate(VibrationEffect.createWaveform(ListVibro[mVibroNum], -1),
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build());
                } else {
                    mVibrator.vibrate(ListVibro[mVibroNum], -1);
                }

            }

        } catch (SecurityException | IOException e) {
            stop();
        }
    }

    public void stop() {
        if (mMediaPlayer != null && mVibrator != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }
}
