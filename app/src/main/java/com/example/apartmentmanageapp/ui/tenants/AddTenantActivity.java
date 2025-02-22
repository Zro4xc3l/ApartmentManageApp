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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    // To store property names, unit IDs, and property document IDs
    private ArrayList<String> propertyNames = new ArrayList<>();
    private List<String> unitIds = new ArrayList<>();
    private List<String> propertyIds = new ArrayList<>();

    // Firebase Authentication variables
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firestore and collections
        db = FirebaseFirestore.getInstance();
        tenantsRef = db.collection("tenants");
        propertiesRef = db.collection("properties");

        // Initialize UI components for steps and buttons
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
        buttonPrevious.setOnClickListener(v -> {
            currentStep--;
            updateStepVisibility();
        });
        buttonNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                currentStep++;
                updateStepVisibility();
            }
        });
        buttonSubmit.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                submitTenantForm();
            }
        });
        btnCancel.setOnClickListener(v -> finish());

        // Initialize step visibility
        updateStepVisibility();

        // Set up date pickers for date fields
        setupDatePicker((EditText) findViewById(R.id.edit_citizen_issued_date));
        setupDatePicker((EditText) findViewById(R.id.edit_citizen_expiry_date));
        setupDatePicker((EditText) findViewById(R.id.edit_move_in_date));
        setupDatePicker((EditText) findViewById(R.id.edit_lease_start_date));
        setupDatePicker((EditText) findViewById(R.id.edit_lease_end_date));

        // Documentation layout should include:
        //   - edit_citizen_id, edit_citizen_issued_date, edit_citizen_expiry_date,
        //   - edit_address_on_citizen_card (for citizen card address)
        //
        // Lease details layout should include:
        //   - spinner_property_select, spinner_unit_select,
        //   - edit_move_in_date, edit_electric_meter_reading, edit_water_meter_reading,
        //   - edit_lease_start_date, edit_lease_end_date,
        //   - edit_monthly_rent, edit_deposit_amount, edit_key_amount, edit_keycard_amount

        // Initialize spinners
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        Spinner relationshipSpinner = findViewById(R.id.spinner_emergency_contact_relationship);

        // Load relationship options from resources
        ArrayAdapter<CharSequence> relationshipAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.relationship_list,
                android.R.layout.simple_spinner_item
        );
        relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshipSpinner.setAdapter(relationshipAdapter);

        // Fetch properties owned by the current user
        fetchProperties(propertySpinner);

        // When a property is selected, fetch its available units.
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (propertyIds == null || propertyIds.size() <= position || propertyIds.get(position) == null) {
                    ArrayList<String> defaultUnitList = new ArrayList<>();
                    defaultUnitList.add("Select Unit");
                    unitSpinner.setAdapter(new UnitSpinnerAdapter(AddTenantActivity.this, defaultUnitList));
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

        // When a unit is selected, fetch its amenities and rent amount (for display)
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (unitIds == null || unitIds.size() <= position) {
                    Log.e("Units", "unitIds list is empty or position out of bounds.");
                    return;
                }
                String selectedUnit = unitIds.get(position);
                if (selectedUnit.equals("Select Unit")) {
                    Log.d("Units", "Default unit selected, skipping amenities and rent fetch.");
                    return;
                }
                Log.d("Units", "Selected unit ID: " + selectedUnit);
                fetchAmenities(selectedUnit);
                fetchRentAmount(selectedUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Units", "No unit selected.");
            }
        });
    }

    // -----------------------------
    // Firestore Data Fetching Methods
    // -----------------------------

    private void fetchProperties(Spinner propertySpinner) {
        String userId = currentUser.getUid();
        propertiesRef.whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyNames.clear();
                    propertyIds.clear();
                    // Add default "Select Property" entry
                    propertyNames.add("Select Property");
                    propertyIds.add(null);
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Properties", "No properties found for user: " + userId);
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
                    propertySpinner.setAdapter(new PropertySpinnerAdapter(AddTenantActivity.this, propertyNames));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTenantActivity.this, "Failed to load properties", Toast.LENGTH_SHORT).show();
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
        // Add default "Select Unit" entry directly
        unitIds.add("Select Unit");
        unitsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Boolean isOccupied = document.getBoolean("occupied");
                        if (isOccupied != null && isOccupied) {
                            Log.d("Units", "Skipping occupied unit: " + document.getId());
                        } else {
                            String unitId = document.getId();
                            unitIds.add(unitId);
                            Log.d("Units", "Fetched available unit ID: " + unitId);
                        }
                    }
                    if (unitIds.size() == 1) {
                        Toast.makeText(AddTenantActivity.this, "No available units for this property", Toast.LENGTH_SHORT).show();
                    }
                    unitSpinner.setAdapter(new UnitSpinnerAdapter(AddTenantActivity.this, unitIds));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTenantActivity.this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    Log.e("Units", "Error fetching units: ", e);
                });
    }

    private void fetchRentAmount(String unitId) {
        if (unitId == null || unitId.isEmpty() || unitId.equals("Select Unit")) {
            Log.d("Rent", "No valid unit selected, skipping rent fetch.");
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
                            Log.d("Rent", "No rent amount found; setting to 0.");
                            updateRentUI(0L);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Rent", "Error fetching rent amount: ", e);
                    Toast.makeText(AddTenantActivity.this, "Failed to load rent amount", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAmenities(String unitId) {
        if (unitId == null || unitId.isEmpty() || unitId.equals("Select Unit")) {
            Log.d("Amenities", "No valid unit selected, skipping amenities fetch.");
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
                    Toast.makeText(AddTenantActivity.this, "Failed to load amenities", Toast.LENGTH_SHORT).show();
                });
    }

    // -----------------------------
    // Navigation and Validation Methods
    // -----------------------------

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
        boolean valid = true;
        EditText firstName = findViewById(R.id.edit_first_name);
        EditText lastName = findViewById(R.id.edit_last_name);
        EditText email = findViewById(R.id.edit_email);
        EditText phoneNumber = findViewById(R.id.edit_phone_number);

        if (firstName.getText().toString().trim().isEmpty()) {
            firstName.setError("First name is required");
            valid = false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastName.setError("Last name is required");
            valid = false;
        }
        if (email.getText().toString().trim().isEmpty()) {
            email.setError("Email is required");
            valid = false;
        }
        if (phoneNumber.getText().toString().trim().isEmpty()) {
            phoneNumber.setError("Phone number is required");
            valid = false;
        }
        return valid;
    }

    private boolean validateDocumentation() {
        boolean valid = true;
        EditText citizenId = findViewById(R.id.edit_citizen_id);
        if (citizenId.getText().toString().trim().length() != 13) {
            citizenId.setError("Citizen ID must be 13 digits");
            valid = false;
        }
        EditText citizenIssuedDate = findViewById(R.id.edit_citizen_issued_date);
        EditText citizenExpiryDate = findViewById(R.id.edit_citizen_expiry_date);
        EditText address = findViewById(R.id.edit_address_on_citizen_card);
        if (citizenIssuedDate.getText().toString().trim().isEmpty()) {
            citizenIssuedDate.setError("Citizen card issue date is required");
            valid = false;
        }
        if (citizenExpiryDate.getText().toString().trim().isEmpty()) {
            citizenExpiryDate.setError("Citizen card expiry date is required");
            valid = false;
        }
        if (address.getText().toString().trim().isEmpty()) {
            address.setError("Citizen card address is required");
            valid = false;
        }
        return valid;
    }

    private boolean validateLeaseDetails() {
        boolean valid = true;
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        EditText moveInDate = findViewById(R.id.edit_move_in_date);
        EditText leaseStartDate = findViewById(R.id.edit_lease_start_date);
        EditText leaseEndDate = findViewById(R.id.edit_lease_end_date);
        EditText electricMeterReading = findViewById(R.id.edit_electric_meter_reading);
        EditText waterMeterReading = findViewById(R.id.edit_water_meter_reading);
        EditText depositAmount = findViewById(R.id.edit_deposit_amount);
        EditText keyAmount = findViewById(R.id.edit_key_amount);
        EditText keycardAmount = findViewById(R.id.edit_keycard_amount);

        String selectedProperty = propertySpinner.getSelectedItem() != null ? propertySpinner.getSelectedItem().toString() : "";
        if (selectedProperty.equals("Select Property")) {
            Toast.makeText(this, "Please select a property", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        String selectedUnit = unitSpinner.getSelectedItem() != null ? unitSpinner.getSelectedItem().toString() : "";
        if (selectedUnit.equals("Select Unit")) {
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (moveInDate.getText().toString().trim().isEmpty()) {
            moveInDate.setError("Move-in date is required");
            valid = false;
        }
        if (leaseStartDate.getText().toString().trim().isEmpty()) {
            leaseStartDate.setError("Lease start date is required");
            valid = false;
        }
        if (leaseEndDate.getText().toString().trim().isEmpty()) {
            leaseEndDate.setError("Lease end date is required");
            valid = false;
        }
        if (electricMeterReading.getText().toString().trim().isEmpty()) {
            electricMeterReading.setError("Electric meter reading is required");
            valid = false;
        }
        if (waterMeterReading.getText().toString().trim().isEmpty()) {
            waterMeterReading.setError("Water meter reading is required");
            valid = false;
        }
        if (depositAmount.getText().toString().trim().isEmpty()) {
            depositAmount.setError("Security deposit is required");
            valid = false;
        }
        if (keyAmount.getText().toString().trim().isEmpty()) {
            keyAmount.setError("Number of keys is required");
            valid = false;
        }
        if (keycardAmount.getText().toString().trim().isEmpty()) {
            keycardAmount.setError("Number of keycards is required");
            valid = false;
        }
        return valid;
    }

    private boolean validateEmergencyContact() {
        boolean valid = true;
        EditText contactName = findViewById(R.id.edit_emergency_contact_name);
        EditText contactPhone = findViewById(R.id.edit_emergency_contact_phone);
        if (contactName.getText().toString().trim().isEmpty()) {
            contactName.setError("Emergency contact name is required");
            valid = false;
        }
        if (contactPhone.getText().toString().trim().isEmpty()) {
            contactPhone.setError("Emergency contact phone is required");
            valid = false;
        }
        return valid;
    }

    // -----------------------------
    // Form Submission
    // -----------------------------

    private void submitTenantForm() {
        if (!validateEmergencyContact()) return;

        // Personal Info
        String firstName = ((EditText) findViewById(R.id.edit_first_name)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.edit_last_name)).getText().toString().trim();
        String email = ((EditText) findViewById(R.id.edit_email)).getText().toString().trim();
        String phoneNumber = ((EditText) findViewById(R.id.edit_phone_number)).getText().toString().trim();
        String citizenId = ((EditText) findViewById(R.id.edit_citizen_id)).getText().toString().trim();

        // Documentation Step
        String citizenIssuedDate = ((EditText) findViewById(R.id.edit_citizen_issued_date)).getText().toString().trim();
        String citizenExpiryDate = ((EditText) findViewById(R.id.edit_citizen_expiry_date)).getText().toString().trim();
        String address = ((EditText) findViewById(R.id.edit_address_on_citizen_card)).getText().toString().trim();

        // Lease Details Step
        String moveInDate = ((EditText) findViewById(R.id.edit_move_in_date)).getText().toString().trim();
        String leaseStartDate = ((EditText) findViewById(R.id.edit_lease_start_date)).getText().toString().trim();
        String leaseEndDate = ((EditText) findViewById(R.id.edit_lease_end_date)).getText().toString().trim();
        String electricMeterReadingStr = ((EditText) findViewById(R.id.edit_electric_meter_reading)).getText().toString().trim();
        String waterMeterReadingStr = ((EditText) findViewById(R.id.edit_water_meter_reading)).getText().toString().trim();
        String monthlyRentStr = ((EditText) findViewById(R.id.edit_monthly_rent)).getText().toString().trim();
        String depositAmountStr = ((EditText) findViewById(R.id.edit_deposit_amount)).getText().toString().trim();
        String keyAmountStr = ((EditText) findViewById(R.id.edit_key_amount)).getText().toString().trim();
        String keycardAmountStr = ((EditText) findViewById(R.id.edit_keycard_amount)).getText().toString().trim();

        // Emergency Contact
        String emergencyContact = ((EditText) findViewById(R.id.edit_emergency_contact_name)).getText().toString().trim();
        String emergencyPhone = ((EditText) findViewById(R.id.edit_emergency_contact_phone)).getText().toString().trim();

        // Retrieve selected property and unit IDs
        Spinner propertySpinner = findViewById(R.id.spinner_property_select);
        int selectedPropertyPosition = propertySpinner.getSelectedItemPosition();
        String propertyId = propertyIds.get(selectedPropertyPosition);

        Spinner unitSpinner = findViewById(R.id.spinner_unit_select);
        int selectedUnitPosition = unitSpinner.getSelectedItemPosition();
        String unitId = unitIds.get(selectedUnitPosition);

        // Prepare tenant data map with safe conversions
        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("first_name", firstName);
        tenantData.put("last_name", lastName);
        tenantData.put("email", email);
        tenantData.put("phone_number", phoneNumber);
        tenantData.put("citizen_id", citizenId);
        tenantData.put("citizen_issued_date", citizenIssuedDate);
        tenantData.put("citizen_expiry_date", citizenExpiryDate);
        tenantData.put("address", address);
        tenantData.put("electric_meter_reading", safeDouble(electricMeterReadingStr));
        tenantData.put("water_meter_reading", safeDouble(waterMeterReadingStr));
        tenantData.put("property", propertyId);
        tenantData.put("unit", unitId);
        tenantData.put("move_in_date", moveInDate);
        tenantData.put("lease_start_date", leaseStartDate);
        tenantData.put("lease_end_date", leaseEndDate);
        tenantData.put("emergency_contact_name", emergencyContact);
        tenantData.put("emergency_contact_phone", emergencyPhone);
        tenantData.put("rentAmount", safeDouble(monthlyRentStr));
        tenantData.put("depositAmount", safeDouble(depositAmountStr));
        tenantData.put("keyAmount", safeInt(keyAmountStr));
        tenantData.put("keycardAmount", safeInt(keycardAmountStr));

        tenantsRef.add(tenantData)
                .addOnSuccessListener(documentReference -> {
                    updateUnitDetails(propertyId, unitId, firstName + " " + lastName);
                    Toast.makeText(AddTenantActivity.this, "Tenant registered successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTenantActivity.this, "Error registering tenant", Toast.LENGTH_SHORT).show();
                    Log.e("TenantSubmit", "Error registering tenant", e);
                });
    }

    private void updateUnitDetails(String propertyId, String unitId, String tenantName) {
        DocumentReference unitRef = db.collection("properties")
                .document(propertyId)
                .collection("units")
                .document(unitId);

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
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            DatePickerDialog datePicker = new DatePickerDialog(AddTenantActivity.this,
                    (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editText.setText(formattedDate);
                        editText.setError(null);
                    }, year, month, day);
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

    // -----------------------------
    // Helper Methods
    // -----------------------------
    private String getEditTextValue(int id) {
        return ((EditText) findViewById(id)).getText().toString().trim();
    }

    private double safeDouble(String value) {
        try {
            return value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int safeInt(String value) {
        try {
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
