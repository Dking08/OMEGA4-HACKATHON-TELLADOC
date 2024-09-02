package com.dking.telladoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static final int SPLASH_TIME_OUT = 1000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
//        new Handler().postDelayed(() -> {
//            if (isUserLoggedIn()) {
//                Intent mainIntent = new Intent(SplashScreen.this, Interface.class);
//                startActivity(mainIntent);
//            } else {
//                Intent loginIntent = new Intent(SplashScreen.this, Login.class);
//                startActivity(loginIntent);
//            }
//            finish();
//        }, SPLASH_TIME_OUT);
        new Handler().postDelayed(this::checkUserStatus, SPLASH_TIME_OUT);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("TellADocPrefs", MODE_PRIVATE);
            String role = sharedPreferences.getString("userRole", "");

            if (!role.isEmpty()) {
                redirectToAppropriateInterface(role);
            } else {
                startActivity(new Intent(SplashScreen.this, Login.class));
                finish();
            }

        } else {
            // User is not logged in, redirect to login screen
            startActivity(new Intent(SplashScreen.this, Login.class));
            finish();
        }
    }

    private void redirectToAppropriateInterface(String role) {
        Log.d("SPLASH KID", "THE ROLE:  "+ role);
        if (role.equals("Doc")) {
            startActivity(new Intent(SplashScreen.this, MainDocInterface.class));
        } else {
            startActivity(new Intent(SplashScreen.this, Interface.class));
        }
        finish();
    }


    private boolean isUserLoggedIn() {

        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}
