package com.example.apartmentmanageapp.ui.maintenance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestMaintenance extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Elements
    private EditText editIssueTitle, editIssueDescription, editExpectedDate, editCost;
    private Spinner spinnerProperty, spinnerUnit;
    private RadioGroup radioUrgency;
    // Error TextView for urgency (make sure it's added in your XML)
    private TextView textUrgencyError;
    private Button buttonUploadPhoto, buttonSaveRequest;
    private ImageButton closeButton;
    private ImageView imagePreview;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;
    private String uploadedImageUrl = "";

    // Map to hold property names and their Firestore document IDs
    private Map<String, String> propertyMap = new HashMap<>();

    // FirebaseAuth instance for authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_maintenance);

        // Initialize FirebaseAuth and check if a user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        editIssueTitle = findViewById(R.id.edit_issue_title);
        spinnerProperty = findViewById(R.id.spinner_property);
        spinnerUnit = findViewById(R.id.spinner_unit);
        editIssueDescription = findViewById(R.id.edit_issue_description);
        editExpectedDate = findViewById(R.id.edit_expected_date);
        editCost = findViewById(R.id.edit_cost);
        radioUrgency = findViewById(R.id.radio_urgency);
        // Initialize the urgency error TextView (make sure it's in your XML)
        textUrgencyError = findViewById(R.id.text_urgency_error);
        textUrgencyError.setVisibility(View.GONE); // hide it initially
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonSaveRequest = findViewById(R.id.button_save_request);
        closeButton = findViewById(R.id.button_close);
        imagePreview = findViewById(R.id.image_preview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("maintenance_photos");

        // Load properties into the spinner
        loadOwnerProperties();

        // Close button functionality
        closeButton.setOnClickListener(v -> finish());

        // Open Date Picker when clicking on the Expected Date field
        editExpectedDate.setFocusable(false);
        editExpectedDate.setOnClickListener(v -> showDatePicker());

        // When a property is selected, load its units if a valid property is chosen.
        spinnerProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProperty = parent.getItemAtPosition(position).toString();
                if (selectedProperty.equals("Select a Property")) {
                    setUnitPlaceholder();
                } else {
                    String propertyId = propertyMap.get(selectedProperty);
                    if (propertyId != null) {
                        loadUnits(propertyId);
                    } else {
                        setUnitPlaceholder();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Image upload functionality
        buttonUploadPhoto.setOnClickListener(v -> openFileChooser());

        // Submit request functionality:
        // Validate that issue title, issue description, and urgency are selected before submission.
        buttonSaveRequest.setOnClickListener(v -> {
            if (!validateIssueFields()) {
                return;
            }
            if (!validateUrgency()) {
                return;
            }
            if (imageUri != null) {
                uploadImageAndSubmit();
            } else {
                submitMaintenanceRequest();
            }
        });
    }

    /**
     * Loads properties from the "properties" collection where the "ownerId" equals the current user's UID.
     * Adds a default "Select a Property" option at the top.
     */
    private void loadOwnerProperties() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String ownerId = currentUser.getUid();
        db.collection("properties")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> propertyList = new ArrayList<>();
                    // Add default option first.
                    propertyList.add("Select a Property");

                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String propertyName = document.getString("name");
                            String propertyId = document.getId();
                            if (propertyName != null) {
                                propertyList.add(propertyName);
                                propertyMap.put(propertyName, propertyId);
                            }
                        }
                        if (propertyList.size() == 1) {
                            Toast.makeText(this, "No properties found for this owner", Toast.LENGTH_SHORT).show();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item, propertyList);
                        spinnerProperty.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No properties found for this owner", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show()
                );
    }

    // Load units for the given propertyId into the unit spinner.
    private void loadUnits(String propertyId) {
        db.collection("properties").document(propertyId).collection("units").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> unitList = new ArrayList<>();
                        unitList.add("Select a Unit"); // Default option

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String unitId = document.getId();
                            unitList.add("Unit " + unitId);
                        }

                        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item, unitList);
                        spinnerUnit.setAdapter(unitAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Sets a placeholder for the unit spinner.
    private void setUnitPlaceholder() {
        List<String> unitPlaceholder = new ArrayList<>();
        unitPlaceholder.add("Select a Unit");

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, unitPlaceholder);
        spinnerUnit.setAdapter(unitAdapter);
    }

    // Shows a DatePicker dialog for selecting the expected date.
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editExpectedDate.setText(sdf.format(calendar.getTime()));
                    // Clear any error message on the date field once a date is selected.
                    editExpectedDate.setError(null);
                }, year, month, day);

        datePickerDialog.show();
    }

    // Opens a file chooser for selecting an image.
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

    // Uploads the selected image to Firebase Storage and then submits the maintenance request.
    private void uploadImageAndSubmit() {
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrl = uri.toString();
                    submitMaintenanceRequest();
                })
        ).addOnFailureListener(e ->
                Toast.makeText(RequestMaintenance.this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        );
    }

    // Validates that the issue title and issue description fields are not empty.
    private boolean validateIssueFields() {
        boolean valid = true;
        String title = editIssueTitle.getText().toString().trim();
        String description = editIssueDescription.getText().toString().trim();
        if (title.isEmpty()) {
            editIssueTitle.setError("Issue title is required");
            valid = false;
        }
        if (description.isEmpty()) {
            editIssueDescription.setError("Issue description is required");
            valid = false;
        }
        return valid;
    }

    // Validates that an urgency level is selected.
    // Instead of a Toast, we show an inline error message in text_urgency_error.
    private boolean validateUrgency() {
        TextView urgencyError = findViewById(R.id.text_urgency_error);
        if (radioUrgency.getCheckedRadioButtonId() == -1) {
            urgencyError.setVisibility(View.VISIBLE);
            Log.d("validateUrgency", "No urgency selected; showing error.");
            return false;
        } else {
            urgencyError.setVisibility(View.GONE);
            return true;
        }
    }

    // Submits the maintenance request along with the current user's ID.
    private void submitMaintenanceRequest() {
        // Validate selection for property and unit.
        String selectedProperty = spinnerProperty.getSelectedItem().toString();
        if (selectedProperty.equals("Select a Property")) {
            Toast.makeText(this, "Please select a property", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedUnit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "Select a Unit";
        if (selectedUnit.equals("Select a Unit")) {
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate issue title, description, and urgency.
        if (!validateIssueFields()) {
            return;
        }
        if (!validateUrgency()) {
            return;
        }

        // Gather remaining input fields.
        String title = editIssueTitle.getText().toString().trim();
        String property = propertyMap.get(selectedProperty);
        String unit = selectedUnit;
        String description = editIssueDescription.getText().toString().trim();
        String expectedDate = editExpectedDate.getText().toString().trim();
        String cost = editCost.getText().toString().trim();
        String urgency = getSelectedUrgency();

        // Get the current logged in user's ID.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = "";
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Prepare the maintenance request data.
        Map<String, Object> maintenanceRequest = new HashMap<>();
        maintenanceRequest.put("title", title);
        maintenanceRequest.put("description", description);
        maintenanceRequest.put("status", "Pending");
        maintenanceRequest.put("expectedDate", expectedDate);
        maintenanceRequest.put("property", property);
        maintenanceRequest.put("unit", unit);
        maintenanceRequest.put("urgency", urgency);
        maintenanceRequest.put("cost", cost);
        maintenanceRequest.put("userId", userId);
        if (!uploadedImageUrl.isEmpty()) {
            maintenanceRequest.put("imageUrl", uploadedImageUrl);
        }

        db.collection("maintenanceRequests").add(maintenanceRequest)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RequestMaintenance.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(RequestMaintenance.this, "Error submitting request", Toast.LENGTH_SHORT).show());
    }

    // Returns the selected urgency level from the radio group.
    private String getSelectedUrgency() {
        int selectedId = radioUrgency.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_low) return "Low";
        else if (selectedId == R.id.radio_medium) return "Medium";
        return "High";
    }

    // Sets up a DatePicker for the provided EditText field.
    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            DatePickerDialog datePicker = new DatePickerDialog(RequestMaintenance.this,
                    (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editText.setText(formattedDate);
                        // Clear any error message on the field once a date is chosen.
                        editText.setError(null);
                    }, year, month, day);
            datePicker.show();
        });
    }
}
