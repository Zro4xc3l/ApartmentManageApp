package com.example.apartmentmanageapp;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            Log.d("FirebaseInit", "Firebase initialized successfully.");
        } else {
            Log.e("FirebaseInit", "Firebase initialization failed!");
        }
    }
}
