package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovementLogDao {
    @Insert
    void insert(MovementLog log);

    @Query("SELECT * FROM movement_logs ORDER BY id DESC")
    List<MovementLog> getAllLogs();

    @Query("DELETE FROM movement_logs")
    void deleteAll();
}
