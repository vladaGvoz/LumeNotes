package com.example.lumenotes.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lumenotes.R;
import com.example.lumenotes.data.NoteDatabase;
import com.example.lumenotes.model.Note;

public class AddEditNoteActivity extends AppCompatActivity {

    EditText title, content;
    Button save;

    NoteDatabase db;
    Note currentNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        db = NoteDatabase.getInstance(this);

        title = findViewById(R.id.editTitle);
        content = findViewById(R.id.editContent);
        save = findViewById(R.id.btnSave);

        int id = getIntent().getIntExtra("NOTE_ID", -1);
        if (id != -1) {
            currentNote = db.noteDao().getNoteById(id);
            if (currentNote != null) {
                title.setText(currentNote.title);
                content.setText(currentNote.content);
            }
        }

        save.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String t = title.getText().toString().trim();
        String c = content.getText().toString().trim();

        if (TextUtils.isEmpty(t)) {
            Toast.makeText(this, "Title required!", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        if (currentNote == null) {
            currentNote = new Note(t, c, now);
            db.noteDao().insert(currentNote);
        } else {
            currentNote.title = t;
            currentNote.content = c;
            currentNote.timestamp = now;
            db.noteDao().update(currentNote);
        }

        finish();
    }
}
