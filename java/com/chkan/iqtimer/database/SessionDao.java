package com.chkan.iqtimer.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Query("SELECT * FROM session")
    List<Session> getAll();

    @Query("SELECT _id,count,date_full FROM session ORDER BY _id DESC")
    Cursor getListCursor();

    @Query("SELECT _id,date,count FROM session")
    Cursor getHistoryCursor();

    @Insert
    void insert(Session session);
}
