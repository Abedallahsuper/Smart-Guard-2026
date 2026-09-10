package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SparklineView extends View {

    private int[] values = new int[7];
    private String[] labels = new String[]{"", "", "", "", "", "", ""};
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public SparklineView(Context context) {
        super(context);
        init();
    }

    public SparklineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(3));
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.cyan));
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.cyan));
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        textPaint.setTextAlign(Paint.Align.CENTER);
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.chart_grid));
        gridPaint.setStrokeWidth(1.5f);
    }

    public void setData(int[] weekly, String[] weekLabels) {
        this.values = weekly != null ? weekly : new int[7];
        if (weekLabels != null) this.labels = weekLabels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPaint.setTextSize(sp(11));
        int count = values.length;
        if (count < 2) return;

        float padL = dp(10);
        float padR = dp(10);
        float padT = dp(16);
        float padB = dp(30);
        float chartW = getWidth() - padL - padR;
        float chartH = getHeight() - padT - padB;
        int max = 1;
        for (int value : values) max = Math.max(max, value);

        for (int i = 1; i <= 3; i++) {
            float y = padT + chartH * (1f - i / 3f);
            canvas.drawLine(padL, y, getWidth() - padR, y, gridPaint);
        }

        fillPaint.setShader(new LinearGradient(
                0, padT,
                0, padT + chartH,
                ContextCompat.getColor(getContext(), R.color.cyan),
                0x001F7A46,
                Shader.TileMode.CLAMP
        ));

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < count; i++) {
            float x = padL + (chartW * i / (count - 1f));
            float y = padT + chartH - (chartH * values[i] / (float) max);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, padT + chartH);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(padL + chartW, padT + chartH);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < count; i++) {
            float x = padL + (chartW * i / (count - 1f));
            float y = padT + chartH - (chartH * values[i] / (float) max);
            canvas.drawCircle(x, y, dp(4.5f), dotPaint);
            String label = i < labels.length ? labels[i] : String.valueOf(i + 1);
            canvas.drawText(label, x, getHeight() - dp(8), textPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
