package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openHome = this::goHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView image = findViewById(R.id.splashImage);
        View brand = findViewById(R.id.splashBrand);
        View rule = findViewById(R.id.splashRule);

        image.setScaleX(1.12f);
        image.setScaleY(1.12f);
        image.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(2400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        brand.setAlpha(0f);
        brand.setTranslationY(28f);
        brand.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(280)
                .setDuration(700)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        rule.setScaleX(0f);
        rule.animate()
                .scaleX(1f)
                .setStartDelay(360)
                .setDuration(480)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        handler.postDelayed(openHome, 2100);
    }

    private void goHome() {
        View root = findViewById(android.R.id.content);
        root.animate()
                .alpha(0f)
                .setDuration(280)
                .withEndAction(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                    finish();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openHome);
        super.onDestroy();
    }
}
