package com.chkan.iqtimer.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Session {

    public Session(String date, int count, String date_full) {
        this.date = date;
        this.count = count;
        this.date_full = date_full;
    }

    @PrimaryKey(autoGenerate = true)
    public long _id;

    public String date;

    public int count;

    public String date_full;
}
