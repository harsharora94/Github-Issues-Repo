package com.example.harsh.testdemo;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by harsh on 1/21/2017.
 */

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        View view = findViewById(R.id.logo);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0.0f, 360f);
        animator.setDuration(2000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent mainIntent = new Intent(SplashScreenActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        },2500);
    }


    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        int orient = newConfig.orientation;
        switch (orient)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                break;
        }
    }

    protected void onPause()
    {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
