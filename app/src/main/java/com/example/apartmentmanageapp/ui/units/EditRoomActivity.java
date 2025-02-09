package com.example.apartmentmanageapp.ui.units;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditRoomActivity extends AppCompatActivity {

    private EditText etRoomNumber, etRoomSize, etFloorLevel, etRentAmount, etAmenitiesList;
    private Button btnSaveChanges, btnCancel;
    private String unitId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);

        db = FirebaseFirestore.getInstance();
        initializeViews();

        // Get unitId from the Intent
        unitId = getIntent().getStringExtra("unitId");
        if (unitId == null) {
            Log.e("EditRoomActivity", "unitId is missing!");
            Toast.makeText(this, "Unit ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.d("EditRoomActivity", "unitId: " + unitId);
        }

        // Instead of relying solely on Intent extras, load updated data from Firestore.
        loadRoomDetailsFromFirestore();
        setupListeners();
    }

    private void initializeViews() {
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etRoomSize = findViewById(R.id.etRoomSize);
        etFloorLevel = findViewById(R.id.etFloorLevel);
        etRentAmount = findViewById(R.id.etRentAmount);
        etAmenitiesList = findViewById(R.id.etAmenitiesList);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
    }

    /**
     * Fetches the current room details from Firestore and populates the EditText fields.
     */
    private void loadRoomDetailsFromFirestore() {
        db.collection("units")
                .document(unitId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String roomNumber = documentSnapshot.getString("unitNumber");
                        String roomSize = documentSnapshot.getString("roomSize");
                        String floorLevel = documentSnapshot.getString("floorLevel");
                        Double rentAmountVal = documentSnapshot.getDouble("rentAmount");
                        String amenities = documentSnapshot.getString("amenities");

                        etRoomNumber.setText(roomNumber != null ? roomNumber : "");
                        etRoomSize.setText(roomSize != null ? roomSize : "");
                        etFloorLevel.setText(floorLevel != null ? floorLevel : "");
                        etRentAmount.setText(rentAmountVal != null ? String.valueOf(rentAmountVal) : "");
                        etAmenitiesList.setText(amenities != null ? amenities : "");
                    } else {
                        Toast.makeText(EditRoomActivity.this, "Room not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRoomActivity.this, "Error loading room details", Toast.LENGTH_SHORT).show();
                    Log.e("EditRoomActivity", "Error fetching room details", e);
                });
    }

    private void setupListeners() {
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoomDetails();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity without saving changes
            }
        });
    }

    private void saveRoomDetails() {
        // Get updated values from the EditTexts
        String roomNumber = etRoomNumber.getText().toString().trim();
        String roomSize = etRoomSize.getText().toString().trim();
        String floorLevel = etFloorLevel.getText().toString().trim();
        String rentAmountStr = etRentAmount.getText().toString().trim();
        String amenities = etAmenitiesList.getText().toString().trim();

        double rentAmount = 0.0;
        try {
            if (!rentAmountStr.isEmpty()) {
                rentAmount = Double.parseDouble(rentAmountStr);
            }
        } catch (NumberFormatException e) {
            Log.e("EditRoomActivity", "Invalid rent amount format: " + rentAmountStr, e);
            Toast.makeText(this, "Invalid rent amount. Using 0 as default.", Toast.LENGTH_SHORT).show();
        }

        // Build a map of room details to update in Firestore
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("unitNumber", roomNumber);
        roomData.put("roomSize", roomSize);
        roomData.put("floorLevel", floorLevel);
        roomData.put("rentAmount", rentAmount);
        roomData.put("amenities", amenities);

        db.collection("units")
                .document(unitId)
                .set(roomData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditRoomActivity.this, "Room details saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRoomActivity.this, "Failed to save room details.", Toast.LENGTH_SHORT).show();
                    Log.e("EditRoomActivity", "Error saving data", e);
                });
    }
}
