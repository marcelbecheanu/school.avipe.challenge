package com.avipe.screens;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.avipe.R;
import com.avipe.screens.Authentication.LoginScreen;
import com.avipe.screens.SliderScreen.SliderScreen;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        LottieAnimationView animationView = findViewById(R.id.circle);
        ImageView circle = findViewById(R.id.circle_white);
        ImageView logo = findViewById(R.id.circle_logo);

        animationView.setVisibility(View.INVISIBLE);
        circle.setVisibility(View.INVISIBLE);
        logo.setVisibility(View.INVISIBLE);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.statusbar_splash));


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationView.setVisibility(View.VISIBLE);
                logo.setVisibility(View.VISIBLE);
                circle.setVisibility(View.VISIBLE);
            }
        }, 1000);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                boolean saved = sharedPref.getBoolean("saved", false);
                if(saved){
                    Intent mainIntent = new Intent(SplashScreen.this, LoginScreen.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }else{
                    Intent mainIntent = new Intent(SplashScreen.this, SliderScreen.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }
            }
        }, 1500);
    }
}