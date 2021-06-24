package com.hfad.iqtimer.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SessionDao {

    @Query("SELECT * FROM session")
    List<Session> getAll();

    @Query("SELECT _id,count,date_full FROM session ORDER BY _id DESC")
    Cursor getListCursor();

    @Query("SELECT _id,date,count FROM session")
    Cursor getHistoryCursor();

    @Query("SELECT * FROM session WHERE _id = :id")
    Session getById(long id);

    @Insert
    void insert(Session session);

    @Update
    void update(Session session);

    @Delete
    void delete(Session session);

}
