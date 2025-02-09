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
            Log.d("MyApplication", "Firebase Initialized Successfully");
        } else {
            Log.e("MyApplication", "Firebase Initialization Failed");
        }
    }
}
