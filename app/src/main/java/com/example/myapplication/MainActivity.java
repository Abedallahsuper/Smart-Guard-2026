package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnConnect;
    TextView txtStatus, txtDeviceName, txtConnectionLabel, txtAiPreview, txtAiRisk, txtLastReport, txtReportStatus;
    View viewPulse, cardLogs, cardStats, cardAiPreview;
    MaterialSwitch switchWeeklyReport;
    boolean pendingSendReport = false;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    InputStream inputStream;

    Handler handler = new Handler();
    boolean isConnected = false;

    MovementLogDao movementLogDao;

    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        txtStatus = findViewById(R.id.txtStatus);
        txtDeviceName = findViewById(R.id.txtDeviceName);
        txtConnectionLabel = findViewById(R.id.txtConnectionLabel);
        txtAiPreview = findViewById(R.id.txtAiPreview);
        txtAiRisk = findViewById(R.id.txtAiRisk);
        txtLastReport = findViewById(R.id.txtLastReport);
        txtReportStatus = findViewById(R.id.txtReportStatus);
        viewPulse = findViewById(R.id.viewPulse);
        cardLogs = findViewById(R.id.cardLogs);
        cardStats = findViewById(R.id.cardStats);
        cardAiPreview = findViewById(R.id.cardAiPreview);
        switchWeeklyReport = findViewById(R.id.switchWeeklyReport);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        movementLogDao = AppDatabase.getInstance(this).movementLogDao();

        applyStatus(R.string.status_waiting, R.string.chip_offline, R.color.text_muted);
        requestPermissionsIfNeeded();
        setupWeeklyReportCard();
        playEntrance();

        btnConnect.setOnClickListener(v -> MotionFx.press(v, this::connectBluetooth));
        cardLogs.setOnClickListener(v -> MotionFx.press(v,
                () -> startActivity(new Intent(this, LogsActivity.class))));
        View.OnClickListener openStats = v -> MotionFx.press(v,
                () -> startActivity(new Intent(this, StatsActivity.class)));
        cardStats.setOnClickListener(openStats);
        cardAiPreview.setOnClickListener(openStats);
        findViewById(R.id.btnViewReport).setOnClickListener(v -> MotionFx.press(v,
                () -> startActivity(new Intent(this, ReportActivity.class))));
        findViewById(R.id.btnSendReportNow).setOnClickListener(v ->
                MotionFx.press(v, this::sendWeeklyReportNow));
    }

    private void playEntrance() {
        MotionFx.stagger(40,
                findViewById(R.id.panelStatus),
                findViewById(R.id.rowNav),
                cardAiPreview,
                findViewById(R.id.panelWeekly),
                txtLastReport,
                findViewById(R.id.rowReportBtns));
        MotionFx.startPulse(viewPulse);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAiPreview();
        refreshReportCard();
    }

    private void setupWeeklyReportCard() {
        switchWeeklyReport.setChecked(ReportPrefs.isEnabled(this));
        switchWeeklyReport.setOnCheckedChangeListener((button, enabled) -> {
            if (enabled && !hasNotificationPermission()) {
                pendingSendReport = false;
                requestNotificationPermission();
            }
            ReportPrefs.setEnabled(this, enabled);
            ReportScheduler.applyEnabledState(this, enabled);
            refreshReportCard();
        });
        refreshReportCard();
    }

    private void refreshReportCard() {
        boolean enabled = ReportPrefs.isEnabled(this);
        txtReportStatus.setText(enabled ? getString(R.string.report_card_hint) : getString(R.string.report_disabled));
        txtLastReport.setText(ReportPrefs.lastSentLabel(this));
    }

    private void sendWeeklyReportNow() {
        if (!hasNotificationPermission()) {
            pendingSendReport = true;
            requestNotificationPermission();
            Toast.makeText(this, R.string.report_permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            List<MovementLog> logs = movementLogDao.getAllLogs();
            PredictionEngine.WeeklyReport report = PredictionEngine.buildWeeklyReport(logs);
            ReportNotifier.send(this, report);
            ReportPrefs.markSent(this);
            runOnUiThread(() -> {
                refreshReportCard();
                Intent open = new Intent(this, ReportActivity.class);
                open.putExtra(ReportActivity.EXTRA_REPORT, report.fullText);
                startActivity(open);
            });
        }).start();
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
        }
    }

    private void loadAiPreview() {
        new Thread(() -> {
            List<MovementLog> logs = movementLogDao.getAllLogs();
            PredictionEngine.Insight insight = PredictionEngine.analyze(logs);
            runOnUiThread(() -> {
                txtAiRisk.setText(R.string.next_motion);
                if (insight.totalMotions == 0) {
                    txtAiPreview.setText(R.string.next_motion_none);
                } else {
                    txtAiPreview.setText(PredictionEngine.formatHour(insight.nextLikelyHour));
                }
            });
        }).start();
    }

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            java.util.ArrayList<String> needed = new java.util.ArrayList<>();
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_CONNECT);
                needed.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            if (!needed.isEmpty()) {
                ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 1);
            }
        }
    }

    private BluetoothDevice findPairedDevice() {
        if (!hasBluetoothConnectPermission()) {
            return null;
        }

        try {
            if (bluetoothAdapter != null) {
                for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) {
                    if (d.getName() != null && d.getName().contains("HC")) {
                        return d;
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void connectBluetooth() {
        if (!hasBluetoothConnectPermission()) {
            requestPermissionsIfNeeded();
            return;
        }

        new Thread(() -> {
            try {
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    runOnUiThread(() -> applyStatus(R.string.status_bt_off, R.string.chip_bt_off, R.color.amber));
                    return;
                }

                device = findPairedDevice();

                if (device == null) {
                    runOnUiThread(() -> applyStatus(R.string.status_no_device, R.string.chip_none, R.color.rose));
                    return;
                }

                runOnUiThread(() -> {
                    applyStatus(R.string.status_connecting, R.string.chip_linking, R.color.amber);
                    try {
                        txtDeviceName.setText(device.getName());
                    } catch (SecurityException ignored) {
                        txtDeviceName.setText(R.string.device_unknown);
                    }
                });

                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();

                inputStream = socket.getInputStream();
                isConnected = true;

                runOnUiThread(() -> {
                    applyStatus(R.string.status_connected, R.string.chip_online, R.color.mint);
                    btnConnect.setText(R.string.reconnect);
                });

                startListening();

            } catch (SecurityException se) {
                runOnUiThread(() -> applyStatus(R.string.status_denied, R.string.chip_denied, R.color.rose));
            } catch (Exception e) {
                runOnUiThread(() -> applyStatus(R.string.status_failed, R.string.chip_failed, R.color.rose));
                e.printStackTrace();
            }
        }).start();
    }

    private void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder msg = new StringBuilder();

            while (isConnected) {
                try {
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);

                    msg.append(data);

                    if (data.contains("\n")) {
                        String finalMsg = msg.toString().trim();
                        msg.setLength(0);

                        if (finalMsg.contains("MOTION_DETECTED")) {
                            saveLogToDb("Motion");
                        } else if (finalMsg.contains("NO_MOTION")) {
                            saveLogToDb("Quiet");
                        }

                        handler.post(() -> updateUI(finalMsg));
                    }

                } catch (Exception e) {
                    isConnected = false;
                    handler.post(() -> applyStatus(R.string.status_disconnected, R.string.chip_lost, R.color.rose));
                }
            }
        }).start();
    }

    private void saveLogToDb(String event) {
        new Thread(() -> {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            movementLogDao.insert(new MovementLog(event, currentTime));
            loadAiPreview();
        }).start();
    }

    private void updateUI(String msg) {
        if (msg.contains("MOTION_DETECTED")) {
            applyStatus(R.string.status_motion, R.string.chip_motion, R.color.rose);
        } else if (msg.contains("NO_MOTION")) {
            applyStatus(R.string.status_idle, R.string.chip_quiet, R.color.mint);
        } else {
            txtStatus.setText(msg);
        }
    }

    private void applyStatus(int statusRes, int labelRes, int colorRes) {
        txtStatus.setText(statusRes);
        txtConnectionLabel.setText(labelRes);
        int color = ContextCompat.getColor(this, colorRes);
        txtConnectionLabel.setTextColor(color);
        GradientDrawable pulse = new GradientDrawable();
        pulse.setShape(GradientDrawable.OVAL);
        pulse.setColor(color);
        viewPulse.setBackground(pulse);
        if (statusRes == R.string.status_motion) {
            MotionFx.burst(viewPulse);
        } else {
            MotionFx.startPulse(viewPulse);
        }
    }

    @Override
    protected void onDestroy() {
        MotionFx.stopPulse(viewPulse);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 || pendingSendReport) {
            if (hasNotificationPermission() && pendingSendReport) {
                pendingSendReport = false;
                sendWeeklyReportNow();
            } else if (!hasNotificationPermission() && switchWeeklyReport.isChecked()) {
                Toast.makeText(this, R.string.report_permission_needed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
