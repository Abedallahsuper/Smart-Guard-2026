package com.example.myapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class ReportScheduler {

    public static final String UNIQUE_WORK = "weekly_prediction_report";

    private ReportScheduler() {
    }

    public static void ensureScheduled(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeeklyReportWorker.class,
                7,
                TimeUnit.DAYS
        ).setInitialDelay(millisUntilNextFridayEvening(), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK);
    }

    public static void applyEnabledState(Context context, boolean enabled) {
        if (enabled) {
            ensureScheduled(context);
        } else {
            cancel(context);
        }
    }

    static long millisUntilNextFridayEvening() {
        Calendar now = Calendar.getInstance();
        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, 20);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        int today = now.get(Calendar.DAY_OF_WEEK);
        int daysUntilFriday = (Calendar.FRIDAY - today + 7) % 7;
        if (daysUntilFriday == 0 && !target.after(now)) {
            daysUntilFriday = 7;
        }
        target.add(Calendar.DAY_OF_YEAR, daysUntilFriday);
        return Math.max(60_000L, target.getTimeInMillis() - now.getTimeInMillis());
    }
}
