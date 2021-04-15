package com.hfad.iqtimer.database;

import android.content.Context;

import com.hfad.iqtimer.R;

public class ListSounds {

    public int [] getList() {

        int [] ListSounds = new int [15];
        ListSounds[0]= 0;
        ListSounds[1]= R.raw.bass;
        ListSounds[2]= R.raw.bell;
        ListSounds[3]= R.raw.bits;
        ListSounds[4]= R.raw.delicate;
        ListSounds[5]= R.raw.doubles;
        ListSounds[6]= R.raw.drip;
        ListSounds[7]= R.raw.hangout;
        ListSounds[8]= R.raw.sms;
        ListSounds[9]= R.raw.soft;
        ListSounds[10]= R.raw.sonorous;
        ListSounds[11]= R.raw.sweet;
        ListSounds[12]= R.raw.tap;
        ListSounds[13]= R.raw.viviza;
        ListSounds[14]= R.raw.woohoo;

        return ListSounds;
    }


    public String [] getListTitle(Context context) {

        String [] ListTitle = new String [15];
        ListTitle[0]= context.getResources().getString(R.string.pref_def_sound);
        ListTitle[1]= "Bass";
        ListTitle[2]= "Bell";
        ListTitle[3]= "Bits";
        ListTitle[4]= "Delicate";
        ListTitle[5]= "Doubles";
        ListTitle[6]= "Drip";
        ListTitle[7]= "Hangout";
        ListTitle[8]= "Sms";
        ListTitle[9]= "Soft";
        ListTitle[10]= "Sonorous";
        ListTitle[11]= "Sweet";
        ListTitle[12]= "Tap";
        ListTitle[13]= "Viviza";
        ListTitle[14]= "Woohoo";

        return ListTitle;
    }

    public String [] getListTitleVibro(Context context) {

        String [] ListTitle = new String [7];
        ListTitle[0]= context.getResources().getString(R.string.no);
        ListTitle[1]= context.getResources().getString(R.string.delicate);
        ListTitle[2]= context.getResources().getString(R.string.strong);
        ListTitle[3]= "SOS";
        ListTitle[4]= context.getResources().getString(R.string.puls);
        ListTitle[5]= context.getResources().getString(R.string.fanfare);
        ListTitle[6]= context.getResources().getString(R.string.starwars);

        return ListTitle;
    }

    public long [][] getListVibro() {
        //1 - задержка перед виброзвонком
        //2 - первый виброзвонок в мс
        //3 - пауза в мс
        //4 - второй виброзвонок в мс
        long[] pattern0 = null;
        long[] patternLow = { 0, 100, 100, 100 };
        long[] patternHigh = { 0, 500, 100, 500 };
        long[] patternSos = {100,30,100,30,100,200,200,30,200,30,200,200,100,30,100,30,100};
        long[] patternPuls = { 0, 300, 100, 300,400,300,100,300 };
        long[] patternFanfare = {50,100,50,100,50,100,400,100,300,100,350,50,200,100,100,50,600};
        long[] patternStarWars = {500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500};
        long[][] ListVibro = {pattern0,patternLow,patternHigh,patternSos,patternPuls,patternFanfare,patternStarWars};

        return ListVibro;
    }
}
