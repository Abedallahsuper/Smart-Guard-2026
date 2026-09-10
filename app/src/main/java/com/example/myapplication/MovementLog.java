package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movement_logs")
public class MovementLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String event;
    public String dateTime;

    public MovementLog(String event, String dateTime) {
        this.event = event;
        this.dateTime = dateTime;
    }
}
