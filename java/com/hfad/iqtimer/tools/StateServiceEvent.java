package com.hfad.iqtimer.tools;

public class StateServiceEvent {
    public final int state;
    public final String text;

    public StateServiceEvent(int state,String text) {
        this.state = state;
        this.text = text;
    }
}

