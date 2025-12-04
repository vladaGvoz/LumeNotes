package com.example.lumenotes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lumenotes.data.NoteDatabase;
import com.example.lumenotes.model.Note;
import com.example.lumenotes.ui.AddEditNoteActivity;
import com.example.lumenotes.ui.NotesAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// cekiranje gitovanja #2

public class MainActivity extends AppCompatActivity {

    public NotesAdapter adapter;
    RecyclerView recyclerView;
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
            clearSelection();
            if (adapter != null) adapter.disableDeleteMode();
            Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });


        fabDelete.setOnClickListener(v -> deleteSelectedNotes());

        setupSwipeToDelete();

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                if (!adapter.getSelectedPositions().isEmpty() && e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    android.view.View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) {
                        clearSelection();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.disableDeleteMode();
        loadNotes();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNotes() {
        notes = db.noteDao().getAllNotes();

        if (notes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
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
        } else {
            adapter.notes = notes;
            adapter.disableDeleteMode();
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteSelectedNotes() {
        List<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) return;

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Notes")
                .setMessage("Are you sure you want to delete selected notes?")
                .setPositiveButton("Delete", (d, which) -> performDelete(selected))
                .setNegativeButton("Cancel", null)
                .show();

        int buttonColor = getColorFromAttr();

        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null)
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(buttonColor);

        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null)
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(buttonColor);
    }

    private int getColorFromAttr() {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        return typedValue.data;
    }



    @SuppressLint("NotifyDataSetChanged")
    private void performDelete(List<Integer> selectedPositions) {

        List<Note> deleted = new ArrayList<>();

        List<Integer> sorted = new ArrayList<>(selectedPositions);
        sorted.sort(Collections.reverseOrder());

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
                    @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int pos = viewHolder.getBindingAdapterPosition();
                List<Integer> single = Collections.singletonList(pos);
                performDelete(single);
            }

        }).attachToRecyclerView(recyclerView);
    }

    private void clearSelection() {
        if (adapter != null) {
            adapter.disableDeleteMode();
            fabDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {

            getCurrentFocus();

            if (adapter != null && !adapter.getSelectedPositions().isEmpty()) {

                int[] recyclerCoords = new int[2];
                recyclerView.getLocationOnScreen(recyclerCoords);

                float x = ev.getRawX();
                float y = ev.getRawY();

                if (x < recyclerCoords[0] || x > recyclerCoords[0] + recyclerView.getWidth()
                        || y < recyclerCoords[1] || y > recyclerCoords[1] + recyclerView.getHeight()) {

                    clearSelection();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
