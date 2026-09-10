package com.example.myapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class WeeklyReportWorker extends Worker {

    public WeeklyReportWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!ReportPrefs.isEnabled(context)) {
            return Result.success();
        }

        List<MovementLog> logs = AppDatabase.getInstance(context).movementLogDao().getAllLogs();
        PredictionEngine.WeeklyReport report = PredictionEngine.buildWeeklyReport(logs);
        ReportNotifier.send(context, report);
        ReportPrefs.markSent(context);
        return Result.success();
    }
}
