package com.example.apartmentmanageapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, skipButton;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (sharedPreferences.contains("user_id")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        skipButton = findViewById(R.id.buttonSkip);

        // Initialize database
        database = openOrCreateDatabase("AptManageDB", MODE_PRIVATE, null);
        createTablesIfNotExists();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    authenticateUser(email, password);
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void authenticateUser(String email, String password) {
        Cursor cursor = database.rawQuery("SELECT * FROM Users WHERE email = ? AND password = ?", new String[]{email, password});

        if (cursor.moveToFirst()) {
            String userId = cursor.getString(cursor.getColumnIndex("user_id"));
            String role = cursor.getString(cursor.getColumnIndex("role"));

            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_id", userId);
            editor.putString("role", role);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void createTablesIfNotExists() {
        database.execSQL("CREATE TABLE IF NOT EXISTS Users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, password TEXT, role TEXT);");
        // Insert dummy data if table is empty
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM Users", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            database.execSQL("INSERT INTO Users (email, password, role) VALUES ('owner@example.com', 'password', 'Owner');");
            database.execSQL("INSERT INTO Users (email, password, role) VALUES ('tenant@example.com', 'password', 'Tenant');");
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
}