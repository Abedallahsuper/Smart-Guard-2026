package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class DonutChartView extends View {

    private float motionValue = 0f;
    private float idleValue = 0f;
    private final Paint motionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint idlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        motionPaint.setStyle(Paint.Style.STROKE);
        motionPaint.setStrokeCap(Paint.Cap.ROUND);
        motionPaint.setColor(ContextCompat.getColor(getContext(), R.color.rose));

        idlePaint.setStyle(Paint.Style.STROKE);
        idlePaint.setStrokeCap(Paint.Cap.ROUND);
        idlePaint.setColor(ContextCompat.getColor(getContext(), R.color.mint));

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.stroke));

        titlePaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        subtitlePaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        legendPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
    }

    public void setValues(int motion, int idle) {
        this.motionValue = motion;
        this.idleValue = idle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = Math.min(getWidth(), getHeight()) * 0.085f;
        motionPaint.setStrokeWidth(stroke);
        idlePaint.setStrokeWidth(stroke);
        trackPaint.setStrokeWidth(stroke);
        titlePaint.setTextSize(sp(22));
        subtitlePaint.setTextSize(sp(12));
        legendPaint.setTextSize(sp(12));

        float size = Math.min(getWidth(), getHeight()) - stroke * 3.2f;
        float left = (getWidth() - size) / 2f;
        float top = stroke;
        arcBounds.set(left, top, left + size, top + size);

        canvas.drawArc(arcBounds, 0, 360, false, trackPaint);

        float total = motionValue + idleValue;
        if (total <= 0) {
            canvas.drawText("No data", getWidth() / 2f, arcBounds.centerY(), titlePaint);
            canvas.drawText("Link sensor", getWidth() / 2f, arcBounds.centerY() + sp(18), subtitlePaint);
            return;
        }

        float motionSweep = 360f * (motionValue / total);
        canvas.drawArc(arcBounds, -90, motionSweep, false, motionPaint);
        if (idleValue > 0) {
            canvas.drawArc(arcBounds, -90 + motionSweep, 360f - motionSweep, false, idlePaint);
        }

        int percent = Math.round(100f * motionValue / total);
        canvas.drawText(percent + "%", getWidth() / 2f, arcBounds.centerY() - sp(2), titlePaint);
        canvas.drawText("Motion", getWidth() / 2f, arcBounds.centerY() + sp(16), subtitlePaint);

        float legendY = arcBounds.bottom + sp(22);
        legendPaint.setTextAlign(Paint.Align.LEFT);
        legendPaint.setColor(ContextCompat.getColor(getContext(), R.color.rose));
        canvas.drawCircle(getWidth() * 0.20f, legendY, 7, legendPaint);
        legendPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        canvas.drawText("Motion " + (int) motionValue, getWidth() * 0.24f, legendY + 5, legendPaint);

        legendPaint.setColor(ContextCompat.getColor(getContext(), R.color.mint));
        canvas.drawCircle(getWidth() * 0.56f, legendY, 7, legendPaint);
        legendPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        canvas.drawText("Quiet " + (int) idleValue, getWidth() * 0.60f, legendY + 5, legendPaint);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
