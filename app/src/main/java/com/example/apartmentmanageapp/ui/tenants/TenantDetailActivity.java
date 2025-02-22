package com.example.apartmentmanageapp.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                String firstName = documentSnapshot.getString("first_name");
                String lastName = documentSnapshot.getString("last_name");
                tenantName.setText(firstName + " " + lastName);
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

                // Retrieve numeric values and convert them to String
                Long rentAmountValue = documentSnapshot.getLong("rentAmount");
                rentAmount.setText("Rent Amount: " + (rentAmountValue != null ? rentAmountValue.toString() : "N/A"));

                Long depositAmountValue = documentSnapshot.getLong("depositAmount");
                depositAmount.setText("Deposit: " + (depositAmountValue != null ? depositAmountValue.toString() : "N/A"));

                Long keyAmountValue = documentSnapshot.getLong("keyAmount");
                keyAmount.setText("Keys Given: " + (keyAmountValue != null ? keyAmountValue.toString() : "N/A"));

                Long keycardAmountValue = documentSnapshot.getLong("keycardAmount");
                keycardAmount.setText("Keycards Given: " + (keycardAmountValue != null ? keycardAmountValue.toString() : "N/A"));
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
        // First retrieve the tenant document to get the full tenant name
        DocumentReference tenantRef = db.collection("tenants").document(tenantId);
        tenantRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("first_name");
                String lastName = documentSnapshot.getString("last_name");
                final String tenantFullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                // Show confirmation dialog before deletion
                new AlertDialog.Builder(this)
                        .setTitle("Delete Tenant")
                        .setMessage("Are you sure you want to delete tenant: " + tenantFullName + "? This action cannot be undone.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Delete the tenant document
                            tenantRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // After deletion, update bills to remove tenant references
                                        removeTenantReferencesFromBills(tenantFullName);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to delete tenant", Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error deleting tenant", e);
                                    });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(this, "Tenant not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error retrieving tenant", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error retrieving tenant", e);
        });
    }

    private void removeTenantReferencesFromBills(String tenantFullName) {
        // Query all bills to find references in unitBills arrays
        db.collection("bills").get().addOnSuccessListener(querySnapshot -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot billDoc : querySnapshot.getDocuments()) {
                // Get the unitBills array from this bill document
                List<Map<String, Object>> unitBills = (List<Map<String, Object>>) billDoc.get("unitBills");
                if (unitBills != null) {
                    // Create a new list filtering out entries with the matching tenantName
                    List<Map<String, Object>> updatedUnitBills = new ArrayList<>();
                    for (Map<String, Object> unitBill : unitBills) {
                        String tName = (String) unitBill.get("tenantName");
                        // Only add the entry if it does NOT match the deleted tenant's full name
                        if (tName == null || !tName.equals(tenantFullName.trim())) {
                            updatedUnitBills.add(unitBill);
                        }
                    }
                    // Only update if there is a difference
                    if (updatedUnitBills.size() != unitBills.size()) {
                        batch.update(billDoc.getReference(), "unitBills", updatedUnitBills);
                    }
                }
            }
            // Commit all updates at once
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tenant and references deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Tenant deleted but failed to update bills", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Tenant deleted but failed to query bills", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

}
