package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class ReportNotifier {

    public static final String CHANNEL_ID = "weekly_prediction_reports";
    public static final int NOTIFICATION_ID = 4101;
    public static final String EXTRA_REPORT = "extra_weekly_report";

    private ReportNotifier() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weekly note",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Home motion summary");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void send(Context context, PredictionEngine.WeeklyReport report) {
        ensureChannel(context);
        Intent open = new Intent(context, ReportActivity.class);
        open.putExtra(EXTRA_REPORT, report.fullText);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pending = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Weekly note")
                .setContentText(report.notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(report.notificationText + "\n" + report.outlook))
                .setColor(ContextCompat.getColor(context, R.color.cyan))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException ignored) {
        }
    }
}
