package com.example.apartmentmanageapp.ui.bills;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.UnitBillAdapter;
import com.example.apartmentmanageapp.model.UnitBill;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateBillActivity extends AppCompatActivity {

    private Spinner spinnerProperty;
    private RecyclerView recyclerView;
    private UnitBillAdapter unitBillAdapter;
    private List<UnitBill> unitBillList;
    private FirebaseFirestore db;
    private Map<String, String> propertyMap;

    private Button buttonViewSummary, buttonCancel;
    private String selectedPropertyName = "";
    private String selectedPropertyAddress = "";
    private String propertyId = "";

    private TextView billPeriodTextView, dueDateTextView;
    private String billingPeriod = "";

    // Firebase Auth
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        initializeUI();

        // Setup Billing Period Selector
        setupBillingPeriodSelector();

        // Load and filter properties by current user's UID
        setupPropertySpinner();

        // Setup buttons for summary and cancel
        setupViewSummaryButton();
        setupCancelButton();
    }

    private void initializeUI() {
        spinnerProperty = findViewById(R.id.spinner_property);
        recyclerView = findViewById(R.id.recyclerView_unit_bills);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        unitBillList = new ArrayList<>();
        unitBillAdapter = new UnitBillAdapter(this, unitBillList);
        recyclerView.setAdapter(unitBillAdapter);

        buttonViewSummary = findViewById(R.id.button_view_summary);
        buttonCancel = findViewById(R.id.button_cancel);
        billPeriodTextView = findViewById(R.id.bill_period);
        dueDateTextView = findViewById(R.id.text_bill_due_date); // ✅ Due Date TextView

        db = FirebaseFirestore.getInstance();
        propertyMap = new HashMap<>();
    }

    /**
     * Sets up the billing period TextView to allow month-year selection.
     */
    private void setupBillingPeriodSelector() {
        Calendar calendar = Calendar.getInstance();
        billingPeriod = getMonthName(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR);
        billPeriodTextView.setText("Billing Period: " + billingPeriod);

        billPeriodTextView.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(
                    CreateBillActivity.this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        billingPeriod = getMonthName(month) + " " + year;
                        billPeriodTextView.setText("Billing Period: " + billingPeriod);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            // Hide the day picker
            int dayPickerId = getResources().getIdentifier("android:id/day", null, null);
            View dayPicker = dpd.getDatePicker().findViewById(dayPickerId);
            if (dayPicker != null) {
                dayPicker.setVisibility(View.GONE);
            }
            dpd.show();
        });
    }

    /**
     * Filters properties by the currently logged-in user's ownerId
     * and populates the spinner.
     */
    private void setupPropertySpinner() {
        db.collection("properties")
                .whereEqualTo("ownerId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> propertyNames = new ArrayList<>();
                    propertyNames.add("Select Property");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String propertyName = doc.getString("name");
                        if (propertyName != null) {
                            propertyNames.add(propertyName);
                            propertyMap.put(propertyName, doc.getId());
                        }
                    }

                    if (propertyNames.size() > 1) {
                        setupSpinnerAdapter(propertyNames);
                    } else {
                        showToast("No properties found for this user.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error loading properties: " + e.getMessage()));
    }

    private void setupSpinnerAdapter(List<String> propertyNames) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                propertyNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProperty.setAdapter(spinnerAdapter);

        spinnerProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selPropName = parent.getItemAtPosition(position).toString();

                if ("Select Property".equals(selPropName)) {
                    unitBillList.clear();
                    unitBillAdapter.notifyDataSetChanged();
                    selectedPropertyName = "";
                    selectedPropertyAddress = "";
                    propertyId = "";
                    dueDateTextView.setText("Due Date: -"); // ✅ Reset Due Date
                } else {
                    selectedPropertyName = selPropName;
                    propertyId = propertyMap.get(selPropName);

                    // ✅ Fetch due date first, then load units
                    fetchDueDate(propertyId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void loadUnitsFromDB(String propertyId) {
        if (propertyId == null || propertyId.isEmpty()) return;

        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(propertyDoc -> {
                    if (propertyDoc.exists()) {
                        selectedPropertyName = propertyDoc.getString("name");
                        selectedPropertyAddress = propertyDoc.getString("address");

                        final Double electricityRate = propertyDoc.getDouble("electricityRate") != null ? propertyDoc.getDouble("electricityRate") : 0.0;
                        final Double minElectricPrice = propertyDoc.getDouble("minElectricPrice") != null ? propertyDoc.getDouble("minElectricPrice") : 0.0;
                        final Double waterRate = propertyDoc.getDouble("waterRate") != null ? propertyDoc.getDouble("waterRate") : 0.0;
                        final Double minWaterPrice = propertyDoc.getDouble("minWaterPrice") != null ? propertyDoc.getDouble("minWaterPrice") : 0.0;

                        db.collection("tenants")
                                .whereEqualTo("property", propertyId)
                                .get()
                                .addOnSuccessListener(tenantSnapshots -> {
                                    unitBillList.clear();
                                    if (tenantSnapshots.isEmpty()) {
                                        showToast("No units found in this property.");
                                        unitBillAdapter.notifyDataSetChanged();
                                        return;
                                    }

                                    for (DocumentSnapshot tenantDoc : tenantSnapshots) {
                                        UnitBill unitBill = new UnitBill();
                                        unitBill.setUnit(tenantDoc.getString("unit"));
                                        unitBill.setTenantName(tenantDoc.getString("first_name") + " " + tenantDoc.getString("last_name"));
                                        Double rentAmount = tenantDoc.getDouble("rentAmount");
                                        unitBill.setRentAmount(rentAmount != null ? rentAmount : 0.0);
                                        unitBill.setPropertyId(propertyId);

                                        // Fetch previous readings
                                        unitBill.setPrevElectricReading(
                                                tenantDoc.contains("electric_meter_reading") ?
                                                        tenantDoc.getDouble("electric_meter_reading") : 0.0);

                                        unitBill.setPrevWaterReading(
                                                tenantDoc.contains("water_meter_reading") ?
                                                        tenantDoc.getDouble("water_meter_reading") : 0.0);

                                        // Fetch CURRENT readings (Make sure this field exists in Firestore)
                                        unitBill.setCurrentElectricReading(
                                                tenantDoc.contains("currentElectricReading") ?
                                                        tenantDoc.getDouble("currentElectricReading") : unitBill.getPrevElectricReading());

                                        unitBill.setCurrentWaterReading(
                                                tenantDoc.contains("currentWaterReading") ?
                                                        tenantDoc.getDouble("currentWaterReading") : unitBill.getPrevWaterReading());

                                        // Assign rates
                                        unitBill.setElectricityRate(electricityRate);
                                        unitBill.setMinElectricPrice(minElectricPrice);
                                        unitBill.setWaterRate(waterRate);
                                        unitBill.setMinWaterPrice(minWaterPrice);

                                        unitBillList.add(unitBill);
                                    }

                                    Log.d("UnitBillAdapter", "Loaded " + unitBillList.size() + " units");
                                    unitBillAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> showToast("Error loading units: " + e.getMessage()));
                    } else {
                        showToast("Property not found.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error loading property: " + e.getMessage()));
    }

    private void fetchDueDate(String propertyId) {
        if (propertyId == null || propertyId.isEmpty()) {
            dueDateTextView.setText("Due Date: -");
            return;
        }

        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("dueDate")) {
                        int dueDate = documentSnapshot.getLong("dueDate").intValue();
                        dueDateTextView.setText("Due Date: " + dueDate);
                    } else {
                        dueDateTextView.setText("Due Date: Not Set");
                    }

                    // ✅ Ensure units are loaded after due date is fetched
                    loadUnitsFromDB(propertyId);

                })
                .addOnFailureListener(e -> {
                    dueDateTextView.setText("Due Date: Error");
                    loadUnitsFromDB(propertyId); // ✅ Load units even if due date fails
                });
    }



    private void setupViewSummaryButton() {
        buttonViewSummary.setOnClickListener(v -> {
            if ("Select Property".equals(spinnerProperty.getSelectedItem().toString())) {
                showToast("Please select a property before viewing summary.");
                return;
            }

            if (unitBillList.isEmpty()) {
                showToast("No unit data available. Please wait until data loads.");
                return;
            }

            Log.d("CreateBillActivity", "Passing " + unitBillList.size() + " units to SummaryActivity");

            Intent intent = new Intent(CreateBillActivity.this, SummaryActivity.class);
            intent.putExtra("unitBillList", (Serializable) unitBillList);
            intent.putExtra("propertyName", selectedPropertyName);
            intent.putExtra("propertyAddress", selectedPropertyAddress);
            intent.putExtra("billingPeriod", billingPeriod);
            intent.putExtra("propertyId", propertyId);
            startActivity(intent);
        });
    }


    private void setupCancelButton() {
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getMonthName(int monthIndex) {
        return new DateFormatSymbols().getMonths()[monthIndex];
    }
}
