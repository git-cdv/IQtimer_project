package com.hfad.iqtimer.database;

public class PrefHelper {

    private static final String INTRO_SNACKBAR_STEP = "INTRO_step";

    public static int getLastIntroStep() {
        return App.getPref().getInt(INTRO_SNACKBAR_STEP, 0);
    }

    public static void setLastIntroStep(int step) {
        App.getPref().edit().putInt(INTRO_SNACKBAR_STEP, step).apply();
    }
}
