package com.example.apartmentmanageapp.ui.units;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Unit;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddUnitActivity extends AppCompatActivity {
    private EditText unitNumberInput, rentAmountInput;
    private Button saveUnitButton;
    private FirebaseFirestore db;
    private String propertyId; // Property ID to associate the unit with the property

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unit);

        unitNumberInput = findViewById(R.id.unit_number_input);
        rentAmountInput = findViewById(R.id.rent_amount_input);
        saveUnitButton = findViewById(R.id.save_unit_button);
        db = FirebaseFirestore.getInstance();

        // Get propertyId from Intent
        propertyId = getIntent().getStringExtra("propertyId");

        saveUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUnit();
            }
        });
    }

    private void addNewUnit() {
        String unitNumber = unitNumberInput.getText().toString().trim();
        String rentAmountStr = rentAmountInput.getText().toString().trim();

        if (unitNumber.isEmpty() || rentAmountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double rentAmount = Double.parseDouble(rentAmountStr);
        Unit newUnit = new Unit(null, propertyId, unitNumber, rentAmount, false, "No Tenant", "Unpaid");

        db.collection("units").add(newUnit)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Unit added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after adding
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add unit", Toast.LENGTH_SHORT).show());
    }
}
