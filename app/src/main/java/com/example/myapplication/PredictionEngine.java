package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionEngine {

    public static class Insight {
        public int totalMotions;
        public int totalIdle;
        public int todayMotions;
        public int[] hourly = new int[24];
        public int[] last7Days = new int[7];
        public String[] last7Labels = new String[7];
        public int peakHour;
        public int nextLikelyHour;
        public int riskPercent;
        public String riskLevel;
        public String summary;
    }

    public static boolean isMotion(String event) {
        if (event == null) return false;
        String value = event.toLowerCase(Locale.ROOT);
        if (value.contains("no_motion") || value.contains("no motion")
                || value.contains("لا توجد") || value.contains("quiet")
                || value.contains("idle")) {
            return false;
        }
        return value.contains("motion") || value.contains("حركة مكتشفة") || value.contains("حركة");
    }

    public static String displayEvent(String event) {
        return isMotion(event) ? "Motion" : "Quiet";
    }

    public static Insight analyze(List<MovementLog> logs) {
        Insight insight = new Insight();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Calendar now = Calendar.getInstance();
        String todayKey = dayKey(now);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) now.clone();
            day.add(Calendar.DAY_OF_YEAR, i - 6);
            insight.last7Labels[i] = new SimpleDateFormat("EEE", Locale.US).format(day.getTime());
        }

        for (MovementLog log : logs) {
            Date date = parse(format, log.dateTime);
            if (date == null) continue;

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            boolean motion = isMotion(log.event);

            if (motion) {
                insight.totalMotions++;
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                insight.hourly[hour]++;
                if (dayKey(cal).equals(todayKey)) {
                    insight.todayMotions++;
                }
                int dayIndex = daysAgo(now, cal);
                if (dayIndex >= 0 && dayIndex < 7) {
                    insight.last7Days[6 - dayIndex]++;
                }
            } else {
                insight.totalIdle++;
            }
        }

        insight.peakHour = indexOfMax(insight.hourly);
        insight.nextLikelyHour = nextPeakAfter(insight.hourly, currentHour);

        int hourScore = insight.hourly[currentHour];
        int neighbor = insight.hourly[(currentHour + 1) % 24];
        int peak = Math.max(1, insight.hourly[insight.peakHour]);
        float pattern = (hourScore * 0.7f + neighbor * 0.3f) / peak;
        float recencyBoost = insight.todayMotions > 0 ? Math.min(0.2f, insight.todayMotions * 0.03f) : 0f;
        insight.riskPercent = Math.min(96, Math.round((pattern * 0.85f + recencyBoost) * 100));

        if (insight.totalMotions == 0) {
            insight.riskPercent = 0;
            insight.riskLevel = "Low";
            insight.summary = "Link the sensor.";
            return insight;
        }

        if (insight.riskPercent >= 65) {
            insight.riskLevel = "High";
        } else if (insight.riskPercent >= 35) {
            insight.riskLevel = "Mid";
        } else {
            insight.riskLevel = "Low";
        }

        insight.summary = "Peak " + formatHour(insight.peakHour)
                + ". Next " + formatHour(insight.nextLikelyHour)
                + ". Chance " + insight.riskPercent + "%.";
        return insight;
    }

    private static Date parse(SimpleDateFormat format, String value) {
        try {
            return format.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String dayKey(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
    }

    private static int daysAgo(Calendar now, Calendar then) {
        long diff = now.getTimeInMillis() - then.getTimeInMillis();
        return (int) (diff / (24L * 60 * 60 * 1000));
    }

    private static int indexOfMax(int[] values) {
        int maxIndex = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[maxIndex]) maxIndex = i;
        }
        return maxIndex;
    }

    private static int nextPeakAfter(int[] hourly, int currentHour) {
        int bestHour = (currentHour + 1) % 24;
        int best = -1;
        for (int offset = 1; offset <= 24; offset++) {
            int hour = (currentHour + offset) % 24;
            if (hourly[hour] > best) {
                best = hourly[hour];
                bestHour = hour;
            }
        }
        return bestHour;
    }

    public static String formatHour(int hour) {
        return String.format(Locale.US, "%02d:00", hour);
    }

    public static WeeklyReport buildWeeklyReport(List<MovementLog> logs) {
        Insight insight = analyze(logs);
        WeeklyReport report = new WeeklyReport();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE d MMM", Locale.US);
        SimpleDateFormat shortDate = new SimpleDateFormat("d MMM", Locale.US);

        Calendar end = Calendar.getInstance();
        Calendar start = (Calendar) end.clone();
        start.add(Calendar.DAY_OF_YEAR, -6);
        report.periodLabel = shortDate.format(start.getTime()) + " — " + shortDate.format(end.getTime());

        int weekMotions = 0;
        for (int count : insight.last7Days) weekMotions += count;
        report.weekMotions = weekMotions;
        report.weekIdle = Math.max(0, insight.totalIdle);
        report.peakHour = insight.peakHour;
        report.riskPercent = insight.riskPercent;
        report.riskLevel = insight.riskLevel;

        int busyIndex = indexOfMax(insight.last7Days);
        int quietIndex = indexOfMin(insight.last7Days);
        report.busiestDay = insight.last7Labels[busyIndex] + " (" + insight.last7Days[busyIndex] + ")";
        report.quietestDay = insight.last7Labels[quietIndex] + " (" + insight.last7Days[quietIndex] + ")";

        StringBuilder daysLine = new StringBuilder();
        for (int i = 0; i < insight.last7Days.length; i++) {
            if (i > 0) daysLine.append("  ·  ");
            daysLine.append(insight.last7Labels[i]).append(" ").append(insight.last7Days[i]);
        }

        if (weekMotions == 0) {
            report.outlook = "No motion this week.";
            report.notificationText = "Weekly: no motion yet.";
            report.fullText = "Weekly note\n"
                    + "Period: " + report.periodLabel + "\n\n"
                    + report.outlook;
            return report;
        }

        int nextStart = insight.peakHour;
        int nextEnd = (insight.peakHour + 2) % 24;
        report.outlook = formatHour(nextStart) + "–" + formatHour(nextEnd)
                + ". " + insight.riskLevel + " (" + insight.riskPercent + "%).";

        report.notificationText = "Weekly: " + weekMotions + " motion. Peak "
                + formatHour(insight.peakHour) + ".";

        report.fullText = "Weekly note\n"
                + "Period: " + report.periodLabel + "\n"
                + "Date: " + dayFormat.format(end.getTime()) + "\n\n"
                + "This week\n"
                + "• Motion: " + weekMotions + "\n"
                + "• Busy: " + report.busiestDay + "\n"
                + "• Quiet: " + report.quietestDay + "\n"
                + "• Peak: " + formatHour(insight.peakHour) + "\n\n"
                + "Days\n"
                + daysLine + "\n\n"
                + "Outlook\n"
                + "• " + insight.riskLevel + " (" + insight.riskPercent + "%)\n"
                + "• Next: " + formatHour(insight.nextLikelyHour) + "\n"
                + "• " + report.outlook;
        return report;
    }

    private static int indexOfMin(int[] values) {
        int minIndex = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[minIndex]) minIndex = i;
        }
        return minIndex;
    }

    public static class WeeklyReport {
        public String periodLabel;
        public int weekMotions;
        public int weekIdle;
        public String busiestDay;
        public String quietestDay;
        public int peakHour;
        public int riskPercent;
        public String riskLevel;
        public String outlook;
        public String fullText;
        public String notificationText;
    }
}
