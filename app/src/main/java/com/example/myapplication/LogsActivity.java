package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmptyLogs;
    private TextView txtLogsCount;
    private ImageView btnClearLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewLogs);
        txtEmptyLogs = findViewById(R.id.txtEmptyLogs);
        txtLogsCount = findViewById(R.id.txtLogsCount);
        btnClearLogs = findViewById(R.id.btnClearLogs);

        btnBack.setOnClickListener(v -> finish());
        btnClearLogs.setOnClickListener(v -> confirmClearLogs());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadLogs();
    }

    private void loadLogs() {
        new Thread(() -> {
            List<MovementLog> logs = AppDatabase.getInstance(this).movementLogDao().getAllLogs();
            runOnUiThread(() -> bindLogs(logs));
        }).start();
    }

    private void bindLogs(List<MovementLog> logs) {
        recyclerView.setAdapter(new LogsAdapter(logs));
        recyclerView.scheduleLayoutAnimation();
        boolean empty = logs.isEmpty();
        txtEmptyLogs.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnClearLogs.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            MotionFx.fadeUp(txtEmptyLogs, 80);
        }
        txtLogsCount.setText(empty ? getString(R.string.logs_subtitle) : getString(R.string.logs_count, logs.size()));
    }

    private void confirmClearLogs() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logs_clear_title)
                .setMessage(R.string.logs_clear_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.logs_clear_confirm, (dialog, which) -> clearLogs())
                .show();
    }

    private void clearLogs() {
        new Thread(() -> {
            AppDatabase.getInstance(this).movementLogDao().deleteAll();
            runOnUiThread(() -> {
                bindLogs(java.util.Collections.emptyList());
                Toast.makeText(this, R.string.logs_cleared, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
