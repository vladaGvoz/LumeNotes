package com.example.lumenotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.lumenotes.data.NoteDatabase;
import com.example.lumenotes.model.Note;
import com.example.lumenotes.ui.AddEditNoteActivity;
import com.example.lumenotes.ui.NotesAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotesAdapter adapter;
    FloatingActionButton fabAdd, fabDelete;

    NoteDatabase db;
    List<Note> notes = new ArrayList<>();

    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = NoteDatabase.getInstance(this);

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        fabAdd = findViewById(R.id.fabAddNote);
        fabDelete = findViewById(R.id.fabDelete);

        tvEmpty = findViewById(R.id.tvEmpty);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });

        fabDelete.setOnClickListener(v -> deleteSelectedNotes());

        setupSwipeToDelete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notes = db.noteDao().getAllNotes();


        if (notes.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new NotesAdapter(notes, this);

        adapter.setListener(new NotesAdapter.NoteActionListener() {
            @Override
            public void onLongPress() {
                fabDelete.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSelectionChanged(int count) {
                fabDelete.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void deleteSelectedNotes() {
        List<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Notes")
                .setMessage("Are you sure you want to delete selected notes?")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(selected))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(List<Integer> selectedPositions) {

        List<Note> deleted = new ArrayList<>();

        List<Integer> sorted = new ArrayList<>(selectedPositions);
        Collections.sort(sorted, Collections.reverseOrder());

        for (int pos : sorted) {
            Note note = notes.get(pos);
            deleted.add(note);
            db.noteDao().delete(note);
            notes.remove(pos);
        }

        adapter.disableDeleteMode();
        fabDelete.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();

        Snackbar.make(recyclerView, "Notes deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> undoDelete(deleted))
                .show();
    }

    private void undoDelete(List<Note> deletedNotes) {
        for (Note n : deletedNotes) {
            db.noteDao().insert(n);
            notes.add(n);
        }

        notes = db.noteDao().getAllNotes();
        loadNotes();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(
                    RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int pos = viewHolder.getAdapterPosition();
                List<Integer> single = Collections.singletonList(pos);
                performDelete(single);
            }

        }).attachToRecyclerView(recyclerView);
    }
}
