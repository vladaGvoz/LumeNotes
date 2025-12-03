package com.example.lumenotes.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.lumenotes.model.Note;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase INSTANCE;

    public static synchronized NoteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            NoteDatabase.class, "note_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract NoteDao noteDao();
}
