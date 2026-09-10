package com.example.myapplication;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public final class MotionFx {

    private MotionFx() {
    }

    public static void fadeUp(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(22f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(420)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();
    }

    public static void stagger(long startDelay, View... views) {
        long delay = startDelay;
        for (View view : views) {
            fadeUp(view, delay);
            delay += 75;
        }
    }

    public static void press(View view, Runnable action) {
        if (view == null) return;
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(90).start();
                    if (action != null) action.run();
                })
                .start();
    }

    public static void startPulse(View view) {
        if (view == null) return;
        stopPulse(view);
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.45f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.45f)
        );
        pulse.setDuration(720);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        view.setTag(R.id.tag_pulse, pulse);
        pulse.start();
    }

    public static void stopPulse(View view) {
        if (view == null) return;
        Object tag = view.getTag(R.id.tag_pulse);
        if (tag instanceof Animator) {
            ((Animator) tag).cancel();
        }
        view.setTag(R.id.tag_pulse, null);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    public static void burst(View view) {
        if (view == null) return;
        stopPulse(view);
        view.animate()
                .scaleX(1.85f)
                .scaleY(1.85f)
                .setDuration(140)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(260)
                        .withEndAction(() -> startPulse(view))
                        .start())
                .start();
    }

    public static void countTo(TextView view, int target) {
        if (view == null) return;
        if (target <= 0) {
            view.setText("0");
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(480);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim ->
                view.setText(String.valueOf(anim.getAnimatedValue())));
        animator.start();
    }
}
