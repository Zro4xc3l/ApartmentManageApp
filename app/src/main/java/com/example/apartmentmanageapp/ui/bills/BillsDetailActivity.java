package com.example.apartmentmanageapp.ui.bills;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.BillUnitDetailsAdapter;
import com.example.apartmentmanageapp.model.Bill;
import com.example.apartmentmanageapp.model.UnitBill;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class BillsDetailActivity extends AppCompatActivity {

    private static final String TAG = "BillsDetailActivity";

    private ImageButton btnBackTop;
    private TextView tvDetailPropertyName, tvDetailPropertyAddress, tvDetailBillingPeriod;
    private TextView tvDetailTotalRent, tvDetailTotalElectric, tvDetailTotalWater;
    private TextView tvDetailTotalAdditional, tvDetailTotalServiceFee, tvDetailGrandTotal, tvDetailBillStatus;
    private RecyclerView rvUnitDetails;
    private Button btnMarkAsPaid;

    private FirebaseFirestore db;
    private String billId;
    private Bill bill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills_detail);

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        btnBackTop = findViewById(R.id.btn_back_top);
        tvDetailPropertyName = findViewById(R.id.tv_detail_property_name);
        tvDetailPropertyAddress = findViewById(R.id.tv_detail_property_address);
        tvDetailBillingPeriod = findViewById(R.id.tv_detail_billing_period);
        tvDetailTotalRent = findViewById(R.id.tv_detail_total_rent);
        tvDetailTotalElectric = findViewById(R.id.tv_detail_total_electric);
        tvDetailTotalWater = findViewById(R.id.tv_detail_total_water);
        tvDetailTotalAdditional = findViewById(R.id.tv_detail_total_additional);
        tvDetailTotalServiceFee = findViewById(R.id.tv_detail_total_service_fee); // Added service fee
        tvDetailGrandTotal = findViewById(R.id.tv_detail_grand_total);
        tvDetailBillStatus = findViewById(R.id.tv_detail_bill_status);
        rvUnitDetails = findViewById(R.id.rv_unit_details);
        btnMarkAsPaid = findViewById(R.id.btn_mark_as_paid);

        btnBackTop.setOnClickListener(v -> finish());

        // Retrieve bill ID from intent
        billId = getIntent().getStringExtra("billId");
        if (billId == null || billId.isEmpty()) {
            Toast.makeText(this, "Error: Bill not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading bill details for ID: " + billId);
        loadBillDetails();
    }

    private void loadBillDetails() {
        db.collection("bills").document(billId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bill = documentSnapshot.toObject(Bill.class);
                        if (bill != null) {
                            bill.setId(billId);
                            Log.d(TAG, "Bill loaded: " + bill.toString());

                            populateBillDetails();
                            loadUnitBills(); // Now correctly retrieving from the main document
                        }
                    } else {
                        Toast.makeText(this, "Bill not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading bill details", e);
                    Toast.makeText(this, "Failed to load bill.", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateBillDetails() {
        tvDetailPropertyName.setText("Property: " + bill.getPropertyName());
        tvDetailPropertyAddress.setText("Address: " + bill.getPropertyAddress());
        tvDetailBillingPeriod.setText("Billing Period: " + bill.getBillingPeriod());
        tvDetailTotalRent.setText("Total Rent: ฿" + String.format("%.2f", bill.getTotalRent()));
        tvDetailTotalElectric.setText("Total Electric: ฿" + String.format("%.2f", bill.getTotalElectric()));
        tvDetailTotalWater.setText("Total Water: ฿" + String.format("%.2f", bill.getTotalWater()));
        tvDetailTotalAdditional.setText("Total Additional: ฿" + String.format("%.2f", bill.getTotalAdditional()));

        // Display service fee
        tvDetailTotalServiceFee.setText("Service Fee: ฿" + String.format("%.2f", bill.getServiceFee()));

        tvDetailGrandTotal.setText("Grand Total: ฿" + String.format("%.2f", bill.getGrandTotal()));
        tvDetailBillStatus.setText("Status: " + bill.getBillStatus());

        rvUnitDetails.setVisibility(View.VISIBLE); // Ensure RecyclerView is visible

        // Enable "Mark as Paid" button only if the bill is unpaid
        if ("Unpaid".equalsIgnoreCase(bill.getBillStatus())) {
            btnMarkAsPaid.setVisibility(View.VISIBLE);
            btnMarkAsPaid.setOnClickListener(v -> markBillAsPaid());
        } else {
            btnMarkAsPaid.setVisibility(View.GONE);
        }
    }

    private void loadUnitBills() {
        if (bill == null || bill.getUnitBills() == null || bill.getUnitBills().isEmpty()) {
            Log.e(TAG, "No unit bills found for this bill.");
            return;
        }

        Log.d(TAG, "Unit Bills: " + bill.getUnitBills()); // Debugging

        setupRecyclerView(bill.getUnitBills());
    }

    private void setupRecyclerView(List<UnitBill> unitBills) {
        BillUnitDetailsAdapter unitAdapter = new BillUnitDetailsAdapter(this, unitBills);
        rvUnitDetails.setLayoutManager(new LinearLayoutManager(this));
        rvUnitDetails.setAdapter(unitAdapter);
    }

    private void markBillAsPaid() {
        if (bill == null) return;

        WriteBatch batch = db.batch();
        DocumentReference billRef = db.collection("bills").document(billId);

        // Update the bill status in Firestore
        batch.update(billRef, "billStatus", "Paid");

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Bill marked as Paid.", Toast.LENGTH_SHORT).show();
            tvDetailBillStatus.setText("Status: Paid");
            btnMarkAsPaid.setVisibility(View.GONE); // Hide button after marking as paid
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating bill status", e);
            Toast.makeText(this, "Failed to update bill status.", Toast.LENGTH_SHORT).show();
        });
    }
}
