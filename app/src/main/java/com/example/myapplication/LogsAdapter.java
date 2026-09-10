package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private final List<MovementLog> logs;

    public LogsAdapter(List<MovementLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        MovementLog log = logs.get(position);
        boolean motion = PredictionEngine.isMotion(log.event);
        holder.txtEvent.setText(PredictionEngine.displayEvent(log.event));
        holder.txtDateTime.setText(log.dateTime);
        int color = ContextCompat.getColor(holder.itemView.getContext(), motion ? R.color.rose : R.color.mint);
        holder.viewAccent.setBackgroundColor(color);
        holder.txtEvent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView txtEvent, txtDateTime;
        View viewAccent;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEvent = itemView.findViewById(R.id.txtEvent);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            viewAccent = itemView.findViewById(R.id.viewAccent);
        }
    }
}
