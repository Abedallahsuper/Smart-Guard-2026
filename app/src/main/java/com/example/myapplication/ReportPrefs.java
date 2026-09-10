package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ReportPrefs {

    private static final String FILE = "weekly_report_prefs";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_LAST_SENT = "last_sent";

    private ReportPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static void markSent(Context context) {
        prefs(context).edit().putLong(KEY_LAST_SENT, System.currentTimeMillis()).apply();
    }

    public static String lastSentLabel(Context context) {
        long value = prefs(context).getLong(KEY_LAST_SENT, 0L);
        if (value <= 0L) return context.getString(R.string.report_not_sent);
        String when = new SimpleDateFormat("d MMM, h:mm a", Locale.US).format(new Date(value));
        return context.getString(R.string.report_last_sent, when);
    }
}
