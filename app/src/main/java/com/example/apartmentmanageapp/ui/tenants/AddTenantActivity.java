package com.example.apartmentmanageapp.ui.tenants;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.PropertySpinnerAdapter;
import com.example.apartmentmanageapp.adapters.UnitSpinnerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class AddTenantActivity extends AppCompatActivity {

    private FrameLayout stepsContainer;
    private View stepPersonalInfo, stepDocumentation, stepLeaseDetails, stepEmergencyContact;
    private Button buttonPrevious, buttonNext, buttonSubmit;
    private int currentStep = 1;

    private FirebaseFirestore db;
    private CollectionReference tenantsRef;
    private CollectionReference propertiesRef; // Declare propertiesRef
    private CollectionReference unitsRef; // Corrected to CollectionReference

    private ArrayList<String> propertyNames = new ArrayList<>(); // To store property names
    private ArrayList<String> unitIds = new ArrayList<>(); // To store unit IDs (unitNumber)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        tenantsRef = db.collection("tenants");
        propertiesRef = db.collection("properties"); // Initialize propertiesRef

        // Initialize UI components
        stepsContainer = findViewById(R.id.steps_container);
        stepPersonalInfo = findViewById(R.id.step_personal_info);
        stepDocumentation = findViewById(R.id.step_documentation);
        stepLeaseDetails = findViewById(R.id.step_lease_details);
        stepEmergencyContact = findViewById(R.id.step_emergency_contact);

        buttonPrevious = findViewById(R.id.button_previous);
        buttonNext = findViewById(R.id.button_next);
        buttonSubmit = findViewById(R.id.button_submit);

        ImageButton btnCancel = findViewById(R.id.btnCancel);

        // Set button click listeners
        buttonPrevious.setOnClickListener(v -> navigateSteps(false));
        buttonNext.setOnClickListener(v -> navigateSteps(true));
        buttonSubmit.setOnClickListener(v -> submitTenantForm());
        btnCancel.setOnClickListener(v -> finish());

        // Initialize step visibility
        updateStepVisibility();

        // Set up date picker for the relevant fields
        setupDatePicker(findViewById(R.id.edit_citizen_issued_date));
        setupDatePicker(findViewById(R.id.edit_citizen_expiry_date));
        setupDatePicker(findViewById(R.id.edit_move_in_date));
        setupDatePicker(findViewById(R.id.edit_lease_start_date));
        setupDatePicker(findViewById(R.id.edit_lease_end_date));

        // Fetch properties from Firestore and populate the Spinner
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        fetchProperties(propertySpinner);

        // Fetch units for the selected property
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedProperty = parentView.getItemAtPosition(position).toString();
                fetchUnits(selectedProperty, unitSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fetchProperties(Spinner propertySpinner) {
        // Fetch properties from Firestore
        propertiesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyNames.clear(); // Clear previous data

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Properties", "No properties found.");
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String propertyName = document.getString("name");
                        if (propertyName != null) {
                            propertyNames.add(propertyName); // Add property name to the list
                            Log.d("Properties", "Fetched property: " + propertyName);  // Log property name
                        }
                    }

                    // Create an instance of the PropertySpinnerAdapter
                    PropertySpinnerAdapter adapter = new PropertySpinnerAdapter(AddTenantActivity.this, propertyNames);
                    propertySpinner.setAdapter(adapter); // Set the adapter to the spinner
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show();
                    Log.e("Properties", "Error fetching properties: ", e);
                });
    }

    private void fetchUnits(String selectedProperty, Spinner unitSpinner) {
        // Fetch the units based on the selected property
        unitsRef = propertiesRef.document(selectedProperty).collection("units");

        unitsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    unitIds.clear(); // Clear previous data

                    // Check if data is empty
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Units", "No units found for this property.");
                        Toast.makeText(this, "No units found for this property", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Use the document ID as the unitId (unit number)
                            String unitId = document.getId(); // This is the document ID, which is the unitId
                            unitIds.add(unitId); // Add unitId to the list
                            Log.d("Units", "Fetched unitId: " + unitId);  // Log unitId
                        }
                    }

                    // Check if unitIds list is populated
                    if (unitIds.isEmpty()) {
                        Toast.makeText(this, "No units found for the selected property", Toast.LENGTH_SHORT).show();
                    } else {
                        // Create an instance of the UnitSpinnerAdapter and set it to the Spinner
                        UnitSpinnerAdapter unitAdapter = new UnitSpinnerAdapter(AddTenantActivity.this, unitIds);
                        unitSpinner.setAdapter(unitAdapter); // Set the unit adapter to the spinner
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    Log.e("Units", "Error fetching units: ", e);
                });
    }

    private void navigateSteps(boolean isNext) {
        if (isNext) {
            if (!validateCurrentStep()) return;
            currentStep++;
        } else {
            currentStep--;
        }
        updateStepVisibility();
    }

    private void updateStepVisibility() {
        stepPersonalInfo.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        stepDocumentation.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        stepLeaseDetails.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        stepEmergencyContact.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);

        buttonPrevious.setVisibility(currentStep == 1 ? View.GONE : View.VISIBLE);
        buttonNext.setVisibility(currentStep == 4 ? View.GONE : View.VISIBLE);
        buttonSubmit.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                return validatePersonalInfo();
            case 2:
                return validateDocumentation();
            case 3:
                return validateLeaseDetails();
            case 4:
                return validateEmergencyContact();
        }
        return true;
    }

    private boolean validatePersonalInfo() {
        EditText firstName = findViewById(R.id.edit_first_name);
        EditText lastName = findViewById(R.id.edit_last_name);
        EditText email = findViewById(R.id.edit_email);
        EditText phoneNumber = findViewById(R.id.edit_phone_number);

        if (firstName.getText().toString().trim().isEmpty()) {
            firstName.setError("First name is required");
            return false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastName.setError("Last name is required");
            return false;
        }
        if (email.getText().toString().trim().isEmpty()) {
            email.setError("Email is required");
            return false;
        }
        if (phoneNumber.getText().toString().trim().isEmpty()) {
            phoneNumber.setError("Phone number is required");
            return false;
        }
        return true;
    }

    private boolean validateDocumentation() {
        EditText citizenId = findViewById(R.id.edit_citizen_id);
        if (citizenId.getText().toString().trim().length() != 13) {
            citizenId.setError("Citizen ID must be 13 digits");
            return false;
        }
        return true;
    }

    private boolean validateLeaseDetails() {
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        EditText moveInDate = findViewById(R.id.edit_move_in_date);
        EditText leaseStartDate = findViewById(R.id.edit_lease_start_date);
        EditText leaseEndDate = findViewById(R.id.edit_lease_end_date);

        if (propertySpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Select a property", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (unitSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Select a unit", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (moveInDate.getText().toString().trim().isEmpty()) {
            moveInDate.setError("Move-in date is required");
            return false;
        }
        if (leaseStartDate.getText().toString().trim().isEmpty()) {
            leaseStartDate.setError("Lease start date is required");
            return false;
        }
        if (leaseEndDate.getText().toString().trim().isEmpty()) {
            leaseEndDate.setError("Lease end date is required");
            return false;
        }
        return true;
    }

    private boolean validateEmergencyContact() {
        EditText contactName = findViewById(R.id.edit_emergency_contact_name);
        EditText contactPhone = findViewById(R.id.edit_emergency_contact_phone);

        if (contactName.getText().toString().trim().isEmpty()) {
            contactName.setError("Emergency contact name is required");
            return false;
        }
        if (contactPhone.getText().toString().trim().isEmpty()) {
            contactPhone.setError("Emergency contact phone is required");
            return false;
        }
        return true;
    }

    private void submitTenantForm() {
        if (!validateEmergencyContact()) return;

        // Gather data
        String firstName = ((EditText) findViewById(R.id.edit_first_name)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.edit_last_name)).getText().toString().trim();
        String email = ((EditText) findViewById(R.id.edit_email)).getText().toString().trim();
        String phoneNumber = ((EditText) findViewById(R.id.edit_phone_number)).getText().toString().trim();
        String citizenId = ((EditText) findViewById(R.id.edit_citizen_id)).getText().toString().trim();
        String property = ((Spinner) findViewById(R.id.spinner_property_select)).getSelectedItem().toString();
        String unit = ((Spinner) findViewById(R.id.spinner_unit_select)).getSelectedItem().toString();
        String moveInDate = ((EditText) findViewById(R.id.edit_move_in_date)).getText().toString().trim();
        String leaseStartDate = ((EditText) findViewById(R.id.edit_lease_start_date)).getText().toString().trim();
        String leaseEndDate = ((EditText) findViewById(R.id.edit_lease_end_date)).getText().toString().trim();
        String emergencyContact = ((EditText) findViewById(R.id.edit_emergency_contact_name)).getText().toString().trim();
        String emergencyPhone = ((EditText) findViewById(R.id.edit_emergency_contact_phone)).getText().toString().trim();

        // Create a map of tenant data
        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("first_name", firstName);
        tenantData.put("last_name", lastName);
        tenantData.put("email", email);
        tenantData.put("phone_number", phoneNumber);
        tenantData.put("citizen_id", citizenId);
        tenantData.put("property", property);
        tenantData.put("unit", unit);
        tenantData.put("move_in_date", moveInDate);
        tenantData.put("lease_start_date", leaseStartDate);
        tenantData.put("lease_end_date", leaseEndDate);
        tenantData.put("emergency_contact_name", emergencyContact);
        tenantData.put("emergency_contact_phone", emergencyPhone);

        // Save to Firestore
        tenantsRef.add(tenantData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tenant registered successfully", Toast.LENGTH_LONG).show();
                    finish(); // Close the activity after submission
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error registering tenant", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(AddTenantActivity.this,
                    (view, year, month, dayOfMonth) ->
                            editText.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }
}
