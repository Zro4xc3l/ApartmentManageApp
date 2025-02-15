package com.example.apartmentmanageapp.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TenantDetailActivity extends AppCompatActivity {

    private TextView tenantName, citizenId, propertyName, unitNumber, phoneNumber, email,
            leaseStart, leaseEnd, moveInDate, emergencyContact, emergencyPhone,
            rentAmount, depositAmount, keyAmount, keycardAmount;
    private Button editButton, deleteButton;
    private FirebaseFirestore db;
    private String tenantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get tenant ID from Intent
        tenantId = getIntent().getStringExtra("tenantId");
        if (tenantId == null || tenantId.isEmpty()) {
            Toast.makeText(this, "Invalid tenant ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        tenantName = findViewById(R.id.text_tenant_name);
        citizenId = findViewById(R.id.text_citizen_id);
        propertyName = findViewById(R.id.text_property_name);
        unitNumber = findViewById(R.id.text_unit_number);
        phoneNumber = findViewById(R.id.text_phone_number);
        email = findViewById(R.id.text_email);
        leaseStart = findViewById(R.id.text_lease_start);
        leaseEnd = findViewById(R.id.text_lease_end);
        moveInDate = findViewById(R.id.text_move_in_date);
        emergencyContact = findViewById(R.id.text_emergency_contact);
        emergencyPhone = findViewById(R.id.text_emergency_phone);
        rentAmount = findViewById(R.id.text_rent_amount);
        depositAmount = findViewById(R.id.text_deposit_amount);
        keyAmount = findViewById(R.id.text_key_amount);
        keycardAmount = findViewById(R.id.text_keycard_amount);
        editButton = findViewById(R.id.button_edit_tenant);
        deleteButton = findViewById(R.id.button_delete_tenant);

        // Load tenant details from Firestore
        loadTenantDetails();

        // Set button listeners
        editButton.setOnClickListener(v -> editTenant());
        deleteButton.setOnClickListener(v -> deleteTenant());
    }

    private void loadTenantDetails() {
        DocumentReference tenantRef = db.collection("tenants").document(tenantId);
        tenantRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Display all tenant information including lease details
                tenantName.setText(documentSnapshot.getString("first_name") + " " + documentSnapshot.getString("last_name"));
                citizenId.setText("Citizen ID: " + documentSnapshot.getString("citizen_id"));
                propertyName.setText("Property: " + documentSnapshot.getString("property"));
                unitNumber.setText("Unit: " + documentSnapshot.getString("unit"));
                phoneNumber.setText("Phone: " + documentSnapshot.getString("phone_number"));
                email.setText("Email: " + documentSnapshot.getString("email"));
                moveInDate.setText("Move-in Date: " + documentSnapshot.getString("move_in_date"));
                leaseStart.setText("Lease Start: " + documentSnapshot.getString("lease_start_date"));
                leaseEnd.setText("Lease End: " + documentSnapshot.getString("lease_end_date"));
                emergencyContact.setText("Emergency Contact: " + documentSnapshot.getString("emergency_contact_name"));
                emergencyPhone.setText("Emergency Phone: " + documentSnapshot.getString("emergency_contact_phone"));
                rentAmount.setText("Rent Amount: " + documentSnapshot.getString("rentAmount"));
                depositAmount.setText("Deposit: " + documentSnapshot.getString("depositAmount"));
                keyAmount.setText("Keys Given: " + documentSnapshot.getString("keyAmount"));
                keycardAmount.setText("Keycards Given: " + documentSnapshot.getString("keycardAmount"));

            } else {
                Toast.makeText(this, "Tenant not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error loading tenant details", e);
            Toast.makeText(this, "Failed to load tenant details", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void editTenant() {
        Intent intent = new Intent(this, EditTenantActivity.class);
        intent.putExtra("tenantId", tenantId);
        startActivity(intent);
    }

    private void deleteTenant() {
        db.collection("tenants").document(tenantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting tenant", e);
                    Toast.makeText(this, "Failed to delete tenant", Toast.LENGTH_SHORT).show();
                });
    }
}
