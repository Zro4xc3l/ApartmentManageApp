package com.example.apartmentmanageapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, fullNameEditText, phoneEditText, inviteCodeEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.editTextEmail);
        fullNameEditText = findViewById(R.id.editTextFullName);
        phoneEditText = findViewById(R.id.editTextPhone);
        inviteCodeEditText = findViewById(R.id.editTextInviteCode);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        registerButton = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String inviteCode = inviteCodeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || inviteCode.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyInviteCode(inviteCode, email, fullName, phone, password);
    }

    private void verifyInviteCode(String inviteCode, String email, String fullName, String phone, String password) {
        db.collection("inviteCodes").document(inviteCode).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getBoolean("canBeUsed")) {
                            createFirebaseUser(email, password, fullName, phone, inviteCode);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Invalid or already used invite code", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to verify invite code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createFirebaseUser(String email, String password, String fullName, String phone, String inviteCode) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, fullName, phone, inviteCode);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String fullName, String phone, String inviteCode) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("phoneNumber", phone);
        userData.put("role", "owner");  // ✅ Default role is "owner"

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    markInviteCodeAsUsed(inviteCode);
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show());
    }

    private void markInviteCodeAsUsed(String inviteCode) {
        db.collection("inviteCodes").document(inviteCode)
                .update("canBeUsed", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to mark invite code as used", Toast.LENGTH_SHORT).show());
    }
}
