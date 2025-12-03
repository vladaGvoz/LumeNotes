package com.example.lumenotes.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lumenotes.MainActivity;
import com.example.lumenotes.R;
import com.example.lumenotes.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public List<Note> notes;
    Context context;

    private boolean deleteMode = false;
    private final List<Integer> selectedPositions = new ArrayList<>();

    public interface NoteActionListener {
        void onLongPress();
        void onSelectionChanged(int count);
    }

    private NoteActionListener listener;

    public void setListener(NoteActionListener listener) {
        this.listener = listener;
    }

    public NotesAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        this.context = context;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {

        Note note = notes.get(position);

        holder.title.setText(note.title);
        holder.content.setText(note.content);

        boolean isSelected = selectedPositions.contains(position);

        holder.itemView.setAlpha(isSelected ? 0.4f : 1f);

        holder.itemView.setOnClickListener(v -> {
            if (!deleteMode) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).adapter.disableDeleteMode();
                }
                Intent intent = new Intent(context, AddEditNoteActivity.class);
                intent.putExtra("NOTE_ID", note.id);
                context.startActivity(intent);
            } else {
                toggleSelection(position);
            }
        });


        holder.itemView.setOnLongClickListener(v -> {
            deleteMode = true;
            toggleSelection(position);
            if (listener != null) listener.onLongPress();
            return true;
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }

        notifyDataSetChanged();

        if (listener != null)
            listener.onSelectionChanged(selectedPositions.size());
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void disableDeleteMode() {
        deleteMode = false;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;

        public NoteViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            content = itemView.findViewById(R.id.noteContent);
        }
    }

    public  void clearSelection() {
        selectedPositions.clear();
        if(listener != null) listener.onSelectionChanged(0);
        notifyDataSetChanged();
    }

}
