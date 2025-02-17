package com.example.apartmentmanageapp.ui.units;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Unit;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddUnitActivity extends AppCompatActivity {

    private EditText unitNumberInput, rentAmountInput, roomSizeInput, floorLevelInput, amenitiesInput, electricMeterInput, waterMeterInput;
    private Button saveUnitButton, cancelUnitButton;
    private FirebaseFirestore db;
    private String propertyId; // Property ID for reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unit);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get propertyId from Intent
        propertyId = getIntent().getStringExtra("propertyId");

        // Debug log to check if propertyId is received
        Log.d("AddUnitActivity", "Property ID: " + propertyId);

        if (propertyId == null || propertyId.isEmpty()) {
            Toast.makeText(this, "Error: Property ID is missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Views
        unitNumberInput = findViewById(R.id.edit_unit_number);
        rentAmountInput = findViewById(R.id.edit_rent_rate);
        roomSizeInput = findViewById(R.id.edit_room_size);
        floorLevelInput = findViewById(R.id.edit_floor_level);
        amenitiesInput = findViewById(R.id.edit_amenities);
        electricMeterInput = findViewById(R.id.edit_electric_meter);
        waterMeterInput = findViewById(R.id.edit_water_meter);
        saveUnitButton = findViewById(R.id.button_save);
        cancelUnitButton = findViewById(R.id.button_cancel);

        // Save button listener
        saveUnitButton.setOnClickListener(v -> addNewUnit());

        // Cancel button listener
        cancelUnitButton.setOnClickListener(v -> finish());
    }

    private void addNewUnit() {
        // Collect input fields
        String unitNumber = unitNumberInput.getText().toString().trim();
        String rentAmountStr = rentAmountInput.getText().toString().trim();
        String roomSize = roomSizeInput.getText().toString().trim();
        String floorLevel = floorLevelInput.getText().toString().trim();
        String amenitiesStr = amenitiesInput.getText().toString().trim();
        String electricMeterStr = electricMeterInput.getText().toString().trim();
        String waterMeterStr = waterMeterInput.getText().toString().trim();

        // Validate required fields (all fields are required except amenities)
        if (unitNumber.isEmpty() ||
                rentAmountStr.isEmpty() ||
                roomSize.isEmpty() ||
                floorLevel.isEmpty() ||
                electricMeterStr.isEmpty() ||
                waterMeterStr.isEmpty()) {
            Toast.makeText(this, "All fields are required except amenities!", Toast.LENGTH_SHORT).show();
            return;
        }

        double rentAmount, electricMeter, waterMeter;
        try {
            rentAmount = Double.parseDouble(rentAmountStr);
            electricMeter = Double.parseDouble(electricMeterStr);
            waterMeter = Double.parseDouble(waterMeterStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric input!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rentAmount <= 0) {
            Toast.makeText(this, "Rent amount must be positive.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert amenities input into a List<String> (amenities is optional)
        List<String> amenitiesList = amenitiesStr.isEmpty() ? new ArrayList<>() : Arrays.asList(amenitiesStr.split("\\s*,\\s*")); // Trim spaces

        // Debugging log before saving to Firestore
        Log.d("AddUnitActivity", "Preparing to save unit: " + unitNumber);

        // Create the Unit object
        Unit newUnit = new Unit(
                unitNumber,       // Unit ID
                propertyId,       // Property Reference
                rentAmount,
                false,            // isOccupied (default: false)
                "No Tenant",      // Default tenant name
                "Unpaid",         // Default rent status
                roomSize,
                floorLevel,
                amenitiesList,    // List of amenities (optional)
                electricMeter,    // Initial Electricity Meter
                waterMeter        // Initial Water Meter
        );

        // Save to Firestore using propertyId as collection
        DocumentReference propertyRef = db.collection("properties").document(propertyId);
        propertyRef.collection("units").document(unitNumber)
                .set(newUnit)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AddUnitActivity", "Unit saved successfully: " + unitNumber);
                    // Increment unitCount in the properties collection
                    incrementUnitCount(propertyRef);
                })
                .addOnFailureListener(e -> {
                    Log.e("AddUnitActivity", "Failed to save unit: " + e.getMessage());
                    Toast.makeText(this, "Failed to add unit", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Method to increment `unitCount` in the property document.
     */
    private void incrementUnitCount(DocumentReference propertyRef) {
        propertyRef.update("unitCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("AddUnitActivity", "Unit count updated successfully.");
                    Toast.makeText(this, "Unit added successfully!", Toast.LENGTH_SHORT).show();
                    navigateToUnitList(); // Navigate back to unit list
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update unit count", Toast.LENGTH_SHORT).show());
    }

    /**
     * Method to navigate back to the unit list.
     */
    private void navigateToUnitList() {
        Intent intent = new Intent(this, UnitsActivity.class);
        intent.putExtra("propertyId", propertyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
