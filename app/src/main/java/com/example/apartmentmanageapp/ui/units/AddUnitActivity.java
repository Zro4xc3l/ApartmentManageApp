package com.example.apartmentmanageapp.ui.units;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Unit;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddUnitActivity extends AppCompatActivity {

    private EditText unitNumberInput, rentAmountInput, roomSizeInput, floorLevelInput, amenitiesInput;
    private Button saveUnitButton, cancelUnitButton;
    private FirebaseFirestore db;
    private String propertyId; // (Optional) If your Unit references a property

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unit);

        // Initialize Views
        unitNumberInput = findViewById(R.id.etRoomNumber);
        rentAmountInput = findViewById(R.id.etRentAmount);
        roomSizeInput   = findViewById(R.id.etRoomSize);
        floorLevelInput = findViewById(R.id.etFloorLevel);
        amenitiesInput  = findViewById(R.id.etAmenities);
        saveUnitButton  = findViewById(R.id.btnSaveNewRm);
        // Find the cancel button (ID as defined in your XML)
        cancelUnitButton = findViewById(R.id.btnCancel);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get propertyId from Intent (if any)
        propertyId = getIntent().getStringExtra("propertyId");

        // Set up button click for saving
        saveUnitButton.setOnClickListener(v -> addNewUnit());

        // Set up cancel button to simply finish the activity
        cancelUnitButton.setOnClickListener(v -> finish());
    }

    private void addNewUnit() {
        // Collect input fields
        String unitNumber  = unitNumberInput.getText().toString().trim();
        String rentAmountStr = rentAmountInput.getText().toString().trim();
        String roomSize    = roomSizeInput.getText().toString().trim();
        String floorLevel  = floorLevelInput.getText().toString().trim();
        String amenitiesStr= amenitiesInput.getText().toString().trim();

        // Basic validation
        if (unitNumber.isEmpty() || rentAmountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in the room number and rent amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        double rentAmount;
        try {
            rentAmount = Double.parseDouble(rentAmountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid rent amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rentAmount <= 0) {
            Toast.makeText(this, "Rent amount must be positive.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use defaults if fields are empty
        String finalRoomSize    = roomSize.isEmpty() ? "Unknown" : roomSize;
        String finalFloorLevel  = floorLevel.isEmpty() ? "Unknown" : floorLevel;
        String finalAmenities   = amenitiesStr.isEmpty() ? "None" : amenitiesStr;

        // Create the Unit object (using room number as document ID)
        Unit newUnit = new Unit(
                unitNumber,       // Set the id as the room number
                propertyId,
                unitNumber,
                rentAmount,
                false,            // isOccupied
                "No Tenant",      // tenantName
                "Unpaid",         // rentStatus
                finalRoomSize,
                finalFloorLevel,
                finalAmenities
        );

        // Save to Firestore, using the room number as the document ID (if desired)
        db.collection("units")
                .document(unitNumber)
                .set(newUnit)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Unit added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after adding
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add unit", Toast.LENGTH_SHORT).show()
                );
    }
}
