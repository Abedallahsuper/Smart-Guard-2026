package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ReportActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT = ReportNotifier.EXTRA_REPORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtPeriod = findViewById(R.id.txtReportPeriod);
        TextView txtBody = findViewById(R.id.txtReportBody);
        MaterialButton btnShare = findViewById(R.id.btnShareReport);

        btnBack.setOnClickListener(v -> finish());

        String extra = getIntent().getStringExtra(EXTRA_REPORT);
        if (extra != null && !extra.isEmpty()) {
            bindReport(extra, txtPeriod, txtBody, btnShare);
        } else {
            new Thread(() -> {
                List<MovementLog> logs = AppDatabase.getInstance(this).movementLogDao().getAllLogs();
                PredictionEngine.WeeklyReport report = PredictionEngine.buildWeeklyReport(logs);
                runOnUiThread(() -> bindReport(report.fullText, txtPeriod, txtBody, btnShare));
            }).start();
        }
    }

    private void bindReport(String fullText, TextView txtPeriod, TextView txtBody, MaterialButton btnShare) {
        String period = "";
        for (String line : fullText.split("\n")) {
            if (line.startsWith("Period:")) {
                period = line.replace("Period:", "").trim();
                break;
            }
        }
        txtPeriod.setText(period.isEmpty() ? getString(R.string.report_subtitle) : period);
        txtBody.setText(fullText);
        MotionFx.stagger(40, findViewById(R.id.btnShareReport));
        btnShare.setOnClickListener(v -> MotionFx.press(v, () -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_title));
            share.putExtra(Intent.EXTRA_TEXT, fullText);
            startActivity(Intent.createChooser(share, getString(R.string.report_share)));
        }));
    }
}
