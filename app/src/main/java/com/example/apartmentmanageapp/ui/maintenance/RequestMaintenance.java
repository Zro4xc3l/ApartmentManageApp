package com.example.apartmentmanageapp.ui.maintenance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
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
    private Button buttonUploadPhoto, buttonSaveRequest;
    private ImageButton closeButton;
    private ImageView imagePreview;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;
    private String uploadedImageUrl = "";

    private Map<String, String> propertyMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_maintenance);

        // Initialize UI elements
        editIssueTitle = findViewById(R.id.edit_issue_title);
        spinnerProperty = findViewById(R.id.spinner_property);
        spinnerUnit = findViewById(R.id.spinner_unit);
        editIssueDescription = findViewById(R.id.edit_issue_description);
        editExpectedDate = findViewById(R.id.edit_expected_date);
        editCost = findViewById(R.id.edit_cost);
        radioUrgency = findViewById(R.id.radio_urgency); // ✅ Urgency selection
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonSaveRequest = findViewById(R.id.button_save_request);
        closeButton = findViewById(R.id.button_close);
        imagePreview = findViewById(R.id.image_preview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("maintenance_photos");

        // Load properties into the spinner
        loadProperties();

        // Close button functionality
        closeButton.setOnClickListener(v -> finish());

        // Open Date Picker when clicking on the Expected Date field
        editExpectedDate.setFocusable(false);
        editExpectedDate.setOnClickListener(v -> showDatePicker());

        // Set up property selection listener
        spinnerProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProperty = parent.getItemAtPosition(position).toString();
                if (!selectedProperty.equals("Select a Property")) {
                    loadUnits(propertyMap.get(selectedProperty));
                } else {
                    setUnitPlaceholder();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Image upload functionality
        buttonUploadPhoto.setOnClickListener(v -> openFileChooser());

        // Submit request functionality
        buttonSaveRequest.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndSubmit();
            } else {
                submitMaintenanceRequest();
            }
        });
    }

    private void loadProperties() {
        db.collection("properties").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> propertyNames = new ArrayList<>();
                propertyNames.add("Select a Property"); // Default option

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String propertyName = document.getString("name");
                    if (propertyName != null) {
                        propertyNames.add(propertyName);
                        propertyMap.put(propertyName, document.getId()); // Store ID
                    }
                }

                // Set adapter for property dropdown
                ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, propertyNames);
                spinnerProperty.setAdapter(propertyAdapter);
            } else {
                Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show();
            }
        });
    }

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

                        // Set adapter for unit dropdown
                        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
                        spinnerUnit.setAdapter(unitAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUnitPlaceholder() {
        List<String> unitPlaceholder = new ArrayList<>();
        unitPlaceholder.add("Select a Unit");

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitPlaceholder);
        spinnerUnit.setAdapter(unitAdapter);
    }

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
                }, year, month, day);

        datePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

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

    private void submitMaintenanceRequest() {
        String title = editIssueTitle.getText().toString().trim();
        String property = propertyMap.get(spinnerProperty.getSelectedItem().toString());
        String unit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "";
        String description = editIssueDescription.getText().toString().trim();
        String expectedDate = editExpectedDate.getText().toString().trim();
        String cost = editCost.getText().toString().trim();
        String urgency = getSelectedUrgency();

        Map<String, Object> maintenanceRequest = new HashMap<>();
        maintenanceRequest.put("title", title);
        maintenanceRequest.put("description", description);
        maintenanceRequest.put("status", "Pending");
        maintenanceRequest.put("expectedDate", expectedDate);
        maintenanceRequest.put("property", property);
        maintenanceRequest.put("unit", unit);
        maintenanceRequest.put("urgency", urgency);
        if (!uploadedImageUrl.isEmpty()) {
            maintenanceRequest.put("imageUrl", uploadedImageUrl);
        }

        db.collection("maintenanceRequests").add(maintenanceRequest)
                .addOnSuccessListener(documentReference -> finish())
                .addOnFailureListener(e -> Toast.makeText(this, "Error submitting request", Toast.LENGTH_SHORT).show());
    }

    private String getSelectedUrgency() {
        int selectedId = radioUrgency.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_low) return "Low";
        else if (selectedId == R.id.radio_medium) return "Medium";
        return "High";
    }
}
