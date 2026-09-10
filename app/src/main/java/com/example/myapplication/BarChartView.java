package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class BarChartView extends View {

    private int[] values = new int[24];
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.cyan_dim));
        highlightPaint.setColor(ContextCompat.getColor(getContext(), R.color.cyan));
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.chart_grid));
        gridPaint.setStrokeWidth(1.5f);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValues(int[] hourly) {
        this.values = hourly != null ? hourly : new int[24];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPaint.setTextSize(sp(10));
        int count = values.length;
        if (count == 0) return;

        float padL = dp(8);
        float padR = dp(8);
        float padT = dp(10);
        float padB = dp(28);
        float chartW = getWidth() - padL - padR;
        float chartH = getHeight() - padT - padB;
        int max = 1;
        int peak = 0;
        for (int i = 0; i < count; i++) {
            if (values[i] > max) {
                max = values[i];
                peak = i;
            }
        }

        for (int i = 1; i <= 3; i++) {
            float y = padT + chartH * (1f - i / 3f);
            canvas.drawLine(padL, y, getWidth() - padR, y, gridPaint);
        }

        float slot = chartW / count;
        float barW = slot * 0.62f;
        for (int i = 0; i < count; i++) {
            float ratio = values[i] / (float) max;
            float h = Math.max(dp(3), chartH * ratio);
            float left = padL + i * slot + (slot - barW) / 2f;
            barRect.set(left, padT + chartH - h, left + barW, padT + chartH);
            canvas.drawRoundRect(barRect, dp(4), dp(4), i == peak && values[i] > 0 ? highlightPaint : barPaint);
            if (i % 3 == 0) {
                canvas.drawText(String.valueOf(i), left + barW / 2f, getHeight() - dp(8), textPaint);
            }
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
