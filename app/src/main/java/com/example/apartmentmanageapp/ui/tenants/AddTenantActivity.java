package com.example.apartmentmanageapp.ui.tenants;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTenantActivity extends AppCompatActivity {

    private FrameLayout stepsContainer;
    private View stepPersonalInfo, stepDocumentation, stepLeaseDetails, stepEmergencyContact;
    private Button buttonPrevious, buttonNext, buttonSubmit;
    private int currentStep = 1;

    private FirebaseFirestore db;
    private CollectionReference tenantsRef;
    private CollectionReference propertiesRef;
    private CollectionReference unitsRef;

    private ArrayList<String> propertyNames = new ArrayList<>(); // To store property names
    private List<String> unitIds = new ArrayList<>();              // To store unit IDs (only available units)
    private List<String> propertyIds = new ArrayList<>();          // To store property document IDs

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        tenantsRef = db.collection("tenants");
        propertiesRef = db.collection("properties");

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

        // Set up date pickers for relevant EditText fields
        setupDatePicker((EditText) findViewById(R.id.edit_citizen_issued_date));
        setupDatePicker((EditText) findViewById(R.id.edit_citizen_expiry_date));
        setupDatePicker((EditText) findViewById(R.id.edit_move_in_date));
        setupDatePicker((EditText) findViewById(R.id.edit_lease_start_date));
        setupDatePicker((EditText) findViewById(R.id.edit_lease_end_date));

        // Initialize spinners
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        Spinner relationshipSpinner = findViewById(R.id.spinner_emergency_contact_relationship);

        // Load relationship options from resources
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.relationship_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshipSpinner.setAdapter(adapter);

        // Fetch properties and populate the property spinner
        fetchProperties(propertySpinner);

        // When a property is selected, fetch its units (only those not occupied)
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (propertyIds.isEmpty() || position >= propertyIds.size() || propertyIds.get(position) == null) {
                    Log.e("Properties", "Invalid selection or propertyIds not populated yet. Skipping fetchUnits.");
                    return;
                }
                String selectedPropertyId = propertyIds.get(position);
                Log.d("Properties", "Selected property ID: " + selectedPropertyId);
                fetchUnits(selectedPropertyId, unitSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Properties", "No property selected.");
            }
        });

        // When a unit is selected, fetch its amenities and rent amount
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (unitIds.isEmpty() || position >= unitIds.size() || unitIds.get(position) == null) {
                    Log.e("Units", "Invalid selection or unitIds not populated yet. Skipping fetchAmenities.");
                    return;
                }
                String selectedUnitId = unitIds.get(position);
                Log.d("Units", "Selected unit ID: " + selectedUnitId);
                fetchAmenities(selectedUnitId);
                fetchRentAmount(selectedUnitId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Units", "No unit selected.");
            }
        });
    }

    private void fetchProperties(Spinner propertySpinner) {
        propertiesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyNames.clear();
                    propertyIds.clear();

                    // Add default "Select Property" entry
                    propertyNames.add("Select Property");
                    propertyIds.add(null);

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Properties", "No properties found.");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String propertyName = document.getString("name");
                        String propertyId = document.getId();
                        if (propertyName != null) {
                            propertyNames.add(propertyName);
                            propertyIds.add(propertyId);
                            Log.d("Properties", "Fetched property: " + propertyName + " (ID: " + propertyId + ")");
                        }
                    }

                    PropertySpinnerAdapter adapter = new PropertySpinnerAdapter(AddTenantActivity.this, propertyNames);
                    propertySpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show();
                    Log.e("Properties", "Error fetching properties: ", e);
                });
    }

    private void fetchUnits(String selectedProperty, Spinner unitSpinner) {
        if (selectedProperty == null || selectedProperty.isEmpty()) {
            Log.d("Units", "No property selected, skipping unit fetch.");
            return;
        }

        Log.d("Units", "Fetching units for property ID: " + selectedProperty);
        unitsRef = propertiesRef.document(selectedProperty).collection("units");
        unitIds.clear();

        // Add default "Select Unit" entry (represented by a null)
        unitIds.add(null);

        unitsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if the unit is occupied; if so, skip it.
                        Boolean isOccupied = document.getBoolean("occupied");
                        if (isOccupied != null && isOccupied) {
                            Log.d("Units", "Skipping occupied unit: " + document.getId());
                        } else {
                            String unitId = document.getId();
                            unitIds.add(unitId);
                            Log.d("Units", "Fetched available unitId: " + unitId);
                        }
                    }
                    if (unitIds.size() == 1) {
                        Toast.makeText(AddTenantActivity.this, "No available units for this property", Toast.LENGTH_SHORT).show();
                    }
                    UnitSpinnerAdapter unitAdapter = new UnitSpinnerAdapter(AddTenantActivity.this, unitIds);
                    unitSpinner.setAdapter(unitAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTenantActivity.this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    Log.e("Units", "Error fetching units: ", e);
                });
    }

    private void fetchRentAmount(String unitId) {
        if (unitId == null || unitId.isEmpty()) {
            Log.d("Rent", "No unit selected, skipping rent fetch.");
            return;
        }
        Log.d("Rent", "Fetching rent amount for unit ID: " + unitId);
        unitsRef.document(unitId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("rentAmount")) {
                        Long rentAmount = documentSnapshot.getLong("rentAmount");
                        if (rentAmount != null) {
                            Log.d("Rent", "Fetched rent amount: " + rentAmount);
                            updateRentUI(rentAmount);
                        } else {
                            Log.d("Rent", "No rent amount found.");
                            updateRentUI(0L);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Rent", "Error fetching rent amount: ", e);
                    Toast.makeText(this, "Failed to load rent amount", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAmenities(String unitId) {
        if (unitId == null || unitId.isEmpty()) {
            Log.d("Amenities", "No unit selected, skipping amenities fetch.");
            return;
        }
        Log.d("Amenities", "Fetching amenities for unit ID: " + unitId);
        unitsRef.document(unitId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("amenities")) {
                        List<String> amenitiesList = (List<String>) documentSnapshot.get("amenities");
                        if (amenitiesList != null && !amenitiesList.isEmpty()) {
                            Log.d("Amenities", "Fetched amenities: " + amenitiesList);
                            updateAmenitiesUI(amenitiesList);
                        } else {
                            Log.d("Amenities", "No amenities found.");
                            updateAmenitiesUI(new ArrayList<>());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Amenities", "Error fetching amenities: ", e);
                    Toast.makeText(this, "Failed to load amenities", Toast.LENGTH_SHORT).show();
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

        // Gather data from the form
        String firstName = ((EditText) findViewById(R.id.edit_first_name)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.edit_last_name)).getText().toString().trim();
        String email = ((EditText) findViewById(R.id.edit_email)).getText().toString().trim();
        String phoneNumber = ((EditText) findViewById(R.id.edit_phone_number)).getText().toString().trim();
        String citizenId = ((EditText) findViewById(R.id.edit_citizen_id)).getText().toString().trim();
        String moveInDate = ((EditText) findViewById(R.id.edit_move_in_date)).getText().toString().trim();
        String leaseStartDate = ((EditText) findViewById(R.id.edit_lease_start_date)).getText().toString().trim();
        String leaseEndDate = ((EditText) findViewById(R.id.edit_lease_end_date)).getText().toString().trim();
        String emergencyContact = ((EditText) findViewById(R.id.edit_emergency_contact_name)).getText().toString().trim();
        String emergencyPhone = ((EditText) findViewById(R.id.edit_emergency_contact_phone)).getText().toString().trim();

        // Retrieve additional fields from the form
        String monthlyRent = ((EditText) findViewById(R.id.edit_monthly_rent)).getText().toString().trim();
        String depositAmount = ((EditText) findViewById(R.id.edit_deposit_amount)).getText().toString().trim();
        String keyAmount = ((EditText) findViewById(R.id.edit_key_amount)).getText().toString().trim();
        String keycardAmount = ((EditText) findViewById(R.id.edit_keycard_amount)).getText().toString().trim();

        // Retrieve the selected property document ID from the propertyIds list
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        int selectedPropertyPosition = propertySpinner.getSelectedItemPosition();
        String propertyId = propertyIds.get(selectedPropertyPosition);

        // Retrieve the selected unit ID from the unitIds list
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        int selectedUnitPosition = unitSpinner.getSelectedItemPosition();
        String unitId = unitIds.get(selectedUnitPosition);

        // Create a map of tenant data
        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("first_name", firstName);
        tenantData.put("last_name", lastName);
        tenantData.put("email", email);
        tenantData.put("phone_number", phoneNumber);
        tenantData.put("citizen_id", citizenId);
        tenantData.put("property", propertyId);
        tenantData.put("unit", unitId);
        tenantData.put("move_in_date", moveInDate);
        tenantData.put("lease_start_date", leaseStartDate);
        tenantData.put("lease_end_date", leaseEndDate);
        tenantData.put("emergency_contact_name", emergencyContact);
        tenantData.put("emergency_contact_phone", emergencyPhone);

        // Add the additional fields
        tenantData.put("rentAmount", monthlyRent);
        tenantData.put("depositAmount", depositAmount);
        tenantData.put("keyAmount", keyAmount);
        tenantData.put("keycardAmount", keycardAmount);

        // Save tenant data to Firestore
        tenantsRef.add(tenantData)
                .addOnSuccessListener(documentReference -> {
                    // After successfully adding the tenant, update the unit's occupied status and tenant name
                    updateUnitDetails(propertyId, unitId, firstName + " " + lastName);
                    Toast.makeText(this, "Tenant registered successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error registering tenant", Toast.LENGTH_SHORT).show();
                    Log.e("TenantSubmit", "Error registering tenant", e);
                });
    }

    private void updateUnitDetails(String propertyId, String unitId, String tenantName) {
        // Reference the specific unit document in Firestore
        DocumentReference unitRef = db.collection("properties")
                .document(propertyId)
                .collection("units")
                .document(unitId);

        // Check if the unit document exists and update it accordingly
        unitRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        unitRef.update("tenantName", tenantName, "occupied", true)
                                .addOnSuccessListener(aVoid -> Log.d("Update", "Unit marked as occupied and tenant name updated"))
                                .addOnFailureListener(e -> Log.e("Update", "Error updating unit details", e));
                    } else {
                        Log.e("Update", "Unit document does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Update", "Error fetching unit document", e));
    }

    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(AddTenantActivity.this,
                    (DatePicker view, int year, int month, int dayOfMonth) ->
                            editText.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }

    private void updateAmenitiesUI(List<String> amenities) {
        TextView amenitiesTextView = findViewById(R.id.text_amenities);
        if (amenities.isEmpty()) {
            amenitiesTextView.setText("No amenities available for this unit.");
        } else {
            StringBuilder amenitiesText = new StringBuilder();
            for (String amenity : amenities) {
                amenitiesText.append("• ").append(amenity).append("\n");
            }
            amenitiesTextView.setText(amenitiesText.toString().trim());
        }
    }

    private void updateRentUI(Long rentAmount) {
        EditText rentEditText = findViewById(R.id.edit_monthly_rent);
        rentEditText.setText(String.valueOf(rentAmount));
    }
}
