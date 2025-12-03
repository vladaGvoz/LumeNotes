package com.example.lumenotes.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title;

    public String content;
    public long timestamp;

    public Note(@NonNull String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
}
