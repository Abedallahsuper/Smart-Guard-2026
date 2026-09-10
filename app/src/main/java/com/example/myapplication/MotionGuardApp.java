package com.example.myapplication;

import android.app.Application;

public class MotionGuardApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ReportNotifier.ensureChannel(this);
        if (ReportPrefs.isEnabled(this)) {
            ReportScheduler.ensureScheduled(this);
        }
    }
}
