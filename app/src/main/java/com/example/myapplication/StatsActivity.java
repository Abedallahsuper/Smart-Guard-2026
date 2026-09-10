package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            List<MovementLog> logs = AppDatabase.getInstance(this).movementLogDao().getAllLogs();
            PredictionEngine.Insight insight = PredictionEngine.analyze(logs);
            runOnUiThread(() -> bindInsight(insight));
        }).start();
    }

    private void bindInsight(PredictionEngine.Insight insight) {
        TextView txtKpiTotal = findViewById(R.id.txtKpiTotal);
        TextView txtKpiToday = findViewById(R.id.txtKpiToday);
        TextView txtKpiIdle = findViewById(R.id.txtKpiIdle);
        TextView txtRiskBadge = findViewById(R.id.txtRiskBadge);
        TextView txtPrediction = findViewById(R.id.txtPrediction);
        DonutChartView chartDonut = findViewById(R.id.chartDonut);
        BarChartView chartHourly = findViewById(R.id.chartHourly);
        SparklineView chartWeek = findViewById(R.id.chartWeek);

        MotionFx.countTo(txtKpiTotal, insight.totalMotions);
        MotionFx.countTo(txtKpiToday, insight.todayMotions);
        MotionFx.countTo(txtKpiIdle, insight.totalIdle);
        txtRiskBadge.setText(insight.riskLevel + " · " + insight.riskPercent + "%");
        txtPrediction.setText(insight.summary);
        chartDonut.setValues(insight.totalMotions, insight.totalIdle);
        chartHourly.setValues(insight.hourly);
        chartWeek.setData(insight.last7Days, insight.last7Labels);
        MotionFx.stagger(40,
                txtRiskBadge,
                txtPrediction,
                findViewById(R.id.panelKpi),
                chartDonut,
                chartHourly,
                chartWeek);
    }
}
