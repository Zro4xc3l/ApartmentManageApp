package com.example.apartmentmanageapp.ui.bills;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.UnitBillAdapter;
import com.example.apartmentmanageapp.model.UnitBill;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateBillActivity extends AppCompatActivity {

    private Spinner spinnerProperty;
    private RecyclerView recyclerView;
    private UnitBillAdapter unitBillAdapter;
    private List<UnitBill> unitBillList;
    private FirebaseFirestore db;
    private Map<String, String> propertyMap; // Maps Property Name -> Property ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        initializeUI();
        setupPropertySpinner();
    }

    private void initializeUI() {
        spinnerProperty = findViewById(R.id.spinner_property);
        recyclerView = findViewById(R.id.recyclerView_unit_bills);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        unitBillList = new ArrayList<>();
        unitBillAdapter = new UnitBillAdapter(this, unitBillList);
        recyclerView.setAdapter(unitBillAdapter);

        db = FirebaseFirestore.getInstance();
        propertyMap = new HashMap<>();
    }

    private void setupPropertySpinner() {
        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> propertyNames = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String propertyName = doc.getString("name");
                        if (propertyName != null) {
                            propertyNames.add(propertyName);
                            propertyMap.put(propertyName, doc.getId());
                        }
                    }

                    if (!propertyNames.isEmpty()) {
                        setupSpinnerAdapter(propertyNames);
                    } else {
                        showToast("No properties found.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error loading properties: " + e.getMessage()));
    }

    private void setupSpinnerAdapter(List<String> propertyNames) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, propertyNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProperty.setAdapter(spinnerAdapter);

        spinnerProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPropertyName = parent.getItemAtPosition(position).toString();
                loadUnitsFromDB(propertyMap.get(selectedPropertyName));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void loadUnitsFromDB(String propertyId) {
        if (propertyId == null || propertyId.isEmpty()) {
            return;
        }

        // First, fetch property details to retrieve rates
        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(propertyDoc -> {
                    if (propertyDoc.exists()) {
                        final Double electricityRate = propertyDoc.getDouble("electricityRate") != null ? propertyDoc.getDouble("electricityRate") : 0.0;
                        final Double minElectricPrice = propertyDoc.getDouble("minElectricPrice") != null ? propertyDoc.getDouble("minElectricPrice") : 0.0;
                        final Double waterRate = propertyDoc.getDouble("waterRate") != null ? propertyDoc.getDouble("waterRate") : 0.0;
                        final Double minWaterPrice = propertyDoc.getDouble("minWaterPrice") != null ? propertyDoc.getDouble("minWaterPrice") : 0.0;

                        // Now query the units subcollection
                        db.collection("properties")
                                .document(propertyId)
                                .collection("units")
                                .whereEqualTo("occupied", true)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    unitBillList.clear();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                        UnitBill unitBill = doc.toObject(UnitBill.class);
                                        if (unitBill != null) {
                                            // Set property-specific values for calculations using final variables
                                            unitBill.setElectricityRate(electricityRate);
                                            unitBill.setMinElectricPrice(minElectricPrice);
                                            unitBill.setWaterRate(waterRate);
                                            unitBill.setMinWaterPrice(minWaterPrice);
                                            unitBillList.add(unitBill);
                                        }
                                    }
                                    Toast.makeText(CreateBillActivity.this, "Fetched " + unitBillList.size() + " units", Toast.LENGTH_SHORT).show();
                                    unitBillAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(CreateBillActivity.this, "Error loading units: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        showToast("Property not found.");
                    }
                })
                .addOnFailureListener(e ->
                        showToast("Error loading property: " + e.getMessage())
                );
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
