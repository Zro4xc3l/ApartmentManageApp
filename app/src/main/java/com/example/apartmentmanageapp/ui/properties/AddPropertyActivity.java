package com.example.apartmentmanageapp.ui.properties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPropertyActivity extends AppCompatActivity {

    private EditText propertyName, propertyAddress, electricityRate, waterRate, serviceFee, minElectricPrice, minWaterPrice;
    private Button saveButton, cancelButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        propertyName = findViewById(R.id.edit_property_name);
        propertyAddress = findViewById(R.id.edit_property_address);
        electricityRate = findViewById(R.id.edit_electricity_rate);
        waterRate = findViewById(R.id.edit_water_rate);
        serviceFee = findViewById(R.id.edit_service_fee);
        minElectricPrice = findViewById(R.id.edit_min_electricity_price);
        minWaterPrice = findViewById(R.id.edit_min_water_price);

        saveButton = findViewById(R.id.button_save);
        cancelButton = findViewById(R.id.button_cancel);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        saveButton.setOnClickListener(v -> saveProperty());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveProperty() {
        String name = propertyName.getText().toString().trim();
        String address = propertyAddress.getText().toString().trim();
        String electricityRateInput = electricityRate.getText().toString().trim();
        String waterRateInput = waterRate.getText().toString().trim();
        String serviceFeeInput = serviceFee.getText().toString().trim();
        String minElectricInput = minElectricPrice.getText().toString().trim();
        String minWaterInput = minWaterPrice.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || electricityRateInput.isEmpty() || waterRateInput.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double electricityRateValue, waterRateValue, serviceFeeValue, minElectricValue, minWaterValue;
        try {
            electricityRateValue = Double.parseDouble(electricityRateInput);
            waterRateValue = Double.parseDouble(waterRateInput);
            serviceFeeValue = serviceFeeInput.isEmpty() ? 0.0 : Double.parseDouble(serviceFeeInput);
            minElectricValue = minElectricInput.isEmpty() ? 0.0 : Double.parseDouble(minElectricInput);
            minWaterValue = minWaterInput.isEmpty() ? 0.0 : Double.parseDouble(minWaterInput);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric input", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Get the logged-in user's ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String ownerId = currentUser.getUid(); // ✅ Store the property under the logged-in owner

        // ✅ Prepare Property Data
        Map<String, Object> propertyData = new HashMap<>();
        propertyData.put("name", name);
        propertyData.put("address", address);
        propertyData.put("unitCount", 0);
        propertyData.put("electricityRate", electricityRateValue);
        propertyData.put("waterRate", waterRateValue);
        propertyData.put("serviceFee", serviceFeeValue);
        propertyData.put("minElectricPrice", minElectricValue);
        propertyData.put("minWaterPrice", minWaterValue);
        propertyData.put("ownerId", ownerId); // ✅ Attach Owner ID

        // ✅ Save property to Firestore (Firestore automatically assigns an ID)
        db.collection("properties")
                .add(propertyData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Property added with ID: " + documentReference.getId());
                    Toast.makeText(AddPropertyActivity.this, "Property added!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent());
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to add property", e);
                    Toast.makeText(AddPropertyActivity.this, "Failed to add property", Toast.LENGTH_SHORT).show();
                });
    }
}
