package com.example.apartmentmanageapp.ui.properties;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Property;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPropertyActivity extends AppCompatActivity {

    private EditText propertyName, propertyAddress;
    private Button saveButton, cancelButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        propertyName = findViewById(R.id.edit_property_name);
        propertyAddress = findViewById(R.id.edit_property_address);
        saveButton = findViewById(R.id.button_save);
        cancelButton = findViewById(R.id.button_cancel);

        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveProperty());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveProperty() {
        String name = propertyName.getText().toString().trim();
        String address = propertyAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Property property = new Property(name, address, 0); // Default unit count to 0

        db.collection("properties").add(property)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddPropertyActivity.this, "Property added!", Toast.LENGTH_SHORT).show();

                    // Send result back to PropertiesFragment
                    setResult(RESULT_OK, new Intent());
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Failed to add property", Toast.LENGTH_SHORT).show());
    }
}
