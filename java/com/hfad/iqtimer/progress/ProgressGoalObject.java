package com.hfad.iqtimer.progress;

public class ProgressGoalObject {

    public String name;
    public String description;
    public String barGoal;
    public String barCurrent;
    public String counGoal;
    public String countCurrent;

    public ProgressGoalObject(String name, String description, String barGoal, String barCurrent, String counGoal, String countCurrent) {
        this.name = name;
        this.description = description;
        this.barGoal = barGoal;
        this.barCurrent = barCurrent;
        this.counGoal = counGoal;
        this.countCurrent = countCurrent;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBarGoal() {
        return barGoal;
    }

    public String getBarCurrent() {
        return barCurrent;
    }

    public String getCounGoal() {
        return counGoal;
    }

    public String getCountCurrent() {
        return countCurrent;
    }
}
