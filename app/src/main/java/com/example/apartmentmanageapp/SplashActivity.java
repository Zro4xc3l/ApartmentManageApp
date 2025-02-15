package com.example.apartmentmanageapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Optionally, set a simple splash layout.
        setContentView(R.layout.activity_splash);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is logged in, redirect to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Not logged in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
