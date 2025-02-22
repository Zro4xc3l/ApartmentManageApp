package com.example.apartmentmanageapp.ui.bills;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.SummaryAdapter;
import com.example.apartmentmanageapp.model.UnitBill;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SummaryAdapter adapter;
    private List<UnitBill> unitBillList;

    // Overall totals TextViews
    private TextView propertyDetailsTextView;
    private TextView totalRentTextView;
    private TextView totalElectricTextView;
    private TextView totalWaterTextView;
    private TextView totalAdditionalTextView;
    private TextView grandTotalTextView;

    // Buttons
    private Button buttonBack;
    private Button buttonSaveBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Retrieve UnitBill list from intent extras
        Serializable serializableList = getIntent().getSerializableExtra("unitBillList");
        unitBillList = serializableList != null ? (List<UnitBill>) serializableList : new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view_unit_summaries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SummaryAdapter(this, unitBillList);
        recyclerView.setAdapter(adapter);

        // Initialize TextViews
        propertyDetailsTextView = findViewById(R.id.property_details);
        totalRentTextView = findViewById(R.id.total_rent);
        totalElectricTextView = findViewById(R.id.total_electric);
        totalWaterTextView = findViewById(R.id.total_water);
        totalAdditionalTextView = findViewById(R.id.total_additional);
        grandTotalTextView = findViewById(R.id.grand_total);

        // Property details
        String propName = getIntent().getStringExtra("propertyName");
        String propAddress = getIntent().getStringExtra("propertyAddress");
        if (propName != null && propAddress != null) {
            propertyDetailsTextView.setText("Property Name: " + propName + "\nAddress: " + propAddress);
        }

        // Buttons
        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> finish());

        buttonSaveBill = findViewById(R.id.button_save_bill);
        buttonSaveBill.setOnClickListener(v -> saveBillToDatabase());

        // Calculate totals
        updateOverallTotals();
    }

    /**
     * Calculates totals and updates the summary UI.
     */
    private void updateOverallTotals() {
        double totalRent = 0, totalElectric = 0, totalWater = 0, totalAdditional = 0;
        double totalElectricUsage = 0, totalWaterUsage = 0;

        for (UnitBill unit : unitBillList) {
            double prevElectric = unit.getPrevElectricReading();
            double currentElectric = unit.getCurrentElectricReading() != null ? unit.getCurrentElectricReading() : prevElectric;
            double electricUsage = Math.max(0, currentElectric - prevElectric);
            double electricCost = Math.max(electricUsage * unit.getElectricityRate(), unit.getMinElectricPrice());

            double prevWater = unit.getPrevWaterReading();
            double currentWater = unit.getCurrentWaterReading() != null ? unit.getCurrentWaterReading() : prevWater;
            double waterUsage = Math.max(0, currentWater - prevWater);
            double waterCost = Math.max(waterUsage * unit.getWaterRate(), unit.getMinWaterPrice());

            double additionalFee = unit.getAdditionalFee() != null ? unit.getAdditionalFee() : 0.0;

            Log.d("SummaryActivity", "Unit: " + unit.getUnit() +
                    " | Electric Usage: " + electricUsage + " | Water Usage: " + waterUsage +
                    " | Additional Fee: " + additionalFee);

            totalElectricUsage += electricUsage;
            totalElectric += electricCost;
            totalWaterUsage += waterUsage;
            totalWater += waterCost;
            totalRent += unit.getRentAmount();
            totalAdditional += additionalFee;
        }

        double grandTotal = totalRent + totalElectric + totalWater + totalAdditional;

        totalRentTextView.setText("Total Rent: ฿" + String.format("%.2f", totalRent));
        totalElectricTextView.setText("Total Electric: ฿" + String.format("%.2f", totalElectric)
                + " (Usage: " + String.format("%.0f", totalElectricUsage) + " units)");
        totalWaterTextView.setText("Total Water: ฿" + String.format("%.2f", totalWater)
                + " (Usage: " + String.format("%.0f", totalWaterUsage) + " units)");
        totalAdditionalTextView.setText("Total Additional: ฿" + String.format("%.2f", totalAdditional));
        grandTotalTextView.setText("Grand Total: ฿" + String.format("%.2f", grandTotal));
    }

    /**
     * Saves the bill to Firestore.
     */
    private void saveBillToDatabase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(SummaryActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userId = currentUser.getUid();
        final String propertyName = getIntent().getStringExtra("propertyName");
        final String billingPeriodExtra = getIntent().getStringExtra("billingPeriod");
        final String propertyId = getIntent().getStringExtra("propertyId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bills")
                .whereEqualTo("propertyName", propertyName)
                .whereEqualTo("billingPeriod", billingPeriodExtra)
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(SummaryActivity.this, "A bill for " + billingPeriodExtra + " already exists.", Toast.LENGTH_LONG).show();
                    } else {
                        // Prepare bill data
                        Map<String, Object> billData = new HashMap<>();
                        billData.put("ownerId", userId);
                        billData.put("propertyName", propertyName);
                        billData.put("propertyAddress", getIntent().getStringExtra("propertyAddress"));
                        billData.put("billingPeriod", billingPeriodExtra);
                        billData.put("unitBills", unitBillList);
                        billData.put("totalRent", totalRentTextView.getText().toString());
                        billData.put("totalElectric", totalElectricTextView.getText().toString());
                        billData.put("totalWater", totalWaterTextView.getText().toString());
                        billData.put("totalAdditional", totalAdditionalTextView.getText().toString());
                        billData.put("grandTotal", grandTotalTextView.getText().toString());
                        billData.put("billStatus", "Unpaid");
                        billData.put("timestamp", System.currentTimeMillis());

                        db.collection("bills").add(billData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(SummaryActivity.this, "Bill saved successfully.", Toast.LENGTH_SHORT).show();

                                    if (propertyId != null && !propertyId.isEmpty()) {
                                        WriteBatch batch = db.batch();
                                        for (UnitBill unit : unitBillList) {
                                            batch.update(
                                                    db.collection("tenants").document(unit.getUnit()),
                                                    "electricMeter", unit.getCurrentElectricReading(),
                                                    "waterMeter", unit.getCurrentWaterReading()
                                            );
                                        }
                                        batch.commit().addOnSuccessListener(aVoid -> finish())
                                                .addOnFailureListener(e -> Toast.makeText(SummaryActivity.this, "Failed to update unit meters.", Toast.LENGTH_SHORT).show());
                                    } else {
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(SummaryActivity.this, "Failed to save bill.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SummaryActivity.this, "Error checking duplicate bill.", Toast.LENGTH_SHORT).show());
    }
}
