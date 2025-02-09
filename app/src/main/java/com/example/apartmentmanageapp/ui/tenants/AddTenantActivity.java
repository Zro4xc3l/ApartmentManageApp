package com.example.apartmentmanageapp.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;

public class AddTenantActivity extends AppCompatActivity {

    private EditText tenantName, unitNumber, phoneNumber;
    private RadioGroup rentStatusGroup;
    private Button saveButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        tenantName = findViewById(R.id.edit_tenant_name);
        unitNumber = findViewById(R.id.edit_unit_number);
        phoneNumber = findViewById(R.id.edit_phone_number);
        rentStatusGroup = findViewById(R.id.rent_status_group);
        saveButton = findViewById(R.id.button_save);
        cancelButton = findViewById(R.id.button_cancel);

        // Handle Save Button Click
        saveButton.setOnClickListener(v -> saveTenant());

        // Handle Cancel Button Click
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveTenant() {
        String name = tenantName.getText().toString().trim();
        String unit = unitNumber.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();

        int selectedId = rentStatusGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String rentStatus = (selectedRadioButton != null) ? selectedRadioButton.getText().toString() : "Unpaid";

        if (name.isEmpty() || unit.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send data back to TenantsFragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("unit", unit);
        resultIntent.putExtra("status", rentStatus);
        resultIntent.putExtra("phone", phone);
        setResult(RESULT_OK, resultIntent);

        // Close AddTenantActivity and go back to Tenant Page
        finish();
    }
}
